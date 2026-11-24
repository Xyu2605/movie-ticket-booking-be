package com.ticket_booking.service.user_security;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.ticket_booking.dto.UserSecurityDto;
import com.ticket_booking.dto.response.FailureResult;
import com.ticket_booking.dto.response.LoginResult;
import com.ticket_booking.entity.User;
import com.ticket_booking.entity.UserSecurity;
import com.ticket_booking.enums.ErrorCode;
import com.ticket_booking.enums.RedisKey;
import com.ticket_booking.exception.ApiException;
import com.ticket_booking.mapper.UserSecurityMapper;
import com.ticket_booking.repository.UserSecurityRepository;
import com.ticket_booking.service.redis.RedisFailureCounterService;
import com.ticket_booking.service.redis.RedisService;
import com.ticket_booking.service.user.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSecurityServiceImpl implements UserSecurityService{

    @Value("${app.security.login}")
    private int maxFailures;

    @Value("${app.security.failure-window-seconds}")
    private int failureWindowSeconds;

    @Value("${app.security.lock-duration-seconds}")
    private int lockDurationSeconds;

    private final UserSecurityRepository userSecurityRepository;
    private final UserSecurityMapper userSecurityMapper;
    private final UserService userService;
    private final RedisService<String, Object, UserSecurityDto> redisService;
    private final RedisFailureCounterService redisFailureCounterService;

    @Override
    public void assertNotLocked(UUID userId){
        Optional<Long> lockedUntilMinutes;
        try{
            lockedUntilMinutes = redisFailureCounterService.findLockedUntil(userId);
        } catch(DataAccessException e){
            log.warn("Redis unavailable, fallback to DB lock check: {}", e.getMessage());
            checkLockInDb(userId);
            return;
        }
        if(lockedUntilMinutes.isPresent() && lockedUntilMinutes.get() > System.currentTimeMillis()){
            long remaining = lockedUntilMinutes.get() - System.currentTimeMillis() / 60000 + 1;
            throw buildLockedException(remaining);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public UserSecurityDto getUserSecurityDto(UUID userId){

        Optional<UserSecurityDto> cachedUserSecurity = getCachedUserSecurity(RedisKey.USER_SECURITY_BY_ID + userId.toString());

        if(cachedUserSecurity.isPresent()){
            return cachedUserSecurity.get();
        }

        UserSecurity userSecurity = userSecurityRepository.findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
        UserSecurityDto dto = userSecurityMapper.toDto(userSecurity);
        setCachedUserSecurity(RedisKey.USER_SECURITY_BY_ID + userId.toString(), dto);
        return dto;
    }

    private void setCachedUserSecurity(String key, UserSecurityDto userSecurityDto){
        if(key == null || userSecurityDto == null){
            return;
        }

        try{
            redisService.set(key, userSecurityDto, Duration.ofMinutes(15));
            log.debug("[Cache SET] key{}: value{}", key, userSecurityDto);
        } catch(Exception e){
            log.warn("[Cache Error] Redis write failed. key={}, error={}", key, e.getMessage());
        }
    }

    private Optional<UserSecurityDto> getCachedUserSecurity(String cacheKey){
        UserSecurityDto userSecurityDto;

        try{
            userSecurityDto = redisService.get(cacheKey);
        } catch(Exception e){
            log.warn("[Cache Error] Redis read failed. key={}, error={}", cacheKey, e.getMessage());
            return Optional.empty();
        }

        if(userSecurityDto == null){
            log.debug("[Cache MISS] {}", cacheKey);
            return Optional.empty();
        }

        try{
            log.debug("[Cache HIT] {}", cacheKey);
            return Optional.of(userSecurityDto);
        } catch(Exception e){
            log.warn("[Cache Error] Corrupt entry, evicting. key={}, error={}", cacheKey, e.getMessage());
            redisService.delete(cacheKey);
            return Optional.empty();
        }
    }

    @Override
    public UserSecurity getUserSecurity(UUID userId){
        return userSecurityRepository.findById(userId).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    private void checkLockInDb(UUID userId){
        UserSecurityDto userSecurityDto = getUserSecurityDto(userId);
        if(userSecurityDto.lockedUntil() != null && userSecurityDto.lockedUntil().isAfter(Instant.now())){
            Duration remaining = Duration.between(Instant.now(), userSecurityDto.lockedUntil());
            throw buildLockedException(remaining.toMinutes());
        }
    }
    
    @Override
    @Transactional
    public void recordLoginFailures(UUID userId, String ip){
        UserSecurity userSecurity = getUserSecurity(userId);
        userSecurity.setLastFailedAt(Instant.now());
        userSecurity.setLastFailedIp(ip);

        try{
            FailureResult result = redisFailureCounterService.recordFailure(userId);
            log.info("Result: {}", result);
            if(result.locked()){
                Instant lockedUntil = Instant.ofEpochMilli(result.lockedUntil());
                log.warn("User {} locked until {}", userId, lockedUntil);
                userSecurity.setLockedUntil(lockedUntil);
            }
        } catch(DataAccessException e){
            log.warn("Redis unavailable, counting in DB: {}", e.getMessage());
            recordLoginFailureToDb(userSecurity, ip);
        }
        userSecurityRepository.save(userSecurity);
        evictAfterCommit(userId);
    }



    private void recordLoginFailureToDb(UserSecurity userSecurity, String ip) {
        Instant now = Instant.now();

        boolean inWindow = userSecurity.getWindowStartedAt() != null && 
                userSecurity.getWindowStartedAt()
                .plusSeconds(failureWindowSeconds)
                        .isAfter(now);

        // Nếu vẫn trong khoảng thời gian giới hạn
        if (inWindow) {
            userSecurity.setFailedAttempts(
                    userSecurity.getFailedAttempts() + 1
            );
        } else {
            // Nếu đã hết window thì bắt đầu lại
            userSecurity.setFailedAttempts(1);
            userSecurity.setWindowStartedAt(now);
        }

        // Nếu số lần đăng nhập sai đạt giới hạn
        if (userSecurity.getFailedAttempts() >= maxFailures) {
            userSecurity.setLockedUntil(
                    now.plusSeconds(lockDurationSeconds)
            );

            log.warn(
                    "User {} LOCKED for {} seconds",
                    userSecurity.getUserId(),
                    lockDurationSeconds
            );
        }

        // Cập nhật cache
        setCachedUserSecurity(
                RedisKey.USER_SECURITY_BY_ID + userSecurity.getUserId(),
                userSecurityMapper.toDto(userSecurity)
        );
    }


    @Override
    @Transactional
    public void resetLoginFailures(UUID userId){
        try{
            redisFailureCounterService.reset(userId);
        } catch(Exception e){
            log.warn("Redis reset failed: {}", e.getMessage());
        }

        Optional<UserSecurity> userSecurity = userSecurityRepository.findById(userId);
        if(userSecurity.isPresent()){
            UserSecurity us = userSecurity.get();
            if(us.getLockedUntil() != null || us.getLastFailedIp() != null){
                us.setFailedAttempts(null);
                us.setWindowStartedAt(null);
                us.setLastFailedAt(null);
                us.setLastFailedIp(null);
                userSecurityRepository.save(us);
                setCachedUserSecurity(RedisKey.USER_SECURITY_BY_ID + userId, userSecurityMapper.toDto(us));
                log.debug("Reset failures for user {}", userId);
            }
        }
    }

    private ApiException buildLockedException(long remainingMinutes) {
        return new ApiException(ErrorCode.ACCOUNT_LOCKED,
            "Tài khoản tạm khóa do đăng nhập sai quá nhiều lần. " +
            "Vui lòng thử lại sau " + remainingMinutes + " phút.");
    }

    private void evictAfterCommit(UUID userId) {
        String key = RedisKey.USER_SECURITY_BY_ID + userId;
        // kiem tran transction con hoatt dong khon?
        if (!TransactionSynchronizationManager.isSynchronizationActive() || !TransactionSynchronizationManager.isActualTransactionActive()) {
            tryEvict(key);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                tryEvict(key);
            }
        });
    }

    private void tryEvict(String key) {
        try {
            redisService.delete(key);
        } catch (DataAccessException e) {
            log.warn("Cache evict failed: key={}, error={}", key, e.getMessage());
        }
    }
}

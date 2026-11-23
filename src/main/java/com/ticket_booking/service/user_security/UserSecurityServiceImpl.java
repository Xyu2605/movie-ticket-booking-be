package com.ticket_booking.service.user_security;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.dto.UserSecurityDto;
import com.ticket_booking.dto.response.FailureResult;
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

    private static final int MAX_FAILURES = 5;
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(5);
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserSecurityRepository userSecurityRepository;
    private final UserSecurityMapper userSecurityMapper;
    private final UserService userService;
    private final RedisService<String, Object, UserSecurityDto> redisService;
    private final RedisFailureCounterService redisFailureCounterService;

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
    public boolean isLocked(UUID userId){
        return userSecurityRepository.findById(userId)
                .map(user -> user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now()))
                .orElse(false);
    }

    @Override
    public long getRemainingLockMinutes(UUID userId){
        Optional<UserSecurity>  userSecurity = userSecurityRepository.findById(userId);
        if(userSecurity.isPresent() && userSecurity.get().isLocked()){
            return userSecurity.get().getRemainingLockMinutes();
        } else{
            return 0L;
        }
    }

    @Override
    public UserSecurity getUserSecurity(UUID userId){
        return userSecurityRepository.findById(userId).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND));
    }
    
    @Override
    public void recordLoginFailure(UUID userId, String ip){
        UserSecurity userSecurity = getOrCreate(userId);
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
        } catch(Exception e){
            log.warn("Redis down, change to record db!");
            recordLoginFailureToDb(userSecurity, ip);
        }
        userSecurityRepository.save(userSecurity);
    }

    private void recordLoginFailureToDb(UserSecurity userSecurity, String ip){
        Instant now = Instant.now();

        boolean inWindow = userSecurity.getWindowStartedAt() != null && userSecurity.getWindowStartedAt().plus(FAILURE_WINDOW).isAfter(now);
        if(inWindow){
            userSecurity.setFailedAttempts(userSecurity.getFailedAttempts() + 1);
        } else{
            userSecurity.setFailedAttempts(1);
            userSecurity.setWindowStartedAt(now);
        }

        if(userSecurity.getFailedAttempts() <= MAX_FAILURES){
            userSecurity.setLockedUntil(now.plus(LOCK_DURATION));
            userSecurity.setFailedAttempts(0);
            userSecurity.setWindowStartedAt(null);
            log.warn("User {} LOCKED for {} minutes", userSecurity.getUserId(), LOCK_DURATION.toString());
        }
    }

    public void unlock(UUID userId){
        UserSecurity userSecurity = getUserSecurity(userId);
        userSecurity.setLockedUntil(null);
        userSecurityRepository.save(userSecurity);
    }

    private UserSecurity getOrCreate(UUID userId){
        Optional<UserSecurity> existedUser = userSecurityRepository.findById(userId);
        if(existedUser.isPresent()){
            return existedUser.get();
        }else{
            UserSecurity userSecurity = new UserSecurity();
            User user = userService.getUserEntityById(userId);
            userSecurity.setUser(user);
            userSecurityRepository.save(userSecurity);
            return userSecurity;
        }
    }
}

package com.ticket_booking.service.user;

import java.io.StringReader;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.checkerframework.checker.units.qual.K;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket_booking.dto.UserDto;
import com.ticket_booking.dto.request.CreateUserRequest;
import com.ticket_booking.dto.request.UpdateUserNameRequest;
import com.ticket_booking.dto.request.UpdateUserPasswordRequest;
import com.ticket_booking.entity.Role;
import com.ticket_booking.entity.User;
import com.ticket_booking.enums.AuthProvider;
import com.ticket_booking.enums.ErrorCode;
import com.ticket_booking.enums.RedisKey;
import com.ticket_booking.enums.RoleType;
import com.ticket_booking.enums.UserState;
import com.ticket_booking.exception.ApiException;
import com.ticket_booking.mapper.UserMapper;
import com.ticket_booking.repository.RoleRepository;
import com.ticket_booking.repository.UserRepository;
import com.ticket_booking.service.jwt.JwtServiceImpl;
import com.ticket_booking.service.redis.RedisService;
import com.ticket_booking.utils.ratelimit.RateLimitInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@RequiredArgsConstructor 
@Slf4j 
public class UserServiceImpl implements UserService{
    
    private final UserRepository userRepository;
    private final RedisService<String, Object, String> redisService;
    private final JwtServiceImpl jwtService;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityByEmail(String email){
        User user = userRepository.findByEmail(normalizeEmail(email))
                    .orElseThrow(()-> new ApiException(ErrorCode.USER_NOT_FOUND));

        log.info("User found with email: {}", email);
        return user;
    }

    @Override
    public UserDto getUserDtoByEmail(String email){
        return findUserDtoByNormalizeEmail(normalizeEmail(email))
                .orElseThrow(()-> new ApiException(ErrorCode.USER_NOT_FOUND));
    }

    @Override 
    public UserDto getUserDtoById(UUID id){
        Optional<UserDto> cachedUser = getCachedUserById(id);
        if(cachedUser.isPresent()){
            return cachedUser.get();
        }

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
        UserDto userDto = userMapper.toDto(user);
        log.info("User found: {}", userDto.id());

        setCacheUserDtoById(id, userDto);

        return userDto;
    }

    @Override
    @Transactional(readOnly = true)
    public User getUserEntityById(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(()-> new ApiException(ErrorCode.USER_NOT_FOUND));
        log.info("Find user in DB: {}", id);
        return user;
    }

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequest request){

        Optional<UserDto> existingUserOpt = findUserDtoByNormalizeEmail(request.email());
        
        UserDto resultUser;

        if(existingUserOpt.isPresent()){

            UserDto existingUser = existingUserOpt.get();
            if(existingUser.isActive()){
                log.warn("User already existed with email: {}", existingUser.email());
                throw new ApiException(ErrorCode.USER_ALREADY_EXISTED);
            } 
            
            log.warn("User with email: {} has been banned!", existingUser.email());
            throw new ApiException(ErrorCode.USER_BANNED);
        } else{
            
            resultUser = createNewUser(request);
        }
        // Cache sau commit (tránh stale nếu rollback)
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    setCacheUserDtoByEmail(resultUser.email(), resultUser);  
                }
            }
        );

        return resultUser;
    }

    @Override
    @Transactional
    public UserDto updateUserName(UUID id, UpdateUserNameRequest request){
        
        if (request.fullName() == null || request.fullName().isBlank()) {
            throw new ApiException(ErrorCode.MALFORMED_REQUEST_BODY);
        }
        
        String newFullName = request.fullName().trim();
        if (newFullName.length() > 50) {
            throw new ApiException(ErrorCode.MALFORMED_REQUEST_BODY);
        }

        User user = getUserEntityById(id);
        user.setFullName(newFullName);
        User savedUser = userRepository.save(user);
        log.info("Updated user {} fullName to: {}", id, newFullName);
        UserDto userDto = userMapper.toDto(savedUser);

        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override 
                public void afterCommit(){
                    safeDeleteKey(RedisKey.USER_BY_EMAIL + user.getEmail());
                    safeDeleteKey(RedisKey.USER_BY_ID + id.toString());

                    setCacheUserDtoByEmail(user.getEmail(), userDto);
                    setCacheUserDtoById(user.getId(), userDto);
                }
            }
        );

        return userDto;
    }

    @Override
    public void updateUserPassword(UUID id, UpdateUserPasswordRequest request){
        
        if (request == null 
            || request.oldPassword() == null || request.oldPassword().isBlank()
            || request.newPassword() == null || request.newPassword().isBlank()
            || request.confirmNewPassword() == null || request.confirmNewPassword().isBlank()
        ) {
            throw new ApiException(ErrorCode.MALFORMED_REQUEST_BODY);
        }

        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR, 
                "Mật khẩu xác nhận không khớp");
        }

        if (request.oldPassword().equals(request.newPassword())) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
            "Mật khẩu mới phải khác mật khẩu cũ");
        }

        if (request.newPassword().length() < 8 || request.newPassword().length() > 255) {
            throw new ApiException(ErrorCode.VALIDATION_ERROR,
                "Mật khẩu phải từ 8-255 ký tự");
        }

        User user = getUserEntityById(id);

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            log.warn("Invalid old password for user {}", id);
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        log.info("User {} changed password successfully", id);

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("User {} change password success:", user.getId());

        // UserDto userDto = userMapper.toDto(savedUser);
        // TransactionSynchronizationManager.registerSynchronization(
        //     new TransactionSynchronization() {
        //         @Override 
        //         public void afterCommit(){
        //             safeDeleteKey(RedisKey.USER_BY_EMAIL + user.getEmail());
        //             safeDeleteKey(RedisKey.USER_BY_ID + id.toString());

        //             setCacheUserDtoByEmail(user.getEmail(), userDto);
        //             setCacheUserDtoById(user.getId(), userDto);
        //         }
        //     }
        // );
        
    }

    @Override
    public User getAuthenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return getUserEntityByEmail(email);
    }

    private Optional<UserDto> findUserDtoByNormalizeEmail(String email){
        Optional<UserDto> cachedUser =  getCacheUserDtoByEmail(email);
        if(cachedUser.isPresent()){
            return cachedUser;
        }

        return userRepository.findByEmail(email)
            .map(user -> {
                UserDto dto = userMapper.toDto(user);
                log.info("User found: {}", dto.id());
                setCacheUserDtoByEmail(email, dto);
                return dto;
            }
        );
    }


    private UserDto createNewUser(CreateUserRequest request){
        log.info("Create new user for email: {}", request.email());
        Role defaultRole = roleRepository.findByType(RoleType.USER)
                            .orElseThrow(() -> new ApiException( ErrorCode.RESOURCE_NOT_FOUND,"Default role User not found. Please run role seeder!"));
        
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        
        User newUser = new User();
        newUser.setFullName(request.fullName());
        newUser.setEmail(normalizedEmail);
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setAuthProvider(AuthProvider.LOCAL);
        newUser.setRole(defaultRole);
        newUser.setActive(true);

        User savedUser = userRepository.save(newUser);
        log.info("Created new user {} with role {}", savedUser.getId(), defaultRole.getName());

        return userMapper.toDto(savedUser);
    }
    
    private void setCacheUserDtoByEmail(String email, UserDto userDto){
        if( email == null || userDto == null){
            return;
        }
        
        String cacheKey = RedisKey.USER_BY_EMAIL + email;
        setCachedUserDto(cacheKey, userDto);
    }

    private Optional<UserDto> getCacheUserDtoByEmail(String email){
        String cacheKey = RedisKey.USER_BY_EMAIL + email;
        Optional<UserDto> cacheUser = getCachedUserDto(cacheKey);

        if(cacheUser.isPresent()){
            return Optional.of(cacheUser.get());
        }

        return Optional.empty();
    }

    private void setCacheUserDtoById(UUID id, UserDto userDto){
        if( id == null || userDto == null){
            return;
        }     
        
        String cacheKey = RedisKey.USER_BY_ID + id.toString();
        setCachedUserDto(cacheKey, userDto);
    }

    private Optional<UserDto> getCachedUserById(UUID id){
        String cacheKey  = RedisKey.USER_BY_ID + id.toString();
        Optional<UserDto> cacheUser = getCachedUserDto(cacheKey);

        if(cacheUser.isPresent()){
            return Optional.of(cacheUser.get());
        }

        return Optional.empty();
    }

    private Optional<UserDto> getCachedUserDto(String cacheKey){
        String cachedUser;

        try{
            cachedUser = redisService.get(cacheKey);
        } catch(Exception e){
            log.warn("[Cache Error] Redis read failed. key={}, error={}", cacheKey, e.getMessage());
            return Optional.empty();
        }

        if(cachedUser == null){
            log.debug("[Cache MISS] {}", cacheKey);
            return Optional.empty();
        }

        try{
            UserDto userDto = objectMapper.readValue(cachedUser, UserDto.class);
            log.debug("[Cache HIT] {}", cacheKey);
            return Optional.of(userDto);
        } catch(JsonProcessingException e){
            // Dữ liệu hỏng hoặc đổi schema: xóa để lần sau ghi lại đúng
            log.warn("[Cache Error] Corrupt entry, evicting. key={}, error={}", cacheKey, e.getMessage());
            safeDeleteKey(cacheKey);
            return Optional.empty();            
        }
    }

    public void safeDeleteKey(String key) {
        try{
            redisService.delete(key);
            log.debug("[DELETE] key{}", key);
        } catch(Exception e){
            log.warn("[Cache Error] Redis delete failed. key={}, error={}", key, e.getMessage());
        } 
    }

    private void setCachedUserDto(String key, UserDto userDto){
        try {
            String value = objectMapper.writeValueAsString(userDto);
            redisService.set(key, value, Duration.ofMinutes(15));
            log.debug("[Cache SET] key{}: value{}", key, value);
        } catch (Exception e) {
            log.warn("[Cache Error] Redis write failed. key={}, error={}", key, e.getMessage());
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

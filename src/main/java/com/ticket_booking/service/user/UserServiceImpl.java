package com.ticket_booking.service.user;

import java.io.StringReader;
import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket_booking.dto.UserDto;
import com.ticket_booking.dto.request.CreateUserRequest;
import com.ticket_booking.dto.response.UserAuthCache;
import com.ticket_booking.entity.Role;
import com.ticket_booking.entity.User;
import com.ticket_booking.enums.AuthProvider;
import com.ticket_booking.enums.ErrorCode;
import com.ticket_booking.enums.RedisKey;
import com.ticket_booking.enums.RoleType;
import com.ticket_booking.enums.UserState;
import com.ticket_booking.exception.ApiException;
import com.ticket_booking.exception.ResourceNotFoundException;
import com.ticket_booking.mapper.UserMapper;
import com.ticket_booking.repository.RoleRepository;
import com.ticket_booking.repository.UserRepository;
import com.ticket_booking.service.jwt.JwtService;
import com.ticket_booking.service.redis.RedisService;
import com.ticket_booking.utils.ratelimit.RateLimitInterceptor;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service 
@RequiredArgsConstructor 
@Slf4j 
public class UserServiceImpl implements UserService{
    
    private final UserRepository userRepository;
    private final RedisService<String, Object, String> redisService;
    private final JwtService jwtService;
    private final RateLimitInterceptor rateLimitInterceptor;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public UserDto createUser(CreateUserRequest request){

        Optional<User> existingUserOpt = findUserEntityByNormalizedEmail(request.email(), false);
        
        UserDto resultUser;

        if(existingUserOpt.isPresent()){

            User existingUser = existingUserOpt.get();

            if(existingUser.isActive()){
                log.warn("User already existed with email: {}", existingUser.getEmail());
                throw new ApiException(ErrorCode.USER_ALREADY_EXISTED);
            } 
            
            log.warn("User with email: {} has been banned!", existingUser.getEmail());
            throw new ApiException(ErrorCode.USER_BANNED);
        } else{
            
            resultUser = createNewUser(request);
        }
        // Cache sau commit (tránh stale nếu rollback)
        TransactionSynchronizationManager.registerSynchronization(
            new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    setCacheUserDto(resultUser.email(), resultUser);  
                }
            }
        );

        return resultUser;
    }

    private Optional<User> findUserEntityByNormalizedEmail(String email, boolean activeOnly){
        // boolean cachePresent = getCachedUserDtoByEmail(email).isPresent();
        Optional<UserDto> cacheUser = getCachedUserDtoByEmail(email);
        if(cacheUser.isPresent()){
            return Optional.of(userMapper.userDtoToEntity(cacheUser.get()));
        }

        Optional<User> user = activeOnly 
                            ? userRepository.findByEmailAndActiveTrue(email) 
                            : userRepository.findByEmail(email);                            
        if(cacheUser.isPresent() && user.map(User::isActive).orElse(false)){
            user.map(userMapper :: toDto).ifPresent(dto -> setCacheUserDto(email, dto));
        }

        return user;
    }   

    private UserDto createNewUser(CreateUserRequest request){
        log.info("Create new user for email: {}", request.email());
        Role defaultRole = roleRepository.findByType(RoleType.USER)
                            .orElseThrow(() -> new IllegalStateException("Default role User not found. Please run role seeder!"));

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

    private Optional<UserDto> findUserDtoByEmail(String email){
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        Optional<UserDto> cachedUser =  getCachedUserDtoByEmail(normalizedEmail);
        if(cachedUser.isPresent()){
            return cachedUser;
        }

        Optional<User> user = userRepository.findByEmail(normalizedEmail);
        
        UserDto userDto = userMapper.toDto(user.get());
        
        setCacheUserDto(normalizedEmail, userDto);
        
        return Optional.of(userDto);
    }
    
    private void setCacheUserDto(String email, UserDto userDto){
        String cacheKey = RedisKey.USER_BY_EMAIL + email;
        try{
            String value = objectMapper.writeValueAsString(userDto);
            redisService.set(cacheKey, value, Duration.ofMinutes(15)); 
            log.debug("[Cache SET] key{}: value{}", cacheKey, value);
        } catch (Exception e){
            log.warn(
                "[Cache Error] Failed to cache user. key={}, error={}",
                cacheKey,
                e.getMessage()
            );
        }
    }

    private Optional<UserDto> getCachedUserDtoByEmail(String email){

        String cacheKey = RedisKey.USER_BY_EMAIL + email;
        try{
            String cachedUser = redisService.get(cacheKey);
            if(cachedUser == null){
                log.debug("[Cache MISS] {}", cacheKey);
            }

            UserDto userDto = objectMapper.readValue(cachedUser, UserDto.class);
            if (userDto == null) {
                log.warn("[Cache Error] Deserialized null value. key={}", cacheKey);
                return Optional.empty();
            }

            log.debug("[Cache HIT] {}", cacheKey);
            return Optional.of(userDto);

        } catch (Exception e){
            log.warn("[Cache Error] Redis unavailable while reading user cache. key={}, error={}", cacheKey, e.getMessage());
            return Optional.empty();
        }
    }

}

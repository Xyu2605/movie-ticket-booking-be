package com.ticket_booking.service.auth;

import java.time.Duration;
import java.util.Locale;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ticket_booking.enums.ErrorCode;
import com.ticket_booking.enums.RedisKey;
import com.ticket_booking.exception.ApiException;
import com.ticket_booking.service.redis.RedisService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoginAttemptService {
    private static final int MAX_FAILURES = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15); 
    private static final Duration FAILURE_WINDOW = Duration.ofMinutes(5);

    private final RedisService<String,Object, String> redisService;
    
    public void assertNotLocked(String email, String ip){
        if(isOverLimit(ip) || isOverLimit(normalizeEmail(email))){
            throw new ApiException(ErrorCode.TOO_MANY_REQUEST, "Vui lòng thử lại sau " + LOCK_DURATION + " phút!");
        }
    }

    public void recordFailure(String email, String ip){
        increment(RedisKey.LOGIN_FAIL_EMAIL + normalizeEmail(email));
        increment(RedisKey.LOGIN_FAIL_IP + ip);
    }

    private void increment(String key){
        Long count = redisService.increment(key);

        if(count != null && count == 1L){
            redisService.expire(key, FAILURE_WINDOW);
        }
    }

    private boolean isOverLimit(String key){
        String value = redisService.get(key);

        return value != null && Integer.parseInt(value) >= MAX_FAILURES;
    }

    public void reset(String email, String ip){
        redisService.delete(normalizeEmail(email));
        redisService.delete(ip);
    }

    private String normalizeEmail(String email){
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

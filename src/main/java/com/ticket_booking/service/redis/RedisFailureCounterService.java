package com.ticket_booking.service.redis;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.esotericsoftware.minlog.Log;
import com.ticket_booking.dto.response.FailureResult;
import com.ticket_booking.enums.RedisKey;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisFailureCounterService {
    
    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List<Long>> recordFailureScript;

    @Value("${app.security.login.max-failures}")
    private int maxFailures;

    @Value("${app.security.login.failure-window-seconds}")
    private int failureWindowSeconds;

    @Value("${app.security.login.lock-duration-seconds}")
    private int lockDurationSeconds;

    public FailureResult recordFailure(UUID userId){
        String key = RedisKey.RECORD_LOGIN_FAILURES + userId;
        List<Long> result = redisTemplate.execute(recordFailureScript, 
            List.of(key), 
            String.valueOf(maxFailures), 
            String.valueOf(failureWindowSeconds * 1000L), 
            String.valueOf(lockDurationSeconds * 1000L)
        );
        
        // if(result == null || result.size() != 3){
        //     throw new IllegalStateException("Invalid redis response!");
        // }

        return new FailureResult(result.get(0) == 1L, result.get(1).intValue(), result.get(2));
    }

    public Long getLockedUntil(UUID userId){
        String key = RedisKey.RECORD_LOGIN_FAILURES + userId;
        try{
            String value =redisTemplate.opsForHash().get(key, "lockedUntil").toString();
            return value != null ? Long.parseLong(value) : null;
        }catch(Exception e){
            return null;
        }
    }


    public Optional<Long> findLockedUntil(UUID userId){
        Object value = redisTemplate.opsForHash().get(RedisKey.RECORD_LOGIN_FAILURES + userId, "lockedUntil");
        return value == null ? Optional.empty() : Optional.of(Long.parseLong(value.toString()));
    }

    public void reset(UUID userId){
        try{
            redisTemplate.delete(RedisKey.RECORD_LOGIN_FAILURES + userId);
        } catch(Exception e){
            log.warn("Failed to reset: {}", e.getMessage());
        }
    }
}

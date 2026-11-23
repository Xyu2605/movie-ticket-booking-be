package com.ticket_booking.service.redis;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import com.ticket_booking.dto.response.FailureResult;
import com.ticket_booking.enums.RedisKey;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisFailureCounterService {
    
    private final StringRedisTemplate redisTemplate;
    private final RedisScript<List<Long>> recordFailureScript;

    @Value("${app.security.login}")
    private int maxFailures;

    @Value("${app.security.failure-window-seconds}")
    private int failureWindowSeconds;

    @Value("${app.security.lock-duration-seconds}")
    private int lockDurationSeconds;

    public FailureResult recordFailure(UUID userId){
        String key = RedisKey.USER_SECURITY_BY_ID + userId;
        List<Long> result = redisTemplate.execute(recordFailureScript, 
            List.of(key), 
            String.valueOf(maxFailures), 
            String.valueOf(failureWindowSeconds * 1000L), 
            String.valueOf(lockDurationSeconds * 1000L)
        );
        
        if(result == null || result.size() != 3){
            throw new IllegalStateException("Invalid redis response!");
        }

        return new FailureResult(result.get(0) == 1L, result.get(1).intValue(), result.get(2));
    }

}

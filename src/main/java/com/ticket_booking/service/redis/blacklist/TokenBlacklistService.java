package com.ticket_booking.service.redis.blacklist;

import java.time.Duration;
import java.util.Date;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.ticket_booking.config.redis.RedisKey;
import com.ticket_booking.service.jwt.JwtService;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j 
public class TokenBlacklistService implements ITokenBlacklistService{

    private final RedisTemplate<String, Object> redisTemplate;
    private final JwtService jwtService;

    // Thêm token vào blacklist khi logout
    public void blacklistToken(String token){
        Date expiration = jwtService.extractClaim(token,Claims :: getExpiration);
        long ttl = expiration.getTime() - System.currentTimeMillis();
        
        if (ttl <= 0) {
            log.debug("Token already expired, skip blacklist");
            return;   // token hết hạn rồi thì không cần blacklist
        }

        redisTemplate.opsForValue().set(
                RedisKey.BLACKLIST_PREFIX + token,
                "blacklisted",
                Duration.ofMillis(ttl)
        );
        log.debug("Blacklisted token, TTL={}ms", ttl);
    }

    // Check token có bị blacklist không
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(
            redisTemplate.hasKey(RedisKey.BLACKLIST_PREFIX + token)
        );
    }
}
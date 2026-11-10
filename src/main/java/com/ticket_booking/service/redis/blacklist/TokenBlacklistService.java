package com.ticket_booking.service.redis.blacklist;

public interface TokenBlacklistService {

    void blacklistToken(String token);

    boolean isBlacklisted(String token);
}

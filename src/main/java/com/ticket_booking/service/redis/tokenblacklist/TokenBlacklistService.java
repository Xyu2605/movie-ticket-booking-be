package com.ticket_booking.service.redis.tokenblacklist;

public interface TokenBlacklistService {

    void blacklistToken(String token);

    boolean isBlacklisted(String token);
}

package com.ticket_booking.service.redis.blacklist;

public interface ITokenBlacklistService {

    void blacklistToken(String token);

    boolean isBlacklisted(String token);
}

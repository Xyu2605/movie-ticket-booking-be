package com.ticket_booking.service.jwt;

import org.springframework.security.core.userdetails.UserDetails;

public interface IJwtService {
    
    String generateAccessToken(UserDetails userDetails);
    String generateRefreshToken(UserDetails userDetails);
    String extractUsername(String token);
    
    Long getExpirationTime(String token);
}

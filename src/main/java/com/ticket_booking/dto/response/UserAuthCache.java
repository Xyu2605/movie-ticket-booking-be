package com.ticket_booking.dto.response;

import java.util.UUID;

import com.ticket_booking.enums.AuthProvider;
import com.ticket_booking.enums.RoleCode;

public record UserAuthCache(
    UUID id,
    String email,
    String password,   
    AuthProvider authProvider,
    String providerId,
    boolean isActive,
    RoleCode roleType 
) {
    
}

package com.ticket_booking.dto.response;

import java.security.AuthProvider;
import java.time.Instant;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String fullName,
    String email,
    String providerId,
    AuthProvider authProvider,
    String role,
    Boolean isActive,
    Instant createdAt,
    Instant updatedAt
) {
    
}

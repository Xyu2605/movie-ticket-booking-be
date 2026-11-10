package com.ticket_booking.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

import com.ticket_booking.enums.AuthProvider;
import com.ticket_booking.enums.RoleType;

public record UserDto(
    UUID id,
    String fullName,
    String email,
    AuthProvider authProvider,
    String profilePicture,
    RoleType roleType,
    Boolean isActive,
    Instant createdAt,
    Instant updatedAt
) {

}

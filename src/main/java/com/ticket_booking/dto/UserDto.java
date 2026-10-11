package com.ticket_booking.dto;

import java.security.AuthProvider;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

public record UserDto(
    UUID id,
    String firstName,
    String lastName,
    String email,
    AuthProvider authProvider,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Collection<String> roles
) {

}

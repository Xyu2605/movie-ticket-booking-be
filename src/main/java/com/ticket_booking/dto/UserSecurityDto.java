package com.ticket_booking.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// @Getter
// @Setter
// @Builder
// @NoArgsConstructor
// @AllArgsConstructor
public record UserSecurityDto(
    UUID userId,
    Integer failedAttempts,
    Instant windowStartedAt,
    Instant lockedUntil,
    Instant lastFailedAt,
    String lastFailedIp,
    Instant createdAt,
    Instant updatedAt
) {

}

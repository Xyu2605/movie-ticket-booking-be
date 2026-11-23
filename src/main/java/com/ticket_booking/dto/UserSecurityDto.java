package com.ticket_booking.dto;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSecurityDto {

    private UUID userId;
    private Integer failedAttempts;
    private Instant windowStartedAt;
    private Instant lockedUntil;
    private Instant updatedAt;
}

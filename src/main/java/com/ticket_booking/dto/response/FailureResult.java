package com.ticket_booking.dto.response;

public record FailureResult(
    boolean locked,
    int failedAttempts,
    long lockedUntil
) {
    
}

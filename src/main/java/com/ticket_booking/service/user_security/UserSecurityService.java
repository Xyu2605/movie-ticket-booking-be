package com.ticket_booking.service.user_security;

import java.util.UUID;

import com.ticket_booking.dto.UserSecurityDto;
import com.ticket_booking.entity.UserSecurity;

public interface UserSecurityService {

    UserSecurity getUserSecurity(UUID userId);
    
    UserSecurityDto getUserSecurityDto(UUID userId);
    boolean isLocked(UUID userId);
    long getRemainingLockMinutes(UUID userId);
    
    void recordLoginFailure(UUID userId, String ip);
    void unlock(UUID userId);
}

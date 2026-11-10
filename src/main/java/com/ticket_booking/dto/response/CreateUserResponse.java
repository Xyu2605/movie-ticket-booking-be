package com.ticket_booking.dto.response;

import com.ticket_booking.dto.UserDto;

public record CreateUserResponse(
    String message,
    boolean success,
    UserDto userDto
) {
    
}

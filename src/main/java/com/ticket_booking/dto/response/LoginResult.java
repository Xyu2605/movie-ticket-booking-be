package com.ticket_booking.dto.response;

import com.ticket_booking.dto.UserDto;

public record LoginResult(
    String accessToken,
    String refreshToken,
    UserDto userDto
) {
    
}

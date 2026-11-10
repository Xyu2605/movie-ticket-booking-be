package com.ticket_booking.dto.response;

import com.ticket_booking.dto.UserDto;

public record RegisterResponse(
    String accessToken,
    UserDto user
) {}

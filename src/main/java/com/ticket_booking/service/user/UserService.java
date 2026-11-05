package com.ticket_booking.service.user;

import com.ticket_booking.dto.UserDto;
import com.ticket_booking.dto.request.CreateUserRequest;

public interface UserService {
    UserDto createUser(CreateUserRequest request);
}

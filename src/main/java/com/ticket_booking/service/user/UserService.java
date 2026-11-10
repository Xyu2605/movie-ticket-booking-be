package com.ticket_booking.service.user;

import java.util.UUID;

import com.ticket_booking.dto.UserDto;
import com.ticket_booking.dto.request.CreateUserRequest;
import com.ticket_booking.dto.request.UpdateUserNameRequest;
import com.ticket_booking.dto.request.UpdateUserPasswordRequest;
import com.ticket_booking.entity.User;

public interface UserService {

    User getUserEntityById(UUID id);
    User getUserEntityByEmail(String email);
    User getAuthenticatedUser();
    
    UserDto createUser(CreateUserRequest request);
    UserDto getUserDtoByEmail(String email);
    UserDto getUserDtoById(UUID id);
    UserDto updateUserName(UUID id, UpdateUserNameRequest request);

    void updateUserPassword(UUID id, UpdateUserPasswordRequest request);

    // void resetPassword(String email);

    // void banUser(UUID id, UUID adminId);
    // void unbanUser(UUID id, UUID adminId);
}

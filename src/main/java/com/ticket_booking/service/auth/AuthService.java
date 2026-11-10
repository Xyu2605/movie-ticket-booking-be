package com.ticket_booking.service.auth;

import com.ticket_booking.dto.request.CreateUserRequest;
import com.ticket_booking.dto.request.LoginRequest;
import com.ticket_booking.dto.response.LoginResponse;
import com.ticket_booking.dto.response.RegisterResponse;

import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request, HttpServletResponse response);
    RegisterResponse register(CreateUserRequest request, HttpServletResponse response);
}

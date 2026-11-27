package com.ticket_booking.service.auth;

import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.ticket_booking.dto.request.CreateUserRequest;
import com.ticket_booking.dto.request.LoginRequest;
import com.ticket_booking.dto.response.LoginResponse;
import com.ticket_booking.dto.response.LoginResult;
import com.ticket_booking.dto.response.RegisterResponse;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    
    LoginResult login(LoginRequest request, HttpServletRequest httpServletRequest);
    LoginResult loginWithGoogle(OAuth2User oauth2User);
    
    RegisterResponse register(CreateUserRequest request, HttpServletResponse response);

    // String getNewAccessToken(String refreshToken);
    
    // void logout(HttpServletRequest request, HttpServletResponse response);
}

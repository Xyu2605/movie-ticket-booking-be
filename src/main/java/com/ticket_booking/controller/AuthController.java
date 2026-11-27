package com.ticket_booking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ticket_booking.dto.request.CreateUserRequest;
import com.ticket_booking.dto.request.LoginRequest;
import com.ticket_booking.dto.response.ApiResponseFormat;
import com.ticket_booking.dto.response.LoginResponse;
import com.ticket_booking.dto.response.LoginResult;
import com.ticket_booking.dto.response.RegisterResponse;
import com.ticket_booking.enums.SuccessCode;
import com.ticket_booking.service.auth.AuthService;
import com.ticket_booking.service.cookie.CookieService;
import com.ticket_booking.service.user.UserService;
import com.ticket_booking.utils.ResponseFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final CookieService cookieService;
    private final AuthService authService;
    private final ResponseFactory responseFactory;

    @PostMapping("/login")
    public ResponseEntity<ApiResponseFormat<LoginResponse>> login(@Valid @RequestBody LoginRequest request,HttpServletRequest httpServletRequest, HttpServletResponse response){
        LoginResult result = authService.login(request, httpServletRequest);

        cookieService.setRefreshTokenCookie(response, result.refreshToken());

        LoginResponse loginResponse = new LoginResponse(result.accessToken(), result.userDto());

        return responseFactory.response(SuccessCode.OK, loginResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponseFormat<RegisterResponse>> register(@Valid @RequestBody CreateUserRequest request, HttpServletResponse response){
        RegisterResponse registerResponse = authService.register(request, response);
        return responseFactory.response(SuccessCode.OK, registerResponse);
    }
}

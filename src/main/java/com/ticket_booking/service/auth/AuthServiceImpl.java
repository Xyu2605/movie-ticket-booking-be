package com.ticket_booking.service.auth;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ticket_booking.dto.UserDto;
import com.ticket_booking.dto.request.LoginRequest;
import com.ticket_booking.dto.response.LoginResponse;
import com.ticket_booking.dto.response.LoginResult;
import com.ticket_booking.repository.UserRepository;
import com.ticket_booking.security.user.CustomUserDetailsService;
import com.ticket_booking.service.cookie.CookieService;
import com.ticket_booking.service.jwt.JwtService;
import com.ticket_booking.service.user.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService{
    
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CookieService cookieService;
    
    @Override 
    public LoginResult login(LoginRequest request){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.email());

        UserDto userDto = userService.getUserDtoByEmail(request.email());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return new LoginResult(accessToken, refreshToken, userDto);
    }
}

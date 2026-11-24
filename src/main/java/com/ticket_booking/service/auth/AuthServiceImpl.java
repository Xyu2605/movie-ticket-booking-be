package com.ticket_booking.service.auth;

import java.time.Duration;
import java.util.UUID;

import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.dto.UserDto;
import com.ticket_booking.dto.UserSecurityDto;
import com.ticket_booking.dto.request.LoginRequest;
import com.ticket_booking.dto.response.LoginResponse;
import com.ticket_booking.dto.response.LoginResult;
import com.ticket_booking.entity.User;
import com.ticket_booking.enums.AuthProvider;
import com.ticket_booking.enums.ErrorCode;
import com.ticket_booking.exception.ApiException;
import com.ticket_booking.mapper.UserMapper;
import com.ticket_booking.repository.UserRepository;
import com.ticket_booking.security.user.CustomUserDetails;
import com.ticket_booking.security.user.CustomUserDetailsService;
import com.ticket_booking.service.cookie.CookieService;
import com.ticket_booking.service.jwt.JwtService;
import com.ticket_booking.service.user.UserService;
import com.ticket_booking.service.user_security.UserSecurityService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserSecurityService userSecurityService;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public LoginResult login(LoginRequest request,HttpServletRequest httpServletRequest) {

        // 1. Tìm user
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new ApiException(ErrorCode.INVALID_CREDENTIALS));

        // 2. Kiểm tra tài khoản có đang bị khóa không
        userSecurityService.assertNotLocked(user.getId());

        // 3. Xác thực thông qua Spring Security
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                )
            );

        } catch (BadCredentialsException e) {

            // 4. Đăng nhập sai -> tăng số lần thất bại
            userSecurityService.recordLoginFailures(user.getId(),httpServletRequest.getRemoteAddr());

            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 5. Đăng nhập thành công -> reset số lần thất bại
        userSecurityService.resetLoginFailures(user.getId());

        // 6. Lấy UserDetails từ Authentication
        CustomUserDetails userDetails =(CustomUserDetails) authentication.getPrincipal();

        // 7. JWT generate
        String accessToken =jwtService.generateAccessToken(userDetails);
        String refreshToken =jwtService.generateRefreshToken(userDetails);

        // 8. Trả kết quả
        return new LoginResult(accessToken,refreshToken,userMapper.toDto(user));
    }
}


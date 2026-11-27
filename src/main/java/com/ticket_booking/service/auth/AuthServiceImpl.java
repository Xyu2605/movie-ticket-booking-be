package com.ticket_booking.service.auth;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ticket_booking.dto.UserDto;
import com.ticket_booking.dto.request.CreateUserRequest;
import com.ticket_booking.dto.request.LoginRequest;
import com.ticket_booking.dto.response.LoginResult;
import com.ticket_booking.dto.response.RegisterResponse;
import com.ticket_booking.entity.User;
import com.ticket_booking.enums.ErrorCode;
import com.ticket_booking.exception.ApiException;
import com.ticket_booking.mapper.UserMapper;
import com.ticket_booking.repository.UserRepository;
import com.ticket_booking.security.user.CustomOAuth2User;
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
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final CookieService cookieService;

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

    @Override
    public LoginResult loginWithGoogle(OAuth2User oAuth2User){
        CustomOAuth2User customOAuth2User = (CustomOAuth2User) oAuth2User;
        User user = customOAuth2User.getUser();
        if(user == null){
            throw new ApiException(ErrorCode.INVALID_CREDENTIALS);
        }

        if(!user.isActive()){
            throw new ApiException(ErrorCode.USER_BANNED);
        }

        CustomUserDetails userDetails = CustomUserDetails.buildUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);
        log.info("Google login success: userId={}", user.getId());
        return new LoginResult(accessToken, refreshToken, userMapper.toDto(user));
    }


    @Override
    public RegisterResponse register(CreateUserRequest request, HttpServletResponse response){
        UserDto user =  userService.createUser(request);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.email());
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);    
        cookieService.setRefreshTokenCookie(response, refreshToken);
        return new RegisterResponse(accessToken, user);
    }
}


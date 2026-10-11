package com.ticket_booking.utils.oauth2;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.ticket_booking.entity.User;
import com.ticket_booking.security.user.CustomUserDetailsService;
import com.ticket_booking.service.jwt.JwtService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j 
@RequiredArgsConstructor 
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler{
    
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;
    
    @Value("${spring.security.oauth2.redirect-url}")
    private String redirectUri;

    @Override 
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication)
            throws IOException, ServletException{
        
        if(response.isCommitted()){
            log.debug("Response has already been committed");
            return;
        }

        CustomOAuth2User principal = (CustomOAuth2User) authentication.getPrincipal();
        User user = principal.getUser();
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.info("User {} logged in via OAuth2", user.getEmail());

        String targetUrl = UriComponentsBuilder
                            .fromUriString(redirectUri)
                            .queryParam("accessToken", accessToken)
                            .queryParam("refreshToken", refreshToken)
                            .queryParam("user", user.getEmail())
                            .queryParam("userId", user.getId())
                            .build()
                            .toUriString();
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

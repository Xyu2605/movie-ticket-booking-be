package com.ticket_booking.utils.oauth2;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component 
@Slf4j 
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler{
    
    @Value("${spring.security.oauth2.redirect-url}")
    private String redirectUri;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        AuthenticationException exception) 
            throws IOException, ServletException{
        
        log.error("OAuth2 authentication failed: {}", exception);
        String errorCode = resolveErrorCode(exception);
        String targetUrl = UriComponentsBuilder
            .fromUriString(redirectUri)
            .queryParam("error", errorCode)
            .build()
            .toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String resolveErrorCode(AuthenticationException exception){
        if(exception instanceof OAuth2AuthenticationException ex){
            return ex.getError().getErrorCode();
        }
        return "oauth2_failed";
    }
}

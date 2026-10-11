package com.ticket_booking.utils;

import java.io.IOException;
import java.time.Instant;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket_booking.dto.response.ApiResponse;
import com.ticket_booking.enums.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component 
public class JwtAuthEntryPoint implements AuthenticationEntryPoint{
    
    // Trả về json 401 thay vì html page
    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException
        ) throws IOException {
        
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        ApiResponse<Object> apiResponse = new ApiResponse<>(
            Instant.now(),
            errorCode.isSuccess(),
            errorCode.getCode(),
            errorCode.getMessage(),
            null
        );

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getHttpStatus().value());
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), apiResponse);  
    }
}

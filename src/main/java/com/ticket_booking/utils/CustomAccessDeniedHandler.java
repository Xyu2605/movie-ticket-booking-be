package com.ticket_booking.utils;


import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticket_booking.dto.response.ApiResponseFormat;
import com.ticket_booking.enums.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler{
    @Override
    public void handle(HttpServletRequest request, 
                       HttpServletResponse response, 
                       AccessDeniedException accessDeniedException) throws IOException{
    
        ErrorCode errorCode = ErrorCode.FORBIDDEN;
        ApiResponseFormat<Object> apiResponse = new ApiResponseFormat<Object>(
                System.currentTimeMillis(),
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

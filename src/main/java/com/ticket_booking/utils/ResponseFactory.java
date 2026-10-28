package com.ticket_booking.utils;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.ticket_booking.dto.response.ApiResponseCode;
import com.ticket_booking.dto.response.ApiResponseFormat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@Component
@RequiredArgsConstructor  
public class ResponseFactory {

    public <T> ResponseEntity<ApiResponseFormat<T>> response(ApiResponseCode apiResponseCode, T data){

        ApiResponseFormat<T> body = new ApiResponseFormat<>(
            System.currentTimeMillis(),
            apiResponseCode.isSuccess(),
            apiResponseCode.getCode(),
            apiResponseCode.getMessage(),
            data
        );

        return new ResponseEntity<>(body, apiResponseCode.getHttpStatus());
    }
}

package com.ticket_booking.enums;

import org.springframework.http.HttpStatus;

import com.ticket_booking.dto.response.ApiResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter 
@RequiredArgsConstructor 
public enum SuccessCode implements ApiResponseCode{
    
    OK(true,"S0001", "success.ok", HttpStatus.OK);

    private final boolean success;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}

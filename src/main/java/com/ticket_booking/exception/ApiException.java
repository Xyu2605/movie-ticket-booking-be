package com.ticket_booking.exception;

import com.ticket_booking.dto.response.ApiResponseCode;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException {

    private final ApiResponseCode errorCode;
    private final transient Object data;

    public ApiException(ApiResponseCode errorCode) {
        this(errorCode, null);
    }

    public ApiException(ApiResponseCode errorCode, Object data) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.data = data;
    }
}
package com.ticket_booking.dto.response;

import org.springframework.http.HttpStatus;

public interface ApiResponseCode {
    boolean isSuccess();
    String getCode();
    String getMessage();
    HttpStatus getHttpStatus();
}

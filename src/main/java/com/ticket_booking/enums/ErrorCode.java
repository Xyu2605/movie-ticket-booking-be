package com.ticket_booking.enums;

import org.springframework.http.HttpStatus;

import com.ticket_booking.dto.response.ApiResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter 
@RequiredArgsConstructor 
public enum ErrorCode implements ApiResponseCode {
    
    INTERNAL_SERVER_ERROR(false,"ERR001", "error.other_error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(false,"ERR002", "error.unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(false,"ERR003", "error.forbidden", HttpStatus.FORBIDDEN),  
    RESOURCE_NOT_FOUND(false,"ERR004", "error.resource_not_found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(false,"ERR005", "error.method_not_allowed", HttpStatus.METHOD_NOT_ALLOWED),
    MALFORMED_REQUEST_BODY(false,"ERR006", "error.invalid_input", HttpStatus.BAD_REQUEST),
    MISSING_REQUEST_PARAMETER(false,"ERR007", "error.required_parameter", HttpStatus.BAD_REQUEST),   
    VALIDATION_ERROR(false,"ERR008", "error.input_validation_failed", HttpStatus.BAD_REQUEST), 
    USER_NOT_FOUND(false,"ERR009","error.user_not_found", HttpStatus.NOT_FOUND),
    ;

    private final boolean success;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
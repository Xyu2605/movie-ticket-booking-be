package com.ticket_booking.exception;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.ticket_booking.dto.response.ApiResponseCode;
import com.ticket_booking.dto.response.ApiResponseFormat;
import com.ticket_booking.enums.ErrorCode;
import com.ticket_booking.utils.ResponseFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j 
@ControllerAdvice 
@RequiredArgsConstructor 
public class GlobalExceptionHandler {
    
    private final ResponseFactory responseFactory;
    
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponseFormat<Object>> handlerApiException(ApiException ex){
        ApiResponseCode errorCode = ex.getErrorCode();   
        log.warn("ApiException occurred: code={}, message='{}'", errorCode.getCode(), errorCode.getMessage());
        return responseFactory.response(errorCode, ex.getData());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseFormat<Object>> handleValidationException(
            MethodArgumentNotValidException ex) {

        log.warn("Validation failed");
        List<String> errors = ex.getBindingResult().getFieldErrors().stream().map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
        ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;
        
        return responseFactory.response(errorCode, errors);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiResponseFormat<Object>> handleUserLoginNotFound(UsernameNotFoundException ex){
        
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        log.warn("User not found: {}", ex.getMessage());
        return responseFactory.response(errorCode, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseFormat<Object>> handleGeneric(Exception ex) {
        log.error("Unexpected error", ex);
        return responseFactory.response(ErrorCode.INTERNAL_SERVER_ERROR, null);
    }

}

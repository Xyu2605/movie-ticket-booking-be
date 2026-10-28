package com.ticket_booking.enums;

import org.springframework.http.HttpStatus;

import com.ticket_booking.dto.response.ApiResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ApiResponseCode {
    INTERNAL_SERVER_ERROR(false, "ERR001", "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED(false, "ERR002", "Không có quyền truy cập", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(false, "ERR003", "Bị cấm truy cập", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND(false, "ERR004", "Không tìm thấy tài nguyên", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(false, "ERR005", "Phương thức không được phép", HttpStatus.METHOD_NOT_ALLOWED),
    MALFORMED_REQUEST_BODY(false, "ERR006", "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    MISSING_REQUEST_PARAMETER(false, "ERR007", "Thiếu tham số bắt buộc", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(false, "ERR008", "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    USER_NOT_FOUND(false, "ERR009", "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    TOO_MANY_REQUEST(false,"ERR010", "Quá nhiều yêu cầu", HttpStatus.TOO_MANY_REQUESTS),
    ;

    private final boolean success;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
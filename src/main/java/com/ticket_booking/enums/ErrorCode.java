package com.ticket_booking.enums;

import org.springframework.http.HttpStatus;

import com.ticket_booking.dto.response.ApiResponseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ApiResponseCode {

    INTERNAL_SERVER_ERROR(false, "ERR_001", "Lỗi hệ thống", HttpStatus.INTERNAL_SERVER_ERROR),

    UNAUTHORIZED(false, "ERR_002", "Không có quyền truy cập", HttpStatus.UNAUTHORIZED),
    FORBIDDEN(false, "ERR_003", "Bị cấm truy cập", HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND(false, "ERR004", "Không tìm thấy tài nguyên", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(false, "ERR005", "Phương thức không được phép", HttpStatus.METHOD_NOT_ALLOWED),
    MALFORMED_REQUEST_BODY(false, "ERR006", "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    MISSING_REQUEST_PARAMETER(false, "ERR007", "Thiếu tham số bắt buộc", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(false, "ERR008", "Dữ liệu không hợp lệ", HttpStatus.BAD_REQUEST),
    TOO_MANY_REQUEST(false,"ERR010", "Quá nhiều yêu cầu", HttpStatus.TOO_MANY_REQUESTS),
    
    // User
    USER_NOT_FOUND(false, "USER_001", "Không tìm thấy người dùng", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTED(false, "USER_002", "Người dùng này đã tồn tại", HttpStatus.CONFLICT),
    USER_BANNED(false, "USER_003", "Tài khoản đã bị vô hiệu hóa", HttpStatus.FORBIDDEN),
    USER_DELETED(false, "USER_OO4", "Tài khoản đã bị xóa", HttpStatus.FORBIDDEN),

    // Auth
    INVALID_CREDENTIALS(false, "AUTH_001", "Email hoặc mật khẩu không đúng", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(false, "AUTH_002", "Token đã hết hạn", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(false, "AUTH_003", "Token không hợp lệ", HttpStatus.UNAUTHORIZED),
    ;

    private final boolean success;
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
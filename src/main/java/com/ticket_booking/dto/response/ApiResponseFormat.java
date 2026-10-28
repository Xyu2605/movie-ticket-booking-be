package com.ticket_booking.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // ẩn field null khi serialize
public class ApiResponseFormat<T> {
    private final long timestamp;
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;
}
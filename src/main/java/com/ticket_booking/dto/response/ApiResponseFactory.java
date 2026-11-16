package com.ticket_booking.dto.response;

public final class ApiResponseFactory {
    
    private ApiResponseFactory(){
        throw new UnsupportedOperationException("Utility class");
    }

    public static <T> ApiResponseFormat<T> response(ApiResponseCode apiResponseCode, T data){

        return new ApiResponseFormat<>(System.currentTimeMillis(), 
            apiResponseCode.isSuccess(), 
            apiResponseCode.getCode(), 
            apiResponseCode.getMessage(), 
            data
        );
    }
}

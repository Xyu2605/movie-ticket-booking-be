package com.ticket_booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateUserNameRequest(
    @NotBlank(message = "Name is required!")
    @Size(max = 255)
    String fullName
) {
    
}

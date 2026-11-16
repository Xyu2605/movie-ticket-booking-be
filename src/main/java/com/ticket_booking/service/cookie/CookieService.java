package com.ticket_booking.service.cookie;

import jakarta.servlet.http.HttpServletResponse;

public interface CookieService {
    void setRefreshTokenCookie(HttpServletResponse response, String refreshToken);
    void clearRefreshTokenCookie(HttpServletResponse response);
}

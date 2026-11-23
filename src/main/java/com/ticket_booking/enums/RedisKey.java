package com.ticket_booking.enums;


public final class RedisKey {
    private RedisKey(){}

    //Jwt
    public static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    //User
    public static final String USER_BY_ID = "user:id:";
    public static final String USER_BY_EMAIL = "user:email:";

    // login

    public static final String LOGIN_FAIL_EMAIL = "login-fail:email:";
    public static final String LOGIN_FAIL_IP = "login-fail:ip";

    // user security
    public static final String USER_SECURITY_BY_ID = "user-security:id:";
}

package com.ticket_booking.enums;


public final class RedisKey {
    private RedisKey(){}

    //Jwt
    public static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    //User
    public static final String USER_BY_ID = "user:id:";
    public static final String USER_BY_EMAIL = "user:email:";
}

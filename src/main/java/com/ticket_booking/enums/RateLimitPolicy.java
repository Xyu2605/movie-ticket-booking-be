package com.ticket_booking.enums;

public enum RateLimitPolicy {
    LOGIN("login"),
    BOOKING("booking"),
    DEFAULT("default");

    private final String name;

    RateLimitPolicy(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }
}

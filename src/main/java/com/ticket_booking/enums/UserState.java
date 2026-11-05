package com.ticket_booking.enums;

import java.util.stream.Stream;

import lombok.Getter;

@Getter
public enum UserState {
    BANNED(0),
    ACTIVE(1);

    private final int value;

    UserState(int value){
        this.value = value;
    }

    public static UserState fromValue(int value){
        return Stream.of(values())
                .filter(state -> state.value == value)
                .findFirst()
                .orElseThrow(()-> new IllegalArgumentException("Unknown UserState value:" + value));
    }

}

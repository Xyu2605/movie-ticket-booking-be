package com.ticket_booking.entity;

import java.util.UUID;

import com.ticket_booking.enums.SeatStatus;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Seat {
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    private Long seatNumber;

    private SeatStatus status;

    private Long row;

    private String collumn;
}

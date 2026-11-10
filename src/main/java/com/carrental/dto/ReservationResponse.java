package com.carrental.dto;

import com.carrental.model.CarType;
import com.carrental.model.Reservation;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(UUID id, String carId, CarType type, LocalDateTime start, LocalDateTime end) {
    public static ReservationResponse from(Reservation r) {
        return new ReservationResponse(r.getId(), r.getCar().getId(), r.getCar().getType(), r.getStart(), r.getEnd());
    }
}

package com.carrental.service;

import com.carrental.dto.ReservationRequest;
import com.carrental.model.Reservation;

import java.util.List;
import java.util.UUID;

public interface RentalService {
    Reservation reserve(ReservationRequest request);

    int availableCount(ReservationRequest request);

    Reservation findById(UUID id);

    List<Reservation> list();

    void cancel(UUID id);
}

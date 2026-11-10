package com.carrental.repository;

import com.carrental.model.Reservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository {
    void save(Reservation r);

    Optional<Reservation> findById(UUID id);

    List<Reservation> findAll();

    boolean deleteById(UUID id);
}

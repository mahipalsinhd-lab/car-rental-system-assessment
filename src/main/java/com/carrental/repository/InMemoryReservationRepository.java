package com.carrental.repository;

import com.carrental.model.Reservation;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryReservationRepository implements ReservationRepository {
    private final ConcurrentMap<UUID, Reservation> reservations = new ConcurrentHashMap<>();

    @Override
    public void save(Reservation reservation) {
        Objects.requireNonNull(reservation, "reservation");
        if (reservations.putIfAbsent(reservation.getId(), reservation) != null) {
            throw new IllegalArgumentException("Reservation ID already exists");
        }
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return Optional.ofNullable(reservations.get(Objects.requireNonNull(id, "id")));
    }

    @Override
    public List<Reservation> findAll() {
        return List.copyOf(reservations.values());
    }

    @Override
    public boolean deleteById(UUID id) {
        return reservations.remove(Objects.requireNonNull(id, "id")) != null;
    }
}

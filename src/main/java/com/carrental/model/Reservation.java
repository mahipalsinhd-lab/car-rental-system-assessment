package com.carrental.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Reservation {
    private final UUID id;
    private final Car car;
    private final LocalDateTime start;
    private final LocalDateTime end;

    public Reservation(UUID id, Car car, LocalDateTime start, LocalDateTime end) {
        this.id = Objects.requireNonNull(id);
        this.car = Objects.requireNonNull(car);
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
        if (!end.isAfter(start)) throw new IllegalArgumentException("End must be after start");
    }

    public UUID getId() {
        return id;
    }

    public Car getCar() {
        return car;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public boolean overlaps(LocalDateTime s, LocalDateTime e) {
        return start.isBefore(e) && s.isBefore(end);
    }
}

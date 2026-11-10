package com.carrental.service;

import com.carrental.dto.ReservationRequest;
import com.carrental.exception.CarUnavailableException;
import com.carrental.exception.InvalidDateRangeException;
import com.carrental.exception.ReservationNotFoundException;
import com.carrental.model.Car;
import com.carrental.model.CarType;
import com.carrental.model.Reservation;
import com.carrental.repository.CarRepository;
import com.carrental.repository.ReservationRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class CarRentalService implements RentalService {
    private final CarRepository cars;
    private final ReservationRepository reservations;
    private final Validator validator;
    private final EnumMap<CarType, ReentrantLock> typeLocks = new EnumMap<>(CarType.class);

    public CarRentalService(CarRepository cars, ReservationRepository reservations, Validator validator) {
        this.cars = Objects.requireNonNull(cars);
        this.reservations = Objects.requireNonNull(reservations);
        this.validator = Objects.requireNonNull(validator);
        for (CarType type : CarType.values()) {
            typeLocks.put(type, new ReentrantLock());
        }
    }

    private LocalDateTime end(ReservationRequest request) {
        if (request == null) throw new IllegalArgumentException("Request is required");
        var violations = validator.validate(request);
        if (!violations.isEmpty()) throw new ConstraintViolationException(violations);
        try {
            return request.start().plusDays(request.days());
        } catch (DateTimeException | ArithmeticException ex) {
            throw new InvalidDateRangeException();
        }
    }

    @Override
    public Reservation reserve(ReservationRequest request) {
        LocalDateTime finish = end(request);
        ReentrantLock lock = typeLocks.get(request.type());
        lock.lock();
        try {
            List<Reservation> existing = reservations.findAll();
            for (Car car : cars.findByType(request.type())) {
                boolean occupied = existing.stream().anyMatch(r ->
                        r.getCar().equals(car) && r.overlaps(request.start(), finish));
                if (!occupied) {
                    Reservation booking = new Reservation(UUID.randomUUID(), car, request.start(), finish);
                    reservations.save(booking);
                    return booking;
                }
            }
            throw new CarUnavailableException();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int availableCount(ReservationRequest request) {
        LocalDateTime finish = end(request);
        ReentrantLock lock = typeLocks.get(request.type());
        lock.lock();
        try {
            List<Reservation> existing = reservations.findAll();
            return (int) cars.findByType(request.type()).stream().filter(car ->
                    existing.stream().noneMatch(r ->
                            r.getCar().equals(car) && r.overlaps(request.start(), finish))).count();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Reservation findById(UUID id) {
        return reservations.findById(id).orElseThrow(ReservationNotFoundException::new);
    }

    @Override
    public List<Reservation> list() {
        return reservations.findAll();
    }

    @Override
    public void cancel(UUID id) {
        if (id == null) throw new ReservationNotFoundException();
        // Find the type, then lock it before removing the booking. All writes for
        // that vehicle category share the same lock as reserve().
        Reservation booking = reservations.findById(id).orElseThrow(ReservationNotFoundException::new);
        ReentrantLock lock = typeLocks.get(booking.getCar().getType());
        lock.lock();
        try {
            if (!reservations.deleteById(id)) throw new ReservationNotFoundException();
        } finally {
            lock.unlock();
        }
    }
}

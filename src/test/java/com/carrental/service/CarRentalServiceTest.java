package com.carrental.service;

import com.carrental.dto.ReservationRequest;
import com.carrental.exception.CarUnavailableException;
import com.carrental.exception.InvalidDateRangeException;
import com.carrental.exception.ReservationNotFoundException;
import com.carrental.model.CarType;
import com.carrental.model.Sedan;
import com.carrental.model.Suv;
import com.carrental.repository.InMemoryCarRepository;
import com.carrental.repository.InMemoryReservationRepository;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class CarRentalServiceTest {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();
    private CarRentalService service;
    private LocalDateTime start;

    @BeforeEach
    void setup() {
        service = new CarRentalService(new InMemoryCarRepository(List.of(new Sedan("1"), new Sedan("2"), new Suv("3"))), new InMemoryReservationRepository(), FACTORY.getValidator());
        start = LocalDateTime.of(2030, 1, 1, 10, 0);
    }

    private ReservationRequest request(CarType t, LocalDateTime s, int days) {
        return new ReservationRequest(t, s, days);
    }

    @Test
    void capacityAndConflict() {
        var req = request(CarType.SEDAN, start, 2);
        assertEquals(2, service.availableCount(req));
        service.reserve(req);
        service.reserve(req);
        assertEquals(0, service.availableCount(req));
        assertThrows(CarUnavailableException.class, () -> service.reserve(req));
    }

    @Test
    void adjacentAndPartialOverlap() {
        service.reserve(request(CarType.SUV, start, 2));
        assertThrows(CarUnavailableException.class, () -> service.reserve(request(CarType.SUV, start.plusDays(1), 2)));
        assertNotNull(service.reserve(request(CarType.SUV, start.plusDays(2), 1)));
    }

    @Test
    void independentInventoryAndCancel() {
        var r = service.reserve(request(CarType.SUV, start, 2));
        assertEquals(r, service.findById(r.getId()));
        assertEquals(1, service.list().size());
        service.cancel(r.getId());
        assertEquals(1, service.availableCount(request(CarType.SUV, start, 2)));
        assertThrows(ReservationNotFoundException.class, () -> service.cancel(r.getId()));
        assertThrows(ReservationNotFoundException.class, () -> service.findById(r.getId()));
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(ConstraintViolationException.class, () -> service.reserve(request(null, start, 1)));
        assertThrows(ConstraintViolationException.class, () -> service.reserve(request(CarType.SEDAN, null, 1)));
        assertThrows(ConstraintViolationException.class, () -> service.reserve(request(CarType.SEDAN, start, 0)));
        assertThrows(ConstraintViolationException.class, () -> service.reserve(request(CarType.SEDAN, start, 366)));
        assertThrows(IllegalArgumentException.class, () -> service.reserve(null));
    }

    @Test
    void handlesOverflow() {
        assertThrows(InvalidDateRangeException.class, () -> service.reserve(request(CarType.SEDAN, LocalDateTime.MAX, 2)));
    }

    @Test
    void emptyInventory() {
        var empty = new CarRentalService(new InMemoryCarRepository(List.of()), new InMemoryReservationRepository(), FACTORY.getValidator());
        assertThrows(CarUnavailableException.class, () -> empty.reserve(request(CarType.VAN, start, 1)));
    }

    @Test
    void concurrentBookingNeverOverbooks() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            var gate = new CountDownLatch(1);
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 20; i++)
                results.add(pool.submit(() -> {
                    gate.await();
                    try {
                        service.reserve(request(CarType.SEDAN, start, 1));
                        return true;
                    } catch (CarUnavailableException e) {
                        return false;
                    }
                }));
            gate.countDown();
            int success = 0;
            for (var f : results) if (f.get(5, TimeUnit.SECONDS)) success++;
            assertEquals(2, success);
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void cancellationMakesCarAvailableAgain() {
        var request = request(CarType.SUV, start, 1);
        var booking = service.reserve(request);
        assertEquals(0, service.availableCount(request));
        service.cancel(booking.getId());
        assertEquals(1, service.availableCount(request));
        assertNotNull(service.reserve(request));
    }

    @Test
    void concurrentDifferentTypesKeepIndependentCapacity() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(12);
        try {
            CountDownLatch gate = new CountDownLatch(1);
            List<Future<Boolean>> sedan = new ArrayList<>();
            List<Future<Boolean>> suv = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                final CarType type = i % 2 == 0 ? CarType.SEDAN : CarType.SUV;
                Future<Boolean> future = pool.submit(() -> {
                    gate.await();
                    try {
                        service.reserve(request(type, start, 1));
                        return true;
                    } catch (CarUnavailableException ex) {
                        return false;
                    }
                });
                (type == CarType.SEDAN ? sedan : suv).add(future);
            }
            gate.countDown();
            int sedanCount = 0, suvCount = 0;
            for (Future<Boolean> future : sedan) if (future.get(5, TimeUnit.SECONDS)) sedanCount++;
            for (Future<Boolean> future : suv) if (future.get(5, TimeUnit.SECONDS)) suvCount++;
            assertEquals(2, sedanCount);
            assertEquals(1, suvCount);
        } finally {
            pool.shutdownNow();
        }
    }
}

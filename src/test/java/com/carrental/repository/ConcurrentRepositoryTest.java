package com.carrental.repository;

import com.carrental.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ConcurrentRepositoryTest {
    @Test
    void concurrentUniqueSavesAreAllRetained() throws Exception {
        var repository = new InMemoryReservationRepository();
        var start = LocalDateTime.of(2030, 1, 1, 10, 0);
        var car = new Sedan("SED-1");
        int count = 100;
        var pool = Executors.newFixedThreadPool(12);
        var gate = new CountDownLatch(1);
        try {
            List<Future<?>> tasks = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                tasks.add(pool.submit(() -> {
                    gate.await();
                    repository.save(new Reservation(UUID.randomUUID(), car, start, start.plusDays(1)));
                    return null;
                }));
            }
            gate.countDown();
            for (Future<?> task : tasks) task.get(10, TimeUnit.SECONDS);
            assertEquals(count, repository.findAll().size());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void duplicateReservationIdIsRejectedWithoutReplacingExisting() {
        var repository = new InMemoryReservationRepository();
        var start = LocalDateTime.of(2030, 1, 1, 10, 0);
        var id = UUID.randomUUID();
        var original = new Reservation(id, new Sedan("SED-1"), start, start.plusDays(1));
        var duplicate = new Reservation(id, new Suv("SUV-1"), start, start.plusDays(1));
        repository.save(original);
        assertThrows(IllegalArgumentException.class, () -> repository.save(duplicate));
        assertSame(original, repository.findById(id).orElseThrow());
    }

    @Test
    void carInventoryIsDeterministicAndUnmodifiable() {
        var repository = new InMemoryCarRepository(List.of(new Van("V-2"), new Van("V-1")));
        assertEquals(List.of("V-1", "V-2"),
                repository.findByType(CarType.VAN).stream().map(Car::getId).toList());
        assertThrows(UnsupportedOperationException.class, () -> repository.findAll().clear());
    }
}

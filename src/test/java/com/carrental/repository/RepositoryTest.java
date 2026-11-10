package com.carrental.repository;

import com.carrental.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {
    @Test
    void carFilteringAndDuplicateIds() {
        var repo = new InMemoryCarRepository(List.of(new Sedan("1"), new Suv("2")));
        assertEquals(1, repo.findByType(CarType.SEDAN).size());
        assertThrows(UnsupportedOperationException.class, () -> repo.findAll().clear());
        assertThrows(IllegalArgumentException.class, () -> new InMemoryCarRepository(List.of(new Sedan("1"), new Van("1"))));
    }

    @Test
    void reservationCrudAndSnapshot() {
        var repo = new InMemoryReservationRepository();
        var s = LocalDateTime.of(2030, 1, 1, 10, 0);
        var r = new Reservation(UUID.randomUUID(), new Sedan("1"), s, s.plusDays(1));
        repo.save(r);
        var snapshot = repo.findAll();
        assertEquals(r, repo.findById(r.getId()).orElseThrow());
        assertTrue(repo.deleteById(r.getId()));
        assertFalse(repo.deleteById(r.getId()));
        assertEquals(1, snapshot.size());
        assertTrue(repo.findAll().isEmpty());
    }
}

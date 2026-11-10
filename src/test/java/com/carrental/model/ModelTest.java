package com.carrental.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {
    @Test
    void polymorphicTypes() {
        Car a = new Sedan("A");
        Car b = new Suv("B");
        Car c = new Van("C");
        assertEquals(CarType.SEDAN, a.getType());
        assertEquals(CarType.SUV, b.getType());
        assertEquals(CarType.VAN, c.getType());
    }

    @Test
    void identityAndInvalidId() {
        assertEquals(new Sedan("A"), new Suv("A"));
        assertThrows(IllegalArgumentException.class, () -> new Sedan(" "));
    }

    @Test
    void overlapsAndBoundary() {
        var s = LocalDateTime.of(2030, 1, 1, 10, 0);
        var r = new Reservation(UUID.randomUUID(), new Sedan("A"), s, s.plusDays(2));
        assertTrue(r.overlaps(s.plusDays(1), s.plusDays(3)));
        assertFalse(r.overlaps(s.plusDays(2), s.plusDays(3)));
        assertFalse(r.overlaps(s.minusDays(1), s));
        assertThrows(IllegalArgumentException.class, () -> new Reservation(UUID.randomUUID(), new Sedan("B"), s, s));
    }
}

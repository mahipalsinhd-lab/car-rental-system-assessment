package com.carrental.model;

import java.util.Objects;

public abstract class Car {
    private final String id;

    protected Car(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("Car ID required");
        this.id = id;
    }

    public final String getId() {
        return id;
    }

    public abstract CarType getType();

    @Override
    public final boolean equals(Object o) {
        return o instanceof Car c && id.equals(c.id);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}

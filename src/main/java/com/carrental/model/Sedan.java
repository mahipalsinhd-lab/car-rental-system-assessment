package com.carrental.model;

public final class Sedan extends Car {
    public Sedan(String id) {
        super(id);
    }

    @Override
    public CarType getType() {
        return CarType.SEDAN;
    }
}

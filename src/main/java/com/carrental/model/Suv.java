package com.carrental.model;

public final class Suv extends Car {
    public Suv(String id) {
        super(id);
    }

    @Override
    public CarType getType() {
        return CarType.SUV;
    }
}

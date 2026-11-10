package com.carrental.model;

public final class Van extends Car {
    public Van(String id) {
        super(id);
    }

    @Override
    public CarType getType() {
        return CarType.VAN;
    }
}

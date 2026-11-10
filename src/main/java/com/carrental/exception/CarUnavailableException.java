package com.carrental.exception;

public final class CarUnavailableException extends RentalException {
    public CarUnavailableException() {
        super(ErrorCode.CAR_UNAVAILABLE, "No car of the requested type is available");
    }
}

package com.carrental.exception;

public final class ReservationNotFoundException extends RentalException {
    public ReservationNotFoundException() {
        super(ErrorCode.RESERVATION_NOT_FOUND, "Reservation not found");
    }
}

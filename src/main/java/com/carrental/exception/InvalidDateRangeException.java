package com.carrental.exception;

public final class InvalidDateRangeException extends RentalException {
    public InvalidDateRangeException() {
        super(ErrorCode.INVALID_DATE_RANGE, "Rental date range is invalid");
    }
}

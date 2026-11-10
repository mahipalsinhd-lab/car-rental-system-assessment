package com.carrental.exception;

public abstract class RentalException extends RuntimeException {
    private final ErrorCode code;

    protected RentalException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() {
        return code;
    }
}

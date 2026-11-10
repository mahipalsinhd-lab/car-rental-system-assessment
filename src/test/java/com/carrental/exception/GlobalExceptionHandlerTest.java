package com.carrental.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {
    @Test
    void customErrorsHaveStableCodes() {
        var h = new GlobalExceptionHandler();
        var a = h.unavailable(new CarUnavailableException());
        assertEquals(HttpStatus.CONFLICT, a.getStatusCode());
        assertEquals("CAR_UNAVAILABLE", a.getBody().code());
        assertNotNull(a.getBody().timestamp());
        var b = h.notFound(new ReservationNotFoundException());
        assertEquals(HttpStatus.NOT_FOUND, b.getStatusCode());
        assertEquals("RESERVATION_NOT_FOUND", b.getBody().code());
        assertEquals(HttpStatus.BAD_REQUEST, h.badRange(new InvalidDateRangeException()).getStatusCode());
    }

    @Test
    void unknownErrorsDoNotLeakDetails() {
        var a = new GlobalExceptionHandler().unexpected(new RuntimeException("secret"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, a.getStatusCode());
        assertFalse(a.getBody().message().contains("secret"));
    }
}

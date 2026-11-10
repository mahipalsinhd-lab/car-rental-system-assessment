package com.carrental.exception;

import com.carrental.dto.ApiError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.Map;
import java.util.TreeMap;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private ResponseEntity<ApiError> response(HttpStatus status, ErrorCode code, String msg, Map<String, String> details) {
        return ResponseEntity.status(status).body(new ApiError(code.name(), msg, Instant.now(), details));
    }

    @ExceptionHandler(CarUnavailableException.class)
    public ResponseEntity<ApiError> unavailable(CarUnavailableException e) {
        return response(HttpStatus.CONFLICT, e.getCode(), e.getMessage(), Map.of());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ApiError> notFound(ReservationNotFoundException e) {
        return response(HttpStatus.NOT_FOUND, e.getCode(), e.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ApiError> badRange(InvalidDateRangeException e) {
        return response(HttpStatus.BAD_REQUEST, e.getCode(), e.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> beanValidation(MethodArgumentNotValidException e) {
        Map<String, String> fields = new TreeMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.put(f.getField(), f.getDefaultMessage()));
        return response(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "Validation failed", fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> constraint(ConstraintViolationException e) {
        Map<String, String> fields = new TreeMap<>();
        e.getConstraintViolations().forEach(v -> fields.put(v.getPropertyPath().toString(), v.getMessage()));
        return response(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "Validation failed", fields);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
    public ResponseEntity<ApiError> malformed(Exception e) {
        return response(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_REQUEST, "Malformed or invalid request", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception e) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "Unexpected server error", Map.of());
    }
}

package com.carrental.dto;

import com.carrental.model.CarType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReservationRequest(@NotNull CarType type, @NotNull LocalDateTime start, @Min(1) @Max(365) int days) {
}

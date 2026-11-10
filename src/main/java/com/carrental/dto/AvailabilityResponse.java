package com.carrental.dto;

import com.carrental.model.CarType;

public record AvailabilityResponse(CarType type, int available) {
}

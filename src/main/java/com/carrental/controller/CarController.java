package com.carrental.controller;

import com.carrental.model.CarType;
import com.carrental.repository.CarRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cars")
@Tag(name = "Cars", description = "Read vehicle inventory")
public class CarController {
    private final CarRepository cars;

    public CarController(CarRepository cars) {
        this.cars = cars;
    }

    @Operation(summary = "List cars", description = "Optionally filter by car type")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Vehicles"), @ApiResponse(responseCode = "400", description = "Invalid car type")})
    @GetMapping
    public List<CarResponse> list(@RequestParam(required = false) CarType type) {
        return (type == null ? cars.findAll() : cars.findByType(type)).stream().map(c -> new CarResponse(c.getId(), c.getType())).toList();
    }

    public record CarResponse(String id, CarType type) {
    }
}

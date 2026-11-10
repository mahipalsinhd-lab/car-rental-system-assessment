package com.carrental.controller;

import com.carrental.dto.ApiError;
import com.carrental.dto.AvailabilityResponse;
import com.carrental.dto.ReservationRequest;
import com.carrental.dto.ReservationResponse;
import com.carrental.service.RentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@Tag(name = "Reservations", description = "Book, inspect, list and cancel rentals")
public class ReservationController {
    private final RentalService service;

    public ReservationController(RentalService service) {
        this.service = service;
    }

    @Operation(summary = "Create reservation", description = "Assign an available vehicle; rental intervals are half-open [start, end).")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Reservation created"), @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(schema = @Schema(implementation = ApiError.class))), @ApiResponse(responseCode = "409", description = "No inventory", content = @Content(schema = @Schema(implementation = ApiError.class)))})
    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(@Valid @RequestBody ReservationRequest request) {
        var r = ReservationResponse.from(service.reserve(request));
        return ResponseEntity.status(HttpStatus.CREATED).header(HttpHeaders.LOCATION, "/api/v1/reservations/" + r.id()).body(r);
    }

    @Operation(summary = "Check availability")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Available vehicle count"), @ApiResponse(responseCode = "400", description = "Invalid request", content = @Content(schema = @Schema(implementation = ApiError.class)))})
    @PostMapping("/availability")
    public AvailabilityResponse availability(@Valid @RequestBody ReservationRequest request) {
        return new AvailabilityResponse(request.type(), service.availableCount(request));
    }

    @Operation(summary = "List reservations")
    @ApiResponse(responseCode = "200", description = "All reservations")
    @GetMapping
    public List<ReservationResponse> list() {
        return service.list().stream().map(ReservationResponse::from).toList();
    }

    @Operation(summary = "Get reservation")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Reservation details"), @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiError.class)))})
    @GetMapping("/{id}")
    public ReservationResponse get(@PathVariable UUID id) {
        return ReservationResponse.from(service.findById(id));
    }

    @Operation(summary = "Cancel reservation")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Cancelled"), @ApiResponse(responseCode = "404", description = "Not found", content = @Content(schema = @Schema(implementation = ApiError.class)))})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID id) {
        service.cancel(id);
    }
}

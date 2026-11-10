package com.carrental.controller;

import com.carrental.exception.CarUnavailableException;
import com.carrental.exception.GlobalExceptionHandler;
import com.carrental.exception.ReservationNotFoundException;
import com.carrental.model.Reservation;
import com.carrental.model.Sedan;
import com.carrental.service.RentalService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReservationControllerTest {
    private final RentalService service = mock(RentalService.class);
    private final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    private final String body = "{\"type\":\"SEDAN\",\"start\":\"2030-01-01T10:00:00\",\"days\":2}";

    private MockMvc mvc() {
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(new ReservationController(service)).setControllerAdvice(new GlobalExceptionHandler()).setValidator(validator).build();
    }

    @Test
    void createAndGet() throws Exception {
        var s = LocalDateTime.of(2030, 1, 1, 10, 0);
        var r = new Reservation(UUID.randomUUID(), new Sedan("S1"), s, s.plusDays(2));
        when(service.reserve(any())).thenReturn(r);
        when(service.findById(r.getId())).thenReturn(r);
        mvc().perform(post("/api/v1/reservations").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andExpect(jsonPath("$.carId").value("S1"));
        mvc().perform(get("/api/v1/reservations/" + r.getId())).andExpect(status().isOk()).andExpect(jsonPath("$.type").value("SEDAN"));
    }

    @Test
    void invalidAndMalformedRequest() throws Exception {
        mvc().perform(post("/api/v1/reservations").contentType(MediaType.APPLICATION_JSON).content("{\"type\":\"SEDAN\",\"start\":\"2030-01-01T10:00:00\",\"days\":0}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
        mvc().perform(post("/api/v1/reservations").contentType(MediaType.APPLICATION_JSON).content("{bad json")).andExpect(status().isBadRequest());
    }

    @Test
    void conflictAndNotFound() throws Exception {
        when(service.reserve(any())).thenThrow(new CarUnavailableException());
        when(service.findById(any())).thenThrow(new ReservationNotFoundException());
        mvc().perform(post("/api/v1/reservations").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CAR_UNAVAILABLE"));
        mvc().perform(get("/api/v1/reservations/" + UUID.randomUUID())).andExpect(status().isNotFound());
    }

    @Test
    void availabilityListCancel() throws Exception {
        when(service.availableCount(any())).thenReturn(2);
        when(service.list()).thenReturn(List.of());
        mvc().perform(post("/api/v1/reservations/availability").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andExpect(jsonPath("$.available").value(2));
        mvc().perform(get("/api/v1/reservations")).andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc().perform(delete("/api/v1/reservations/" + UUID.randomUUID())).andExpect(status().isNoContent());
    }
}

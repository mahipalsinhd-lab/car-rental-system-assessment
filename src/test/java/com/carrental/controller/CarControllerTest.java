package com.carrental.controller;

import com.carrental.exception.GlobalExceptionHandler;
import com.carrental.model.Sedan;
import com.carrental.model.Van;
import com.carrental.repository.InMemoryCarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CarControllerTest {
    @Test
    void listAndFilterAndBadEnum() throws Exception {
        var mvc = MockMvcBuilders.standaloneSetup(new CarController(new InMemoryCarRepository(List.of(new Sedan("A"), new Van("B"))))).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/api/v1/cars")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
        mvc.perform(get("/api/v1/cars?type=VAN")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("B"));
        mvc.perform(get("/api/v1/cars?type=TRUCK")).andExpect(status().isBadRequest());
    }
}

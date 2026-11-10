package com.carrental.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI rentalApi() {
        return new OpenAPI().info(new Info().title("Car Rental API").version("v1").description("Car rental booking API's"));
    }
}

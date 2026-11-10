package com.carrental.repository;

import com.carrental.model.*;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryCarRepository implements CarRepository {
    private final ConcurrentMap<String, Car> cars = new ConcurrentHashMap<>();

    public InMemoryCarRepository() {
        this(List.of(new Sedan("SED-1"), new Sedan("SED-2"),
                new Suv("SUV-1"), new Suv("SUV-2"), new Van("VAN-1")));
    }

    public InMemoryCarRepository(List<Car> fleet) {
        Objects.requireNonNull(fleet, "fleet");
        for (Car car : fleet) {
            Objects.requireNonNull(car, "car");
            if (cars.putIfAbsent(car.getId(), car) != null) {
                throw new IllegalArgumentException("Duplicate car ID: " + car.getId());
            }
        }
    }

    @Override
    public List<Car> findByType(CarType type) {
        Objects.requireNonNull(type, "type");
        return cars.values().stream().filter(car -> car.getType() == type)
                .sorted(Comparator.comparing(Car::getId)).toList();
    }

    @Override
    public List<Car> findAll() {
        return cars.values().stream().sorted(Comparator.comparing(Car::getId)).toList();
    }
}

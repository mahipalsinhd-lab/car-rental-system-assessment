package com.carrental.repository;

import com.carrental.model.Car;
import com.carrental.model.CarType;

import java.util.List;

public interface CarRepository {
    List<Car> findByType(CarType type);

    List<Car> findAll();
}

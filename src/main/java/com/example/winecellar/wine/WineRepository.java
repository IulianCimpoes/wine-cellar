package com.example.winecellar.wine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WineRepository extends JpaRepository<Wine, Long> {
    // Derived query method: Spring builds the query from the method name
    List<Wine> findByCountryIgnoreCase(String country);
}

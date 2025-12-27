package com.example.winecellar.winery;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WineryRepository extends JpaRepository<Winery, Long> {
    boolean existsByNameIgnoreCaseAndCountryIgnoreCase(String name, String country);
    boolean existsByNameIgnoreCaseAndCountryIgnoreCaseAndIdNot(String name, String country, Long id);
}

package com.example.winecellar.wine;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WineService {

    private final WineRepository wineRepository;

    public WineService(WineRepository wineRepository) {
        this.wineRepository = wineRepository;  // constructor-based DI
    }

    public List<Wine> findAll() {
        return wineRepository.findAll();
    }

    public Wine create(Wine wine) {
        return wineRepository.save(wine);
    }

    public Wine findById(Long id) {
        return wineRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Wine not found: " + id));
    }

    public List<Wine> findByCountry(String country) {
        return wineRepository.findByCountryIgnoreCase(country);
    }
}

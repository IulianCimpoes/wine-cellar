package com.example.winecellar.wine;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WineService {

    private final WineRepository wineRepository;

    public WineService(WineRepository wineRepository) {
        this.wineRepository = wineRepository;  // constructor-based DI
    }

    public List<Wine> findAll() {
        return wineRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Page<Wine> findAllPaged(Pageable pageable) {
        return wineRepository.findAll(pageable);
    }

    public Wine create(Wine wine) {
        return wineRepository.save(wine);
    }

    @Transactional(readOnly = true)
    public Wine findById(Long id) {
        return wineRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Wine not found: " + id));
    }

    public List<Wine> findByCountry(String country) {
        return wineRepository.findByCountryIgnoreCase(country);
    }

    @Transactional(readOnly = true)
    public List<Wine> findAllWithWinery() {
        return wineRepository.findAllWithWinery();
    }

    public List<Wine> findByCountryWithWinery(String country) {
        return wineRepository.findByCountryWithWinery(country);
    }
}

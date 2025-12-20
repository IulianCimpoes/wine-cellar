package com.example.winecellar.winery;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WineryService {

    private final WineryRepository wineryRepository;

    public WineryService(WineryRepository wineryRepository) {
        this.wineryRepository = wineryRepository;
    }

    public Winery create(Winery winery) {
        return wineryRepository.save(winery);
    }

    public Winery findById(Long id) {
        return wineryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Winery not found: " + id));
    }

    public List<Winery> findAll() {
        return wineryRepository.findAll();
    }
}

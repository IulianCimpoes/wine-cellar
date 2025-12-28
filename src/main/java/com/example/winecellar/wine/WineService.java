package com.example.winecellar.wine;


import com.example.winecellar.common.exception.ConflictException;
import com.example.winecellar.common.exception.NotFoundException;
import com.example.winecellar.winery.Winery;
import com.example.winecellar.winery.WineryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WineService {

    private final WineRepository wineRepository;
    private final WineryRepository wineryRepository;

    public WineService(WineRepository wineRepository, WineryRepository wineryRepository) {
        this.wineRepository = wineRepository;
        this.wineryRepository = wineryRepository;
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
        return wineRepository.findById(id)
                             .orElseThrow(() -> new NotFoundException("Wine not found: " + id));
    }

    public Page<Wine> findByWineryIdPaged(Long wineryId, Pageable pageable) {
        return wineRepository.findByWineryRef_Id(wineryId, pageable);
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

    @Transactional(readOnly = true)
    public List<Wine> findByWineryId(Long wineryId) {
        return wineRepository.findByWineryRef_Id(wineryId);
    }

    public Wine update(Long id, WineUpdateRequest request) {

        Wine wine = findById(id);
        Winery winery = wineryRepository.findById(request.wineryId())
                                        .orElseThrow(() -> new NotFoundException("Winery not found: " + request.wineryId()));
        wine.setName(request.name());
        wine.setCountry(request.country());
        wine.setWineryRef(winery);
        wine.setWineYear(request.wineYear());
        wine.setPrice(request.price());
        return wineRepository.save(wine);
    }
}

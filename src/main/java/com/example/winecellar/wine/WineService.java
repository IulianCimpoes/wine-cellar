package com.example.winecellar.wine;


import com.example.winecellar.common.exception.ConflictException;
import com.example.winecellar.common.exception.NotFoundException;
import com.example.winecellar.winery.Winery;
import com.example.winecellar.winery.WineryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class WineService {

    private final WineRepository wineRepository;
    private final WineryRepository wineryRepository;

    public WineService(WineRepository wineRepository, WineryRepository wineryRepository) {
        this.wineRepository = wineRepository;
        this.wineryRepository = wineryRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Wine> findAll() {
        return wineRepository.findAll();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Page<Wine> findAllPaged(Pageable pageable) {
        return wineRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Wine findById(Long id) {
        return wineRepository.findById(id)
                             .orElseThrow(() -> new NotFoundException("Wine not found: " + id));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Page<Wine> findByWineryIdPaged(Long wineryId, Pageable pageable) {
        return wineRepository.findByWineryRef_Id(wineryId, pageable);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Wine> findByCountry(String country) {
        return wineRepository.findByCountryIgnoreCase(country);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Wine> findAllWithWinery() {
        return wineRepository.findAllWithWinery();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Wine> findByCountryWithWinery(String country) {
        return wineRepository.findByCountryWithWinery(country);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Wine> findByWineryId(Long wineryId) {
        return wineRepository.findByWineryRef_Id(wineryId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Wine create(Wine wine) {
        if (wineRepository.existsByNameIgnoreCaseAndWineYearAndWineryRef_Id(wine.getName(), wine.getWineYear(), wine.getWineryRef()
                                                                                                                    .getId())) {
            throw new ConflictException("Wine already exists: " + wine.getName() + " (" + wine.getWineYear() + ") for winery " + wine.getWineryRef()
                                                                                                                                     .getId());
        }

        return wineRepository.save(wine);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Wine update(Long id, WineUpdateRequest request) {

        Wine wine = findById(id);
        Winery winery = wineryRepository.findById(request.wineryId())
                                        .orElseThrow(() -> new NotFoundException("Winery not found: " + request.wineryId()));

        if (!Objects.equals(wine.getVersion(), request.version())) {
            throw new ConflictException("Wine was updated by another transaction. Please refresh and retry.");
        }

        if (wineRepository.existsByNameIgnoreCaseAndWineYearAndWineryRef_IdAndIdNot(request.name(), request.wineYear(), request.wineryId(), id)) {
            throw new ConflictException("Wine already exists: " + request.name() + " (" + request.wineYear() + ") for winery " + request.wineryId());
        }

        wine.setName(request.name());
        wine.setCountry(request.country());
        wine.setWineryRef(winery);
        wine.setWineYear(request.wineYear());
        wine.setPrice(request.price());
        return wineRepository.save(wine);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        findById(id);
        wineRepository.deleteById(id);
    }

}

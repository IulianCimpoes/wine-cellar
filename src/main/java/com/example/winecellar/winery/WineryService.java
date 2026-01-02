package com.example.winecellar.winery;

import com.example.winecellar.common.exception.ConflictException;
import com.example.winecellar.common.exception.NotFoundException;
import com.example.winecellar.wine.WineRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class WineryService {

    private final WineryRepository wineryRepository;
    private final WineRepository wineRepository;

    public WineryService(WineryRepository wineryRepository, WineRepository wineRepository) {
        this.wineryRepository = wineryRepository;
        this.wineRepository = wineRepository;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Winery findById(Long id) {
        return wineryRepository.findById(id)
                               .orElseThrow(() -> new NotFoundException("Winery not found: " + id));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Winery> findAll() {
        return wineryRepository.findAll();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Page<Winery> findAllPaged(Pageable pageable) {
        return wineryRepository.findAll(pageable);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Winery create(Winery winery) {
        if (wineryRepository.existsByNameIgnoreCaseAndCountryIgnoreCase(winery.getName(), winery.getCountry())) {
            throw new ConflictException("Winery already exists: " + winery.getName() + " (" + winery.getCountry() + ")");
        }
        return wineryRepository.save(winery);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Winery update(Long id, WineryUpdateRequest request) {
        Winery winery = findById(id);

        if (!Objects.equals(winery.getVersion(), request.version())) {
            throw new ConflictException("Winery was updated by another transaction. Please refresh and retry.");
        }

        if (wineryRepository.existsByNameIgnoreCaseAndCountryIgnoreCaseAndIdNot(request.name(), request.country(), winery.getId())) {
            throw new ConflictException("Winery already exists: " + winery.getName() + " (" + winery.getCountry() + ")");
        }

        winery.setName(request.name());
        winery.setCountry(request.country());
        return wineryRepository.save(winery);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Winery patch(Long id, WineryPatchRequest request) {
        Winery winery = findById(id);

        if (!Objects.equals(winery.getVersion(), request.version())) {
            throw new ConflictException("Winery was updated by another transaction. Please refresh and retry.");
        }

        String newName = request.name() != null ? request.name() : winery.getName();
        String newCountry = request.country() != null ? request.country() : winery.getCountry();

        if (wineryRepository.existsByNameIgnoreCaseAndCountryIgnoreCaseAndIdNot(newName, newCountry, id)) {
            throw new ConflictException("Winery already exists: " + newName + " (" + newCountry + ")");
        }

        winery.setName(newName);
        winery.setCountry(newCountry);
        return wineryRepository.save(winery);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public void delete(Long id) {
        Winery winery = findById(id);

        long winesCount = wineRepository.countByWineryRef_Id(id);
        if (winesCount > 0) {
            throw new ConflictException("Cannot delete winery " + id + " because it has " + winesCount + " wines.");
        }

        wineryRepository.delete(winery);
    }

}

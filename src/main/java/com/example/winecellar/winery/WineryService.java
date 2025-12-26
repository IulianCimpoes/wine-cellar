package com.example.winecellar.winery;

import com.example.winecellar.common.exception.ConflictException;
import com.example.winecellar.common.exception.NotFoundException;
import com.example.winecellar.wine.WineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WineryService {

    private final WineryRepository wineryRepository;
    private final WineRepository wineRepository;

    public WineryService(WineryRepository wineryRepository, WineRepository wineRepository) {
        this.wineryRepository = wineryRepository;
        this.wineRepository = wineRepository;
    }

    public Winery create(Winery winery) {
        return wineryRepository.save(winery);
    }

    public Winery findById(Long id) {
        return wineryRepository.findById(id).orElseThrow(() -> new NotFoundException("Winery not found: " + id));
    }

    public List<Winery> findAll() {
        return wineryRepository.findAll();
    }

    public Winery update(Long id, WineryUpdateRequest request) {
        Winery winery = findById(id);
        winery.setName(request.name());
        winery.setCountry(request.country());
        return wineryRepository.save(winery);
    }

    public void delete(Long id) {
        Winery winery = findById(id);

        long winesCount = wineRepository.countByWineryRef_Id(id);
        if (winesCount > 0) {
            throw new ConflictException("Cannot delete winery " + id + " because it has " + winesCount + " wines.");
        }

        wineryRepository.delete(winery);
    }


}

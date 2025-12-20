package com.example.winecellar.wine;

import com.example.winecellar.winery.WineryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps between persistence model (Wine) and API model (WineResponse).
 */
@Component
public class WineMapper {


    private final WineryMapper  wineryMapper;

    public WineMapper(WineryMapper wineryMapper) {
        this.wineryMapper = wineryMapper;
    }

    public WineResponse toResponse(Wine wine) {
        if (wine == null) {
            return null;
        }
        return new WineResponse(
                wine.getId(),
                wine.getName(),
                wineryMapper.toResponse(wine.getWineryRef()),
                wine.getCountry(),
                wine.getWineYear(),
                wine.getPrice()
        );
    }

    public List<WineResponse> toResponseList(List<Wine> wines) {
        return wines.stream()
                .map(this::toResponse)
                .toList();
    }
}

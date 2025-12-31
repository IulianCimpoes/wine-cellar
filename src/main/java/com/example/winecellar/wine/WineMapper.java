package com.example.winecellar.wine;

import com.example.winecellar.winery.Winery;
import com.example.winecellar.winery.WineryMapper;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Maps between persistence model (Wine) and API model (WineResponse).
 */
@Component
public class WineMapper {


    private final WineryMapper wineryMapper;

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
                wine.getPrice(),
                wine.getVersion()
        );
    }

    public List<WineResponse> toResponseList(List<Wine> wines) {
        return wines.stream()
                    .map(this::toResponse)
                    .toList();
    }

    public Wine toEntity(WineCreateRequest request, Winery winery) {
        return Wine.builder()
                   .name(request.name())
                   .wineryRef(winery)
                   .country(request.country())
                   .wineYear(request.wineYear())
                   .price(request.price())
                   .build();
    }

    public Wine toEntity(WineUpdateRequest request, Winery winery) {
        return Wine.builder()
                   .name(request.name())
                   .wineryRef(winery)
                   .country(request.country())
                   .wineYear(request.wineYear())
                   .price(request.price())
                   .build();
    }
}

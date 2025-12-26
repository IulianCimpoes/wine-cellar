package com.example.winecellar.winery;

import org.springframework.stereotype.Component;

@Component
public class WineryMapper {

    public WineryResponse toResponse(Winery winery) {
        if (winery == null) {
            return null;
        }
        return new WineryResponse(
                winery.getId(),
                winery.getName(),
                winery.getCountry()
        );
    }

    public Winery toEntity(WineryCreateRequest request){
        return Winery.builder()
                              .name(request.name())
                              .country(request.country())
                              .build();
    }
}

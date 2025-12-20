package com.example.winecellar.wine;

import com.example.winecellar.winery.WineryService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wines")
public class WineController {

    private final WineService wineService;
    private final WineMapper wineMapper;
    private final WineryService wineryService;

    public WineController(WineService wineService,
                          WineMapper wineMapper,
                          WineryService wineryService) {
        this.wineService = wineService;
        this.wineMapper = wineMapper;
        this.wineryService = wineryService;
    }

    @GetMapping
    public List<WineResponse> getAll(@RequestParam(name = "country", required = false) String country) {
        if (country == null || country.isBlank()) {
            return wineMapper.toResponseList(wineService.findAll());
        } else {
            return wineMapper.toResponseList(wineService.findByCountry(country));
        }
    }

    @PostMapping
    public WineResponse create(@Valid @RequestBody WineCreateRequest request) {
        var winery = wineryService.findById(request.wineryId());

        var wine = Wine.builder()
                .name(request.name())
                .wineryRef(winery)
                .country(request.country())
                .wineYear(request.wineYear())
                .price(request.price())
                .build();

        var saved = wineService.create(wine);
        return wineMapper.toResponse(saved);
    }

    @GetMapping("/{id}")
    public WineResponse getById(@PathVariable Long id) {
        return wineMapper.toResponse(wineService.findById(id));
    }
}

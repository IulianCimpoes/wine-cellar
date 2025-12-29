package com.example.winecellar.wine;

import com.example.winecellar.winery.WineryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wines")
public class WineController {

    private final WineService wineService;
    private final WineMapper wineMapper;
    private final WineryService wineryService;

    public WineController(WineService wineService, WineMapper wineMapper, WineryService wineryService) {
        this.wineService = wineService;
        this.wineMapper = wineMapper;
        this.wineryService = wineryService;
    }

    @GetMapping
    public List<WineResponse> getAll(@RequestParam(name = "country", required = false) String country) {
        if (country == null || country.isBlank()) {
            return wineMapper.toResponseList(wineService.findAllWithWinery());
        } else {
            return wineMapper.toResponseList(wineService.findByCountryWithWinery(country));
        }
    }

    @GetMapping("/paged")
    public Page<WineResponse> getPaged(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "5") int size, @RequestParam(defaultValue = "name") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return wineService.findAllPaged(pageable)
                          .map(wineMapper::toResponse);
    }

    @PostMapping
    public WineResponse create(@Valid @RequestBody WineCreateRequest request) {
        var winery = wineryService.findById(request.wineryId());
        var saved = wineService.create(wineMapper.toEntity(request, winery));
        return wineMapper.toResponse(saved);
    }

    @GetMapping("/{id}")
    public WineResponse getById(@PathVariable Long id) {
        return wineMapper.toResponse(wineService.findById(id));
    }

    @PutMapping("/{id}")
    public WineResponse update(@PathVariable Long id,
                               @Valid @RequestBody WineUpdateRequest request) {
        return wineMapper.toResponse(wineService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        wineService.delete(id);
    }


}

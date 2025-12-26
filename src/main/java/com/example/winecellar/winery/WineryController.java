package com.example.winecellar.winery;

import com.example.winecellar.wine.WineMapper;
import com.example.winecellar.wine.WineResponse;
import com.example.winecellar.wine.WineService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wineries")
public class WineryController {

    private final WineryService wineryService;
    private final WineryMapper wineryMapper;
    private final WineService wineService;
    private final WineMapper wineMapper;

    public WineryController(WineryService wineryService, WineryMapper wineryMapper, WineService wineService, WineMapper wineMapper) {
        this.wineryService = wineryService;
        this.wineryMapper = wineryMapper;
        this.wineService = wineService;
        this.wineMapper = wineMapper;
    }

    @PostMapping
    public WineryResponse create(@Valid @RequestBody WineryCreateRequest request) {
        return wineryMapper.toResponse(wineryService.create(wineryMapper.toEntity(request)));
    }

    @GetMapping
    public List<WineryResponse> getAll() {
        return wineryService.findAll().stream()
                .map(wineryMapper::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public WineryResponse getById(@PathVariable Long id) {
        return wineryMapper.toResponse(wineryService.findById(id));
    }

    @PutMapping("/{id}")
    public WineryResponse update(@PathVariable Long id,
                                 @Valid @RequestBody WineryUpdateRequest request) {
        return wineryMapper.toResponse(wineryService.update(id, request));
    }

    @GetMapping("/{id}/wines")
    public List<WineResponse> getWinesForWinery(@PathVariable Long id) {
        // ensure winery exists (nice 404 instead of empty list for wrong id)
        wineryService.findById(id);
        return wineMapper.toResponseList(wineService.findByWineryId(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        wineryService.delete(id);
    }



}

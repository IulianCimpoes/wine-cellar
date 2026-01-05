package com.example.winecellar.winery;

import com.example.winecellar.wine.WineMapper;
import com.example.winecellar.wine.WineResponse;
import com.example.winecellar.wine.WineService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/wineries", "/api/v1/wineries"})
public class WineryControllerV1 {

    private final WineryService wineryService;
    private final WineryMapper wineryMapper;
    private final WineService wineService;
    private final WineMapper wineMapper;

    public WineryControllerV1(WineryService wineryService, WineryMapper wineryMapper, WineService wineService, WineMapper wineMapper) {
        this.wineryService = wineryService;
        this.wineryMapper = wineryMapper;
        this.wineService = wineService;
        this.wineMapper = wineMapper;
    }

    @PostMapping
    public WineryResponse create(@Valid @RequestBody WineryCreateRequest request) {
        return wineryMapper.toResponse(wineryService.create(wineryMapper.toEntity(request)));
    }

    @PutMapping("/{id}")
    public WineryResponse update(@PathVariable Long id,
                                 @Valid @RequestBody WineryUpdateRequest request) {
        return wineryMapper.toResponse(wineryService.update(id, request));
    }

    @PatchMapping("/{id}")
    public WineryResponse patch(@PathVariable Long id,
                                @Valid @RequestBody WineryPatchRequest request) {
        return wineryMapper.toResponse(wineryService.patch(id, request));
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

    @GetMapping("/{id}/wines")
    public List<WineResponse> getWinesForWinery(@PathVariable Long id) {
        wineryService.findById(id);
        return wineMapper.toResponseList(wineService.findByWineryId(id));
    }

    @GetMapping("/paged")
    public Page<WineryResponse> getPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return wineryService.findAllPaged(pageable).map(wineryMapper::toResponse);
    }

    @GetMapping("/{id}/wines/paged")
    public Page<WineResponse> getWinesForWineryPaged(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "name") String sort
    ) {
        wineryService.findById(id); // ensure 404 if winery missing
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return wineService.findByWineryIdPaged(id, pageable).map(wineMapper::toResponse);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        wineryService.delete(id);
    }



}

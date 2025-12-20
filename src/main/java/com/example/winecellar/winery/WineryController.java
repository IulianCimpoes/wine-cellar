package com.example.winecellar.winery;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wineries")
public class WineryController {

    private final WineryService wineryService;
    private final WineryMapper wineryMapper;

    public WineryController(WineryService wineryService, WineryMapper wineryMapper) {
        this.wineryService = wineryService;
        this.wineryMapper = wineryMapper;
    }

    @PostMapping
    public WineryResponse create(@Valid @RequestBody WineryCreateRequest request) {
        var winery = Winery.builder()
                .name(request.name())
                .country(request.country())
                .build();

        return wineryMapper.toResponse(wineryService.create(winery));
    }

    @GetMapping
    public List<WineryResponse> getAll() {
        return wineryService.findAll().stream()
                .map(wineryMapper::toResponse)
                .toList();
    }
}

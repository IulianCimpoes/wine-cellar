package com.example.winecellar.winery;

import com.example.winecellar.common.api.PagedResponse;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v2/wineries")
@Validated
public class WineryControllerV2 {

    private final WineryService wineryService;
    private final WineryMapper wineryMapper;

    public WineryControllerV2(WineryService wineryService, WineryMapper wineryMapper) {
        this.wineryService = wineryService;
        this.wineryMapper = wineryMapper;
    }

    @GetMapping
    public PagedResponse<WineryResponse> list(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = "desc".equalsIgnoreCase(direction)
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<WineryResponse> result = wineryService.findAllPaged(pageable)
                .map(wineryMapper::toResponse);

        return PagedResponse.from(result);
    }
}

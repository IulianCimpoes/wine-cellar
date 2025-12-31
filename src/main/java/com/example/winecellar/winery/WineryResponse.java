package com.example.winecellar.winery;

public record WineryResponse(
        Long id,
        String name,
        String country,
        Long  version
) {}

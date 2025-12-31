package com.example.winecellar.wine;

import com.example.winecellar.winery.WineryResponse;

import java.math.BigDecimal;

/**
 * API response shape for wines.
 * This is what we send back to clients.
 */
public record WineResponse(
        Long id,
        String name,
        WineryResponse winery,
        String country,
        int wineYear,
        BigDecimal price,
        Long version
) {}

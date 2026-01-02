package com.example.winecellar.wine;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WinePatchRequest(
        String name,
        Integer wineYear,
        BigDecimal price,
        Long wineryId,
        String country,
        @NotNull(message = "Version is required")
        Long version
) {
    @AssertTrue(message = "At least one field must be provided")
    public boolean hasAtLeastOneField() {
        return name != null || wineYear != null || price != null || wineryId != null || country != null;
    }
}

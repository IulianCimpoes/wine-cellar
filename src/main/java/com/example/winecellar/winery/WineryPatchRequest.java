package com.example.winecellar.winery;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record WineryPatchRequest(
        String name,
        String country,
        @NotNull(message = "Version is required")
        Long version
) {
    @AssertTrue(message = "At least one field must be provided: name or country")
    public boolean hasAtLeastOneField() {
        return name != null || country != null;
    }
}

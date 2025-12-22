package com.example.winecellar.winery;

import jakarta.validation.constraints.NotBlank;

public record WineryUpdateRequest(
        @NotBlank String name,
        @NotBlank String country
) {}

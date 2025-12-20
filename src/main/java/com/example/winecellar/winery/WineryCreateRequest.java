package com.example.winecellar.winery;

import jakarta.validation.constraints.NotBlank;

public record WineryCreateRequest(
        @NotBlank String name,
        @NotBlank String country
) {}

package com.example.winecellar.winery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WineryUpdateRequest(@NotBlank String name, @NotBlank String country, @NotNull Long version) {
}

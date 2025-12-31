package com.example.winecellar.wine;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record WineUpdateRequest(

        @NotBlank(message = "Name is required") String name,

        @NotNull(message = "Winery Id is required") Long wineryId,

        @NotBlank(message = "Country is required") String country,

        @Min(value = 1900, message = "Year must be >= 1900") @Max(value = 2100, message = "Year must be <= 2100") int wineYear,

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive") BigDecimal price,

        @NotNull(message = "Version is required") Long version) {
}

package com.example.winecellar.common.api;

public record PageMeta(
        int number,
        int size,
        long totalElements,
        int totalPages
) {}

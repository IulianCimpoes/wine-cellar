package com.example.winecellar.common.events;

public record WineryCreatedPayload(
        Long wineryId,
        String name,
        String country
) {}

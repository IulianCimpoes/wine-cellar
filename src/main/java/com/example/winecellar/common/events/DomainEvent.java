package com.example.winecellar.common.events;

import java.time.Instant;
import java.util.UUID;

public record DomainEvent<T>(
        UUID eventId,
        String eventType,
        int schemaVersion,
        Instant occurredAt,
        String requestId,
        T payload
) {}

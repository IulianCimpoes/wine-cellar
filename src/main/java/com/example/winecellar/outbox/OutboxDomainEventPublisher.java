package com.example.winecellar.outbox;

import com.example.winecellar.common.events.DomainEvent;
import com.example.winecellar.common.events.DomainEventPublisher;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
@Profile({"dev", "prod"})
public class OutboxDomainEventPublisher implements DomainEventPublisher {

    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public OutboxDomainEventPublisher(OutboxEventRepository outboxRepo, ObjectMapper objectMapper, Clock clock) {
        this.outboxRepo = outboxRepo;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public void publish(String key, Object event) {
        if (!(event instanceof DomainEvent<?> domainEvent)) {
            throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
        }

        String payloadJson = toJson(domainEvent);

        OutboxEvent outbox = OutboxEvent.newEvent(
                domainEvent.eventType(),
                "Winery",
                key,
                payloadJson,
                domainEvent.requestId(),
                domainEvent.occurredAt() != null ? domainEvent.occurredAt() : Instant.now(clock)
        );

        outboxRepo.save(outbox);
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize domain event to outbox JSON", e);
        }
    }
}

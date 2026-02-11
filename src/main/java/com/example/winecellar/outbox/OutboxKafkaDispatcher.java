package com.example.winecellar.outbox;

import com.example.winecellar.common.events.DomainEvent;
import com.example.winecellar.common.events.EventProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Profile({"dev", "prod"})
public class OutboxKafkaDispatcher {

    private static final TypeReference<DomainEvent<JsonNode>> DOMAIN_EVENT_JSON =
            new TypeReference<>() {};

    private final OutboxEventRepository repo;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final EventProperties props;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public OutboxKafkaDispatcher(OutboxEventRepository repo,
                                KafkaTemplate<String, Object> kafkaTemplate,
                                EventProperties props,
                                ObjectMapper objectMapper,
                                Clock clock) {
        this.repo = repo;
        this.kafkaTemplate = kafkaTemplate;
        this.props = props;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:1000}")
    @Transactional
    public void dispatchBatch() {
        List<OutboxEvent> batch = repo.findTop50ByStatusOrderByOccurredAtAsc("NEW");
        if (batch.isEmpty()) return;

        for (OutboxEvent e : batch) {
            DomainEvent<JsonNode> event = fromJson(e.getPayload());

            try {
                // Block for determinism (simple + testable). We can make it async later.
                kafkaTemplate
                        .send(props.topic(), e.getAggregateId(), event)
                        .get(5, TimeUnit.SECONDS);

                e.markSent(Instant.now(clock));
            } catch (Exception ex) {
                // For now: leave as NEW so it retries next poll.
                // Later we’ll add FAILED + retry_count + last_error + backoff.
            }
        }
        // No explicit save needed if OutboxEvent is managed (loaded via JPA in txn).
        // But it’s OK to call repo.saveAll(batch) if you prefer clarity.
    }

    private DomainEvent<JsonNode> fromJson(String json) {
        try {
            return objectMapper.readValue(json, DOMAIN_EVENT_JSON);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot deserialize outbox payload to DomainEvent", e);
        }
    }
}

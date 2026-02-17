package com.example.winecellar.outbox;

import com.example.winecellar.common.events.DomainEvent;
import com.example.winecellar.common.events.EventProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.outbox.max-retries:10}")
    private int maxRetries;

    @Value("${app.outbox.max-backoff-seconds:60}")
    private long maxBackoffSeconds;

    public OutboxKafkaDispatcher(OutboxEventRepository repo,
                                KafkaTemplate<String, Object> kafkaTemplate,
                                EventProperties props,
                                ObjectMapper objectMapper,
                                Clock clock,
                                 int maxRetries,
                                 long maxBackoffSeconds) {
        this.repo = repo;
        this.kafkaTemplate = kafkaTemplate;
        this.props = props;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.maxRetries = maxRetries;
        this.maxBackoffSeconds = maxBackoffSeconds;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-delay-ms:1000}")
    @Transactional
    public void dispatchBatch() {
        Instant now = Instant.now(clock);

        List<OutboxEvent> batch = repo.findEligible(now);
        if (batch.isEmpty()) return;

        for (OutboxEvent e : batch) {
            if (e.isRetryExhausted(maxRetries)) {
                // simplest: stop retrying by moving it out of eligible states
                // (you can add DEAD later; for now mark FAILED and push nextAttempt far future)
                e.markFailed("Retry exhausted (maxRetries=" + maxRetries + ")", now.plusSeconds(365L * 24 * 3600));
                continue;
            }

            DomainEvent<JsonNode> event;
            try {
                event = fromJson(e.getPayload());
            } catch (Exception ex) {
                // payload is corrupted -> no point retrying endlessly
                e.markFailed("Deserialization failed: " + safeMsg(ex), now.plusSeconds(365L * 24 * 3600));
                continue;
            }

            try {
                kafkaTemplate.send(props.topic(), e.getAggregateId(), event)
                             .get(5, TimeUnit.SECONDS);

                e.markSent(now);
            } catch (Exception ex) {
                Instant nextAttempt = now.plusSeconds(computeBackoffSeconds(e.getRetryCount(), maxBackoffSeconds));
                e.markFailed(safeMsg(ex), nextAttempt);
            }
        }
    }

    private long computeBackoffSeconds(int currentRetryCount, long maxBackoffSeconds) {
        // currentRetryCount is "before increment" in our flow.
        // For the first failure, retryCount=0 -> backoff=1s.
        long exp = 1L << Math.min(currentRetryCount, 30); // avoid overflow
        long delay = Math.min(maxBackoffSeconds, exp);
        return Math.max(1, delay);
    }

    private String safeMsg(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.isBlank()) return ex.getClass().getSimpleName();
        // keep it bounded
        return msg.length() > 500 ? msg.substring(0, 500) : msg;
    }


    private DomainEvent<JsonNode> fromJson(String json) {
        try {
            return objectMapper.readValue(json, DOMAIN_EVENT_JSON);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot deserialize outbox payload to DomainEvent", e);
        }
    }
}

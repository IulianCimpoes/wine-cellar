package com.example.winecellar.outbox;

import com.example.winecellar.common.events.DomainEvent;
import com.example.winecellar.common.events.EventProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OutboxKafkaDispatcherTest {

    private static final String TOPIC = "winecellar.events";
    private static final Instant NOW = Instant.parse("2026-02-11T10:00:00Z");
    private static final Instant OCCURRED_AT = Instant.parse("2026-02-11T09:00:00Z");

    private OutboxEventRepository repo;

    @SuppressWarnings("unchecked")
    private KafkaTemplate<String, Object> kafkaTemplate;

    private ObjectMapper om;
    private EventProperties props;
    private Clock clock;
    private OutboxKafkaDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        repo = mock(OutboxEventRepository.class);

        kafkaTemplate = (KafkaTemplate<String, Object>) mock(KafkaTemplate.class);

        om = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        props = new EventProperties(TOPIC);
        clock = Clock.fixed(NOW, ZoneOffset.UTC);

        dispatcher = new OutboxKafkaDispatcher(repo, kafkaTemplate, props, om, clock, 10, 60);
    }

    @Test
    void dispatchBatch_success_marksSent() throws Exception {

        OutboxEvent outbox = newOutboxEvent("42", "req-123", "42", OCCURRED_AT);
        when(repo.findEligible(any())).thenReturn(List.of(outbox));

        when(kafkaTemplate.send(eq(TOPIC), eq("42"), any()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        dispatcher.dispatchBatch();

        assertThat(outbox.getStatus()).isEqualTo("SENT");
        assertThat(outbox.getSentAt()).isEqualTo(NOW);
        assertThat(outbox.getNextAttemptAt()).isNull();
        assertThat(outbox.getLastError()).isNull();

        verify(kafkaTemplate).send(eq(TOPIC), eq("42"), any());
    }

    @Test
    void dispatchBatch_failure_marksFailed_incrementsRetry_andSchedulesNextAttempt() throws Exception {
        OutboxEvent outbox = newOutboxEvent("42", "req-123", "42", OCCURRED_AT);
        when(repo.findEligible(any())).thenReturn(List.of(outbox));

        when(kafkaTemplate.send(eq(TOPIC), eq("42"), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("boom")));

        dispatcher.dispatchBatch();

        assertThat(outbox.getStatus()).isEqualTo("FAILED");
        assertThat(outbox.getRetryCount()).isEqualTo(1);

        // first failure => backoff 1s
        assertThat(outbox.getNextAttemptAt()).isEqualTo(NOW.plusSeconds(1));

        assertThat(outbox.getLastError()).contains("boom");

        verify(kafkaTemplate).send(eq(TOPIC), eq("42"), any());
    }

    @Test
    void dispatchBatch_retryExhausted_doesNotSend_andStopsImmediateRetries() throws Exception {

        OutboxEvent outbox = newOutboxEvent("42", "req-123", "42", OCCURRED_AT);

        // simulate it has already failed 10 times (maxRetries=10)
        for (int i = 0; i < 10; i++) {
            outbox.markFailed("prev", NOW);
        }

        when(repo.findEligible(any())).thenReturn(List.of(outbox));

        dispatcher.dispatchBatch();

        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());

        // - push next attempt far in the future
        assertThat(outbox.getStatus()).isEqualTo("FAILED");
        assertThat(outbox.getRetryCount()).isGreaterThanOrEqualTo(10);
        assertThat(outbox.getNextAttemptAt()).isAfter(NOW.plusSeconds(60)); // far future
        assertThat(outbox.getLastError()).contains("Retry exhausted");
    }

    private OutboxEvent newOutboxEvent(String key, String requestId, String wineryId, Instant occurredAt) throws Exception {
        ObjectNode payloadNode = JsonNodeFactory.instance.objectNode().put("wineryId", wineryId);

        DomainEvent<ObjectNode> event = new DomainEvent<>(
                UUID.randomUUID(),
                "WineryCreated",
                1,
                occurredAt,
                requestId,
                payloadNode
        );

        String payloadJson = om.writeValueAsString(event);

        return OutboxEvent.newEvent(
                "WineryCreated",
                "Winery",
                key,
                payloadJson,
                requestId,
                occurredAt
        );
    }
}

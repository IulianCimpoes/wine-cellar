package com.example.winecellar.outbox;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "outbox_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 36)
    private String aggregateId;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "request_id", length = 100)
    private String requestId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "sent_at")
    private Instant sentAt;

    public static OutboxEvent newEvent(
            String eventType,
            String aggregateType,
            String aggregateId,
            String payload,
            String requestId,
            Instant occurredAt
    ) {
        OutboxEvent e = new OutboxEvent();
        e.id = UUID.randomUUID().toString();
        e.eventType = eventType;
        e.aggregateType = aggregateType;
        e.aggregateId = aggregateId;
        e.payload = payload;
        e.requestId = requestId;
        e.occurredAt = occurredAt;
        e.status = "NEW";
        return e;
    }

    public void markSent(Instant now) {
        this.status = "SENT";
        this.sentAt = now;
    }
}

# Eventing Architecture (Kafka + Transactional Outbox)

This document describes the asynchronous eventing design of WineCellar, focusing on Kafka publishing and the Transactional Outbox pattern.

------------------------------------------------------------------------

## Motivation

WineCellar publishes domain events when business operations occur (e.g., `WineryCreated`).

Publishing directly to Kafka inside the request transaction risks distributed inconsistency:
- DB commit succeeds but Kafka publish fails
- Kafka publish succeeds but DB transaction rolls back

To eliminate these failure modes, WineCellar uses the **Transactional Outbox** pattern.

------------------------------------------------------------------------

## High-Level Design

### Synchronous (request path)

1. Persist domain change (e.g., create winery)
2. Create a `DomainEvent`
3. Insert a row into `outbox_event`
4. Commit transaction

Kafka is not contacted on the request thread.

### Asynchronous (dispatcher)

A scheduled component (`OutboxKafkaDispatcher`) periodically:
1. Selects eligible outbox rows (`NEW` or `FAILED` and due)
2. Deserializes the stored event
3. Publishes it to Kafka
4. Marks the outbox row as `SENT` or `FAILED`

------------------------------------------------------------------------

## Outbox Table

### The outbox table stores:

- event metadata (eventType, requestId, occurredAt, schemaVersion, eventId)
- aggregate routing (aggregateId used as Kafka key)
- durable payload (serialized DomainEvent JSON)
- dispatch state (status, sentAt, retry fields)

### Statuses:

- NEW – ready to publish
- FAILED – publish failed and will be retried
- SENT – successfully published
------------------------------------------------------------------------

## Retry & Backoff Policy

### Configuration:

- maxRetries = 10
- maxBackoffSeconds = 60

### Backoff:

delay = min(maxBackoffSeconds, 2^retryCount)

If retry_count >= maxRetries, the event is treated as exhausted and no longer retried immediately.

------------------------------------------------------------------------

## Kafka Publishing

### Dispatcher publishing:

1. Topic: winecellar.events
2. Key: aggregateId
3. Value: full DomainEvent payload

This preserves ordering per aggregate and enables traceability via requestId.

------------------------------------------------------------------------

## Testing

### Unit tests:

- dispatcher success path
- failure + backoff behavior
- retry exhaustion behavior

### Integration tests:

- verify creating a winery inserts an outbox row

Kafka is not required for tests by default

------------------------------------------------------------------------

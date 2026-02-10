CREATE TABLE outbox_event (
                              id VARCHAR(36) PRIMARY KEY,
                              event_type VARCHAR(100) NOT NULL,
                              aggregate_type VARCHAR(100) NOT NULL,
                              aggregate_id VARCHAR(36) NOT NULL,
                              payload CLOB NOT NULL,
                              request_id VARCHAR(100),
                              occurred_at TIMESTAMP NOT NULL,
                              status VARCHAR(20) NOT NULL
);

CREATE INDEX idx_outbox_status_occurred_at
    ON outbox_event(status, occurred_at);

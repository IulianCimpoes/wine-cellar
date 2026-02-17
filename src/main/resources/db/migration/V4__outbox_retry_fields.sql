ALTER TABLE outbox_event ADD COLUMN retry_count INT NOT NULL DEFAULT 0;
ALTER TABLE outbox_event ADD COLUMN last_error CLOB;
ALTER TABLE outbox_event ADD COLUMN next_attempt_at TIMESTAMP;

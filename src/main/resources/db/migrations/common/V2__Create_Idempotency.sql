CREATE TABLE idempotency_keys
(
    id              UUID NOT NULL,
    subscription_id UUID NOT NULL,
    CONSTRAINT pk_idempotency_keys PRIMARY KEY (id)
);

ALTER TABLE idempotency_keys
    ADD CONSTRAINT uc_idempotency_keys_subscription UNIQUE (subscription_id);

ALTER TABLE idempotency_keys
    ADD CONSTRAINT FK_IDEMPOTENCY_KEYS_ON_SUBSCRIPTION FOREIGN KEY (subscription_id) REFERENCES subscriptions (id);
CREATE TABLE idempotency_keys
(
    idempotency_key_id              UUID NOT NULL,
    subscription_id                 UUID NOT NULL,
    CONSTRAINT pk_idempotency_keys PRIMARY KEY (idempotency_key_id)
);

ALTER TABLE idempotency_keys
    ADD CONSTRAINT uc_idempotency_keys_subscription UNIQUE (subscription_id);

ALTER TABLE idempotency_keys
    ADD CONSTRAINT FK_IDEMPOTENCY_KEYS_ON_SUBSCRIPTION FOREIGN KEY (subscription_id) REFERENCES subscriptions (subscription_id);
CREATE TABLE subscriptions
(
    id                        UUID                        NOT NULL,
    legacy_payment_profile_id BIGINT                      NOT NULL,
    amount                    BIGINT                      NOT NULL,
    day_of_payment            VARCHAR(255)                NOT NULL,
    start_date                date                        NOT NULL,
    end_date                  date,
    subscription_type         VARCHAR(255)                NOT NULL,
    reference                 VARCHAR(255),
    created_by                VARCHAR(255)                NOT NULL,
    created_at                TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    prison_id                 VARCHAR(255)                NOT NULL,
    prison_number             VARCHAR(255)                NOT NULL,
    sub_account_ref           VARCHAR(255)                NOT NULL,
    CONSTRAINT pk_subscriptions PRIMARY KEY (id)
);

ALTER TABLE subscriptions
    ADD CONSTRAINT uc_legacy_payment_profile_id UNIQUE (legacy_payment_profile_id);
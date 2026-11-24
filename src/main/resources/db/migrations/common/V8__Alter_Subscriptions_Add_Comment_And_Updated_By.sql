ALTER TABLE subscriptions
    ADD COLUMN comment TEXT;
COMMENT ON COLUMN subscriptions.comment IS 'Comment text used to provide more context to the subscription. [Sensitivity: OFFICIAL-SENSITIVE]';

ALTER TABLE subscriptions_aud
    ADD COLUMN comment TEXT;
COMMENT ON COLUMN subscriptions_aud.comment IS 'Comment text used to provide more context to the subscription. [Sensitivity: OFFICIAL-SENSITIVE]';

ALTER TABLE subscriptions
    ADD COLUMN updated_by VARCHAR(20);
COMMENT ON COLUMN subscriptions.updated_by IS 'Username that last updated the subscription. [Sensitivity: STAFF]';

ALTER TABLE subscriptions_aud
    ADD COLUMN updated_by VARCHAR(20);
COMMENT ON COLUMN subscriptions_aud.updated_by IS 'Username that last updated the subscription. [Sensitivity: STAFF]';

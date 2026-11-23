ALTER TABLE subscriptions
    ADD COLUMN comment TEXT;
COMMENT ON COLUMN subscriptions.comment IS 'Comment text used to provide more context to the subscription. [Sensitivity: OFFICIAL-SENSITIVE]';
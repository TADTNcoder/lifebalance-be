ALTER TABLE resourcecapital.capital_histories
    DROP CONSTRAINT chk_capital_histories_reference_type;

ALTER TABLE resourcecapital.capital_histories
    ADD CONSTRAINT chk_capital_histories_reference_type
        CHECK (reference_type IS NULL OR reference_type IN ('MANUAL', 'TASK', 'ALLOCATION', 'TARGET_CAPITAL_CYCLE'));

ALTER TABLE resourcecapital.capital_histories
    DROP CONSTRAINT chk_capital_histories_reference_pair;

ALTER TABLE resourcecapital.capital_histories
    ADD CONSTRAINT chk_capital_histories_reference_pair
        CHECK (
            (reference_type IS NULL AND reference_id IS NULL)
            OR (reference_type = 'MANUAL' AND reference_id IS NULL)
            OR (reference_type IS NOT NULL AND reference_type <> 'MANUAL' AND reference_id IS NOT NULL)
        );

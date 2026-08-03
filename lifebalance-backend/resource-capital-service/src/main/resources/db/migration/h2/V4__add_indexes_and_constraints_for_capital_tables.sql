ALTER TABLE resourcecapital.capital_cycles
    ADD COLUMN active_owner_type_key VARCHAR(128)
        GENERATED ALWAYS AS (
            CASE
                WHEN status = 'ACTIVE' THEN CAST(owner_id AS VARCHAR) || ':' || cycle_type
                ELSE CAST(id AS VARCHAR)
            END
        );

CREATE UNIQUE INDEX uq_capital_cycles_owner_type_active
    ON resourcecapital.capital_cycles (active_owner_type_key);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM resourcecapital.capital_cycles
        WHERE NOT (
            (cycle_type = 'DAILY' AND start_date = end_date)
            OR (cycle_type = 'WEEKLY' AND end_date = start_date + 6)
            OR (
                cycle_type = 'MONTHLY'
                AND EXTRACT(DAY FROM start_date) = 1
                AND end_date = (start_date + INTERVAL '1 month' - INTERVAL '1 day')::date
            )
        )
    ) THEN
        RAISE EXCEPTION
            'Cannot add chk_resourcecapital_capital_cycles_period_by_type: capital_cycles rows violate cycle_type period rules.';
    END IF;
END $$;

ALTER TABLE resourcecapital.capital_cycles
    ADD CONSTRAINT chk_resourcecapital_capital_cycles_period_by_type
    CHECK (
        (cycle_type = 'DAILY' AND start_date = end_date)
        OR (cycle_type = 'WEEKLY' AND end_date = start_date + 6)
        OR (
            cycle_type = 'MONTHLY'
            AND EXTRACT(DAY FROM start_date) = 1
            AND end_date = (start_date + INTERVAL '1 month' - INTERVAL '1 day')::date
        )
    );

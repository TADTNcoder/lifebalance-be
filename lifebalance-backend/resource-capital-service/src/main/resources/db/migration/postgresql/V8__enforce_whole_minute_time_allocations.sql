DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM resourcecapital.capital_allocations
        WHERE capital_type = 'TIME'
          AND allocated_amount <> floor(allocated_amount)
    ) THEN
        RAISE EXCEPTION
            'Cannot add chk_capital_allocations_time_whole_minutes: fractional TIME allocation rows exist.';
    END IF;
END $$;

ALTER TABLE resourcecapital.capital_allocations
    ADD CONSTRAINT chk_capital_allocations_time_whole_minutes
    CHECK (
        capital_type <> 'TIME'
        OR allocated_amount = floor(allocated_amount)
    );

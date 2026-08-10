ALTER TABLE resourcecapital.capital_allocations
    ADD CONSTRAINT chk_capital_allocations_time_whole_minutes
    CHECK (
        capital_type <> 'TIME'
        OR allocated_amount = floor(allocated_amount)
    );

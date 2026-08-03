DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM resourcecapital.capital_cycles
        WHERE status = 'ACTIVE'
        GROUP BY owner_id, cycle_type
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION 'Cannot create uq_capital_cycles_owner_type_active: duplicate ACTIVE capital cycles exist for the same owner_id and cycle_type.';
    END IF;
END $$;

CREATE UNIQUE INDEX uq_capital_cycles_owner_type_active
    ON resourcecapital.capital_cycles (owner_id, cycle_type)
    WHERE status = 'ACTIVE';

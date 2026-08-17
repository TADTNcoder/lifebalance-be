CREATE OR REPLACE FUNCTION resourcecapital.prevent_capital_adjustment_mutation()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'capital_adjustments is append-only; % is not allowed', TG_OP
        USING ERRCODE = '45000';
END;
$$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_trigger
        WHERE tgname = 'trg_capital_adjustments_append_only'
          AND tgrelid = 'resourcecapital.capital_adjustments'::regclass
    ) THEN
        EXECUTE '
            CREATE TRIGGER trg_capital_adjustments_append_only
            BEFORE UPDATE OR DELETE ON resourcecapital.capital_adjustments
            FOR EACH ROW
            EXECUTE FUNCTION resourcecapital.prevent_capital_adjustment_mutation()
        ';
    END IF;
END $$;

COMMENT ON TABLE resourcecapital.capital_adjustments IS
    'Append-only capital adjustment ledger linked to owner and capital cycle.';

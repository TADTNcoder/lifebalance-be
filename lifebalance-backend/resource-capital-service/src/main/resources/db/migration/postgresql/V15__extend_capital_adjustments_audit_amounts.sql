ALTER TABLE resourcecapital.capital_adjustments
    ADD COLUMN IF NOT EXISTS user_id UUID,
    ADD COLUMN IF NOT EXISTS amount_delta NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS previous_amount NUMERIC(19, 4),
    ADD COLUMN IF NOT EXISTS new_amount NUMERIC(19, 4);

UPDATE resourcecapital.capital_adjustments adjustment
SET user_id = cycle.owner_id
FROM resourcecapital.capital_cycles cycle
WHERE adjustment.capital_cycle_id = cycle.id
  AND adjustment.user_id IS NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'resourcecapital'
          AND table_name = 'capital_adjustments'
          AND column_name = 'amount'
    ) THEN
        UPDATE resourcecapital.capital_adjustments
        SET previous_amount = 0,
            amount_delta = CASE
                WHEN adjustment_type = 'DECREASE' THEN -amount
                ELSE amount
            END,
            new_amount = CASE
                WHEN adjustment_type = 'DECREASE' THEN 0
                ELSE amount
            END
        WHERE amount_delta IS NULL
          AND amount IS NOT NULL;
    END IF;
END $$;

ALTER TABLE resourcecapital.capital_adjustments
    ALTER COLUMN user_id SET NOT NULL,
    ALTER COLUMN amount_delta SET NOT NULL,
    ALTER COLUMN previous_amount SET NOT NULL,
    ALTER COLUMN new_amount SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_adjustments_previous_amount'
          AND conrelid = 'resourcecapital.capital_adjustments'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_adjustments
            ADD CONSTRAINT chk_capital_adjustments_previous_amount
            CHECK (previous_amount >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_adjustments_new_amount'
          AND conrelid = 'resourcecapital.capital_adjustments'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_adjustments
            ADD CONSTRAINT chk_capital_adjustments_new_amount
            CHECK (new_amount >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_capital_adjustments_amount_delta'
          AND conrelid = 'resourcecapital.capital_adjustments'::regclass
    ) THEN
        ALTER TABLE resourcecapital.capital_adjustments
            ADD CONSTRAINT chk_capital_adjustments_amount_delta
            CHECK (amount_delta = new_amount - previous_amount);
    END IF;
END $$;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS resourcecapital.money_capitals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    capital_cycle_id UUID NOT NULL,
    allocated_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    available_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    spent_amount NUMERIC(19, 4) NOT NULL DEFAULT 0,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

ALTER TABLE resourcecapital.money_capitals
    ADD COLUMN IF NOT EXISTS allocated_amount NUMERIC(19, 4) NOT NULL DEFAULT 0;

ALTER TABLE resourcecapital.money_capitals
    ADD COLUMN IF NOT EXISTS available_amount NUMERIC(19, 4) NOT NULL DEFAULT 0;

ALTER TABLE resourcecapital.money_capitals
    ADD COLUMN IF NOT EXISTS spent_amount NUMERIC(19, 4) NOT NULL DEFAULT 0;

ALTER TABLE resourcecapital.money_capitals
    ADD COLUMN IF NOT EXISTS currency VARCHAR(3);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'resourcecapital'
          AND table_name = 'money_capitals'
          AND column_name = 'currency_code'
    ) THEN
        UPDATE resourcecapital.money_capitals
        SET currency = UPPER(currency_code)
        WHERE currency IS NULL;
    END IF;
END $$;

UPDATE resourcecapital.money_capitals
SET currency = 'VND'
WHERE currency IS NULL;

ALTER TABLE resourcecapital.money_capitals
    ALTER COLUMN currency SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_money_capitals_cycle'
          AND conrelid = 'resourcecapital.money_capitals'::regclass
    ) THEN
        ALTER TABLE resourcecapital.money_capitals
            ADD CONSTRAINT fk_money_capitals_cycle
            FOREIGN KEY (capital_cycle_id)
            REFERENCES resourcecapital.capital_cycles (id)
            ON DELETE RESTRICT;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'uk_money_capitals_cycle'
          AND conrelid = 'resourcecapital.money_capitals'::regclass
    ) THEN
        ALTER TABLE resourcecapital.money_capitals
            ADD CONSTRAINT uk_money_capitals_cycle
            UNIQUE (capital_cycle_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_money_capitals_allocated_amount'
          AND conrelid = 'resourcecapital.money_capitals'::regclass
    ) THEN
        ALTER TABLE resourcecapital.money_capitals
            ADD CONSTRAINT chk_money_capitals_allocated_amount
            CHECK (allocated_amount >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_money_capitals_available_amount'
          AND conrelid = 'resourcecapital.money_capitals'::regclass
    ) THEN
        ALTER TABLE resourcecapital.money_capitals
            ADD CONSTRAINT chk_money_capitals_available_amount
            CHECK (available_amount >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_money_capitals_spent_amount'
          AND conrelid = 'resourcecapital.money_capitals'::regclass
    ) THEN
        ALTER TABLE resourcecapital.money_capitals
            ADD CONSTRAINT chk_money_capitals_spent_amount
            CHECK (spent_amount >= 0);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_money_capitals_currency'
          AND conrelid = 'resourcecapital.money_capitals'::regclass
    ) THEN
        ALTER TABLE resourcecapital.money_capitals
            ADD CONSTRAINT chk_money_capitals_currency
            CHECK (currency ~ '^[A-Z]{3}$');
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_money_capitals_capital_cycle_id
    ON resourcecapital.money_capitals (capital_cycle_id);

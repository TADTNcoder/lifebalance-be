-- Remove legacy balance columns introduced by V10/V11.
-- Current JPA entities keep planned capital on time_capitals/money_capitals,
-- while allocation/spent/remaining values are derived from capital_allocations.
-- The legacy money_capitals.currency column is NOT NULL and is not mapped by
-- MoneyCapital, causing inserts to fail after a cycle is created.

ALTER TABLE resourcecapital.time_capitals
    DROP CONSTRAINT IF EXISTS chk_time_capitals_allocated_hours,
    DROP CONSTRAINT IF EXISTS chk_time_capitals_available_hours,
    DROP CONSTRAINT IF EXISTS chk_time_capitals_spent_hours;

ALTER TABLE resourcecapital.time_capitals
    DROP COLUMN IF EXISTS allocated_hours,
    DROP COLUMN IF EXISTS available_hours,
    DROP COLUMN IF EXISTS spent_hours;

ALTER TABLE resourcecapital.money_capitals
    DROP CONSTRAINT IF EXISTS chk_money_capitals_allocated_amount,
    DROP CONSTRAINT IF EXISTS chk_money_capitals_available_amount,
    DROP CONSTRAINT IF EXISTS chk_money_capitals_spent_amount,
    DROP CONSTRAINT IF EXISTS chk_money_capitals_currency;

ALTER TABLE resourcecapital.money_capitals
    DROP COLUMN IF EXISTS allocated_amount,
    DROP COLUMN IF EXISTS available_amount,
    DROP COLUMN IF EXISTS spent_amount,
    DROP COLUMN IF EXISTS currency;

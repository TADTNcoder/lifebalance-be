ALTER TABLE finance.financial_transactions
    ADD COLUMN IF NOT EXISTS income_source_type VARCHAR(32) NOT NULL DEFAULT 'ONE_OFF';

ALTER TABLE finance.financial_transactions
    ADD COLUMN IF NOT EXISTS salary_period VARCHAR(7);

ALTER TABLE finance.financial_transactions
    ADD COLUMN IF NOT EXISTS base_salary NUMERIC(19, 4);

ALTER TABLE finance.financial_transactions
    ADD COLUMN IF NOT EXISTS bonus_amount NUMERIC(19, 4);

ALTER TABLE finance.financial_transactions
    ADD COLUMN IF NOT EXISTS deduction_amount NUMERIC(19, 4);

CREATE INDEX IF NOT EXISTS idx_fin_transactions_monthly_salary
    ON finance.financial_transactions(owner_id, task_id, salary_period, status);

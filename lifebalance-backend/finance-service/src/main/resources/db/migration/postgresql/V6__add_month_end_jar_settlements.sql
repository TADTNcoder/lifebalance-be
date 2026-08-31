ALTER TABLE finance.financial_transactions
    ADD COLUMN IF NOT EXISTS system_generated BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS finance.finance_monthly_jar_settlements (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    period_start DATE NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    main_pool_account_id UUID NOT NULL,
    jar_account_id UUID NOT NULL,
    settlement_transaction_id UUID,
    allocated_amount NUMERIC(19, 4) NOT NULL,
    actual_expense_amount NUMERIC(19, 4) NOT NULL,
    closing_balance NUMERIC(19, 4) NOT NULL,
    transferred_amount NUMERIC(19, 4) NOT NULL,
    variance_amount NUMERIC(19, 4) NOT NULL,
    settled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fin_monthly_settlement_main_pool
        FOREIGN KEY (main_pool_account_id) REFERENCES finance.finance_accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fin_monthly_settlement_jar
        FOREIGN KEY (jar_account_id) REFERENCES finance.finance_accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fin_monthly_settlement_transaction
        FOREIGN KEY (settlement_transaction_id) REFERENCES finance.financial_transactions(id) ON DELETE RESTRICT,
    CONSTRAINT uq_fin_monthly_settlement_jar_period UNIQUE (jar_account_id, period_start),
    CONSTRAINT uq_fin_monthly_settlement_transaction UNIQUE (settlement_transaction_id),
    CONSTRAINT chk_fin_monthly_settlement_period_start
        CHECK (period_start = date_trunc('month', period_start)::date),
    CONSTRAINT chk_fin_monthly_settlement_currency
        CHECK (currency_code = upper(currency_code) AND length(currency_code) = 3),
    CONSTRAINT chk_fin_monthly_settlement_amounts
        CHECK (allocated_amount >= 0 AND actual_expense_amount >= 0 AND transferred_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_fin_monthly_settlement_owner_period
    ON finance.finance_monthly_jar_settlements(owner_id, currency_code, period_start);

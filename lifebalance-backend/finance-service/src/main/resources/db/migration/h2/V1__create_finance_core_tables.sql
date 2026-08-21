CREATE SCHEMA IF NOT EXISTS finance;

CREATE TABLE IF NOT EXISTS finance.finance_accounts (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    account_type VARCHAR(32) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    opening_balance NUMERIC(19, 4) NOT NULL DEFAULT 0,
    current_balance NUMERIC(19, 4) NOT NULL DEFAULT 0,
    status VARCHAR(16) NOT NULL,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_finance_accounts_type
        CHECK (account_type IN ('CASH', 'BANK_ACCOUNT', 'CREDIT_CARD', 'DIGITAL_WALLET', 'SAVINGS', 'INVESTMENT', 'OTHER')),
    CONSTRAINT chk_finance_accounts_status
        CHECK (status IN ('ACTIVE', 'ARCHIVED')),
    CONSTRAINT chk_finance_accounts_currency
        CHECK (currency_code = upper(currency_code) AND length(currency_code) = 3),
    CONSTRAINT chk_finance_accounts_opening_balance
        CHECK (opening_balance >= 0)
);

CREATE TABLE IF NOT EXISTS finance.finance_categories (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    name VARCHAR(120) NOT NULL,
    category_type VARCHAR(16) NOT NULL,
    color VARCHAR(20),
    icon VARCHAR(50),
    status VARCHAR(16) NOT NULL,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_finance_categories_type
        CHECK (category_type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT chk_finance_categories_status
        CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE TABLE IF NOT EXISTS finance.financial_transactions (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    transaction_type VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    source_account_id UUID,
    destination_account_id UUID,
    category_id UUID,
    amount NUMERIC(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    transaction_date TIMESTAMP WITH TIME ZONE NOT NULL,
    description VARCHAR(1000),
    task_id UUID,
    capital_cycle_id UUID,
    capital_allocation_id UUID,
    voided_at TIMESTAMP WITH TIME ZONE,
    void_reason VARCHAR(1000),
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_fin_transactions_source_account
        FOREIGN KEY (source_account_id) REFERENCES finance.finance_accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fin_transactions_destination_account
        FOREIGN KEY (destination_account_id) REFERENCES finance.finance_accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fin_transactions_category
        FOREIGN KEY (category_id) REFERENCES finance.finance_categories(id) ON DELETE RESTRICT,
    CONSTRAINT chk_fin_transactions_type
        CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'TRANSFER')),
    CONSTRAINT chk_fin_transactions_status
        CHECK (status IN ('POSTED', 'VOIDED')),
    CONSTRAINT chk_fin_transactions_amount
        CHECK (amount > 0),
    CONSTRAINT chk_fin_transactions_currency
        CHECK (currency_code = upper(currency_code) AND length(currency_code) = 3),
    CONSTRAINT chk_fin_transactions_accounts
        CHECK (
            (transaction_type = 'INCOME' AND source_account_id IS NULL AND destination_account_id IS NOT NULL)
            OR (transaction_type = 'EXPENSE' AND source_account_id IS NOT NULL AND destination_account_id IS NULL)
            OR (transaction_type = 'TRANSFER' AND source_account_id IS NOT NULL AND destination_account_id IS NOT NULL AND source_account_id <> destination_account_id)
        )
);

CREATE TABLE IF NOT EXISTS finance.finance_budgets (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    category_id UUID,
    name VARCHAR(120) NOT NULL,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    amount_limit NUMERIC(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    alert_threshold_percent NUMERIC(5, 2) NOT NULL DEFAULT 80,
    status VARCHAR(16) NOT NULL,
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_fin_budgets_category
        FOREIGN KEY (category_id) REFERENCES finance.finance_categories(id) ON DELETE RESTRICT,
    CONSTRAINT chk_fin_budgets_amount_limit
        CHECK (amount_limit > 0),
    CONSTRAINT chk_fin_budgets_threshold
        CHECK (alert_threshold_percent >= 0 AND alert_threshold_percent <= 100),
    CONSTRAINT chk_fin_budgets_currency
        CHECK (currency_code = upper(currency_code) AND length(currency_code) = 3),
    CONSTRAINT chk_fin_budgets_period
        CHECK (period_end >= period_start),
    CONSTRAINT chk_fin_budgets_status
        CHECK (status IN ('ACTIVE', 'ARCHIVED'))
);

CREATE TABLE IF NOT EXISTS finance.recurring_transaction_rules (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    transaction_type VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    source_account_id UUID,
    destination_account_id UUID,
    category_id UUID,
    amount NUMERIC(19, 4) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    frequency VARCHAR(16) NOT NULL,
    interval_count INTEGER NOT NULL,
    starts_on DATE NOT NULL,
    next_run_date DATE NOT NULL,
    ends_on DATE,
    description VARCHAR(1000),
    created_by UUID,
    updated_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_fin_recurring_source_account
        FOREIGN KEY (source_account_id) REFERENCES finance.finance_accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fin_recurring_destination_account
        FOREIGN KEY (destination_account_id) REFERENCES finance.finance_accounts(id) ON DELETE RESTRICT,
    CONSTRAINT fk_fin_recurring_category
        FOREIGN KEY (category_id) REFERENCES finance.finance_categories(id) ON DELETE RESTRICT,
    CONSTRAINT chk_fin_recurring_type
        CHECK (transaction_type IN ('INCOME', 'EXPENSE', 'TRANSFER')),
    CONSTRAINT chk_fin_recurring_status
        CHECK (status IN ('ACTIVE', 'PAUSED', 'ENDED')),
    CONSTRAINT chk_fin_recurring_amount
        CHECK (amount > 0),
    CONSTRAINT chk_fin_recurring_currency
        CHECK (currency_code = upper(currency_code) AND length(currency_code) = 3),
    CONSTRAINT chk_fin_recurring_frequency
        CHECK (frequency IN ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY')),
    CONSTRAINT chk_fin_recurring_interval
        CHECK (interval_count > 0),
    CONSTRAINT chk_fin_recurring_period
        CHECK (ends_on IS NULL OR ends_on >= starts_on),
    CONSTRAINT chk_fin_recurring_accounts
        CHECK (
            (transaction_type = 'INCOME' AND source_account_id IS NULL AND destination_account_id IS NOT NULL)
            OR (transaction_type = 'EXPENSE' AND source_account_id IS NOT NULL AND destination_account_id IS NULL)
            OR (transaction_type = 'TRANSFER' AND source_account_id IS NOT NULL AND destination_account_id IS NOT NULL AND source_account_id <> destination_account_id)
        )
);

CREATE TABLE IF NOT EXISTS finance.finance_histories (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL,
    actor_id UUID,
    action_type VARCHAR(64) NOT NULL,
    reference_type VARCHAR(64) NOT NULL,
    reference_id UUID NOT NULL,
    reason VARCHAR(1000),
    old_value TEXT,
    new_value TEXT,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_fin_accounts_owner_status
    ON finance.finance_accounts(owner_id, status);
CREATE INDEX IF NOT EXISTS idx_fin_accounts_owner_currency
    ON finance.finance_accounts(owner_id, currency_code);
CREATE INDEX IF NOT EXISTS idx_fin_categories_owner_type_status
    ON finance.finance_categories(owner_id, category_type, status);
CREATE INDEX IF NOT EXISTS idx_fin_transactions_owner_date
    ON finance.financial_transactions(owner_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_fin_transactions_owner_type_status
    ON finance.financial_transactions(owner_id, transaction_type, status);
CREATE INDEX IF NOT EXISTS idx_fin_transactions_account_date
    ON finance.financial_transactions(source_account_id, destination_account_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_fin_transactions_category_date
    ON finance.financial_transactions(category_id, transaction_date);
CREATE INDEX IF NOT EXISTS idx_fin_transactions_task
    ON finance.financial_transactions(owner_id, task_id);
CREATE INDEX IF NOT EXISTS idx_fin_transactions_capital_refs
    ON finance.financial_transactions(owner_id, capital_cycle_id, capital_allocation_id);
CREATE INDEX IF NOT EXISTS idx_fin_budgets_owner_period
    ON finance.finance_budgets(owner_id, period_start, period_end, status);
CREATE INDEX IF NOT EXISTS idx_fin_budgets_category_period
    ON finance.finance_budgets(category_id, period_start, period_end);
CREATE INDEX IF NOT EXISTS idx_fin_recurring_owner_next_run
    ON finance.recurring_transaction_rules(owner_id, status, next_run_date);
CREATE INDEX IF NOT EXISTS idx_fin_histories_owner_time
    ON finance.finance_histories(owner_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_fin_histories_reference
    ON finance.finance_histories(reference_type, reference_id, occurred_at);

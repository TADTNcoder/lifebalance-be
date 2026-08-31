ALTER TABLE finance.financial_transactions
    ADD COLUMN IF NOT EXISTS transaction_name VARCHAR(255);

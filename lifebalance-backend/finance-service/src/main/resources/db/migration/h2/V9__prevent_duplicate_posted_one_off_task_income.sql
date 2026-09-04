ALTER TABLE finance.financial_transactions
    ADD COLUMN IF NOT EXISTS posted_one_off_task_income_key VARCHAR(128)
    GENERATED ALWAYS AS (
        CASE
            WHEN transaction_type = 'INCOME'
                AND income_source_type = 'ONE_OFF'
                AND status = 'POSTED'
                AND task_id IS NOT NULL
            THEN CAST(owner_id AS VARCHAR) || ':' || CAST(task_id AS VARCHAR)
            ELSE NULL
        END
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_fin_transactions_posted_one_off_task_income
    ON finance.financial_transactions(posted_one_off_task_income_key);

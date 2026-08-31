ALTER TABLE finance.financial_transactions
    ADD COLUMN IF NOT EXISTS posted_monthly_salary_key VARCHAR(128)
    GENERATED ALWAYS AS (
        CASE
            WHEN income_source_type = 'MONTHLY_SALARY' AND status = 'POSTED'
            THEN CAST(owner_id AS VARCHAR) || ':' || CAST(task_id AS VARCHAR) || ':' || salary_period
            ELSE NULL
        END
    );

CREATE UNIQUE INDEX IF NOT EXISTS uq_fin_transactions_posted_monthly_salary
    ON finance.financial_transactions(posted_monthly_salary_key);

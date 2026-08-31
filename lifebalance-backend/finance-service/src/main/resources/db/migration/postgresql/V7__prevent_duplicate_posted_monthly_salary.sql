CREATE UNIQUE INDEX IF NOT EXISTS uq_fin_transactions_posted_monthly_salary
    ON finance.financial_transactions(owner_id, task_id, salary_period)
    WHERE income_source_type = 'MONTHLY_SALARY'
      AND status = 'POSTED';

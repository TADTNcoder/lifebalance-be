CREATE UNIQUE INDEX IF NOT EXISTS uq_fin_transactions_posted_one_off_task_income
    ON finance.financial_transactions(owner_id, task_id)
    WHERE transaction_type = 'INCOME'
      AND income_source_type = 'ONE_OFF'
      AND status = 'POSTED'
      AND task_id IS NOT NULL;

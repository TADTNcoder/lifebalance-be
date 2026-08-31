UPDATE finance.finance_accounts account
SET opening_balance = account.opening_balance + COALESCE((
    SELECT SUM(posted_transfer.amount)
    FROM finance.financial_transactions posted_transfer
    WHERE posted_transfer.transaction_type = 'TRANSFER'
      AND posted_transfer.status = 'POSTED'
      AND posted_transfer.destination_account_id = account.id
), 0)
WHERE account.account_type = 'JAR'
  AND EXISTS (
      SELECT 1
      FROM finance.financial_transactions posted_transfer
      WHERE posted_transfer.transaction_type = 'TRANSFER'
        AND posted_transfer.status = 'POSTED'
        AND posted_transfer.destination_account_id = account.id
  );

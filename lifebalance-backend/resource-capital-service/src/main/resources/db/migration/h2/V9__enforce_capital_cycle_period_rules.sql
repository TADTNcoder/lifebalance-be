ALTER TABLE resourcecapital.capital_cycles
    ADD CONSTRAINT chk_resourcecapital_capital_cycles_period_by_type
    CHECK (
        (cycle_type = 'DAILY' AND start_date = end_date)
        OR (cycle_type = 'WEEKLY' AND DATEDIFF('DAY', start_date, end_date) = 6)
        OR (
            cycle_type = 'MONTHLY'
            AND DAYOFMONTH(start_date) = 1
            AND end_date = DATEADD('DAY', -1, DATEADD('MONTH', 1, start_date))
        )
    );

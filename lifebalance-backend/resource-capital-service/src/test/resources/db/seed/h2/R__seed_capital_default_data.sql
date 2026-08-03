-- H2 test equivalent of the Resource Capital development seed.

INSERT INTO resourcecapital.capital_cycles (
    id,
    owner_id,
    name,
    description,
    cycle_type,
    start_date,
    end_date,
    status,
    over_allocation_allowed,
    activated_at,
    created_at,
    updated_at,
    version
)
SELECT *
FROM (
    VALUES
    (
        '81100000-0000-4000-8000-000000082026',
        '975946b2-90c3-4206-a2de-4f6652fcaa71',
        'Chu ky Thang 8/2026',
        'Development fixture active monthly capital cycle.',
        'MONTHLY',
        DATE '2026-08-01',
        DATE '2026-08-31',
        'ACTIVE',
        false,
        TIMESTAMP WITH TIME ZONE '2026-08-01 00:00:00+00',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        0
    )
) AS seed_cycle (
    id,
    owner_id,
    name,
    description,
    cycle_type,
    start_date,
    end_date,
    status,
    over_allocation_allowed,
    activated_at,
    created_at,
    updated_at,
    version
)
WHERE NOT EXISTS (
    SELECT 1
    FROM resourcecapital.capital_cycles cycle
    WHERE cycle.id = seed_cycle.id
);

INSERT INTO resourcecapital.capital_cycles (
    id,
    owner_id,
    name,
    description,
    cycle_type,
    start_date,
    end_date,
    status,
    over_allocation_allowed,
    activated_at,
    created_at,
    updated_at,
    version
)
SELECT *
FROM (
    VALUES
    (
        '81100000-0000-4000-8000-000000092026',
        '975946b2-90c3-4206-a2de-4f6652fcaa71',
        'Chu ky Thang 9/2026',
        'Development fixture draft monthly capital cycle without initialized capital.',
        'MONTHLY',
        DATE '2026-09-01',
        DATE '2026-09-30',
        'DRAFT',
        false,
        NULL,
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        0
    )
) AS seed_cycle (
    id,
    owner_id,
    name,
    description,
    cycle_type,
    start_date,
    end_date,
    status,
    over_allocation_allowed,
    activated_at,
    created_at,
    updated_at,
    version
)
WHERE NOT EXISTS (
    SELECT 1
    FROM resourcecapital.capital_cycles cycle
    WHERE cycle.id = seed_cycle.id
);

INSERT INTO resourcecapital.time_capitals (
    id,
    capital_cycle_id,
    planned_minutes,
    created_at,
    updated_at,
    version
)
SELECT
    '81100000-0000-4000-8000-000000000001',
    cycle.id,
    9600,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM resourcecapital.capital_cycles cycle
WHERE cycle.id = '81100000-0000-4000-8000-000000082026'
  AND NOT EXISTS (
      SELECT 1
      FROM resourcecapital.time_capitals time_capital
      WHERE time_capital.capital_cycle_id = cycle.id
  );

INSERT INTO resourcecapital.money_capitals (
    id,
    capital_cycle_id,
    planned_amount,
    currency_code,
    created_at,
    updated_at,
    version
)
SELECT
    '81100000-0000-4000-8000-000000000002',
    cycle.id,
    15000000.0000,
    'VND',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
FROM resourcecapital.capital_cycles cycle
WHERE cycle.id = '81100000-0000-4000-8000-000000082026'
  AND NOT EXISTS (
      SELECT 1
      FROM resourcecapital.money_capitals money_capital
      WHERE money_capital.capital_cycle_id = cycle.id
  );

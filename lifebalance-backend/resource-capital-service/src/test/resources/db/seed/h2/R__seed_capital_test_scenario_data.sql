-- Test-only capital scenario fixtures.
-- Loaded only when the integration-seed profile adds classpath:db/seed/h2.

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
        '18300000-0000-4000-8000-000000000100',
        '18300000-0000-4000-8000-000000000001',
        'Test Capital Scenario Cycle',
        'Integration-test fixture with allocation, over-allocation, adjustment, reallocation, release, and history.',
        'MONTHLY',
        DATE '2026-08-01',
        DATE '2026-08-31',
        'ACTIVE',
        true,
        TIMESTAMP WITH TIME ZONE '2026-08-01 00:00:00+00',
        TIMESTAMP WITH TIME ZONE '2026-08-01 00:00:00+00',
        TIMESTAMP WITH TIME ZONE '2026-08-07 10:00:00+00',
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
    '18300000-0000-4000-8000-000000000101',
    cycle.id,
    480,
    TIMESTAMP WITH TIME ZONE '2026-08-01 00:00:00+00',
    TIMESTAMP WITH TIME ZONE '2026-08-02 09:00:00+00',
    0
FROM resourcecapital.capital_cycles cycle
WHERE cycle.id = '18300000-0000-4000-8000-000000000100'
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
    '18300000-0000-4000-8000-000000000102',
    cycle.id,
    1000.0000,
    'USD',
    TIMESTAMP WITH TIME ZONE '2026-08-01 00:00:00+00',
    TIMESTAMP WITH TIME ZONE '2026-08-02 09:05:00+00',
    0
FROM resourcecapital.capital_cycles cycle
WHERE cycle.id = '18300000-0000-4000-8000-000000000100'
  AND NOT EXISTS (
      SELECT 1
      FROM resourcecapital.money_capitals money_capital
      WHERE money_capital.capital_cycle_id = cycle.id
  );

INSERT INTO resourcecapital.capital_allocations (
    id,
    capital_cycle_id,
    user_id,
    capital_type,
    target_type,
    target_id,
    allocated_amount,
    spent_amount,
    released_amount,
    status,
    is_over_allocated,
    over_allocation_confirmed,
    note,
    created_at,
    updated_at,
    version,
    created_by,
    updated_by
)
SELECT *
FROM (
    VALUES
    (
        '18300000-0000-4000-8000-000000000201',
        '18300000-0000-4000-8000-000000000100',
        '18300000-0000-4000-8000-000000000001',
        'TIME',
        'TASK',
        '18300000-0000-4000-8000-000000000301',
        70.0000,
        10.0000,
        20.0000,
        'ACTIVE',
        false,
        false,
        'Within-limit allocation after 30 minutes reallocated and 20 minutes released.',
        TIMESTAMP WITH TIME ZONE '2026-08-03 08:00:00+00',
        TIMESTAMP WITH TIME ZONE '2026-08-07 10:00:00+00',
        0,
        '18300000-0000-4000-8000-000000000001',
        '18300000-0000-4000-8000-000000000001'
    ),
    (
        '18300000-0000-4000-8000-000000000202',
        '18300000-0000-4000-8000-000000000100',
        '18300000-0000-4000-8000-000000000001',
        'TIME',
        'PROJECT',
        '18300000-0000-4000-8000-000000000302',
        630.0000,
        0.0000,
        0.0000,
        'ACTIVE',
        true,
        true,
        'Confirmed over-allocation; negative remaining is an over-limit state, not extra capital.',
        TIMESTAMP WITH TIME ZONE '2026-08-04 09:00:00+00',
        TIMESTAMP WITH TIME ZONE '2026-08-06 11:00:00+00',
        0,
        '18300000-0000-4000-8000-000000000001',
        '18300000-0000-4000-8000-000000000001'
    ),
    (
        '18300000-0000-4000-8000-000000000203',
        '18300000-0000-4000-8000-000000000100',
        '18300000-0000-4000-8000-000000000001',
        'MONEY',
        'TASK_CATALOG',
        '18300000-0000-4000-8000-000000000303',
        1500.0000,
        0.0000,
        0.0000,
        'ACTIVE',
        true,
        false,
        'Unconfirmed over-allocation fixture for validator and warning-flow tests.',
        TIMESTAMP WITH TIME ZONE '2026-08-04 10:00:00+00',
        TIMESTAMP WITH TIME ZONE '2026-08-04 10:00:00+00',
        0,
        '18300000-0000-4000-8000-000000000001',
        '18300000-0000-4000-8000-000000000001'
    )
) AS seed_allocation (
    id,
    capital_cycle_id,
    user_id,
    capital_type,
    target_type,
    target_id,
    allocated_amount,
    spent_amount,
    released_amount,
    status,
    is_over_allocated,
    over_allocation_confirmed,
    note,
    created_at,
    updated_at,
    version,
    created_by,
    updated_by
)
WHERE NOT EXISTS (
    SELECT 1
    FROM resourcecapital.capital_allocations allocation
    WHERE allocation.id = seed_allocation.id
);

INSERT INTO resourcecapital.capital_adjustments (
    capital_cycle_id,
    user_id,
    capital_type,
    adjustment_type,
    amount_delta,
    previous_amount,
    new_amount,
    reason,
    created_at,
    updated_at,
    created_by,
    updated_by
)
SELECT *
FROM (
    VALUES
    (
        '18300000-0000-4000-8000-000000000100',
        '18300000-0000-4000-8000-000000000001',
        'TIME',
        'INCREASE',
        60.0000,
        420.0000,
        480.0000,
        'Test fixture increase to cover additional planning capacity.',
        TIMESTAMP '2026-08-02 09:00:00',
        TIMESTAMP '2026-08-02 09:00:00',
        '18300000-0000-4000-8000-000000000001',
        '18300000-0000-4000-8000-000000000001'
    ),
    (
        '18300000-0000-4000-8000-000000000100',
        '18300000-0000-4000-8000-000000000001',
        'MONEY',
        'DECREASE',
        -100.0000,
        1100.0000,
        1000.0000,
        'Test fixture decrease after budget correction.',
        TIMESTAMP '2026-08-02 09:05:00',
        TIMESTAMP '2026-08-02 09:05:00',
        '18300000-0000-4000-8000-000000000001',
        '18300000-0000-4000-8000-000000000001'
    )
) AS seed_adjustment (
    capital_cycle_id,
    user_id,
    capital_type,
    adjustment_type,
    amount_delta,
    previous_amount,
    new_amount,
    reason,
    created_at,
    updated_at,
    created_by,
    updated_by
)
WHERE NOT EXISTS (
    SELECT 1
    FROM resourcecapital.capital_adjustments adjustment
    WHERE adjustment.capital_cycle_id = seed_adjustment.capital_cycle_id
      AND adjustment.user_id = seed_adjustment.user_id
      AND adjustment.capital_type = seed_adjustment.capital_type
      AND adjustment.adjustment_type = seed_adjustment.adjustment_type
      AND adjustment.created_at = seed_adjustment.created_at
);

INSERT INTO resourcecapital.capital_reallocations (
    from_allocation_id,
    to_allocation_id,
    amount,
    reason,
    created_at
)
SELECT
    '18300000-0000-4000-8000-000000000201',
    '18300000-0000-4000-8000-000000000202',
    30.0000,
    'Move unused minutes from the task allocation to the project allocation.',
    TIMESTAMP WITH TIME ZONE '2026-08-06 11:00:00+00'
WHERE NOT EXISTS (
    SELECT 1
    FROM resourcecapital.capital_reallocations reallocation
    WHERE reallocation.from_allocation_id = '18300000-0000-4000-8000-000000000201'
      AND reallocation.to_allocation_id = '18300000-0000-4000-8000-000000000202'
      AND reallocation.created_at = TIMESTAMP WITH TIME ZONE '2026-08-06 11:00:00+00'
);

INSERT INTO resourcecapital.capital_releases (
    allocation_id,
    released_amount,
    reason,
    released_at
)
SELECT
    '18300000-0000-4000-8000-000000000201',
    20.0000,
    'Release unused minutes back to the cycle remaining capital.',
    TIMESTAMP WITH TIME ZONE '2026-08-07 10:00:00+00'
WHERE NOT EXISTS (
    SELECT 1
    FROM resourcecapital.capital_releases release
    WHERE release.allocation_id = '18300000-0000-4000-8000-000000000201'
      AND release.released_at = TIMESTAMP WITH TIME ZONE '2026-08-07 10:00:00+00'
);

INSERT INTO resourcecapital.capital_histories (
    id,
    capital_cycle_id,
    capital_type,
    action_type,
    amount,
    before_amount,
    after_amount,
    reason,
    description,
    reference_type,
    reference_id,
    actor_type,
    actor_id,
    created_at
)
SELECT *
FROM (
    VALUES
    (
        '18300000-0000-4000-8000-000000000401',
        '18300000-0000-4000-8000-000000000100',
        'TIME',
        'ADJUSTMENT_INCREASE',
        60.0000,
        420.0000,
        480.0000,
        'Test fixture increase to cover additional planning capacity.',
        'Time capital adjusted upward from 420 to 480 minutes.',
        NULL,
        NULL,
        'USER',
        '18300000-0000-4000-8000-000000000001',
        TIMESTAMP WITH TIME ZONE '2026-08-02 09:00:00+00'
    ),
    (
        '18300000-0000-4000-8000-000000000402',
        '18300000-0000-4000-8000-000000000100',
        'MONEY',
        'ADJUSTMENT_DECREASE',
        100.0000,
        1100.0000,
        1000.0000,
        'Test fixture decrease after budget correction.',
        'Money capital adjusted downward from 1100 to 1000 USD.',
        NULL,
        NULL,
        'USER',
        '18300000-0000-4000-8000-000000000001',
        TIMESTAMP WITH TIME ZONE '2026-08-02 09:05:00+00'
    ),
    (
        '18300000-0000-4000-8000-000000000403',
        '18300000-0000-4000-8000-000000000100',
        'TIME',
        'ALLOCATE',
        120.0000,
        0.0000,
        120.0000,
        'Initial task allocation inside available time.',
        'Allocated 120 minutes to a task target within cycle balance.',
        'ALLOCATION',
        '18300000-0000-4000-8000-000000000201',
        'USER',
        '18300000-0000-4000-8000-000000000001',
        TIMESTAMP WITH TIME ZONE '2026-08-03 08:00:00+00'
    ),
    (
        '18300000-0000-4000-8000-000000000404',
        '18300000-0000-4000-8000-000000000100',
        'TIME',
        'ALLOCATE',
        600.0000,
        0.0000,
        600.0000,
        'User confirmed project over-allocation.',
        'Allocated 600 minutes and marked the target as over-allocated.',
        'ALLOCATION',
        '18300000-0000-4000-8000-000000000202',
        'USER',
        '18300000-0000-4000-8000-000000000001',
        TIMESTAMP WITH TIME ZONE '2026-08-04 09:00:00+00'
    ),
    (
        '18300000-0000-4000-8000-000000000405',
        '18300000-0000-4000-8000-000000000100',
        'MONEY',
        'ALLOCATE',
        1500.0000,
        0.0000,
        1500.0000,
        'User has not confirmed this over-allocation fixture.',
        'Captured an unconfirmed over-allocation state for validator tests.',
        'ALLOCATION',
        '18300000-0000-4000-8000-000000000203',
        'USER',
        '18300000-0000-4000-8000-000000000001',
        TIMESTAMP WITH TIME ZONE '2026-08-04 10:00:00+00'
    ),
    (
        '18300000-0000-4000-8000-000000000406',
        '18300000-0000-4000-8000-000000000100',
        'TIME',
        'OVER_ALLOCATION_APPROVED',
        600.0000,
        480.0000,
        -120.0000,
        'Explicit approval for planned time over-allocation.',
        'Approval records that negative remaining represents an over-limit state.',
        'ALLOCATION',
        '18300000-0000-4000-8000-000000000202',
        'USER',
        '18300000-0000-4000-8000-000000000001',
        TIMESTAMP WITH TIME ZONE '2026-08-04 09:01:00+00'
    ),
    (
        '18300000-0000-4000-8000-000000000407',
        '18300000-0000-4000-8000-000000000100',
        'TIME',
        'REALLOCATE',
        30.0000,
        120.0000,
        90.0000,
        'Move unused minutes from the task allocation to the project allocation.',
        'Reallocated 30 minutes out of the task allocation without overwriting the original allocation evidence.',
        'ALLOCATION',
        '18300000-0000-4000-8000-000000000201',
        'USER',
        '18300000-0000-4000-8000-000000000001',
        TIMESTAMP WITH TIME ZONE '2026-08-06 11:00:00+00'
    ),
    (
        '18300000-0000-4000-8000-000000000408',
        '18300000-0000-4000-8000-000000000100',
        'TIME',
        'REALLOCATE',
        30.0000,
        600.0000,
        630.0000,
        'Move unused minutes from the task allocation to the project allocation.',
        'Reallocated 30 minutes into the confirmed over-allocated project allocation.',
        'ALLOCATION',
        '18300000-0000-4000-8000-000000000202',
        'USER',
        '18300000-0000-4000-8000-000000000001',
        TIMESTAMP WITH TIME ZONE '2026-08-06 11:00:01+00'
    ),
    (
        '18300000-0000-4000-8000-000000000409',
        '18300000-0000-4000-8000-000000000100',
        'TIME',
        'RELEASE',
        20.0000,
        90.0000,
        70.0000,
        'Release unused minutes back to the cycle remaining capital.',
        'Released 20 minutes from the task allocation and preserved release history.',
        'ALLOCATION',
        '18300000-0000-4000-8000-000000000201',
        'USER',
        '18300000-0000-4000-8000-000000000001',
        TIMESTAMP WITH TIME ZONE '2026-08-07 10:00:00+00'
    )
) AS seed_history (
    id,
    capital_cycle_id,
    capital_type,
    action_type,
    amount,
    before_amount,
    after_amount,
    reason,
    description,
    reference_type,
    reference_id,
    actor_type,
    actor_id,
    created_at
)
WHERE NOT EXISTS (
    SELECT 1
    FROM resourcecapital.capital_histories history
    WHERE history.id = seed_history.id
);

-- Development seed data for Resource Capital.
-- These fixtures are intentionally loaded only by local/dev profiles, not production.
-- owner_id matches the demo Keycloak user user@lifebalance.local from the local realm export.

WITH seed_cycles(
    id,
    owner_id,
    name,
    description,
    cycle_type,
    start_date,
    end_date,
    status,
    activated_at
) AS (
    VALUES
        (
            '81100000-0000-4000-8000-000000082026'::uuid,
            '975946b2-90c3-4206-a2de-4f6652fcaa71'::uuid,
            'Chu ky Thang 8/2026',
            'Development fixture active monthly capital cycle.',
            'MONTHLY',
            DATE '2026-08-01',
            DATE '2026-08-31',
            'ACTIVE',
            TIMESTAMPTZ '2026-08-01 00:00:00+00'
        ),
        (
            '81100000-0000-4000-8000-000000092026'::uuid,
            '975946b2-90c3-4206-a2de-4f6652fcaa71'::uuid,
            'Chu ky Thang 9/2026',
            'Development fixture draft monthly capital cycle without initialized capital.',
            'MONTHLY',
            DATE '2026-09-01',
            DATE '2026-09-30',
            'DRAFT',
            NULL::timestamptz
        )
)
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
SELECT
    seed_cycle.id,
    seed_cycle.owner_id,
    seed_cycle.name,
    seed_cycle.description,
    seed_cycle.cycle_type,
    seed_cycle.start_date,
    seed_cycle.end_date,
    seed_cycle.status,
    false,
    seed_cycle.activated_at,
    now(),
    now(),
    0
FROM seed_cycles seed_cycle
ON CONFLICT DO NOTHING;

WITH active_august_cycle AS (
    SELECT cycle.id
    FROM resourcecapital.capital_cycles cycle
    WHERE cycle.id = '81100000-0000-4000-8000-000000082026'::uuid
)
INSERT INTO resourcecapital.time_capitals (
    id,
    capital_cycle_id,
    planned_minutes,
    created_at,
    updated_at,
    version
)
SELECT
    '81100000-0000-4000-8000-000000000001'::uuid,
    active_august_cycle.id,
    9600,
    now(),
    now(),
    0
FROM active_august_cycle
ON CONFLICT (capital_cycle_id) DO NOTHING;

WITH active_august_cycle AS (
    SELECT cycle.id
    FROM resourcecapital.capital_cycles cycle
    WHERE cycle.id = '81100000-0000-4000-8000-000000082026'::uuid
)
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
    '81100000-0000-4000-8000-000000000002'::uuid,
    active_august_cycle.id,
    15000000.0000,
    'VND',
    now(),
    now(),
    0
FROM active_august_cycle
ON CONFLICT (capital_cycle_id) DO NOTHING;

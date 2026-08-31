-- Task names may repeat for an owner when the task is scheduled/planned for a
-- different time. Keep the uniqueness guard for the complete time identity,
-- including untimed tasks (NULL values are normalized to a sentinel).
DROP INDEX IF EXISTS task.uq_tasks_owner_name_active;

CREATE UNIQUE INDEX uq_tasks_owner_name_time_active
ON task.tasks (
    owner_id,
    lower(trim(name)),
    COALESCE(deadline, DATE '0001-01-01'),
    COALESCE(planned_start_at, TIMESTAMPTZ '0001-01-01 00:00:00+00'),
    COALESCE(planned_end_at, TIMESTAMPTZ '0001-01-01 00:00:00+00'),
    COALESCE(scheduled_start_at, TIMESTAMPTZ '0001-01-01 00:00:00+00'),
    COALESCE(scheduled_end_at, TIMESTAMPTZ '0001-01-01 00:00:00+00')
)
WHERE deleted_at IS NULL;

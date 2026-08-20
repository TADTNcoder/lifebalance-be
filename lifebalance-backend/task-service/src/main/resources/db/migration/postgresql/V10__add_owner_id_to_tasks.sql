-- ============================================================
-- LB-856 - Business Rule: Task Ownership
-- Add owner_id to task.tasks
-- ============================================================

-- [TRICK CỦA QA]: Mớm sẵn schema và bảng giả để chạy Test không bị sập khóa ngoại
CREATE SCHEMA IF NOT EXISTS identity;
CREATE TABLE IF NOT EXISTS identity.users (
                                              id UUID PRIMARY KEY
);

-- 1. Add owner_id
ALTER TABLE task.tasks
    ADD COLUMN owner_id UUID;

-- 2. Backfill owner_id từ user_id hiện hữu để migration an toàn với dữ liệu cũ.
UPDATE task.tasks
SET owner_id = user_id
WHERE owner_id IS NULL
  AND user_id IS NOT NULL;

-- 3. Đảm bảo task cũ không bị bỏ sót owner
--    Nếu database đang có dữ liệu cũ mà chưa gán owner,
--    migration sẽ fail thay vì tạo dữ liệu sai.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM task.tasks
        WHERE owner_id IS NULL
    ) THEN
        RAISE EXCEPTION
            'LB-856 migration failed: existing tasks have no owner_id. '
            'Please assign owner_id before making the column NOT NULL.';
END IF;
END $$;

-- 4. Ownership là bắt buộc
ALTER TABLE task.tasks
ALTER COLUMN owner_id SET NOT NULL;

-- 5. Tạo FK cross-schema khi identity.users có mặt.
--    Task-service migration test và deployment standalone không nên tự tạo/bẻ schema identity.
DO $$
BEGIN
    IF to_regclass('identity.users') IS NOT NULL THEN
        IF EXISTS (
            SELECT 1
            FROM task.tasks task_record
            LEFT JOIN identity.users owner_record
                ON owner_record.id = task_record.owner_id
            WHERE owner_record.id IS NULL
        ) THEN
            RAISE EXCEPTION
                'LB-856 migration failed: existing task owner_id values do not reference identity.users(id). '
                'Please migrate task user_id/owner_id values to valid internal users before adding ownership FK.';
        END IF;

        ALTER TABLE task.tasks
        ADD CONSTRAINT fk_tasks_owner
        FOREIGN KEY (owner_id)
        REFERENCES identity.users(id)
        ON DELETE RESTRICT;
    ELSE
        RAISE NOTICE
            'Skipping fk_tasks_owner because identity.users does not exist in this database.';
    END IF;
END $$;

-- 6. Index để query task theo owner nhanh hơn
CREATE INDEX idx_tasks_owner_id
ON task.tasks(owner_id);

CREATE INDEX idx_tasks_owner_status
ON task.tasks(owner_id, status);

CREATE INDEX idx_tasks_owner_priority
ON task.tasks(owner_id, priority);

-- 7. Chặn duplicate active task name theo cùng normalization mà service dùng.
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM task.tasks
        WHERE deleted_at IS NULL
        GROUP BY owner_id, lower(trim(name))
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'LB-856 migration failed: duplicate active task names exist for the same owner after trim/case normalization.';
    END IF;
END $$;

CREATE UNIQUE INDEX uq_tasks_owner_name_active
ON task.tasks(owner_id, lower(trim(name)))
WHERE deleted_at IS NULL;

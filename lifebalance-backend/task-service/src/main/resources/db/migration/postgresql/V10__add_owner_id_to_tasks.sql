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

-- 2. Foreign key tới user nội bộ (GIỮ NGUYÊN LOGIC CỦA DEV)
ALTER TABLE task.tasks
    ADD CONSTRAINT fk_tasks_owner
        FOREIGN KEY (owner_id)
            REFERENCES identity.users(id);

-- 3. Index để query task theo owner nhanh hơn
CREATE INDEX idx_tasks_owner_id
    ON task.tasks(owner_id);

-- 4. Đảm bảo task cũ không bị bỏ sót owner
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

-- 5. Ownership là bắt buộc
ALTER TABLE task.tasks
    ALTER COLUMN owner_id SET NOT NULL;
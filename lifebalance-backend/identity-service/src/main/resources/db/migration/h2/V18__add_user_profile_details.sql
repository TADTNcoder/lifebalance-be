ALTER TABLE identity.users
    ADD COLUMN IF NOT EXISTS phone VARCHAR(20);

ALTER TABLE identity.users
    ADD COLUMN IF NOT EXISTS gender VARCHAR(50);

ALTER TABLE identity.users
    ADD COLUMN IF NOT EXISTS birth_date DATE;

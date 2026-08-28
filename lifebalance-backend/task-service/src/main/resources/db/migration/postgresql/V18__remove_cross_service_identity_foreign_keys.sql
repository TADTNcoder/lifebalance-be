-- Task service stores the identity of the owner, but it does not own the
-- identity.users table.  The identity service uses a separate database, so
-- foreign keys from task tables to identity.users can never be satisfied by a
-- normal authenticated user.  Ownership is enforced at the service boundary
-- (the authenticated owner id is used in every repository query) instead.
--
-- V10-V17 were also used by standalone migration tests, where a small
-- identity.users compatibility table was created.  Remove every FK that
-- targets that compatibility table so existing installations are repaired
-- without changing or deleting any task data.
DO $$
DECLARE
    constraint_record RECORD;
BEGIN
    FOR constraint_record IN
        SELECT
            source_namespace.nspname AS source_schema,
            source_table.relname AS source_table,
            source_constraint.conname AS constraint_name
        FROM pg_constraint source_constraint
        JOIN pg_class source_table
            ON source_table.oid = source_constraint.conrelid
        JOIN pg_namespace source_namespace
            ON source_namespace.oid = source_table.relnamespace
        WHERE source_constraint.contype = 'f'
          AND source_namespace.nspname = 'task'
          AND source_constraint.confrelid = to_regclass('identity.users')
    LOOP
        EXECUTE format(
            'ALTER TABLE %I.%I DROP CONSTRAINT IF EXISTS %I',
            constraint_record.source_schema,
            constraint_record.source_table,
            constraint_record.constraint_name
        );
    END LOOP;
END $$;

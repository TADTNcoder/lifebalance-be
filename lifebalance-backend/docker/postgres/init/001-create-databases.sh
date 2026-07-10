#!/usr/bin/env bash
set -euo pipefail

create_user_and_database() {
  local database="$1"
  local username="$2"
  local password="$3"

  psql --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    DO
    \$\$
    BEGIN
      IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '${username}') THEN
        CREATE ROLE ${username} LOGIN PASSWORD '${password}';
      END IF;
    END
    \$\$;

    SELECT 'CREATE DATABASE ${database} OWNER ${username}'
    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = '${database}')\\gexec

    GRANT ALL PRIVILEGES ON DATABASE ${database} TO ${username};
EOSQL
}

create_user_and_database "$KEYCLOAK_DB" "$KEYCLOAK_DB_USER" "$KEYCLOAK_DB_PASSWORD"
create_user_and_database "$IDENTITY_DB" "$IDENTITY_DB_USER" "$IDENTITY_DB_PASSWORD"
create_user_and_database "$TASK_DB" "$TASK_DB_USER" "$TASK_DB_PASSWORD"
create_user_and_database "$TIMELINE_DB" "$TIMELINE_DB_USER" "$TIMELINE_DB_PASSWORD"
create_user_and_database "$FINANCE_DB" "$FINANCE_DB_USER" "$FINANCE_DB_PASSWORD"
create_user_and_database "$NOTIFICATION_DB" "$NOTIFICATION_DB_USER" "$NOTIFICATION_DB_PASSWORD"
create_user_and_database "$ANALYTICS_DB" "$ANALYTICS_DB_USER" "$ANALYTICS_DB_PASSWORD"
create_user_and_database "$AI_DB" "$AI_DB_USER" "$AI_DB_PASSWORD"

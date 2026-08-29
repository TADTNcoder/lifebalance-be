# LifeBalance Docker Development Platform

## Runtime Layout

The platform is defined by:

- `compose.yaml`: default development platform.
- `compose.override.yaml`: local debug ports for internal services.
- `compose.dev.yaml`: optional local tools enabled without profiles.
- `compose.prod.yaml`: production-oriented baseline with fewer exposed ports.
- `compose.staging.yaml`: production-profile staging overlay with gateway/Keycloak access and runtime OpenAPI export enabled.
- `.env.example`: committed environment contract.
- `.env`: local machine values, ignored by Git.

## Core Services

- PostgreSQL: `localhost:5432`
- Keycloak: `localhost:8082`
- Keycloak management health: `localhost:9000`
- Eureka Discovery Server: `localhost:8761`
- Spring Cloud Gateway: `localhost:8080`

Internal service debug ports are enabled by `compose.override.yaml`:

- Identity: `localhost:8091`
- Task: `localhost:8092`
- Finance: `localhost:8093`
- Notification: `localhost:8094`
- AI: `localhost:8095`
- Timeline: `localhost:8096`
- Analytics: `localhost:8097`

## Commands

Start core platform:

```powershell
docker compose up -d --build
```

Start with optional tools:

```powershell
docker compose --profile messaging --profile cache --profile storage --profile monitoring up -d --build
```

Or use the helper:

```powershell
.\scripts\docker-up.ps1 -WithTools
```

Validate the production and staging models without starting containers:

```powershell
docker compose --env-file .env.example -f compose.yaml -f compose.prod.yaml config --quiet
docker compose --env-file .env.example -f compose.yaml -f compose.prod.yaml -f compose.staging.yaml --profile monitoring config --quiet
```

Start a production-like staging stack with monitoring:

```powershell
Copy-Item .env.example .env.staging
# Replace every placeholder secret and configure the public Keycloak issuer first.
docker compose --env-file .env.staging -f compose.yaml -f compose.prod.yaml -f compose.staging.yaml --profile monitoring up -d --build
```

Tail logs:

```powershell
.\scripts\docker-logs.ps1 gateway
```

Authentication failure logs:

```powershell
.\scripts\docker-logs.ps1 gateway | Select-String "event=authentication_failure"
.\scripts\docker-logs.ps1 keycloak | Select-String "LOGIN_ERROR"
```

Backend services emit centralized auth audit records from `lifebalance-security` with `event=authentication_failure`.
Keycloak realm import enables event logging for login and token failure events through the `jboss-logging` listener.

Stop:

```powershell
.\scripts\docker-down.ps1
```

Stop and remove volumes:

```powershell
.\scripts\docker-down.ps1 -Volumes
```

## Keycloak Auto Import

Keycloak starts with `--import-realm` and imports JSON files from:

```text
docker/keycloak/realm
```

That folder is mounted into the container as:

```text
/opt/keycloak/data/import
```

To export the current `lifebalance` realm to the mounted export folder:

```powershell
docker exec lifebalance-keycloak /opt/keycloak/bin/kc.sh export --realm lifebalance --file /opt/keycloak/data/export/lifebalance-realm.json
```

The exported file appears at:

```text
docker/keycloak/export/lifebalance-realm.json
```

To make that export auto-import on the next clean Keycloak database initialization, copy it into the import folder:

```powershell
Copy-Item docker\keycloak\export\lifebalance-realm.json docker\keycloak\realm\lifebalance-realm.json -Force
```

Keycloak import uses `IGNORE_EXISTING`, so it will not overwrite an already-created realm in the existing Postgres volume. For a full re-import, remove the Postgres volume first.

## Networking Rules

Containers must communicate through Docker DNS names:

- `postgres:5432`
- `keycloak:8080`
- `discovery-server:8080`
- `gateway:8080`
- `identity-service:8080`

Do not use `localhost` for container-to-container calls. Inside a container, `localhost` means that same container.

## Health Checks

Spring Boot services use Actuator:

```text
/actuator/health/readiness
/actuator/health/liveness
```

Compose dependency order uses `depends_on.condition: service_healthy` for infrastructure and Spring services.

## OpenAPI release contract

All eight business services include springdoc and infer a complete runtime contract from their controllers. Production keeps `/v3/api-docs` disabled; the staging overlay enables JSON docs without exposing Swagger UI.

Check the static readiness policy:

```powershell
.\scripts\check-openapi-readiness.ps1
```

Export and validate every staging contract:

```powershell
.\scripts\export-openapi.ps1 -EnvFile .env.staging
```

The export fails if a service is unavailable or returns an OpenAPI document without paths.

## Immutable release images

`IMAGE_REGISTRY` controls the image namespace and `APP_VERSION` controls the immutable tag. Defaults preserve local image names. GitHub release workflows publish to `ghcr.io/<owner>/lifebalance/<service>:<version>`; deploy that registry prefix and the same version across the stack.

## Optional Profiles

- `messaging`: RabbitMQ
- `cache`: Redis
- `storage`: MinIO
- `monitoring`: Prometheus and Grafana

## Notes

The current backend repository contains an independent-service layout. The Dockerfile at `docker/spring/Dockerfile` is therefore designed for one service per build context.

If the project returns to a root Maven multi-module layout later, switch Spring service `build.context` back to `.` and change the Dockerfile to build with `-pl <module> -am`.

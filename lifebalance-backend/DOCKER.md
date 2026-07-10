# LifeBalance Docker Development Platform

## Runtime Layout

The platform is defined by:

- `compose.yaml`: default development platform.
- `compose.override.yaml`: local debug ports for internal services.
- `compose.dev.yaml`: optional local tools enabled without profiles.
- `compose.prod.yaml`: production-oriented baseline with fewer exposed ports.
- `.env.example`: committed environment contract.
- `.env`: local machine values, ignored by Git.

## Core Services

- PostgreSQL: `localhost:5432`
- Keycloak: `localhost:8082`
- Keycloak management health: `localhost:9000`
- Eureka Discovery Server: `localhost:8761`
- Spring Cloud Gateway: `localhost:8080`
- Frontend: `localhost:5173`

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

Tail logs:

```powershell
.\scripts\docker-logs.ps1 gateway
```

Stop:

```powershell
.\scripts\docker-down.ps1
```

Stop and remove volumes:

```powershell
.\scripts\docker-down.ps1 -Volumes
```

## Networking Rules

Containers must communicate through Docker DNS names:

- `postgres:5432`
- `keycloak:8080`
- `discovery-server:8761`
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

## Optional Profiles

- `messaging`: RabbitMQ
- `cache`: Redis
- `storage`: MinIO
- `monitoring`: Prometheus and Grafana

## Notes

The current backend repository contains an independent-service layout. The Dockerfile at `docker/spring/Dockerfile` is therefore designed for one service per build context.

If the project returns to a root Maven multi-module layout later, switch Spring service `build.context` back to `.` and change the Dockerfile to build with `-pl <module> -am`.

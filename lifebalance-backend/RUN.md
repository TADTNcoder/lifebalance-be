# Huong dan chay LifeBalance Backend

## Yeu cau

- Docker Desktop
- Java 21 tro len
- Maven wrapper da co san trong repo: `mvnw.cmd`
- Node.js neu muon chay frontend local

Chay cac lenh ben duoi tai thu muc root:

```powershell
cd E:\lifebalance-be\lifebalance-backend
```

## 1. Chuan bi bien moi truong

Neu chua co file `.env`, tao tu file mau:

```powershell
Copy-Item .env.example .env
```

Trong moi truong dev co the giu gia tri mac dinh. Neu cong `5432`, `8080`, `8082`, `8761` da bi chiem, sua port trong `.env`.

## 2. Chay toan bo backend bang Docker

```powershell
docker compose up -d --build
```

Lenh nay se build va chay:

- PostgreSQL: `localhost:5432`
- Keycloak: `http://localhost:8082`
- Eureka: `http://localhost:8761`
- Gateway: `http://localhost:8080`
- Cac service noi bo, dong thoi duoc expose debug port qua `compose.override.yaml`

Port service truc tiep:

- Identity: `http://localhost:8091`
- Task: `http://localhost:8092`
- Finance: `http://localhost:8093`
- Notification: `http://localhost:8094`
- AI: `http://localhost:8095`
- Timeline: `http://localhost:8096`
- Analytics: `http://localhost:8097`

Co the dung script tuong duong:

```powershell
.\scripts\docker-up.ps1
```

## 3. Chay kem tools phu

Neu can RabbitMQ, Redis, MinIO, Prometheus, Grafana:

```powershell
.\scripts\docker-up.ps1 -WithTools
```

Hoac:

```powershell
docker compose --profile messaging --profile cache --profile storage --profile monitoring up -d --build
```

## 4. Kiem tra trang thai

Xem container:

```powershell
docker compose ps
```

Xem log tat ca service:

```powershell
.\scripts\docker-logs.ps1
```

Xem log mot service:

```powershell
.\scripts\docker-logs.ps1 identity-service
```

Kiem tra health:

```powershell
curl http://localhost:8080/actuator/health
curl http://localhost:8091/actuator/health
```

Mo Eureka de xem service da register:

```text
http://localhost:8761
```

## 5. Chay rieng identity-service tren may local

Cach nay dung khi muon debug trong IDE. Nen de infrastructure chay bang Docker truoc:

```powershell
docker compose up -d postgres keycloak discovery-server
```

Sau do chay identity-service bang Maven:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:IDENTITY_SERVICE_PORT="8091"
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/lifebalance_identity"
$env:SPRING_DATASOURCE_USERNAME="identity_user"
$env:SPRING_DATASOURCE_PASSWORD="change-me"
$env:KEYCLOAK_ISSUER_URI="http://localhost:8082/realms/lifebalance"
$env:EUREKA_DEFAULT_ZONE="http://localhost:8761/eureka"
.\mvnw.cmd -pl identity-service spring-boot:run
```

Neu password trong `.env` khac `change-me`, doi lai bien `SPRING_DATASOURCE_PASSWORD`.

## 6. Chay test

Test rieng identity-service:

```powershell
.\mvnw.cmd -pl identity-service test
```

Test toan bo backend:

```powershell
.\mvnw.cmd test
```

## 7. Chay frontend local

Frontend nam trong thu muc `frontend` va dung Vite:

```powershell
cd frontend
npm install
npm run dev
```

Mac dinh frontend goi API qua:

```text
http://localhost:8080
```

## 8. Dung he thong

Dung container, giu lai database volume:

```powershell
.\scripts\docker-down.ps1
```

Dung va xoa volume database/cache:

```powershell
.\scripts\docker-down.ps1 -Volumes
```

Dung `-Volumes` khi muon reset sach PostgreSQL, Keycloak import lai realm, va tao lai data tu dau.

## Loi thuong gap

Neu service khong start, xem log service do:

```powershell
.\scripts\docker-logs.ps1 identity-service
```

Neu Keycloak realm khong dung, reset volume:

```powershell
.\scripts\docker-down.ps1 -Volumes
docker compose up -d --build
```

Neu chay service local ma khong ket noi duoc database/Eureka/Keycloak, kiem tra lai cac bien dang dung `localhost`, khong dung Docker DNS nhu `postgres`, `keycloak`, `discovery-server`.

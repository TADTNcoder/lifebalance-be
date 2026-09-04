# Cẩm nang kiến trúc Backend LifeBalance

> Tài liệu học và chuẩn bị vấn đáp, bám theo mã nguồn hiện tại của dự án LifeBalance.
>
> Cập nhật theo repository ngày 03/09/2026. Những nội dung ghi **hiện trạng** là điều đã quan sát thấy trong code hoặc cấu hình. Những nội dung ghi **đề xuất** là hướng cải tiến, không được nói với giảng viên rằng hệ thống đã triển khai.

---

## 0. Cách sử dụng tài liệu

Nếu chỉ có ít thời gian, hãy đọc theo thứ tự:

1. Phần 1: Bài trình bày ngắn về toàn hệ thống.
2. Phần 2: Sơ đồ và trách nhiệm từng thành phần.
3. Mục 5: Luồng hoàn thành task từ đầu đến cuối.
4. Mục 8: Security.
5. Mục 9 và 10: Transaction, dữ liệu và tích hợp service.
6. Phần VII: Câu hỏi vấn đáp.
7. Phần VIII: Những câu không được trả lời sai.

Khi trả lời một câu hỏi kiến trúc, dùng công thức sau:

> **Mục tiêu → Thiết kế hiện tại → Lý do chọn → Đánh đổi → Cách xử lý lỗi/cải tiến.**

Ví dụ:

> Mục tiêu là tách định tuyến khỏi nghiệp vụ. Hệ thống dùng API Gateway làm điểm vào chung và Eureka để tìm service. Cách này giúp client không cần biết địa chỉ từng service. Đánh đổi là gateway có thể thành nút nghẽn hoặc điểm lỗi đơn nếu chỉ chạy một instance, vì vậy khi production cần nhân bản gateway và có load balancer phía trước.

---

# Phần I — Hiểu toàn bộ hệ thống

## 1. Bài trình bày kiến trúc trong 2 phút

LifeBalance Backend được xây theo hướng **microservice với Spring Boot**. Hệ thống có một API Gateway làm điểm vào cho client, một Eureka Discovery Server quản lý đăng ký và tìm kiếm service, Keycloak phụ trách xác thực, và nhiều business service được tách theo năng lực nghiệp vụ như identity, task, timeline, resource capital, finance, notification, analytics và AI.

Mỗi business service là một ứng dụng Spring Boot độc lập, có controller, service, domain/entity, repository và migration riêng. Mỗi service sở hữu một database logic riêng trong PostgreSQL. Các service không nên truy cập trực tiếp bảng của nhau; khi cần dữ liệu hoặc kích hoạt hành vi ở service khác, chúng giao tiếp qua HTTP REST.

Request từ người dùng đi vào gateway. Gateway kiểm tra JWT và dựa trên path để chuyển request tới service tương ứng. Các business service cũng dùng chung module `lifebalance-security` để tự xác thực JWT, vì vậy security không chỉ phụ thuộc vào gateway. JWT được kiểm tra thời hạn, issuer và audience; sau đó các claim của Keycloak được ánh xạ thành `KeycloakUserPrincipal` chứa user ID, username, email và roles.

Trong một service, controller xử lý giao thức HTTP và lấy danh tính người dùng; service layer thực hiện use case và quản lý transaction; domain/entity giữ trạng thái nghiệp vụ; repository giao tiếp với database. Lỗi được chuẩn hóa bởi `lifebalance-common` thành một cấu trúc API response thống nhất.

Một số nghiệp vụ cần đồng bộ nhiều service. Ví dụ khi task thay đổi, `task-service` lưu task và history trong transaction của mình, sau đó mới gọi timeline, notification hoặc analytics sau khi commit. Cách này tránh việc service bên ngoài được gọi khi transaction chính còn có thể rollback. Tuy nhiên, callback sau commit hiện chỉ retry và ghi log; nó chưa phải message bền vững, nên vẫn có khả năng database đã commit nhưng downstream chưa nhận được dữ liệu.

Hệ thống được đóng gói và chạy bằng Docker Compose, có health check, graceful shutdown, Prometheus và Grafana. Redis được hỗ trợ cho RBAC cache nhưng cache mặc định hiện là Caffeine. RabbitMQ và MinIO có trong hạ tầng Compose nhưng chưa thấy luồng ứng dụng chính sử dụng chúng. Vì vậy không nên gọi kiến trúc hiện tại là event-driven hoàn chỉnh.

Đó là bức tranh tổng thể: gateway và discovery lo hạ tầng truy cập; Keycloak và shared security lo danh tính; mỗi business service sở hữu một miền nghiệp vụ và database; REST cùng cơ chế after-commit kết nối các miền; metrics, logs và container hỗ trợ vận hành.

## 2. Architecture khác API như thế nào?

### API trả lời câu hỏi gì?

- Endpoint nào tồn tại?
- Dùng HTTP method nào?
- Request và response có field gì?
- Status code và error code là gì?
- Client gọi chức năng như thế nào?

### Architecture trả lời câu hỏi gì?

- Tại sao hệ thống được chia thành các service như vậy?
- Thành phần nào chịu trách nhiệm cho nghiệp vụ và dữ liệu nào?
- Request đi qua những lớp và service nào?
- Security được thực hiện ở đâu?
- Transaction kết thúc ở đâu?
- Các service nhất quán dữ liệu như thế nào?
- Khi gateway, database hoặc downstream service lỗi thì điều gì xảy ra?
- Hệ thống được cấu hình, triển khai, theo dõi và mở rộng ra sao?

Nói ngắn gọn:

> **API là hợp đồng bên ngoài; architecture là cách toàn hệ thống phối hợp để thực hiện hợp đồng đó.**

## 3. Sơ đồ toàn hệ thống

```text
                         ┌──────────────────────┐
                         │   Web/Mobile Client  │
                         └──────────┬───────────┘
                                    │ HTTPS + Bearer JWT
                                    ▼
                         ┌──────────────────────┐
                         │     API Gateway      │
                         │ route + JWT validate │
                         └──────────┬───────────┘
                                    │ lb://SERVICE-NAME
                         ┌──────────▼───────────┐
                         │   Eureka Discovery   │
                         └──────────────────────┘

          ┌──────────────────── Business services ────────────────────┐
          │                                                            │
          │ Identity   Task   Timeline   Capital   Finance             │
          │ Notification   Analytics   AI                              │
          │                                                            │
          └───────────────┬───────────────────────────────┬────────────┘
                          │                               │
                  JPA + Flyway                    REST integrations
                          │                  (Bearer + internal secret)
                          ▼                               │
                 ┌────────────────┐                      │
                 │   PostgreSQL   │◄─────────────────────┘
                 │ DB per service │
                 └────────────────┘

        Keycloak: authentication/JWT       Caffeine/Redis: RBAC cache
        Prometheus: metrics                Grafana: visualization
        RabbitMQ/MinIO: có trong Compose, chưa thấy luồng code chính dùng
```

## 4. Danh mục module và trách nhiệm

### 4.1 Module hạ tầng và dùng chung

| Module | Cổng local mặc định | Trách nhiệm |
|---|---:|---|
| `gateway` | 8080 | Điểm vào chung; xác thực JWT; route theo path; dùng Eureka để tìm service |
| `discovery-server` | 8761 | Eureka registry; nhận đăng ký từ gateway và các service |
| `lifebalance-common` | Không chạy độc lập | `ApiResponse`, `ApiError`, `AppException`, global exception handler, pagination dùng chung |
| `lifebalance-security` | Không chạy độc lập | Auto-configuration cho OAuth2 Resource Server, JWT validation, user mapping, 401/403 và method security |

### 4.2 Business service

| Service | Cổng local | Trách nhiệm chính | Nhóm dữ liệu/chức năng quan sát được |
|---|---:|---|---|
| `identity-service` | 8091 | Danh tính và phân quyền | User, role, permission, role assignment, audit, password, administration/support |
| `task-service` | 8092 | Quản lý công việc | Task, category, tag, recurring rule, reminder, task history, planning/lifecycle |
| `finance-service` | 8093 | Quản lý tài chính | Account, category, transaction, history, report |
| `notification-service` | 8094 | Quản lý thông báo | Notification, delivery, retry, preference, template, history |
| `ai-service` | 8095 | Trợ lý và gợi ý trong LifeBalance | Conversation, message, insight, recommendation, history |
| `timeline-service` | 8096 | Lịch và vị trí thời gian | Timeline task, placement, conflict/availability, timeline history |
| `analytics-service` | 8097 | Theo dõi và đánh giá | Actual record, evaluation, tracking, report, history |
| `resource-capital-service` | 8098 | Quản lý nguồn lực/vốn | Capital, cycle, allocation, adjustment, remaining/available capital, time/money capital |

### 4.3 Hạ tầng bên ngoài ứng dụng

| Thành phần | Vai trò | Trạng thái cần hiểu đúng |
|---|---|---|
| PostgreSQL | Lưu dữ liệu bền vững | Một PostgreSQL container nhưng tạo database/user riêng cho Keycloak và từng service |
| Keycloak | Identity Provider | Phát JWT, quản lý realm/client/role; identity-service có khả năng đồng bộ role và đổi password khi bật cấu hình |
| Caffeine | In-memory cache | Là loại RBAC cache mặc định của identity-service |
| Redis | Distributed cache | Có hỗ trợ cho RBAC khi đặt `lifebalance.rbac.cache.type=redis`; không phải mặc định |
| Prometheus | Thu thập metrics | Các ứng dụng expose actuator health/info/prometheus |
| Grafana | Hiển thị metrics | Dùng datasource Prometheus trong Docker |
| RabbitMQ | Message broker | Có container trong Compose nhưng chưa thấy dependency/producer/consumer trong code hiện tại |
| MinIO | Object storage | Có container trong Compose nhưng chưa thấy client/use case ứng dụng trong code hiện tại |

### 4.4 Vì sao chia service như vậy?

Các service được chia theo **business capability/bounded context**, không phải chỉ chia theo bảng database:

- Task quyết định trạng thái và vòng đời của công việc.
- Timeline quyết định lịch đặt và xung đột thời gian.
- Finance quyết định giao dịch và số liệu tài chính.
- Resource Capital quyết định phân bổ nguồn lực.
- Analytics quyết định cách ghi nhận thực tế và đánh giá.
- Notification quyết định vòng đời và giao nhận thông báo.
- Identity quyết định user, role, permission.

Ưu điểm:

- Mỗi miền có trách nhiệm rõ.
- Có thể phát triển, kiểm thử và triển khai tương đối độc lập.
- Lỗi hoặc tải cao của một miền có thể được cô lập.
- Database schema của mỗi miền có thể tiến hóa riêng.

Đánh đổi:

- Giao tiếp mạng phức tạp hơn lời gọi trong cùng process.
- Không có một transaction ACID bao trùm nhiều service.
- Cần timeout, retry, idempotency, tracing và quan sát hệ thống tốt.
- Dữ liệu có thể nhất quán trễ.
- Chi phí cấu hình và vận hành cao hơn modular monolith.

---

# Phần II — Cách request chạy trong hệ thống

## 5. Luồng request chuẩn

Ví dụ client gọi `PATCH /api/tasks/{id}/complete`:

1. Client lấy access token từ Keycloak.
2. Client gửi request tới gateway với `Authorization: Bearer <JWT>`.
3. Security filter của gateway xác thực chữ ký và kiểm tra token.
4. Gateway so path với route `/api/tasks/**`.
5. Gateway dùng URI `lb://TASK-SERVICE`, lấy instance từ Eureka và chuyển request.
6. `task-service` tự xác thực lại JWT bằng shared security module.
7. `KeycloakUserMappingFilter` ánh xạ JWT thành `KeycloakUserPrincipal` và đặt vào request attribute `currentUser`.
8. `TaskController` lấy `userId` từ current user và dùng nó như `ownerId`.
9. `TaskServiceImpl.complete` mở local database transaction.
10. Service tìm task bằng cả `id` và `ownerId`, nhờ vậy user không thể thao tác task của người khác chỉ bằng cách đoán ID.
11. `TaskLifecyclePolicy` kiểm tra chuyển trạng thái hiện tại sang `COMPLETED` có hợp lệ không.
12. Domain task được cập nhật progress thành 100 và đánh dấu hoàn thành.
13. Task và change history được lưu trong database của task-service.
14. Integration action được đăng ký để thực thi sau commit.
15. Transaction commit. Response được map thành `TaskResponse`.
16. Callback after-commit có thể đồng bộ Timeline, tạo Notification và tạo seed cho Analytics tùy cấu hình/policy.

### 5.1 Sequence diagram của luồng hoàn thành task

```text
Client        Gateway       Task Service       Task DB       Downstream services
  │              │               │                │                 │
  │ PATCH + JWT  │               │                │                 │
  ├─────────────►│ validate JWT  │                │                 │
  │              ├──────────────►│ validate JWT   │                 │
  │              │               │ owner + policy │                 │
  │              │               ├───────────────►│ save task       │
  │              │               ├───────────────►│ save history    │
  │              │               │ register after-commit callback  │
  │              │               │◄───────────────┤ commit OK       │
  │              │               ├─────────────────────────────────►│
  │              │               │ REST, retry, log failure         │
  │◄─────────────┴───────────────┤                                  │
  │          TaskResponse         │                                  │
```

Điểm phải nhớ:

- Transaction chỉ bảo vệ task database và history nằm trong cùng service.
- Lời gọi downstream xảy ra sau commit, nên lỗi downstream không rollback task đã hoàn thành.
- `afterCommit` mô tả thời điểm thực thi, không đồng nghĩa với bất đồng bộ. Callback hiện vẫn chạy đồng bộ trên thread xử lý request sau khi commit, nên downstream chậm vẫn có thể làm response chậm.
- Đây là dạng eventual consistency, không phải distributed ACID transaction.
- After-commit callback hiện không phải hàng đợi bền vững. Process chết đúng thời điểm có thể làm mất integration action.

## 6. Kiến trúc bên trong một business service

Mẫu phổ biến:

```text
HTTP Request
    │
    ▼
Controller ──► DTO validation
    │
    ▼
Service interface ──► Service implementation + @Transactional
    │
    ├──► Policy / Validator
    ├──► Domain / Entity
    ├──► Repository ──► Database
    ├──► History / Audit
    └──► Integration publisher/client
```

### 6.1 Controller

Controller nên chịu trách nhiệm:

- Map HTTP route, method, path/query/header/body.
- Chạy Bean Validation qua `@Valid`.
- Lấy identity của current user.
- Gọi đúng application service/use case.
- Chuyển kết quả thành HTTP response.

Controller không nên:

- Chứa nhiều business rule.
- Tự truy cập repository.
- Tự quản lý transaction.
- Tự gọi nhiều downstream service để điều phối nghiệp vụ phức tạp.

### 6.2 DTO

- Request DTO định nghĩa dữ liệu client được phép gửi.
- Response DTO định nghĩa dữ liệu được phép trả về.
- DTO giúp không làm lộ toàn bộ JPA entity và tránh client điều khiển các field nội bộ.
- Validation cú pháp như `@NotBlank`, `@Size`, `@Positive`, regex nên đặt ở request DTO.

### 6.3 Service layer

Service layer chịu trách nhiệm thực thi use case:

- Kiểm tra rule nghiệp vụ.
- Tải aggregate/entity cần thiết.
- Thay đổi trạng thái domain.
- Điều phối repository, history và integration.
- Đặt transaction boundary bằng `@Transactional`.

### 6.4 Domain/entity

Domain giữ trạng thái và hành vi nghiệp vụ. Ví dụ task có vòng đời:

```text
DRAFT → PLANNED/SCHEDULED → IN_PROGRESS → COMPLETED
                    │              │
                    └── ON_HOLD ───┘

Các nhánh khác: CANCELLED, ARCHIVED; một số trạng thái có thể restore/reopen
theo TaskLifecyclePolicy chứ không được chuyển tùy ý.
```

Không nên chỉ nhìn enum rồi kết luận transition nào cũng được. `TaskLifecyclePolicy` mới là nơi xác định rule chuyển trạng thái thực tế.

### 6.5 Repository

Repository là abstraction cho persistence. Một chi tiết security quan trọng là truy vấn theo cả resource ID và owner ID, ví dụ `findByIdAndOwnerId`. Điều này biến ownership thành một phần của truy vấn, giảm nguy cơ IDOR — người dùng biết UUID của bản ghi khác vẫn không lấy được dữ liệu.

### 6.6 Mapper

Mapper chuyển entity/domain thành response DTO. Nó giúp API contract không bị ràng buộc trực tiếp với cấu trúc persistence.

### 6.7 History và audit

- History nghiệp vụ mô tả một entity đã thay đổi như thế nào.
- Security audit mô tả sự kiện xác thực/phân quyền.
- Identity audit dùng transactional event listener sau commit để lưu audit cho user/role/permission.
- Cần phân biệt audit với application log: audit phục vụ truy vết hành động; log phục vụ chẩn đoán hệ thống.

---

# Phần III — Các khối kiến trúc quan trọng

## 7. API Gateway và Service Discovery

### 7.1 Gateway làm gì?

Gateway hiện có ba trách nhiệm chính:

1. Tạo một địa chỉ vào thống nhất cho client.
2. Xác thực JWT trước khi chuyển request.
3. Route request theo path đến service đích.

Ví dụ route:

| Path | Service đích |
|---|---|
| `/api/identity/**`, `/api/auth/**`, `/api/users/**`, `/api/roles/**` | Identity |
| `/api/tasks/**`, `/api/categories/**`, `/api/tags/**` | Task |
| `/api/timeline/**` | Timeline |
| `/api/v1/capital/**` và các path capital liên quan | Resource Capital |
| `/api/finance/**`, `/api/transactions/**` | Finance |
| `/api/notifications/**` | Notification |
| `/api/analytics/**` | Analytics |
| `/api/ai/**` | AI |

### 7.2 Eureka làm gì?

- Các service đăng ký `spring.application.name`, địa chỉ và trạng thái với Eureka.
- Gateway route bằng `lb://SERVICE-NAME` thay vì giữ một IP cụ thể.
- Load balancer phía client chọn một instance khỏe trong registry.
- Khi scale nhiều instance, gateway không cần đổi route cho từng IP.

### 7.3 Điều Eureka không làm

- Không xác thực người dùng.
- Không lưu business data.
- Không thay thế API Gateway.
- Không bảo đảm transaction hay message delivery.
- Không tự sửa một service đang lỗi.

### 7.4 Một điểm kiến trúc cần nói chính xác

Gateway dùng Eureka qua `lb://...`. Tuy nhiên, integration giữa các business service hiện dùng `RestClient` với base URL cấu hình trực tiếp như `http://timeline-service:8080` hoặc `http://localhost:8096`. Nghĩa là luồng nội bộ đang dựa vào Docker DNS/hostname hoặc cấu hình môi trường, không đi qua gateway và cũng không thể kết luận rằng mọi lời gọi nội bộ đều dùng Eureka.

Đây không nhất thiết là sai. Service-to-service thường không cần vòng qua public gateway. Nhưng nếu muốn discovery/load balancing thống nhất cho lời gọi nội bộ, cần dùng client có tích hợp service discovery hoặc một cơ chế service mesh.

## 8. Security từ đầu đến cuối

### 8.1 Authentication và authorization

- **Authentication**: xác định người gọi là ai.
- **Authorization**: xác định người đó được phép làm gì trên tài nguyên nào.

Keycloak và JWT chủ yếu giải quyết authentication và cung cấp role claims. Business service vẫn phải kiểm tra permission và ownership.

### 8.2 JWT validation

Shared security module cấu hình ứng dụng thành OAuth2 Resource Server stateless:

- Tắt form login và HTTP Basic.
- Tắt server-side session bằng `SessionCreationPolicy.STATELESS`.
- Tắt CSRF vì API không dựa trên cookie session để xác thực.
- Xác thực chữ ký JWT bằng JWK của issuer.
- Kiểm tra thời hạn token.
- Kiểm tra issuer nếu được cấu hình.
- Bắt buộc audience chứa `lifebalance-api` hoặc client ID đã cấu hình.

Tại sao cần audience? Một token có chữ ký Keycloak hợp lệ nhưng được phát cho một ứng dụng khác vẫn không nên được API LifeBalance chấp nhận.

### 8.3 User mapping

Sau khi JWT hợp lệ, hệ thống ánh xạ các claim:

- `sub`: subject/keycloak ID.
- `lifebalance_user_id`: UUID nội bộ; nếu không có thì thử dùng `sub` nếu parse được UUID.
- `preferred_username`, `email`, `name`.
- `realm_access.roles`: realm roles.
- `resource_access[client-id].roles`: client roles.
- `aud`, `azp`.

Kết quả là `KeycloakUserPrincipal`, được đặt vào request attribute `currentUser`.

### 8.4 Public endpoint

Shared security cho phép công khai một số endpoint:

- Actuator health và Prometheus.
- OpenAPI/Swagger.
- Các endpoint GET status/health theo pattern cấu hình.

Các request còn lại phải authenticated, trừ khi một service tự cung cấp `SecurityFilterChain` khác.

### 8.5 Vì sao gateway và service đều kiểm tra JWT?

Đây là defense in depth:

- Gateway chặn sớm request không hợp lệ.
- Service vẫn an toàn nếu bị gọi trực tiếp trong internal network.
- Service không phải tin tuyệt đối rằng mọi request chắc chắn đã đi qua gateway.

Đánh đổi là có thêm chi phí validate token và cần giữ cấu hình issuer/audience nhất quán.

### 8.6 RBAC và ownership

Identity-service có permission evaluation và dùng `@PreAuthorize` cho các thao tác quản trị như `role:create`, `user:update`, `audit:read`.

Các service dữ liệu cá nhân thường dùng ownership:

1. Lấy `ownerId` từ principal đã xác thực.
2. Không tin `ownerId` do client tự gửi.
3. Query dữ liệu bằng cả ID và owner ID.

RBAC trả lời “user có quyền loại hành động này không?”. Ownership trả lời “resource cụ thể này có thuộc user đó không?”. Hai kiểm tra bổ sung cho nhau.

### 8.7 Cache RBAC

Identity-service hỗ trợ hai loại cache:

- Caffeine: mặc định, nhanh, nằm trong từng process.
- Redis: tùy chọn, chia sẻ được giữa nhiều instance.

TTL mặc định là 15 phút. Tradeoff cần nhớ:

- Cache tăng tốc đọc permission.
- Permission vừa thay đổi có thể bị stale nếu không evict đúng cách.
- Caffeine đơn giản nhưng mỗi instance có cache riêng.
- Redis nhất quán hơn giữa instance nhưng tạo thêm network dependency.

### 8.8 Service-to-service security

Luồng Task → Timeline/Notification/Analytics/Finance hiện có thể gửi:

- Bearer token của user gốc.
- Header `X-Lifebalance-Internal-Secret`.

Service đích có thể yêu cầu cả danh tính user và internal credential, đồng thời đối chiếu owner trong payload với owner trong token. Điều này giúp chống service giả mạo và confused-deputy attack.

Hạn chế:

- Forward user token làm integration phụ thuộc request context và thời hạn token.
- Shared secret cần được xoay vòng và lưu trong secret manager.
- Một hướng production mạnh hơn là OAuth2 client credentials/workload identity, kết hợp actor context riêng nếu cần audit theo user.

### 8.9 401 và 403

- `401 Unauthorized`: chưa xác thực được — thiếu token, token sai, token hết hạn.
- `403 Forbidden`: đã xác thực nhưng không đủ quyền, owner không khớp hoặc thiếu internal credential.

Shared security có authentication entry point và access denied handler để trả cấu trúc lỗi thống nhất và ghi security audit log.

## 9. Dữ liệu, transaction và consistency

### 9.1 Database per service

Script PostgreSQL tạo database/user riêng cho:

- Keycloak.
- Identity.
- Task.
- Timeline.
- Resource Capital.
- Finance.
- Notification.
- Analytics.
- AI.

Đây là **cô lập logic**: mỗi service có database và credential riêng, nhưng hiện các database cùng nằm trên một PostgreSQL container/instance.

Không được nói “mỗi service có một database server vật lý riêng”. Câu đúng là:

> Mỗi service sở hữu database logic riêng; deployment hiện gom chúng trong một PostgreSQL instance để đơn giản hóa môi trường.

### 9.2 Source of truth

Nguyên tắc cần bảo vệ:

- Task-service là nguồn chuẩn của task lifecycle.
- Timeline-service là nguồn chuẩn của placement và xung đột lịch.
- Finance-service là nguồn chuẩn của account/transaction.
- Identity-service là nguồn chuẩn của business user/role/permission; Keycloak là identity provider và token issuer.

Khi service khác giữ bản sao hoặc projection, bản sao đó không tự trở thành source of truth.

### 9.3 Không dùng foreign key xuyên service

Service có thể lưu UUID tham chiếu đến entity của service khác, nhưng không nên tạo database foreign key xuyên bounded context. Lý do:

- Tránh coupling schema và deployment.
- Service không cần truy cập database của nhau.
- Cho phép mỗi service tiến hóa độc lập.

Đánh đổi là tính tồn tại của reference phải được kiểm tra qua API, event hoặc cơ chế reconciliation, không còn được một foreign key chung bảo đảm.

### 9.4 Flyway

Flyway quản lý schema bằng migration có version:

- Schema thay đổi có lịch sử.
- Môi trường chạy cùng một chuỗi migration.
- Không phụ thuộc Hibernate tự ý tạo/sửa schema production.
- Có thể kiểm thử migration riêng.

Không nên sửa migration đã chạy ở môi trường chia sẻ. Hãy thêm migration phiên bản mới.

### 9.5 Transaction boundary

Một `@Transactional` trong task-service chỉ bao phủ datasource của task-service. Nó không bao phủ HTTP call sang timeline hoặc notification.

ACID áp dụng trong local database transaction:

- Atomicity: task và thay đổi trong transaction cùng commit hoặc rollback.
- Consistency: constraint và business rule giữ dữ liệu hợp lệ.
- Isolation: transaction đồng thời được database cô lập theo mức cấu hình.
- Durability: sau commit, dữ liệu được PostgreSQL lưu bền vững.

### 9.6 Eventual consistency

Sau khi task commit, timeline có thể chưa được cập nhật ngay. Trong một khoảng thời gian ngắn:

- Task đã `COMPLETED`.
- Timeline hoặc Analytics vẫn giữ trạng thái cũ.

Nếu hệ thống cuối cùng đồng bộ lại được, đây là eventual consistency. Nhưng nếu chỉ log lỗi và không có retry bền vững/reconciliation, không thể bảo đảm “eventually” trong mọi failure case.

### 9.7 Vì sao dùng after-commit?

Nếu gọi downstream trước commit:

1. Timeline cập nhật thành công.
2. Sau đó task transaction rollback.
3. Hai service mâu thuẫn dữ liệu.

Gọi sau commit tránh loại sai lệch này. Tuy nhiên nó tạo một khoảng trống mới:

1. Task commit thành công.
2. Process chết trước hoặc trong khi gọi Timeline.
3. Integration có thể bị mất.

Giải pháp production thường dùng **Transactional Outbox**:

1. Trong cùng transaction, lưu task và một outbox event.
2. Worker đọc outbox và gửi message/HTTP.
3. Chỉ đánh dấu event đã gửi sau khi downstream xác nhận.
4. Retry an toàn với event ID/idempotency key.

Trong hệ thống hiện tại, Outbox là **đề xuất**, chưa được coi là đã triển khai.

## 10. Giao tiếp giữa các service

### 10.1 Các quan hệ đã thấy rõ

```text
Task ─────────► Timeline
  ├───────────► Notification
  ├───────────► Analytics
  └───────────► Finance (có client/capability; cần phân biệt phần đã wiring)

Resource Capital ─► Task (kiểm tra allocation target)
        └─────────► Notification

Identity ─────────► Keycloak Admin/Token endpoints
```

### 10.2 Cách `task-service` gọi downstream

`RestTaskIntegrationClient` có cấu hình mặc định:

- Tối đa 3 attempts.
- Backoff cơ sở 200 ms, tăng theo attempt.
- Connect timeout 3 giây.
- Read timeout 5 giây.
- Retry lỗi mạng, HTTP 5xx, 408, 425 và 429.
- Không retry lỗi client vĩnh viễn như phần lớn HTTP 4xx.
- Ghi warning nếu cuối cùng thất bại.

Các feature flag mặc định cũng cần nói chính xác:

- Timeline endpoint được bật mặc định, nhưng lời gọi sẽ bị bỏ qua nếu thiếu Bearer token hoặc internal secret.
- Notification endpoint có `enabled=true` nhưng `policyApproved=false`, nên notification sync chưa hoạt động theo mặc định.
- Analytics endpoint có `enabled=true` nhưng `actualSeedEnabled=false`, nên actual seed chưa hoạt động theo mặc định.
- Finance endpoint có capability gọi monthly income, nhưng method publish tương ứng chưa thấy được wiring từ `TaskServiceImpl`.

Điểm tốt:

- Có timeout, tránh chờ vô hạn.
- Phân biệt lỗi retryable và non-retryable.
- Tự kiểm soát retry cho POST để tránh HTTP client ngầm nhân số lần gọi.
- Integration xảy ra after commit.

Điểm cần cải thiện:

- Retry nằm trên request thread/callback và dùng sleep; nó chiếm tài nguyên.
- Không có durable queue/outbox nên process crash có thể làm mất tác vụ.
- POST retry có thể tạo bản ghi trùng nếu service đích không idempotent.
- Chỉ log failure chưa tạo cơ chế replay hay cảnh báo nghiệp vụ.
- Các lời gọi downstream trong callback được thực hiện tuần tự; một lời gọi chậm kéo dài toàn callback.

### 10.3 Idempotency

Idempotency nghĩa là xử lý cùng một yêu cầu nhiều lần vẫn cho kết quả logic giống một lần.

Điều này quan trọng vì:

1. Client gửi POST.
2. Server xử lý thành công nhưng response bị mất.
3. Client retry.
4. Nếu không có idempotency, dữ liệu có thể được tạo hai lần.

Hướng cải tiến:

- Gửi `eventId` hoặc `Idempotency-Key`.
- Service nhận lưu key đã xử lý.
- Dùng unique constraint theo business key phù hợp.
- Upsert khi ngữ nghĩa nghiệp vụ cho phép.

### 10.4 Synchronous REST và asynchronous messaging

| Tiêu chí | REST đồng bộ | Message bất đồng bộ |
|---|---|---|
| Response ngay | Dễ | Không mặc định |
| Coupling thời gian | Hai service phải cùng sẵn sàng | Producer và consumer có thể lệch thời gian |
| Đơn giản | Dễ hiểu hơn | Cần broker, retry, DLQ, consumer idempotency |
| Reliability | Phụ thuộc retry/circuit breaker | Có thể bền vững nếu broker cấu hình đúng |
| Use case phù hợp | Query/command cần kết quả tức thì | Notification, analytics, audit, propagation |

Không phải mọi REST call đều nên đổi thành message. Ví dụ validation cần câu trả lời ngay có thể hợp với REST; notification và analytics thường phù hợp hơn với event.

### 10.5 RabbitMQ trong kiến trúc hiện tại

RabbitMQ được khai báo trong Docker Compose nhưng chưa thấy code producer/consumer hay AMQP dependency. Vì vậy câu trả lời chính xác là:

> Hạ tầng đã chuẩn bị RabbitMQ, nhưng các integration chính hiện vẫn dùng synchronous REST kết hợp callback after-commit. RabbitMQ là khả năng mở rộng trong tương lai, chưa phải luồng chạy hiện tại.

### 10.6 AI service hiện tại

AI service có domain cho conversation, message, insight và recommendation. Chưa thấy dependency hoặc client tới OpenAI/Ollama/Anthropic/Gemini trong code hiện tại. Vì vậy không nên khẳng định rằng service đang gọi một LLM bên ngoài. Có thể mô tả nó là miền nghiệp vụ AI/recommendation hiện tại, với khả năng tích hợp model thật sau này.

---

# Phần IV — Chất lượng và vận hành hệ thống

## 11. Error handling và API contract dùng chung

`lifebalance-common` chuẩn hóa response:

```text
ApiResponse<T>
├── success
├── data
├── error
└── timestamp

ApiError
├── code
├── message
└── details
```

Global exception handler xử lý:

- `AppException`: error code nghiệp vụ và HTTP status tương ứng.
- Bean Validation: 400 với chi tiết field.
- Sai kiểu path/query hoặc JSON không hợp lệ: 400.
- Authentication failure: 401 và security audit.
- Access denied: 403 và security audit.
- Resource không tồn tại: 404.
- Lỗi không dự kiến: log stack trace nội bộ, trả 500 với message tổng quát.

Lợi ích:

- Frontend xử lý lỗi thống nhất.
- Không lộ stack trace hoặc chi tiết nhạy cảm cho client.
- Error code ổn định hơn việc frontend phụ thuộc vào message tự nhiên.

Rủi ro của shared error module:

- Thay đổi không tương thích ảnh hưởng nhiều service.
- Nếu module chứa quá nhiều business logic, các service bị coupling.
- Cần versioning và test tương thích.

## 12. Observability

### 12.1 Ba trụ cột

1. **Logs**: sự kiện chi tiết của từng service.
2. **Metrics**: số liệu tổng hợp như request rate, latency, error rate, JVM, connection pool.
3. **Traces**: đường đi của một request xuyên gateway và nhiều service.

Hiện trạng quan sát được:

- Actuator health/info/prometheus được cấu hình.
- Prometheus và Grafana có trong Compose.
- Có application log và security audit log.
- Có hỗ trợ đọc `X-Request-ID` hoặc `X-Correlation-ID` trong security audit.

Điểm cần kiểm tra/cải thiện:

- Gateway có tạo correlation ID nếu client không gửi hay không?
- Header có được truyền qua mọi internal request không?
- Log pattern có đưa correlation ID từ MDC vào mọi dòng log không?
- Có OpenTelemetry/Zipkin/Tempo distributed tracing chưa?
- Có alert theo error rate, p95 latency, database pool và integration failure chưa?

### 12.2 Health, liveness và readiness

- Liveness trả lời: process còn sống hay bị deadlock?
- Readiness trả lời: instance đã sẵn sàng nhận traffic chưa?
- Một dependency tùy chọn không nhất thiết phải làm readiness fail.

Ví dụ Redis health của identity-service mặc định tắt, hợp lý khi cache mặc định là Caffeine hoặc Redis chỉ là tùy chọn.

### 12.3 Golden signals

Khi được hỏi nên monitor gì, trả lời bốn nhóm:

- Latency: p50/p95/p99 theo endpoint.
- Traffic: request/second, message/event rate.
- Errors: tỷ lệ 4xx/5xx, integration failure, authentication failure.
- Saturation: CPU, memory, thread pool, DB connection pool, disk.

## 13. Deployment và configuration

### 13.1 Profile

Các service có profile `local`, `dev`, `prod`; Compose có thêm biến thể dev/staging/prod.

- Local thường trỏ đến `localhost` và các cổng 809x.
- Dev/container dùng hostname như `postgres`, `keycloak`, `timeline-service`.
- Prod yêu cầu nhiều giá trị quan trọng từ environment, tránh hard-code credential.

### 13.2 Docker Compose

Compose chịu trách nhiệm:

- Khởi chạy Postgres, Keycloak, service registry, gateway và business services.
- Cấu hình health check và dependency startup.
- Gắn persistent volume.
- Tách edge network và internal network.
- Khởi chạy Prometheus/Grafana cùng các hạ tầng tùy chọn.

`depends_on` với health condition chỉ giúp thứ tự khởi động ban đầu; nó không phải cơ chế tự phục hồi business-level khi dependency chết sau đó.

### 13.3 Graceful shutdown

`server.shutdown: graceful` cho phép ứng dụng:

- Ngừng nhận request mới.
- Cho request đang chạy một khoảng thời gian hoàn tất.
- Giảm lỗi trong lúc rolling deployment.

Nó không bảo đảm callback after-commit hay công việc nền luôn hoàn thành nếu container bị kill cưỡng bức.

### 13.4 Scaling

Stateless JWT giúp scale ngang vì service không giữ HTTP session server-side. Tuy nhiên khi scale cần chú ý:

- Caffeine cache không chia sẻ giữa instance.
- Scheduled job có thể chạy trùng nếu không có distributed lock.
- Consumer/event handler phải idempotent.
- Database pool và connection limit phải được tính theo tổng instance.
- Gateway và Eureka cũng cần chiến lược HA nếu yêu cầu availability cao.

### 13.5 Single points of failure hiện có thể có

Trong một Compose deployment đơn:

- Một gateway instance là điểm vào duy nhất.
- Một Eureka instance là registry duy nhất.
- Một PostgreSQL instance chứa nhiều logical database.
- Một Keycloak instance ảnh hưởng login/token discovery và một số flow quản trị.

Điều này phù hợp môi trường học tập/dev hoặc deployment nhỏ, nhưng production HA cần replication, multiple instances, external load balancer, backup và recovery plan.

## 14. Kiểm thử kiến trúc

Các loại test nên biết:

| Loại test | Mục tiêu |
|---|---|
| Unit test | Rule/policy/service method độc lập |
| Controller/MockMvc test | HTTP mapping, validation, status/response |
| Repository test | Query và persistence mapping |
| Migration test | Flyway chạy được trên database thực/compatible |
| Security integration test | Public/protected endpoint, JWT, 401/403, permission |
| Contract test | Consumer và provider thống nhất request/response |
| Integration test | Service + database + dependency giả/thật |
| End-to-end test | Client → Gateway → nhiều service → database |
| Resilience test | Timeout, retry, dependency unavailable, duplicate request |
| Architecture test | Cấm controller gọi repository, cấm import xuyên bounded context |

Điểm đặc biệt cần test cho LifeBalance:

- Task chỉ đọc/sửa theo owner.
- State transition hợp lệ và không hợp lệ.
- After-commit không chạy trước commit.
- Retry không tạo duplicate downstream.
- Internal endpoint từ chối khi thiếu secret hoặc owner mismatch.
- Permission cache được evict khi role/permission thay đổi.
- Gateway route đúng và service tự chặn request trực tiếp thiếu token.

---

# Phần V — Đánh giá kiến trúc hiện tại

## 15. Điểm mạnh

1. Service boundary bám tương đối rõ vào các business capability.
2. Gateway tạo entry point chung và route bằng service name.
3. Security được đóng gói thành auto-configuration dùng chung.
4. Gateway và service cùng validate JWT, có defense in depth.
5. JWT kiểm tra audience, không chỉ chữ ký/issuer.
6. Ownership được đưa vào nhiều repository query.
7. Database và migration tách theo service.
8. JPA `open-in-view` được tắt, tránh lazy query ngoài service transaction.
9. Error response và security error được chuẩn hóa.
10. Integration có timeout, selective retry và chạy sau commit.
11. Có task/history/audit phục vụ truy vết.
12. Có health probe, Prometheus, Grafana và graceful shutdown.

## 16. Rủi ro và hướng cải tiến

| Rủi ro hiện tại/điểm cần xác minh | Ảnh hưởng | Đề xuất |
|---|---|---|
| After-commit action không bền vững | Commit thành công nhưng downstream không nhận | Transactional Outbox + RabbitMQ/worker |
| POST retry chưa chắc idempotent | Tạo record trùng | Event ID/idempotency key + unique constraint |
| Gọi nhiều downstream tuần tự | Latency và resource occupation | Async dispatch/broker hoặc executor được kiểm soát |
| Base URL nội bộ cấu hình trực tiếp | Discovery/load balancing không đồng nhất | Discovery-aware client hoặc service mesh nếu cần |
| Forward user Bearer token | Phụ thuộc request context/token lifetime | Workload identity/client credentials + actor context |
| Shared internal secret | Khó xoay vòng, blast radius lớn | Secret manager, mTLS hoặc OAuth2 service identity |
| Một PostgreSQL instance | Lỗi vật lý ảnh hưởng nhiều service | Managed HA Postgres/replication; tách instance theo nhu cầu |
| Một gateway/Eureka/Keycloak instance | Điểm lỗi đơn | Multiple instances + load balancer + HA storage |
| Caffeine cache khi scale | Permission cache khác nhau giữa instance | Redis hoặc explicit invalidation event |
| Chưa thấy distributed tracing đầy đủ | Khó debug request xuyên service | OpenTelemetry + trace backend + correlation ID |
| RabbitMQ/MinIO có nhưng chưa dùng | Tăng chi phí vận hành không cần thiết | Tắt profile mặc định hoặc triển khai use case rõ ràng |
| AI service chưa thấy model provider | Tên gọi có thể gây kỳ vọng sai | Mô tả đúng capability hoặc thêm provider abstraction/evaluation |
| Nhiều service cho dự án nhỏ | Complexity cao hơn giá trị | Đánh giá lại modular monolith vs microservice theo tải/team |

Khi giảng viên hỏi “kiến trúc có hoàn hảo không?”, câu trả lời tốt là:

> Không có kiến trúc hoàn hảo, chỉ có kiến trúc phù hợp với constraint. Thiết kế hiện tại có separation rõ, security dùng chung và database ownership tốt. Đánh đổi lớn nhất là distributed consistency và độ tin cậy của integration sau commit. Nếu yêu cầu production cao hơn, ưu tiên của em là outbox/idempotency, distributed tracing, service identity và loại bỏ single point of failure.

---

# Phần VI — Kịch bản thuyết trình

## 17. Bài trình bày 5–7 phút

### Mở đầu

> Backend LifeBalance được thiết kế theo microservice. Em chia hệ thống thành ba nhóm: edge/platform, shared libraries và business services. Mục tiêu là tách trách nhiệm nghiệp vụ, dữ liệu và deployment giữa các miền như identity, task, timeline, finance và analytics.

### Sơ đồ tổng thể

> Client không gọi trực tiếp từng service mà đi qua API Gateway. Gateway kiểm tra JWT và route request theo path. Các service đăng ký với Eureka; gateway dùng `lb://service-name` để tìm instance. Keycloak phát access token. PostgreSQL lưu dữ liệu với database logic riêng cho từng service.

### Cấu trúc bên trong service

> Bên trong một service, controller xử lý HTTP và current user; service layer xử lý use case và transaction; domain giữ trạng thái nghiệp vụ; repository truy cập database; Flyway quản lý schema. Request/response dùng DTO thay vì trả JPA entity trực tiếp.

### Security

> Gateway và từng service cùng dùng shared security module. Token được kiểm tra thời hạn, issuer và audience. Claim được map thành principal chứa internal user ID và roles. Ngoài authentication, hệ thống dùng permission cho nghiệp vụ quản trị và owner scoping cho dữ liệu cá nhân. Vì vậy biết UUID của resource không đồng nghĩa với được truy cập resource đó.

### Use case minh họa

> Khi user hoàn thành task, controller lấy owner ID từ token. Service tìm task theo cả task ID và owner ID, kiểm tra state transition, đặt progress 100, lưu task và history trong local transaction. Sau commit, publisher có thể gọi timeline, notification và analytics. Việc gọi sau commit tránh downstream cập nhật khi transaction chính rollback.

### Consistency và failure

> Transaction không thể bao phủ nhiều service. Do đó hệ thống chấp nhận eventual consistency. REST client có timeout và retry, nhưng callback sau commit chưa bền vững; nếu process chết sau commit, event có thể mất. Hướng cải tiến là transactional outbox, RabbitMQ và idempotent consumer.

### Vận hành và kết luận

> Hệ thống có Docker Compose, health checks, graceful shutdown, Prometheus và Grafana. Điểm mạnh là boundary, security và data ownership rõ. Điểm cần cải thiện là durable messaging, tracing, HA và service-to-service identity. Như vậy architecture không chỉ là danh sách service mà còn là luồng request, data ownership, transaction boundary và cách hệ thống phản ứng khi lỗi.

## 18. Bài trả lời 30 giây

> LifeBalance Backend là hệ microservice Spring Boot gồm gateway, Eureka, shared common/security và tám business service. Client đi qua gateway; Keycloak phát JWT; gateway và service cùng validate token. Mỗi service sở hữu database logic riêng, dùng JPA/Flyway và giao tiếp qua REST. Với nghiệp vụ xuyên service, dữ liệu local commit trước rồi mới integration after-commit, nên hệ thống chấp nhận eventual consistency. Hạ tầng có Prometheus/Grafana; hướng nâng cấp quan trọng nhất là outbox, message broker, idempotency và distributed tracing.

---

# Phần VII — Bộ câu hỏi vấn đáp và câu trả lời mẫu

## 19. Câu hỏi nền tảng

### Câu 1. Kiến trúc của hệ thống là gì?

Hệ thống theo hướng microservice với API Gateway, Eureka service discovery, Keycloak/OAuth2 Resource Server, database-per-service ở mức logic, synchronous REST integration và một số callback after-commit. Các concern dùng chung như security và error contract được đóng gói thành Maven module dùng chung.

### Câu 2. Tại sao gọi đây là microservice?

Vì các business capability được tách thành ứng dụng Spring Boot độc lập, có process, cấu hình, database logic, migration và API riêng. Chúng giao tiếp qua network thay vì gọi method trong cùng process.

### Câu 3. Có phải cứ nhiều module là microservice không?

Không. Nhiều module có thể chỉ là modular monolith nếu cùng deploy trong một process và dùng chung database. Ở đây các business service có executable riêng, port riêng và container riêng nên mang đặc tính microservice. `lifebalance-common` và `lifebalance-security` chỉ là library module, không phải microservice.

### Câu 4. Bounded context là gì?

Là ranh giới trong đó một mô hình nghiệp vụ và thuật ngữ có ý nghĩa nhất quán. Ví dụ Task sở hữu vòng đời công việc; Timeline sở hữu placement và conflict. Cùng một task ID có thể xuất hiện ở Timeline như một reference/projection nhưng Timeline không trở thành nguồn chuẩn của task lifecycle.

### Câu 5. Tại sao không gộp Task và Timeline?

Task tập trung vào nội dung, phân loại và vòng đời công việc; Timeline tập trung vào lịch, placement và conflict. Tách ra giúp mô hình và tải độc lập. Đánh đổi là cần đồng bộ và xử lý eventual consistency. Nếu quy mô/team nhỏ, gộp thành module trong modular monolith cũng là phương án hợp lý; lựa chọn phụ thuộc constraint.

### Câu 6. Nhược điểm lớn nhất của microservice là gì?

Distributed systems complexity: network có thể lỗi, không có transaction chung, dữ liệu nhất quán trễ, debug khó và chi phí vận hành cao. Vì vậy cần timeout, retry, idempotency, observability và automation tốt.

## 20. Gateway và discovery

### Câu 7. Tại sao cần API Gateway?

Để client có một entry point, che giấu địa chỉ service, tập trung route và chặn authentication failure sớm. Gateway cũng là nơi phù hợp cho cross-cutting policy như CORS, rate limit hoặc request ID, dù cần xác minh từng policy đã triển khai hay chưa.

### Câu 8. Gateway có chứa business logic không?

Không nên. Gateway chỉ nên xử lý concern ở biên như routing, authentication sơ bộ và traffic policy. Business rule thuộc service sở hữu domain.

### Câu 9. Eureka hoạt động thế nào?

Service đăng ký tên và địa chỉ với registry. Gateway tra registry theo service name và client-side load balancer chọn instance. Nhờ vậy route không phụ thuộc một IP cố định.

### Câu 10. Nếu Eureka chết thì mọi request có chết ngay không?

Không nhất thiết ngay lập tức vì client thường có registry cache. Nhưng không thể phát hiện instance mới/thay đổi lâu dài, nên khả năng route sẽ suy giảm. Với yêu cầu HA cần nhiều Eureka instance hoặc giải pháp discovery do nền tảng cung cấp.

### Câu 11. Service-to-service có đi qua gateway không?

Theo code hiện tại, các integration chính dùng `RestClient` và base URL trực tiếp, nên không đi qua gateway. Điều này tránh thêm một hop và tách internal traffic khỏi public edge. Đổi lại cần tự quản lý internal discovery/security/observability.

### Câu 12. Mọi lời gọi có dùng Eureka không?

Không. Gateway dùng `lb://...` với Eureka. Internal REST hiện dùng hostname/base URL cấu hình trực tiếp. Các service vẫn đăng ký Eureka, nhưng không nên suy ra rằng mọi RestClient call đều dùng registry.

## 21. Security

### Câu 13. Keycloak đóng vai trò gì?

Keycloak là identity provider và authorization server: quản lý realm/client/user/role, xác thực đăng nhập và phát JWT. Business services là resource server, nhận và kiểm tra access token.

### Câu 14. JWT gồm những phần nào?

Header, payload và signature. Header mô tả thuật toán/key ID; payload chứa claim; signature giúp phát hiện token bị sửa. Payload được base64url encoding chứ không phải mã hóa, nên không đặt dữ liệu bí mật trong claim.

### Câu 15. Hệ thống kiểm tra JWT gì?

Chữ ký qua JWK, thời hạn, issuer khi cấu hình và audience bắt buộc theo client ID. Sau đó claim được map thành principal nội bộ.

### Câu 16. Tại sao kiểm tra audience?

Để bảo đảm token được phát cho LifeBalance API, không phải một client/resource server khác. Chữ ký hợp lệ một mình chưa đủ để chứng minh token được phép dùng cho API này.

### Câu 17. Vì sao gateway kiểm tra rồi service vẫn kiểm tra?

Defense in depth. Nếu service bị gọi trực tiếp hoặc gateway bị bypass, service vẫn bảo vệ resource. Nó cũng làm trust boundary rõ hơn.

### Câu 18. Stateless nghĩa là gì?

Server không lưu HTTP login session cho từng client. Mỗi request mang bearer token đủ để xác thực. Điều này hỗ trợ scale ngang, nhưng revoke token tức thì khó hơn nếu access token còn hạn.

### Câu 19. Vì sao tắt CSRF?

API dùng bearer token trong Authorization header và không dựa vào cookie session được browser tự động gửi, nên nguy cơ CSRF truyền thống giảm. Nếu sau này dùng authentication cookie thì phải đánh giá lại.

### Câu 20. Khác nhau giữa 401 và 403?

401 là không xác thực được. 403 là đã xác thực nhưng không được phép thực hiện hành động hoặc không sở hữu resource.

### Câu 21. Role và permission khác nhau thế nào?

Role là nhóm quyền, ví dụ admin/user. Permission là quyền thao tác cụ thể như `role:create` hoặc `audit:read`. Gán permission vào role giúp quản lý theo nhóm thay vì gán từng quyền trực tiếp cho từng user.

### Câu 22. Ownership có thay RBAC được không?

Không hoàn toàn. Ownership bảo vệ tài nguyên cá nhân; RBAC/permission kiểm soát loại thao tác. Một admin có thể có quyền đọc rộng, còn user thường chỉ thao tác dữ liệu của chính mình.

### Câu 23. Tại sao query theo `id` và `ownerId`?

Để chống IDOR. Kể cả người dùng biết UUID của resource khác, repository không trả về vì owner không khớp.

### Câu 24. Caffeine và Redis cache khác nhau thế nào?

Caffeine ở trong memory của từng instance, nhanh và đơn giản nhưng không chia sẻ. Redis là network cache dùng chung, phù hợp nhiều instance nhưng tạo dependency và latency mới. Hiện Caffeine là mặc định, Redis là tùy chọn.

### Câu 25. Internal secret đã đủ an toàn chưa?

Nó tốt hơn endpoint nội bộ không có credential, nhưng shared secret có blast radius và bài toán rotation. Production có thể dùng mTLS hoặc OAuth2 client credentials/workload identity. Ngoài secret, hệ thống còn đối chiếu owner/token cho một số internal request.

## 22. Data và transaction

### Câu 26. Mỗi service có database riêng thật không?

Có database logic và credential riêng, nhưng các database hiện chạy trong cùng một PostgreSQL container/instance. Đó là logical isolation, chưa phải physical isolation.

### Câu 27. Tại sao không cho service dùng chung bảng?

Dùng chung bảng phá vỡ ownership, làm schema và deployment coupling, cho phép service bypass business rule của nhau. Giao tiếp qua API/event giữ ranh giới rõ hơn.

### Câu 28. Tại sao dùng UUID?

UUID có thể sinh phân tán, khó đoán tuần tự và tránh va chạm ID giữa service. Đánh đổi là index lớn hơn integer và locality kém nếu dùng UUID ngẫu nhiên.

### Câu 29. Flyway dùng để làm gì?

Version hóa thay đổi schema, chạy migration nhất quán giữa môi trường và tạo audit trail cho database evolution.

### Câu 30. `open-in-view: false` có ý nghĩa gì?

JPA session không kéo dài đến lúc render response. Service phải tải đủ dữ liệu trong transaction, tránh query ngầm từ controller/serializer và làm transaction boundary rõ hơn.

### Câu 31. Transaction của task-service có rollback timeline-service không?

Không. `@Transactional` chỉ áp dụng local datasource. HTTP call tới service khác nằm ngoài transaction đó.

### Câu 32. Distributed transaction là gì và hệ thống có dùng không?

Distributed transaction cố tạo atomicity trên nhiều resource/service, ví dụ 2PC. Không thấy hệ thống dùng 2PC. Hệ thống hiện ưu tiên local transaction và after-commit integration/eventual consistency.

### Câu 33. Eventual consistency là gì?

Các service không nhất thiết đồng bộ ngay tại mọi thời điểm nhưng được thiết kế để hội tụ về trạng thái nhất quán sau đó. Muốn bảo đảm hội tụ phải có retry bền vững, idempotency hoặc reconciliation, không chỉ gọi một lần.

### Câu 34. Vì sao ghi history trong cùng transaction?

Để tránh task thay đổi nhưng history không được ghi hoặc ngược lại. Nếu cùng datasource/transaction, hai thay đổi commit/rollback cùng nhau.

### Câu 35. Vì sao audit listener chạy AFTER_COMMIT?

Để chỉ ghi audit success sau khi thay đổi nghiệp vụ thật sự commit. Listener còn bắt lỗi riêng để audit persistence failure không phá kết quả nghiệp vụ đã commit.

## 23. Integration và resilience

### Câu 36. Vì sao gọi downstream sau commit?

Để không cập nhật downstream nếu local transaction cuối cùng rollback. Đây là lựa chọn consistency hợp lý, nhưng cần giải quyết khoảng trống giữa local commit và delivery.

### Câu 37. Nếu downstream lỗi sau commit thì sao?

Task vẫn đã commit. Client hiện retry một số lỗi tối đa theo cấu hình và ghi warning nếu thất bại. Không thấy durable replay trong luồng này, nên có nguy cơ sai lệch kéo dài.

### Câu 37a. After-commit có nghĩa là asynchronous không?

Không. After-commit chỉ nói callback chạy sau khi local transaction commit. Trong code hiện tại callback chạy đồng bộ trên thread xử lý request, nên các HTTP call và thời gian retry vẫn có thể kéo dài thời gian trả response. Muốn bất đồng bộ bền vững cần executor/queue phù hợp, tốt hơn là outbox kết hợp broker cho luồng quan trọng.

### Câu 38. Outbox Pattern giải quyết gì?

Nó atomically lưu business change và event cần gửi trong cùng database transaction. Worker có thể retry gửi event sau crash, đóng khoảng trống giữa commit và publish.

### Câu 39. Outbox có giải quyết duplicate không?

Không hoàn toàn. Broker/worker vẫn có thể deliver nhiều lần. Consumer phải idempotent hoặc deduplicate theo event ID.

### Câu 40. Tại sao phải đặt timeout?

Không có timeout, thread có thể chờ dependency vô hạn, làm cạn thread pool và gây cascading failure. Task integration hiện có connect và read timeout.

### Câu 41. Có nên retry mọi lỗi không?

Không. Lỗi mạng, timeout, 5xx hoặc 429 có thể tạm thời; nhiều 4xx là lỗi request vĩnh viễn, retry chỉ tăng tải. Retry POST còn phải đi cùng idempotency.

### Câu 42. Circuit breaker khác retry thế nào?

Retry thử lại cùng yêu cầu. Circuit breaker tạm ngừng gọi một dependency đang lỗi nhiều để tránh làm hệ thống quá tải và cho dependency thời gian phục hồi. Chưa nên nói hệ thống đã có circuit breaker nếu chưa có bằng chứng cấu hình/code.

### Câu 43. RabbitMQ đang dùng ở đâu?

Hiện RabbitMQ có trong Compose nhưng chưa thấy producer/consumer trong code. Vì vậy nó là hạ tầng chuẩn bị, chưa tham gia luồng nghiệp vụ chính.

### Câu 44. Khi nào nên dùng RabbitMQ?

Cho các side effect bất đồng bộ như notification, analytics projection, audit propagation hoặc integration event cần delivery bền vững. Query cần kết quả ngay vẫn phù hợp với REST.

### Câu 45. Nếu callback gọi Timeline thành công nhưng Notification thất bại thì sao?

Task và Timeline đã cập nhật, Notification chưa có. Đây là partial success giữa service. Cần event riêng, retry/replay độc lập và idempotency để mỗi consumer hội tụ mà không rollback toàn hệ thống.

### Câu 46. Monthly income integration đã chạy khi complete task chưa?

Code có capability `publishMonthlyIncomeReady` và client gọi Finance, nhưng hiện chưa thấy `TaskServiceImpl` gọi method này. Không nên khẳng định luồng complete task tự động ghi lương nếu chưa wiring và test end-to-end xác nhận.

## 24. Observability và deployment

### Câu 47. Prometheus và Grafana khác nhau thế nào?

Prometheus scrape, lưu và query time-series metrics. Grafana dùng datasource như Prometheus để dựng dashboard và visualization.

### Câu 48. Log và audit khác nhau thế nào?

Log phục vụ vận hành/debug; audit phục vụ chứng minh ai làm gì, lúc nào, trên đối tượng nào và kết quả ra sao. Audit thường có yêu cầu retention và chống sửa nghiêm ngặt hơn.

### Câu 49. Correlation ID dùng để làm gì?

Gắn một ID chung vào log của gateway và các downstream service để truy vết một request xuyên hệ thống. Nó không thay thế distributed trace nhưng giúp tìm log liên quan.

### Câu 50. Metrics chưa đủ, vì sao cần tracing?

Metrics cho biết hệ thống chậm hoặc lỗi; trace cho biết request chậm ở hop nào, database hay downstream nào. Microservice đặc biệt cần trace vì một request đi qua nhiều process.

### Câu 51. Health check có bảo đảm service đúng nghiệp vụ không?

Không. Health thường chỉ kiểm tra process/dependency cơ bản. Service có thể trả health UP nhưng một use case vẫn lỗi do cấu hình hoặc dữ liệu. Cần metrics, synthetic check và test nghiệp vụ bổ sung.

### Câu 52. `depends_on` có thay retry được không?

Không. Nó hỗ trợ thứ tự start container, nhưng dependency có thể chết sau khi ứng dụng đã khởi động. Runtime vẫn cần timeout, retry, circuit breaker và recovery.

### Câu 53. Graceful shutdown có ích gì?

Giảm request bị cắt giữa chừng khi deploy/restart bằng cách ngừng nhận việc mới và chờ việc đang chạy. Nó không bảo vệ khi process bị kill cưỡng bức hoặc máy chết.

### Câu 54. Vì sao dùng nhiều profile?

Để tách khác biệt môi trường như port, database URL, Keycloak URL, logging và exposure. Code nghiệp vụ giữ nguyên, cấu hình được inject theo môi trường.

### Câu 55. Làm sao scale service?

Chạy nhiều instance stateless, đăng ký discovery và phân tải. Sau đó phải tính database connections, cache consistency, scheduled jobs, idempotency và shared storage.

## 25. Câu hỏi phản biện thiết kế

### Câu 56. Tại sao không chọn modular monolith?

Microservice hợp lý nếu cần team/deployment/scale độc lập và domain đủ tách biệt. Nếu đây là đội nhỏ và tải chưa lớn, modular monolith có thể giảm đáng kể distributed complexity. Câu trả lời tốt là thừa nhận tradeoff thay vì nói microservice luôn tốt hơn.

### Câu 57. Shared common/security có làm mất độc lập không?

Có mức coupling ở compile time và release. Nó giúp thống nhất nhưng thay đổi breaking có blast radius lớn. Cần giữ module nhỏ, ổn định, version hóa và tránh đưa business logic đặc thù vào shared library.

### Câu 58. Một PostgreSQL instance có trái database-per-service không?

Không trái hoàn toàn vì ownership/schema/credential vẫn tách logic. Nhưng nó giảm failure isolation vật lý. Đây là giải pháp thực dụng cho dev hoặc quy mô nhỏ; production có thể tách khi yêu cầu availability/tải/bảo mật tăng.

### Câu 59. Điểm yếu lớn nhất em sẽ sửa đầu tiên là gì?

Durability của cross-service integration. Em sẽ thêm transactional outbox, event ID và idempotent consumer; dùng RabbitMQ đã có trong hạ tầng cho side effect phù hợp. Sau đó bổ sung distributed tracing và alert.

### Câu 60. Làm sao chứng minh service boundary đúng?

Kiểm tra mỗi service có business vocabulary và invariant riêng, sở hữu dữ liệu riêng, thay đổi thường tập trung trong một miền, và giao tiếp bằng hợp đồng rõ. Nếu một use case luôn buộc sửa đồng thời nhiều service hoặc chatty calls quá nhiều, boundary có thể chưa đúng.

### Câu 61. Làm sao tránh distributed monolith?

Không dùng chung database/schema, tránh deployment bắt buộc đồng thời, version hóa contract, giảm synchronous call chain, dùng event khi phù hợp, có test contract và cho service degrade độc lập.

### Câu 62. Hệ thống có phải event-driven không?

Chưa phải event-driven ở mức liên service. Có event/callback nội bộ Spring và after-commit publisher, nhưng các side effect chính vẫn gọi REST. RabbitMQ chưa thấy được nối vào producer/consumer.

### Câu 63. AI service có thực sự dùng AI model không?

Chưa thấy provider SDK/client tới LLM trong code hiện tại. Nó có domain và API cho conversation/insight/recommendation. Muốn khẳng định dùng AI model cần chỉ ra provider abstraction, prompt/model call, timeout, token/cost control và evaluation.

### Câu 64. Làm sao bảo vệ dữ liệu khi service bị gọi trực tiếp?

Mỗi service tự là OAuth2 Resource Server, validate JWT, lấy owner từ token và kiểm tra permission/ownership. Network policy và internal credential bổ sung lớp bảo vệ nhưng không thay application-level authorization.

### Câu 65. Backup một PostgreSQL container có đủ không?

Backup là bước đầu nhưng cần kiểm tra restore. Production cần lịch backup, retention, encryption, point-in-time recovery, restore drill và RPO/RTO rõ. Vì một instance chứa nhiều database, sự cố có blast radius lớn.

---

# Phần VIII — Những điều không được trả lời sai

## 26. Câu đúng và câu sai thường gặp

### Sai: “Mọi service call đều đi qua Gateway.”

Đúng: Client traffic đi qua Gateway; internal integration hiện dùng base URL trực tiếp.

### Sai: “Mọi service call đều dùng Eureka.”

Đúng: Gateway dùng `lb://` và Eureka. Internal `RestClient` hiện dùng URL/hostname cấu hình.

### Sai: “Hệ thống đã event-driven bằng RabbitMQ.”

Đúng: RabbitMQ đã có container nhưng chưa thấy producer/consumer; integration chính hiện là REST after commit.

### Sai: “Redis đang là cache mặc định.”

Đúng: Caffeine là RBAC cache mặc định; Redis là tùy chọn khi đổi property.

### Sai: “Mỗi service có một PostgreSQL server riêng.”

Đúng: Mỗi service có database/user logic riêng trong cùng một PostgreSQL instance của Compose.

### Sai: “After commit bảo đảm downstream chắc chắn nhận được event.”

Đúng: Nó chỉ bảo đảm không dispatch trước local commit; không bảo đảm durable delivery khi process/network lỗi.

### Sai: “Retry sẽ bảo đảm không mất dữ liệu.”

Đúng: Retry xử lý một số lỗi tạm thời. Muốn reliability cần durable state/outbox; muốn tránh duplicate cần idempotency.

### Sai: “JWT được mã hóa nên payload bí mật.”

Đúng: JWT payload thường chỉ encode; signature chống sửa nhưng không che nội dung.

### Sai: “Gateway validate JWT rồi service không cần validate.”

Đúng: Service validate lại để giữ trust boundary và chống gateway bypass.

### Sai: “Transaction của task-service rollback được timeline và notification.”

Đúng: Transaction chỉ áp dụng local datasource. Cross-service consistency là eventual/compensating, không phải ACID chung.

### Sai: “AI service hiện đang gọi OpenAI.”

Đúng: Chưa thấy model provider client/dependency trong code; chỉ nên mô tả capability hiện có.

### Sai: “Monthly income chắc chắn được ghi khi task complete.”

Đúng: Có publisher/client hỗ trợ, nhưng chưa thấy service completion wiring gọi `publishMonthlyIncomeReady`.

---

# Phần IX — Checklist tự kiểm tra

## 27. Bạn đã nắm architecture nếu trả lời được các câu sau mà không nhìn tài liệu

### Toàn hệ thống

- [ ] Kể tên ba nhóm module: platform, shared library, business service.
- [ ] Vẽ được đường đi Client → Gateway → Service → Database.
- [ ] Giải thích vai trò khác nhau của Gateway, Eureka và Keycloak.
- [ ] Nói được service nào sở hữu dữ liệu nào.

### Một service

- [ ] Giải thích Controller → Service → Domain → Repository.
- [ ] Nói transaction bắt đầu/kết thúc ở đâu.
- [ ] Phân biệt DTO, entity, mapper, policy và validator.
- [ ] Giải thích cách owner ID được lấy và kiểm tra.

### Security

- [ ] Phân biệt authentication/authorization và 401/403.
- [ ] Giải thích issuer, audience, signature, expiration.
- [ ] Giải thích vì sao gateway và service cùng validate.
- [ ] Phân biệt RBAC và ownership.

### Distributed systems

- [ ] Giải thích local transaction và eventual consistency.
- [ ] Giải thích lợi ích/hạn chế của after-commit.
- [ ] Giải thích timeout, retry, circuit breaker, idempotency.
- [ ] Mô tả Transactional Outbox mà không nói hệ thống đã có.

### Vận hành

- [ ] Phân biệt logs, metrics, traces.
- [ ] Nói được Prometheus và Grafana làm gì.
- [ ] Phân biệt liveness/readiness.
- [ ] Chỉ ra single points of failure trong Compose đơn.

## 28. Bài tập tốt nhất để thực sự hiểu code

Thực hiện lần lượt cho use case `PATCH /api/tasks/{id}/complete`:

1. Chỉ ra route trong gateway.
2. Chỉ ra nơi JWT được validate.
3. Chỉ ra nơi JWT được map thành current user.
4. Chỉ ra controller lấy owner ID.
5. Chỉ ra service method và `@Transactional`.
6. Chỉ ra policy kiểm tra state transition.
7. Chỉ ra repository query theo ID và owner.
8. Chỉ ra nơi lưu task và history.
9. Chỉ ra nơi đăng ký after-commit.
10. Chỉ ra downstream endpoints, timeout và retry.
11. Mô tả ba failure case: rollback trước commit, downstream 500, process chết sau commit.
12. Đề xuất outbox và idempotency key cho luồng này.

Nếu làm được bài tập trên, bạn không chỉ “thuộc sơ đồ” mà đã hiểu kiến trúc vận hành.

---

# Phần X — Bản đồ mã nguồn nên đọc

## 29. Các file gốc

- Parent module: `lifebalance-backend/pom.xml`.
- Runtime topology: `lifebalance-backend/compose.yaml`.
- PostgreSQL logical databases: `lifebalance-backend/docker/postgres/init/001-create-databases.sh`.
- Gateway routes: `lifebalance-backend/gateway/src/main/resources/application.yaml`.
- Shared security: `lifebalance-backend/lifebalance-security/src/main/java/com/lifebalance/security/keycloak/LifebalanceSecurityAutoConfiguration.java`.
- JWT user mapping: `lifebalance-backend/lifebalance-security/src/main/java/com/lifebalance/security/keycloak/KeycloakUserMapper.java`.
- Request current-user filter: `lifebalance-backend/lifebalance-security/src/main/java/com/lifebalance/security/keycloak/KeycloakUserMappingFilter.java`.
- Error contract: `lifebalance-backend/lifebalance-common/src/main/java/com/lifebalance/common/error/GlobalExceptionHandler.java`.
- Task request flow: `lifebalance-backend/task-service/src/main/java/com/lifebalance/task/controller/TaskController.java`.
- Task use cases: `lifebalance-backend/task-service/src/main/java/com/lifebalance/task/service/impl/TaskServiceImpl.java`.
- Task integration scheduling: `lifebalance-backend/task-service/src/main/java/com/lifebalance/task/integration/AfterCommitTaskIntegrationPublisher.java`.
- Task REST integration: `lifebalance-backend/task-service/src/main/java/com/lifebalance/task/integration/RestTaskIntegrationClient.java`.
- Task integration settings: `lifebalance-backend/task-service/src/main/java/com/lifebalance/task/integration/TaskIntegrationProperties.java`.
- Identity RBAC cache: `lifebalance-backend/identity-service/src/main/java/com/lifebalance/identity/config/RbacCacheConfig.java`.
- ERD hiện có: `docs/erd.md`.
- Task/Timeline diagrams hiện có: `docs/task-timeline-diagrams.md`.

## 30. Kết luận cần nhớ

Architecture của LifeBalance không nằm trong một folder tên `architecture`. Nó được thể hiện qua:

- Cách chia bounded context thành service.
- Ownership của database.
- Route ở gateway và registry ở Eureka.
- Security filter và permission/ownership checks.
- Transaction boundary trong service layer.
- Cách integration được dispatch sau commit.
- Cách lỗi, retry, cache, logging, metrics và deployment được xử lý.

Câu kết luận tốt khi vấn đáp:

> Em đánh giá kiến trúc bằng bốn tiêu chí: ranh giới trách nhiệm, ownership dữ liệu, luồng giao tiếp và hành vi khi lỗi. LifeBalance đã có nền tảng microservice rõ với gateway, discovery, shared security và database logic riêng. Phần cần hoàn thiện nhất để đạt độ tin cậy production là durable integration bằng outbox/message broker, idempotency, distributed tracing và high availability.

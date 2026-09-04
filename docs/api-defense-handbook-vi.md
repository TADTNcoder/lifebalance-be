# Nội dung bảo vệ API dự án LifeBalance

> Kịch bản thuyết trình, demo và trả lời phản biện về API, bám theo mã nguồn hiện tại của dự án.
>
> Cập nhật ngày 04/09/2026. Tài liệu phân biệt rõ **hiện trạng đã có trong code** và **đề xuất cải tiến**. Không trình bày đề xuất như một chức năng đã triển khai.

---

## 0. Cách dùng tài liệu

Nếu buổi bảo vệ khoảng 10–12 phút, hãy dùng:

1. Mục 1 để mở đầu.
2. Mục 2–9 làm nội dung chính.
3. Mục 10 làm demo.
4. Mục 11–12 để tự đánh giá và kết luận.
5. Mục 15 để luyện câu hỏi phản biện.

Nếu chỉ được nói 3 phút, dùng nguyên bài nói ở Mục 14.

Khi trả lời một câu hỏi, dùng cấu trúc:

> **Mục tiêu API → Thiết kế hiện tại → Ví dụ trong LifeBalance → Lợi ích → Đánh đổi/cải tiến.**

Ví dụ:

> Mục tiêu của pagination là không trả toàn bộ dữ liệu trong một lần. LifeBalance nhận `page`, `size` và sort/filter ở các API danh sách. Việc này giảm băng thông và tải database. Đánh đổi là client phải quản lý trang và cần một contract pagination thống nhất giữa các service.

---

# Phần I — Bài bảo vệ API hoàn chỉnh

## 1. Mở đầu

### Nội dung để nói

> Đề tài của em là LifeBalance, một hệ thống hỗ trợ quản lý công việc, lịch trình, nguồn lực, tài chính, thông báo, phân tích và gợi ý. Frontend không truy cập trực tiếp database mà giao tiếp với backend thông qua API.
>
> API trong dự án đóng vai trò là hợp đồng giữa frontend với backend và giữa các service. Hợp đồng này quy định URL, HTTP method, dữ liệu đầu vào, dữ liệu trả về, status code, quy tắc xác thực, phân quyền và validation.
>
> Dự án sử dụng HTTP REST, dữ liệu chủ yếu ở dạng JSON. API được chia theo business domain thay vì gom tất cả chức năng vào một controller lớn. Các request từ client đi qua API Gateway, sau đó được định tuyến đến service sở hữu nghiệp vụ tương ứng.

### Ý chính cần nhớ

- HTTP là giao thức truyền request/response.
- REST là phong cách tổ chức API theo resource và semantics của HTTP.
- JSON là định dạng dữ liệu phổ biến, không phải điều kiện bắt buộc của REST.
- API là hợp đồng bên ngoài; service/domain/database là cách thực thi bên trong.

## 2. Tổng quan API của LifeBalance

### 2.1 Các nhóm API

| Nhóm API | Base path tiêu biểu | Trách nhiệm |
|---|---|---|
| Identity | `/api/auth`, `/api/users`, `/api/roles`, `/api/permissions` | Current user, user, role, permission, audit và quản trị |
| Task | `/api/tasks`, `/api/categories`, `/api/tags` | Task CRUD, lifecycle, category, tag, reminder, recurring rule |
| Timeline | `/api/timeline` | Timeline task, placement, conflict, availability, history |
| Resource Capital | `/api/v1/capital`, `/api/v1/capital-cycles`, `/api/v1/capital-allocations` | Vốn thời gian/tiền, chu kỳ, phân bổ và điều chỉnh |
| Finance | `/api/finance`, `/api/transactions` | Account, category, transaction, history và report |
| Notification | `/api/notifications` | Notification, delivery, retry, preference, template và history |
| Analytics | `/api/analytics` | Actual record, evaluation, tracking và report |
| AI | `/api/ai` | Conversation, insight, recommendation và history |

### 2.2 Quy mô API trong code hiện tại

Số liệu sau đếm các handler method có `@GetMapping`, `@PostMapping`, `@PutMapping`, `@PatchMapping` hoặc `@DeleteMapping`. Đây là số handler, không phải số URL duy nhất vì một handler có thể có nhiều alias.

| Service | Controller | GET | POST | PUT | PATCH | DELETE | Tổng handler |
|---|---:|---:|---:|---:|---:|---:|---:|
| Identity | 11 | 37 | 15 | 7 | 7 | 5 | 71 |
| Task | 8 | 12 | 7 | 6 | 14 | 5 | 44 |
| Timeline | 4 | 9 | 2 | 0 | 4 | 0 | 15 |
| Resource Capital | 8 | 9 | 15 | 1 | 1 | 1 | 27 |
| Finance | 6 | 9 | 6 | 0 | 3 | 0 | 18 |
| Notification | 6 | 9 | 3 | 1 | 9 | 0 | 22 |
| Analytics | 6 | 14 | 4 | 0 | 4 | 0 | 22 |
| AI | 5 | 9 | 4 | 0 | 4 | 0 | 17 |
| **Tổng** | **54** | **108** | **56** | **15** | **46** | **11** | **236** |

Không cần đọc toàn bộ con số khi thuyết trình. Có thể nói:

> Backend hiện có hơn 200 API handler trên tám business service. Em không trình bày theo cách liệt kê từng endpoint mà tập trung vào convention dùng chung và hai use case đại diện là Task Lifecycle và Capital Allocation.

## 3. Request đi vào hệ thống như thế nào?

```text
Frontend/Mobile
      │
      │ HTTP request + Bearer JWT + JSON
      ▼
 API Gateway
      │ route theo path
      ▼
Business Service
      │ Controller → Service → Repository
      ▼
   Database
      │
      ▼
HTTP response + JSON/status code
```

### Nội dung để nói

> Client sử dụng một địa chỉ Gateway thay vì phải biết port của từng service. Gateway đối chiếu path, ví dụ `/api/tasks/**` được chuyển đến Task Service, `/api/finance/**` được chuyển đến Finance Service.
>
> Gateway dùng Eureka và URI dạng `lb://SERVICE-NAME` để tìm service. Sau khi request tới business service, service vẫn tự validate JWT. Việc validate ở cả Gateway và service là defense in depth: Gateway chặn request sai sớm, còn service vẫn an toàn nếu bị gọi trực tiếp trong internal network.

### Một điểm cần trả lời chính xác

- Client traffic: đi qua Gateway.
- Internal REST giữa service: hiện dùng base URL trực tiếp như `http://timeline-service:8080`, không đi vòng qua Gateway.
- Vì vậy không được nói “mọi request trong hệ thống đều đi qua Gateway”.

## 4. REST convention và HTTP method

### 4.1 Resource API

Resource được đặt tên bằng danh từ:

```text
/api/tasks
/api/tasks/{id}
/api/categories
/api/notifications
/api/finance/accounts
```

| Method | Mục đích thường dùng | Ví dụ |
|---|---|---|
| `GET` | Đọc resource | `GET /api/tasks/{id}` |
| `POST` | Tạo resource hoặc command không idempotent | `POST /api/tasks` |
| `PUT` | Cập nhật/thay thế representation | `PUT /api/tasks/{id}` |
| `PATCH` | Cập nhật một phần hoặc business transition | `PATCH /api/tasks/{id}/complete` |
| `DELETE` | Xóa resource | `DELETE /api/tasks/{id}` |

### 4.2 Tại sao Task Lifecycle dùng action endpoint?

Các endpoint:

```text
PATCH /api/tasks/{id}/plan
PATCH /api/tasks/{id}/pause
PATCH /api/tasks/{id}/resume
PATCH /api/tasks/{id}/complete
PATCH /api/tasks/{id}/cancel
PATCH /api/tasks/{id}/reopen
PATCH /api/tasks/{id}/archive
PATCH /api/tasks/{id}/restore
```

### Nội dung để nói

> Một transition như hoàn thành task không đơn giản là cho client sửa trực tiếp field `status`. Backend phải kiểm tra trạng thái hiện tại, rule chuyển trạng thái, owner, progress, completed time, history và integration. Vì vậy dự án biểu diễn các transition quan trọng thành command endpoint. Cách này làm business intent rõ và ngăn client đặt một trạng thái tùy ý.

### Tradeoff

- Ưu điểm: rõ ý nghĩa nghiệp vụ, dễ đặt rule và audit.
- Nhược điểm: không hoàn toàn thuần CRUD; số endpoint tăng.
- Đây vẫn là API dựa trên HTTP/REST semantics, thường được gọi là pragmatic REST.

## 5. Thiết kế request

Một request có bốn nhóm thông tin.

### 5.1 Path variable

Xác định resource cụ thể:

```http
GET /api/tasks/8a1c...f20
```

```java
@PathVariable UUID id
```

### 5.2 Query parameter

Dùng cho filter, search, pagination và sorting:

```http
GET /api/tasks?status=PLANNED&priority=HIGH&page=0&size=10&sortBy=createdAt&sortDirection=DESC
```

### 5.3 Header

```http
Authorization: Bearer <access-token>
Content-Type: application/json
X-Request-ID: request-123
```

### 5.4 Request body

Ví dụ tạo task:

```json
{
  "name": "Chuẩn bị bảo vệ API",
  "description": "Đọc tài liệu và luyện câu hỏi phản biện",
  "currency": "VND",
  "priority": "HIGH",
  "deadline": "2026-09-10",
  "estimatedMinutes": 180,
  "estimatedCost": 0
}
```

### 5.5 Vì sao dùng DTO?

API nhận và trả DTO thay vì dùng JPA entity trực tiếp để:

- Không cho client ghi các field nội bộ như ID, owner, audit time.
- Không lộ cấu trúc database.
- Tránh lỗi lazy loading và vòng lặp serialization.
- Cho API contract và schema database thay đổi tương đối độc lập.
- Đặt validation phù hợp từng use case.

## 6. Validation

### 6.1 Validation hình thức

Bean Validation xử lý các rule có thể kiểm tra ngay trên request:

```text
@NotBlank      không được trống
@NotNull       không được null
@Size          giới hạn độ dài
@Positive      phải lớn hơn 0
@PositiveOrZero không được âm
@Pattern       đúng biểu thức định dạng
@Digits        giới hạn phần nguyên/thập phân
```

Ví dụ `CreateTaskRequest`:

- `name` không trống và tối đa 255 ký tự.
- `estimatedMinutes` phải dương nếu có.
- `estimatedCost` không được âm.
- Currency của monthly income phải có đúng ba chữ cái viết hoa.
- Period phải có dạng `YYYY-MM`.

### 6.2 Validation nghiệp vụ

Service/domain/policy xử lý các rule cần database hoặc trạng thái hệ thống:

- Tên task có xung đột theo owner và thời gian không?
- Category có thuộc owner hiện tại không?
- Task có được phép chuyển từ trạng thái hiện tại sang `COMPLETED` không?
- Capital cycle có active không?
- Số vốn phân bổ có vượt số vốn còn lại không?
- Target của allocation có tồn tại không?

### Câu nói quan trọng

> Validation annotation bảo vệ cấu trúc đầu vào; business validation bảo vệ invariant của domain. Một API đúng phải có cả hai lớp.

## 7. Authentication, authorization và ownership

### 7.1 Bearer JWT

Client gửi:

```http
Authorization: Bearer eyJ...
```

Hệ thống kiểm tra:

1. Chữ ký JWT.
2. Thời gian hiệu lực.
3. Issuer của Keycloak.
4. Audience, mặc định là `lifebalance-api`.
5. Các claim cần thiết.

Sau đó JWT được ánh xạ thành `KeycloakUserPrincipal` gồm:

- Keycloak subject.
- Internal user ID.
- Username và email.
- Realm roles và client roles.

### 7.2 RBAC và permission

Identity API sử dụng permission chi tiết, ví dụ:

```text
role:create
role:update
role:assign
permission:read
user:update
audit:read
```

Method security được kiểm tra bằng `@PreAuthorize` và permission evaluation service.

### 7.3 Ownership

Các API dữ liệu cá nhân thường:

1. Lấy owner ID từ JWT/current principal.
2. Không tin owner ID do client tự khai báo.
3. Query resource bằng cả resource ID và owner ID.

Ví dụ logic:

```text
findByIdAndOwnerId(taskId, authenticatedUserId)
```

Điều này chống IDOR: người dùng biết UUID của task khác vẫn không đọc hoặc sửa được.

### 7.4 401 và 403

- `401 Unauthorized`: chưa xác thực được, ví dụ thiếu token hoặc token sai.
- `403 Forbidden`: đã xác thực nhưng không có permission, owner không khớp hoặc thiếu internal credential.

### Nội dung để nói

> API không chỉ kiểm tra user đã đăng nhập hay chưa. Với API quản trị, hệ thống kiểm tra permission. Với dữ liệu cá nhân, hệ thống kiểm tra ownership. Authentication, permission và ownership là ba lớp khác nhau.

## 8. Thiết kế response và error

### 8.1 Response envelope dùng chung

`lifebalance-common` định nghĩa:

```json
{
  "success": true,
  "data": {
    "id": "...",
    "name": "Chuẩn bị bảo vệ API"
  },
  "error": null,
  "timestamp": "2026-09-04T10:00:00Z"
}
```

Khi lỗi:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "Request validation failed",
    "details": {
      "name": "must not be blank"
    }
  },
  "timestamp": "2026-09-04T10:00:00Z"
}
```

### 8.2 Error handling

Global exception handler xử lý:

- `AppException`: lỗi nghiệp vụ có code và status riêng.
- Bean Validation: `400` với lỗi theo field.
- Sai kiểu path/query hoặc JSON: `400`.
- Authentication failure: `401`.
- Access denied: `403`.
- Không tìm thấy resource: `404`.
- Lỗi không dự kiến: log nội bộ và trả `500` tổng quát.

### 8.3 Status code nên trình bày

| Status | Ý nghĩa |
|---:|---|
| `200 OK` | Đọc/cập nhật thành công |
| `201 Created` | Tạo resource thành công |
| `204 No Content` | Thành công nhưng không có body |
| `400 Bad Request` | Request hoặc validation sai |
| `401 Unauthorized` | Chưa xác thực được |
| `403 Forbidden` | Không có quyền |
| `404 Not Found` | Không tìm thấy resource |
| `409 Conflict` | Xung đột dữ liệu/trạng thái |
| `429 Too Many Requests` | Vượt rate limit |
| `500 Internal Server Error` | Lỗi không dự kiến |
| `503 Service Unavailable` | Service tạm thời không khả dụng |

### 8.4 Hiện trạng chưa hoàn toàn đồng nhất

Nhiều service dùng `ApiResponse<T>`, nhưng một số Task controller trả `TaskResponse`, `Page<TaskResponse>` hoặc `void` trực tiếp.

Cách nói khi bảo vệ:

> Dự án đã có response model dùng chung nhưng mức áp dụng chưa hoàn toàn đồng nhất giữa các service. Đây là technical debt em nhận diện được. Hướng cải tiến là chuẩn hóa success envelope, error envelope và pagination contract, đồng thời bảo đảm thay đổi tương thích ngược với frontend.

Không nên cố phủ nhận inconsistency nếu giảng viên chỉ ra từ code.

## 9. Pagination, filtering và sorting

Ví dụ:

```http
GET /api/tasks?keyword=api&status=PLANNED&page=0&size=10&sortBy=createdAt&sortDirection=DESC
```

Capital allocation:

```http
GET /api/v1/capital-allocations?capitalCycleId=<uuid>&capitalType=TIME&status=ACTIVE&page=0&size=20
```

### Vì sao cần pagination?

- Tránh load toàn bộ bảng vào memory.
- Giảm response size và băng thông.
- Giảm thời gian query và serialization.
- Cho UI tải dữ liệu từng phần.

### API cần bảo vệ gì?

- Chuẩn hóa page index bắt đầu từ 0 hay 1.
- Giới hạn page size tối đa.
- Chỉ cho sort theo danh sách field cho phép.
- Index database theo filter/sort thường dùng.
- Dùng một pagination response thống nhất.

LifeBalance có `PageableLimits` dùng chung để normalize/giới hạn pageable ở nhiều API.

---

# Phần II — Hai use case dùng để bảo vệ

## 10. Use case 1: Tạo và hoàn thành Task

### 10.1 Tạo task

Request:

```http
POST /api/tasks
Authorization: Bearer <JWT>
Content-Type: application/json
```

```json
{
  "name": "Chuẩn bị bảo vệ API",
  "description": "Luyện phần REST và security",
  "currency": "VND",
  "priority": "HIGH",
  "deadline": "2026-09-10",
  "estimatedMinutes": 180,
  "estimatedCost": 0
}
```

Luồng xử lý:

1. Gateway và Task Service validate JWT.
2. Filter map JWT thành current user.
3. Controller lấy `ownerId` từ current user.
4. DTO validation kiểm tra format.
5. Service kiểm tra name/time/category rule.
6. Task được tạo với owner hiện tại và status `DRAFT`.
7. Repository lưu task.
8. Service ghi Task Change History.
9. Integration action được đăng ký after commit.
10. API trả task đã tạo.

### Nội dung để nói

> Điểm quan trọng là client không được quyền tự chỉ định owner. Owner lấy từ JWT. Việc này biến authentication context thành dữ liệu kiểm soát truy cập và chống tạo resource thay cho user khác.

### 10.2 Hoàn thành task

Request:

```http
PATCH /api/tasks/{id}/complete
Authorization: Bearer <JWT>
Content-Type: application/json
```

```json
{
  "reason": "Đã hoàn thành phần API và kiểm thử"
}
```

Rule:

- `reason` tối đa 500 ký tự.
- Task phải tồn tại và thuộc current user.
- Trạng thái hiện tại phải được phép chuyển sang `COMPLETED`.
- Progress được cập nhật thành 100.
- Completed time và history được ghi nhận.

Các failure case:

| Trường hợp | Kết quả mong đợi |
|---|---|
| Không có token | `401` |
| Token hợp lệ nhưng không đủ quyền | `403` |
| Task không tồn tại/không thuộc owner | `404` theo cách che giấu resource |
| Transition không hợp lệ | `400` hoặc `409` theo error contract |
| Body không đúng JSON | `400` |
| Lỗi không dự kiến | `500`, không lộ stack trace |

### Vì sao đây là API tốt để demo?

Nó thể hiện đủ:

- HTTP method và path.
- Bearer JWT.
- DTO validation.
- Ownership.
- Domain state transition.
- Transaction và history.
- Error contract.
- Cross-service side effect sau commit.

## 11. Use case 2: Capital Allocation và xác nhận vượt vốn

Đây là use case thể hiện API không chỉ CRUD mà còn bảo vệ business invariant.

### 11.1 Allocate capital

```http
POST /api/v1/capital-allocations
Authorization: Bearer <JWT>
Content-Type: application/json
```

Ví dụ request:

```json
{
  "capitalCycleId": "11111111-1111-1111-1111-111111111111",
  "capitalType": "TIME",
  "targetType": "TASK",
  "taskId": "22222222-2222-2222-2222-222222222222",
  "amount": 120,
  "allowOverAllocation": false,
  "reason": "Phân bổ thời gian cho task bảo vệ đồ án"
}
```

Validation hình thức:

- Cycle ID, capital type, target type và amount bắt buộc.
- Amount phải dương.
- Amount tối đa 15 chữ số phần nguyên và 4 chữ số thập phân.
- Reason tối đa 1000 ký tự.

Validation nghiệp vụ:

- Capital cycle thuộc owner và đang hợp lệ.
- Target tồn tại.
- Capital type phù hợp.
- Số vốn còn lại đủ hoặc có quy trình xác nhận vượt vốn.

### 11.2 Xác nhận over-allocation hai bước

Bước 1 — yêu cầu phân tích/xác nhận:

```http
POST /api/v1/capital-allocations/over-allocation-confirmation
```

Response có thể chứa:

```json
{
  "confirmationRequired": true,
  "confirmationField": "overAllocationConfirmationKey",
  "confirmationKey": "<short-lived-key>",
  "operationType": "ALLOCATE",
  "availableAmount": 60,
  "requestedAmount": 120,
  "shortageAmount": 60,
  "projectedRemainingAmount": -60,
  "remainingState": "OVER_ALLOCATED"
}
```

Bước 2 — client gửi lại allocation cùng xác nhận:

```json
{
  "capitalCycleId": "11111111-1111-1111-1111-111111111111",
  "capitalType": "TIME",
  "targetType": "TASK",
  "taskId": "22222222-2222-2222-2222-222222222222",
  "amount": 120,
  "allowOverAllocation": true,
  "overAllocationConfirmationKey": "<short-lived-key>",
  "reason": "Người dùng xác nhận vượt quỹ thời gian"
}
```

### Nội dung để nói

> Hệ thống không chỉ nhận một boolean `allowOverAllocation` rồi tin client. API có bước chuẩn bị confirmation để server tính shortage và phát confirmation key gắn với operation. Client hiển thị cảnh báo cho user rồi gửi lại key. Cách này làm consent rõ hơn và giảm việc client giả mạo một xác nhận không dựa trên phép tính hiện tại của server.

### Điểm phản biện cần chuẩn bị

- Confirmation key có hạn dùng không?
- Có ràng buộc với owner, cycle, amount và operation không?
- Có dùng một lần không?
- Nếu số vốn thay đổi giữa bước 1 và 2 thì server có tính lại không?
- Có audit lý do chấp nhận over-allocation không?

Nếu chưa kiểm chứng hết trong code, trả lời:

> Contract đã hỗ trợ confirmation key. Với production hardening, key cần được ký hoặc lưu server-side, có TTL, gắn với user và payload, đồng thời server phải tính lại available amount khi thực thi để chống race condition.

---

# Phần III — API documentation, versioning và compatibility

## 12. OpenAPI và Swagger

Shared security cho phép truy cập:

```text
/v3/api-docs/**
/swagger-ui/**
/swagger-ui.html
```

OpenAPI mô tả:

- Path và HTTP method.
- Request parameters/body.
- Response schema.
- Status code.
- Security scheme.
- Operation summary và description.

### Hiện trạng cần nói đúng

- Nhiều controller, đặc biệt Resource Capital, có `@Operation` và `@Tag`.
- Repository root có `openapi.yaml` và `openapi-advanced-capital.yaml` nhưng chúng chỉ bao phủ một phần API.
- API surface trong code lớn hơn đáng kể so với hai central spec này.

### Hướng cải tiến

- Chọn một source of truth: generated spec từ code hoặc design-first spec.
- Aggregate spec từ tất cả service tại API portal/gateway.
- Kiểm tra spec trong CI.
- So sánh breaking changes trước release.
- Sinh client SDK nếu frontend cần.

### Swagger không thay thế test

Swagger giúp khám phá và thử request thủ công. Nó không thay:

- Unit test.
- Controller test.
- Security test.
- Contract test.
- End-to-end test.

## 13. API versioning

Hiện path có nhiều kiểu:

```text
/api/tasks
/api/timeline
/api/v1/capital
/api/v1/identity
/users và /api/users
```

### Cách trình bày trung thực

> Resource Capital và một số Identity endpoint đã dùng `/v1`, trong khi các domain khác chưa version hóa bằng path. Một số Identity API còn duy trì cả alias có và không có `/api`. Đây là dấu hiệu quá trình chuyển đổi và là điểm cần chuẩn hóa.

### Đề xuất convention

Một phương án dễ hiểu:

```text
/api/v1/tasks
/api/v1/timeline
/api/v1/finance
/api/v1/notifications
```

Nguyên tắc:

- Không tạo version mới cho mọi thay đổi nhỏ.
- Thêm field optional thường tương thích ngược.
- Xóa/đổi tên field, đổi kiểu hoặc semantics là breaking change.
- Có deprecation period cho version cũ.
- Không duy trì alias vô thời hạn nếu không có lý do tương thích.

---

# Phần IV — Chất lượng API

## 14. Kiểm thử API

### 14.1 Test cần có

| Loại test | Kiểm tra |
|---|---|
| DTO validation test | Required field, length, range, format |
| Controller/MockMvc test | Mapping, request parsing, status và response |
| Security integration test | Public/protected, token, 401/403, permission |
| Service test | Business rule và state transition |
| Repository test | Owner scoping và query/filter |
| Contract test | Consumer/provider không lệch schema |
| End-to-end test | Client → Gateway → service → database |
| Resilience test | Timeout, retry, downstream unavailable |

### 14.2 Test matrix cho `complete task`

1. Hoàn thành task hợp lệ.
2. Body không có reason vẫn hợp lệ nếu optional.
3. Reason trên 500 ký tự trả `400`.
4. Không token trả `401`.
5. Token sai audience trả `401`.
6. Task của user khác không truy cập được.
7. Task không tồn tại trả `404`.
8. Transition không hợp lệ bị từ chối.
9. Progress trở thành 100.
10. History được ghi.
11. Transaction rollback thì integration không chạy.
12. Downstream lỗi không rollback task đã commit.

### 14.3 Contract test quan trọng thế nào?

Trong microservice, compile vẫn thành công dù provider đã đổi JSON mà consumer không biết. Contract test phát hiện lệch giữa:

- Field name/type.
- Required/optional.
- Enum value.
- Status code.
- Error response.

## 15. Non-functional API concerns

Khi bảo vệ, API không chỉ là CRUD. Cần nhắc tới:

### Performance

- Pagination và database index.
- Giới hạn response size.
- Tránh N+1 query.
- Cache dữ liệu phù hợp.
- Theo dõi p95/p99 latency.

### Reliability

- Timeout cho downstream.
- Retry chỉ với lỗi tạm thời.
- Idempotency cho POST/command.
- Circuit breaker nếu dependency lỗi kéo dài.

### Security

- HTTPS ở production.
- Bearer JWT, issuer, audience, expiry.
- Permission và ownership.
- Không log token/password.
- Rate limiting cho login và API nhạy cảm.
- Không trả stack trace cho client.

### Observability

- Request ID/correlation ID.
- Access log và security audit.
- Metrics theo endpoint/status.
- Distributed tracing cho request xuyên service.

### Compatibility

- Versioning và deprecation.
- OpenAPI contract.
- Không thay đổi enum/field/status tùy tiện.

---

# Phần V — Kịch bản trình bày

## 16. Kịch bản 10–12 phút theo slide

### Slide 1 — Giới thiệu API của LifeBalance

**Trên slide:**

- HTTP REST + JSON.
- Hợp đồng Frontend ↔ Backend và Service ↔ Service.
- 8 business domain.

**Nội dung nói:**

> API là lớp giao tiếp chính của LifeBalance. Frontend gửi HTTP request dưới dạng JSON và nhận response/status code. API không chỉ cung cấp CRUD mà còn biểu diễn các use case như lập kế hoạch task, hoàn thành task, phân bổ vốn, đánh giá và gửi thông báo.

### Slide 2 — API landscape

**Trên slide:**

- Identity, Task, Timeline, Capital.
- Finance, Notification, Analytics, AI.
- Hơn 200 handler methods.

**Nội dung nói:**

> API được chia theo domain. Cách chia này giúp mỗi service sở hữu contract và business data riêng. Em tập trung minh họa Task API và Capital API vì hai nhóm thể hiện rõ REST, security, validation và business rule.

### Slide 3 — Request flow

**Trên slide:**

```text
Client → Gateway → Service → Database
```

**Nội dung nói:**

> Client gọi Gateway. Gateway route theo path và tìm service qua Eureka. JWT được validate tại Gateway và validate lại ở service. Sau đó Controller parse request, Service xử lý nghiệp vụ, Repository lưu dữ liệu và response được trả về dạng JSON.

### Slide 4 — REST convention

**Trên slide:**

```text
GET    /api/tasks/{id}
POST   /api/tasks
PUT    /api/tasks/{id}
PATCH  /api/tasks/{id}/complete
DELETE /api/tasks/{id}
```

**Nội dung nói:**

> Resource API dùng danh từ. GET đọc, POST tạo, PUT cập nhật, PATCH thay đổi một phần hoặc transition và DELETE xóa. Các lifecycle action dùng command endpoint vì backend cần kiểm tra state machine, không cho client sửa status tùy ý.

### Slide 5 — Request và validation

**Trên slide:**

- Path, query, header, JSON body.
- DTO + Bean Validation.
- Business validation ở service/domain.

**Nội dung nói:**

> Request được tách thành DTO. Validation annotation kiểm tra format, range và length. Những rule cần trạng thái hoặc database được kiểm tra ở service và policy. Việc tách hai lớp giúp lỗi đầu vào rõ và invariant nghiệp vụ không bị bypass.

### Slide 6 — API security

**Trên slide:**

- Keycloak JWT.
- Issuer + audience + expiry.
- RBAC + ownership.
- 401 khác 403.

**Nội dung nói:**

> Owner không lấy từ body mà lấy từ token. API quản trị dùng permission; API dữ liệu cá nhân query theo resource ID và owner ID. Nhờ đó người dùng không thể truy cập dữ liệu khác chỉ bằng cách đổi UUID.

### Slide 7 — Response và lỗi

**Trên slide:**

```text
success, data, error, timestamp
```

**Nội dung nói:**

> Common module chuẩn hóa error code, message và details. Validation trả 400, authentication 401, authorization 403, not found 404 và lỗi không dự kiến trả 500 nhưng không lộ stack trace. Response success hiện chưa được áp dụng hoàn toàn đồng nhất ở tất cả controller; đây là điểm em đề xuất chuẩn hóa.

### Slide 8 — Demo Task Lifecycle

**Trên slide:**

```text
POST /api/tasks
PATCH /api/tasks/{id}/complete
```

**Nội dung nói:**

> Khi complete, backend kiểm tra owner và transition, cập nhật progress 100, ghi completed time và history. Đây là ví dụ cho thấy API command ánh xạ vào domain rule, chứ không chỉ sửa một cột database.

### Slide 9 — Demo Capital confirmation

**Trên slide:**

```text
Prepare confirmation → User confirms → Allocate
```

**Nội dung nói:**

> Khi amount vượt vốn còn lại, API trả thông tin shortage và confirmation key. Người dùng xác nhận có chủ đích rồi gửi lại key. Server vẫn phải kiểm tra lại dữ liệu để chống race condition. Đây là thiết kế API hai bước cho một hành động rủi ro.

### Slide 10 — Documentation và testing

**Trên slide:**

- OpenAPI/Swagger.
- Controller/security/contract/E2E tests.
- Pagination và compatibility.

**Nội dung nói:**

> Swagger mô tả contract nhưng không thay test. Dự án có controller và security tests; hướng hoàn thiện là aggregate OpenAPI toàn hệ thống, contract tests giữa service và kiểm tra breaking changes trong CI.

### Slide 11 — Đánh giá

**Trên slide:**

**Điểm mạnh:** domain API, JWT/ownership, validation, error model.

**Cải tiến:** versioning, response envelope, idempotency, tracing.

**Nội dung nói:**

> Điểm mạnh là API bám business domain và có nhiều lớp security/validation. Technical debt chính là versioning và success response chưa đồng nhất, central OpenAPI chưa bao phủ toàn bộ, và POST retry cần idempotency. Đây là các ưu tiên em lựa chọn nếu tiếp tục hardening production.

### Slide 12 — Kết luận

**Nội dung nói:**

> Em đánh giá một API tốt theo năm tiêu chí: contract rõ, semantics đúng, dữ liệu hợp lệ, truy cập an toàn và thay đổi có kiểm soát. LifeBalance đã có nền tảng các tiêu chí này; phần tiếp theo là chuẩn hóa contract và tăng độ tin cậy vận hành.

## 17. Bài nói 3 phút

> API của LifeBalance được thiết kế theo HTTP REST và chia theo tám business domain: Identity, Task, Timeline, Resource Capital, Finance, Notification, Analytics và AI. Frontend gọi một API Gateway; Gateway route request tới service tương ứng. Dữ liệu chủ yếu truyền dưới dạng JSON.
>
> URL được tổ chức theo resource, ví dụ `/api/tasks` và `/api/tasks/{id}`. Hệ thống dùng GET để đọc, POST để tạo, PUT để cập nhật, DELETE để xóa và PATCH cho các thay đổi một phần hoặc business transition như complete, pause và resume.
>
> Request được biểu diễn bằng DTO. Bean Validation kiểm tra required field, độ dài, định dạng và giới hạn số. Các rule cần dữ liệu hệ thống, ví dụ owner, state transition hoặc số vốn còn lại, được kiểm tra ở service và domain policy.
>
> API được bảo vệ bằng JWT do Keycloak phát. Gateway và service đều validate token, gồm chữ ký, thời hạn, issuer và audience. Backend lấy owner ID từ token thay vì tin dữ liệu client gửi. API quản trị kiểm tra permission; API cá nhân kiểm tra ownership.
>
> Use case tiêu biểu là hoàn thành task. Client gọi `PATCH /api/tasks/{id}/complete`; backend kiểm tra owner và transition, đặt progress 100, lưu completed time và history. Một use case khác là capital allocation: nếu phân bổ vượt vốn còn lại, API có bước tạo confirmation key để user xác nhận có chủ đích.
>
> Lỗi được chuẩn hóa thành error code, message và details, với 400 cho validation, 401 cho authentication, 403 cho authorization, 404 cho resource không tồn tại và 500 cho lỗi không dự kiến. API danh sách có pagination, filtering và sorting.
>
> Điểm mạnh của API là bám business domain, có validation và security nhiều lớp. Điểm cần cải tiến là thống nhất versioning, success response, OpenAPI toàn hệ thống, idempotency và distributed tracing.

---

# Phần VI — Kịch bản demo API

## 18. Chuẩn bị trước demo

- Gateway, Eureka, Keycloak, PostgreSQL và service cần demo đều healthy.
- Có user demo và access token hợp lệ.
- Chuẩn bị sẵn token trong Postman/environment variable, không chiếu secret/password.
- Reset hoặc biết trước dữ liệu demo.
- Mở sẵn Swagger/Postman và log cần thiết.
- Có ảnh/video dự phòng nếu môi trường lỗi.

Không dán full token lên slide hoặc commit token vào repository.

## 19. Demo Task API trong 5 bước

### Bước 1 — Không gửi token

```http
GET /api/tasks
```

Kỳ vọng: `401`.

Điểm nói:

> Endpoint mặc định được bảo vệ. Request chưa có authentication context bị chặn trước khi vào business use case.

### Bước 2 — Tạo task hợp lệ

```http
POST /api/tasks
Authorization: Bearer <JWT>
Content-Type: application/json
```

```json
{
  "name": "API Defense Demo",
  "priority": "HIGH",
  "deadline": "2026-09-10",
  "estimatedMinutes": 90,
  "estimatedCost": 0
}
```

Lưu lại `id` trong response.

Điểm nói:

> Owner được lấy từ token, không có trong request body.

### Bước 3 — Validation failure

```json
{
  "name": "",
  "estimatedMinutes": -10
}
```

Kỳ vọng: `400`, details chỉ rõ field.

Điểm nói:

> API trả lỗi có cấu trúc để frontend hiển thị chính xác trên form.

### Bước 4 — Hoàn thành task

```http
PATCH /api/tasks/<TASK_ID>/complete
Authorization: Bearer <JWT>
Content-Type: application/json
```

```json
{
  "reason": "Hoàn thành demo"
}
```

Kỳ vọng: status `COMPLETED`, progress `100`.

### Bước 5 — Thử transition không hợp lệ

Gọi complete lần nữa hoặc một transition không được policy cho phép.

Kỳ vọng: business error, không làm hỏng dữ liệu.

Điểm nói:

> API không cho client chuyển state tùy ý; domain policy giữ invariant.

## 20. Thứ tự demo nên tránh

- Không demo hàng chục endpoint.
- Không giải thích code line-by-line trên màn hình.
- Không dùng dữ liệu thật hoặc credential thật.
- Không phụ thuộc hoàn toàn vào internet.
- Không chỉnh database trực tiếp để “làm cho API chạy”.
- Không bỏ qua failure case; một lỗi validation/401 thường chứng minh thiết kế tốt hơn thêm một CRUD endpoint.

---

# Phần VII — Đánh giá API

## 21. Điểm mạnh

1. API được chia theo business domain.
2. Resource naming tương đối rõ ở phần lớn service.
3. Lifecycle command thể hiện business intent.
4. DTO tách API contract khỏi JPA entity.
5. Có Bean Validation và business validation.
6. Gateway và service cùng validate JWT.
7. Có audience validation.
8. Identity API dùng permission chi tiết.
9. API cá nhân có owner scoping.
10. Error model và global exception handler dùng chung.
11. Có pagination limits.
12. Có OpenAPI/Swagger và controller tests.
13. Capital API có quy trình confirmation cho hành động rủi ro.

## 22. Điểm chưa hoàn thiện và hướng xử lý

| Hiện trạng | Vấn đề | Đề xuất |
|---|---|---|
| `/api/...` và `/api/v1/...` cùng tồn tại | Versioning không thống nhất | Chọn convention, migration và deprecation plan |
| Một số Identity endpoint có alias `/users` và `/api/users` | Contract trùng, khó duy trì | Giữ alias tạm thời rồi deprecate |
| Success response chưa thống nhất | Frontend phải xử lý nhiều dạng | Chuẩn hóa envelope/pagination |
| Root OpenAPI chỉ bao phủ một phần | Khó có API catalog đầy đủ | Aggregate/generated spec trong CI |
| POST/internal call có retry | Có nguy cơ duplicate | Idempotency key và deduplication |
| Chưa thấy rate limiting rõ ở Gateway | Dễ abuse endpoint | Rate limit theo user/IP/client |
| Correlation/tracing chưa đầy đủ | Khó debug xuyên service | OpenTelemetry + propagation |
| Internal API dùng shared secret | Rotation và blast radius | OAuth2 client credentials/mTLS |
| Nhiều enum trong contract | Thêm/đổi enum có thể break client | Document unknown-value strategy/versioning |
| Offset/page pagination | Page sâu có thể chậm | Cursor pagination cho stream lớn nếu cần |

### Cách trả lời khi giảng viên chỉ ra lỗi

Không nên nói “cái đó không ảnh hưởng”. Hãy trả lời:

> Đúng, đây là điểm chưa đồng nhất của phiên bản hiện tại. Nguyên nhân là các module phát triển theo giai đoạn. Em đánh giá tác động là frontend và tài liệu phải xử lý nhiều contract. Hướng sửa của em là chuẩn hóa ở version tiếp theo, giữ backward compatibility và dùng contract test để kiểm soát migration.

---

# Phần VIII — Câu hỏi vấn đáp

## 23. Kiến thức REST và HTTP

### Câu 1. API là gì?

API là hợp đồng cho phép hai thành phần phần mềm giao tiếp. Với HTTP API, hợp đồng gồm URL, method, header, request body, response body, status code, security và error semantics.

### Câu 2. REST là gì?

REST là phong cách kiến trúc tổ chức hệ thống quanh resource, representation, stateless interaction và semantics chuẩn của HTTP. REST không phải một giao thức.

### Câu 3. REST có bắt buộc dùng JSON không?

Không. REST có thể dùng JSON, XML hoặc định dạng khác. LifeBalance chủ yếu dùng JSON vì dễ dùng với frontend và hệ sinh thái Spring.

### Câu 4. Endpoint là gì?

Là tổ hợp URL và HTTP method đại diện cho một operation, ví dụ `GET /api/tasks/{id}` khác `DELETE /api/tasks/{id}`.

### Câu 5. GET có được làm thay đổi dữ liệu không?

Không nên. GET phải safe và không tạo side effect nghiệp vụ quan sát được. Điều này quan trọng cho cache, retry, crawler và proxy.

### Câu 6. PUT và PATCH khác nhau thế nào?

PUT thường gửi representation để thay thế/cập nhật resource và có tính idempotent. PATCH thay đổi một phần hoặc thực hiện transition; idempotency phụ thuộc contract.

### Câu 7. POST có idempotent không?

Không mặc định. Gửi cùng POST hai lần có thể tạo hai resource. Muốn retry an toàn cần idempotency key, business unique key hoặc deduplication.

### Câu 8. Vì sao dùng `PATCH /tasks/{id}/complete` thay vì client gửi status?

Vì complete là business command có precondition và side effect: kiểm tra transition, owner, cập nhật progress/time, history và integration. Không nên cho client bypass rule bằng cách sửa status trực tiếp.

### Câu 9. URI nên dùng danh từ hay động từ?

Resource URI thường dùng danh từ. Với domain command quan trọng, action subresource như `/complete` là một thiết kế pragmatic, miễn semantics và status code rõ ràng.

### Câu 10. HTTP status code khác error code ứng dụng thế nào?

HTTP status mô tả nhóm kết quả chung cho client/proxy. Application error code mô tả lỗi nghiệp vụ cụ thể, ví dụ `VALIDATION_FAILED` hoặc `TASK_NOT_FOUND`.

## 24. Request, response và validation

### Câu 11. Vì sao không nhận JPA entity trực tiếp?

Để tránh mass assignment, không lộ persistence model, tránh serialization issue và cho contract thay đổi độc lập hơn database.

### Câu 12. Path parameter và query parameter khác nhau thế nào?

Path xác định resource/quan hệ chính. Query thường dùng filter, pagination, sorting hoặc option không thay đổi identity của resource.

### Câu 13. Validation nên đặt ở đâu?

Format/range/required ở request DTO; rule nghiệp vụ ở service/domain/policy; database constraint làm lớp bảo vệ cuối cho uniqueness/integrity.

### Câu 14. `400` và `409` khác nhau thế nào?

400 là request không hợp lệ về format/validation chung. 409 là request hợp lệ về cú pháp nhưng xung đột trạng thái hiện tại hoặc resource, ví dụ duplicate business key hoặc invalid state conflict. Contract phải thống nhất.

### Câu 15. Có nên luôn trả `200` rồi đặt lỗi trong body không?

Không. Cần dùng HTTP status đúng để client, gateway và monitoring hiểu kết quả. Body bổ sung error code chi tiết.

### Câu 16. Khi tạo resource nên trả gì?

Thường `201 Created`, representation đã tạo và có thể có `Location` header. Capital allocation hiện trả 201; cần chuẩn hóa behavior tương tự cho create endpoint khác nếu contract yêu cầu.

### Câu 17. Xóa thành công trả `200` hay `204`?

Cả hai có thể hợp lệ. Nếu không có body thì 204 phù hợp. Quan trọng là contract nhất quán.

### Câu 18. Vì sao cần response envelope?

Để thống nhất data, error và metadata. Nhưng envelope cũng làm response dài hơn; nếu dùng thì phải áp dụng nhất quán.

### Câu 19. Có nên trả stack trace cho client không?

Không. Stack trace có thể lộ implementation và dữ liệu nhạy cảm. Server log chi tiết nội bộ, client chỉ nhận error code/message an toàn.

### Câu 20. Pagination page/size có hạn chế gì?

Page sâu có thể query chậm và dữ liệu thay đổi giữa các trang. Với feed lớn hoặc real-time có thể dùng cursor pagination.

## 25. Security

### Câu 21. Authentication và authorization khác nhau thế nào?

Authentication xác định ai đang gọi. Authorization xác định người đó được làm gì trên resource nào.

### Câu 22. JWT có được mã hóa không?

Thông thường payload chỉ base64url encode, không bí mật. Signature chống sửa token. Không đặt password hoặc secret trong claim.

### Câu 23. Hệ thống kiểm tra JWT những gì?

Chữ ký, expiry, issuer và audience. Sau đó map claim thành internal principal.

### Câu 24. Vì sao audience quan trọng?

Để token phát cho resource/client khác không được sử dụng với LifeBalance API dù cùng Keycloak issuer.

### Câu 25. Tại sao Gateway validate rồi service vẫn validate?

Defense in depth và giữ trust boundary. Service vẫn an toàn khi bị gọi trực tiếp.

### Câu 26. 401 và 403 khác nhau thế nào?

401 là chưa xác thực được. 403 là đã xác thực nhưng không đủ quyền hoặc owner không khớp.

### Câu 27. IDOR là gì và dự án chống thế nào?

IDOR là truy cập resource người khác bằng cách thay ID. Dự án lấy current user từ JWT và query theo cả resource ID và owner ID.

### Câu 28. Client có được gửi owner ID không?

Payload có thể chứa owner ở một số internal contract, nhưng server không được tin nó độc lập. Public API nên lấy owner từ token; internal API phải đối chiếu payload owner với authenticated owner.

### Câu 29. CORS có phải authentication không?

Không. CORS là browser policy kiểm soát origin được phép gọi. Attacker không dùng browser vẫn có thể gửi HTTP request, nên API vẫn cần authentication/authorization.

### Câu 30. HTTPS có còn cần khi đã dùng JWT không?

Có. Nếu không có TLS, token và dữ liệu có thể bị nghe lén. JWT không tự mã hóa đường truyền.

## 26. Documentation, testing và compatibility

### Câu 31. Swagger là gì?

Swagger UI là giao diện đọc/thử OpenAPI contract. Nó không phải bản thân API và không thay automated tests.

### Câu 32. Code-first và design-first khác nhau thế nào?

Code-first sinh spec từ controller/annotation. Design-first viết OpenAPI trước rồi implement. Cả hai đều được nếu có một source of truth và CI kiểm tra đồng bộ.

### Câu 33. API versioning để làm gì?

Quản lý breaking changes và cho client thời gian migration. Không nên tăng version cho mọi thay đổi tương thích.

### Câu 34. Thêm field response có breaking không?

Thường không nếu client bỏ qua field lạ. Nhưng có thể break client deserialize quá chặt. Cần contract rule và test với consumer.

### Câu 35. Thêm enum value có breaking không?

Có thể. Client dùng exhaustive switch có thể lỗi. Cần document unknown-value strategy hoặc version contract.

### Câu 36. Contract test giải quyết gì?

Phát hiện consumer và provider không thống nhất request/response trước khi deploy.

### Câu 37. Vì sao root OpenAPI không đủ?

Hai spec ở root chỉ mô tả một phần Task/Capital trong khi code có nhiều service/controller hơn. Cần aggregate hoặc generate spec cho toàn bộ service.

### Câu 38. Test API quan trọng nhất là gì?

Không có một test duy nhất. Tối thiểu cần happy path, validation, authentication, authorization/ownership, not found, conflict, response contract và transaction side effects.

## 27. Phản biện thiết kế LifeBalance

### Câu 39. API hiện tại có hoàn toàn RESTful không?

Nó là pragmatic REST: resource CRUD kết hợp action endpoints cho domain command. Thiết kế ưu tiên business semantics rõ hơn sự thuần REST tuyệt đối.

### Câu 40. Điểm chưa nhất quán lớn nhất là gì?

Versioned/unversioned path và success response envelope chưa thống nhất. Central OpenAPI cũng chưa bao phủ toàn bộ API surface.

### Câu 41. Nếu được sửa một việc đầu tiên, bạn sửa gì?

Chuẩn hóa contract: naming/versioning, response/error/pagination envelope và aggregate OpenAPI. Sau đó thêm contract test để tránh tái phát.

### Câu 42. API có chịu được retry không?

GET/PUT/DELETE thường dễ idempotent hơn. POST/command cần đánh giá riêng. Cross-service POST có retry nên cần idempotency key/deduplication để tránh duplicate.

### Câu 43. Rate limiting nên đặt ở đâu?

Policy tổng thể có thể đặt ở Gateway theo IP/user/client. Endpoint nhạy cảm như password/login vẫn cần rule chuyên biệt ở Identity/Keycloak.

### Câu 44. Vì sao Capital confirmation cần hai bước?

Để server cung cấp thông tin rủi ro và user xác nhận có chủ đích trước hành động vượt giới hạn. Key phải gắn với payload/user/TTL và server phải revalidate khi execute.

### Câu 45. Nếu available capital đổi sau khi phát key thì sao?

Server không được tin kết quả cũ; phải lock/version check hoặc tính lại trong transaction. Nếu điều kiện thay đổi, yêu cầu confirmation mới hoặc từ chối conflict.

### Câu 46. API nội bộ và API công khai khác nhau thế nào?

Public API phục vụ client bên ngoài qua Gateway. Internal API phục vụ service-to-service, có thể yêu cầu Bearer context và internal credential. Cả hai vẫn cần contract, authorization, validation và observability.

### Câu 47. Tại sao không cho frontend gọi trực tiếp từng service?

Gateway che topology, thống nhất entry point và policy biên. Nếu frontend biết mọi service URL, coupling deployment và security exposure tăng.

### Câu 48. API và architecture liên quan thế nào?

API là boundary/contract; architecture quyết định service nào sở hữu contract, request được route ra sao, dữ liệu nằm đâu và failure được xử lý thế nào.

### Câu 49. Làm sao đo API tốt hay không?

Đo correctness/error rate, latency p95/p99, availability, contract stability, security incidents, client integration effort và mức dễ quan sát/debug.

### Câu 50. Kết luận ngắn về API LifeBalance?

API đã bám domain, có JWT/permission/ownership, DTO validation và error model. Ưu tiên tiếp theo là thống nhất contract/versioning, idempotency, API catalog và tracing.

---

# Phần IX — Những điều không được trả lời sai

## 28. Câu sai và cách nói đúng

### Sai: “REST là một protocol.”

Đúng: HTTP là protocol; REST là architectural style.

### Sai: “REST bắt buộc dùng JSON.”

Đúng: JSON là representation được dự án lựa chọn.

### Sai: “Mọi API đều đi qua Gateway.”

Đúng: Client API đi qua Gateway; internal REST hiện dùng base URL trực tiếp.

### Sai: “Gateway validate rồi service không cần validate.”

Đúng: Service validate lại để chống bypass và giữ defense in depth.

### Sai: “JWT đã mã hóa thông tin.”

Đúng: JWT payload thường chỉ encode; HTTPS mới bảo vệ đường truyền.

### Sai: “401 là không đủ quyền.”

Đúng: 401 là chưa xác thực; 403 là không đủ quyền.

### Sai: “Client gửi owner ID nên backend biết chủ sở hữu.”

Đúng: Owner phải lấy từ authenticated principal hoặc đối chiếu chặt với principal.

### Sai: “Validation annotation kiểm tra toàn bộ nghiệp vụ.”

Đúng: Annotation kiểm tra format/range; business rule nằm ở service/domain/policy.

### Sai: “PATCH luôn không idempotent.”

Đúng: Idempotency phụ thuộc semantics của operation.

### Sai: “Swagger chứng minh API chạy đúng.”

Đúng: Swagger mô tả/thử contract; correctness cần automated tests.

### Sai: “Toàn bộ response đã thống nhất ApiResponse.”

Đúng: Common envelope đã có nhưng một số Task API vẫn trả DTO/Page trực tiếp.

### Sai: “Tất cả API đã version `/v1`.”

Đúng: Capital và một số Identity API có `/v1`; các domain khác chưa đồng nhất.

### Sai: “Retry POST luôn an toàn.”

Đúng: Retry POST có thể duplicate nếu không có idempotency/deduplication.

### Sai: “Over-allocation key nghĩa là không cần kiểm tra lại vốn.”

Đúng: Server vẫn phải revalidate điều kiện khi thực thi để chống race condition.

---

# Phần X — Checklist trước khi bảo vệ

## 29. Kiến thức phải trả lời được

- [ ] API khác architecture như thế nào?
- [ ] HTTP khác REST như thế nào?
- [ ] GET/POST/PUT/PATCH/DELETE dùng khi nào?
- [ ] Vì sao Task dùng lifecycle action endpoint?
- [ ] DTO khác entity như thế nào?
- [ ] Validation hình thức khác business validation thế nào?
- [ ] JWT được kiểm tra gì?
- [ ] Authentication, permission và ownership khác nhau thế nào?
- [ ] 400/401/403/404/409 khác nhau thế nào?
- [ ] Pagination dùng để làm gì?
- [ ] OpenAPI/Swagger dùng để làm gì?
- [ ] API hiện tại chưa thống nhất ở đâu?
- [ ] Idempotency giải quyết vấn đề gì?
- [ ] Capital over-allocation confirmation hoạt động ra sao?

## 30. Checklist demo

- [ ] Service healthy.
- [ ] Token demo hợp lệ nhưng không bị lộ.
- [ ] Request collection đã lưu.
- [ ] Có task/cycle demo phù hợp.
- [ ] Đã thử lại toàn bộ demo trước buổi bảo vệ.
- [ ] Có ảnh/video dự phòng.
- [ ] Demo ít nhất một success case.
- [ ] Demo ít nhất một validation/security failure.
- [ ] Không mở file chứa secret trên màn hình.
- [ ] Có câu kết luận sau demo.

## 31. Câu kết luận nên học thuộc

> Em không đánh giá API chỉ bằng số lượng endpoint. Em đánh giá theo năm yếu tố: contract có rõ không, HTTP semantics có đúng không, dữ liệu có được validation không, resource có được bảo vệ bằng authentication/authorization/ownership không, và contract có thể phát triển mà không làm hỏng client không. LifeBalance đã có nền tảng tốt về domain API, JWT, validation và error handling. Phần em ưu tiên hoàn thiện là versioning, response consistency, idempotency, API catalog và distributed tracing.

---

# Phần XI — Bản đồ mã nguồn để chứng minh

## 32. File nên mở nếu giảng viên yêu cầu

- Gateway routes: `lifebalance-backend/gateway/src/main/resources/application.yaml`.
- Shared security: `lifebalance-backend/lifebalance-security/src/main/java/com/lifebalance/security/keycloak/LifebalanceSecurityAutoConfiguration.java`.
- JWT user mapping: `lifebalance-backend/lifebalance-security/src/main/java/com/lifebalance/security/keycloak/KeycloakUserMapper.java`.
- Current-user filter: `lifebalance-backend/lifebalance-security/src/main/java/com/lifebalance/security/keycloak/KeycloakUserMappingFilter.java`.
- Common response: `lifebalance-backend/lifebalance-common/src/main/java/com/lifebalance/common/api/ApiResponse.java`.
- Common error: `lifebalance-backend/lifebalance-common/src/main/java/com/lifebalance/common/api/ApiError.java`.
- Global exception handling: `lifebalance-backend/lifebalance-common/src/main/java/com/lifebalance/common/error/GlobalExceptionHandler.java`.
- Task API: `lifebalance-backend/task-service/src/main/java/com/lifebalance/task/controller/TaskController.java`.
- Create Task DTO: `lifebalance-backend/task-service/src/main/java/com/lifebalance/task/dto/request/CreateTaskRequest.java`.
- Task lifecycle body: `lifebalance-backend/task-service/src/main/java/com/lifebalance/task/dto/request/TaskLifecycleActionRequest.java`.
- Task use case: `lifebalance-backend/task-service/src/main/java/com/lifebalance/task/service/impl/TaskServiceImpl.java`.
- Task lifecycle rule: `lifebalance-backend/task-service/src/main/java/com/lifebalance/task/validation/TaskLifecyclePolicy.java`.
- Capital Allocation API: `lifebalance-backend/resource-capital-service/src/main/java/com/lifebalance/resourcecapital/controller/CapitalAllocationController.java`.
- Capital Allocation request: `lifebalance-backend/resource-capital-service/src/main/java/com/lifebalance/resourcecapital/dto/CreateCapitalAllocationRequest.java`.
- Over-allocation response: `lifebalance-backend/resource-capital-service/src/main/java/com/lifebalance/resourcecapital/dto/OverAllocationConfirmationResponse.java`.
- Partial central specifications: `openapi.yaml` và `openapi-advanced-capital.yaml`.

## 33. Tóm tắt cuối cùng

Khi bảo vệ API LifeBalance, không nên chỉ đọc danh sách endpoint. Hãy chứng minh rằng bạn hiểu:

1. API giải quyết use case nào.
2. Vì sao chọn URL và HTTP method đó.
3. Request được validation thế nào.
4. Current user và quyền được xác định thế nào.
5. Business invariant được bảo vệ ở đâu.
6. Response/status/error contract ra sao.
7. API được document và test thế nào.
8. Điểm chưa hoàn thiện và kế hoạch cải tiến.

Nếu trình bày được hai luồng Task Lifecycle và Capital Over-allocation theo tám câu hỏi trên, bạn đã thể hiện rằng mình hiểu API ở mức thiết kế và vận hành, không chỉ biết gọi endpoint.

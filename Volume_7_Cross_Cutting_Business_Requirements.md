# LifeBalance
# Volume 7 - Cross-cutting Business Requirements

## 1. Document Overview

### 1.1 Purpose

Volume 7 - Cross-cutting Business Requirements là tài liệu chuẩn hóa các yêu cầu nghiệp vụ dùng chung cho toàn bộ hệ thống LifeBalance. Tài liệu này không phải là tài liệu đặc tả cho một module riêng lẻ. Đây là bộ tiêu chuẩn nghiệp vụ áp dụng xuyên suốt các module đã được định nghĩa trong các volume trước, bao gồm Identity & Authorization, Resource Capital Management, Task & Timeline Management, Tracking, Evaluation & Reporting, Administration & Support và các capability liên quan.

Mục đích chính của tài liệu là bảo đảm các module của LifeBalance vận hành theo cùng một hệ nguyên tắc, cùng một chuẩn xác thực nghiệp vụ, cùng một cách xử lý quyền truy cập, cùng một cách diễn giải thuật ngữ, cùng một cách ghi nhận lịch sử và cùng một cách đánh giá rủi ro. Khi các tài liệu thiết kế, kiểm thử hoặc triển khai được xây dựng ở các giai đoạn sau, các bên liên quan cần sử dụng Volume 7 như chuẩn nghiệp vụ chung để tránh mâu thuẫn giữa các module.

Tài liệu này chỉ phân tích ở góc độ Business Analysis. Nội dung không đi vào thiết kế hoặc triển khai kỹ thuật.

### 1.2 Scope

Phạm vi của tài liệu bao gồm:

- Global Business Rules áp dụng cho toàn hệ thống.
- Validation Rules được chuẩn hóa theo loại dữ liệu nghiệp vụ.
- Business Policies dùng chung.
- Permission Matrix ở mức nghiệp vụ.
- Business Constraints và Assumptions.
- Non-functional Requirements ở mức nghiệp vụ.
- Error Handling Principles.
- Audit và Activity History Requirements.
- Notification Requirements nếu được phê duyệt.
- Search, Filter, Sort và Pagination Standards.
- Export và Import Requirements ở mức nghiệp vụ.
- Business Glossary chung.
- Traceability Matrix cấp nghiệp vụ.
- Risks, Open Questions và Suggested Improvements cấp toàn hệ thống.

### 1.3 Objectives

| Objective ID | Objective | Description |
|---|---|---|
| CBR-OBJ-001 | Chuẩn hóa yêu cầu dùng chung | Thiết lập các rule và policy áp dụng cho mọi module. |
| CBR-OBJ-002 | Bảo đảm nhất quán nghiệp vụ | Tránh mâu thuẫn giữa các module trong ownership, permission, status, validation và history. |
| CBR-OBJ-003 | Hỗ trợ truy vết yêu cầu | Liên kết business goals với requirements, use cases, user stories và acceptance criteria. |
| CBR-OBJ-004 | Hỗ trợ kiểm thử và thiết kế sau này | Cung cấp chuẩn nghiệp vụ để làm đầu vào cho test case, UI/UX, thiết kế dữ liệu và thiết kế dịch vụ ở giai đoạn sau. |
| CBR-OBJ-005 | Tăng governance | Xác định nguyên tắc audit, activity history, policy, permission và configuration management. |
| CBR-OBJ-006 | Bảo vệ quyền riêng tư cá nhân | Chuẩn hóa nguyên tắc User chỉ thao tác trên dữ liệu thuộc quyền của mình. |

### 1.4 Business Value

Tài liệu này tạo giá trị bằng cách giảm rủi ro không nhất quán trong toàn bộ bộ yêu cầu LifeBalance. Nếu mỗi module tự diễn giải riêng về quyền truy cập, trạng thái, audit, lịch sử, validation hoặc báo cáo, hệ thống sẽ khó kiểm thử, khó vận hành và dễ phát sinh tranh chấp nghiệp vụ. Volume 7 đóng vai trò chuẩn hóa những vấn đề đó.

Giá trị thứ nhất là tạo ra một nguồn tham chiếu chung. Các business analyst, product owner, developer, tester, UI/UX designer và stakeholder có thể sử dụng tài liệu này để hiểu các nguyên tắc dùng chung.

Giá trị thứ hai là tăng khả năng quản trị yêu cầu. Các thay đổi trong một module có thể được đánh giá tác động dựa trên Global Business Rules và Business Policies.

Giá trị thứ ba là bảo vệ tính nhất quán của triết lý sản phẩm. LifeBalance xem mọi công việc là khoản đầu tư nguồn lực, do đó các quy tắc về Planned, Actual, Allocation, Evaluation và History phải nhất quán trên toàn hệ thống.

Giá trị thứ tư là hỗ trợ chất lượng vận hành. Audit, activity history, support, error handling và notification được chuẩn hóa giúp hệ thống dễ hỗ trợ và kiểm tra hơn.

## 2. Cross-cutting Business Scope

Cross-cutting Business Requirements là các yêu cầu không thuộc riêng một module nhưng ảnh hưởng đến mọi module. Các yêu cầu này có thể được áp dụng trực tiếp hoặc được module cụ thể hóa thêm trong tài liệu riêng.

| Cross-cutting Area | Applies To | Description |
|---|---|---|
| Ownership | All user-owned data | Mọi dữ liệu cá nhân phải gắn với chủ sở hữu và chỉ được thao tác bởi actor có quyền. |
| Authentication | Protected features | Chức năng được bảo vệ yêu cầu actor đã xác thực. |
| Authorization | Protected actions | Mọi hành động được bảo vệ phải kiểm tra quyền trước khi thực hiện. |
| Validation | All business inputs | Dữ liệu nhập phải được kiểm tra theo rule nghiệp vụ trước khi chấp nhận. |
| Status Management | Accounts, tasks, tickets, cycles, evaluations | Trạng thái nghiệp vụ phải thuộc tập giá trị hợp lệ và quyết định hành động được phép. |
| Audit | Sensitive administrative or business actions | Hành động quan trọng phải có khả năng truy vết. |
| Activity History | User-facing or operational history | Hành động quan trọng có thể xuất hiện trong history theo phạm vi hiển thị. |
| Reporting Scope | Dashboard and reports | Báo cáo chỉ hiển thị dữ liệu thuộc phạm vi quyền và filter được chọn. |
| Error Handling | All modules | Lỗi phải được xử lý rõ ràng, không làm thay đổi trạng thái nếu validation thất bại. |
| Search and Filter | Lists, reports, history, logs | Tìm kiếm, lọc, sắp xếp và phân trang phải có chuẩn nghiệp vụ thống nhất. |
| Export | Reports and histories | Export chỉ áp dụng khi policy cho phép và phải tuân thủ phạm vi quyền. |
| Notification | Reminders, support, announcements, status changes | Notification chỉ được gửi khi có mục đích nghiệp vụ rõ ràng. |
| Maintenance | System-wide operation | Maintenance status phải được truyền đạt rõ nếu được áp dụng. |

## 3. Global Business Rules

| Rule ID | Global Business Rule |
|---|---|
| GBR-001 | Một User chỉ được thao tác trên dữ liệu thuộc quyền sở hữu hoặc phạm vi quyền của mình. |
| GBR-002 | Mọi chức năng được bảo vệ phải yêu cầu actor đã xác thực. |
| GBR-003 | Mọi hành động được bảo vệ phải được kiểm tra authorization trước khi thực hiện. |
| GBR-004 | Mọi thay đổi quyền phải được kiểm tra trước khi thực hiện. |
| GBR-005 | Staff không được thay đổi Role hoặc Permission nếu không có chính sách đặc biệt được phê duyệt. |
| GBR-006 | Chỉ Admin hoặc actor được phân quyền rõ ràng mới được thay đổi cấu hình hệ thống. |
| GBR-007 | Chỉ Admin hoặc actor được phân quyền rõ ràng mới được quản lý Role và Permission. |
| GBR-008 | Actor không được thực hiện hành động làm mất khả năng quản trị tối thiểu của hệ thống nếu không có quy trình bảo vệ. |
| GBR-009 | Dữ liệu cá nhân của User không được hiển thị cho Staff hoặc Admin ngoài phạm vi chính sách được phê duyệt. |
| GBR-010 | Mọi thao tác quan trọng phải có khả năng truy vết theo Audit Policy hoặc History Policy. |
| GBR-011 | Hành động bị validation từ chối không được làm thay đổi trạng thái nghiệp vụ hiện tại. |
| GBR-012 | Giá trị planned là baseline đánh giá và không được thay đổi trong module evaluation nếu không có quy trình liên quan được phê duyệt. |
| GBR-013 | Chỉ Task Completed mới được đánh giá cuối cùng nếu policy không cho phép ngoại lệ. |
| GBR-014 | Task không có Time Capital hoặc estimated time hợp lệ không được xuất hiện trên Timeline nếu policy không cho phép ngoại lệ. |
| GBR-015 | Tổng nguồn lực phân bổ không được vượt nguồn lực khả dụng nếu User chưa chủ động cho phép over allocation. |
| GBR-016 | Over allocation phải được thể hiện rõ là tình trạng vượt mức, không phải nguồn lực bổ sung. |
| GBR-017 | Variance được hiểu là Actual - Planned ở mức business rule cơ bản. |
| GBR-018 | Dashboard và Report chỉ hiển thị dữ liệu trong phạm vi quyền và filter được chọn. |
| GBR-019 | Báo cáo phải phản ánh dữ liệu tại thời điểm truy xuất. |
| GBR-020 | Empty state phải được thể hiện rõ, không được hiển thị KPI hoặc kết luận gây hiểu nhầm. |
| GBR-021 | Ticket phải có trạng thái hợp lệ trong toàn bộ support workflow. |
| GBR-022 | Ticket đã đóng chỉ được mở lại theo Ticket Policy. |
| GBR-023 | Configuration mới phải hợp lệ trước khi được áp dụng. |
| GBR-024 | Activity Log không thay thế Audit đối với hành động quản trị nhạy cảm. |
| GBR-025 | Export phải tuân thủ cùng phạm vi quyền với dữ liệu đang được xem. |
| GBR-026 | Search và Filter không được trả dữ liệu ngoài phạm vi quyền. |
| GBR-027 | Dữ liệu không đủ phải được hiển thị là không đủ dữ liệu, không được tự suy diễn. |
| GBR-028 | Các thuật ngữ Planned, Actual, Variance, Efficiency, History và Audit phải được sử dụng nhất quán trên toàn hệ thống. |
| GBR-029 | Mọi thay đổi trạng thái quan trọng phải tuân thủ lifecycle hoặc status policy tương ứng. |
| GBR-030 | Notification chỉ được gửi khi có mục đích nghiệp vụ rõ ràng và actor nhận phù hợp. |

## 4. Validation Rules

### 4.1 Validation Classification

| Classification | Definition |
|---|---|
| Mandatory | Dữ liệu bắt buộc phải có để thực hiện hành động. |
| Optional | Dữ liệu không bắt buộc nhưng có thể cải thiện giá trị nghiệp vụ. |
| Conditional | Dữ liệu bắt buộc trong một điều kiện cụ thể. |
| Derived | Dữ liệu được xác định từ dữ liệu khác theo rule nghiệp vụ. |

### 4.2 Global Validation Rules

| Rule ID | Area | Classification | Validation Rule |
|---|---|---|---|
| VAL-001 | Identity | Mandatory | Actor phải được xác thực trước khi truy cập chức năng được bảo vệ. |
| VAL-002 | Identity | Mandatory | Actor phải có permission phù hợp trước khi thực hiện hành động được bảo vệ. |
| VAL-003 | Identity | Conditional | Tài khoản locked hoặc deactivated không được đăng nhập hoặc tiếp tục hành động được bảo vệ theo policy. |
| VAL-004 | Identity | Conditional | Thay đổi Role hoặc Permission phải kiểm tra tác động governance. |
| VAL-005 | Task | Mandatory | Task Name là bắt buộc khi tạo Task. |
| VAL-006 | Task | Mandatory | Task phải thuộc đúng một User sở hữu. |
| VAL-007 | Task | Conditional | Priority phải thuộc tập giá trị được phê duyệt nếu được sử dụng. |
| VAL-008 | Task | Conditional | Deadline phải hợp lệ và không được mâu thuẫn với scheduled start theo policy. |
| VAL-009 | Task | Conditional | Progress phải nằm trong khoảng 0 đến 100 nếu được cập nhật. |
| VAL-010 | Timeline | Conditional | Task phải có Time Capital hoặc estimated time hợp lệ để xuất hiện trên Timeline. |
| VAL-011 | Timeline | Conditional | Timeline move phải kiểm tra deadline, conflict và cycle policy. |
| VAL-012 | Resource | Mandatory | Time Capital và Money Capital không được âm. |
| VAL-013 | Resource | Conditional | Allocation amount phải lớn hơn 0. |
| VAL-014 | Resource | Conditional | Release amount không được vượt allocated amount còn hiệu lực. |
| VAL-015 | Resource | Conditional | Over allocation phải được User xác nhận nếu policy cho phép. |
| VAL-016 | Tracking | Conditional | Actual Time phải lớn hơn hoặc bằng 0 nếu được ghi nhận. |
| VAL-017 | Tracking | Conditional | Actual Cost phải lớn hơn hoặc bằng 0 nếu được ghi nhận. |
| VAL-018 | Tracking | Conditional | Planned và Actual phải đủ dữ liệu để tính Variance tương ứng. |
| VAL-019 | Reporting | Mandatory | Period thống kê hoặc report phải hợp lệ. |
| VAL-020 | Reporting | Conditional | Start period không được sau end period. |
| VAL-021 | Administration | Mandatory | Ticket phải có tiêu đề và nội dung khi tạo. |
| VAL-022 | Administration | Conditional | Ticket status, priority và category phải thuộc tập giá trị được phê duyệt nếu được sử dụng. |
| VAL-023 | Administration | Conditional | Configuration update phải có giá trị hợp lệ theo Configuration Policy. |
| VAL-024 | Administration | Conditional | Role không được trùng tên nếu policy yêu cầu unique name. |
| VAL-025 | Administration | Conditional | Permission không được gán sai phạm vi nghiệp vụ. |
| VAL-026 | Global | Derived | Variance được xác định từ Actual và Planned. |
| VAL-027 | Global | Derived | Efficiency được xác định từ Planned, Actual, completion và policy. |
| VAL-028 | Global | Derived | Remaining Capital được xác định từ capital, allocation, adjustment và policy. |

## 5. Business Policies

| Policy | Definition |
|---|---|
| Authentication Policy | Xác định điều kiện actor được xem là đã xác thực và cách xử lý session hết hạn hoặc không hợp lệ ở mức nghiệp vụ. |
| Authorization Policy | Xác định cách kiểm tra Role, Permission, ownership và phạm vi quyền trước khi cho phép hành động. |
| Planning Policy | Xác định thông tin tối thiểu để Task hoặc kế hoạch được xem là hợp lệ. |
| Resource Allocation Policy | Xác định cách phân bổ, phân bổ lại, giải phóng và kiểm soát nguồn lực. |
| Over Allocation Policy | Xác định khi nào User được phép phân bổ vượt nguồn lực khả dụng và điều kiện xác nhận. |
| Task Lifecycle Policy | Xác định trạng thái Task, điều kiện chuyển trạng thái và hành động được phép. |
| Timeline Policy | Xác định điều kiện Task xuất hiện trên Timeline và cách xử lý drag & drop, conflict hoặc reschedule. |
| Evaluation Policy | Xác định cách ghi Actual, tính Variance và đánh giá Efficiency. |
| Reporting Policy | Xác định loại report, period, filter, scope, empty state, drill-down và export. |
| Audit Policy | Xác định hành động nào phải audit, ai được xem audit và mức thông tin tối thiểu. |
| Activity History Policy | Xác định hành động nào xuất hiện trong activity history và ai được xem. |
| Support Policy | Xác định trách nhiệm Staff, escalation, resolution và closure. |
| Ticket Policy | Xác định ticket status, priority, category và lifecycle. |
| Role Management Policy | Xác định cách quản lý, gán, thu hồi và kiểm tra Role. |
| Permission Policy | Xác định cách quản lý, gán, thu hồi và kiểm tra Permission. |
| Configuration Policy | Xác định cấu hình nào được Admin xem hoặc cập nhật và điều kiện hợp lệ. |
| Retention Policy | Xác định thời gian lưu giữ dữ liệu nghiệp vụ ở mức chính sách, cần xác nhận thêm theo pháp lý và vận hành. |
| History Policy | Xác định lịch sử nào được giữ để User hoặc Admin truy vết thay đổi. |
| Notification Policy | Xác định khi nào gửi notification, gửi cho ai và mục đích nghiệp vụ. |
| Maintenance Policy | Xác định cách thông báo và xử lý trạng thái bảo trì. |
| Security Policy | Xác định nguyên tắc bảo vệ dữ liệu, quyền truy cập, least privilege và xử lý dữ liệu nhạy cảm ở mức nghiệp vụ. |

## 6. Permission Matrix

| Feature | Guest | User | Staff | Admin |
|---|---|---|---|---|
| Landing Page | View | View | View | View |
| Register | Create | - | - | - |
| Login/Logout | Create Session | Manage Own Session | Manage Own Session | Manage Own Session |
| Own Profile | - | View, Update | View, Update | View, Update |
| User Management | - | - | - | View, Update, Manage |
| Staff Management | - | - | - | View, Create, Update, Delete, Manage |
| Role Management | - | - | - | View, Create, Update, Delete, Manage, Approve |
| Permission Management | - | - | - | View, Create, Update, Delete, Manage, Approve |
| Own Resource Capital | - | View, Create, Update, Manage | - | View limited if policy allows |
| Resource History | - | View | View limited if policy allows | View limited if policy allows |
| Own Task | - | View, Create, Update, Delete, Manage | - | View limited if policy allows |
| Own Timeline | - | View, Create, Update, Manage | - | View limited if policy allows |
| Own Tracking/Evaluation | - | View, Create, Update, Manage | - | View limited if policy allows |
| Own Dashboard/Reports | - | View, Export if policy allows | - | View limited if policy allows |
| Support Ticket | - | Create, View Own | View, Update, Manage | View, Update, Manage |
| Activity Log | - | View Own if policy allows | View limited | View, Manage |
| Audit | - | - | View limited if explicitly assigned | View, Manage |
| System Configuration | - | - | - | View, Update, Manage, Approve |
| System Dashboard | - | - | View limited if policy allows | View, Manage |
| Announcement | - | View | View | View, Create, Update, Manage, Approve |
| Maintenance Status | View if public | View if public | View limited | View, Update, Manage |

## 7. Business Constraints

### 7.1 Business Constraints

| Constraint | Description |
|---|---|
| Personal Resource Focus | LifeBalance tập trung vào quản lý nguồn lực cá nhân, không phải quản lý dự án doanh nghiệp theo nhóm. |
| Two Core Resources | Time và Money là hai resource cốt lõi trong phạm vi hiện tại. |
| User-owned Data | Dữ liệu cá nhân phải gắn với User owner và không được truy cập tùy tiện. |
| Evaluation Depends on Data Quality | Dashboard, KPI và Report phụ thuộc vào Planned và Actual đầy đủ. |
| Scope Discipline | Các tính năng ngoài vision phải được đưa vào future scope hoặc open question. |

### 7.2 Operational Constraints

| Constraint | Description |
|---|---|
| Staff Capacity | Khả năng xử lý ticket phụ thuộc số lượng và năng lực Staff. |
| Admin Governance | Admin có quyền cao nhưng phải chịu audit và self-protection rule. |
| Support Process Maturity | Chất lượng hỗ trợ phụ thuộc vào Ticket Policy, Support Policy và escalation rule. |
| Data Completeness | User có thể không nhập đủ Actual, Category, Tag hoặc Estimated values. |

### 7.3 Legal Constraints

| Constraint | Description |
|---|---|
| Personal Data Protection | Dữ liệu tài khoản, Task, resource, Actual, report và ticket có thể là dữ liệu cá nhân. |
| Financial Sensitivity | Money Capital, Planned Cost và Actual Cost là thông tin nhạy cảm. |
| Consumer Protection | Hệ thống không được cam kết kết quả tài chính hoặc productivity tuyệt đối. |
| Jurisdiction Dependency | Yêu cầu pháp lý cụ thể phụ thuộc khu vực triển khai và cần xác minh riêng. |

### 7.4 Resource Constraints

| Constraint | Description |
|---|---|
| Limited Time and Money | Triết lý sản phẩm yêu cầu xử lý nguồn lực như hữu hạn. |
| User Input Dependency | Nhiều chỉ số phụ thuộc dữ liệu do User nhập. |
| Support Resources | Hỗ trợ vận hành phụ thuộc Staff và policy. |

### 7.5 Time Constraints

| Constraint | Description |
|---|---|
| Capital Cycles | Resource capital được quản lý theo daily, weekly và monthly. |
| Timeline Scheduling | Timeline phụ thuộc schedule và Time Capital hợp lệ. |
| Reporting Periods | Report và Dashboard phụ thuộc period hợp lệ. |
| Ticket Handling Period | Ticket có thể cần xử lý theo SLA nếu policy được phê duyệt. |

## 8. Assumptions

| Assumption ID | Assumption |
|---|---|
| ASM-001 | User là chủ sở hữu chính của dữ liệu cá nhân. |
| ASM-002 | Time và Money là hai nguồn lực cốt lõi trong phạm vi hiện tại. |
| ASM-003 | RBAC là mô hình phân quyền nghiệp vụ chính. |
| ASM-004 | Staff hỗ trợ vận hành nhưng không mặc định có quyền sửa dữ liệu cá nhân. |
| ASM-005 | Admin quản trị hệ thống nhưng vẫn phải tuân thủ audit và governance. |
| ASM-006 | Actual Time và Actual Cost do User ghi nhận hoặc xác nhận. |
| ASM-007 | Dashboard và Report chỉ có giá trị khi dữ liệu nền tảng đủ chất lượng. |
| ASM-008 | Recurring Task, Reminder, Announcement, Maintenance Mode và Export cần policy xác nhận nếu đưa vào release. |
| ASM-009 | Retention period và compliance detail chưa được xác nhận ở cấp pháp lý. |
| ASM-010 | Các volume sau hoặc tài liệu kỹ thuật sẽ kế thừa thuật ngữ và rule từ Volume 7. |

## 9. Non-functional Requirements

| NFR ID | Category | Requirement |
|---|---|---|
| GNFR-001 | Availability | Các chức năng cốt lõi như login, task access, capital access, tracking và support cần khả dụng phù hợp với nhu cầu sử dụng cá nhân và vận hành. |
| GNFR-002 | Performance | Các thao tác thường dùng như search, filter, dashboard, timeline và report phải phản hồi trong thời gian phù hợp với ngữ cảnh nghiệp vụ. |
| GNFR-003 | Security | Dữ liệu cá nhân, tài khoản, quyền truy cập, nguồn lực và báo cáo phải được bảo vệ theo ownership và permission. |
| GNFR-004 | Usability | Thuật ngữ, trạng thái, validation error và KPI phải được trình bày rõ ràng ở mức người dùng hiểu được. |
| GNFR-005 | Reliability | Trạng thái nghiệp vụ, balance, task lifecycle, ticket lifecycle và KPI phải nhất quán theo rule. |
| GNFR-006 | Maintainability | Business rule và policy phải có khả năng được cập nhật khi stakeholder phê duyệt thay đổi. |
| GNFR-007 | Scalability | Hệ thống phải phù hợp với tăng trưởng User, Task, Capital Cycle, History, Ticket, Report và Audit. |
| GNFR-008 | Auditability | Hành động nhạy cảm phải có khả năng truy vết. |
| GNFR-009 | Accessibility | Chức năng quan trọng không nên phụ thuộc duy nhất vào thao tác khó tiếp cận như drag & drop. |
| GNFR-010 | Localization | Thuật ngữ, ngày giờ, tiền tệ và period cần có khả năng được diễn giải phù hợp với ngữ cảnh địa phương nếu triển khai đa khu vực. |
| GNFR-011 | Compliance | Hệ thống cần hỗ trợ nguyên tắc bảo vệ dữ liệu cá nhân và xử lý thông tin nhạy cảm ở mức nghiệp vụ. |
| GNFR-012 | Business Continuity | Các chức năng vận hành như support, admin lock/unlock và audit cần có khả năng duy trì hoạt động phù hợp khi có sự cố. |
| GNFR-013 | Data Retention | Dữ liệu history, audit, ticket và report cần có retention policy ở mức nghiệp vụ. |
| GNFR-014 | Disaster Recovery Requirement | Cần xác định yêu cầu phục hồi nghiệp vụ đối với dữ liệu quan trọng như account, role, permission, task, capital, actual và audit. |

## 10. Error Handling Principles

| Error Type | Principle |
|---|---|
| Validation Error | Thông báo phải chỉ ra dữ liệu không hợp lệ và không làm thay đổi trạng thái hiện tại. |
| Permission Error | Actor đã xác thực nhưng thiếu quyền phải được từ chối rõ ràng, không tiết lộ dữ liệu ngoài phạm vi. |
| Authentication Error | Actor chưa xác thực hoặc session không hợp lệ phải được yêu cầu xác thực lại theo policy. |
| Business Rule Violation | Hành động vi phạm rule phải bị từ chối và giải thích ở mức nghiệp vụ nếu phù hợp. |
| Unexpected Error | Hệ thống cần thông báo lỗi chung, tránh lộ thông tin nhạy cảm, và không gây thay đổi không rõ trạng thái. |
| Maintenance Mode | Nếu hệ thống đang bảo trì, actor cần được thông báo theo Maintenance Policy. |
| Session Expired | Actor cần được yêu cầu xác thực lại hoặc xử lý theo Authentication Policy. |
| Empty Data | Dashboard, report, history hoặc search không có dữ liệu phải hiển thị trạng thái rỗng rõ ràng. |
| Conflict Error | Xung đột Timeline, ticket assignment hoặc concurrent update phải được xử lý theo policy tương ứng. |

## 11. Audit Requirements

| Audit Event | Required | Rationale |
|---|---|---|
| Login | Conditional | Cần xác nhận audit policy; có giá trị trong điều tra truy cập. |
| Logout | Conditional | Hỗ trợ truy vết phiên nếu policy yêu cầu. |
| Role Change | Yes | Thay đổi quyền truy cập nhạy cảm. |
| Permission Change | Yes | Thay đổi quyền truy cập nhạy cảm. |
| User Lock | Yes | Ảnh hưởng trực tiếp quyền truy cập. |
| User Unlock | Yes | Khôi phục quyền truy cập. |
| Deactivate/Reactivate Account | Yes | Thay đổi vòng đời tài khoản. |
| Capital Adjustment | Yes | Ảnh hưởng planned capital và balance. |
| Over Allocation Approval | Yes | Quyết định vượt nguồn lực khả dụng. |
| Task Completion | Conditional | Có giá trị truy vết lifecycle; áp dụng nếu History/Audit Policy yêu cầu. |
| Task Reopen | Conditional | Ảnh hưởng evaluation và lifecycle. |
| Actual Update | Conditional | Ảnh hưởng report và KPI. |
| Evaluation Finalization | Conditional | Ảnh hưởng kết quả đánh giá. |
| Configuration Update | Yes | Ảnh hưởng hành vi hệ thống. |
| Ticket Escalation | Conditional | Hỗ trợ support governance. |
| Ticket Closure/Reopen | Conditional | Hỗ trợ kiểm tra support. |
| Report Export | Conditional | Cần audit nếu export chứa dữ liệu nhạy cảm. |
| Announcement Broadcast | Conditional | Ảnh hưởng thông tin vận hành gửi rộng. |
| Maintenance Mode Change | Yes if supported | Ảnh hưởng truy cập và vận hành. |

## 12. Activity History Requirements

| History Area | Actions | Viewer | Retention Notes |
|---|---|---|---|
| Account Activity | Login, logout, status changes if policy allows | User for own activity; Admin if authorized | Retention period cần xác nhận. |
| Capital History | Create cycle, adjust, allocate, reallocate, release, transfer, close, reopen | User owner; Staff/Admin only if authorized | Cần đủ lâu để User hiểu biến động nguồn vốn. |
| Task History | Create, update, schedule, reschedule, complete, cancel, reopen, archive, restore | User owner | Cần phục vụ truy vết lifecycle. |
| Evaluation History | Record Actual, update Actual, evaluate, re-evaluate | User owner | Cần phục vụ giải thích KPI và report. |
| Ticket History | Create, assign, update, escalate, resolve, close, reopen | Staff/Admin; User for own ticket if policy allows | Cần phục vụ support accountability. |
| Administration History | Role, permission, configuration, lock/unlock | Admin | Có thể thuộc audit hơn là activity history. |
| Export History | Report export, history export if supported | User for own export; Admin if policy allows | Cần xác nhận theo privacy policy. |

## 13. Notification Requirements

Notification là phạm vi có điều kiện. Nếu hệ thống có Notification, các nguyên tắc sau được áp dụng.

| Notification Event | Recipient | Purpose |
|---|---|---|
| Account Locked | Account owner; Admin/Staff if policy allows | Thông báo tài khoản bị khóa và hướng xử lý. |
| Account Unlocked | Account owner | Thông báo quyền truy cập được khôi phục. |
| Password/Account Change | Account owner | Cảnh báo thay đổi nhạy cảm. |
| Task Reminder | User owner | Nhắc Task theo deadline hoặc schedule nếu policy cho phép. |
| Deadline Approaching | User owner | Cảnh báo Task sắp đến hạn. |
| Overdue Task | User owner | Thông báo Task quá hạn. |
| Capital Over Allocation | User owner | Cảnh báo vượt nguồn lực. |
| Actual Recording Reminder | User owner | Nhắc ghi Actual sau Task Completed nếu policy cho phép. |
| Ticket Assigned | Assigned Staff | Thông báo có ticket cần xử lý. |
| Ticket Updated/Resolved/Closed | Ticket requester and assigned Staff if policy allows | Cập nhật trạng thái support. |
| Ticket Escalated | Admin or escalation group | Yêu cầu xử lý cấp cao hơn. |
| Announcement | Target audience | Truyền thông vận hành. |
| Maintenance Notice | Affected users or all users | Thông báo bảo trì. |

## 14. Search & Filter Standards

### 14.1 Search Standards

| Standard | Description |
|---|---|
| Scope-bound Search | Search chỉ trả kết quả trong phạm vi quyền của actor. |
| Clear Criteria | Tiêu chí search cần có ý nghĩa nghiệp vụ rõ ràng. |
| Empty Result | Không có kết quả phải hiển thị trạng thái rõ ràng. |
| Sensitive Data Protection | Search không được làm lộ dữ liệu nhạy cảm ngoài phạm vi quyền. |

### 14.2 Filter Standards

| Filter Type | Applicable Areas |
|---|---|
| Period | Capital, Task, Timeline, Report, Audit, Activity Log, Ticket. |
| Status | Account, Task, Ticket, Cycle, Evaluation. |
| Category | Task, Report, Statistics. |
| Tag | Task, Report, Statistics. |
| Resource Type | Capital, Evaluation, Report. |
| Priority | Task, Ticket. |
| Actor | Audit, Activity Log, Administration. |
| Role/Permission | Identity, Administration. |

### 14.3 Sort Standards

Sort có thể áp dụng theo:

- Created time.
- Updated time.
- Deadline.
- Priority.
- Status.
- Scheduled time.
- Ticket priority.
- Ticket status.
- Report period.

Sort không được làm thay đổi dữ liệu nghiệp vụ.

### 14.4 Pagination Standards

Pagination ở mức nghiệp vụ cần:

- Giúp User hoặc Admin xử lý danh sách dài.
- Không làm thay đổi phạm vi filter.
- Không bỏ qua dữ liệu trong phạm vi quyền.
- Hiển thị tổng quan kết quả nếu policy yêu cầu.

## 15. Export & Import Requirements

### 15.1 Export Requirements

| Export Type | Scope | Constraints |
|---|---|---|
| Report Export | Daily, Weekly, Monthly, Yearly, Category, Tag, Resource, Productivity, Trend reports | Chỉ export dữ liệu trong phạm vi report đang xem và format được policy phê duyệt. |
| History Export | Capital, Task, Evaluation, Ticket, Activity History if supported | Cần kiểm soát quyền và có thể cần audit. |
| Dashboard Export | User Dashboard hoặc Admin Dashboard if supported | Chỉ export summary trong phạm vi quyền. |
| Audit Export | Admin only if policy allows | Cần policy nghiêm ngặt do dữ liệu nhạy cảm. |

### 15.2 Import Requirements

Import chưa được xác định là phạm vi chính thức trong các volume hiện tại. Nếu cần import, phải xác nhận:

- Loại dữ liệu được import.
- Actor được phép import.
- Validation rule trước khi chấp nhận.
- Cách xử lý dữ liệu lỗi.
- Tác động đến history và audit.
- Rủi ro privacy và ownership.

## 16. Business Glossary

| Term | Definition |
|---|---|
| Actual | Giá trị thực tế được ghi nhận sau hoặc trong quá trình thực hiện. |
| Activity History | Lịch sử hoạt động được hiển thị cho User hoặc actor vận hành theo policy. |
| Activity Log | Bản ghi hoạt động phục vụ hỗ trợ và vận hành. |
| Admin | Actor quản trị có quyền quản lý hệ thống theo policy. |
| Allocation | Việc gán nguồn lực dự kiến cho mục đích sử dụng. |
| Audit | Bản ghi phục vụ kiểm tra và trách nhiệm giải trình cho hành động quan trọng. |
| Capital | Tổng năng lực nguồn lực của User trong một chu kỳ. |
| Category | Phân loại có cấu trúc dùng để nhóm Task hoặc dữ liệu phân tích. |
| Configuration | Thiết lập hệ thống ở mức nghiệp vụ. |
| Dashboard | Góc nhìn tổng quan về dữ liệu nghiệp vụ hoặc vận hành. |
| Efficiency | Mức độ sử dụng nguồn lực hợp lý so với kế hoạch và kết quả. |
| Guest | Actor chưa xác thực hoặc chưa đăng ký. |
| History | Lịch sử thay đổi hoặc hoạt động có thể xem lại theo policy. |
| Money Capital | Nguồn lực tiền bạc dự kiến của User. |
| Permission | Quyền cho phép actor thực hiện hành động hoặc truy cập khu vực. |
| Planned | Giá trị dự kiến hoặc kế hoạch trước khi thực hiện. |
| Report | Tập thông tin có cấu trúc phục vụ phân tích hoặc vận hành. |
| Resource | Nguồn lực hữu hạn; hiện tại gồm Time và Money. |
| Role | Nhóm Permission đại diện cho trách nhiệm hoặc phạm vi truy cập. |
| Staff | Actor vận hành hỗ trợ User và xử lý ticket. |
| Support Ticket | Yêu cầu hỗ trợ được ghi nhận và xử lý theo workflow. |
| Tag | Nhãn linh hoạt dùng để phân loại Task hoặc thống kê. |
| Task | Đơn vị công việc cá nhân mà User dự định thực hiện. |
| Timeline | Góc nhìn theo thời gian hiển thị Task đủ điều kiện scheduling. |
| Time Capital | Nguồn lực thời gian dự kiến của User. |
| User | Actor đã đăng ký sử dụng LifeBalance để quản lý dữ liệu cá nhân. |
| Variance | Sai lệch giữa Actual và Planned. |

## 17. Traceability Matrix

| Business Goal | Business Requirement | Functional Requirement Reference | Use Case Reference | User Story Reference | Acceptance Criteria Reference |
|---|---|---|---|---|---|
| Resource-aware personal management | User can define and manage resource capital | RCM-FR-001 to RCM-FR-050 | RCM-UC-001 to RCM-UC-027 | RCM-US-001 to RCM-US-026 | RCM-AC-001 to RCM-AC-044 |
| Task as resource investment | User can create, plan, schedule and complete Task | TTM-FR-001 to TTM-FR-060 | TTM-UC-001 to TTM-UC-031 | TTM-US-001 to TTM-US-028 | TTM-AC-001 to TTM-AC-046 |
| Planned vs Actual evaluation | User can record Actual and evaluate variance | TER-FR-001 to TER-FR-060 | TER-UC-001 to TER-UC-025 | TER-US-001 to TER-US-022 | TER-AC-001 to TER-AC-042 |
| Secure access and role governance | Actor access is controlled by IAM and RBAC | IAM-FR-001 to IAM-FR-045 | IAM-UC-001 to IAM-UC-028 | IAM-US-001 to IAM-US-028 | IAM-AC-001 to IAM-AC-048 |
| Operational accountability | Staff/Admin can support and govern operations | ADM-FR-001 to ADM-FR-060 | ADM-UC-001 to ADM-UC-033 | ADM-US-001 to ADM-US-020 | ADM-AC-001 to ADM-AC-036 |
| Continuous improvement | User can view dashboards, reports, trends and KPI | TER-FR-013 to TER-FR-058 | TER-UC-008 to TER-UC-022 | TER-US-009 to TER-US-021 | TER-AC-017 to TER-AC-039 |
| Cross-module consistency | Global policies, validation and audit standards apply system-wide | GBR-001 to GBR-030; VAL-001 to VAL-028 | Cross-volume use cases | Cross-volume user stories | Cross-volume acceptance criteria |

## 18. Risks

### 18.1 Business Risks

| Risk | Description | Mitigation Direction |
|---|---|---|
| Product identity diluted | LifeBalance bị hiểu như to-do app thông thường. | Duy trì resource-aware language trong mọi module. |
| KPI misunderstood | User hiểu KPI như đánh giá tuyệt đối. | Giải thích KPI là công cụ cải thiện cá nhân. |
| Scope expansion | Tính năng ngoài vision được thêm vào không kiểm soát. | Dùng Volume 7 làm standard kiểm tra scope. |

### 18.2 Requirement Risks

| Risk | Description | Mitigation Direction |
|---|---|---|
| Rules conflict between modules | Business rule ở module khác nhau mâu thuẫn. | Dùng Global Business Rules làm chuẩn ưu tiên. |
| Assumptions become hidden requirements | Giả định bị hiểu thành yêu cầu chính thức. | Duy trì Assumptions và Open Questions rõ ràng. |
| Missing policy detail | Một số policy chưa có ngưỡng hoặc trạng thái cụ thể. | Xác nhận trong Open Questions trước khi thiết kế chi tiết. |

### 18.3 Operational Risks

| Risk | Description | Mitigation Direction |
|---|---|---|
| Support overload | Ticket tăng nhưng Staff giới hạn. | Ticket Policy, prioritization và dashboard support. |
| Audit overload | Audit quá nhiều hoặc quá ít. | Audit Policy cần xác định event và mức thông tin phù hợp. |
| Admin lockout | Admin cuối cùng mất quyền. | Self-protection rule. |

### 18.4 Governance Risks

| Risk | Description | Mitigation Direction |
|---|---|---|
| Excessive permissions | Actor được cấp quyền vượt trách nhiệm. | Least privilege và periodic access review. |
| Configuration misuse | Cấu hình bị thay đổi sai. | Configuration Policy và audit. |
| Poor history quality | History không đủ giải thích thay đổi. | History Policy xác định action và viewer. |

### 18.5 Compliance Risks

| Risk | Description | Mitigation Direction |
|---|---|---|
| Personal data exposure | Dữ liệu cá nhân bị xem ngoài phạm vi. | Ownership, authorization và export scope. |
| Financial sensitivity | Money Capital và Actual Cost bị lộ. | Security Policy và limited access. |
| Undefined retention | Chưa rõ thời gian lưu dữ liệu. | Retention Policy cần xác nhận pháp lý. |

## 19. Open Questions

| Question ID | Open Question |
|---|---|
| CBR-OQ-001 | Retention period cho audit, activity history, ticket, report và user data là bao lâu? |
| CBR-OQ-002 | Notification có thuộc phạm vi release hiện tại không? |
| CBR-OQ-003 | Export có hỗ trợ PDF, Excel và CSV trong release hiện tại không? |
| CBR-OQ-004 | Import có được hỗ trợ không? Nếu có, loại dữ liệu nào được import? |
| CBR-OQ-005 | Có yêu cầu localization về tiền tệ, ngôn ngữ và múi giờ không? |
| CBR-OQ-006 | Maintenance Mode có thuộc phạm vi release hiện tại không? |
| CBR-OQ-007 | Có cần approval nhiều cấp cho thay đổi Role/Permission nhạy cảm không? |
| CBR-OQ-008 | Có cần periodic access review cho Staff và Admin không? |
| CBR-OQ-009 | Các ngưỡng KPI chính thức là gì? |
| CBR-OQ-010 | Các event nào bắt buộc audit, event nào chỉ activity history? |
| CBR-OQ-011 | Staff/Admin được xem dữ liệu cá nhân của User ở mức nào trong tình huống hỗ trợ? |
| CBR-OQ-012 | Disaster recovery requirement ở mức nghiệp vụ cần thời gian khôi phục kỳ vọng là bao lâu? |
| CBR-OQ-013 | Data retention phải tuân thủ quy định pháp lý cụ thể của quốc gia nào? |
| CBR-OQ-014 | Search, filter và report có giới hạn phạm vi thời gian tối đa không? |
| CBR-OQ-015 | Empty KPI có hiển thị là 0 hay "không đủ dữ liệu"? |

## 20. Suggested Improvements

| Improvement ID | Suggested Improvement | Business Rationale |
|---|---|---|
| CBR-SI-001 | Thiết lập Business Rule Catalog riêng được quản lý theo version. | Giúp kiểm soát thay đổi rule xuyên module. |
| CBR-SI-002 | Xây dựng Permission Catalog có mô tả nghiệp vụ. | Giúp Admin hiểu quyền trước khi gán. |
| CBR-SI-003 | Thiết lập Data Completeness Indicator cho Dashboard và Report. | Giúp User biết KPI đáng tin đến đâu. |
| CBR-SI-004 | Chuẩn hóa Empty State Messaging cho toàn hệ thống. | Giảm hiểu nhầm khi không có dữ liệu. |
| CBR-SI-005 | Xây dựng Policy Register cho toàn bộ policies. | Giúp governance và change control tốt hơn. |
| CBR-SI-006 | Thiết lập periodic access review cho Staff và Admin. | Giảm rủi ro quyền lỗi thời. |
| CBR-SI-007 | Chuẩn hóa reason codes cho cancel, reopen, lock, unlock, adjust và escalate. | Tăng chất lượng history và report. |
| CBR-SI-008 | Bổ sung glossary song ngữ nếu dự án có cả người dùng tiếng Việt và tiếng Anh. | Giảm sai lệch thuật ngữ. |
| CBR-SI-009 | Xây dựng standard cho confidence level của KPI. | Tránh quyết định dựa trên dữ liệu thiếu. |
| CBR-SI-010 | Thiết lập review checkpoint khi thay đổi rule cross-module. | Giảm rủi ro ảnh hưởng dây chuyền giữa module. |

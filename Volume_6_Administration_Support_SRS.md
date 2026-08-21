# LifeBalance
# Volume 6 - Administration & Support SRS

## 1. Module Overview

### 1.1 Purpose

Administration & Support là module phục vụ hoạt động vận hành, hỗ trợ người dùng, quản trị tài khoản, quản trị nhân sự hỗ trợ, quản trị vai trò, quản trị quyền, kiểm tra audit, xem activity log, quản lý cấu hình hệ thống ở mức nghiệp vụ và theo dõi tình trạng vận hành thông qua System Dashboard.

Module này được xây dựng dựa trên định hướng từ các volume trước của LifeBalance. LifeBalance là hệ thống quản lý nguồn lực cá nhân, do đó thông tin người dùng có tính riêng tư và cần được vận hành theo nguyên tắc kiểm soát quyền, trách nhiệm giải trình và hỗ trợ đúng phạm vi. Administration & Support không can thiệp vào nội dung kế hoạch cá nhân của User trừ khi có chính sách ngoại lệ rõ ràng. Trọng tâm của module là bảo đảm hệ thống có thể được vận hành ổn định, có khả năng hỗ trợ người dùng, có kiểm soát thay đổi quản trị và có khả năng truy vết các hành động nhạy cảm.

Tài liệu này chỉ phân tích nghiệp vụ quản trị và hỗ trợ. Tài liệu không mô tả các module nghiệp vụ cá nhân như quản lý công việc, lịch cá nhân, nguồn vốn cá nhân hoặc đánh giá hiệu quả cá nhân. Các thuật ngữ liên quan đến User, Staff, Admin, Role, Permission, Audit và Activity Log được kế thừa từ các volume trước, đặc biệt là Volume 2 - Identity & Authorization.

### 1.2 Objectives

| Objective ID | Objective | Description |
|---|---|---|
| ADM-OBJ-001 | Quản lý người dùng | Hỗ trợ Admin xem, tìm kiếm, lọc, cập nhật trạng thái và quản lý vòng đời tài khoản User theo chính sách. |
| ADM-OBJ-002 | Quản lý Staff | Hỗ trợ Admin quản lý nhân sự Staff, phân công phạm vi hỗ trợ và thu hồi vai trò vận hành nếu cần. |
| ADM-OBJ-003 | Quản lý Support Ticket | Hỗ trợ tiếp nhận, phân công, xử lý, leo thang, giải quyết, đóng và mở lại ticket theo quy trình hỗ trợ. |
| ADM-OBJ-004 | Quản lý Role và Permission | Hỗ trợ Admin quản lý Role, Permission, gán và thu hồi quyền theo mô hình phân quyền đã được phê duyệt. |
| ADM-OBJ-005 | Quản lý khóa tài khoản tạm thời | Hỗ trợ Staff hoặc Admin khóa tạm thời tài khoản theo đúng quy trình và hỗ trợ Admin mở khóa khi đủ điều kiện. |
| ADM-OBJ-006 | Quản lý cấu hình hệ thống | Cho phép Admin xem và cập nhật cấu hình hệ thống thuộc phạm vi quản trị đã được phê duyệt. |
| ADM-OBJ-007 | Hỗ trợ Audit và Activity Log | Cho phép actor có quyền xem, tìm kiếm và lọc audit hoặc activity log phục vụ kiểm tra, hỗ trợ và trách nhiệm giải trình. |
| ADM-OBJ-008 | Cung cấp System Dashboard | Cho phép Admin xem tổng quan vận hành hệ thống ở mức quản trị. |
| ADM-OBJ-009 | Hỗ trợ thông báo vận hành | Cho phép phát thông báo hệ thống nếu chính sách announcement được phê duyệt. |
| ADM-OBJ-010 | Bảo đảm governance | Thiết lập business rule và policy để tránh lạm quyền, sai phạm phân quyền hoặc xử lý support thiếu nhất quán. |

### 1.3 Business Value

Administration & Support tạo giá trị nghiệp vụ bằng cách bảo đảm LifeBalance không chỉ là một sản phẩm cho người dùng cuối mà còn là một hệ thống có khả năng vận hành, hỗ trợ và kiểm soát thay đổi.

Giá trị thứ nhất là tăng độ tin cậy vận hành. Khi User gặp vấn đề về tài khoản, truy cập hoặc sử dụng, Staff có quy trình tiếp nhận và xử lý thông qua Support Ticket. Điều này giúp hoạt động hỗ trợ có trách nhiệm và có trạng thái rõ ràng.

Giá trị thứ hai là tăng khả năng kiểm soát quyền. Role và Permission được quản lý bởi Admin theo chính sách, giúp hạn chế việc Staff có quyền vượt phạm vi hoặc User truy cập khu vực không phù hợp.

Giá trị thứ ba là hỗ trợ trách nhiệm giải trình. Các thao tác quản trị như thay đổi quyền, khóa tài khoản, mở khóa tài khoản, cập nhật cấu hình và xử lý ticket cần có khả năng được audit hoặc xem lại trong activity log theo policy.

Giá trị thứ tư là hỗ trợ ra quyết định vận hành. System Dashboard và Business Reports giúp Admin hiểu số lượng ticket, tình trạng xử lý, hoạt động người dùng, thay đổi quyền và tình hình vận hành chung.

Giá trị thứ năm là giảm rủi ro trong hỗ trợ. Quy trình ticket, escalation, resolution và closure giúp tránh xử lý tùy tiện, bỏ sót yêu cầu hoặc đóng ticket không đúng.

### 1.4 Responsibilities

| Responsibility | Description |
|---|---|
| User Management | Xem, tìm kiếm, lọc, cập nhật thông tin hoặc trạng thái tài khoản User theo quyền quản trị. |
| Staff Management | Quản lý Staff, phân công Staff, gỡ Staff khỏi phạm vi hỗ trợ hoặc thay đổi trạng thái Staff theo policy. |
| Support Ticket Management | Quản lý vòng đời ticket từ tạo, tiếp nhận, phân công, xử lý, leo thang, giải quyết, đóng và mở lại. |
| Role Management | Quản lý Role, gán Role, thu hồi Role và kiểm soát thay đổi Role. |
| Permission Management | Quản lý Permission, gán Permission, thu hồi Permission và kiểm soát thay đổi Permission. |
| Temporary Account Lock | Khóa tài khoản tạm thời theo quy trình và lý do hợp lệ. |
| Unlock Account | Mở khóa tài khoản theo quyền và điều kiện được phê duyệt. |
| Configuration Management | Xem và cập nhật cấu hình hệ thống trong phạm vi quản trị. |
| Audit Review | Xem và tìm kiếm audit phục vụ kiểm tra các hành động quản trị. |
| Activity Log Review | Xem, tìm kiếm và lọc activity log phục vụ hỗ trợ và vận hành. |
| System Dashboard | Cung cấp tổng quan vận hành cho Admin và actor được phân quyền. |
| Announcement and Maintenance Awareness | Hỗ trợ thông báo vận hành hoặc trạng thái bảo trì nếu thuộc phạm vi được phê duyệt. |

## 2. Business Scope

### 2.1 In Scope

| Scope Area | Description |
|---|---|
| Administration | Các hoạt động quản trị vận hành hệ thống ở mức nghiệp vụ. |
| Support | Các hoạt động hỗ trợ User thông qua Staff và ticket. |
| Ticket | Quản lý support ticket và vòng đời xử lý ticket. |
| Configuration | Xem và cập nhật cấu hình hệ thống thuộc phạm vi quản trị. |
| Audit | Xem và tìm kiếm audit phục vụ trách nhiệm giải trình. |
| Activity Log | Xem, tìm kiếm và lọc activity log phục vụ hỗ trợ và vận hành. |
| Role | Quản lý Role và gán hoặc thu hồi Role theo chính sách. |
| Permission | Quản lý Permission và gán hoặc thu hồi Permission theo chính sách. |
| System Dashboard | Xem tổng quan vận hành hệ thống. |
| User Management | Xem, tìm kiếm, lọc, cập nhật, deactivate, reactivate, lock và unlock tài khoản theo quyền. |
| Staff Management | Xem, quản lý, phân công và gỡ Staff theo chính sách vận hành. |
| Announcement | Phát thông báo hệ thống nếu chính sách announcement được phê duyệt. |
| Maintenance Status | Xem trạng thái bảo trì nếu chính sách maintenance được phê duyệt. |

### 2.2 Out of Scope

| Out of Scope Area | Explanation |
|---|---|
| Quản lý nghiệp vụ cá nhân của User | Module không mô tả cách User quản lý kế hoạch, nguồn lực, lịch cá nhân hoặc đánh giá cá nhân. |
| Nội dung chi tiết dữ liệu cá nhân | Staff và Admin không mặc định được can thiệp vào nội dung cá nhân của User. |
| Tư vấn sử dụng cá nhân | Staff hỗ trợ vận hành, không thay User quyết định kế hoạch cá nhân. |
| Thiết kế giao diện | Tài liệu không mô tả bố cục màn hình, wireframe hoặc thành phần giao diện cụ thể. |
| Chính sách pháp lý chi tiết | Các yêu cầu compliance theo khu vực cần được xác nhận bởi bên có thẩm quyền. |
| Tự động xử lý toàn bộ ticket | Module không mặc định tự động quyết định resolution hoặc escalation nếu chưa có policy. |
| Quản trị tài chính hoặc kế toán | Module không xử lý nghiệp vụ tài chính chuyên môn. |

### 2.3 Dependencies

| Dependency | Description | Impact |
|---|---|---|
| Volume 1 - Vision & Business Overview | Cung cấp actor, scope và nguyên tắc vận hành. | Administration & Support phải tôn trọng quyền riêng tư và trách nhiệm giải trình. |
| Volume 2 - Identity & Authorization | Cung cấp Role, Permission, account status, lock/unlock và authorization. | Quản trị User, Staff, Role, Permission phụ thuộc vào mô hình IAM. |
| Support Policy | Cần xác định SLA, trạng thái ticket, escalation và closure. | Ảnh hưởng đến Support Ticket requirements. |
| Audit Policy | Cần xác định hành động nào phải audit và ai được xem audit. | Ảnh hưởng đến auditability và governance. |
| Configuration Policy | Cần xác định cấu hình nào được xem hoặc cập nhật bởi Admin. | Ảnh hưởng đến update configuration. |
| Temporary Lock Policy | Cần xác định ai được khóa, thời hạn khóa, lý do và điều kiện unlock. | Ảnh hưởng đến Staff và Admin operations. |
| Maintenance Policy | Cần xác định Maintenance Mode có thuộc phạm vi hiện tại hay không. | Ảnh hưởng đến maintenance status và announcement. |

## 3. Business Concepts

| Concept | Definition |
|---|---|
| Support Ticket | Yêu cầu hỗ trợ được tạo bởi User, Staff hoặc actor được phép nhằm ghi nhận vấn đề, câu hỏi hoặc yêu cầu vận hành cần xử lý. |
| Ticket Status | Trạng thái của ticket trong vòng đời hỗ trợ, ví dụ New, Received, Assigned, In Progress, Escalated, Resolved, Closed, Reopened nếu policy cho phép. |
| Ticket Priority | Mức độ ưu tiên xử lý ticket, phản ánh mức độ ảnh hưởng hoặc khẩn cấp theo support policy. |
| Ticket Category | Nhóm phân loại ticket, ví dụ account access, permission issue, usage question, system issue hoặc nhóm khác được phê duyệt. |
| Activity Log | Bản ghi hoạt động vận hành hoặc hoạt động hệ thống ở mức nghiệp vụ, phục vụ hỗ trợ, theo dõi và kiểm tra. |
| Audit | Bản ghi có tính kiểm tra và trách nhiệm giải trình đối với hành động quan trọng, đặc biệt là hành động quản trị, phân quyền và thay đổi trạng thái tài khoản. |
| Configuration | Thiết lập hệ thống ở mức nghiệp vụ có thể ảnh hưởng đến vận hành, chính sách hoặc hành vi hệ thống được kiểm soát. |
| Role | Nhóm permission đại diện cho trách nhiệm hoặc phạm vi truy cập của actor. |
| Permission | Quyền cho phép actor thực hiện một hành động hoặc truy cập một khu vực chức năng. |
| Staff | Actor vận hành chịu trách nhiệm tiếp nhận và xử lý ticket trong phạm vi quyền được cấp. |
| Administrator | Actor quản trị có quyền quản lý User, Staff, Role, Permission, Configuration, Audit và System Dashboard theo policy. |
| Temporary Lock | Trạng thái khóa tạm thời tài khoản nhằm ngăn truy cập trong một tình huống vận hành hoặc rủi ro xác định. |
| Account Status | Trạng thái nghiệp vụ của tài khoản như active, locked, deactivated hoặc trạng thái khác được phê duyệt. |
| System Dashboard | Góc nhìn tổng quan về tình hình vận hành hệ thống dành cho Admin hoặc actor có quyền. |
| Announcement | Thông báo vận hành được phát tới nhóm người dùng hoặc toàn hệ thống nếu chính sách cho phép. |
| Maintenance Mode | Trạng thái hệ thống đang trong bảo trì, có thể ảnh hưởng đến quyền truy cập hoặc thông báo vận hành nếu thuộc phạm vi được phê duyệt. |

## 4. Actors

### 4.1 Staff

| Attribute | Description |
|---|---|
| Responsibilities | Tiếp nhận ticket, xử lý ticket trong phạm vi được phân công, hỗ trợ User, xem activity log trong phạm vi được cấp, khóa tạm thời tài khoản theo quy trình nếu có quyền. |
| Permissions | Receive Ticket; View Assigned Ticket; Update Ticket; Escalate Ticket; Resolve Ticket; Search Ticket; Filter Ticket; View Activity Log if authorized; Temporary Lock Account within scope. |
| Limitations | Staff không được thay đổi Role, không được thay đổi Permission, không được cập nhật cấu hình hệ thống, không được xem audit quản trị nếu không được cấp quyền, không được tự khóa chính mình hoặc khóa Admin nếu policy không cho phép. |

Staff là actor vận hành tuyến đầu. Staff có quyền xử lý ticket và một số hành động hỗ trợ giới hạn. Staff không phải actor quản trị quyền và không có quyền can thiệp tùy ý vào tài khoản hoặc dữ liệu cá nhân.

### 4.2 Admin

| Attribute | Description |
|---|---|
| Responsibilities | Quản lý User, Staff, Role, Permission, cấu hình hệ thống, audit, activity log, system dashboard và các hành động quản trị nhạy cảm. |
| Permissions | Manage User; Manage Staff; Manage Role; Manage Permission; Assign Role; Revoke Role; Assign Permission; Revoke Permission; View Audit; Search Audit; View Configuration; Update Configuration; View System Dashboard; Unlock Account; Reactivate User; Deactivate User; Broadcast Announcement if approved. |
| Limitations | Admin phải tuân thủ audit, governance và policy. Admin không được tự làm mất quyền quản trị tối thiểu, không được bỏ qua rule, không được can thiệp dữ liệu cá nhân ngoài phạm vi chính sách rõ ràng. |

Admin là actor có quyền quản trị cao nhất trong phạm vi Administration & Support, nhưng quyền này phải được kiểm soát bằng business rule, audit và policy.

## 5. Functional Requirements

| Requirement ID | Requirement Name | Description | Primary Actor |
|---|---|---|---|
| ADM-FR-001 | View User | Hệ thống phải cho phép Admin xem thông tin User trong phạm vi quản trị được phê duyệt. | Admin |
| ADM-FR-002 | Search User | Hệ thống phải cho phép Admin tìm kiếm User theo tiêu chí được phê duyệt. | Admin |
| ADM-FR-003 | Filter User | Hệ thống phải cho phép Admin lọc User theo trạng thái, vai trò hoặc tiêu chí quản trị được phê duyệt. | Admin |
| ADM-FR-004 | Update User Information | Hệ thống phải cho phép Admin cập nhật thông tin User thuộc phạm vi quản trị được phép. | Admin |
| ADM-FR-005 | Deactivate User | Hệ thống phải cho phép Admin vô hiệu hóa tài khoản User theo policy. | Admin |
| ADM-FR-006 | Reactivate User | Hệ thống phải cho phép Admin kích hoạt lại tài khoản User theo policy. | Admin |
| ADM-FR-007 | Temporary Lock Account | Hệ thống phải cho phép Staff hoặc Admin khóa tạm thời tài khoản trong phạm vi quyền được cấp. | Staff, Admin |
| ADM-FR-008 | Unlock Account | Hệ thống phải cho phép Admin mở khóa tài khoản theo Temporary Lock Policy. | Admin |
| ADM-FR-009 | View Staff | Hệ thống phải cho phép Admin xem danh sách và thông tin Staff trong phạm vi quản trị. | Admin |
| ADM-FR-010 | Manage Staff | Hệ thống phải cho phép Admin quản lý Staff theo Staff Management Policy. | Admin |
| ADM-FR-011 | Assign Staff | Hệ thống phải cho phép Admin hoặc actor được phân quyền phân công Staff xử lý ticket hoặc phạm vi hỗ trợ. | Admin |
| ADM-FR-012 | Remove Staff | Hệ thống phải cho phép Admin gỡ Staff khỏi ticket hoặc phạm vi hỗ trợ theo policy. | Admin |
| ADM-FR-013 | Create Ticket | Hệ thống phải cho phép tạo support ticket theo phạm vi được phê duyệt. | User, Staff, Admin |
| ADM-FR-014 | Receive Ticket | Hệ thống phải cho phép Staff tiếp nhận ticket mới. | Staff |
| ADM-FR-015 | Assign Ticket | Hệ thống phải cho phép ticket được phân công cho Staff phù hợp. | Staff, Admin |
| ADM-FR-016 | Update Ticket | Hệ thống phải cho phép Staff cập nhật ticket trong phạm vi được phân công. | Staff |
| ADM-FR-017 | Escalate Ticket | Hệ thống phải cho phép Staff leo thang ticket khi vượt phạm vi xử lý hoặc cần quyết định Admin. | Staff |
| ADM-FR-018 | Resolve Ticket | Hệ thống phải cho phép Staff hoặc Admin đánh dấu ticket đã xử lý theo policy. | Staff, Admin |
| ADM-FR-019 | Close Ticket | Hệ thống phải cho phép đóng ticket khi đáp ứng điều kiện closure. | Staff, Admin |
| ADM-FR-020 | Reopen Ticket | Hệ thống phải cho phép mở lại ticket đã đóng theo policy. | Staff, Admin |
| ADM-FR-021 | Search Ticket | Hệ thống phải cho phép Staff và Admin tìm kiếm ticket theo tiêu chí hỗ trợ. | Staff, Admin |
| ADM-FR-022 | Filter Ticket | Hệ thống phải cho phép Staff và Admin lọc ticket theo status, priority, category, assignee hoặc period. | Staff, Admin |
| ADM-FR-023 | View Activity Log | Hệ thống phải cho phép actor có quyền xem activity log trong phạm vi được cấp. | Staff, Admin |
| ADM-FR-024 | Search Activity Log | Hệ thống phải cho phép tìm kiếm activity log theo tiêu chí được phê duyệt. | Staff, Admin |
| ADM-FR-025 | Filter Activity Log | Hệ thống phải cho phép lọc activity log theo actor, action, period hoặc category nếu policy cho phép. | Staff, Admin |
| ADM-FR-026 | View Audit | Hệ thống phải cho phép Admin xem audit trong phạm vi quản trị. | Admin |
| ADM-FR-027 | Search Audit | Hệ thống phải cho phép Admin tìm kiếm audit theo tiêu chí được phê duyệt. | Admin |
| ADM-FR-028 | View Configuration | Hệ thống phải cho phép Admin xem cấu hình hệ thống thuộc phạm vi quản trị. | Admin |
| ADM-FR-029 | Update Configuration | Hệ thống phải cho phép Admin cập nhật cấu hình hợp lệ theo Configuration Policy. | Admin |
| ADM-FR-030 | View System Dashboard | Hệ thống phải cho phép Admin xem System Dashboard. | Admin |
| ADM-FR-031 | View System Statistics | Hệ thống phải cho phép Admin xem thống kê vận hành hệ thống. | Admin |
| ADM-FR-032 | Manage Role | Hệ thống phải cho phép Admin quản lý Role theo Role Management Policy. | Admin |
| ADM-FR-033 | Manage Permission | Hệ thống phải cho phép Admin quản lý Permission theo Permission Policy. | Admin |
| ADM-FR-034 | Assign Role | Hệ thống phải cho phép Admin gán Role cho tài khoản hợp lệ. | Admin |
| ADM-FR-035 | Revoke Role | Hệ thống phải cho phép Admin thu hồi Role khỏi tài khoản hợp lệ. | Admin |
| ADM-FR-036 | Assign Permission | Hệ thống phải cho phép Admin gán Permission theo policy. | Admin |
| ADM-FR-037 | Revoke Permission | Hệ thống phải cho phép Admin thu hồi Permission theo policy. | Admin |
| ADM-FR-038 | Broadcast Announcement | Hệ thống phải cho phép Admin phát thông báo nếu Announcement Policy được phê duyệt. | Admin |
| ADM-FR-039 | View Maintenance Status | Hệ thống phải cho phép Admin xem trạng thái maintenance nếu Maintenance Policy được phê duyệt. | Admin |
| ADM-FR-040 | Validate Ticket Status | Hệ thống phải kiểm tra ticket status hợp lệ trước khi cập nhật. | System |
| ADM-FR-041 | Validate Ticket Priority | Hệ thống phải kiểm tra ticket priority thuộc tập giá trị được phê duyệt. | System |
| ADM-FR-042 | Validate Ticket Category | Hệ thống phải kiểm tra ticket category hợp lệ. | System |
| ADM-FR-043 | Validate Account Lock Scope | Hệ thống phải kiểm tra actor có được khóa tài khoản mục tiêu hay không. | System |
| ADM-FR-044 | Validate Role Change | Hệ thống phải kiểm tra thay đổi Role không vi phạm governance rule. | System |
| ADM-FR-045 | Validate Permission Change | Hệ thống phải kiểm tra thay đổi Permission không vi phạm Permission Policy. | System |
| ADM-FR-046 | Record Administration Audit | Hệ thống phải ghi nhận audit cho thao tác quản trị quan trọng theo policy. | System |
| ADM-FR-047 | Record Ticket History | Hệ thống phải ghi nhận lịch sử thay đổi ticket theo Ticket Policy. | System |
| ADM-FR-048 | View Ticket Detail | Hệ thống phải cho phép Staff hoặc Admin xem chi tiết ticket trong phạm vi quyền. | Staff, Admin |
| ADM-FR-049 | Update Ticket Priority | Hệ thống phải cho phép actor có quyền cập nhật priority của ticket theo policy. | Staff, Admin |
| ADM-FR-050 | Update Ticket Category | Hệ thống phải cho phép actor có quyền cập nhật category của ticket theo policy. | Staff, Admin |
| ADM-FR-051 | Add Ticket Comment | Hệ thống phải cho phép Staff hoặc Admin thêm comment xử lý vào ticket theo policy. | Staff, Admin |
| ADM-FR-052 | View User Account Status | Hệ thống phải cho phép Admin xem account status của User. | Admin |
| ADM-FR-053 | View Staff Account Status | Hệ thống phải cho phép Admin xem account status của Staff. | Admin |
| ADM-FR-054 | Validate Configuration Change | Hệ thống phải kiểm tra cấu hình mới hợp lệ trước khi cập nhật. | System |
| ADM-FR-055 | View Role Assignment | Hệ thống phải cho phép Admin xem Role assignment hiện tại. | Admin |
| ADM-FR-056 | View Permission Assignment | Hệ thống phải cho phép Admin xem Permission assignment hiện tại. | Admin |
| ADM-FR-057 | Search Role | Hệ thống phải cho phép Admin tìm kiếm Role. | Admin |
| ADM-FR-058 | Search Permission | Hệ thống phải cho phép Admin tìm kiếm Permission. | Admin |
| ADM-FR-059 | View Support Queue | Hệ thống phải cho phép Staff hoặc Admin xem hàng đợi ticket theo phạm vi được cấp. | Staff, Admin |
| ADM-FR-060 | Escalation Review | Hệ thống phải cho phép Admin xem ticket đã được escalate để quyết định xử lý tiếp. | Admin |

## 6. Non-functional Requirements

| NFR ID | Category | Requirement | Description |
|---|---|---|---|
| ADM-NFR-001 | Security | Privileged Access Control | Chức năng quản trị phải chỉ khả dụng cho actor có quyền phù hợp. |
| ADM-NFR-002 | Security | Least Privilege | Staff chỉ được cấp quyền cần thiết để hỗ trợ, không được có quyền quản trị ngoài phạm vi. |
| ADM-NFR-003 | Availability | Support Availability | Ticket và support queue cần khả dụng để xử lý yêu cầu hỗ trợ. |
| ADM-NFR-004 | Availability | Administration Availability | Chức năng quản trị cần khả dụng khi Admin cần xử lý sự cố hoặc thay đổi quyền. |
| ADM-NFR-005 | Performance | Search Response | Tìm kiếm User, Ticket, Audit và Log phải phản hồi phù hợp với nhu cầu vận hành. |
| ADM-NFR-006 | Performance | Dashboard Response | System Dashboard phải phản hồi phù hợp để Admin theo dõi nhanh tình trạng vận hành. |
| ADM-NFR-007 | Auditability | Administration Traceability | Mọi thao tác quản trị quan trọng phải có khả năng truy vết theo policy. |
| ADM-NFR-008 | Auditability | Permission Change Traceability | Thay đổi Role và Permission phải được audit. |
| ADM-NFR-009 | Maintainability | Policy Maintainability | Các policy quản trị và hỗ trợ phải có khả năng được cập nhật theo quyết định nghiệp vụ. |
| ADM-NFR-010 | Reliability | Ticket State Reliability | Trạng thái ticket phải phản ánh đúng vòng đời xử lý. |
| ADM-NFR-011 | Reliability | Account Status Reliability | Account status phải phản ánh đúng quyết định lock, unlock, deactivate hoặc reactivate. |
| ADM-NFR-012 | Usability | Operational Clarity | Staff và Admin phải hiểu rõ trạng thái ticket, account status và hành động được phép. |
| ADM-NFR-013 | Usability | Safe Error Handling | Thông báo lỗi vận hành phải rõ ràng nhưng không tiết lộ thông tin nhạy cảm. |
| ADM-NFR-014 | Scalability | Ticket Growth | Module phải hỗ trợ số lượng ticket tăng theo quy mô sử dụng. |
| ADM-NFR-015 | Scalability | Log Growth | Module phải hỗ trợ tăng trưởng audit và activity log theo thời gian. |
| ADM-NFR-016 | Operational Continuity | Maintenance Awareness | Nếu Maintenance Mode được phê duyệt, trạng thái bảo trì phải được truyền đạt rõ cho actor phù hợp. |
| ADM-NFR-017 | Operational Continuity | Escalation Continuity | Ticket escalation phải giúp yêu cầu không bị thất lạc khi vượt phạm vi Staff. |
| ADM-NFR-018 | Security | Self-protection | Hệ thống phải ngăn hoặc kiểm soát hành động actor tự làm mất quyền vận hành quan trọng. |

## 7. Business Rules

| Business Rule ID | Business Rule |
|---|---|
| ADM-BR-001 | Staff không được thay đổi Role. |
| ADM-BR-002 | Staff không được thay đổi Permission. |
| ADM-BR-003 | Staff chỉ được khóa tài khoản theo đúng quy trình và trong phạm vi được cấp. |
| ADM-BR-004 | Staff không được tự khóa chính mình nếu policy không cho phép. |
| ADM-BR-005 | Staff không được khóa Admin nếu không có chính sách đặc biệt. |
| ADM-BR-006 | Admin có quyền cấu hình hệ thống trong phạm vi được phê duyệt. |
| ADM-BR-007 | Chỉ Admin mới được thay đổi Permission nếu không có role quản trị khác được phê duyệt. |
| ADM-BR-008 | Chỉ Admin mới được gán hoặc thu hồi Role nếu policy không quy định khác. |
| ADM-BR-009 | Ticket phải có trạng thái hợp lệ. |
| ADM-BR-010 | Ticket phải có priority hợp lệ nếu priority là bắt buộc. |
| ADM-BR-011 | Ticket phải có category hợp lệ nếu category là bắt buộc. |
| ADM-BR-012 | Ticket đã đóng chỉ được mở lại theo Ticket Policy. |
| ADM-BR-013 | Ticket đã resolved chỉ được closed khi đáp ứng điều kiện closure. |
| ADM-BR-014 | Ticket escalation phải có lý do nếu policy yêu cầu. |
| ADM-BR-015 | Ticket assignment phải gán cho Staff hợp lệ hoặc nhóm xử lý hợp lệ theo policy. |
| ADM-BR-016 | Ticket không có Staff xử lý phải nằm trong hàng đợi hoặc trạng thái chưa phân công. |
| ADM-BR-017 | Staff chỉ được cập nhật ticket được phân công hoặc ticket thuộc phạm vi queue được cấp. |
| ADM-BR-018 | Admin có thể xem và xử lý ticket escalated. |
| ADM-BR-019 | Mọi thao tác quản trị quan trọng phải được Audit. |
| ADM-BR-020 | Thay đổi Role phải được Audit. |
| ADM-BR-021 | Thay đổi Permission phải được Audit. |
| ADM-BR-022 | Temporary Lock và Unlock phải được Audit. |
| ADM-BR-023 | Deactivate và Reactivate tài khoản phải được Audit. |
| ADM-BR-024 | Update Configuration phải được Audit. |
| ADM-BR-025 | Activity Log chỉ được xem bởi actor có quyền. |
| ADM-BR-026 | Audit chỉ được xem bởi Admin hoặc actor được phân quyền rõ ràng. |
| ADM-BR-027 | Role không được trùng tên nếu policy yêu cầu tên duy nhất. |
| ADM-BR-028 | Permission không được gán sai phạm vi. |
| ADM-BR-029 | Role đang được sử dụng không được xóa hoặc vô hiệu hóa nếu policy chưa xác định cách xử lý người dùng đang có Role đó. |
| ADM-BR-030 | Permission đang được sử dụng không được thu hồi nếu hành động làm mất khả năng quản trị tối thiểu mà không có quy trình bảo vệ. |
| ADM-BR-031 | Admin không được tự thu hồi quyền Admin cuối cùng nếu điều đó làm hệ thống mất khả năng quản trị. |
| ADM-BR-032 | Admin không được deactivate Admin cuối cùng nếu không có quy trình bảo vệ. |
| ADM-BR-033 | Unlock Account chỉ được thực hiện bởi Admin hoặc actor được policy cho phép. |
| ADM-BR-034 | Tài khoản bị temporary lock không được đăng nhập theo policy IAM. |
| ADM-BR-035 | Tài khoản deactivate không được đăng nhập hoặc thực hiện hành động được bảo vệ. |
| ADM-BR-036 | Configuration mới phải hợp lệ trước khi cập nhật. |
| ADM-BR-037 | Announcement chỉ được phát bởi Admin nếu Announcement Policy được phê duyệt. |
| ADM-BR-038 | Maintenance Status chỉ được thay đổi nếu Maintenance Policy cho phép và actor có quyền. |
| ADM-BR-039 | System Dashboard chỉ hiển thị dữ liệu vận hành thuộc phạm vi quyền. |
| ADM-BR-040 | User Management không cho phép Staff cập nhật thông tin User nếu Staff không có quyền rõ ràng. |
| ADM-BR-041 | Staff Management chỉ thuộc Admin nếu policy không quy định khác. |
| ADM-BR-042 | Ticket comment phải thuộc ticket hợp lệ và actor có quyền. |
| ADM-BR-043 | Ticket bị đóng nhầm chỉ được reopen theo policy. |
| ADM-BR-044 | Search và Filter không được trả dữ liệu ngoài phạm vi quyền. |
| ADM-BR-045 | Hành động bị từ chối không được làm thay đổi trạng thái tài khoản, ticket, role, permission hoặc configuration. |
| ADM-BR-046 | Mọi thay đổi support workflow quan trọng phải có lịch sử xử lý nếu Ticket Policy yêu cầu. |
| ADM-BR-047 | Activity Log không thay thế Audit đối với hành động quản trị nhạy cảm. |
| ADM-BR-048 | Audit phải phản ánh actor thực hiện hành động ở mức nghiệp vụ nếu policy yêu cầu. |
| ADM-BR-049 | System Dashboard Summary không được hiển thị dữ liệu cá nhân chi tiết nếu không có policy cho phép. |
| ADM-BR-050 | Staff bị remove khỏi ticket không được tiếp tục xử lý ticket đó nếu không còn quyền. |

## 8. Administration Policies

### 8.1 User Management Policy

User Management Policy định nghĩa quyền và giới hạn trong việc xem, tìm kiếm, lọc, cập nhật, deactivate, reactivate, temporary lock và unlock tài khoản User.

Nguyên tắc chính:

- Admin có quyền quản lý User trong phạm vi được phê duyệt.
- Staff không mặc định được cập nhật thông tin User.
- Temporary lock phải có lý do và phạm vi hợp lệ nếu policy yêu cầu.
- Deactivate khác temporary lock và cần điều kiện rõ ràng.
- Reactivate không tự động unlock nếu policy không quy định.

### 8.2 Role Management Policy

Role Management Policy định nghĩa cách tạo, cập nhật, gán, thu hồi hoặc vô hiệu hóa Role.

Nguyên tắc chính:

- Role phải có tên và ý nghĩa nghiệp vụ rõ ràng.
- Role không được trùng tên nếu policy yêu cầu duy nhất.
- Thay đổi Role phải được audit.
- Role đang được sử dụng cần được xử lý cẩn trọng trước khi xóa hoặc vô hiệu hóa.
- Staff không được thay đổi Role.

### 8.3 Permission Policy

Permission Policy định nghĩa cách quản lý Permission và ràng buộc khi gán hoặc thu hồi Permission.

Nguyên tắc chính:

- Permission phải có phạm vi nghiệp vụ rõ ràng.
- Chỉ Admin hoặc actor được phân quyền rõ mới được thay đổi Permission.
- Permission không được gán sai phạm vi.
- Thu hồi Permission phải kiểm tra tác động vận hành.
- Thay đổi Permission phải được audit.

### 8.4 Ticket Policy

Ticket Policy định nghĩa vòng đời và quy tắc xử lý support ticket.

Trạng thái tham chiếu:

- New.
- Received.
- Assigned.
- In Progress.
- Escalated.
- Resolved.
- Closed.
- Reopened.

Nguyên tắc chính:

- Ticket phải có tiêu đề và nội dung.
- Ticket phải có status hợp lệ.
- Ticket có thể có priority và category theo policy.
- Ticket được assignment cho Staff hoặc queue hợp lệ.
- Ticket closed chỉ được reopen theo policy.

### 8.5 Support Policy

Support Policy định nghĩa trách nhiệm Staff, escalation và chất lượng xử lý.

Nguyên tắc chính:

- Staff chỉ xử lý ticket trong phạm vi được cấp.
- Ticket vượt phạm vi phải được escalate.
- Resolution phải phản ánh cách xử lý hoặc kết quả hỗ trợ.
- Staff không thay User quyết định nội dung cá nhân.
- Support action có thể cần lịch sử xử lý.

### 8.6 Configuration Policy

Configuration Policy định nghĩa cấu hình nào được Admin xem hoặc cập nhật.

Nguyên tắc chính:

- Configuration phải hợp lệ trước khi cập nhật.
- Update Configuration phải được audit.
- Cấu hình nhạy cảm có thể yêu cầu xác nhận bổ sung.
- Staff không được cập nhật Configuration.

### 8.7 Audit Policy

Audit Policy định nghĩa hành động nào cần ghi nhận và ai được xem audit.

Các hành động nên audit:

- Role change.
- Permission change.
- Temporary lock.
- Unlock.
- Deactivate.
- Reactivate.
- Configuration update.
- Staff management action.
- Admin-sensitive action.

### 8.8 Activity Log Policy

Activity Log Policy định nghĩa phạm vi xem và sử dụng activity log.

Nguyên tắc chính:

- Activity Log phục vụ hỗ trợ và vận hành.
- Activity Log chỉ được xem bởi actor có quyền.
- Activity Log không thay thế Audit cho hành động nhạy cảm.
- Search và Filter log phải tuân thủ phạm vi quyền.

### 8.9 Temporary Lock Policy

Temporary Lock Policy định nghĩa khi nào tài khoản được khóa tạm thời và ai được phép khóa.

Nguyên tắc chính:

- Staff có thể khóa User nếu có quyền và lý do hợp lệ.
- Staff không được tự khóa chính mình nếu policy không cho phép.
- Staff không được khóa Admin nếu không có chính sách đặc biệt.
- Unlock mặc định thuộc Admin nếu policy chưa xác nhận actor khác.
- Lock và unlock phải được audit.

### 8.10 System Maintenance Policy

System Maintenance Policy định nghĩa cách hệ thống thông báo hoặc hiển thị trạng thái bảo trì nếu thuộc phạm vi.

Nguyên tắc chính:

- Maintenance Status phải rõ ràng cho Admin.
- Announcement có thể được sử dụng để thông báo bảo trì nếu policy cho phép.
- Maintenance Mode không được kích hoạt hoặc thay đổi bởi Staff nếu không có quyền.
- Tác động của maintenance đến User cần được xác định trong policy riêng.

## 9. Workflows

### 9.1 Receive Ticket

#### Main Flow

1. Ticket mới được tạo hoặc xuất hiện trong support queue.
2. Staff xem danh sách ticket mới trong phạm vi quyền.
3. Staff chọn ticket để receive.
4. Hệ thống kiểm tra quyền của Staff.
5. Hệ thống chuyển ticket sang trạng thái Received hoặc trạng thái tương đương theo policy.
6. Hệ thống ghi nhận ticket history nếu policy yêu cầu.

#### Alternative Flow

- Ticket được tự động đưa vào queue chờ phân công nếu chưa có Staff nhận.
- Admin có thể receive ticket trong trường hợp hỗ trợ trực tiếp nếu policy cho phép.

#### Exception Flow

- Staff không có quyền với queue: hệ thống từ chối.
- Ticket đã được xử lý bởi Staff khác: hệ thống thông báo trạng thái hiện tại.

### 9.2 Assign Ticket

#### Main Flow

1. Admin hoặc actor có quyền chọn ticket cần phân công.
2. Actor chọn Staff phù hợp.
3. Hệ thống kiểm tra Staff hợp lệ và có phạm vi xử lý.
4. Hệ thống gán ticket cho Staff.
5. Ticket chuyển sang Assigned hoặc trạng thái được policy xác định.
6. Hệ thống ghi nhận ticket history.

#### Alternative Flow

- Ticket được gán vào nhóm xử lý nếu policy hỗ trợ queue theo nhóm.
- Staff tự nhận ticket nếu policy cho phép.

#### Exception Flow

- Staff không hợp lệ hoặc không còn active: hệ thống từ chối.
- Actor không có quyền assign: hệ thống từ chối.

### 9.3 Resolve Ticket

#### Main Flow

1. Staff mở ticket được phân công.
2. Staff cập nhật thông tin xử lý.
3. Staff nhập resolution hoặc kết quả hỗ trợ theo policy.
4. Hệ thống kiểm tra ticket status.
5. Hệ thống chuyển ticket sang Resolved.
6. Hệ thống ghi nhận ticket history.

#### Alternative Flow

- Staff escalate nếu ticket vượt phạm vi.
- Admin resolve ticket escalated nếu cần quyết định quản trị.

#### Exception Flow

- Ticket không thuộc phạm vi Staff: hệ thống từ chối.
- Ticket thiếu thông tin resolution bắt buộc: hệ thống yêu cầu bổ sung.

### 9.4 Close Ticket

#### Main Flow

1. Staff hoặc Admin chọn ticket đã resolved.
2. Hệ thống kiểm tra điều kiện closure.
3. Actor xác nhận close.
4. Hệ thống chuyển ticket sang Closed.
5. Hệ thống ghi nhận ticket history.

#### Alternative Flow

- User xác nhận hài lòng trước khi close nếu policy yêu cầu.
- Ticket tự close sau một thời gian resolved nếu policy cho phép.

#### Exception Flow

- Ticket chưa resolved hoặc chưa đủ điều kiện closure: hệ thống từ chối.

### 9.5 Temporary Lock

#### Main Flow

1. Staff hoặc Admin chọn tài khoản cần khóa.
2. Actor nhập lý do nếu policy yêu cầu.
3. Hệ thống kiểm tra quyền và phạm vi khóa.
4. Hệ thống kiểm tra target account có thể bị khóa.
5. Actor xác nhận.
6. Hệ thống đặt tài khoản vào trạng thái temporary lock.
7. Hệ thống ghi nhận audit.

#### Alternative Flow

- Admin khóa Staff hoặc User nếu policy cho phép.
- Staff chỉ khóa User trong phạm vi được cấp.

#### Exception Flow

- Staff tự khóa chính mình: hệ thống từ chối nếu policy không cho phép.
- Staff khóa Admin: hệ thống từ chối.
- Tài khoản đã deactivated: hệ thống xử lý theo policy trạng thái.

### 9.6 Unlock

#### Main Flow

1. Admin chọn tài khoản đang temporary lock.
2. Hệ thống kiểm tra quyền unlock.
3. Hệ thống kiểm tra account status.
4. Admin xác nhận unlock.
5. Hệ thống gỡ trạng thái temporary lock nếu điều kiện đáp ứng.
6. Hệ thống ghi nhận audit.

#### Alternative Flow

- Temporary lock tự hết hạn nếu policy cho phép.

#### Exception Flow

- Tài khoản không bị lock: hệ thống thông báo không có hành động cần thực hiện.
- Tài khoản deactivated: unlock không tự động reactivate nếu policy không cho phép.

### 9.7 Assign Role

#### Main Flow

1. Admin chọn tài khoản mục tiêu.
2. Admin chọn Role cần gán.
3. Hệ thống kiểm tra Role hợp lệ và tài khoản mục tiêu hợp lệ.
4. Hệ thống kiểm tra governance rule.
5. Admin xác nhận.
6. Hệ thống gán Role.
7. Hệ thống ghi nhận audit.

#### Alternative Flow

- Tài khoản đã có Role đó, hệ thống thông báo không có thay đổi.

#### Exception Flow

- Role không hợp lệ hoặc Admin không có quyền: hệ thống từ chối.

### 9.8 Remove Role

#### Main Flow

1. Admin chọn tài khoản mục tiêu.
2. Admin chọn Role cần thu hồi.
3. Hệ thống kiểm tra governance rule.
4. Admin xác nhận.
5. Hệ thống thu hồi Role.
6. Hệ thống ghi nhận audit.

#### Alternative Flow

- Tài khoản không có Role đó, hệ thống thông báo không có thay đổi.

#### Exception Flow

- Thu hồi Role làm mất Admin cuối cùng: hệ thống từ chối hoặc yêu cầu quy trình bảo vệ.

### 9.9 Update Permission

#### Main Flow

1. Admin chọn Permission hoặc Role cần cập nhật.
2. Admin chọn hành động assign hoặc revoke.
3. Hệ thống kiểm tra Permission Policy.
4. Admin xác nhận.
5. Hệ thống cập nhật Permission assignment.
6. Hệ thống ghi nhận audit.

#### Alternative Flow

- Permission đã được gán, hệ thống thông báo không có thay đổi.

#### Exception Flow

- Permission không hợp lệ hoặc thay đổi gây mất khả năng quản trị tối thiểu: hệ thống từ chối.

### 9.10 Update Configuration

#### Main Flow

1. Admin mở Configuration.
2. Admin chọn cấu hình cần cập nhật.
3. Admin nhập giá trị mới.
4. Hệ thống kiểm tra Configuration Policy.
5. Admin xác nhận thay đổi.
6. Hệ thống cập nhật cấu hình.
7. Hệ thống ghi nhận audit.

#### Alternative Flow

- Cấu hình nhạy cảm yêu cầu xác nhận bổ sung nếu policy quy định.

#### Exception Flow

- Giá trị cấu hình không hợp lệ: hệ thống từ chối.
- Actor không có quyền: hệ thống từ chối.

## 10. Use Case List

| Use Case ID | Use Case Name | Primary Actor | Summary |
|---|---|---|---|
| ADM-UC-001 | View User | Admin | Xem thông tin User. |
| ADM-UC-002 | Search User | Admin | Tìm kiếm User. |
| ADM-UC-003 | Filter User | Admin | Lọc User. |
| ADM-UC-004 | Update User Information | Admin | Cập nhật thông tin User trong phạm vi quản trị. |
| ADM-UC-005 | Deactivate User | Admin | Vô hiệu hóa tài khoản User. |
| ADM-UC-006 | Reactivate User | Admin | Kích hoạt lại tài khoản User. |
| ADM-UC-007 | Temporary Lock Account | Staff, Admin | Khóa tạm thời tài khoản. |
| ADM-UC-008 | Unlock Account | Admin | Mở khóa tài khoản. |
| ADM-UC-009 | View Staff | Admin | Xem thông tin Staff. |
| ADM-UC-010 | Manage Staff | Admin | Quản lý Staff. |
| ADM-UC-011 | Create Ticket | User, Staff, Admin | Tạo support ticket. |
| ADM-UC-012 | Receive Ticket | Staff | Tiếp nhận ticket. |
| ADM-UC-013 | Assign Ticket | Staff, Admin | Phân công ticket. |
| ADM-UC-014 | Update Ticket | Staff, Admin | Cập nhật ticket. |
| ADM-UC-015 | Escalate Ticket | Staff | Leo thang ticket. |
| ADM-UC-016 | Resolve Ticket | Staff, Admin | Xử lý ticket. |
| ADM-UC-017 | Close Ticket | Staff, Admin | Đóng ticket. |
| ADM-UC-018 | Reopen Ticket | Staff, Admin | Mở lại ticket. |
| ADM-UC-019 | Search Ticket | Staff, Admin | Tìm kiếm ticket. |
| ADM-UC-020 | Filter Ticket | Staff, Admin | Lọc ticket. |
| ADM-UC-021 | View Activity Log | Staff, Admin | Xem activity log. |
| ADM-UC-022 | View Audit | Admin | Xem audit. |
| ADM-UC-023 | View Configuration | Admin | Xem cấu hình hệ thống. |
| ADM-UC-024 | Update Configuration | Admin | Cập nhật cấu hình hệ thống. |
| ADM-UC-025 | View System Dashboard | Admin | Xem dashboard quản trị. |
| ADM-UC-026 | Manage Role | Admin | Quản lý Role. |
| ADM-UC-027 | Manage Permission | Admin | Quản lý Permission. |
| ADM-UC-028 | Assign Role | Admin | Gán Role. |
| ADM-UC-029 | Revoke Role | Admin | Thu hồi Role. |
| ADM-UC-030 | Assign Permission | Admin | Gán Permission. |
| ADM-UC-031 | Revoke Permission | Admin | Thu hồi Permission. |
| ADM-UC-032 | Broadcast Announcement | Admin | Phát thông báo nếu policy cho phép. |
| ADM-UC-033 | View Maintenance Status | Admin | Xem trạng thái bảo trì nếu policy cho phép. |

## 11. Use Case Specification

### ADM-UC-001 - View User

| Field | Description |
|---|---|
| ID | ADM-UC-001 |
| Description | Admin xem thông tin User trong phạm vi quản trị. |
| Primary Actor | Admin |
| Trigger | Admin mở thông tin User. |
| Preconditions | Admin đã xác thực và có quyền quản lý User. |
| Main Flow | 1. Admin chọn User. 2. Hệ thống kiểm tra quyền. 3. Hệ thống hiển thị thông tin được phép. |
| Alternative Flow | Admin chỉ xem account status nếu policy giới hạn. |
| Exception Flow | Không có quyền hoặc User không tồn tại dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu User. |
| Business Rules | ADM-BR-039, ADM-BR-044 |

### ADM-UC-002 - Search User

| Field | Description |
|---|---|
| ID | ADM-UC-002 |
| Description | Admin tìm kiếm User theo tiêu chí quản trị. |
| Primary Actor | Admin |
| Trigger | Admin nhập tiêu chí tìm kiếm. |
| Preconditions | Admin có quyền search User. |
| Main Flow | 1. Admin nhập tiêu chí. 2. Hệ thống kiểm tra quyền. 3. Hệ thống trả kết quả trong phạm vi quyền. |
| Alternative Flow | Không có kết quả, hệ thống hiển thị trạng thái không có dữ liệu. |
| Exception Flow | Tiêu chí không hợp lệ hoặc không có quyền dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | ADM-BR-044 |

### ADM-UC-003 - Filter User

| Field | Description |
|---|---|
| ID | ADM-UC-003 |
| Description | Admin lọc User theo trạng thái hoặc tiêu chí quản trị. |
| Primary Actor | Admin |
| Trigger | Admin chọn filter. |
| Preconditions | Admin có quyền xem User. |
| Main Flow | 1. Admin chọn filter. 2. Hệ thống kiểm tra filter. 3. Hệ thống hiển thị User phù hợp. |
| Alternative Flow | Admin kết hợp nhiều filter nếu policy cho phép. |
| Exception Flow | Filter không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | ADM-BR-044 |

### ADM-UC-004 - Update User Information

| Field | Description |
|---|---|
| ID | ADM-UC-004 |
| Description | Admin cập nhật thông tin User trong phạm vi quản trị được phép. |
| Primary Actor | Admin |
| Trigger | Admin gửi thay đổi thông tin User. |
| Preconditions | Admin có quyền update User và trường thông tin thuộc phạm vi được phép. |
| Main Flow | 1. Admin chọn User. 2. Admin nhập thay đổi. 3. Hệ thống validate. 4. Admin xác nhận. 5. Hệ thống cập nhật và audit nếu policy yêu cầu. |
| Alternative Flow | Admin hủy trước xác nhận. |
| Exception Flow | Thay đổi vượt quyền hoặc không hợp lệ dẫn đến từ chối. |
| Postconditions | User information được cập nhật nếu hợp lệ. |
| Business Rules | ADM-BR-019, ADM-BR-040, ADM-BR-045 |

### ADM-UC-005 - Deactivate User

| Field | Description |
|---|---|
| ID | ADM-UC-005 |
| Description | Admin vô hiệu hóa tài khoản User. |
| Primary Actor | Admin |
| Trigger | Admin chọn deactivate. |
| Preconditions | Tài khoản mục tiêu đủ điều kiện deactivate. |
| Main Flow | 1. Admin chọn User. 2. Hệ thống kiểm tra quyền. 3. Admin nhập lý do nếu cần. 4. Admin xác nhận. 5. Hệ thống deactivate tài khoản. 6. Hệ thống ghi audit. |
| Alternative Flow | Tài khoản đã deactivated, hệ thống thông báo trạng thái hiện tại. |
| Exception Flow | Actor không đủ quyền hoặc target không hợp lệ dẫn đến từ chối. |
| Postconditions | Tài khoản không còn active theo policy. |
| Business Rules | ADM-BR-023, ADM-BR-035 |

### ADM-UC-006 - Reactivate User

| Field | Description |
|---|---|
| ID | ADM-UC-006 |
| Description | Admin kích hoạt lại tài khoản User. |
| Primary Actor | Admin |
| Trigger | Admin chọn reactivate. |
| Preconditions | Tài khoản đang deactivated và đủ điều kiện reactivate. |
| Main Flow | 1. Admin chọn User. 2. Hệ thống kiểm tra điều kiện. 3. Admin xác nhận. 4. Hệ thống reactivate. 5. Hệ thống ghi audit. |
| Alternative Flow | Nếu tài khoản còn temporary lock, reactivate không tự unlock trừ khi policy cho phép. |
| Exception Flow | Tài khoản không đủ điều kiện dẫn đến từ chối. |
| Postconditions | Tài khoản active nếu không còn hạn chế khác. |
| Business Rules | ADM-BR-023, ADM-BR-033 |

### ADM-UC-007 - Temporary Lock Account

| Field | Description |
|---|---|
| ID | ADM-UC-007 |
| Description | Staff hoặc Admin khóa tạm thời tài khoản theo policy. |
| Primary Actor | Staff, Admin |
| Trigger | Actor chọn temporary lock. |
| Preconditions | Actor có quyền lock và target account nằm trong phạm vi được phép. |
| Main Flow | 1. Actor chọn tài khoản. 2. Actor nhập lý do nếu cần. 3. Hệ thống kiểm tra scope. 4. Actor xác nhận. 5. Hệ thống lock tài khoản. 6. Hệ thống ghi audit. |
| Alternative Flow | Admin khóa Staff hoặc User nếu policy cho phép. |
| Exception Flow | Staff tự khóa chính mình hoặc khóa Admin khi không được phép dẫn đến từ chối. |
| Postconditions | Tài khoản bị temporary lock. |
| Business Rules | ADM-BR-003, ADM-BR-004, ADM-BR-005, ADM-BR-022, ADM-BR-034 |

### ADM-UC-008 - Unlock Account

| Field | Description |
|---|---|
| ID | ADM-UC-008 |
| Description | Admin mở khóa tài khoản đang temporary lock. |
| Primary Actor | Admin |
| Trigger | Admin chọn unlock. |
| Preconditions | Tài khoản đang temporary lock và đủ điều kiện unlock. |
| Main Flow | 1. Admin chọn tài khoản. 2. Hệ thống kiểm tra quyền. 3. Admin xác nhận. 4. Hệ thống unlock. 5. Hệ thống ghi audit. |
| Alternative Flow | Lock tự hết hạn nếu policy cho phép. |
| Exception Flow | Tài khoản không bị lock hoặc deactivated dẫn đến xử lý theo policy. |
| Postconditions | Tài khoản không còn temporary lock nếu hợp lệ. |
| Business Rules | ADM-BR-022, ADM-BR-033 |

### ADM-UC-009 - View Staff

| Field | Description |
|---|---|
| ID | ADM-UC-009 |
| Description | Admin xem thông tin Staff. |
| Primary Actor | Admin |
| Trigger | Admin mở danh sách Staff. |
| Preconditions | Admin có quyền Manage Staff hoặc View Staff. |
| Main Flow | 1. Admin yêu cầu xem Staff. 2. Hệ thống kiểm tra quyền. 3. Hệ thống hiển thị thông tin được phép. |
| Alternative Flow | Không có Staff phù hợp, hệ thống hiển thị trạng thái rỗng. |
| Exception Flow | Không có quyền dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | ADM-BR-041, ADM-BR-044 |

### ADM-UC-010 - Manage Staff

| Field | Description |
|---|---|
| ID | ADM-UC-010 |
| Description | Admin quản lý Staff theo policy. |
| Primary Actor | Admin |
| Trigger | Admin thực hiện hành động quản lý Staff. |
| Preconditions | Admin có quyền Manage Staff. |
| Main Flow | 1. Admin chọn Staff. 2. Admin chọn hành động. 3. Hệ thống kiểm tra policy. 4. Admin xác nhận. 5. Hệ thống cập nhật. 6. Hệ thống audit nếu cần. |
| Alternative Flow | Admin hủy thao tác trước xác nhận. |
| Exception Flow | Hành động vi phạm governance dẫn đến từ chối. |
| Postconditions | Staff được cập nhật theo hành động hợp lệ. |
| Business Rules | ADM-BR-019, ADM-BR-041 |

### ADM-UC-011 - Create Ticket

| Field | Description |
|---|---|
| ID | ADM-UC-011 |
| Description | Actor được phép tạo support ticket. |
| Primary Actor | User, Staff, Admin |
| Trigger | Actor gửi yêu cầu hỗ trợ. |
| Preconditions | Actor có quyền tạo ticket theo policy. |
| Main Flow | 1. Actor nhập tiêu đề và nội dung. 2. Actor chọn category/priority nếu cần. 3. Hệ thống validate. 4. Hệ thống tạo ticket ở trạng thái New. |
| Alternative Flow | Staff tạo ticket thay mặt một tình huống hỗ trợ nếu policy cho phép. |
| Exception Flow | Thiếu tiêu đề hoặc nội dung dẫn đến từ chối. |
| Postconditions | Ticket mới được tạo. |
| Business Rules | ADM-BR-009, ADM-BR-010, ADM-BR-011 |

### ADM-UC-012 - Receive Ticket

| Field | Description |
|---|---|
| ID | ADM-UC-012 |
| Description | Staff tiếp nhận ticket mới. |
| Primary Actor | Staff |
| Trigger | Staff chọn ticket trong queue. |
| Preconditions | Staff có quyền với queue. |
| Main Flow | 1. Staff chọn ticket. 2. Hệ thống kiểm tra quyền. 3. Hệ thống chuyển ticket sang Received. |
| Alternative Flow | Ticket được giữ trong queue nếu chưa có Staff receive. |
| Exception Flow | Ticket đã được receive bởi actor khác dẫn đến thông báo trạng thái hiện tại. |
| Postconditions | Ticket được tiếp nhận. |
| Business Rules | ADM-BR-016, ADM-BR-017 |

### ADM-UC-013 - Assign Ticket

| Field | Description |
|---|---|
| ID | ADM-UC-013 |
| Description | Ticket được phân công cho Staff phù hợp. |
| Primary Actor | Staff, Admin |
| Trigger | Actor chọn assign ticket. |
| Preconditions | Actor có quyền assign và Staff mục tiêu hợp lệ. |
| Main Flow | 1. Actor chọn ticket. 2. Actor chọn Staff. 3. Hệ thống validate. 4. Hệ thống assign ticket. |
| Alternative Flow | Ticket được assign vào queue nhóm nếu policy cho phép. |
| Exception Flow | Staff không hợp lệ hoặc actor không có quyền dẫn đến từ chối. |
| Postconditions | Ticket có assignee hợp lệ. |
| Business Rules | ADM-BR-015, ADM-BR-016 |

### ADM-UC-014 - Update Ticket

| Field | Description |
|---|---|
| ID | ADM-UC-014 |
| Description | Staff hoặc Admin cập nhật ticket. |
| Primary Actor | Staff, Admin |
| Trigger | Actor nhập thay đổi ticket. |
| Preconditions | Actor có quyền với ticket. |
| Main Flow | 1. Actor mở ticket. 2. Actor cập nhật thông tin. 3. Hệ thống validate status/category/priority. 4. Hệ thống cập nhật ticket. |
| Alternative Flow | Actor thêm comment xử lý. |
| Exception Flow | Ticket không thuộc phạm vi hoặc status không hợp lệ dẫn đến từ chối. |
| Postconditions | Ticket được cập nhật. |
| Business Rules | ADM-BR-009, ADM-BR-010, ADM-BR-011, ADM-BR-017, ADM-BR-042 |

### ADM-UC-015 - Escalate Ticket

| Field | Description |
|---|---|
| ID | ADM-UC-015 |
| Description | Staff leo thang ticket khi vượt phạm vi xử lý. |
| Primary Actor | Staff |
| Trigger | Staff chọn escalate. |
| Preconditions | Ticket thuộc phạm vi Staff và đủ điều kiện escalate. |
| Main Flow | 1. Staff chọn ticket. 2. Staff nhập lý do nếu cần. 3. Hệ thống chuyển ticket sang Escalated. 4. Hệ thống đưa ticket vào phạm vi review của Admin. |
| Alternative Flow | Staff chuyển ticket sang nhóm chuyên trách nếu policy cho phép. |
| Exception Flow | Thiếu lý do bắt buộc hoặc ticket không thuộc phạm vi dẫn đến từ chối. |
| Postconditions | Ticket ở trạng thái Escalated. |
| Business Rules | ADM-BR-014, ADM-BR-018 |

### ADM-UC-016 - Resolve Ticket

| Field | Description |
|---|---|
| ID | ADM-UC-016 |
| Description | Staff hoặc Admin đánh dấu ticket đã xử lý. |
| Primary Actor | Staff, Admin |
| Trigger | Actor chọn resolve. |
| Preconditions | Ticket đang ở trạng thái có thể resolve. |
| Main Flow | 1. Actor nhập resolution. 2. Hệ thống kiểm tra điều kiện. 3. Hệ thống chuyển ticket sang Resolved. |
| Alternative Flow | Ticket cần escalation thay vì resolve. |
| Exception Flow | Thiếu resolution bắt buộc hoặc ticket không hợp lệ dẫn đến từ chối. |
| Postconditions | Ticket ở trạng thái Resolved. |
| Business Rules | ADM-BR-013, ADM-BR-017 |

### ADM-UC-017 - Close Ticket

| Field | Description |
|---|---|
| ID | ADM-UC-017 |
| Description | Actor có quyền đóng ticket đã xử lý. |
| Primary Actor | Staff, Admin |
| Trigger | Actor chọn close ticket. |
| Preconditions | Ticket đáp ứng điều kiện closure. |
| Main Flow | 1. Actor chọn ticket. 2. Hệ thống kiểm tra closure. 3. Actor xác nhận. 4. Hệ thống chuyển ticket sang Closed. |
| Alternative Flow | Ticket tự close theo policy sau thời gian resolved. |
| Exception Flow | Ticket chưa đủ điều kiện close dẫn đến từ chối. |
| Postconditions | Ticket ở trạng thái Closed. |
| Business Rules | ADM-BR-012, ADM-BR-013 |

### ADM-UC-018 - Reopen Ticket

| Field | Description |
|---|---|
| ID | ADM-UC-018 |
| Description | Actor có quyền mở lại ticket đã đóng. |
| Primary Actor | Staff, Admin |
| Trigger | Actor chọn reopen ticket. |
| Preconditions | Ticket đang Closed và policy cho phép reopen. |
| Main Flow | 1. Actor chọn ticket. 2. Actor nhập lý do nếu cần. 3. Hệ thống kiểm tra policy. 4. Hệ thống chuyển ticket sang Reopened. |
| Alternative Flow | Ticket quay về Assigned hoặc In Progress theo policy. |
| Exception Flow | Ticket không đủ điều kiện reopen dẫn đến từ chối. |
| Postconditions | Ticket được mở lại theo policy. |
| Business Rules | ADM-BR-012, ADM-BR-043 |

### ADM-UC-019 - Search Ticket

| Field | Description |
|---|---|
| ID | ADM-UC-019 |
| Description | Staff hoặc Admin tìm kiếm ticket. |
| Primary Actor | Staff, Admin |
| Trigger | Actor nhập tiêu chí. |
| Preconditions | Actor có quyền xem ticket. |
| Main Flow | 1. Actor nhập tiêu chí. 2. Hệ thống kiểm tra phạm vi. 3. Hệ thống trả ticket phù hợp. |
| Alternative Flow | Không có kết quả, hệ thống hiển thị không có dữ liệu. |
| Exception Flow | Tiêu chí không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi ticket. |
| Business Rules | ADM-BR-044 |

### ADM-UC-020 - Filter Ticket

| Field | Description |
|---|---|
| ID | ADM-UC-020 |
| Description | Staff hoặc Admin lọc ticket. |
| Primary Actor | Staff, Admin |
| Trigger | Actor chọn filter. |
| Preconditions | Actor có quyền xem ticket. |
| Main Flow | 1. Actor chọn filter. 2. Hệ thống validate. 3. Hệ thống hiển thị ticket phù hợp. |
| Alternative Flow | Actor kết hợp nhiều filter nếu policy cho phép. |
| Exception Flow | Filter không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi ticket. |
| Business Rules | ADM-BR-044 |

### ADM-UC-021 - View Activity Log

| Field | Description |
|---|---|
| ID | ADM-UC-021 |
| Description | Actor có quyền xem activity log. |
| Primary Actor | Staff, Admin |
| Trigger | Actor mở activity log. |
| Preconditions | Actor có quyền View Activity Log. |
| Main Flow | 1. Actor chọn phạm vi log. 2. Hệ thống kiểm tra quyền. 3. Hệ thống hiển thị activity log được phép. |
| Alternative Flow | Actor search hoặc filter log. |
| Exception Flow | Không có quyền dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | ADM-BR-025, ADM-BR-047 |

### ADM-UC-022 - View Audit

| Field | Description |
|---|---|
| ID | ADM-UC-022 |
| Description | Admin xem audit phục vụ kiểm tra hành động quản trị. |
| Primary Actor | Admin |
| Trigger | Admin mở audit. |
| Preconditions | Admin có quyền View Audit. |
| Main Flow | 1. Admin chọn audit scope. 2. Hệ thống kiểm tra quyền. 3. Hệ thống hiển thị audit. |
| Alternative Flow | Admin search audit theo actor, action hoặc period. |
| Exception Flow | Không có quyền dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu audit. |
| Business Rules | ADM-BR-026, ADM-BR-048 |

### ADM-UC-023 - View Configuration

| Field | Description |
|---|---|
| ID | ADM-UC-023 |
| Description | Admin xem cấu hình hệ thống thuộc phạm vi quản trị. |
| Primary Actor | Admin |
| Trigger | Admin mở configuration. |
| Preconditions | Admin có quyền View Configuration. |
| Main Flow | 1. Admin yêu cầu xem configuration. 2. Hệ thống kiểm tra quyền. 3. Hệ thống hiển thị cấu hình được phép. |
| Alternative Flow | Admin chỉ xem cấu hình nhạy cảm ở mức giới hạn nếu policy quy định. |
| Exception Flow | Không có quyền dẫn đến từ chối. |
| Postconditions | Không thay đổi cấu hình. |
| Business Rules | ADM-BR-006 |

### ADM-UC-024 - Update Configuration

| Field | Description |
|---|---|
| ID | ADM-UC-024 |
| Description | Admin cập nhật cấu hình hệ thống hợp lệ. |
| Primary Actor | Admin |
| Trigger | Admin gửi thay đổi cấu hình. |
| Preconditions | Admin có quyền Update Configuration. |
| Main Flow | 1. Admin nhập giá trị mới. 2. Hệ thống validate configuration. 3. Admin xác nhận. 4. Hệ thống cập nhật. 5. Hệ thống ghi audit. |
| Alternative Flow | Cấu hình nhạy cảm yêu cầu xác nhận bổ sung. |
| Exception Flow | Giá trị không hợp lệ hoặc không có quyền dẫn đến từ chối. |
| Postconditions | Cấu hình được cập nhật nếu hợp lệ. |
| Business Rules | ADM-BR-024, ADM-BR-036 |

### ADM-UC-025 - View System Dashboard

| Field | Description |
|---|---|
| ID | ADM-UC-025 |
| Description | Admin xem System Dashboard. |
| Primary Actor | Admin |
| Trigger | Admin mở dashboard quản trị. |
| Preconditions | Admin có quyền View System Dashboard. |
| Main Flow | 1. Admin chọn period/filter. 2. Hệ thống kiểm tra quyền. 3. Hệ thống hiển thị thông tin vận hành. |
| Alternative Flow | Không có dữ liệu, hệ thống hiển thị trạng thái rỗng. |
| Exception Flow | Không có quyền dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | ADM-BR-039, ADM-BR-049 |

### ADM-UC-026 - Manage Role

| Field | Description |
|---|---|
| ID | ADM-UC-026 |
| Description | Admin quản lý Role theo policy. |
| Primary Actor | Admin |
| Trigger | Admin thực hiện thay đổi Role. |
| Preconditions | Admin có quyền Manage Role. |
| Main Flow | 1. Admin chọn Role. 2. Admin nhập thay đổi. 3. Hệ thống validate Role. 4. Admin xác nhận. 5. Hệ thống cập nhật. 6. Hệ thống audit. |
| Alternative Flow | Admin chỉ xem Role assignment. |
| Exception Flow | Role trùng tên hoặc đang được sử dụng không thể thay đổi theo policy dẫn đến từ chối. |
| Postconditions | Role được cập nhật nếu hợp lệ. |
| Business Rules | ADM-BR-020, ADM-BR-027, ADM-BR-029 |

### ADM-UC-027 - Manage Permission

| Field | Description |
|---|---|
| ID | ADM-UC-027 |
| Description | Admin quản lý Permission theo policy. |
| Primary Actor | Admin |
| Trigger | Admin thực hiện thay đổi Permission. |
| Preconditions | Admin có quyền Manage Permission. |
| Main Flow | 1. Admin chọn Permission. 2. Admin nhập thay đổi. 3. Hệ thống validate. 4. Admin xác nhận. 5. Hệ thống cập nhật. 6. Hệ thống audit. |
| Alternative Flow | Admin chỉ xem Permission assignment. |
| Exception Flow | Permission sai phạm vi hoặc thay đổi vi phạm governance dẫn đến từ chối. |
| Postconditions | Permission được cập nhật nếu hợp lệ. |
| Business Rules | ADM-BR-007, ADM-BR-021, ADM-BR-028 |

### ADM-UC-028 - Assign Role

| Field | Description |
|---|---|
| ID | ADM-UC-028 |
| Description | Admin gán Role cho tài khoản hợp lệ. |
| Primary Actor | Admin |
| Trigger | Admin chọn assign Role. |
| Preconditions | Role và tài khoản mục tiêu hợp lệ. |
| Main Flow | 1. Admin chọn tài khoản. 2. Admin chọn Role. 3. Hệ thống kiểm tra. 4. Admin xác nhận. 5. Hệ thống gán Role. 6. Hệ thống audit. |
| Alternative Flow | Tài khoản đã có Role, hệ thống thông báo không có thay đổi. |
| Exception Flow | Role không hợp lệ hoặc actor không có quyền dẫn đến từ chối. |
| Postconditions | Tài khoản có Role được gán. |
| Business Rules | ADM-BR-008, ADM-BR-020 |

### ADM-UC-029 - Revoke Role

| Field | Description |
|---|---|
| ID | ADM-UC-029 |
| Description | Admin thu hồi Role khỏi tài khoản. |
| Primary Actor | Admin |
| Trigger | Admin chọn revoke Role. |
| Preconditions | Tài khoản đang có Role cần thu hồi. |
| Main Flow | 1. Admin chọn tài khoản. 2. Admin chọn Role. 3. Hệ thống kiểm tra governance. 4. Admin xác nhận. 5. Hệ thống thu hồi Role. 6. Hệ thống audit. |
| Alternative Flow | Tài khoản không có Role, hệ thống thông báo không có thay đổi. |
| Exception Flow | Thu hồi làm mất Admin cuối cùng dẫn đến từ chối hoặc quy trình bảo vệ. |
| Postconditions | Role không còn hiệu lực với tài khoản theo policy. |
| Business Rules | ADM-BR-008, ADM-BR-020, ADM-BR-031 |

### ADM-UC-030 - Assign Permission

| Field | Description |
|---|---|
| ID | ADM-UC-030 |
| Description | Admin gán Permission theo policy. |
| Primary Actor | Admin |
| Trigger | Admin chọn assign Permission. |
| Preconditions | Permission và phạm vi gán hợp lệ. |
| Main Flow | 1. Admin chọn Permission. 2. Admin chọn phạm vi gán. 3. Hệ thống validate. 4. Admin xác nhận. 5. Hệ thống gán Permission. 6. Hệ thống audit. |
| Alternative Flow | Permission đã được gán, hệ thống thông báo không có thay đổi. |
| Exception Flow | Permission sai phạm vi dẫn đến từ chối. |
| Postconditions | Permission được gán nếu hợp lệ. |
| Business Rules | ADM-BR-007, ADM-BR-021, ADM-BR-028 |

### ADM-UC-031 - Revoke Permission

| Field | Description |
|---|---|
| ID | ADM-UC-031 |
| Description | Admin thu hồi Permission theo policy. |
| Primary Actor | Admin |
| Trigger | Admin chọn revoke Permission. |
| Preconditions | Permission đang được gán. |
| Main Flow | 1. Admin chọn Permission. 2. Hệ thống kiểm tra tác động. 3. Admin xác nhận. 4. Hệ thống thu hồi Permission. 5. Hệ thống audit. |
| Alternative Flow | Permission không được gán, hệ thống thông báo không có thay đổi. |
| Exception Flow | Thu hồi làm mất khả năng quản trị tối thiểu dẫn đến từ chối. |
| Postconditions | Permission bị thu hồi nếu hợp lệ. |
| Business Rules | ADM-BR-007, ADM-BR-021, ADM-BR-030 |

### ADM-UC-032 - Broadcast Announcement

| Field | Description |
|---|---|
| ID | ADM-UC-032 |
| Description | Admin phát thông báo hệ thống nếu policy cho phép. |
| Primary Actor | Admin |
| Trigger | Admin tạo announcement. |
| Preconditions | Announcement Policy được phê duyệt và Admin có quyền. |
| Main Flow | 1. Admin nhập nội dung thông báo. 2. Admin chọn phạm vi nhận. 3. Hệ thống validate. 4. Admin xác nhận. 5. Hệ thống phát announcement. |
| Alternative Flow | Announcement được lên lịch nếu policy cho phép. |
| Exception Flow | Nội dung hoặc phạm vi không hợp lệ dẫn đến từ chối. |
| Postconditions | Announcement được phát theo policy. |
| Business Rules | ADM-BR-037 |

### ADM-UC-033 - View Maintenance Status

| Field | Description |
|---|---|
| ID | ADM-UC-033 |
| Description | Admin xem trạng thái maintenance nếu policy cho phép. |
| Primary Actor | Admin |
| Trigger | Admin mở maintenance status. |
| Preconditions | Maintenance Policy thuộc phạm vi được phê duyệt. |
| Main Flow | 1. Admin yêu cầu xem maintenance status. 2. Hệ thống kiểm tra quyền. 3. Hệ thống hiển thị trạng thái. |
| Alternative Flow | Không có maintenance active, hệ thống hiển thị trạng thái bình thường. |
| Exception Flow | Không có quyền dẫn đến từ chối. |
| Postconditions | Không thay đổi trạng thái. |
| Business Rules | ADM-BR-038 |

## 12. User Stories

| Story ID | User Story |
|---|---|
| ADM-US-001 | As an Admin, I want to view users so that I can support account governance. |
| ADM-US-002 | As an Admin, I want to search and filter users so that I can locate accounts efficiently. |
| ADM-US-003 | As an Admin, I want to deactivate and reactivate users so that account access can be governed. |
| ADM-US-004 | As a Staff member, I want to receive tickets so that I can start support handling. |
| ADM-US-005 | As a Staff member, I want to update tickets so that support progress is visible. |
| ADM-US-006 | As a Staff member, I want to escalate tickets so that issues beyond my scope reach Admin. |
| ADM-US-007 | As a Staff member, I want to resolve tickets so that user issues are handled. |
| ADM-US-008 | As an Admin, I want to manage Staff so that operational responsibilities are controlled. |
| ADM-US-009 | As an Admin, I want to manage Role so that access responsibilities remain governed. |
| ADM-US-010 | As an Admin, I want to manage Permission so that access rights remain aligned with policy. |
| ADM-US-011 | As an Admin, I want to assign and revoke roles so that users and staff have appropriate access. |
| ADM-US-012 | As an Admin, I want to assign and revoke permissions so that excessive access can be removed. |
| ADM-US-013 | As a Staff member, I want to temporarily lock accounts within scope so that urgent access risks can be controlled. |
| ADM-US-014 | As an Admin, I want to unlock accounts so that legitimate access can be restored. |
| ADM-US-015 | As an Admin, I want to view audit so that sensitive administrative actions can be reviewed. |
| ADM-US-016 | As a Staff member, I want to view permitted activity logs so that I can support users effectively. |
| ADM-US-017 | As an Admin, I want to update configuration so that system behavior can follow approved policy. |
| ADM-US-018 | As an Admin, I want to view System Dashboard so that I can monitor operational status. |
| ADM-US-019 | As an Admin, I want to broadcast announcements when approved so that operational messages can reach users. |
| ADM-US-020 | As an Admin, I want to view maintenance status so that I can understand system operating condition. |

## 13. Acceptance Criteria

| AC ID | Given | When | Then |
|---|---|---|---|
| ADM-AC-001 | Given Admin has Manage User permission | When Admin views User | Then the system displays permitted User information. |
| ADM-AC-002 | Given Admin searches User with valid criteria | When matching users exist | Then the system displays matching users. |
| ADM-AC-003 | Given Admin filters User with valid filter | When matching users exist | Then the system displays filtered users. |
| ADM-AC-004 | Given Admin updates User information within scope | When Admin confirms | Then the system updates information and audits if required. |
| ADM-AC-005 | Given Admin deactivates an eligible User | When Admin confirms | Then the account becomes deactivated and audit is recorded. |
| ADM-AC-006 | Given Admin reactivates an eligible User | When Admin confirms | Then the account becomes active according to policy and audit is recorded. |
| ADM-AC-007 | Given Staff has lock permission and target is within scope | When Staff confirms temporary lock | Then the account becomes temporarily locked and audit is recorded. |
| ADM-AC-008 | Given Staff attempts to lock self | When policy does not allow | Then the system rejects the action. |
| ADM-AC-009 | Given Admin unlocks a locked account | When Admin confirms | Then the account is unlocked if no other restriction applies. |
| ADM-AC-010 | Given Admin views Staff | When Admin has permission | Then the system displays Staff information. |
| ADM-AC-011 | Given Admin manages Staff within policy | When Admin confirms | Then Staff state or assignment is updated. |
| ADM-AC-012 | Given actor creates ticket with title and content | When validation passes | Then the system creates ticket. |
| ADM-AC-013 | Given ticket title or content is missing | When actor submits ticket | Then the system rejects ticket creation. |
| ADM-AC-014 | Given Staff has queue access | When Staff receives ticket | Then ticket status becomes Received or equivalent. |
| ADM-AC-015 | Given actor assigns ticket to valid Staff | When assignment is confirmed | Then ticket is assigned. |
| ADM-AC-016 | Given Staff updates assigned ticket | When update is valid | Then ticket is updated. |
| ADM-AC-017 | Given Staff escalates ticket with required reason | When escalation is confirmed | Then ticket becomes Escalated. |
| ADM-AC-018 | Given Staff resolves ticket with required resolution | When resolution is submitted | Then ticket becomes Resolved. |
| ADM-AC-019 | Given ticket meets closure conditions | When actor closes ticket | Then ticket becomes Closed. |
| ADM-AC-020 | Given closed ticket meets reopen policy | When actor reopens ticket | Then ticket becomes Reopened or policy-defined status. |
| ADM-AC-021 | Given actor searches ticket with valid criteria | When matching tickets exist | Then the system displays matching tickets within scope. |
| ADM-AC-022 | Given actor filters ticket with valid filters | When matching tickets exist | Then the system displays filtered tickets. |
| ADM-AC-023 | Given Staff has View Activity Log permission | When Staff views log | Then the system displays permitted logs. |
| ADM-AC-024 | Given Admin has View Audit permission | When Admin views audit | Then the system displays audit information. |
| ADM-AC-025 | Given Admin updates configuration with valid value | When Admin confirms | Then configuration is updated and audit is recorded. |
| ADM-AC-026 | Given configuration value is invalid | When Admin submits update | Then the system rejects the update. |
| ADM-AC-027 | Given Admin views System Dashboard | When Admin has permission | Then operational summary is displayed. |
| ADM-AC-028 | Given Admin assigns Role to eligible account | When Admin confirms | Then Role is assigned and audit is recorded. |
| ADM-AC-029 | Given Admin revokes Role and governance is not violated | When Admin confirms | Then Role is revoked and audit is recorded. |
| ADM-AC-030 | Given Admin assigns Permission within policy | When Admin confirms | Then Permission is assigned and audit is recorded. |
| ADM-AC-031 | Given Admin revokes Permission without violating governance | When Admin confirms | Then Permission is revoked and audit is recorded. |
| ADM-AC-032 | Given Staff attempts to change Role | When action is submitted | Then the system rejects the action. |
| ADM-AC-033 | Given Staff attempts to change Permission | When action is submitted | Then the system rejects the action. |
| ADM-AC-034 | Given Announcement Policy is approved | When Admin broadcasts valid announcement | Then the announcement is published according to policy. |
| ADM-AC-035 | Given Admin views Maintenance Status | When policy allows | Then the system displays maintenance status. |
| ADM-AC-036 | Given any rejected administrative action | When validation fails | Then no administrative state is changed. |

## 14. Business Scenarios

| Scenario | Description | Expected Result |
|---|---|---|
| User gửi Ticket | User tạo ticket với tiêu đề và nội dung. | Ticket được tạo ở trạng thái New nếu hợp lệ. |
| Staff nhận Ticket | Staff mở support queue và receive ticket. | Ticket chuyển sang Received hoặc trạng thái tương đương. |
| Staff chuyển Ticket | Staff hoặc Admin assign ticket cho Staff phù hợp. | Ticket có assignee hợp lệ. |
| Staff xử lý Ticket | Staff cập nhật ticket, thêm comment và resolution. | Ticket chuyển sang Resolved nếu đủ điều kiện. |
| Staff khóa tài khoản | Staff khóa tạm thời User trong phạm vi quyền và có lý do hợp lệ. | Tài khoản locked và action được audit. |
| Admin mở khóa | Admin unlock tài khoản đang temporary lock. | Tài khoản được unlock nếu không còn restriction khác. |
| Admin cập nhật cấu hình | Admin thay đổi cấu hình hợp lệ. | Configuration cập nhật và audit được ghi nhận. |
| Admin thay đổi Permission | Admin assign hoặc revoke Permission theo policy. | Permission thay đổi và audit được ghi nhận. |
| Admin xem Audit | Admin mở audit để kiểm tra hành động quản trị. | Audit hiển thị trong phạm vi quyền. |
| Admin xem Dashboard | Admin mở System Dashboard. | Dashboard hiển thị summary vận hành. |
| Ticket bị đóng nhầm | Staff hoặc Admin reopen ticket theo policy. | Ticket chuyển sang Reopened hoặc trạng thái phù hợp. |
| Ticket vượt phạm vi Staff | Staff escalate ticket. | Ticket vào trạng thái Escalated và Admin có thể review. |

## 15. Edge Cases

| Edge Case ID | Scenario | Expected Business Handling |
|---|---|---|
| ADM-EC-001 | Staff tự khóa chính mình. | Hệ thống từ chối nếu policy không cho phép. |
| ADM-EC-002 | Staff khóa Admin. | Hệ thống từ chối nếu không có chính sách đặc biệt. |
| ADM-EC-003 | Admin tự thu hồi quyền Admin. | Hệ thống chặn hoặc yêu cầu quy trình bảo vệ nếu làm mất quyền quản trị cần thiết. |
| ADM-EC-004 | Admin cuối cùng bị deactivate. | Hệ thống chặn hoặc yêu cầu quy trình bảo vệ. |
| ADM-EC-005 | Ticket không có Staff xử lý. | Ticket nằm trong queue chưa phân công hoặc trạng thái tương đương. |
| ADM-EC-006 | Ticket bị đóng nhầm. | Ticket chỉ được reopen theo policy. |
| ADM-EC-007 | User bị khóa khi đang đăng nhập. | Trạng thái lock phải được áp dụng theo IAM policy ở hành động tiếp theo. |
| ADM-EC-008 | Role bị xóa khi vẫn còn người dùng sử dụng. | Hệ thống từ chối hoặc yêu cầu policy xử lý Role đang được gán. |
| ADM-EC-009 | Permission bị thu hồi trong phiên làm việc. | Effective permission phải thay đổi theo IAM policy. |
| ADM-EC-010 | Hệ thống vào Maintenance Mode. | Actor được thông báo theo Maintenance Policy. |
| ADM-EC-011 | Ticket escalated nhưng không có Admin xử lý. | Ticket nằm trong escalation queue và cần cảnh báo vận hành theo policy. |
| ADM-EC-012 | Staff bị remove khỏi ticket đang xử lý. | Staff không còn quyền cập nhật ticket đó sau khi removal có hiệu lực. |
| ADM-EC-013 | Configuration update không hợp lệ. | Hệ thống từ chối và không thay đổi configuration. |
| ADM-EC-014 | Permission được gán sai phạm vi. | Hệ thống từ chối. |
| ADM-EC-015 | Role trùng tên. | Hệ thống từ chối nếu policy yêu cầu tên duy nhất. |
| ADM-EC-016 | Ticket thiếu tiêu đề. | Hệ thống từ chối tạo ticket. |
| ADM-EC-017 | Ticket thiếu nội dung. | Hệ thống từ chối tạo ticket. |
| ADM-EC-018 | Ticket priority không hợp lệ. | Hệ thống từ chối cập nhật priority. |
| ADM-EC-019 | Ticket category không hợp lệ. | Hệ thống từ chối cập nhật category. |
| ADM-EC-020 | Staff cố xem Audit quản trị. | Hệ thống từ chối nếu Staff không có quyền audit. |
| ADM-EC-021 | Admin export hoặc xem dữ liệu cá nhân ngoài policy thông qua dashboard quản trị. | Hệ thống không hiển thị dữ liệu chi tiết ngoài phạm vi được phép. |
| ADM-EC-022 | Activity Log không có dữ liệu trong period. | Hệ thống hiển thị trạng thái không có dữ liệu. |
| ADM-EC-023 | Audit quá nhiều kết quả. | Hệ thống xử lý theo Audit Policy về phạm vi tìm kiếm. |
| ADM-EC-024 | Announcement gửi nhầm phạm vi. | Hệ thống phải validate phạm vi trước khi phát. |
| ADM-EC-025 | Maintenance Status bị thay đổi bởi actor không có quyền. | Hệ thống từ chối. |

## 16. Validation Rules

| Validation Rule ID | Rule |
|---|---|
| ADM-VR-001 | Ticket bắt buộc có tiêu đề. |
| ADM-VR-002 | Ticket bắt buộc có nội dung. |
| ADM-VR-003 | Ticket status phải thuộc tập trạng thái được phê duyệt. |
| ADM-VR-004 | Ticket priority phải thuộc tập giá trị được phê duyệt nếu được sử dụng. |
| ADM-VR-005 | Ticket category phải hợp lệ nếu được sử dụng. |
| ADM-VR-006 | Ticket assignee phải là Staff hợp lệ hoặc queue hợp lệ. |
| ADM-VR-007 | Ticket close chỉ được thực hiện khi đáp ứng closure condition. |
| ADM-VR-008 | Ticket reopen chỉ được thực hiện nếu policy cho phép. |
| ADM-VR-009 | Role không được trùng tên nếu policy yêu cầu tên duy nhất. |
| ADM-VR-010 | Role phải có ý nghĩa nghiệp vụ rõ ràng. |
| ADM-VR-011 | Permission phải có phạm vi nghiệp vụ rõ ràng. |
| ADM-VR-012 | Permission không được gán sai phạm vi. |
| ADM-VR-013 | Assign Role phải kiểm tra account target hợp lệ. |
| ADM-VR-014 | Revoke Role phải kiểm tra tác động governance. |
| ADM-VR-015 | Assign Permission phải kiểm tra Permission Policy. |
| ADM-VR-016 | Revoke Permission phải kiểm tra tác động vận hành. |
| ADM-VR-017 | Staff không được thay đổi Role. |
| ADM-VR-018 | Staff không được thay đổi Permission. |
| ADM-VR-019 | Temporary Lock phải có target account hợp lệ. |
| ADM-VR-020 | Temporary Lock phải kiểm tra actor có quyền khóa target. |
| ADM-VR-021 | Unlock phải kiểm tra target account đang locked. |
| ADM-VR-022 | Deactivate phải kiểm tra target account đủ điều kiện. |
| ADM-VR-023 | Reactivate phải kiểm tra target account đủ điều kiện. |
| ADM-VR-024 | Configuration update phải có giá trị hợp lệ theo policy. |
| ADM-VR-025 | Announcement phải có nội dung và phạm vi nhận hợp lệ nếu được sử dụng. |
| ADM-VR-026 | Maintenance Status chỉ được xem hoặc thay đổi bởi actor có quyền. |
| ADM-VR-027 | Search và Filter phải dùng tiêu chí hợp lệ. |
| ADM-VR-028 | Hành động bị từ chối không được thay đổi trạng thái nghiệp vụ. |

## 17. Business Reports

| Report Type | Purpose | Main Content |
|---|---|---|
| Ticket Report | Giúp Admin và Staff hiểu tình hình ticket. | Số ticket theo status, priority, category, assignee, period, reopened ticket. |
| Support Performance Report | Đánh giá hiệu quả hỗ trợ vận hành. | Ticket received, resolved, closed, escalated, pending, average handling indicators nếu policy định nghĩa. |
| User Activity Report | Cung cấp góc nhìn vận hành về hoạt động tài khoản. | Active users, locked accounts, deactivated accounts, recent account status changes. |
| Audit Report | Hỗ trợ kiểm tra hành động quản trị nhạy cảm. | Role changes, permission changes, account locks, unlocks, configuration updates. |
| System Operation Report | Cung cấp thông tin vận hành tổng quan. | Support queue, maintenance status, announcement status, operational alerts nếu policy cho phép. |
| Role Assignment Report | Theo dõi Role được gán cho tài khoản. | Accounts by role, recent role assignment, role revocation. |
| Permission Change Report | Theo dõi thay đổi Permission. | Permission assignment, revocation, actor thực hiện, thời điểm ở mức nghiệp vụ. |
| Configuration Change Report | Theo dõi thay đổi cấu hình. | Configuration changed, actor, time, reason nếu policy yêu cầu. |
| System Dashboard Summary | Tóm tắt dashboard quản trị trong một period. | Ticket summary, account status summary, audit highlights, operational status. |

## 18. Administration Dashboard Requirements

### 18.1 Purpose

Administration Dashboard cung cấp góc nhìn tổng quan về tình trạng vận hành hệ thống cho Admin. Dashboard này phục vụ quản trị và hỗ trợ, không phải dashboard nghiệp vụ cá nhân của User.

### 18.2 Target Users

Đối tượng sử dụng chính là Admin. Staff có thể được xem một phần thông tin vận hành nếu policy cho phép, ví dụ support queue hoặc ticket summary thuộc phạm vi xử lý.

### 18.3 Information to Display

| Information Group | Description |
|---|---|
| Ticket Summary | Số lượng ticket theo status, priority, category và assignee. |
| Support Queue | Ticket mới, ticket chưa phân công, ticket escalated, ticket quá hạn nếu policy có SLA. |
| Account Status Summary | Số tài khoản active, locked, deactivated và recently changed. |
| Staff Workload | Số ticket đang được phân công cho Staff nếu policy cho phép. |
| Role and Permission Highlights | Thay đổi Role/Permission gần đây hoặc cảnh báo governance nếu policy định nghĩa. |
| Audit Highlights | Hành động quản trị nhạy cảm gần đây. |
| Configuration Status | Cấu hình quan trọng và thay đổi gần đây nếu policy cho phép. |
| Maintenance Status | Trạng thái bảo trì nếu Maintenance Policy được phê duyệt. |
| Announcement Status | Thông báo đang active hoặc scheduled nếu Announcement Policy được phê duyệt. |

### 18.4 Filters

Dashboard quản trị có thể hỗ trợ filter theo:

- Period.
- Ticket status.
- Ticket priority.
- Ticket category.
- Staff assignee.
- Account status.
- Role.
- Permission.
- Audit action type.

### 18.5 Drill-down

Drill-down nếu được phê duyệt có thể cho phép Admin đi từ summary đến:

- Ticket list.
- Ticket detail.
- User account detail.
- Staff workload detail.
- Audit detail.
- Configuration change detail.
- Role assignment detail.

Drill-down chỉ hiển thị thông tin trong phạm vi quyền của actor.

## 19. Risks

### 19.1 Business Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Quyền Staff quá rộng | Staff có thể can thiệp vượt trách nhiệm. | Rủi ro dữ liệu và governance. | Áp dụng least privilege và policy rõ ràng. |
| Admin hiểu quyền quản trị là quyền không giới hạn | Admin có thể can thiệp dữ liệu cá nhân không cần thiết. | Giảm niềm tin User. | Tách quyền quản trị vận hành khỏi dữ liệu cá nhân. |
| Ticket process không rõ | Staff xử lý ticket không nhất quán. | Giảm chất lượng support. | Định nghĩa Ticket Policy và Support Policy. |

### 19.2 Operational Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Ticket tồn đọng | Ticket không có Staff xử lý hoặc không được escalate. | Giảm hài lòng User. | Support queue và escalation dashboard cần rõ. |
| Configuration thay đổi sai | Cấu hình không hợp lệ ảnh hưởng vận hành. | Gián đoạn dịch vụ. | Validate configuration và audit. |
| Maintenance không được thông báo | User không biết hệ thống đang bảo trì. | Tăng ticket và giảm trải nghiệm. | Announcement và Maintenance Policy. |

### 19.3 Security Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Role/Permission bị thay đổi sai | Actor có quyền không phù hợp. | Truy cập trái phép hoặc mất quyền vận hành. | Audit và governance validation. |
| Admin cuối cùng mất quyền | Hệ thống mất khả năng quản trị. | Rủi ro vận hành nghiêm trọng. | Self-protection rule. |
| Activity Log bị xem sai phạm vi | Log có thể chứa thông tin nhạy cảm. | Rủi ro quyền riêng tư. | Access control và scope filtering. |

### 19.4 Support Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Ticket bị đóng nhầm | Vấn đề chưa được xử lý nhưng ticket bị closed. | User phải tạo lại yêu cầu. | Reopen policy rõ ràng. |
| Ticket bị escalate quá nhiều | Staff không xử lý được hoặc policy mơ hồ. | Tăng tải Admin. | Định nghĩa escalation criteria. |
| Ticket thiếu thông tin | Staff không đủ dữ liệu xử lý. | Chậm resolution. | Validation và yêu cầu thông tin tối thiểu. |

### 19.5 Governance Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Audit thiếu thông tin | Không thể truy vết hành động quản trị. | Giảm accountability. | Audit Policy xác định thông tin tối thiểu. |
| Permission scope không rõ | Quyền được gán sai phạm vi. | Rủi ro bảo mật và vận hành. | Permission catalog và validation. |
| Role không được rà soát | Role lỗi thời vẫn được dùng. | Cấp quyền không phù hợp. | Periodic access review là cải tiến đề xuất. |

## 20. Open Questions

| Question ID | Open Question | Impact Area |
|---|---|---|
| ADM-OQ-001 | Ticket status chính thức gồm những trạng thái nào? | Ticket Policy |
| ADM-OQ-002 | Ticket priority chính thức gồm những mức nào? | Ticket Policy |
| ADM-OQ-003 | Ticket category chính thức gồm những nhóm nào? | Support Policy |
| ADM-OQ-004 | User có được tự tạo ticket không, hay ticket chỉ do Staff tạo? | Ticket Creation |
| ADM-OQ-005 | Staff có được tự assign ticket cho chính mình không? | Assignment |
| ADM-OQ-006 | Ticket close có cần User xác nhận không? | Closure |
| ADM-OQ-007 | Ticket closed có thể reopen trong bao lâu? | Reopen |
| ADM-OQ-008 | Staff có được unlock account không, hay chỉ Admin? | Lock Policy |
| ADM-OQ-009 | Temporary lock có thời hạn mặc định không? | Lock Policy |
| ADM-OQ-010 | Staff có được khóa Staff khác không? | Lock Policy |
| ADM-OQ-011 | Admin có được tự thu hồi quyền Admin của chính mình không? | Governance |
| ADM-OQ-012 | Hệ thống có yêu cầu luôn tồn tại ít nhất một Admin active không? | Governance |
| ADM-OQ-013 | Role đang được gán có được xóa không? | Role Policy |
| ADM-OQ-014 | Permission đang được dùng có được thu hồi ngay không? | Permission Policy |
| ADM-OQ-015 | Activity Log bao gồm những loại hoạt động nào? | Activity Log |
| ADM-OQ-016 | Audit cần lưu những thông tin tối thiểu nào ở mức nghiệp vụ? | Audit |
| ADM-OQ-017 | Configuration nào được phép Admin cập nhật? | Configuration |
| ADM-OQ-018 | Announcement có thuộc phạm vi release hiện tại không? | Announcement |
| ADM-OQ-019 | Maintenance Mode có thuộc phạm vi release hiện tại không? | Maintenance |
| ADM-OQ-020 | System Dashboard có cho Staff xem một phần không? | Dashboard Access |

## 21. Suggested Improvements

| Improvement ID | Suggested Improvement | Business Rationale |
|---|---|---|
| ADM-SI-001 | Thiết lập SLA cho ticket theo priority. | Giúp support có tiêu chuẩn xử lý rõ ràng. |
| ADM-SI-002 | Thiết lập periodic access review cho Staff và Admin. | Giảm rủi ro quyền lỗi thời hoặc quá rộng. |
| ADM-SI-003 | Bắt buộc lý do cho lock, unlock, deactivate, reactivate và permission change. | Tăng accountability. |
| ADM-SI-004 | Thiết lập approval bổ sung cho thay đổi Permission nhạy cảm. | Giảm rủi ro thay đổi quyền sai. |
| ADM-SI-005 | Xây dựng permission catalog có mô tả nghiệp vụ rõ ràng. | Giúp Admin hiểu quyền trước khi gán. |
| ADM-SI-006 | Cảnh báo ticket chưa có Staff xử lý quá lâu. | Giảm ticket tồn đọng. |
| ADM-SI-007 | Cảnh báo khi Admin cuối cùng có nguy cơ mất quyền. | Bảo vệ khả năng quản trị. |
| ADM-SI-008 | Phân loại audit event theo mức độ rủi ro. | Hỗ trợ ưu tiên kiểm tra. |
| ADM-SI-009 | Cung cấp support knowledge base trong tương lai. | Giảm ticket lặp lại và tăng chất lượng support. |
| ADM-SI-010 | Cung cấp dashboard workload cho Staff. | Giúp phân bổ ticket cân bằng hơn. |
| ADM-SI-011 | Chuẩn hóa closure reason và escalation reason. | Tăng chất lượng báo cáo support. |
| ADM-SI-012 | Thiết lập maintenance announcement template. | Giúp thông báo vận hành nhất quán. |

## Appendix A. Traceability Summary

| Source | Related ADM Content |
|---|---|
| Volume 1 - Actors | Staff và Admin là actor chính của Administration & Support. |
| Volume 1 - Administration and Support Scope | Administration, Support, Audit và System Dashboard được triển khai thành SRS riêng. |
| Volume 2 - Identity & Authorization | Role, Permission, Temporary Lock, Unlock và Account Status được kế thừa. |
| Previous Business Principles | Privacy Respect, Operational Accountability và Scope Discipline định hướng policy quản trị. |

## Appendix B. ADM Glossary

| Term | Definition |
|---|---|
| Activity Log | Thông tin hoạt động phục vụ hỗ trợ và vận hành. |
| Admin | Actor quản trị hệ thống theo phạm vi quyền được cấp. |
| Audit | Bản ghi phục vụ kiểm tra và trách nhiệm giải trình cho hành động nhạy cảm. |
| Configuration | Thiết lập hệ thống ở mức nghiệp vụ. |
| Maintenance Mode | Trạng thái bảo trì nếu được policy phê duyệt. |
| Permission | Quyền thực hiện hành động hoặc truy cập khu vực chức năng. |
| Role | Nhóm Permission đại diện cho trách nhiệm hoặc phạm vi truy cập. |
| Staff | Actor vận hành xử lý support ticket và hỗ trợ User theo quyền. |
| Support Ticket | Yêu cầu hỗ trợ được ghi nhận để xử lý theo workflow. |
| Temporary Lock | Khóa tài khoản tạm thời theo policy. |
| Ticket Priority | Mức độ ưu tiên xử lý ticket. |
| Ticket Status | Trạng thái của ticket trong vòng đời hỗ trợ. |

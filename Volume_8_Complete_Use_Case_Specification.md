# LifeBalance
# Volume 8 - Complete Use Case Specification

## 1. Document Overview

### 1.1 Purpose

Tài liệu này tổng hợp và đặc tả toàn bộ Use Case của hệ thống LifeBalance ở mức nghiệp vụ. Tài liệu kế thừa các yêu cầu, thuật ngữ, business rule, actor và phạm vi từ Volume 1 đến Volume 7. Mục đích chính là cung cấp một nguồn tham chiếu thống nhất để Business Analyst, Developer, QA, Product Owner và stakeholder có thể hiểu đầy đủ hành vi kỳ vọng của hệ thống theo góc nhìn use case.

Tài liệu này không thay thế các volume module đã hoàn thành. Thay vào đó, tài liệu gom các Use Case đã được xác định trong từng volume, chuẩn hóa đặc tả, bổ sung quan hệ giữa Use Case, tổng hợp workflow, precondition, postcondition, exception, edge case và traceability.

### 1.2 Scope

Phạm vi tài liệu bao gồm các nhóm Use Case:

- Identity & Authorization.
- Resource Capital Management.
- Task & Timeline Management.
- Tracking, Evaluation & Reporting.
- Administration & Support.
- Cross-cutting Business Requirements.

Volume 7 không định nghĩa Functional Requirement dạng `FR`. Vì vậy, các yêu cầu dùng chung từ Volume 7 được ánh xạ thông qua Global Business Rules, Validation Rules, Business Policies, Audit Requirements, Activity History Requirements và Permission Matrix.

### 1.3 Objectives

| Objective ID | Objective | Description |
|---|---|---|
| UC-OBJ-001 | Tổng hợp Use Case toàn hệ thống | Ghi nhận toàn bộ Use Case từ các volume module. |
| UC-OBJ-002 | Chuẩn hóa đặc tả Use Case | Áp dụng một cấu trúc đặc tả thống nhất theo IEEE 29148, BABOK và UML Use Case Specification. |
| UC-OBJ-003 | Bảo đảm traceability | Ánh xạ Functional Requirement, Business Rule, User Story và Acceptance Criteria về Use Case tương ứng. |
| UC-OBJ-004 | Hỗ trợ kiểm thử | Cung cấp luồng chính, luồng thay thế, luồng ngoại lệ và business exception để xây dựng test case. |
| UC-OBJ-005 | Hỗ trợ quản trị phạm vi | Xác định actor, module, priority và quan hệ giữa các Use Case. |

### 1.4 Audience

| Audience | Usage |
|---|---|
| Business Analyst | Dùng làm tài liệu kiểm soát hành vi nghiệp vụ và traceability. |
| Product Owner | Dùng để xác nhận phạm vi và ưu tiên Use Case. |
| Developer | Dùng để hiểu hành vi nghiệp vụ cần triển khai. |
| QA/Test Engineer | Dùng để xây dựng test scenario, test case và acceptance test. |
| UI/UX Designer | Dùng để hiểu mục tiêu người dùng và luồng nghiệp vụ. |
| Academic Reviewer | Dùng để đánh giá tính đầy đủ của mô hình Use Case. |

## 2. Use Case Catalog

### 2.1 Identity & Authorization

| Use Case ID | Use Case Name | Primary Actor | Brief Description | Priority | Module |
|---|---|---|---|---|---|
| IAM-UC-001 | View Landing Page | Guest | Guest xem thông tin công khai. | Medium | Identity |
| IAM-UC-002 | Register Account | Guest | Guest đăng ký tài khoản. | Critical | Identity |
| IAM-UC-003 | Login | User, Staff, Admin | Actor đăng nhập. | Critical | Identity |
| IAM-UC-004 | Logout | User, Staff, Admin | Actor kết thúc phiên. | High | Identity |
| IAM-UC-005 | View Own Profile | User, Staff, Admin | Xem hồ sơ cá nhân. | High | Identity |
| IAM-UC-006 | Update Own Profile | User, Staff, Admin | Cập nhật hồ sơ cá nhân. | High | Identity |
| IAM-UC-007 | Change Password | User, Staff, Admin | Đổi mật khẩu. | Critical | Identity |
| IAM-UC-008 | Forgot Password | User, Staff, Admin | Khôi phục mật khẩu. | Critical | Identity |
| IAM-UC-009 | Validate Session and Token | System | Kiểm tra phiên hợp lệ. | Critical | Identity |
| IAM-UC-010 | Validate Authorization | System | Kiểm tra role và permission. | Critical | Identity |
| IAM-UC-011 | Search User | Admin | Admin tìm tài khoản. | High | Identity |
| IAM-UC-012 | View User Detail | Admin | Admin xem chi tiết tài khoản. | High | Identity |
| IAM-UC-013 | Update User Account | Admin | Admin cập nhật tài khoản. | High | Identity |
| IAM-UC-014 | Deactivate User | Admin | Admin vô hiệu hóa tài khoản. | Critical | Identity |
| IAM-UC-015 | Reactivate User | Admin | Admin kích hoạt lại tài khoản. | Critical | Identity |
| IAM-UC-016 | Temporary Lock User | Staff, Admin | Khóa tạm thời tài khoản. | Critical | Identity |
| IAM-UC-017 | Unlock User | Admin | Mở khóa tài khoản. | Critical | Identity |
| IAM-UC-018 | Manage Staff | Admin | Quản lý Staff. | High | Identity |
| IAM-UC-019 | Manage Role | Admin | Quản lý Role. | Critical | Identity |
| IAM-UC-020 | Assign Role | Admin | Gán Role. | Critical | Identity |
| IAM-UC-021 | Revoke Role | Admin | Thu hồi Role. | Critical | Identity |
| IAM-UC-022 | Manage Permission | Admin | Quản lý Permission. | Critical | Identity |
| IAM-UC-023 | Assign Permission | Admin | Gán Permission. | Critical | Identity |
| IAM-UC-024 | Revoke Permission | Admin | Thu hồi Permission. | Critical | Identity |
| IAM-UC-025 | View Audit | Admin, Staff if authorized | Xem audit/log theo quyền. | High | Identity |
| IAM-UC-026 | Authorize System Configuration | Admin | Kiểm tra quyền cấu hình. | Critical | Identity |
| IAM-UC-027 | Handle Unauthorized Access | System | Xử lý chưa xác thực. | Critical | Identity |
| IAM-UC-028 | Handle Forbidden Access | System | Xử lý thiếu quyền. | Critical | Identity |

### 2.2 Resource Capital Management

| Use Case ID | Use Case Name | Primary Actor | Brief Description | Priority | Module |
|---|---|---|---|---|---|
| RCM-UC-001 | Create Capital Cycle | User | Tạo chu kỳ nguồn vốn. | Critical | Resource Capital |
| RCM-UC-002 | Update Capital Cycle | User | Cập nhật chu kỳ. | High | Resource Capital |
| RCM-UC-003 | Activate Capital Cycle | User | Kích hoạt chu kỳ. | Critical | Resource Capital |
| RCM-UC-004 | Close Capital Cycle | User | Đóng chu kỳ. | High | Resource Capital |
| RCM-UC-005 | Reopen Capital Cycle | User | Mở lại chu kỳ nếu policy cho phép. | Medium | Resource Capital |
| RCM-UC-006 | Search Capital Cycle | User | Tìm chu kỳ. | Medium | Resource Capital |
| RCM-UC-007 | View Capital Cycle Detail | User | Xem chi tiết chu kỳ. | High | Resource Capital |
| RCM-UC-008 | Set Time Capital | User | Thiết lập Time Capital. | Critical | Resource Capital |
| RCM-UC-009 | Set Money Capital | User | Thiết lập Money Capital. | Critical | Resource Capital |
| RCM-UC-010 | Adjust Time Capital | User | Điều chỉnh Time Capital. | High | Resource Capital |
| RCM-UC-011 | Adjust Money Capital | User | Điều chỉnh Money Capital. | High | Resource Capital |
| RCM-UC-012 | Allocate Time Capital | User | Phân bổ Time Capital. | Critical | Resource Capital |
| RCM-UC-013 | Allocate Money Capital | User | Phân bổ Money Capital. | Critical | Resource Capital |
| RCM-UC-014 | Reallocate Capital | User | Phân bổ lại nguồn vốn. | High | Resource Capital |
| RCM-UC-015 | Release Allocated Capital | User | Giải phóng nguồn vốn. | High | Resource Capital |
| RCM-UC-016 | Allow Over Allocation | User | Cho phép vượt mức. | High | Resource Capital |
| RCM-UC-017 | Transfer Remaining Capital | User | Chuyển số dư nếu policy cho phép. | Medium | Resource Capital |
| RCM-UC-018 | View Available Capital | User | Xem vốn khả dụng. | High | Resource Capital |
| RCM-UC-019 | View Allocated Capital | User | Xem vốn đã phân bổ. | High | Resource Capital |
| RCM-UC-020 | View Remaining Capital | User | Xem vốn còn lại. | High | Resource Capital |
| RCM-UC-021 | View Capital Summary | User | Xem tóm tắt nguồn vốn. | High | Resource Capital |
| RCM-UC-022 | View Allocation History | User | Xem lịch sử phân bổ. | Medium | Resource Capital |
| RCM-UC-023 | View Adjustment History | User | Xem lịch sử điều chỉnh. | Medium | Resource Capital |
| RCM-UC-024 | Filter Capital History | User | Lọc lịch sử nguồn vốn. | Medium | Resource Capital |
| RCM-UC-025 | Validate Capital Ownership | System | Kiểm tra sở hữu nguồn vốn. | Critical | Resource Capital |
| RCM-UC-026 | Validate Capital Balance | System | Kiểm tra balance. | Critical | Resource Capital |
| RCM-UC-027 | View Authorized Capital Summary | Staff, Admin | Xem summary nếu được quyền. | Low | Resource Capital |

### 2.3 Task & Timeline Management

| Use Case ID | Use Case Name | Primary Actor | Brief Description | Priority | Module |
|---|---|---|---|---|---|
| TTM-UC-001 | Create Task | User | Tạo Task. | Critical | Task |
| TTM-UC-002 | Update Task | User | Cập nhật Task. | High | Task |
| TTM-UC-003 | Delete Task | User | Xóa Task theo policy. | Medium | Task |
| TTM-UC-004 | Archive Task | User | Lưu trữ Task. | Medium | Task |
| TTM-UC-005 | Restore Task | User | Khôi phục Task. | Medium | Task |
| TTM-UC-006 | Duplicate Task | User | Nhân bản Task. | Medium | Task |
| TTM-UC-007 | View Task Detail | User | Xem chi tiết Task. | High | Task |
| TTM-UC-008 | Search Task | User | Tìm Task. | High | Task |
| TTM-UC-009 | Filter Task | User | Lọc Task. | High | Task |
| TTM-UC-010 | Sort Task | User | Sắp xếp Task. | Medium | Task |
| TTM-UC-011 | Assign Category | User | Gán Category. | Medium | Task |
| TTM-UC-012 | Assign Tag | User | Gắn Tag. | Medium | Task |
| TTM-UC-013 | Remove Tag | User | Gỡ Tag. | Low | Task |
| TTM-UC-014 | Set Priority | User | Gán Priority. | High | Task |
| TTM-UC-015 | Set Deadline | User | Đặt Deadline. | High | Task |
| TTM-UC-016 | Estimate Task Resources | User | Ước lượng Time/Cost. | Critical | Task |
| TTM-UC-017 | Plan Task | User | Chuyển sang Planned. | Critical | Task |
| TTM-UC-018 | Schedule Task | User | Đưa Task lên Timeline. | Critical | Timeline |
| TTM-UC-019 | Reschedule Task | User | Đổi lịch Task. | High | Timeline |
| TTM-UC-020 | View Timeline | User | Xem Timeline. | High | Timeline |
| TTM-UC-021 | Drag & Drop Timeline Task | User | Kéo thả Task trên Timeline. | High | Timeline |
| TTM-UC-022 | Update Progress | User | Cập nhật tiến độ. | High | Task |
| TTM-UC-023 | Pause Task | User | Tạm dừng Task. | Medium | Task |
| TTM-UC-024 | Resume Task | User | Tiếp tục Task. | Medium | Task |
| TTM-UC-025 | Complete Task | User | Hoàn thành Task. | Critical | Task |
| TTM-UC-026 | Cancel Task | User | Hủy Task. | High | Task |
| TTM-UC-027 | Reopen Task | User | Mở lại Task. | Medium | Task |
| TTM-UC-028 | Manage Recurring Task | User | Quản lý Task lặp nếu policy cho phép. | Low | Task |
| TTM-UC-029 | Manage Task Reminder | User | Quản lý reminder nếu policy cho phép. | Low | Task |
| TTM-UC-030 | Validate Task Ownership | System | Kiểm tra sở hữu Task. | Critical | Task |
| TTM-UC-031 | Validate Timeline Eligibility | System | Kiểm tra điều kiện Timeline. | Critical | Timeline |

### 2.4 Tracking, Evaluation & Reporting

| Use Case ID | Use Case Name | Primary Actor | Brief Description | Priority | Module |
|---|---|---|---|---|---|
| TER-UC-001 | Record Actual Time | User | Ghi Actual Time. | Critical | Tracking |
| TER-UC-002 | Update Actual Time | User | Cập nhật Actual Time. | High | Tracking |
| TER-UC-003 | Record Actual Cost | User | Ghi Actual Cost. | Critical | Tracking |
| TER-UC-004 | Update Actual Cost | User | Cập nhật Actual Cost. | High | Tracking |
| TER-UC-005 | View Planned vs Actual | User | Xem Planned vs Actual. | High | Evaluation |
| TER-UC-006 | Calculate Variance | System | Tính Variance. | Critical | Evaluation |
| TER-UC-007 | Evaluate Resource Efficiency | System | Đánh giá Efficiency. | Critical | Evaluation |
| TER-UC-008 | View Productivity Summary | User | Xem productivity summary. | High | Reporting |
| TER-UC-009 | View Statistics | User | Xem daily/weekly/monthly/yearly statistics. | High | Reporting |
| TER-UC-010 | View Resource Utilization | User | Xem resource utilization. | High | Reporting |
| TER-UC-011 | View Dashboard | User | Xem dashboard cá nhân. | High | Reporting |
| TER-UC-012 | View Report | User | Xem report. | High | Reporting |
| TER-UC-013 | Export Report | User | Xuất report nếu policy cho phép. | Medium | Reporting |
| TER-UC-014 | Search Report | User | Tìm report result. | Medium | Reporting |
| TER-UC-015 | Filter Report | User | Lọc report. | Medium | Reporting |
| TER-UC-016 | View History | User | Xem history actual/evaluation. | Medium | Tracking |
| TER-UC-017 | Compare Periods | User | So sánh period. | Medium | Reporting |
| TER-UC-018 | View Trend | User | Xem trend. | Medium | Reporting |
| TER-UC-019 | View Category Statistics | User | Xem thống kê Category. | Medium | Reporting |
| TER-UC-020 | View Tag Statistics | User | Xem thống kê Tag. | Medium | Reporting |
| TER-UC-021 | View Timeline Statistics | User | Xem thống kê Timeline. | Medium | Reporting |
| TER-UC-022 | View Personal KPI | User | Xem KPI cá nhân. | High | Reporting |
| TER-UC-023 | Re-evaluate Task | User | Đánh giá lại Task. | Medium | Evaluation |
| TER-UC-024 | Validate Access Scope | System | Kiểm tra phạm vi quyền. | Critical | Reporting |
| TER-UC-025 | Handle Empty Dashboard or Report | System | Xử lý không có dữ liệu. | High | Reporting |

### 2.5 Administration & Support

| Use Case ID | Use Case Name | Primary Actor | Brief Description | Priority | Module |
|---|---|---|---|---|---|
| ADM-UC-001 | View User | Admin | Xem User. | High | Administration |
| ADM-UC-002 | Search User | Admin | Tìm User. | High | Administration |
| ADM-UC-003 | Filter User | Admin | Lọc User. | Medium | Administration |
| ADM-UC-004 | Update User Information | Admin | Cập nhật User. | High | Administration |
| ADM-UC-005 | Deactivate User | Admin | Vô hiệu hóa User. | Critical | Administration |
| ADM-UC-006 | Reactivate User | Admin | Kích hoạt lại User. | Critical | Administration |
| ADM-UC-007 | Temporary Lock Account | Staff, Admin | Khóa tạm thời. | Critical | Administration |
| ADM-UC-008 | Unlock Account | Admin | Mở khóa. | Critical | Administration |
| ADM-UC-009 | View Staff | Admin | Xem Staff. | High | Administration |
| ADM-UC-010 | Manage Staff | Admin | Quản lý Staff. | High | Administration |
| ADM-UC-011 | Create Ticket | User, Staff, Admin | Tạo ticket. | High | Support |
| ADM-UC-012 | Receive Ticket | Staff | Tiếp nhận ticket. | High | Support |
| ADM-UC-013 | Assign Ticket | Staff, Admin | Phân công ticket. | High | Support |
| ADM-UC-014 | Update Ticket | Staff, Admin | Cập nhật ticket. | High | Support |
| ADM-UC-015 | Escalate Ticket | Staff | Leo thang ticket. | High | Support |
| ADM-UC-016 | Resolve Ticket | Staff, Admin | Xử lý ticket. | High | Support |
| ADM-UC-017 | Close Ticket | Staff, Admin | Đóng ticket. | Medium | Support |
| ADM-UC-018 | Reopen Ticket | Staff, Admin | Mở lại ticket. | Medium | Support |
| ADM-UC-019 | Search Ticket | Staff, Admin | Tìm ticket. | Medium | Support |
| ADM-UC-020 | Filter Ticket | Staff, Admin | Lọc ticket. | Medium | Support |
| ADM-UC-021 | View Activity Log | Staff, Admin | Xem activity log. | High | Administration |
| ADM-UC-022 | View Audit | Admin | Xem audit. | Critical | Administration |
| ADM-UC-023 | View Configuration | Admin | Xem cấu hình. | High | Administration |
| ADM-UC-024 | Update Configuration | Admin | Cập nhật cấu hình. | Critical | Administration |
| ADM-UC-025 | View System Dashboard | Admin | Xem dashboard quản trị. | High | Administration |
| ADM-UC-026 | Manage Role | Admin | Quản lý Role. | Critical | Administration |
| ADM-UC-027 | Manage Permission | Admin | Quản lý Permission. | Critical | Administration |
| ADM-UC-028 | Assign Role | Admin | Gán Role. | Critical | Administration |
| ADM-UC-029 | Revoke Role | Admin | Thu hồi Role. | Critical | Administration |
| ADM-UC-030 | Assign Permission | Admin | Gán Permission. | Critical | Administration |
| ADM-UC-031 | Revoke Permission | Admin | Thu hồi Permission. | Critical | Administration |
| ADM-UC-032 | Broadcast Announcement | Admin | Phát thông báo nếu policy cho phép. | Low | Administration |
| ADM-UC-033 | View Maintenance Status | Admin | Xem trạng thái bảo trì nếu policy cho phép. | Low | Administration |

### 2.6 Cross-cutting Use Cases

| Use Case ID | Use Case Name | Primary Actor | Brief Description | Priority | Module |
|---|---|---|---|---|---|
| CBR-UC-001 | Validate Ownership and Access Scope | System | Kiểm tra ownership và phạm vi quyền dùng chung. | Critical | Cross-cutting |
| CBR-UC-002 | Handle Business Validation Error | System | Xử lý lỗi validation dùng chung. | Critical | Cross-cutting |
| CBR-UC-003 | Record Audit Event | System | Ghi nhận audit event theo policy. | Critical | Cross-cutting |
| CBR-UC-004 | Record Activity History | System | Ghi nhận activity history theo policy. | High | Cross-cutting |
| CBR-UC-005 | Apply Search Filter Sort Standard | System | Áp dụng chuẩn search/filter/sort. | High | Cross-cutting |
| CBR-UC-006 | Export Business Data | User, Admin | Xuất dữ liệu nếu policy cho phép. | Medium | Cross-cutting |
| CBR-UC-007 | Send Business Notification | System | Gửi notification theo policy nếu được phê duyệt. | Medium | Cross-cutting |
| CBR-UC-008 | Handle Maintenance Mode | System, Admin | Xử lý trạng thái bảo trì nếu policy cho phép. | Medium | Cross-cutting |

## 3. Use Case Specification

### 3.1 Specification Convention

Để tài liệu có thể bao phủ toàn bộ Use Case mà không lặp lại mô tả dư thừa, mỗi Use Case được đặc tả theo cấu trúc chuẩn sau trong bảng module. Các trường được hiểu như sau:

| Field | Meaning |
|---|---|
| UC | Use Case ID và tên. |
| Description | Mục tiêu nghiệp vụ của Use Case. |
| Actors | Primary Actor và Supporting Actor nếu có. |
| Trigger | Sự kiện bắt đầu Use Case. |
| Preconditions | Điều kiện cần trước khi Use Case bắt đầu. |
| Postconditions | Kết quả sau khi Use Case kết thúc thành công. |
| Main Success Scenario | Luồng thành công chính ở mức nghiệp vụ. |
| Alternative Flows | Luồng thay thế hợp lệ. |
| Exception Flows | Lỗi hoặc tình huống từ chối. |
| Business Exceptions | Ngoại lệ nghiệp vụ thường gặp. |
| Special Requirements | Ràng buộc phi chức năng hoặc policy đặc biệt. |
| Traceability | FR, US, AC, BR, module liên quan. |
| Assumptions/Open Questions | Giả định hoặc điểm cần xác nhận nếu có. |

### 3.2 Identity & Authorization Use Case Specifications

| UC | Description | Actors | Trigger | Preconditions | Postconditions | Main Success Scenario | Alternative Flows | Exception Flows | Business Exceptions | Special Requirements | Traceability | Assumptions/Open Questions |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| IAM-UC-001 View Landing Page | Cho phép Guest xem thông tin công khai. | Primary: Guest; Supporting: System | Guest truy cập điểm vào công khai. | Không yêu cầu xác thực. | Guest vẫn chưa xác thực. | Hệ thống hiển thị nội dung công khai. | Guest chuyển sang Register hoặc Login. | Nội dung bảo vệ yêu cầu Unauthorized Handling. | Permission denied với nội dung không công khai. | Chỉ hiển thị thông tin công khai. | IAM-FR-001; IAM-US-001; IAM-AC-001; IAM-BR-001,002; Module Identity. | Landing content do product policy xác định. |
| IAM-UC-002 Register Account | Guest tạo tài khoản mới. | Primary: Guest; Supporting: System | Guest chọn đăng ký. | Chính sách đăng ký cho phép. | Account mới được tạo hoặc chờ xác minh. | Nhập thông tin, validate, tạo account, gán role mặc định. | Chờ xác minh nếu policy yêu cầu. | Thiếu hoặc trùng thông tin. | Duplicate registration; invalid registration data. | Default role phải hợp lệ. | IAM-FR-002,003,044; IAM-US-002; IAM-AC-002-005; IAM-BR-003,031. | Role mặc định cần xác nhận. |
| IAM-UC-003 Login | Actor đăng nhập để truy cập chức năng được phép. | Primary: User/Staff/Admin; Supporting: System | Actor gửi thông tin đăng nhập. | Account tồn tại và đủ điều kiện. | Session hợp lệ được thiết lập. | Validate credential, account status, role/permission, tạo session. | Actor đã có session hợp lệ. | Sai thông tin, locked, deactivated. | Account locked; invalid credential; inactive account. | Không tiết lộ thông tin nhạy cảm. | IAM-FR-004,005,013; IAM-US-003; IAM-AC-006-010; IAM-BR-012-017. | Chính sách login failure cần xác nhận. |
| IAM-UC-004 Logout | Actor kết thúc phiên. | Primary: User/Staff/Admin; Supporting: System | Actor chọn logout. | Actor có session hoặc context phiên. | Session hiện tại kết thúc. | Xác định session, kết thúc session, xác nhận logout. | Logout một phiên hoặc tất cả phiên theo policy. | Session đã hết hạn. | Invalid session. | Session reuse phải bị từ chối. | IAM-FR-006,011; IAM-US-004; IAM-AC-014,015; IAM-BR-017-019. | Multi-session policy cần xác nhận. |
| IAM-UC-005 View Own Profile | Actor xem hồ sơ cá nhân. | Primary: User/Staff/Admin; Supporting: System | Actor mở profile. | Đã xác thực. | Hồ sơ được xem, dữ liệu không đổi. | Validate session, kiểm tra quyền, hiển thị profile. | Admin xem account khác qua UC riêng. | Session invalid hoặc không có quyền. | Unauthorized/Forbidden. | Chỉ hiển thị trường được phép. | IAM-FR-007; IAM-US-005; IAM-AC-017; IAM-BR-004,020. | Trường profile cần policy. |
| IAM-UC-006 Update Own Profile | Actor cập nhật hồ sơ cá nhân. | Primary: User/Staff/Admin; Supporting: System | Actor lưu thay đổi profile. | Đã xác thực, trường được phép sửa. | Profile được cập nhật. | Nhập thay đổi, validate, cập nhật. | Actor hủy thao tác. | Field restricted hoặc invalid. | Invalid profile data. | Có thể audit nếu trường nhạy cảm. | IAM-FR-008; IAM-US-006; IAM-AC-018,019; IAM-BR-004,035. | Danh sách trường editable cần xác nhận. |
| IAM-UC-007 Change Password | Actor đổi mật khẩu. | Primary: User/Staff/Admin; Supporting: System | Actor chọn đổi mật khẩu. | Đã xác thực và account đủ điều kiện. | Mật khẩu thay đổi theo policy. | Xác minh hiện tại, nhập mật khẩu mới, validate, cập nhật. | Yêu cầu xác thực lại. | Xác minh thất bại, password không đạt policy. | Weak password; invalid verification. | Credential không được hiển thị. | IAM-FR-009; IAM-US-007; IAM-AC-020,021; IAM-BR-036. | Password policy cần xác nhận. |
| IAM-UC-008 Forgot Password | Actor khôi phục mật khẩu. | Primary: User/Staff/Admin; Supporting: System | Actor chọn quên mật khẩu. | Account đủ điều kiện recovery. | Mật khẩu mới được thiết lập nếu xác minh thành công. | Nhập nhận diện, xác minh danh tính, đặt mật khẩu mới. | Liên hệ support nếu cần. | Account locked/deactivated không đủ điều kiện. | Recovery denied. | Không tiết lộ account enumeration. | IAM-FR-010; IAM-US-008; IAM-AC-022,023; IAM-BR-037,038. | Recovery method cần xác nhận. |
| IAM-UC-009 Validate Session and Token | Kiểm tra phiên trước hành động bảo vệ. | Primary: System; Supporting: User/Staff/Admin | Actor truy cập chức năng bảo vệ. | Có thông tin session. | Chỉ session hợp lệ được tiếp tục. | Validate session/token, check account status. | Refresh/reauth theo policy. | Expired/invalid token. | Account locked mid-session. | Áp dụng mọi module. | IAM-FR-011,012,013,045; IAM-US-009; IAM-AC-011-016; IAM-BR-017-019,042. | Hiệu lực status mid-session cần xác nhận. |
| IAM-UC-010 Validate Authorization | Kiểm tra role/permission. | Primary: System; Supporting: Actor | Actor yêu cầu hành động bảo vệ. | Actor đã xác thực. | Hành động được cho phép hoặc từ chối. | Xác định required permission, effective permission, quyết định. | Nhiều role được tổng hợp theo policy. | Thiếu permission. | Forbidden. | Áp dụng toàn hệ thống. | IAM-FR-014-016; IAM-US-010; IAM-AC-024-028; IAM-BR-020-022. | Effective permission rule cần xác nhận. |
| IAM-UC-011 Search User | Admin tìm account. | Primary: Admin; Supporting: System | Admin nhập tiêu chí. | Admin có quyền. | Kết quả trong phạm vi quyền. | Validate quyền, trả kết quả. | Không có kết quả. | Tiêu chí không hợp lệ. | Forbidden. | Không lộ dữ liệu ngoài quyền. | IAM-FR-019; IAM-US-011; IAM-AC-029; IAM-BR-039. | Search fields cần xác nhận. |
| IAM-UC-012 View User Detail | Admin xem chi tiết account. | Primary: Admin; Supporting: System | Admin chọn account. | Có quyền xem. | Detail được hiển thị. | Validate quyền, hiển thị detail. | Account không còn tồn tại. | Thiếu quyền. | Forbidden. | Dữ liệu nhạy cảm giới hạn theo policy. | IAM-FR-020; IAM-US-012; IAM-AC-030; IAM-BR-039,040. | Detail scope cần xác nhận. |
| IAM-UC-013 Update User Account | Admin cập nhật account. | Primary: Admin; Supporting: System | Admin lưu thay đổi. | Có quyền, target hợp lệ. | Account được cập nhật. | Validate quyền/rule, xác nhận, cập nhật, audit nếu cần. | Admin hủy. | Thay đổi vượt quyền. | Governance violation. | Sensitive action confirmation. | IAM-FR-021; IAM-US-013; IAM-AC-031; IAM-BR-010,034,035. | Editable admin fields cần xác nhận. |
| IAM-UC-014 Deactivate User | Admin vô hiệu hóa account. | Primary: Admin; Supporting: System | Admin chọn deactivate. | Target đủ điều kiện. | Account deactivated. | Validate quyền, lý do, xác nhận, deactivate, audit. | Account đã deactivated. | Vi phạm self-protection. | Last admin protection. | Audit bắt buộc. | IAM-FR-022; IAM-US-014; IAM-AC-032; IAM-BR-013,026,034. | Điều kiện deactivate cần xác nhận. |
| IAM-UC-015 Reactivate User | Admin kích hoạt lại account. | Primary: Admin; Supporting: System | Admin chọn reactivate. | Account deactivated và đủ điều kiện. | Account active nếu không bị hạn chế khác. | Validate, xác nhận, reactivate, audit. | Account vẫn locked sau reactivate nếu policy. | Không đủ điều kiện. | Conflicting account status. | Audit bắt buộc. | IAM-FR-023; IAM-US-015; IAM-AC-033; IAM-BR-026,042. | Reactivate vs unlock cần xác nhận. |
| IAM-UC-016 Temporary Lock User | Staff/Admin khóa tạm User. | Primary: Staff/Admin; Supporting: System | Actor chọn lock. | Có permission và target trong scope. | Account locked. | Chọn target, lý do, validate scope, lock, audit. | Admin có scope rộng hơn Staff. | Staff tự khóa, khóa Admin, target invalid. | Scope violation. | Audit bắt buộc. | IAM-FR-024; IAM-US-016; IAM-AC-034,035; IAM-BR-006,009,012,026. | Lock duration cần xác nhận. |
| IAM-UC-017 Unlock User | Admin mở khóa. | Primary: Admin; Supporting: System | Admin chọn unlock. | Account đang locked. | Account unlocked nếu hợp lệ. | Validate, xác nhận, unlock, audit. | Lock tự hết hạn nếu policy. | Account không locked/deactivated. | Status conflict. | Audit bắt buộc. | IAM-FR-025; IAM-US-017; IAM-AC-036; IAM-BR-026,034,042. | Staff unlock? cần xác nhận. |
| IAM-UC-018 Manage Staff | Admin quản lý Staff. | Primary: Admin; Supporting: System | Admin thực hiện action Staff. | Có quyền Manage Staff. | Staff state/assignment cập nhật. | Chọn Staff, validate, cập nhật, audit nếu cần. | Hủy thao tác. | Vi phạm governance. | Last admin/staff scope issues. | Audit với action nhạy cảm. | IAM-FR-026; IAM-US-018; IAM-BR-010,044. | Staff lifecycle cần xác nhận. |
| IAM-UC-019 Manage Role | Admin quản lý Role. | Primary: Admin; Supporting: System | Admin tạo/sửa/trạng thái Role. | Có quyền Manage Role. | Role cập nhật hợp lệ. | Validate Role, kiểm tra impact, xác nhận, audit. | View only. | Role đang dùng, mất admin tối thiểu. | Governance violation. | Audit bắt buộc. | IAM-FR-027; IAM-US-019; IAM-BR-024,027,031,033. | Role deletion policy cần xác nhận. |
| IAM-UC-020 Assign Role | Admin gán Role. | Primary: Admin; Supporting: System | Admin chọn assign. | Role và account hợp lệ. | Role assigned. | Chọn account/role, validate, xác nhận, assign, audit. | Role đã tồn tại. | Role invalid/target invalid. | Governance violation. | Effective permission refresh. | IAM-FR-029; IAM-US-020; IAM-AC-037; IAM-BR-024,027,031,042. | Hiệu lực ngay hay sau session cần xác nhận. |
| IAM-UC-021 Revoke Role | Admin thu hồi Role. | Primary: Admin; Supporting: System | Admin chọn revoke. | Account đang có Role. | Role revoked nếu hợp lệ. | Validate impact, xác nhận, revoke, audit. | Account không có Role. | Mất Admin cuối cùng. | Governance violation. | Audit bắt buộc. | IAM-FR-030; IAM-US-021; IAM-AC-038; IAM-BR-024,028,032,033,043. | Quy trình bảo vệ cần xác nhận. |
| IAM-UC-022 Manage Permission | Admin quản lý Permission. | Primary: Admin; Supporting: System | Admin thay đổi Permission catalog/assignment. | Có quyền. | Permission cập nhật hợp lệ. | Validate, kiểm tra impact, xác nhận, audit. | View only. | Permission invalid hoặc gây mất governance. | Scope violation. | Audit bắt buộc. | IAM-FR-028; IAM-US-022; IAM-BR-025,029,031. | Direct permission có được dùng không? |
| IAM-UC-023 Assign Permission | Admin gán Permission. | Primary: Admin; Supporting: System | Admin chọn assign Permission. | Permission và scope hợp lệ. | Permission assigned. | Validate, xác nhận, assign, audit. | Permission đã tồn tại. | Permission sai phạm vi. | Scope violation. | Audit bắt buộc. | IAM-FR-031; IAM-US-023; IAM-AC-039; IAM-BR-025,029,031. | Permission gán qua Role hay direct cần xác nhận. |
| IAM-UC-024 Revoke Permission | Admin thu hồi Permission. | Primary: Admin; Supporting: System | Admin chọn revoke Permission. | Permission đang được gán. | Permission revoked. | Validate impact, xác nhận, revoke, audit. | Permission không tồn tại trên scope. | Mất quyền quản trị tối thiểu. | Governance violation. | Audit bắt buộc. | IAM-FR-032; IAM-US-024; IAM-AC-040; IAM-BR-025,030,032,043. | Effective revocation policy cần xác nhận. |
| IAM-UC-025 View Audit | Actor có quyền xem audit/log. | Primary: Admin; Staff if authorized; Supporting: System | Actor mở audit/log. | Có permission phù hợp. | Audit/log hiển thị trong scope. | Validate quyền, áp filter, hiển thị. | Staff chỉ xem log giới hạn. | Thiếu quyền. | Forbidden. | Không sửa audit. | IAM-FR-033,034,036; IAM-US-025,026; IAM-AC-043-045; IAM-BR-040. | Staff audit scope cần xác nhận. |
| IAM-UC-026 Authorize System Configuration | Kiểm tra quyền cấu hình. | Primary: Admin; Supporting: System | Admin truy cập cấu hình. | Có permission cấu hình. | Access allowed hoặc denied. | Validate permission, cho truy cập nếu hợp lệ, audit change nếu có. | View only. | Thiếu permission. | Forbidden. | Áp dụng trước update configuration. | IAM-FR-035; IAM-US-025; IAM-AC-046,047; IAM-BR-041. | Danh mục cấu hình cần xác nhận. |
| IAM-UC-027 Handle Unauthorized Access | Xử lý actor chưa xác thực. | Primary: System; Supporting: Guest | Guest truy cập chức năng bảo vệ. | Chức năng yêu cầu login. | Access bị từ chối. | Detect unauthenticated, hướng đến login hoặc thông báo. | Hiển thị nội dung công khai nếu có. | Không áp dụng. | Unauthorized. | Không lộ dữ liệu bảo vệ. | IAM-FR-017; IAM-US-027; IAM-AC-024; IAM-BR-002,023. | Thông báo UX cần xác nhận. |
| IAM-UC-028 Handle Forbidden Access | Xử lý actor thiếu quyền. | Primary: System; Supporting: User/Staff/Admin | Actor đã login nhưng thiếu permission. | Actor đã xác thực. | Hành động không thực hiện. | Validate permission, deny, thông báo phù hợp. | Nếu session invalid chuyển Unauthorized. | Thiếu quyền. | Forbidden. | Không lộ dữ liệu ngoài quyền. | IAM-FR-018; IAM-US-028; IAM-AC-025; IAM-BR-020,022. | Chính sách thông báo cần xác nhận. |

### 3.3 Resource Capital Management Use Case Specifications

| UC | Description | Actors | Trigger | Preconditions | Postconditions | Main Success Scenario | Alternative Flows | Exception Flows | Business Exceptions | Special Requirements | Traceability | Assumptions/Open Questions |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| RCM-UC-001 Create Capital Cycle | User tạo daily/weekly/monthly cycle. | Primary: User; Supporting: System | User chọn tạo cycle. | User authenticated; ownership valid. | Cycle được tạo. | Chọn type/period, validate overlap, nhập capital, tạo, ghi history. | Tạo draft; nhập một resource trước. | Type/period/capital invalid. | Duplicate cycle; invalid period. | History theo policy. | RCM-FR-001-003,010-013,037; RCM-US-001; RCM-AC-001-003; RCM-BR-001,004-006,011,012. | Cycle overlap policy cần xác nhận. |
| RCM-UC-002 Update Capital Cycle | Cập nhật thông tin cycle. | User/System | User chỉnh cycle. | Cycle thuộc User và editable. | Cycle cập nhật. | Chọn cycle, validate status, cập nhật, history. | Hủy thao tác. | Closed cycle. | Invalid status. | Không mất history. | RCM-FR-004,043,046; RCM-US-002; RCM-AC-004,005; RCM-BR-009,018,025. | Editable fields cần xác nhận. |
| RCM-UC-003 Activate Capital Cycle | Kích hoạt cycle. | User/System | User chọn activate. | Cycle hợp lệ, thuộc User. | Cycle active. | Validate active rule, xác nhận, activate, history. | Có active cycle khác xử lý theo policy. | Vi phạm active rule. | Multiple active conflict. | State awareness. | RCM-FR-005,047,048; RCM-US-003; RCM-AC-006,007; RCM-BR-007,008,025. | Có cho nhiều active cycle không? |
| RCM-UC-004 Close Capital Cycle | Đóng cycle. | User/System | User chọn close. | Cycle có thể đóng. | Cycle closed. | Kiểm tra remaining/allocation, cảnh báo, xử lý transfer nếu có, close, history. | Transfer remaining. | Chưa đủ điều kiện close. | Remaining unresolved; active allocation. | Không mất planned/allocated/history. | RCM-FR-006,028,029,039; RCM-US-004; RCM-AC-008,037,038; RCM-BR-034,035,045,046. | Close khi còn allocation cần xác nhận. |
| RCM-UC-005 Reopen Capital Cycle | Mở lại cycle đã đóng. | User/System | User chọn reopen. | Cycle closed; policy cho phép. | Cycle reopened. | Validate policy, xác nhận, reopen, history. | Yêu cầu reason. | Policy không cho reopen. | Reopen after transfer. | Trạng thái phải rõ. | RCM-FR-007; RCM-US-005; RCM-AC-009,010; RCM-BR-036,037. | Reopen limit cần xác nhận. |
| RCM-UC-006 Search Capital Cycle | Tìm cycle. | User/System | User nhập tiêu chí. | User authenticated. | Kết quả hiển thị. | Validate ownership, search, trả kết quả. | Không có kết quả. | Tìm của User khác. | Permission denied. | Scope-bound search. | RCM-FR-008; RCM-BR-001; RCM-US-021 partial. | Search criteria cần xác nhận. |
| RCM-UC-007 View Capital Cycle Detail | Xem chi tiết cycle. | User/System | User chọn cycle. | Cycle thuộc User. | Detail hiển thị. | Validate ownership, hiển thị planned/allocated/remaining/status. | Chuyển summary/history. | Cycle không tồn tại. | Permission denied. | Không đổi dữ liệu. | RCM-FR-009,033; RCM-AC-032; RCM-BR-001,038. | Detail level cần xác nhận. |
| RCM-UC-008 Set Time Capital | Thiết lập Time Capital. | User/System | User nhập time. | Cycle editable. | Time Capital set. | Validate >=0/unit, save, update balance. | Set khi tạo cycle. | Negative/invalid unit. | Invalid capital. | Unit consistency. | RCM-FR-010,012,049; RCM-US-006; RCM-AC-011,012; RCM-BR-011,042. | Time unit cần xác nhận. |
| RCM-UC-009 Set Money Capital | Thiết lập Money Capital. | User/System | User nhập money. | Cycle editable. | Money Capital set. | Validate >=0/currency, save, update balance. | Set khi tạo cycle. | Negative/invalid currency. | Invalid capital. | Currency consistency. | RCM-FR-011,013,049; RCM-US-007; RCM-AC-013,014; RCM-BR-012,043. | Currency policy cần xác nhận. |
| RCM-UC-010 Adjust Time Capital | Điều chỉnh Time Capital. | User/System | User chọn adjust time. | Cycle cho phép adjust. | Time Capital và balance cập nhật. | Nhập adjustment/reason, validate impact, confirm, update, history. | Tạo over allocation nếu policy cho phép. | Missing reason; invalid impact. | Over allocation conflict. | Không mất history. | RCM-FR-014,016,017,035,037; RCM-US-008; RCM-AC-015,017,018; RCM-BR-018-021,026. | Reason bắt buộc? |
| RCM-UC-011 Adjust Money Capital | Điều chỉnh Money Capital. | User/System | User chọn adjust money. | Cycle cho phép adjust. | Money Capital và balance cập nhật. | Nhập adjustment/reason, validate impact, confirm, update, history. | Tạo over allocation nếu policy cho phép. | Missing reason; invalid impact. | Over allocation conflict. | Không mất history. | RCM-FR-015-017,035,037; RCM-US-009; RCM-AC-016-018; RCM-BR-018-021,026. | Reason bắt buộc? |
| RCM-UC-012 Allocate Time Capital | Phân bổ Time Capital. | User/System | User phân bổ time. | Active cycle; target hợp lệ. | Time allocated. | Nhập amount/target, validate available, allocate, update remaining, history. | Over allocation confirmed. | Not enough resource; invalid target. | Resource not enough. | Amount >0. | RCM-FR-018,020-023,038; RCM-US-010; RCM-AC-019,021-023; RCM-BR-013,015,016,022. | Allocation target policy cần xác nhận. |
| RCM-UC-013 Allocate Money Capital | Phân bổ Money Capital. | User/System | User phân bổ money. | Active cycle; target hợp lệ. | Money allocated. | Nhập amount/target, validate available, allocate, update remaining, history. | Over allocation confirmed. | Not enough resource; invalid target. | Resource not enough. | Amount >0. | RCM-FR-019-023,038; RCM-US-011; RCM-AC-020-023; RCM-BR-014-016,022. | Target policy cần xác nhận. |
| RCM-UC-014 Reallocate Capital | Điều chỉnh allocation. | User/System | User chọn reallocate. | Allocation hợp lệ. | Allocation và balance cập nhật. | Nhập giá trị mới, validate, confirm, update, history. | Giảm allocation trả về remaining. | Vi phạm balance/policy. | Invalid allocation. | History bắt buộc. | RCM-FR-024,025,038; RCM-US-012; RCM-AC-024,025; RCM-BR-023,029,030. | Reallocation reason cần xác nhận. |
| RCM-UC-015 Release Allocated Capital | Giải phóng allocation. | User/System | User chọn release. | Allocation còn hiệu lực. | Capital released. | Nhập amount, validate <= allocated, confirm, release, history. | Release toàn bộ. | Amount vượt allocation. | Invalid release. | Cập nhật remaining. | RCM-FR-026,027,038; RCM-US-013; RCM-AC-026,027; RCM-BR-024,028. | Release closed cycle? |
| RCM-UC-016 Allow Over Allocation | Xác nhận vượt mức. | User/System | Allocation/adjustment vượt available. | Policy cho phép. | Remaining có thể âm và flagged. | Cảnh báo, User xác nhận, record decision, complete action. | User hủy. | Policy không cho phép. | Over allocation denied. | Must be explicit. | RCM-FR-022,023,045; RCM-US-014; RCM-AC-021,022,031; RCM-BR-016,017,027,044. | Có giới hạn over allocation không? |
| RCM-UC-017 Transfer Remaining Capital | Chuyển số dư. | User/System | User close/transfer. | Policy cho phép; remaining hợp lệ. | Remaining transferred. | Chọn source/target, validate positive remaining, confirm, transfer, history. | Không transfer. | Remaining âm/target invalid. | Invalid transfer. | Không double count. | RCM-FR-028,029,037; RCM-US-022; RCM-AC-037,038; RCM-BR-031-033,054. | Time có được transfer? |
| RCM-UC-018 View Available Capital | Xem capital khả dụng. | User/System | User mở view. | Có cycle thuộc User. | Available hiển thị. | Validate ownership, calculate, display time/money riêng. | View theo resource type. | Cycle không thuộc User. | Permission denied. | Không cộng gộp Time/Money. | RCM-FR-030,044; RCM-US-015; RCM-AC-028; RCM-BR-039,041. | Calculation detail theo policy. |
| RCM-UC-019 View Allocated Capital | Xem capital đã phân bổ. | User/System | User mở allocated. | Có cycle thuộc User. | Allocated hiển thị. | Validate ownership, display allocated time/money. | Filter theo target nếu policy. | Invalid cycle. | Permission denied. | Read-only. | RCM-FR-031; RCM-US-016; RCM-AC-029; RCM-BR-001,041. | Filter target cần xác nhận. |
| RCM-UC-020 View Remaining Capital | Xem capital còn lại. | User/System | User mở remaining. | Có cycle thuộc User. | Remaining hiển thị. | Calculate remaining, display; nếu âm mark over allocation. | View theo resource type. | Invalid cycle. | Permission denied. | Over allocation indicator. | RCM-FR-032,044,045; RCM-US-017; RCM-AC-030,031; RCM-BR-017,040,041. | None. |
| RCM-UC-021 View Capital Summary | Xem summary nguồn vốn. | User/System | User mở summary. | Có cycle thuộc User. | Summary hiển thị. | Validate, display planned/allocated/remaining/status. | Chi tiết Time/Money. | Không có quyền. | Permission denied. | Summary consistency. | RCM-FR-033; RCM-US-018; RCM-AC-032; RCM-BR-038,041. | None. |
| RCM-UC-022 View Allocation History | Xem lịch sử allocation. | User/System | User mở history. | Có quyền. | History hiển thị. | Filter scope, validate ownership, display allocation/reallocation/release. | Không có dữ liệu. | Xem của người khác. | Permission denied. | History read-only. | RCM-FR-034,036,038; RCM-US-019,021; RCM-AC-033,035,036; RCM-BR-022-024,049. | Retention cần xác nhận. |
| RCM-UC-023 View Adjustment History | Xem lịch sử adjustment. | User/System | User mở adjustment history. | Có quyền. | History hiển thị. | Validate, display adjustment events/reasons. | Filter Time/Money. | Không có quyền. | Permission denied. | History read-only. | RCM-FR-035,036,037; RCM-US-020,021; RCM-AC-034-036; RCM-BR-021,026,049. | Reason visibility cần xác nhận. |
| RCM-UC-024 Filter Capital History | Lọc history. | User/System | User chọn filter. | Có quyền history. | Kết quả lọc hiển thị. | Validate criteria, apply, display. | Không có kết quả. | Criteria invalid. | Invalid filter. | Scope-bound. | RCM-FR-036; RCM-US-021; RCM-AC-035,036; RCM-BR-049. | Filter catalog cần xác nhận. |
| RCM-UC-025 Validate Capital Ownership | Kiểm tra ownership capital. | System; Supporting: Actor | Actor truy cập capital. | Actor authenticated. | Request được tiếp tục hoặc denied. | Xác định actor/owner/scope, quyết định. | Staff/Admin limited view if policy. | Không có quyền. | Permission denied. | Áp dụng mọi RCM UC. | RCM-FR-040-042,050; RCM-US-023; RCM-AC-039-042; RCM-BR-001-003,047,048. | Staff/Admin view scope cần xác nhận. |
| RCM-UC-026 Validate Capital Balance | Kiểm tra balance. | System; Supporting: User | Allocation/adjustment/release/transfer. | Cycle valid. | Action được tiếp tục hoặc denied. | Determine planned/allocated/remaining, validate rules. | Over allocation confirmation. | Invalid value/policy violation. | Resource not enough. | Critical validation. | RCM-FR-021,023,025,027,029,044,045; RCM-US-024; RCM-AC-019-027; RCM-BR-015-017,039,040. | Formula details theo policy. |
| RCM-UC-027 View Authorized Capital Summary | Staff/Admin xem summary giới hạn. | Staff/Admin; Supporting: System | Actor yêu cầu support view. | Policy cho phép. | Summary giới hạn hiển thị. | Validate permission/purpose, display scope. | Metadata only. | Không có quyền. | Privacy violation denied. | Không chỉnh sửa capital. | RCM-FR-050; RCM-US-025,026; RCM-AC-040-042; RCM-BR-002,003,047,048. | Có cho phép không? |

### 3.4 Task & Timeline Use Case Specifications

| UC | Description | Actors | Trigger | Preconditions | Postconditions | Main Success Scenario | Alternative Flows | Exception Flows | Business Exceptions | Special Requirements | Traceability | Assumptions/Open Questions |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TTM-UC-001 Create Task | User tạo Task mới. | User/System | User chọn tạo Task. | User authenticated. | Task được tạo thuộc User. | Nhập name, validate, create Draft, assign owner. | Tạo đủ planning để Planned. | Name missing/invalid. | Invalid input. | Ownership bắt buộc. | TTM-FR-001,002,056; TTM-US-001; TTM-AC-001,002; TTM-BR-001,005,006. | Field bắt buộc ngoài name cần xác nhận. |
| TTM-UC-002 Update Task | User cập nhật Task. | User/System | User lưu thay đổi. | Task thuộc User, status editable. | Task cập nhật. | Validate ownership/status/data, update, history if policy. | Completed yêu cầu reopen. | Invalid status/data. | Task completed locked. | Không đổi nếu fail. | TTM-FR-003,053,056; TTM-US-002; TTM-AC-003,004; TTM-BR-002,018,054. | Planning important fields cần xác nhận. |
| TTM-UC-003 Delete Task | User xóa Task. | User/System | User chọn delete. | Task thuộc User, deletable. | Task không còn hiện trong list chính. | Validate policy, confirm, delete. | Đề xuất archive. | Status không cho delete. | Invalid status. | Delete policy. | TTM-FR-004; TTM-US-003; TTM-AC-005,006; TTM-BR-022,023. | Có hỗ trợ delete hay chỉ archive? |
| TTM-UC-004 Archive Task | User lưu trữ Task. | User/System | User chọn archive. | Task archivable. | Task Archived. | Validate, confirm, archive, remove from main timeline. | Bulk archive if policy. | Not owner/invalid status. | Permission denied. | Archive != delete. | TTM-FR-005,059; TTM-US-004; TTM-AC-039,040; TTM-BR-020,024,040,041. | Bulk archive? |
| TTM-UC-005 Restore Task | User khôi phục Task. | User/System | User chọn restore. | Task Archived. | Task restored theo policy. | Validate, determine restore status, confirm, restore. | Restore previous/default status. | Task not archived. | Invalid status. | Timeline placement policy. | TTM-FR-006,060; TTM-US-005; TTM-AC-041; TTM-BR-021,025. | Restore status cần xác nhận. |
| TTM-UC-006 Duplicate Task | User nhân bản Task. | User/System | User chọn duplicate. | Source Task thuộc User. | New Task tạo độc lập. | Validate, copy allowed fields, create new Task status policy. | User edit before confirm. | Source not owned. | Permission denied. | Không copy history nếu policy không cho. | TTM-FR-007; TTM-US-006; TTM-AC-007; TTM-BR-031,032. | Fields copied cần xác nhận. |
| TTM-UC-007 View Task Detail | User xem chi tiết Task. | User/System | User mở detail. | Task thuộc User. | Detail hiển thị. | Validate ownership, display task data. | Chuyển sang edit. | Not owner. | Permission denied. | Read-only unless edit allowed. | TTM-FR-008; TTM-AC-008; TTM-BR-002. | Detail scope. |
| TTM-UC-008 Search Task | User tìm Task. | User/System | User nhập keyword. | User authenticated. | Matching tasks shown. | Scope owner, search, display. | No results. | Invalid criteria. | Invalid search. | Scope-bound. | TTM-FR-009; TTM-US-016; TTM-BR-055. | Search fields. |
| TTM-UC-009 Filter Task | User lọc Task. | User/System | User chọn filter. | User authenticated. | Filtered tasks shown. | Validate filters, apply within ownership. | Combine filters. | Invalid filter. | Invalid filter. | Scope-bound. | TTM-FR-010; TTM-US-017; TTM-BR-055. | Filter logic for tags. |
| TTM-UC-010 Sort Task | User sắp xếp Task. | User/System | User chọn sort. | Viewing task list. | Sorted view shown. | Validate sort field, sort. | Asc/desc. | Invalid sort. | Invalid criteria. | Không đổi dữ liệu. | TTM-FR-011; TTM-US-018; TTM-BR-056. | Sort catalog. |
| TTM-UC-011 Assign Category | Gán Category cho Task. | User/System | User chọn category. | Task owned; category valid. | Category assigned/changed. | Validate ownership/category/status, assign. | Change/remove if policy. | Invalid category. | Category required/invalid. | Category lifecycle out of scope. | TTM-FR-012-014; TTM-US-007; TTM-AC-009,010; TTM-BR-028-030. | Category mandatory? |
| TTM-UC-012 Assign Tag | Gắn Tag cho Task. | User/System | User chọn tag. | Task owned; tag valid. | Tags assigned. | Validate tags and limit, assign. | Multiple tags. | Invalid/exceed limit. | Tag limit exceeded. | Tag lifecycle out of scope. | TTM-FR-015,017; TTM-US-008; TTM-AC-011; TTM-BR-026,027. | Tag max cần xác nhận. |
| TTM-UC-013 Remove Tag | Gỡ Tag. | User/System | User remove tag. | Tag attached. | Tag removed. | Validate ownership, remove. | Remove multiple. | Tag not attached. | No-op or invalid. | Scope-bound. | TTM-FR-016; TTM-AC-012; TTM-BR-026,027. | Bulk remove? |
| TTM-UC-014 Set Priority | Gán/cập nhật priority. | User/System | User chọn priority. | Task owned; status allows. | Priority updated. | Validate priority set, update. | Change multiple times before completed. | Invalid priority. | Invalid value. | Policy set required. | TTM-FR-018-020; TTM-US-009; TTM-AC-013,014; TTM-BR-015. | Priority values. |
| TTM-UC-015 Set Deadline | Đặt/đổi deadline. | User/System | User nhập deadline. | Task owned; status allows. | Deadline updated. | Validate date, compare schedule, update. | Deadline past warning. | Deadline before start. | Invalid deadline. | Overdue policy. | TTM-FR-021-023; TTM-US-010; TTM-AC-015,016; TTM-BR-013,014,057. | Past deadline allowed? |
| TTM-UC-016 Estimate Task Resources | Nhập estimated time/cost. | User/System | User nhập estimates. | Task owned. | Estimates updated. | Validate time/cost, save. | Only time or cost. | Time invalid/cost negative. | Invalid estimate. | Time required for Timeline. | TTM-FR-024-027; TTM-US-011,012; TTM-AC-017-020; TTM-BR-011,012,058,059. | Link with capital allocation. |
| TTM-UC-017 Plan Task | Chuyển Task sang Planned. | User/System | User hoàn tất planning. | Draft/editable Task. | Task Planned. | Validate planning policy, transition. | Remain Draft if incomplete. | Missing mandatory info. | Invalid planning. | Status transition. | TTM-FR-028; TTM-US-013 partial; TTM-AC-021,022; TTM-BR-007,008. | Required planning fields. |
| TTM-UC-018 Schedule Task | Đưa Task lên Timeline. | User/System | User chọn schedule. | Planned; Time Capital/estimated time valid. | Task Scheduled and visible on Timeline. | Select time, validate eligibility/deadline/conflict, schedule. | Conflict warning/confirmation. | No Time Capital; invalid schedule. | Timeline eligibility failed. | Timeline only eligible tasks. | TTM-FR-029,031,032,035-037; TTM-US-013,015,027; TTM-AC-023,024,028-030; TTM-BR-009,010,033,036. | Conflict policy. |
| TTM-UC-019 Reschedule Task | Thay đổi lịch. | User/System | User chọn new schedule. | Scheduled Task. | Schedule updated. | Validate status, deadline, conflict, update, history. | Warning if conflict. | Invalid new time. | Invalid schedule. | Timeline history if policy. | TTM-FR-030,033,035-037,057; TTM-AC-025; TTM-BR-034-037,053. | Cycle policy. |
| TTM-UC-020 View Timeline | Xem Timeline. | User/System | User mở Timeline. | User authenticated. | Timeline hiển thị eligible tasks. | Determine eligible tasks, display by time. | Empty timeline. | No access. | Permission denied. | No non-eligible tasks. | TTM-FR-031,032; TTM-US-015,027; TTM-AC-029,030; TTM-BR-009,010,055. | Time range default. |
| TTM-UC-021 Drag & Drop Timeline Task | Kéo thả Task. | User/System | User kéo Task. | Task on Timeline and movable. | Schedule updated or unchanged. | Drag, validate target, apply update. | Conflict handled by policy. | Invalid position. | Conflict/deadline violation. | Alternative non-drag should exist. | TTM-FR-034-037; TTM-US-014; TTM-AC-026-028; TTM-BR-034-037. | Need confirmation/undo? |
| TTM-UC-022 Update Progress | Cập nhật progress. | User/System | User nhập progress. | Task status allows. | Progress updated. | Validate 0-100, update, suggest complete if 100. | Progress 100 no auto complete if policy. | Out of range. | Invalid progress. | Status consistency. | TTM-FR-038,039; TTM-US-019; TTM-AC-031,032; TTM-BR-016,017. | Auto complete? |
| TTM-UC-023 Pause Task | Tạm dừng Task. | User/System | User chọn pause. | Task In Progress. | Task On Hold. | Validate status, transition. | Reason if policy. | Not In Progress. | Invalid status. | History if policy. | TTM-FR-040; TTM-US-020; TTM-AC-033; TTM-BR-038. | Pause reason? |
| TTM-UC-024 Resume Task | Tiếp tục Task. | User/System | User chọn resume. | Task On Hold. | Task In Progress. | Validate status, transition. | Reschedule before resume. | Not On Hold. | Invalid status. | History if policy. | TTM-FR-041; TTM-US-021; TTM-AC-034; TTM-BR-039. | None. |
| TTM-UC-025 Complete Task | Hoàn thành Task. | User/System | User chọn complete. | Task completion eligible. | Task Completed. | Validate status/rule, confirm, transition, handle timeline. | Require progress threshold. | Not eligible. | Invalid status. | Input for TER. | TTM-FR-042,043,046; TTM-US-022; TTM-AC-035,036; TTM-BR-018,045,046,060. | Completion rule. |
| TTM-UC-026 Cancel Task | Hủy Task. | User/System | User chọn cancel. | Task cancellable. | Task Cancelled. | Validate status, reason if needed, cancel, handle timeline. | User cancels action. | Completed/Archived direct cancel not allowed. | Invalid status. | Reason if policy. | TTM-FR-044,058; TTM-US-023; TTM-AC-037; TTM-BR-019,044. | Cancel reason? |
| TTM-UC-027 Reopen Task | Mở lại Task. | User/System | User chọn reopen. | Completed/Cancelled; policy allows. | Task reopened to policy status. | Validate, reason, transition. | Return Planned/Scheduled/In Progress. | Policy disallows. | Reopen denied. | Affects TER. | TTM-FR-045; TTM-US-024; TTM-AC-038; TTM-BR-042,043. | Target status. |
| TTM-UC-028 Manage Recurring Task | Quản lý recurring nếu supported. | User/System | User chọn recurring. | Policy approved. | Recurring rule/occurrence created. | Define rule, validate, save/generate. | Edit rule. | Invalid rule. | Recurring conflict. | Conditional scope. | TTM-FR-047-049; TTM-US-025; TTM-AC-042,043; TTM-BR-047-049. | Recurring in scope? |
| TTM-UC-029 Manage Task Reminder | Quản lý reminder nếu supported. | User/System | User set reminder. | Policy approved; Task valid. | Reminder set/updated/removed. | Enter reminder, validate, save. | Change/remove reminder. | Invalid reminder time. | Reminder after deadline. | Conditional scope. | TTM-FR-050,051; TTM-US-026; TTM-AC-044; TTM-BR-050,051. | Reminder in scope? |
| TTM-UC-030 Validate Task Ownership | Kiểm tra ownership Task. | System; Supporting: Actor | Actor accesses Task. | Actor authenticated. | Request allowed or denied. | Determine owner/scope, decide. | Staff/Admin limited view if policy. | No right. | Permission denied. | Applies all TTM. | TTM-FR-052,054,055; TTM-US-028; TTM-AC-045,046; TTM-BR-001-004. | Staff/Admin view scope. |
| TTM-UC-031 Validate Timeline Eligibility | Kiểm tra Timeline eligibility. | System; Supporting: User | Schedule/view timeline. | Task exists. | Eligible tasks shown/accepted. | Check Time Capital/estimated time/status/schedule. | Non-eligible remains in task list. | Invalid Task excluded. | No Time Capital. | Applies timeline. | TTM-FR-032,035; TTM-US-027; TTM-AC-023,024,030; TTM-BR-009,010,059. | Exceptions policy. |

### 3.5 Tracking, Evaluation & Reporting Use Case Specifications

| UC | Description | Actors | Trigger | Preconditions | Postconditions | Main Success Scenario | Alternative Flows | Exception Flows | Business Exceptions | Special Requirements | Traceability | Assumptions/Open Questions |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| TER-UC-001 Record Actual Time | Ghi Actual Time. | User/System | User nhập actual time. | Task đủ điều kiện, thuộc User. | Actual Time recorded. | Validate eligibility/value, save, history. | Save without evaluation if policy. | Negative/not completed. | Task not eligible. | Actual >=0. | TER-FR-001,005,007,050; TER-US-001; TER-AC-001,002,005; TER-BR-001,003,017. | Actual before completed? |
| TER-UC-002 Update Actual Time | Cập nhật Actual Time. | User/System | User edit actual time. | Policy allows; evaluation not locked. | Actual Time updated; re-eval if needed. | Validate, update, trigger re-evaluation, history. | Require reason. | Finalized evaluation. | Locked evaluation. | History if policy. | TER-FR-002,005,041,042,050; TER-US-003; TER-AC-006,008; TER-BR-003,035-037. | Finalization policy. |
| TER-UC-003 Record Actual Cost | Ghi Actual Cost. | User/System | User nhập actual cost. | Task eligible. | Actual Cost recorded. | Validate eligibility/value, save, history. | No cost if policy. | Negative value. | Invalid actual. | Actual >=0. | TER-FR-003,006,007,050; TER-US-002; TER-AC-003,004,005; TER-BR-001,004,015. | Missing cost treated how? |
| TER-UC-004 Update Actual Cost | Cập nhật Actual Cost. | User/System | User edit actual cost. | Policy allows. | Actual Cost updated. | Validate, update, re-evaluate if needed. | Require reason. | Finalized evaluation. | Locked evaluation. | History if policy. | TER-FR-004,006,041,042,050; TER-US-003; TER-AC-007,008; TER-BR-004,035-037. | Finalization policy. |
| TER-UC-005 View Planned vs Actual | Xem Planned/Actual. | User/System | User opens evaluation. | User has scope. | Comparison displayed. | Load planned/actual, show values/missing states. | Partial data. | No access. | Insufficient data. | Planned not changed here. | TER-FR-008; TER-US-004; TER-AC-009,010; TER-BR-005-007,017. | Missing baseline policy. |
| TER-UC-006 Calculate Variance | Tính variance. | System | Evaluation/dashboard/report requested. | Planned/Actual valid. | Variance calculated or marked insufficient. | Actual - Planned, classify positive/negative/zero. | Missing planned/actual. | Invalid values. | Cannot calculate. | Business formula only. | TER-FR-009,010,038; TER-US-005,006; TER-AC-011-014; TER-BR-008-012. | None. |
| TER-UC-007 Evaluate Resource Efficiency | Đánh giá efficiency. | System | User requests evaluation/KPI. | Sufficient data per policy. | Efficiency displayed. | Use variance/completion/context policy, display result. | Partial Time/Cost evaluation. | Insufficient data. | Efficiency unavailable. | Context-sensitive. | TER-FR-011,034-036; TER-US-007; TER-AC-015,016; TER-BR-031-033,049. | Thresholds need confirmation. |
| TER-UC-008 View Productivity Summary | Xem summary productivity. | User/System | User opens summary. | Valid period. | Summary displayed. | Select period, aggregate KPI, show. | Insufficient data. | Invalid period. | Empty data. | No absolute judgment. | TER-FR-013,037; TER-US-009; TER-AC-039; TER-BR-027,031,042,047. | Productivity formula. |
| TER-UC-009 View Statistics | Xem period statistics. | User/System | User selects daily/weekly/monthly/yearly. | Period valid. | Statistics displayed. | Validate period, aggregate stats. | Empty state. | Invalid period. | No data. | Period clarity. | TER-FR-014-017,046,047; TER-US-010; TER-AC-017-020; TER-BR-023,024,041. | Custom period? |
| TER-UC-010 View Resource Utilization | Xem utilization. | User/System | User selects resource utilization. | Data or empty state. | Utilization displayed. | Filter period/resource, aggregate Time/Money separately. | Only one resource has data. | Invalid filter. | Insufficient data. | Do not combine Time/Money improperly. | TER-FR-018; TER-US-011; TER-BR-048. | Formula policy. |
| TER-UC-011 View Dashboard | Xem dashboard cá nhân. | User/System | User opens dashboard. | Authorized scope. | Dashboard shown. | Select filters, validate, aggregate KPI/stats/trends. | Empty dashboard. | Invalid filter/period. | No data. | Scope-bound, no misleading KPI. | TER-FR-019,043-045,047; TER-US-012; TER-AC-021,022; TER-BR-017,025,046. | Drill-down in scope? |
| TER-UC-012 View Report | Xem report. | User/System | User selects report. | Report type/period valid. | Report shown. | Select type/filter, validate, aggregate, display. | Empty report. | Invalid period/type. | No data. | Reflects data at access time. | TER-FR-020,051-058; TER-US-013; TER-AC-023,024; TER-BR-018,021,026. | Report catalog. |
| TER-UC-013 Export Report | Xuất report nếu allowed. | User/System | User selects export. | Export policy permits. | Export generated. | Choose format, validate scope/policy, export, history if needed. | Choose another format. | Format denied/large report. | Export not allowed. | Same scope as viewed report. | TER-FR-021-023,048; TER-US-014; TER-AC-025-028; TER-BR-022,045. | Export in scope? |
| TER-UC-014 Search Report | Tìm report result/history. | User/System | User enters criteria. | User authorized. | Matching results shown. | Validate criteria/scope, search. | No result. | Invalid criteria. | Scope violation. | Scope-bound. | TER-FR-024; TER-US-015; TER-AC-029; TER-BR-043. | Search scope. |
| TER-UC-015 Filter Report | Lọc report. | User/System | User selects filters. | Filters valid. | Filtered report shown. | Validate filters, apply, display. | Combine filters. | Invalid filter. | Scope violation. | Scope-bound. | TER-FR-025; TER-US-015; TER-AC-030,031; TER-BR-043. | Filter combinations. |
| TER-UC-016 View History | Xem history actual/evaluation. | User/System | User opens history. | Policy allows, scope valid. | History shown. | Select period/filter, validate, display. | No history. | No access. | Permission denied. | History read-only. | TER-FR-026,039,050; TER-US-016; TER-AC-032; TER-BR-037,038. | Retention. |
| TER-UC-017 Compare Periods | So sánh periods. | User/System | User selects compare. | Periods valid/comparable. | Comparison shown. | Validate periods, aggregate KPI, display differences. | One period lacks data. | Invalid period. | Insufficient data. | Indicate scope. | TER-FR-027; TER-US-17; TER-AC-033; TER-BR-039. | Comparable period rule. |
| TER-UC-018 View Trend | Xem trend. | User/System | User selects trend. | Enough data per policy. | Trend shown. | Select KPI/range, validate minimum data, display. | Insufficient data. | Invalid range. | Trend unavailable. | No unsupported causality. | TER-FR-028; TER-US-018; TER-AC-034,035; TER-BR-040. | Minimum periods. |
| TER-UC-019 View Category Statistics | Thống kê Category. | User/System | User selects category stats. | Category data or empty. | Stats shown. | Select period/filter, aggregate by category. | Uncategorized handling. | Invalid filter. | No data. | Scope-bound. | TER-FR-029; TER-US-019; TER-AC-036; TER-BR-028. | Uncategorized policy. |
| TER-UC-020 View Tag Statistics | Thống kê Tag. | User/System | User selects tag stats. | Tag data or empty. | Stats shown. | Aggregate by tag in scope. | Multi-tag handling. | Invalid filter. | No data. | Scope-bound. | TER-FR-030; TER-US-020; TER-AC-037; TER-BR-029. | Multi-tag counting. |
| TER-UC-021 View Timeline Statistics | Thống kê Timeline. | User/System | User selects timeline stats. | Timeline data exists or empty. | Stats shown. | Select period, use timeline data, aggregate. | No timeline data. | Invalid period. | No data. | Read-only use of timeline data. | TER-FR-031; TER-AC-038; TER-BR-030. | Scope of timeline stats. |
| TER-UC-022 View Personal KPI | Xem KPI cá nhân. | User/System | User opens KPI view. | KPI policy defined. | KPI shown or insufficient data. | Select KPI/period, validate data, display. | Not enough data. | KPI not defined. | Insufficient data. | Explain KPI meaning. | TER-FR-012,032,033,037; TER-US-008,021; TER-AC-039; TER-BR-031,042,047. | KPI formulas. |
| TER-UC-023 Re-evaluate Task | Đánh giá lại sau Actual update. | User/System | Actual changed. | Evaluation not locked or reopen allowed. | Evaluation updated. | Recalculate variance/efficiency, history. | Require confirmation. | Finalized locked. | Re-evaluation denied. | Affected by Task Reopen. | TER-FR-041,042; TER-BR-035,036,050. | Reopen policy. |
| TER-UC-024 Validate Access Scope | Kiểm tra phạm vi quyền. | System; Supporting: Actor | Actor requests report/dashboard/history. | Actor authenticated. | Data allowed or denied. | Determine actor/scope, filter data. | Staff/Admin limited view. | No permission. | Permission denied. | Applies all TER views. | TER-FR-049,059,060; TER-US-022; TER-AC-040-042; TER-BR-017-020. | Staff/Admin scope. |
| TER-UC-025 Handle Empty Dashboard or Report | Xử lý không có dữ liệu. | System | Dashboard/report requested with no data. | Period/filter valid. | Empty state shown. | Detect empty, do not calculate misleading KPI, display message. | Suggest changing filter if policy. | None. | No data. | No false zero KPI. | TER-FR-047; TER-AC-022,024; TER-BR-025,026,042. | Empty messaging. |

### 3.6 Administration & Support Use Case Specifications

| UC | Description | Actors | Trigger | Preconditions | Postconditions | Main Success Scenario | Alternative Flows | Exception Flows | Business Exceptions | Special Requirements | Traceability | Assumptions/Open Questions |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| ADM-UC-001 View User | Admin xem User. | Admin/System | Admin chọn User. | Admin authorized. | Info displayed. | Validate permission, display permitted fields. | Limited status-only view. | No permission/not found. | Forbidden. | No personal detail beyond policy. | ADM-FR-001,052; ADM-US-001; ADM-AC-001; ADM-BR-039,044. | Detail scope. |
| ADM-UC-002 Search User | Admin tìm User. | Admin/System | Admin enters criteria. | Authorized. | Results shown. | Validate criteria/scope, display matches. | No results. | Invalid criteria. | Forbidden. | Scope-bound. | ADM-FR-002; ADM-US-002; ADM-AC-002; ADM-BR-044. | Search fields. |
| ADM-UC-003 Filter User | Admin lọc User. | Admin/System | Admin selects filters. | Authorized. | Filtered results shown. | Validate filters, apply. | Multiple filters. | Invalid filter. | Forbidden. | Scope-bound. | ADM-FR-003; ADM-AC-003; ADM-BR-044. | Filter catalog. |
| ADM-UC-004 Update User Information | Admin cập nhật User. | Admin/System | Admin saves change. | Has permission; field editable. | User info updated. | Validate, confirm, update, audit if needed. | Cancel action. | Out of scope change. | Governance violation. | Audit sensitive updates. | ADM-FR-004; ADM-AC-004; ADM-BR-019,040,045. | Editable fields. |
| ADM-UC-005 Deactivate User | Admin deactivate account. | Admin/System | Admin selects deactivate. | Target eligible. | Account deactivated. | Validate, reason, confirm, deactivate, audit. | Already deactivated. | Invalid target. | Last admin protection. | Audit required. | ADM-FR-005; ADM-US-003; ADM-AC-005; ADM-BR-023,035. | Reason required? |
| ADM-UC-006 Reactivate User | Admin reactivate account. | Admin/System | Admin selects reactivate. | Target eligible. | Account active per policy. | Validate, confirm, reactivate, audit. | Still locked if policy. | Not eligible. | Status conflict. | Audit required. | ADM-FR-006; ADM-US-003; ADM-AC-006; ADM-BR-023,033. | Reactivate+unlock relationship. |
| ADM-UC-007 Temporary Lock Account | Staff/Admin lock account. | Staff/Admin/System | Actor selects lock. | Has lock permission and scope. | Account locked. | Choose account, reason, validate scope, confirm, lock, audit. | Admin locks wider scope. | Staff self-lock/Admin-lock. | Scope violation. | Audit required. | ADM-FR-007,043; ADM-US-013; ADM-AC-007,008; ADM-BR-003-005,022,034. | Lock duration. |
| ADM-UC-008 Unlock Account | Admin unlock account. | Admin/System | Admin selects unlock. | Account locked. | Account unlocked if eligible. | Validate, confirm, unlock, audit. | Auto expiry if policy. | Account not locked/deactivated. | Status conflict. | Audit required. | ADM-FR-008; ADM-US-014; ADM-AC-009; ADM-BR-022,033. | Staff unlock? |
| ADM-UC-009 View Staff | Admin xem Staff. | Admin/System | Admin opens Staff list. | Authorized. | Staff info displayed. | Validate permission, display. | Empty list. | No permission. | Forbidden. | Limited info by policy. | ADM-FR-009,053; ADM-AC-010; ADM-BR-041,044. | Staff fields. |
| ADM-UC-010 Manage Staff | Admin quản lý Staff. | Admin/System | Admin performs staff action. | Has Manage Staff. | Staff updated. | Select Staff/action, validate, confirm, update, audit if needed. | Cancel action. | Governance violation. | Invalid status. | Audit sensitive changes. | ADM-FR-010-012; ADM-US-008; ADM-AC-011; ADM-BR-019,041. | Staff lifecycle. |
| ADM-UC-011 Create Ticket | Actor tạo ticket. | User/Staff/Admin/System | Actor submits ticket. | Actor allowed. | Ticket New. | Enter title/content/category/priority, validate, create. | Staff creates on behalf if policy. | Missing title/content. | Invalid ticket. | Minimum info required. | ADM-FR-013,040-042; ADM-US-004 partial; ADM-AC-012,013; ADM-BR-009-011. | Who can create ticket? |
| ADM-UC-012 Receive Ticket | Staff tiếp nhận ticket. | Staff/System | Staff receives from queue. | Staff queue access. | Ticket Received. | Select ticket, validate, update status, history. | Remains unassigned. | Already received. | Assignment conflict. | Ticket history. | ADM-FR-014,059; ADM-US-004; ADM-AC-014; ADM-BR-016,017. | Queue policy. |
| ADM-UC-013 Assign Ticket | Gán ticket. | Staff/Admin/System | Actor assigns. | Actor authorized; Staff valid. | Ticket assigned. | Choose ticket/assignee, validate, assign, history. | Assign group queue. | Invalid Staff/no permission. | Invalid assignee. | Scope check. | ADM-FR-015; ADM-US-005 partial; ADM-AC-015; ADM-BR-015,016. | Self assign? |
| ADM-UC-014 Update Ticket | Cập nhật ticket. | Staff/Admin/System | Actor updates ticket. | Has ticket scope. | Ticket updated. | Edit fields/comment, validate, update, history. | Add comment only. | Invalid status/priority/category. | Invalid ticket state. | Ticket history. | ADM-FR-016,048-051; ADM-US-005; ADM-AC-016; ADM-BR-009-011,017,042. | Comment visibility. |
| ADM-UC-015 Escalate Ticket | Staff escalate ticket. | Staff/Admin/System | Staff selects escalate. | Ticket in scope; eligible. | Ticket Escalated. | Enter reason, validate, change status, notify/review queue. | Transfer group. | Missing reason/no scope. | Escalation denied. | Admin review. | ADM-FR-017,060; ADM-US-006; ADM-AC-017; ADM-BR-014,018. | Escalation criteria. |
| ADM-UC-016 Resolve Ticket | Resolve ticket. | Staff/Admin/System | Actor submits resolution. | Ticket resolvable. | Ticket Resolved. | Enter resolution, validate, set Resolved, history. | Escalate instead. | Missing resolution. | Invalid state. | Resolution required if policy. | ADM-FR-018; ADM-US-007; ADM-AC-018; ADM-BR-013,017. | Resolution fields. |
| ADM-UC-017 Close Ticket | Close ticket. | Staff/Admin/System | Actor closes. | Ticket meets closure. | Ticket Closed. | Validate closure, confirm, close, history. | Auto close if policy. | Not resolved/not eligible. | Closure denied. | Closure reason if policy. | ADM-FR-019; ADM-AC-019; ADM-BR-012,013. | User confirmation? |
| ADM-UC-018 Reopen Ticket | Reopen ticket. | Staff/Admin/System | Actor reopens. | Closed and reopen allowed. | Ticket Reopened/status policy. | Enter reason, validate, reopen, history. | Return Assigned/In Progress. | Policy disallows. | Reopen denied. | History. | ADM-FR-020; ADM-AC-020; ADM-BR-012,043. | Reopen window. |
| ADM-UC-019 Search Ticket | Search ticket. | Staff/Admin/System | Actor enters criteria. | Authorized. | Results shown. | Validate scope, search. | No results. | Invalid criteria. | Scope violation. | Scope-bound. | ADM-FR-021; ADM-AC-021; ADM-BR-044. | Search fields. |
| ADM-UC-020 Filter Ticket | Filter ticket. | Staff/Admin/System | Actor selects filter. | Authorized. | Filtered results shown. | Validate filters, apply. | Multi-filter. | Invalid filter. | Scope violation. | Scope-bound. | ADM-FR-022; ADM-AC-022; ADM-BR-044. | Filter catalog. |
| ADM-UC-021 View Activity Log | View activity log. | Staff/Admin/System | Actor opens log. | Has permission. | Log displayed. | Select scope, validate, display. | Search/filter log. | No permission. | Forbidden. | Log not audit. | ADM-FR-023-025; ADM-US-016; ADM-AC-023; ADM-BR-025,047. | Log event catalog. |
| ADM-UC-022 View Audit | Admin xem audit. | Admin/System | Admin opens audit. | Has View Audit. | Audit displayed. | Select scope/filter, validate, display. | Search audit. | No permission. | Forbidden. | Audit read-only. | ADM-FR-026,027,046; ADM-US-015; ADM-AC-024; ADM-BR-026,048. | Audit retention. |
| ADM-UC-023 View Configuration | Admin xem cấu hình. | Admin/System | Admin opens config. | Has permission. | Config displayed. | Validate, display allowed config. | Sensitive config limited. | No permission. | Forbidden. | Read-only unless update. | ADM-FR-028; ADM-AC-025 partial; ADM-BR-006. | Config list. |
| ADM-UC-024 Update Configuration | Admin cập nhật cấu hình. | Admin/System | Admin saves config. | Has update permission. | Config updated. | Enter value, validate, confirm, update, audit. | Extra confirmation. | Invalid value/no permission. | Invalid configuration. | Audit required. | ADM-FR-029,054; ADM-US-017; ADM-AC-025,026; ADM-BR-024,036. | Sensitive config rules. |
| ADM-UC-025 View System Dashboard | Admin xem dashboard quản trị. | Admin/System | Admin opens dashboard. | Has permission. | Operational summary shown. | Select filters, validate, display summary. | Empty state. | No permission. | Forbidden. | No personal detail unless policy. | ADM-FR-030,031; ADM-US-018; ADM-AC-027; ADM-BR-039,049. | Staff partial view? |
| ADM-UC-026 Manage Role | Admin quản lý Role. | Admin/System | Admin changes Role. | Has Manage Role. | Role updated. | Validate name/usage/impact, confirm, update, audit. | View assignment only. | Duplicate/role in use. | Governance violation. | Audit required. | ADM-FR-032,055,057; ADM-US-009; ADM-BR-020,027,029. | Delete Role policy. |
| ADM-UC-027 Manage Permission | Admin quản lý Permission. | Admin/System | Admin changes Permission. | Has Manage Permission. | Permission updated. | Validate scope/impact, confirm, update, audit. | View assignment only. | Wrong scope/governance issue. | Governance violation. | Audit required. | ADM-FR-033,056,058; ADM-US-010; ADM-BR-007,021,028. | Permission catalog. |
| ADM-UC-028 Assign Role | Admin gán Role. | Admin/System | Admin assign. | Role/account valid. | Role assigned. | Validate, confirm, assign, audit. | Already assigned. | Invalid Role/account. | Governance violation. | Audit required. | ADM-FR-034,044; ADM-US-011; ADM-AC-028; ADM-BR-008,020. | Effectivity policy. |
| ADM-UC-029 Revoke Role | Admin thu hồi Role. | Admin/System | Admin revoke. | Account has Role. | Role revoked. | Validate impact, confirm, revoke, audit. | Role not assigned. | Last Admin risk. | Governance violation. | Self-protection. | ADM-FR-035,044; ADM-US-011; ADM-AC-029; ADM-BR-008,020,031. | Approval required? |
| ADM-UC-030 Assign Permission | Admin gán Permission. | Admin/System | Admin assign permission. | Permission/scope valid. | Permission assigned. | Validate scope, confirm, assign, audit. | Already assigned. | Wrong scope. | Permission scope error. | Audit required. | ADM-FR-036,045; ADM-US-012; ADM-AC-030; ADM-BR-007,021,028. | Direct vs Role assignment. |
| ADM-UC-031 Revoke Permission | Admin thu hồi Permission. | Admin/System | Admin revoke permission. | Permission assigned. | Permission revoked. | Validate impact, confirm, revoke, audit. | Not assigned. | Loss of admin capability. | Governance violation. | Audit required. | ADM-FR-037,045; ADM-US-012; ADM-AC-031; ADM-BR-007,021,030. | Effectivity policy. |
| ADM-UC-032 Broadcast Announcement | Admin phát thông báo. | Admin/System | Admin creates announcement. | Policy approved; has permission. | Announcement broadcast. | Enter content/scope, validate, confirm, publish. | Schedule if policy. | Invalid content/scope. | Announcement denied. | Conditional scope. | ADM-FR-038; ADM-US-019; ADM-AC-034; ADM-BR-037. | In release? |
| ADM-UC-033 View Maintenance Status | Admin xem maintenance. | Admin/System | Admin opens status. | Policy approved. | Status displayed. | Validate permission, display status. | No active maintenance. | No permission. | Forbidden. | Conditional scope. | ADM-FR-039; ADM-US-020; ADM-AC-035; ADM-BR-038. | In release? |

### 3.7 Cross-cutting Use Case Specifications

| UC | Description | Actors | Trigger | Preconditions | Postconditions | Main Success Scenario | Alternative Flows | Exception Flows | Business Exceptions | Special Requirements | Traceability | Assumptions/Open Questions |
|---|---|---|---|---|---|---|---|---|---|---|---|---|
| CBR-UC-001 Validate Ownership and Access Scope | Chuẩn kiểm tra ownership/scope cho toàn hệ thống. | System; Supporting: all actors | Actor truy cập dữ liệu hoặc hành động bảo vệ. | Actor authenticated if protected. | Allowed or denied. | Identify actor, owner, permission, scope; decide. | Limited Staff/Admin view if policy. | Missing permission. | Permission denied. | Applies all modules. | GBR-001-009,018,026; VAL-001-004. | Staff/Admin scope unresolved. |
| CBR-UC-002 Handle Business Validation Error | Xử lý dữ liệu không hợp lệ. | System | Validation fails. | Action submitted. | No state change. | Detect invalid data, reject action, provide business message. | Suggest correction if possible. | None. | Validation error. | No partial state change. | GBR-011,027; VAL catalog; Error Handling Principles. | Message standard cần xác nhận. |
| CBR-UC-003 Record Audit Event | Ghi audit cho action nhạy cảm. | System; Supporting: Admin/Staff/User | Sensitive action completed. | Audit policy requires. | Audit event recorded. | Identify actor/action/object/time/reason if policy, record event. | Conditional audit events. | Audit unavailable policy gap. | Audit required. | Cannot replace with activity log. | GBR-010,024; Audit Requirements. | Audit fields/retention. |
| CBR-UC-004 Record Activity History | Ghi activity history. | System | History-worthy action completed. | History policy requires. | History visible by scope. | Identify event, viewer scope, record history. | User-facing or admin-facing history. | Policy missing. | History unavailable. | Visibility controlled. | Activity History Requirements; GBR-010,024. | Retention. |
| CBR-UC-005 Apply Search Filter Sort Standard | Chuẩn search/filter/sort. | System; Supporting: User/Staff/Admin | Actor searches/filters/sorts. | Actor has access to list/report. | Results returned within scope. | Validate criteria, apply scope, return ordered data. | Empty result. | Invalid criteria. | Scope violation. | No data mutation. | Search & Filter Standards; GBR-026. | Pagination policy. |
| CBR-UC-006 Export Business Data | Xuất dữ liệu nghiệp vụ nếu policy cho phép. | User/Admin; Supporting: System | Actor selects export. | Export policy allows; actor has scope. | Export generated. | Validate format/scope, export selected data, audit if needed. | Empty export if policy. | Format denied or scope issue. | Export denied. | Same scope as source view. | Export & Import Requirements; GBR-025. | Formats in scope? |
| CBR-UC-007 Send Business Notification | Gửi notification nếu policy cho phép. | System; Supporting: Recipient | Trigger event occurs. | Notification policy approved. | Notification sent or skipped by policy. | Determine event/recipient/purpose, validate, send. | User preference if policy. | Missing recipient/policy. | Notification not sent. | No technical channel specified. | Notification Requirements; GBR-030. | Notification in release? |
| CBR-UC-008 Handle Maintenance Mode | Xử lý trạng thái bảo trì. | System/Admin | Maintenance status active or viewed. | Maintenance policy approved. | Actor informed or access controlled by policy. | Determine maintenance state, show message/status, restrict if policy. | Announcement. | Actor lacks permission to change/view detail. | Maintenance access limitation. | Business continuity. | Maintenance Policy; Error Handling Principles. | Maintenance scope. |

## 4. Use Case Relationship

### 4.1 Include Relationships

| Base Use Case | Included Use Case | Business Rationale |
|---|---|---|
| All protected use cases | IAM-UC-009 Validate Session and Token | Mọi hành động được bảo vệ cần session hợp lệ. |
| All protected use cases | IAM-UC-010 Validate Authorization | Mọi hành động được bảo vệ cần kiểm tra quyền. |
| All user-owned data use cases | CBR-UC-001 Validate Ownership and Access Scope | Bảo vệ ownership xuyên hệ thống. |
| RCM allocation/adjustment use cases | RCM-UC-026 Validate Capital Balance | Mọi thay đổi vốn cần kiểm tra balance. |
| TTM scheduling use cases | TTM-UC-031 Validate Timeline Eligibility | Task phải đủ điều kiện Timeline. |
| TER dashboard/report/history use cases | TER-UC-024 Validate Access Scope | Báo cáo và dashboard phải giới hạn phạm vi quyền. |
| ADM sensitive action use cases | CBR-UC-003 Record Audit Event | Hành động quản trị nhạy cảm cần audit. |
| Search/filter use cases | CBR-UC-005 Apply Search Filter Sort Standard | Chuẩn hóa kết quả và scope. |

### 4.2 Extend Relationships

| Base Use Case | Extension Use Case | Extension Condition |
|---|---|---|
| IAM-UC-003 Login | IAM-UC-008 Forgot Password | Actor không thể đăng nhập vì quên mật khẩu. |
| RCM-UC-012/013 Allocate Capital | RCM-UC-016 Allow Over Allocation | Allocation vượt available capital và policy cho phép. |
| RCM-UC-004 Close Capital Cycle | RCM-UC-017 Transfer Remaining Capital | Remaining capital dương và transfer policy cho phép. |
| TTM-UC-018 Schedule Task | TTM-UC-021 Drag & Drop Timeline Task | User thay đổi schedule qua Timeline. |
| TTM-UC-025 Complete Task | TER-UC-001/003 Record Actual | Task hoàn thành và User ghi Actual. |
| TER-UC-012 View Report | TER-UC-013 Export Report | User muốn xuất report và policy cho phép. |
| ADM-UC-016 Resolve Ticket | ADM-UC-017 Close Ticket | Ticket đã resolved và closure condition đạt. |
| ADM-UC-017 Close Ticket | ADM-UC-018 Reopen Ticket | Ticket đóng nhầm hoặc issue chưa xử lý xong. |

### 4.3 Generalization Relationships

| General Use Case | Specialized Use Cases | Business Rationale |
|---|---|---|
| Manage Account Status | IAM-UC-014, IAM-UC-015, IAM-UC-016, IAM-UC-017, ADM-UC-005, ADM-UC-006, ADM-UC-007, ADM-UC-008 | Các use case đều thay đổi trạng thái account. |
| Manage Access Rights | IAM-UC-019 to IAM-UC-024, ADM-UC-026 to ADM-UC-031 | Các use case đều quản lý role/permission. |
| Manage Capital | RCM-UC-008 to RCM-UC-017 | Các use case đều thay đổi hoặc xử lý nguồn vốn. |
| Manage Task Status | TTM-UC-017, TTM-UC-023 to TTM-UC-027 | Các use case đều thay đổi lifecycle Task. |
| Generate Insight | TER-UC-008 to TER-UC-022 | Các use case đều cung cấp phân tích, KPI hoặc báo cáo. |
| Manage Ticket | ADM-UC-011 to ADM-UC-020 | Các use case đều thuộc vòng đời ticket. |

## 5. Business Process Mapping

| Business Process | Use Cases | Functional Requirement Mapping |
|---|---|---|
| Identity Access Process | IAM-UC-001 to IAM-UC-010, CBR-UC-001 | IAM-FR-001 to IAM-FR-018; IAM-FR-040,045 |
| Account Governance Process | IAM-UC-011 to IAM-UC-018, ADM-UC-001 to ADM-UC-010 | IAM-FR-019 to IAM-FR-026; ADM-FR-001 to ADM-FR-012; ADM-FR-052,053 |
| Role and Permission Governance | IAM-UC-019 to IAM-UC-024, ADM-UC-026 to ADM-UC-031 | IAM-FR-027 to IAM-FR-032; ADM-FR-032 to ADM-FR-037; ADM-FR-055 to ADM-FR-058 |
| Resource Capital Planning | RCM-UC-001 to RCM-UC-011 | RCM-FR-001 to RCM-FR-017; RCM-FR-043,047-049 |
| Resource Allocation and Balance | RCM-UC-012 to RCM-UC-021, RCM-UC-026 | RCM-FR-018 to RCM-FR-033; RCM-FR-044,045 |
| Resource History and Access | RCM-UC-022 to RCM-UC-025, RCM-UC-027 | RCM-FR-034 to RCM-FR-042; RCM-FR-046,050 |
| Task Planning | TTM-UC-001 to TTM-UC-017, TTM-UC-030 | TTM-FR-001 to TTM-FR-028; TTM-FR-052-056 |
| Timeline Scheduling | TTM-UC-018 to TTM-UC-021, TTM-UC-031 | TTM-FR-029 to TTM-FR-037; TTM-FR-057 |
| Task Execution State | TTM-UC-022 to TTM-UC-027 | TTM-FR-038 to TTM-FR-046; TTM-FR-058-060 |
| Optional Task Planning Support | TTM-UC-028, TTM-UC-029 | TTM-FR-047 to TTM-FR-051 |
| Tracking and Evaluation | TER-UC-001 to TER-UC-007, TER-UC-023 | TER-FR-001 to TER-FR-012; TER-FR-033 to TER-FR-042; TER-FR-050 |
| Reporting and Dashboard | TER-UC-008 to TER-UC-022, TER-UC-025 | TER-FR-013 to TER-FR-032; TER-FR-043 to TER-FR-058 |
| Support Workflow | ADM-UC-011 to ADM-UC-020 | ADM-FR-013 to ADM-FR-022; ADM-FR-040 to ADM-FR-042; ADM-FR-047 to ADM-FR-051; ADM-FR-059,060 |
| Administration Monitoring | ADM-UC-021 to ADM-UC-025, ADM-UC-032, ADM-UC-033 | ADM-FR-023 to ADM-FR-031; ADM-FR-038,039,046,054 |
| Cross-cutting Governance | CBR-UC-001 to CBR-UC-008 | GBR-001 to GBR-030; VAL-001 to VAL-028; Business Policies |

## 6. Actor - Use Case Matrix

| Use Case Group | Guest | User | Staff | Admin |
|---|---|---|---|---|
| IAM-UC-001 Landing | View | View | View | View |
| IAM-UC-002 Register | Execute | - | - | - |
| IAM-UC-003 to IAM-UC-008 Account Self-service | - | Execute | Execute | Execute |
| IAM-UC-009 to IAM-UC-010 Validation | - | Monitor as subject | Monitor as subject | Monitor as subject |
| IAM-UC-011 to IAM-UC-018 Account/Staff Management | - | - | Execute limited for lock | Manage |
| IAM-UC-019 to IAM-UC-026 Role/Permission/Config/Audit | - | - | View limited if authorized | Manage, Approve |
| IAM-UC-027 Unauthorized | Execute as denied actor | - | - | - |
| IAM-UC-028 Forbidden | - | Execute as denied actor | Execute as denied actor | Execute as denied actor |
| RCM-UC-001 to RCM-UC-024 Resource Owner Actions | - | Execute, Manage | - | - |
| RCM-UC-025 to RCM-UC-027 Resource Access Validation/Summary | - | Execute as owner | View limited | View limited |
| TTM-UC-001 to TTM-UC-029 Task Owner Actions | - | Execute, Manage | - | - |
| TTM-UC-030 to TTM-UC-031 Task/Timeline Validation | - | Execute as owner | View limited | View limited |
| TER-UC-001 to TER-UC-023 Tracking/Reporting Owner Actions | - | Execute, View, Export if allowed | - | - |
| TER-UC-024 to TER-UC-025 Scope/Empty Handling | - | Monitor as subject | View limited | View limited |
| ADM-UC-001 to ADM-UC-010 User/Staff Admin | - | - | - | Manage |
| ADM-UC-011 Ticket Creation | - | Execute | Execute | Execute |
| ADM-UC-012 to ADM-UC-020 Ticket Workflow | - | View own if policy | Execute, Manage limited | Manage |
| ADM-UC-021 Activity Log | - | - | View limited | Manage |
| ADM-UC-022 Audit | - | - | View limited if authorized | Manage, Monitor |
| ADM-UC-023 to ADM-UC-033 Admin Operations | - | - | View limited for dashboard/status if policy | Manage, Approve, Monitor |
| CBR-UC-001 to CBR-UC-008 Cross-cutting | View if public | Execute as subject | Execute/Monitor per permission | Manage/Monitor per permission |

## 7. Module - Use Case Matrix

| Module | Use Cases | Business Goal | Business Rule Reference |
|---|---|---|---|
| Identity & Authorization | IAM-UC-001 to IAM-UC-028 | Secure access and role-based governance | IAM-BR-001 to IAM-BR-045; GBR-001 to GBR-010 |
| Resource Capital Management | RCM-UC-001 to RCM-UC-027 | Resource visibility, allocation and balance control | RCM-BR-001 to RCM-BR-055; GBR-015,016 |
| Task & Timeline Management | TTM-UC-001 to TTM-UC-031 | Task planning, scheduling, progress and lifecycle | TTM-BR-001 to TTM-BR-060; GBR-013,014 |
| Tracking, Evaluation & Reporting | TER-UC-001 to TER-UC-025 | Planned vs Actual evaluation and insight | TER-BR-001 to TER-BR-050; GBR-017 to GBR-020 |
| Administration & Support | ADM-UC-001 to ADM-UC-033 | Operational support, governance and auditability | ADM-BR-001 to ADM-BR-050; GBR-021 to GBR-024 |
| Cross-cutting Requirements | CBR-UC-001 to CBR-UC-008 | Standardized governance across all modules | GBR-001 to GBR-030; VAL-001 to VAL-028 |

## 8. Functional Requirement Traceability

| Functional Requirement Range | Use Case Mapping | Acceptance Criteria Mapping | User Story Mapping |
|---|---|---|---|
| IAM-FR-001 to IAM-FR-018 | IAM-UC-001 to IAM-UC-010, IAM-UC-027, IAM-UC-028 | IAM-AC-001 to IAM-AC-028 | IAM-US-001 to IAM-US-010, IAM-US-027, IAM-US-028 |
| IAM-FR-019 to IAM-FR-026 | IAM-UC-011 to IAM-UC-018 | IAM-AC-029 to IAM-AC-036 | IAM-US-011 to IAM-US-018 |
| IAM-FR-027 to IAM-FR-045 | IAM-UC-019 to IAM-UC-026 | IAM-AC-037 to IAM-AC-048 | IAM-US-019 to IAM-US-026 |
| RCM-FR-001 to RCM-FR-009 | RCM-UC-001 to RCM-UC-007 | RCM-AC-001 to RCM-AC-010 | RCM-US-001 to RCM-US-005 |
| RCM-FR-010 to RCM-FR-017 | RCM-UC-008 to RCM-UC-011 | RCM-AC-011 to RCM-AC-018 | RCM-US-006 to RCM-US-009 |
| RCM-FR-018 to RCM-FR-029 | RCM-UC-012 to RCM-UC-017, RCM-UC-026 | RCM-AC-019 to RCM-AC-027, RCM-AC-037,038 | RCM-US-010 to RCM-US-014, RCM-US-022,024 |
| RCM-FR-030 to RCM-FR-050 | RCM-UC-018 to RCM-UC-027 | RCM-AC-028 to RCM-AC-044 | RCM-US-015 to RCM-US-026 |
| TTM-FR-001 to TTM-FR-017 | TTM-UC-001 to TTM-UC-013 | TTM-AC-001 to TTM-AC-012 | TTM-US-001 to TTM-US-008 |
| TTM-FR-018 to TTM-FR-028 | TTM-UC-014 to TTM-UC-017 | TTM-AC-013 to TTM-AC-022 | TTM-US-009 to TTM-US-012 |
| TTM-FR-029 to TTM-FR-037 | TTM-UC-018 to TTM-UC-021, TTM-UC-031 | TTM-AC-023 to TTM-AC-030 | TTM-US-013 to TTM-US-015, TTM-US-027 |
| TTM-FR-038 to TTM-FR-046 | TTM-UC-022 to TTM-UC-027 | TTM-AC-031 to TTM-AC-038 | TTM-US-019 to TTM-US-024 |
| TTM-FR-047 to TTM-FR-060 | TTM-UC-028 to TTM-UC-031 | TTM-AC-039 to TTM-AC-046 | TTM-US-025 to TTM-US-028 |
| TER-FR-001 to TER-FR-012 | TER-UC-001 to TER-UC-007 | TER-AC-001 to TER-AC-016 | TER-US-001 to TER-US-008 |
| TER-FR-013 to TER-FR-032 | TER-UC-008 to TER-UC-022 | TER-AC-017 to TER-AC-039 | TER-US-009 to TER-US-021 |
| TER-FR-033 to TER-FR-050 | TER-UC-006, TER-UC-007, TER-UC-022 to TER-UC-025 | TER-AC-011 to TER-AC-016, TER-AC-034 to TER-AC-042 | TER-US-005 to TER-US-008, TER-US-021, TER-US-022 |
| TER-FR-051 to TER-FR-060 | TER-UC-012, TER-UC-024 | TER-AC-023,024,040-042 | TER-US-013, TER-US-022 |
| ADM-FR-001 to ADM-FR-012 | ADM-UC-001 to ADM-UC-010 | ADM-AC-001 to ADM-AC-011 | ADM-US-001 to ADM-US-003, ADM-US-008 |
| ADM-FR-013 to ADM-FR-022 | ADM-UC-011 to ADM-UC-020 | ADM-AC-012 to ADM-AC-022 | ADM-US-004 to ADM-US-007 |
| ADM-FR-023 to ADM-FR-031 | ADM-UC-021 to ADM-UC-025 | ADM-AC-023 to ADM-AC-027 | ADM-US-015 to ADM-US-018 |
| ADM-FR-032 to ADM-FR-060 | ADM-UC-026 to ADM-UC-033, ADM-UC-007, ADM-UC-008 | ADM-AC-028 to ADM-AC-036 | ADM-US-009 to ADM-US-020 |
| Volume 7 GBR/VAL/Policies | CBR-UC-001 to CBR-UC-008 | Cross-volume AC where applicable | Cross-volume US where applicable |

## 9. Business Rule Traceability

| Business Rule Group | Use Case Mapping | Workflow Mapping | Acceptance Criteria Mapping |
|---|---|---|---|
| IAM-BR-001 to IAM-BR-045 | IAM-UC-001 to IAM-UC-028 | Identity login, authorization, account governance, role/permission workflows | IAM-AC-001 to IAM-AC-048 |
| RCM-BR-001 to RCM-BR-055 | RCM-UC-001 to RCM-UC-027 | Capital cycle, allocation, adjustment, close, history workflows | RCM-AC-001 to RCM-AC-044 |
| TTM-BR-001 to TTM-BR-060 | TTM-UC-001 to TTM-UC-031 | Task create/edit/plan/schedule/progress/complete/archive workflows | TTM-AC-001 to TTM-AC-046 |
| TER-BR-001 to TER-BR-050 | TER-UC-001 to TER-UC-025 | Record actual, evaluate, dashboard, report, trend workflows | TER-AC-001 to TER-AC-042 |
| ADM-BR-001 to ADM-BR-050 | ADM-UC-001 to ADM-UC-033 | Ticket, lock/unlock, role, permission, configuration workflows | ADM-AC-001 to ADM-AC-036 |
| GBR-001 to GBR-030 | CBR-UC-001 to CBR-UC-008 and all protected module use cases | Cross-cutting workflows | Cross-volume AC |

## 10. Workflow Summary

| Use Case Group | Main Flow | Alternative Flow | Exception Flow |
|---|---|---|---|
| IAM-UC-001 to IAM-UC-008 | Actor submits identity/self-service request, system validates, action completes. | Verification, recovery, already-authenticated or partial access paths. | Invalid credential, locked account, invalid data, missing permission. |
| IAM-UC-009 to IAM-UC-028 | System validates session/authorization or Admin manages accounts/roles/permissions. | Limited scope access, view-only, no-op if already assigned. | Unauthorized, forbidden, governance violation, invalid status. |
| RCM-UC-001 to RCM-UC-007 | User manages cycle lifecycle after validation. | Draft/reopen/transfer-related variants. | Invalid cycle type, duplicate period, closed cycle. |
| RCM-UC-008 to RCM-UC-017 | User sets, adjusts, allocates, reallocates, releases or transfers capital. | Over allocation confirmation, partial release, no transfer. | Resource not enough, invalid amount, invalid target, policy denied. |
| RCM-UC-018 to RCM-UC-027 | User/system views balance/history or validates ownership/balance. | Empty history, limited Staff/Admin view. | Permission denied, insufficient data, invalid filter. |
| TTM-UC-001 to TTM-UC-017 | User creates, edits, classifies, estimates and plans Task. | Draft remains incomplete, optional category/tag, duplicate. | Invalid name, invalid status, invalid deadline, no ownership. |
| TTM-UC-018 to TTM-UC-021 | User schedules or moves Task on Timeline. | Conflict warning or confirmation. | No Time Capital, deadline violation, timeline conflict denied. |
| TTM-UC-022 to TTM-UC-031 | User updates lifecycle/progress or system validates ownership/eligibility. | Pause/resume/reopen/restore variants. | Invalid status, progress out of range, policy denied. |
| TER-UC-001 to TER-UC-007 | User records actual, system calculates variance and efficiency. | Partial data evaluation. | Task not completed, invalid actual, insufficient data. |
| TER-UC-008 to TER-UC-025 | User views dashboard/report/history/trend/KPI; system handles scope and empty states. | Empty state, filter comparison, export. | Invalid period/filter, permission denied, export denied. |
| ADM-UC-001 to ADM-UC-010 | Admin manages User/Staff/account status. | Limited view, no-op if status already set. | Governance violation, invalid target, no permission. |
| ADM-UC-011 to ADM-UC-020 | Ticket is created, received, assigned, updated, escalated, resolved, closed or reopened. | Queue assignment, auto-close, escalation. | Missing ticket data, invalid status, no assignee, no permission. |
| ADM-UC-021 to ADM-UC-033 | Admin/Staff views logs/audit/config/dashboard or manages roles/permissions/announcements/maintenance. | View-only, scheduled announcement, limited Staff view. | Invalid configuration, role/permission violation, no permission. |
| CBR-UC-001 to CBR-UC-008 | System applies global ownership, validation, audit, history, export and notification rules. | Conditional policy paths. | Missing policy, scope violation, validation error. |

## 11. Preconditions Catalog

| Precondition ID | Precondition | Applies To |
|---|---|---|
| PRE-001 | Actor is authenticated for protected use cases. | Most User/Staff/Admin use cases. |
| PRE-002 | Actor has required permission. | All protected actions. |
| PRE-003 | Actor owns the target data or has approved access scope. | User-owned Task, Capital, Report, History. |
| PRE-004 | Account is active and not locked/deactivated. | Login and protected actions. |
| PRE-005 | Target account exists and is eligible. | Account management, lock/unlock, role assignment. |
| PRE-006 | Capital Cycle exists and is in valid status. | Resource use cases. |
| PRE-007 | Available capital or over allocation policy supports requested allocation. | Allocation use cases. |
| PRE-008 | Task exists and belongs to User. | Task use cases. |
| PRE-009 | Task status allows requested transition. | Task lifecycle use cases. |
| PRE-010 | Task has Time Capital or estimated time for Timeline. | Timeline use cases. |
| PRE-011 | Task is Completed or eligible for final Actual. | Tracking and evaluation. |
| PRE-012 | Period/filter/report type is valid. | Dashboard/report/statistics. |
| PRE-013 | Ticket exists and actor has ticket scope. | Support use cases. |
| PRE-014 | Role/Permission exists and is valid. | Access governance. |
| PRE-015 | Configuration is within Admin scope. | Configuration use cases. |
| PRE-016 | Conditional policy is approved. | Recurring, reminder, export, announcement, maintenance. |

## 12. Postconditions Catalog

| Postcondition ID | Postcondition | Applies To |
|---|---|---|
| POST-001 | Account/session is created, ended, locked, unlocked, deactivated or reactivated. | Identity/Admin. |
| POST-002 | Role or Permission assignment is changed and audit recorded. | IAM/Admin. |
| POST-003 | Capital Cycle is created, activated, closed or reopened. | RCM. |
| POST-004 | Capital value, allocation, remaining balance or history is updated. | RCM. |
| POST-005 | Task is created, updated, scheduled, completed, cancelled, reopened, archived or restored. | TTM. |
| POST-006 | Timeline placement is created or changed. | Timeline. |
| POST-007 | Actual Time/Cost is recorded or updated. | TER. |
| POST-008 | Variance, efficiency, KPI, dashboard or report is displayed or marked insufficient. | TER. |
| POST-009 | Ticket is created, assigned, escalated, resolved, closed or reopened. | ADM Support. |
| POST-010 | Configuration, announcement or maintenance status is viewed or changed according to policy. | ADM. |
| POST-011 | Audit or activity history is recorded or displayed according to scope. | Cross-cutting/Admin. |
| POST-012 | If validation fails, no business state is changed. | All modules. |

## 13. Business Exceptions Catalog

| Exception | Description | Common Use Cases |
|---|---|---|
| Permission Denied | Actor lacks permission or ownership scope. | All protected UCs. |
| Authentication Required | Actor is not authenticated or session expired. | IAM/CBR. |
| Account Locked | Account cannot login or continue protected action. | IAM/ADM. |
| Invalid Status | Current status does not allow requested action. | Task, Cycle, Ticket, Account. |
| Resource Not Enough | Available capital insufficient and over allocation not allowed. | RCM allocation. |
| Cycle Closed | Closed cycle does not allow changes. | RCM. |
| Task Completed | Completed Task cannot change planning without reopen. | TTM/TER. |
| Task Not Completed | Task not eligible for final Actual/Evaluation. | TER. |
| Timeline Eligibility Failed | Task lacks Time Capital or schedule validity. | TTM Timeline. |
| Timeline Conflict | Task conflicts with another timeline item. | TTM. |
| Invalid Period | Period/filter/report range invalid. | TER/ADM reports. |
| Insufficient Data | KPI, variance or trend cannot be calculated. | TER. |
| Ticket Not Assignable | Ticket or Staff not eligible for assignment. | ADM Support. |
| Ticket Closed | Closed ticket cannot be updated unless reopened. | ADM Support. |
| Governance Violation | Role/permission/config action would violate governance. | IAM/ADM. |
| Invalid Configuration | Configuration value violates policy. | ADM. |
| Maintenance Active | System under maintenance affects access. | Cross-cutting/Admin. |

## 14. Edge Case Catalog

| Module | Edge Case Examples |
|---|---|
| Identity | Locked account login; role revoked during session; Admin self-revokes Admin; Staff self-locks; invalid token; multiple sessions. |
| Resource Capital | Allocation before cycle exists; adjustment below allocated amount; over allocation; transfer negative remaining; reopen after transfer; concurrent allocations. |
| Task | Deadline passed; Task without Time Capital; Completed Task planning edit; In Progress Task cancelled; multiple Tags; no Category; recurring deadline conflict. |
| Timeline | Drag over another Task; move beyond deadline; archived Task still on Timeline; restored Task with old schedule; Task with zero estimated time. |
| Tracking | Actual negative; missing Planned; missing Actual; Task reopened after evaluation; empty dashboard; large report; trend insufficient data. |
| Administration | Ticket without Staff; ticket closed by mistake; role in use removed; permission revoked mid-session; maintenance mode; invalid configuration. |
| Cross-cutting | Permission error should not reveal data; empty state should not show false KPI; export out of scope denied; history retention undefined. |

## 15. Use Case Priority

| Priority | Criteria | Use Case Examples |
|---|---|---|
| Critical | Required for access, core value, data protection or governance. | Login, Authorization, Capital Allocation, Create Task, Schedule Task, Complete Task, Record Actual, Role/Permission changes, Account Lock. |
| High | Required for standard business operation and frequent usage. | Update Profile, View Capital Summary, Search Task, View Dashboard, Ticket Workflow, View Audit. |
| Medium | Important for productivity, traceability or operational convenience. | Archive, Restore, Compare Periods, Export Report, Filter History, Reopen Ticket. |
| Low | Conditional or future-oriented policy-dependent capabilities. | Recurring Task, Reminder, Announcement, Maintenance Status, Authorized limited summary views. |

## 16. Risks

### 16.1 Requirement Risks

| Risk | Description | Mitigation |
|---|---|---|
| Use Case scope overlap | Module use cases may overlap in account, history, report or permission areas. | Use Volume 7 standards and relationship matrix. |
| Missing policy decisions | Recurring, reminder, export, notification and maintenance remain conditional. | Track open questions before implementation planning. |
| Ambiguous business exceptions | Exceptions may be interpreted differently by teams. | Use Business Exceptions Catalog as shared reference. |

### 16.2 Business Risks

| Risk | Description | Mitigation |
|---|---|---|
| Task reduced to simple to-do behavior | Use cases may be implemented without resource philosophy. | Keep RCM and TTM traceability to product philosophy. |
| KPI misuse | Evaluation use cases may be interpreted as judgment rather than improvement. | Use KPI definitions and policy wording from Volume 5. |
| Support overreach | Staff/Admin may access or alter personal data beyond intended scope. | Enforce ownership and permission use cases. |

### 16.3 Use Case Coverage Risks

| Risk | Description | Mitigation |
|---|---|---|
| FR not mapped to UC | Some low-level validation FRs may be overlooked. | Section 8 maps FR ranges to validation/system use cases. |
| AC not linked to UC | Acceptance criteria may be tested without business context. | Use traceability tables per module. |
| Cross-cutting requirements missed | Volume 7 has no FR IDs. | CBR use cases map global rules and validation policies. |

## 17. Open Questions

| Question ID | Open Question |
|---|---|
| UC-OQ-001 | Các policy conditional nào sẽ thuộc release đầu tiên: recurring, reminder, export, announcement, maintenance, notification? |
| UC-OQ-002 | Có cần tách thêm Use Case cho từng loại report riêng hay giữ nhóm View Report với report type? |
| UC-OQ-003 | Staff/Admin được xem dữ liệu cá nhân của User ở mức nào trong tình huống hỗ trợ? |
| UC-OQ-004 | Effective permission thay đổi có hiệu lực ngay trong session hay sau khi đăng nhập lại? |
| UC-OQ-005 | Có yêu cầu approval nhiều cấp cho Role/Permission/Configuration không? |
| UC-OQ-006 | Completion rule có yêu cầu progress 100% không? |
| UC-OQ-007 | Timeline conflict mặc định là từ chối, cảnh báo hay cho phép có xác nhận? |
| UC-OQ-008 | Actual có được ghi trước khi Task Completed không? |
| UC-OQ-009 | Export report có được audit bắt buộc không? |
| UC-OQ-010 | Retention cho audit, history và ticket là bao lâu ở mức nghiệp vụ? |

## 18. Suggested Improvements

| Improvement ID | Suggested Improvement | Business Rationale |
|---|---|---|
| UC-SI-001 | Thiết lập Use Case Review Checklist cho mọi thay đổi yêu cầu. | Bảo đảm use case, FR, AC và rule vẫn đồng bộ. |
| UC-SI-002 | Tạo policy decision log cho các use case conditional. | Giảm ambiguity trước khi triển khai. |
| UC-SI-003 | Bổ sung reason code chuẩn cho lock, reopen, cancel, adjust, escalate. | Tăng chất lượng audit và report. |
| UC-SI-004 | Xây dựng test scenario catalog dựa trên Business Exceptions Catalog. | Hỗ trợ QA bao phủ negative path. |
| UC-SI-005 | Thiết lập access review định kỳ cho Staff/Admin use cases. | Giảm rủi ro lạm quyền. |
| UC-SI-006 | Chuẩn hóa empty state và insufficient data wording. | Giảm hiểu nhầm trong dashboard/report. |
| UC-SI-007 | Duy trì traceability matrix theo version khi volume trước thay đổi. | Tránh tài liệu Use Case bị lỗi thời. |

# LifeBalance
# Volume 9 – User Stories & Product Backlog

## 1. Document Overview

### 1.1 Purpose

Tài liệu này chuyển hóa toàn bộ yêu cầu nghiệp vụ của LifeBalance từ các volume trước thành Product Backlog theo phương pháp Agile. Trọng tâm của tài liệu là xác định đầy đủ Epic, Feature, User Story, Acceptance Criteria, mức ưu tiên, phụ thuộc, rủi ro, kế hoạch phát hành và khuyến nghị lập kế hoạch Sprint.

Tài liệu đóng vai trò cầu nối giữa định hướng nghiệp vụ cấp cao và kế hoạch phát triển sản phẩm theo từng vòng lặp. Mỗi User Story được viết theo cấu trúc chuẩn “As a / I want / So that” và được kiểm chứng bằng Acceptance Criteria theo dạng Given / When / Then.

### 1.2 Scope

Phạm vi tài liệu bao gồm toàn bộ LifeBalance:

| Nhóm phạm vi | Nội dung bao phủ |
|---|---|
| Identity & Authorization | Đăng ký, đăng nhập, đăng xuất, hồ sơ cá nhân, phiên làm việc, kiểm soát quyền, vai trò, quyền hạn, trạng thái tài khoản |
| Resource Capital Management | Chu kỳ nguồn vốn, Time Capital, Money Capital, phân bổ, điều chỉnh, số dư, lịch sử nguồn lực |
| Task & Timeline Management | Task, lập kế hoạch, lịch thực hiện, Timeline, kéo thả, trạng thái, tiến độ, hoàn thành, lưu trữ |
| Tracking, Evaluation & Reporting | Actual Time, Actual Cost, Planned vs Actual, Variance, Efficiency, KPI, Dashboard, Report, phân tích xu hướng |
| Administration & Support | Quản lý User, Staff, Ticket, cấu hình nghiệp vụ, Audit, Activity Log, System Dashboard quản trị |
| Cross-cutting Requirements | Quy tắc dùng chung, kiểm tra hợp lệ, lịch sử, Audit, tìm kiếm, lọc, xuất dữ liệu, thông báo, duy trì vận hành |

### 1.3 Objectives

Tài liệu hướng tới các mục tiêu sau:

1. Chuẩn hóa toàn bộ Product Backlog của LifeBalance theo Epic, Feature và User Story.
2. Đảm bảo mọi Functional Requirement từ Volume 2 đến Volume 7 đều được ánh xạ sang ít nhất một User Story.
3. Cung cấp Acceptance Criteria đủ rõ để Product Owner, Scrum Master, Business Analyst và nhóm phát triển thống nhất phạm vi hoàn thành.
4. Xác định MVP, Release Planning, Sprint Planning và Product Roadmap ở mức nghiệp vụ.
5. Tạo cơ sở cho quản lý thay đổi, đánh giá ưu tiên và kiểm soát phạm vi sản phẩm.

### 1.4 Audience

| Đối tượng | Mục đích sử dụng tài liệu |
|---|---|
| Product Owner | Quản lý Product Backlog, ưu tiên phát triển, xác định MVP và Release |
| Scrum Master | Hỗ trợ lập Sprint, quản lý phụ thuộc và loại bỏ trở ngại |
| Business Analyst | Duy trì traceability từ yêu cầu nghiệp vụ đến User Story |
| Development Team | Hiểu mục tiêu nghiệp vụ, phạm vi Story và điều kiện chấp nhận |
| QA / Tester | Thiết kế kiểm thử nghiệp vụ dựa trên Acceptance Criteria |
| Stakeholder quản trị | Theo dõi tiến độ hiện thực hóa mục tiêu sản phẩm |

## 2. Product Vision Summary

LifeBalance là hệ thống quản lý nguồn lực cá nhân giúp người dùng lập kế hoạch, phân bổ, theo dõi và đánh giá việc sử dụng hai nguồn lực hữu hạn: thời gian và tiền bạc. Sản phẩm dựa trên triết lý: mọi công việc đều tiêu tốn nguồn lực, và mọi nguồn lực đều hữu hạn.

Khác với To-do App truyền thống chỉ tập trung vào danh sách việc cần làm, LifeBalance xem mỗi công việc như một khoản đầu tư nguồn lực. Người dùng không chỉ tạo Task, mà còn phải hiểu mình có bao nhiêu nguồn lực, đã phân bổ vào đâu, đã sử dụng thực tế thế nào và hiệu quả ra sao.

Product Backlog trong tài liệu này được liên kết trực tiếp với Vision thông qua các năng lực sản phẩm sau:

| Vision Element | Backlog Alignment |
|---|---|
| Lập kế hoạch nguồn lực | Epic Resource Capital, Task Planning, Timeline Scheduling |
| Thực hiện công việc | Epic Task & Timeline, Task Progress, Task Completion |
| Đánh giá hiệu quả | Epic Tracking, Evaluation, Reporting, KPI |
| Kiểm soát vận hành | Epic Identity, Administration, Support, Cross-cutting |
| Cải thiện liên tục | Feature Trend Analysis, History, Personal KPI, Productivity Report |

Backlog được tổ chức theo hướng tăng dần giá trị: trước tiên người dùng phải có danh tính và quyền truy cập hợp lệ; sau đó thiết lập nguồn lực; tiếp theo tạo và lập kế hoạch Task; sau khi thực hiện thì ghi nhận Actual; cuối cùng đánh giá hiệu quả và vận hành hệ thống ổn định.

## 3. Story Mapping

Story Map mô tả hành trình sản phẩm từ góc nhìn hoạt động nghiệp vụ. Mỗi Activity được phân rã thành Task nghiệp vụ, Feature và User Story tương ứng.

### 3.1 Story Map tổng thể

| Activity | Task nghiệp vụ | Feature | User Story |
|---|---|---|---|
| Access LifeBalance | Truy cập Landing Page | Guest Access | US-IAM-001 |
| Access LifeBalance | Tạo tài khoản | Registration | US-IAM-002 |
| Access LifeBalance | Đăng nhập | Authentication | US-IAM-003 |
| Access LifeBalance | Đăng xuất | Authentication | US-IAM-004 |
| Access LifeBalance | Duy trì phiên làm việc | Session Management | US-IAM-005 |
| Manage Personal Identity | Xem hồ sơ | Profile Management | US-IAM-006 |
| Manage Personal Identity | Cập nhật hồ sơ | Profile Management | US-IAM-007 |
| Manage Personal Identity | Đổi mật khẩu | Credential Management | US-IAM-008 |
| Recover Access | Quên mật khẩu | Credential Recovery | US-IAM-009 |
| Control Access | Xác thực trạng thái tài khoản | Account Status | US-IAM-010 |
| Control Access | Kiểm tra quyền truy cập | Authorization | US-IAM-011 |
| Govern Access | Quản lý Role | Role Management | US-IAM-012, US-IAM-013 |
| Govern Access | Quản lý Permission | Permission Management | US-IAM-014, US-IAM-015 |
| Govern Access | Gán và thu hồi Role | Role Assignment | US-IAM-016, US-IAM-017 |
| Govern Access | Gán và thu hồi Permission | Permission Assignment | US-IAM-018, US-IAM-019 |
| Manage Account | Tìm kiếm User | User Management | US-IAM-020 |
| Manage Account | Xem chi tiết User | User Management | US-IAM-021 |
| Manage Account | Cập nhật User | User Management | US-IAM-022 |
| Manage Account | Khóa, mở, ngưng, kích hoạt tài khoản | Account Governance | US-IAM-023 đến US-IAM-026 |
| Monitor Access | Xem Audit | Audit View | US-IAM-027 |
| Monitor Access | Kiểm soát quyền cấu hình | Configuration Authorization | US-IAM-028 |
| Manage Capital | Tạo chu kỳ nguồn vốn | Capital Cycle | US-RCM-001 |
| Manage Capital | Cập nhật chu kỳ | Capital Cycle | US-RCM-002 |
| Manage Capital | Kích hoạt chu kỳ | Capital Cycle | US-RCM-003 |
| Manage Capital | Đóng, mở lại chu kỳ | Capital Cycle | US-RCM-004, US-RCM-005 |
| Define Capital | Thiết lập Time Capital | Capital Setup | US-RCM-006 |
| Define Capital | Thiết lập Money Capital | Capital Setup | US-RCM-007 |
| Adjust Capital | Điều chỉnh Time Capital | Capital Adjustment | US-RCM-008 |
| Adjust Capital | Điều chỉnh Money Capital | Capital Adjustment | US-RCM-009 |
| Allocate Capital | Phân bổ nguồn lực | Capital Allocation | US-RCM-010 |
| Allocate Capital | Phân bổ lại nguồn lực | Capital Reallocation | US-RCM-011 |
| Allocate Capital | Giải phóng nguồn lực | Capital Release | US-RCM-012 |
| Allocate Capital | Cho phép vượt mức | Over Allocation | US-RCM-013 |
| Review Capital | Xem nguồn lực khả dụng | Capital Balance | US-RCM-014 |
| Review Capital | Xem nguồn lực còn lại | Capital Balance | US-RCM-015 |
| Review Capital | Xem lịch sử phân bổ | Capital History | US-RCM-016 |
| Review Capital | Xem lịch sử điều chỉnh | Capital History | US-RCM-017 |
| Review Capital | Tìm kiếm, lọc và tổng hợp | Capital Inquiry | US-RCM-018 đến US-RCM-020 |
| Manage Task | Tạo Task | Task Management | US-TTM-001 |
| Manage Task | Cập nhật Task | Task Management | US-TTM-002 |
| Manage Task | Xóa, lưu trữ, khôi phục Task | Task Governance | US-TTM-003 đến US-TTM-005 |
| Manage Task | Nhân bản Task | Task Productivity | US-TTM-006 |
| Classify Task | Gán Category | Category Assignment | US-TTM-007 |
| Classify Task | Gán và gỡ Tag | Tag Assignment | US-TTM-008, US-TTM-009 |
| Plan Task | Đặt Priority | Priority | US-TTM-010 |
| Plan Task | Đặt Deadline | Deadline | US-TTM-011 |
| Plan Task | Ước lượng Time và Cost | Estimation | US-TTM-012, US-TTM-013 |
| Schedule Task | Đưa Task lên Timeline | Timeline Scheduling | US-TTM-014 |
| Schedule Task | Đổi lịch và kéo thả | Timeline Movement | US-TTM-015, US-TTM-016 |
| Review Task | Xem Timeline, tìm kiếm, lọc, sắp xếp | Task Inquiry | US-TTM-017 đến US-TTM-020 |
| Execute Task | Cập nhật tiến độ | Progress | US-TTM-021 |
| Execute Task | Tạm dừng, tiếp tục, hoàn thành, hủy, mở lại | Task Lifecycle | US-TTM-022 đến US-TTM-026 |
| Repeat Work | Thiết lập Recurring Task | Recurring Task | US-TTM-027 |
| Support Planning | Nhắc việc Task | Task Reminder | US-TTM-028 |
| Track Actual | Ghi nhận Actual Time | Actual Recording | US-TER-001 |
| Track Actual | Cập nhật Actual Time | Actual Recording | US-TER-002 |
| Track Actual | Ghi nhận Actual Cost | Actual Recording | US-TER-003 |
| Track Actual | Cập nhật Actual Cost | Actual Recording | US-TER-004 |
| Evaluate | Xem Planned vs Actual | Evaluation | US-TER-005 |
| Evaluate | Tính Variance | Variance Analysis | US-TER-006, US-TER-007 |
| Evaluate | Đánh giá Efficiency | Efficiency Evaluation | US-TER-008 |
| Evaluate | Tính Completion Rate | Productivity KPI | US-TER-009 |
| Analyze | Xem Summary và Statistics | Dashboard Analytics | US-TER-010 đến US-TER-014 |
| Analyze | Xem Utilization và Dashboard | Dashboard Analytics | US-TER-015, US-TER-016 |
| Report | Xem, tìm kiếm, lọc, xuất Report | Reporting | US-TER-017 đến US-TER-020 |
| Improve | Xem History, so sánh kỳ, xu hướng, KPI | Productivity Improvement | US-TER-021 đến US-TER-025 |
| Operate System | Xem, tìm kiếm, lọc User | User Administration | US-ADM-001 đến US-ADM-003 |
| Operate System | Cập nhật, ngưng, kích hoạt User | User Administration | US-ADM-004 đến US-ADM-006 |
| Operate System | Khóa và mở tài khoản | Account Operation | US-ADM-007, US-ADM-008 |
| Operate Staff | Xem và quản lý Staff | Staff Administration | US-ADM-009, US-ADM-010 |
| Support User | Tạo, nhận, gán, cập nhật Ticket | Ticket Management | US-ADM-011 đến US-ADM-014 |
| Support User | Chuyển cấp, xử lý, đóng, mở lại Ticket | Ticket Workflow | US-ADM-015 đến US-ADM-018 |
| Support User | Tìm kiếm và lọc Ticket | Ticket Inquiry | US-ADM-019, US-ADM-020 |
| Monitor Operation | Xem và tìm kiếm Activity Log | Operational Monitoring | US-ADM-021, US-ADM-022 |
| Monitor Governance | Xem và tìm kiếm Audit | Audit Monitoring | US-ADM-023, US-ADM-024 |
| Configure Operation | Xem và cập nhật cấu hình nghiệp vụ | Configuration | US-ADM-025, US-ADM-026 |
| Monitor Operation | Xem System Dashboard và Statistics | System Dashboard | US-ADM-027, US-ADM-028 |
| Govern Access | Quản lý Role và Permission | Governance | US-ADM-029 đến US-ADM-033 |
| Apply Standards | Tuân thủ quy tắc toàn cục | Cross-cutting Rules | US-CBR-001 |
| Apply Standards | Kiểm tra dữ liệu nghiệp vụ | Validation Standards | US-CBR-002 |
| Apply Standards | Lịch sử, Audit, Notification | Cross-cutting Governance | US-CBR-003 đến US-CBR-005 |
| Apply Standards | Tìm kiếm, lọc, xuất dữ liệu | Cross-cutting Inquiry | US-CBR-006, US-CBR-007 |
| Apply Standards | Xử lý lỗi nghiệp vụ | Error Handling | US-CBR-008 |

### 3.2 Nguyên tắc đọc Story Map

Story Map không thay thế Product Backlog chi tiết. Story Map giúp xác định thứ tự giá trị từ trái sang phải và từ trên xuống dưới. Các Story ở tầng nền tảng như Identity, quyền truy cập và chu kỳ nguồn vốn có mức phụ thuộc cao hơn nên thường được ưu tiên trước trong MVP.

## 4. Epic Catalog

| Epic ID | Epic Name | Description | Business Goal | Priority | Dependencies |
|---|---|---|---|---|---|
| E-IAM | Identity & Authorization | Quản lý danh tính, đăng nhập, phiên làm việc, vai trò, quyền và trạng thái tài khoản | Đảm bảo người dùng hợp lệ truy cập đúng phạm vi | Critical | Không có phụ thuộc nghiệp vụ trước đó |
| E-RCM | Resource Capital Management | Quản lý Time Capital, Money Capital, chu kỳ, phân bổ, điều chỉnh và số dư | Cho phép người dùng biết mình có bao nhiêu nguồn lực và đã phân bổ vào đâu | Critical | E-IAM |
| E-TTM | Task & Timeline Management | Quản lý Task, lập kế hoạch, Timeline, tiến độ và vòng đời Task | Giúp người dùng biến nguồn lực thành kế hoạch thực hiện cụ thể | Critical | E-IAM, E-RCM |
| E-TER | Tracking, Evaluation & Reporting | Ghi nhận Actual, so sánh Planned vs Actual, đánh giá hiệu quả và báo cáo | Giúp người dùng hiểu mức độ hiệu quả khi sử dụng nguồn lực | High | E-IAM, E-RCM, E-TTM |
| E-ADM | Administration & Support | Vận hành hệ thống, hỗ trợ người dùng, quản trị tài khoản, Ticket, Audit và cấu hình nghiệp vụ | Đảm bảo hệ thống được vận hành, hỗ trợ và kiểm soát phù hợp | High | E-IAM |
| E-CBR | Cross-cutting Business Requirements | Chuẩn hóa quy tắc dùng chung, kiểm tra dữ liệu, lịch sử, Audit, Notification, tìm kiếm và xử lý lỗi | Đảm bảo tính nhất quán nghiệp vụ trên toàn hệ thống | Critical | Áp dụng cho mọi Epic |

## 5. Feature Catalog

### 5.1 Identity & Authorization Features

| Feature ID | Feature Name | Description | Business Value | Priority | Dependencies |
|---|---|---|---|---|---|
| IAM-F01 | Guest Access & Registration | Cho phép Guest truy cập trang giới thiệu và tạo tài khoản | Mở đầu hành trình người dùng | Critical | Không có |
| IAM-F02 | Authentication & Session | Đăng nhập, đăng xuất, quản lý phiên và kiểm tra token ở mức nghiệp vụ | Bảo vệ truy cập và duy trì trải nghiệm ổn định | Critical | IAM-F01 |
| IAM-F03 | Profile & Credential | Xem hồ sơ, cập nhật hồ sơ, đổi và khôi phục mật khẩu | Giúp User tự quản lý danh tính cá nhân | High | IAM-F02 |
| IAM-F04 | Account Status & Access Handling | Xác thực trạng thái tài khoản, xử lý truy cập không hợp lệ và bị từ chối | Ngăn truy cập trái phạm vi | Critical | IAM-F02 |
| IAM-F05 | Role & Permission Governance | Quản lý, gán, thu hồi Role và Permission | Đảm bảo phân quyền theo RBAC nghiệp vụ | Critical | IAM-F02 |
| IAM-F06 | User Governance & Audit | Tìm kiếm, xem, cập nhật User, khóa, mở, ngưng, kích hoạt và xem Audit | Hỗ trợ quản trị danh tính | High | IAM-F05 |

### 5.2 Resource Capital Management Features

| Feature ID | Feature Name | Description | Business Value | Priority | Dependencies |
|---|---|---|---|---|---|
| RCM-F01 | Capital Cycle Management | Tạo, cập nhật, kích hoạt, đóng và mở lại chu kỳ nguồn vốn | Cung cấp khung thời gian quản lý nguồn lực | Critical | IAM-F02 |
| RCM-F02 | Capital Setup | Thiết lập Time Capital và Money Capital theo chu kỳ | Xác định nguồn lực khả dụng ban đầu | Critical | RCM-F01 |
| RCM-F03 | Capital Adjustment | Điều chỉnh Time Capital và Money Capital có kiểm soát | Phản ánh thay đổi thực tế của nguồn lực | High | RCM-F02 |
| RCM-F04 | Allocation & Reallocation | Phân bổ, phân bổ lại, giải phóng và cho phép vượt mức | Gắn nguồn lực với kế hoạch sử dụng | Critical | RCM-F02 |
| RCM-F05 | Balance & History | Xem khả dụng, còn lại, lịch sử và tổng hợp nguồn vốn | Tăng khả năng truy vết và ra quyết định | High | RCM-F04 |

### 5.3 Task & Timeline Management Features

| Feature ID | Feature Name | Description | Business Value | Priority | Dependencies |
|---|---|---|---|---|---|
| TTM-F01 | Task Core Management | Tạo, cập nhật, xóa, lưu trữ, khôi phục và nhân bản Task | Quản lý đơn vị công việc trung tâm | Critical | IAM-F02 |
| TTM-F02 | Task Planning Attributes | Category, Tag, Priority, Deadline, Estimated Time, Estimated Cost | Chuẩn hóa thông tin lập kế hoạch | Critical | TTM-F01, RCM-F02 |
| TTM-F03 | Timeline Scheduling | Đưa Task lên Timeline, đổi lịch, kéo thả và xem Timeline | Biến kế hoạch thành lịch thực hiện | Critical | TTM-F02, RCM-F04 |
| TTM-F04 | Task Inquiry | Tìm kiếm, lọc, sắp xếp và xem chi tiết Task | Tăng hiệu quả quản lý danh sách công việc | High | TTM-F01 |
| TTM-F05 | Task Lifecycle Execution | Cập nhật tiến độ, tạm dừng, tiếp tục, hoàn thành, hủy và mở lại | Quản lý trạng thái thực hiện công việc | Critical | TTM-F02 |
| TTM-F06 | Recurring & Reminder | Công việc lặp lại và nhắc việc nếu thuộc phạm vi được xác nhận | Hỗ trợ thói quen và kế hoạch định kỳ | Medium | TTM-F01 |

### 5.4 Tracking, Evaluation & Reporting Features

| Feature ID | Feature Name | Description | Business Value | Priority | Dependencies |
|---|---|---|---|---|---|
| TER-F01 | Actual Recording | Ghi nhận và cập nhật Actual Time, Actual Cost | Cung cấp dữ liệu đánh giá thực tế | Critical | TTM-F05 |
| TER-F02 | Variance & Efficiency | Planned vs Actual, Variance, Efficiency, Completion Rate | Đánh giá chất lượng lập kế hoạch | Critical | TER-F01 |
| TER-F03 | Statistics & Dashboard | Daily, Weekly, Monthly, Yearly Statistics, Utilization, Dashboard | Hiển thị tổng quan hiệu quả cá nhân | High | TER-F02 |
| TER-F04 | Reports & Export | Xem, tìm kiếm, lọc và xuất Report | Hỗ trợ phân tích và lưu trữ kết quả | High | TER-F03 |
| TER-F05 | Trend & KPI Analysis | History, Compare Periods, Trend, Category, Tag, Timeline Statistics, Personal KPI | Hỗ trợ cải thiện liên tục | Medium | TER-F03 |

### 5.5 Administration & Support Features

| Feature ID | Feature Name | Description | Business Value | Priority | Dependencies |
|---|---|---|---|---|---|
| ADM-F01 | User Administration | Xem, tìm kiếm, lọc, cập nhật, khóa, mở, ngưng và kích hoạt User | Hỗ trợ vận hành người dùng | High | IAM-F05 |
| ADM-F02 | Staff Administration | Xem, quản lý, gán và gỡ Staff | Quản trị lực lượng hỗ trợ | High | IAM-F05 |
| ADM-F03 | Ticket Management | Tạo, nhận, gán, cập nhật, chuyển cấp, xử lý, đóng và mở lại Ticket | Đảm bảo hỗ trợ người dùng có quy trình | High | IAM-F02 |
| ADM-F04 | Activity & Audit Monitoring | Xem, tìm kiếm và lọc Activity Log, Audit | Tăng tính kiểm soát và truy vết | High | IAM-F05 |
| ADM-F05 | Configuration & System Dashboard | Xem, cập nhật cấu hình nghiệp vụ, xem thống kê vận hành | Hỗ trợ quản trị vận hành | Medium | IAM-F05 |
| ADM-F06 | Role & Permission Administration | Quản lý Role, Permission, gán và thu hồi quyền | Bảo đảm quản trị truy cập đúng thẩm quyền | Critical | IAM-F05 |

### 5.6 Cross-cutting Features

| Feature ID | Feature Name | Description | Business Value | Priority | Dependencies |
|---|---|---|---|---|---|
| CBR-F01 | Global Rule Compliance | Áp dụng quy tắc toàn cục về quyền sở hữu, trạng thái và truy vết | Giảm sai lệch nghiệp vụ giữa các module | Critical | Tất cả Epic |
| CBR-F02 | Validation Standards | Chuẩn hóa kiểm tra bắt buộc, tùy chọn, có điều kiện và dẫn xuất | Đảm bảo dữ liệu nghiệp vụ hợp lệ | Critical | Tất cả Epic |
| CBR-F03 | Audit & Activity Standards | Chuẩn hóa yêu cầu Audit và Activity History | Đảm bảo minh bạch và kiểm soát | High | Tất cả Epic |
| CBR-F04 | Notification Standards | Chuẩn hóa sự kiện thông báo, đối tượng nhận và mục đích | Hỗ trợ người dùng phản ứng kịp thời | Medium | IAM-F02 |
| CBR-F05 | Search, Filter & Export Standards | Chuẩn hóa tìm kiếm, lọc, sắp xếp, phân trang và xuất dữ liệu | Tăng tính nhất quán trải nghiệm nghiệp vụ | High | Các module có danh sách |
| CBR-F06 | Error Handling Principles | Chuẩn hóa phản hồi cho lỗi xác thực, quyền, quy tắc và bảo trì | Giúp người dùng hiểu và xử lý lỗi đúng cách | High | Tất cả Epic |

## 6. User Story Catalog

### 6.1 Identity & Authorization User Stories

| Story ID | Epic | Feature | User Story | Priority | Business Value | Story Point | Dependencies | Assumptions |
|---|---|---|---|---|---|---:|---|---|
| US-IAM-001 | E-IAM | IAM-F01 | As a Guest, I want to view the Landing Page so that I can understand LifeBalance before registering. | Medium | Tăng nhận biết sản phẩm | 2 | Không có | Nội dung Landing Page do Product Owner phê duyệt |
| US-IAM-002 | E-IAM | IAM-F01 | As a Guest, I want to register an account so that I can use LifeBalance as a User. | Critical | Mở đầu vòng đời người dùng | 5 | US-IAM-001 | Quy tắc dữ liệu đăng ký được xác nhận trong Open Questions |
| US-IAM-003 | E-IAM | IAM-F02 | As a User, I want to login so that I can access my authorized LifeBalance functions. | Critical | Đảm bảo truy cập hợp lệ | 5 | US-IAM-002 | Tài khoản đã tồn tại và hợp lệ |
| US-IAM-004 | E-IAM | IAM-F02 | As an authenticated actor, I want to logout so that my session is ended intentionally. | High | Bảo vệ phiên làm việc | 3 | US-IAM-003 | Áp dụng cho User, Staff, Admin |
| US-IAM-005 | E-IAM | IAM-F02 | As an authenticated actor, I want my session to be managed so that access remains valid only within allowed conditions. | Critical | Giảm rủi ro truy cập không hợp lệ | 8 | US-IAM-003 | Chính sách thời lượng phiên cần xác nhận |
| US-IAM-006 | E-IAM | IAM-F03 | As a User, I want to view my profile so that I can verify my personal information. | High | Tự kiểm soát danh tính cá nhân | 3 | US-IAM-003 | User chỉ xem hồ sơ của mình |
| US-IAM-007 | E-IAM | IAM-F03 | As a User, I want to update my profile so that my personal information remains accurate. | High | Dữ liệu hồ sơ phù hợp thực tế | 5 | US-IAM-006 | Trường được phép sửa cần được xác nhận |
| US-IAM-008 | E-IAM | IAM-F03 | As a User, I want to change my password so that I can maintain account security. | High | Tăng an toàn tài khoản | 5 | US-IAM-003 | Chính sách mật khẩu cần xác nhận |
| US-IAM-009 | E-IAM | IAM-F03 | As a User, I want to recover a forgotten password so that I can regain account access. | High | Giảm mất quyền truy cập hợp lệ | 5 | US-IAM-002 | Kênh xác minh cần xác nhận |
| US-IAM-010 | E-IAM | IAM-F04 | As the business, I want account status validated before access so that locked or inactive accounts cannot proceed. | Critical | Kiểm soát trạng thái tài khoản | 5 | US-IAM-003 | Trạng thái tài khoản dùng chung theo Volume 2 |
| US-IAM-011 | E-IAM | IAM-F04 | As the business, I want permission validation before protected actions so that actors only perform authorized work. | Critical | Bảo vệ phạm vi quyền | 8 | US-IAM-003 | RBAC là mô hình nghiệp vụ được chọn |
| US-IAM-012 | E-IAM | IAM-F05 | As an Admin, I want to manage roles so that access responsibilities are organized by business role. | Critical | Quản trị quyền theo vai trò | 8 | US-IAM-011 | Chỉ Admin thực hiện |
| US-IAM-013 | E-IAM | IAM-F05 | As an Admin, I want to revoke roles so that outdated responsibilities are removed. | Critical | Giảm quyền không còn phù hợp | 5 | US-IAM-012 | Thu hồi Role phải được Audit |
| US-IAM-014 | E-IAM | IAM-F05 | As an Admin, I want to manage permissions so that allowed actions are controlled centrally. | Critical | Kiểm soát quyền hạn | 8 | US-IAM-011 | Chỉ Admin thực hiện |
| US-IAM-015 | E-IAM | IAM-F05 | As an Admin, I want to revoke permissions so that access can be reduced when needed. | Critical | Giảm rủi ro quyền dư thừa | 5 | US-IAM-014 | Thu hồi Permission phải được Audit |
| US-IAM-016 | E-IAM | IAM-F05 | As an Admin, I want to assign a role to an account so that the actor can perform appropriate responsibilities. | Critical | Gắn trách nhiệm với tài khoản | 5 | US-IAM-012 | Không được tạo xung đột quyền nghiêm trọng |
| US-IAM-017 | E-IAM | IAM-F05 | As an Admin, I want to remove a role from an account so that responsibility changes are reflected. | Critical | Quản trị thay đổi vai trò | 5 | US-IAM-013 | Không tự thu hồi quyền quản trị cuối cùng nếu chưa có chính sách |
| US-IAM-018 | E-IAM | IAM-F05 | As an Admin, I want to assign permissions so that access can match operational needs. | High | Linh hoạt quản trị quyền | 5 | US-IAM-014 | Phạm vi Permission cần được xác nhận |
| US-IAM-019 | E-IAM | IAM-F05 | As an Admin, I want to revoke permissions so that accounts no longer retain unnecessary access. | High | Giảm rủi ro vận hành | 5 | US-IAM-015 | Permission bị thu hồi ảnh hưởng phiên hiện tại theo chính sách |
| US-IAM-020 | E-IAM | IAM-F06 | As a Staff or Admin, I want to search users so that I can find accounts for support or administration. | High | Tăng hiệu quả hỗ trợ | 3 | US-IAM-011 | Staff chỉ xem phạm vi được phép |
| US-IAM-021 | E-IAM | IAM-F06 | As a Staff or Admin, I want to view user detail so that I can understand account status and support context. | High | Hỗ trợ đúng ngữ cảnh | 3 | US-IAM-020 | Dữ liệu nhạy cảm được giới hạn theo vai trò |
| US-IAM-022 | E-IAM | IAM-F06 | As an Admin, I want to update user information so that account records remain valid. | High | Quản trị dữ liệu tài khoản | 5 | US-IAM-021 | Staff không được cập nhật nếu chưa được xác nhận |
| US-IAM-023 | E-IAM | IAM-F06 | As a Staff, I want to temporarily lock a user account so that suspected issues can be contained. | High | Giảm rủi ro hỗ trợ và vận hành | 5 | US-IAM-021 | Staff chỉ khóa tạm theo quy trình |
| US-IAM-024 | E-IAM | IAM-F06 | As an Admin, I want to unlock a user account so that legitimate access can be restored. | High | Khôi phục truy cập hợp lệ | 5 | US-IAM-023 | Điều kiện mở khóa cần xác nhận |
| US-IAM-025 | E-IAM | IAM-F06 | As an Admin, I want to deactivate an account so that it can no longer be used when required. | High | Kiểm soát vòng đời tài khoản | 5 | US-IAM-021 | Tác động tới dữ liệu cá nhân cần xác nhận |
| US-IAM-026 | E-IAM | IAM-F06 | As an Admin, I want to reactivate an account so that a valid user can return to service. | High | Phục hồi tài khoản hợp lệ | 5 | US-IAM-025 | Điều kiện kích hoạt lại cần xác nhận |
| US-IAM-027 | E-IAM | IAM-F06 | As an Admin, I want to view identity audit records so that access-related changes are traceable. | High | Tăng kiểm soát quản trị | 5 | US-IAM-011 | Thời gian lưu giữ Audit cần xác nhận |
| US-IAM-028 | E-IAM | IAM-F04 | As the business, I want configuration actions authorized so that only qualified Admins can change system-level settings. | Critical | Bảo vệ thiết lập vận hành | 5 | US-IAM-014 | Chỉ kiểm soát nghiệp vụ quyền cấu hình |

### 6.2 Resource Capital Management User Stories

| Story ID | Epic | Feature | User Story | Priority | Business Value | Story Point | Dependencies | Assumptions |
|---|---|---|---|---|---|---:|---|---|
| US-RCM-001 | E-RCM | RCM-F01 | As a User, I want to create a capital cycle so that I can plan resources by daily, weekly, or monthly period. | Critical | Khởi tạo nền tảng quản lý nguồn lực | 8 | US-IAM-003 | Chu kỳ hỗ trợ Daily, Weekly, Monthly |
| US-RCM-002 | E-RCM | RCM-F01 | As a User, I want to update a capital cycle so that the cycle reflects my planning context. | High | Linh hoạt điều chỉnh kỳ kế hoạch | 5 | US-RCM-001 | Chỉ cập nhật khi trạng thái cho phép |
| US-RCM-003 | E-RCM | RCM-F01 | As a User, I want to activate a capital cycle so that resource planning uses the intended period. | Critical | Xác định chu kỳ đang hoạt động | 5 | US-RCM-001 | Mỗi phạm vi chỉ có một chu kỳ hoạt động |
| US-RCM-004 | E-RCM | RCM-F01 | As a User, I want to close a capital cycle so that the period can be finalized for review. | High | Chốt kỳ nguồn lực | 5 | US-RCM-003 | Điều kiện đóng chu kỳ cần xác nhận |
| US-RCM-005 | E-RCM | RCM-F01 | As a User, I want to reopen a closed cycle when allowed so that valid corrections can be made. | Medium | Hỗ trợ điều chỉnh sau kỳ | 5 | US-RCM-004 | Reopen là phạm vi cần xác nhận |
| US-RCM-006 | E-RCM | RCM-F02 | As a User, I want to set Time Capital so that I know how much time is available for planning. | Critical | Xác định nguồn lực thời gian | 5 | US-RCM-001 | Giá trị không âm |
| US-RCM-007 | E-RCM | RCM-F02 | As a User, I want to set Money Capital so that I know how much money is available for planning. | Critical | Xác định nguồn lực tiền bạc | 5 | US-RCM-001 | Giá trị không âm |
| US-RCM-008 | E-RCM | RCM-F03 | As a User, I want to adjust Time Capital so that changes in available time are reflected. | High | Giữ kế hoạch sát thực tế | 5 | US-RCM-006 | Điều chỉnh phải có lịch sử |
| US-RCM-009 | E-RCM | RCM-F03 | As a User, I want to adjust Money Capital so that changes in available money are reflected. | High | Giữ kế hoạch tài chính cá nhân sát thực tế | 5 | US-RCM-007 | Điều chỉnh phải có lịch sử |
| US-RCM-010 | E-RCM | RCM-F04 | As a User, I want to allocate capital so that tasks receive planned time and money before execution. | Critical | Liên kết nguồn lực với công việc | 8 | US-RCM-006, US-RCM-007 | Task liên quan thuộc phạm vi hợp lệ |
| US-RCM-011 | E-RCM | RCM-F04 | As a User, I want to reallocate capital so that resource plans can adapt to changed priorities. | High | Tăng khả năng thích nghi kế hoạch | 8 | US-RCM-010 | Không làm mất lịch sử phân bổ trước đó |
| US-RCM-012 | E-RCM | RCM-F04 | As a User, I want to release allocated capital so that unused resources return to the available balance. | High | Tối ưu nguồn lực còn lại | 5 | US-RCM-010 | Chỉ release khi trạng thái cho phép |
| US-RCM-013 | E-RCM | RCM-F04 | As a User, I want to allow over allocation deliberately so that exceptional plans can be recorded transparently. | Medium | Hỗ trợ ngoại lệ có kiểm soát | 5 | US-RCM-010 | Phải có cảnh báo nghiệp vụ |
| US-RCM-014 | E-RCM | RCM-F05 | As a User, I want to view available capital so that I understand resources before allocation. | Critical | Ra quyết định phân bổ đúng | 3 | US-RCM-006, US-RCM-007 | Chỉ xem dữ liệu của mình |
| US-RCM-015 | E-RCM | RCM-F05 | As a User, I want to view remaining capital so that I know what is still usable. | Critical | Kiểm soát số dư nguồn lực | 3 | US-RCM-010 | Số còn lại dựa trên phân bổ hợp lệ |
| US-RCM-016 | E-RCM | RCM-F05 | As a User, I want to view allocation history so that resource decisions are traceable. | High | Minh bạch sử dụng nguồn lực | 5 | US-RCM-010 | Lịch sử không bị ghi đè |
| US-RCM-017 | E-RCM | RCM-F05 | As a User, I want to view adjustment history so that changes to capital are explainable. | High | Truy vết điều chỉnh | 5 | US-RCM-008, US-RCM-009 | Lý do điều chỉnh có thể cần xác nhận |
| US-RCM-018 | E-RCM | RCM-F05 | As a User, I want to search capital cycles so that I can find the relevant period quickly. | Medium | Tăng hiệu quả tra cứu | 3 | US-RCM-001 | Tiêu chí tìm kiếm cần xác nhận |
| US-RCM-019 | E-RCM | RCM-F05 | As a User, I want to filter capital history so that I can review specific changes. | Medium | Hỗ trợ kiểm tra lịch sử | 3 | US-RCM-016 | Bộ lọc chuẩn theo Volume 7 |
| US-RCM-020 | E-RCM | RCM-F05 | As a User, I want to view a capital summary so that I can understand overall resource position. | High | Tổng quan nguồn lực cá nhân | 5 | US-RCM-014, US-RCM-015 | Summary ở mức nghiệp vụ |

### 6.3 Task & Timeline Management User Stories

| Story ID | Epic | Feature | User Story | Priority | Business Value | Story Point | Dependencies | Assumptions |
|---|---|---|---|---|---|---:|---|---|
| US-TTM-001 | E-TTM | TTM-F01 | As a User, I want to create a task so that I can capture work that needs to be planned. | Critical | Thiết lập đơn vị công việc trung tâm | 8 | US-IAM-003 | Task thuộc đúng một User |
| US-TTM-002 | E-TTM | TTM-F01 | As a User, I want to update a task so that the work description remains accurate. | Critical | Duy trì kế hoạch đúng thực tế | 5 | US-TTM-001 | Không vi phạm trạng thái Task |
| US-TTM-003 | E-TTM | TTM-F01 | As a User, I want to delete a task when allowed so that invalid work items are removed. | Medium | Giảm nhiễu danh sách công việc | 5 | US-TTM-001 | Điều kiện xóa cần xác nhận |
| US-TTM-004 | E-TTM | TTM-F01 | As a User, I want to archive a task so that completed or inactive work no longer clutters active planning. | Medium | Tổ chức Task hiệu quả | 5 | US-TTM-001 | Archive không đồng nghĩa mất lịch sử |
| US-TTM-005 | E-TTM | TTM-F01 | As a User, I want to restore an archived task so that relevant work can return to active planning. | Medium | Linh hoạt khôi phục Task | 5 | US-TTM-004 | Chỉ restore khi trạng thái hợp lệ |
| US-TTM-006 | E-TTM | TTM-F01 | As a User, I want to duplicate a task so that similar work can be planned faster. | Medium | Tăng hiệu quả thao tác | 3 | US-TTM-001 | Thuộc tính được sao chép cần xác nhận |
| US-TTM-007 | E-TTM | TTM-F02 | As a User, I want to assign a category so that tasks can be organized by area of life. | High | Phân loại kế hoạch | 3 | US-TTM-001 | Category có thể tùy biến theo phạm vi |
| US-TTM-008 | E-TTM | TTM-F02 | As a User, I want to assign tags so that tasks can be grouped flexibly. | High | Tăng khả năng tìm kiếm và phân tích | 3 | US-TTM-001 | Một Task có nhiều Tag |
| US-TTM-009 | E-TTM | TTM-F02 | As a User, I want to remove a tag so that task classification remains relevant. | Medium | Giảm phân loại sai | 2 | US-TTM-008 | Không ảnh hưởng lịch sử đánh giá |
| US-TTM-010 | E-TTM | TTM-F02 | As a User, I want to set and update task priority so that important work is visible. | High | Hỗ trợ ra quyết định ưu tiên | 3 | US-TTM-001 | Bộ giá trị Priority cần xác nhận |
| US-TTM-011 | E-TTM | TTM-F02 | As a User, I want to set and change a deadline so that time-bound work is controlled. | High | Kiểm soát hạn hoàn thành | 5 | US-TTM-001 | Deadline phải hợp lệ theo quy tắc |
| US-TTM-012 | E-TTM | TTM-F02 | As a User, I want to estimate time so that the task can be planned against Time Capital. | Critical | Liên kết Task với nguồn lực thời gian | 5 | US-RCM-006, US-TTM-001 | Task có Time Capital mới lên Timeline |
| US-TTM-013 | E-TTM | TTM-F02 | As a User, I want to estimate cost so that the task can be planned against Money Capital. | High | Liên kết Task với nguồn lực tiền bạc | 5 | US-RCM-007, US-TTM-001 | Cost có thể bằng 0 nếu không dùng tiền |
| US-TTM-014 | E-TTM | TTM-F03 | As a User, I want to schedule a task on the timeline so that I know when to execute it. | Critical | Chuyển kế hoạch thành lịch | 8 | US-TTM-012 | Timeline chỉ nhận Task có Time Capital |
| US-TTM-015 | E-TTM | TTM-F03 | As a User, I want to reschedule a task so that my timeline adapts to changes. | High | Linh hoạt kế hoạch | 5 | US-TTM-014 | Không vi phạm chu kỳ nguồn vốn |
| US-TTM-016 | E-TTM | TTM-F03 | As a User, I want to move a task by drag and drop so that timeline adjustments are efficient. | High | Tăng trải nghiệm lập lịch | 8 | US-TTM-014 | Chồng lấn lịch xử lý theo chính sách |
| US-TTM-017 | E-TTM | TTM-F03 | As a User, I want to view my timeline so that I can understand scheduled work. | Critical | Nhìn rõ lịch thực hiện | 5 | US-TTM-014 | Chỉ hiển thị Task có Time Capital |
| US-TTM-018 | E-TTM | TTM-F04 | As a User, I want to search tasks so that I can locate work quickly. | High | Tăng hiệu quả quản lý Task | 3 | US-TTM-001 | Tìm kiếm theo tiêu chí nghiệp vụ chuẩn |
| US-TTM-019 | E-TTM | TTM-F04 | As a User, I want to filter tasks so that I can focus on relevant work. | High | Giảm tải thông tin | 3 | US-TTM-001 | Bộ lọc gồm trạng thái, thời gian, phân loại nếu xác nhận |
| US-TTM-020 | E-TTM | TTM-F04 | As a User, I want to sort and view task details so that I can review work in the order I need. | High | Hỗ trợ ra quyết định | 3 | US-TTM-001 | Tiêu chí sắp xếp cần xác nhận |
| US-TTM-021 | E-TTM | TTM-F05 | As a User, I want to update task progress so that execution status is visible. | Critical | Theo dõi thực hiện | 5 | US-TTM-001 | Progress từ 0 đến 100 |
| US-TTM-022 | E-TTM | TTM-F05 | As a User, I want to pause a task so that interrupted work is represented accurately. | Medium | Phản ánh trạng thái thực tế | 3 | US-TTM-021 | Chỉ pause khi trạng thái cho phép |
| US-TTM-023 | E-TTM | TTM-F05 | As a User, I want to resume a paused task so that work can continue. | Medium | Khôi phục tiến độ | 3 | US-TTM-022 | Trạng thái trước đó phải hợp lệ |
| US-TTM-024 | E-TTM | TTM-F05 | As a User, I want to complete a task so that it becomes eligible for evaluation. | Critical | Kết nối với đánh giá hiệu quả | 5 | US-TTM-021 | Completed Task là đầu vào cho Tracking |
| US-TTM-025 | E-TTM | TTM-F05 | As a User, I want to cancel a task so that work no longer intended is closed properly. | Medium | Kiểm soát công việc bị hủy | 5 | US-TTM-001 | Tác động nguồn lực cần tuân thủ chính sách |
| US-TTM-026 | E-TTM | TTM-F05 | As a User, I want to reopen a task when allowed so that valid corrections can be made. | Medium | Hỗ trợ sửa sai có kiểm soát | 5 | US-TTM-024 | Reopen cần chính sách rõ |
| US-TTM-027 | E-TTM | TTM-F06 | As a User, I want to create recurring tasks so that repeated work can be planned efficiently. | Medium | Hỗ trợ thói quen định kỳ | 8 | US-TTM-001 | Recurring Task là phạm vi cần xác nhận |
| US-TTM-028 | E-TTM | TTM-F06 | As a User, I want task reminders so that I can act before planned time or deadline. | Medium | Giảm bỏ sót công việc | 5 | US-TTM-011, US-TTM-014 | Notification policy cần xác nhận |

### 6.4 Tracking, Evaluation & Reporting User Stories

| Story ID | Epic | Feature | User Story | Priority | Business Value | Story Point | Dependencies | Assumptions |
|---|---|---|---|---|---|---:|---|---|
| US-TER-001 | E-TER | TER-F01 | As a User, I want to record actual time so that completed work reflects real time consumption. | Critical | Cơ sở đánh giá hiệu quả thời gian | 5 | US-TTM-024 | Chỉ Task đủ điều kiện được ghi Actual cuối cùng |
| US-TER-002 | E-TER | TER-F01 | As a User, I want to update actual time so that mistakes can be corrected when allowed. | High | Tăng độ chính xác dữ liệu đánh giá | 5 | US-TER-001 | Điều kiện sửa Actual cần xác nhận |
| US-TER-003 | E-TER | TER-F01 | As a User, I want to record actual cost so that completed work reflects real money consumption. | Critical | Cơ sở đánh giá hiệu quả chi phí | 5 | US-TTM-024 | Actual Cost không âm |
| US-TER-004 | E-TER | TER-F01 | As a User, I want to update actual cost so that cost records remain accurate. | High | Tăng độ tin cậy đánh giá | 5 | US-TER-003 | Điều kiện sửa Actual cần xác nhận |
| US-TER-005 | E-TER | TER-F02 | As a User, I want to view planned versus actual values so that I can understand deviations. | Critical | Minh bạch kế hoạch và thực tế | 5 | US-TER-001, US-TER-003 | Planned được kế thừa từ Task và Resource |
| US-TER-006 | E-TER | TER-F02 | As a User, I want time variance calculated so that time planning accuracy can be evaluated. | Critical | Đánh giá lệch thời gian | 5 | US-TER-005 | Variance = Actual - Planned ở mức nghiệp vụ |
| US-TER-007 | E-TER | TER-F02 | As a User, I want cost variance calculated so that cost planning accuracy can be evaluated. | Critical | Đánh giá lệch chi phí | 5 | US-TER-005 | Variance = Actual - Planned ở mức nghiệp vụ |
| US-TER-008 | E-TER | TER-F02 | As a User, I want resource efficiency calculated so that I can assess whether resource use was effective. | Critical | Đánh giá hiệu quả nguồn lực | 8 | US-TER-006, US-TER-007 | Thang đánh giá cần xác nhận |
| US-TER-009 | E-TER | TER-F02 | As a User, I want completion rate calculated so that I understand execution reliability. | High | Đo khả năng hoàn thành | 5 | US-TTM-024 | Khoảng thời gian thống kê hợp lệ |
| US-TER-010 | E-TER | TER-F03 | As a User, I want a productivity summary so that I can see overall performance. | High | Tổng quan hiệu suất cá nhân | 5 | US-TER-008 | Summary không thay thế báo cáo chi tiết |
| US-TER-011 | E-TER | TER-F03 | As a User, I want daily statistics so that I can review short-term performance. | High | Phản hồi nhanh theo ngày | 3 | US-TER-005 | Ngày thống kê hợp lệ |
| US-TER-012 | E-TER | TER-F03 | As a User, I want weekly statistics so that I can review weekly planning accuracy. | High | Đánh giá chu kỳ tuần | 3 | US-TER-005 | Tuần thống kê hợp lệ |
| US-TER-013 | E-TER | TER-F03 | As a User, I want monthly statistics so that I can assess monthly resource use. | High | Đánh giá chu kỳ tháng | 3 | US-TER-005 | Tháng thống kê hợp lệ |
| US-TER-014 | E-TER | TER-F03 | As a User, I want yearly statistics so that long-term patterns can be reviewed. | Medium | Phân tích dài hạn | 3 | US-TER-005 | Năm thống kê hợp lệ |
| US-TER-015 | E-TER | TER-F03 | As a User, I want to view resource utilization so that I know how much planned capital was used. | High | Kiểm soát mức sử dụng nguồn lực | 5 | US-RCM-020, US-TER-005 | Utilization ở mức nghiệp vụ |
| US-TER-016 | E-TER | TER-F03 | As a User, I want to view a dashboard so that key performance information is visible in one place. | High | Ra quyết định nhanh | 8 | US-TER-010 | Dashboard không thiết kế giao diện tại tài liệu này |
| US-TER-017 | E-TER | TER-F04 | As a User, I want to view reports so that I can analyze results by period and dimension. | High | Hỗ trợ phân tích có cấu trúc | 8 | US-TER-010 | Loại Report theo Volume 5 |
| US-TER-018 | E-TER | TER-F04 | As a User, I want to export reports so that I can keep or share business-level summaries. | Medium | Lưu giữ và trao đổi kết quả | 5 | US-TER-017 | Định dạng xuất cần xác nhận |
| US-TER-019 | E-TER | TER-F04 | As a User, I want to search reports so that I can find relevant reports quickly. | Medium | Tăng hiệu quả tra cứu | 3 | US-TER-017 | Theo chuẩn Search & Filter |
| US-TER-020 | E-TER | TER-F04 | As a User, I want to filter reports so that I can focus on specific periods or dimensions. | Medium | Giảm tải thông tin | 3 | US-TER-017 | Bộ lọc cần thống nhất |
| US-TER-021 | E-TER | TER-F05 | As a User, I want to view evaluation history so that past performance remains traceable. | High | Học từ lịch sử | 5 | US-TER-005 | Lịch sử không bị ghi đè |
| US-TER-022 | E-TER | TER-F05 | As a User, I want to compare periods so that I can understand improvement or decline. | Medium | Hỗ trợ cải thiện liên tục | 5 | US-TER-011, US-TER-012, US-TER-013 | Kỳ so sánh phải hợp lệ |
| US-TER-023 | E-TER | TER-F05 | As a User, I want to view trends so that I can identify patterns in resource use. | Medium | Phát hiện xu hướng | 5 | US-TER-022 | Cách diễn giải xu hướng cần xác nhận |
| US-TER-024 | E-TER | TER-F05 | As a User, I want category, tag, and timeline statistics so that I can analyze productivity by context. | Medium | Phân tích sâu theo phân loại | 8 | US-TTM-007, US-TTM-008, US-TTM-017 | Dữ liệu phân loại có thể thiếu |
| US-TER-025 | E-TER | TER-F05 | As a User, I want to view personal KPIs so that I can measure progress against productivity goals. | Medium | Quản lý mục tiêu cá nhân | 8 | US-TER-008, US-TER-009 | Ngưỡng KPI cần xác nhận |

### 6.5 Administration & Support User Stories

| Story ID | Epic | Feature | User Story | Priority | Business Value | Story Point | Dependencies | Assumptions |
|---|---|---|---|---|---|---:|---|---|
| US-ADM-001 | E-ADM | ADM-F01 | As a Staff or Admin, I want to view users so that I can support or administer accounts. | High | Nền tảng quản trị người dùng | 3 | US-IAM-011 | Phạm vi xem theo vai trò |
| US-ADM-002 | E-ADM | ADM-F01 | As a Staff or Admin, I want to search users so that account lookup is efficient. | High | Tăng tốc hỗ trợ | 3 | US-ADM-001 | Tiêu chí tìm kiếm cần xác nhận |
| US-ADM-003 | E-ADM | ADM-F01 | As a Staff or Admin, I want to filter users so that I can focus on relevant account groups. | Medium | Quản lý danh sách tốt hơn | 3 | US-ADM-001 | Bộ lọc theo trạng thái, vai trò nếu xác nhận |
| US-ADM-004 | E-ADM | ADM-F01 | As an Admin, I want to update user information so that account records remain correct. | High | Duy trì dữ liệu tài khoản hợp lệ | 5 | US-ADM-001 | Staff không có quyền nếu chưa xác nhận |
| US-ADM-005 | E-ADM | ADM-F01 | As an Admin, I want to deactivate users so that inappropriate or inactive access can be stopped. | High | Kiểm soát truy cập vận hành | 5 | US-ADM-001 | Tác động trạng thái cần rõ |
| US-ADM-006 | E-ADM | ADM-F01 | As an Admin, I want to reactivate users so that legitimate access can be restored. | High | Khôi phục sử dụng hợp lệ | 5 | US-ADM-005 | Điều kiện reactivate cần xác nhận |
| US-ADM-007 | E-ADM | ADM-F01 | As a Staff or Admin, I want to temporarily lock an account so that risk can be contained. | High | Xử lý rủi ro tức thời | 5 | US-ADM-001 | Staff khóa theo quy trình |
| US-ADM-008 | E-ADM | ADM-F01 | As an Admin, I want to unlock an account so that the user can access again when eligible. | High | Khôi phục quyền truy cập | 5 | US-ADM-007 | Điều kiện unlock cần xác nhận |
| US-ADM-009 | E-ADM | ADM-F02 | As an Admin, I want to view staff so that support capacity can be managed. | High | Quản lý đội hỗ trợ | 3 | US-IAM-011 | Chỉ Admin |
| US-ADM-010 | E-ADM | ADM-F02 | As an Admin, I want to manage staff assignment or removal so that responsibilities stay current. | High | Điều phối vận hành | 8 | US-ADM-009 | Phạm vi Staff cần xác nhận |
| US-ADM-011 | E-ADM | ADM-F03 | As a User or Staff, I want to create a support ticket so that a support need is recorded. | High | Ghi nhận yêu cầu hỗ trợ | 5 | US-IAM-003 | Actor tạo Ticket cần xác nhận |
| US-ADM-012 | E-ADM | ADM-F03 | As a Staff, I want to receive tickets so that support work can begin. | High | Bắt đầu quy trình hỗ trợ | 5 | US-ADM-011 | Quy tắc phân phối Ticket cần xác nhận |
| US-ADM-013 | E-ADM | ADM-F03 | As a Staff or Admin, I want to assign tickets so that ownership is clear. | High | Rõ trách nhiệm xử lý | 5 | US-ADM-012 | Staff tự nhận hay được gán cần xác nhận |
| US-ADM-014 | E-ADM | ADM-F03 | As a Staff, I want to update ticket information so that support progress is visible. | High | Minh bạch xử lý Ticket | 5 | US-ADM-013 | Trường cập nhật theo trạng thái |
| US-ADM-015 | E-ADM | ADM-F03 | As a Staff, I want to escalate tickets so that complex issues reach proper authority. | Medium | Giảm tồn đọng vấn đề khó | 5 | US-ADM-014 | Điều kiện escalated cần xác nhận |
| US-ADM-016 | E-ADM | ADM-F03 | As a Staff, I want to resolve tickets so that user issues are addressed. | High | Hoàn tất xử lý hỗ trợ | 5 | US-ADM-014 | Cần ghi kết quả xử lý |
| US-ADM-017 | E-ADM | ADM-F03 | As a Staff, I want to close tickets so that resolved support items are finalized. | High | Kết thúc quy trình hỗ trợ | 3 | US-ADM-016 | Close theo chính sách Ticket |
| US-ADM-018 | E-ADM | ADM-F03 | As a Staff or Admin, I want to reopen tickets when valid so that premature closure can be corrected. | Medium | Sửa lỗi quy trình hỗ trợ | 5 | US-ADM-017 | Điều kiện reopen cần xác nhận |
| US-ADM-019 | E-ADM | ADM-F03 | As a Staff or Admin, I want to search tickets so that support records can be found quickly. | Medium | Tăng năng suất hỗ trợ | 3 | US-ADM-011 | Theo chuẩn tìm kiếm |
| US-ADM-020 | E-ADM | ADM-F03 | As a Staff or Admin, I want to filter tickets so that I can manage workload by status, priority, or category. | Medium | Ưu tiên xử lý hỗ trợ | 3 | US-ADM-011 | Giá trị Status, Priority, Category cần xác nhận |
| US-ADM-021 | E-ADM | ADM-F04 | As a Staff or Admin, I want to view activity logs so that operational actions can be reviewed. | High | Quan sát vận hành | 5 | US-IAM-011 | Staff xem phạm vi hạn chế |
| US-ADM-022 | E-ADM | ADM-F04 | As a Staff or Admin, I want to search and filter activity logs so that relevant events can be investigated. | High | Hỗ trợ truy vết | 5 | US-ADM-021 | Bộ lọc theo thời gian, actor, hành động nếu xác nhận |
| US-ADM-023 | E-ADM | ADM-F04 | As an Admin, I want to view audit records so that governance-critical changes are traceable. | High | Kiểm soát quản trị | 5 | US-IAM-027 | Chỉ Admin |
| US-ADM-024 | E-ADM | ADM-F04 | As an Admin, I want to search audit records so that specific governance events can be reviewed. | High | Điều tra thay đổi quan trọng | 5 | US-ADM-023 | Thời gian lưu cần xác nhận |
| US-ADM-025 | E-ADM | ADM-F05 | As an Admin, I want to view configuration settings so that operational policy can be understood. | Medium | Minh bạch thiết lập vận hành | 3 | US-IAM-028 | Chỉ xem cấu hình nghiệp vụ |
| US-ADM-026 | E-ADM | ADM-F05 | As an Admin, I want to update configuration settings so that operation follows approved policy. | Medium | Điều chỉnh vận hành có kiểm soát | 8 | US-ADM-025 | Mọi thay đổi phải được Audit |
| US-ADM-027 | E-ADM | ADM-F05 | As an Admin, I want to view a system dashboard so that operational status is visible. | Medium | Theo dõi vận hành tổng quan | 5 | US-IAM-011 | Dashboard quản trị, không phải Dashboard User |
| US-ADM-028 | E-ADM | ADM-F05 | As an Admin, I want to view system statistics so that operation can be assessed by business indicators. | Medium | Đo lường vận hành | 5 | US-ADM-027 | Chỉ ở mức thống kê nghiệp vụ |
| US-ADM-029 | E-ADM | ADM-F06 | As an Admin, I want to manage roles so that staff and user responsibilities remain governed. | Critical | Quản trị truy cập | 8 | US-IAM-012 | Không trùng vai trò không hợp lệ |
| US-ADM-030 | E-ADM | ADM-F06 | As an Admin, I want to manage permissions so that allowed administrative actions are controlled. | Critical | Kiểm soát quyền quản trị | 8 | US-IAM-014 | Chỉ Admin |
| US-ADM-031 | E-ADM | ADM-F06 | As an Admin, I want to assign and revoke roles so that accounts reflect current responsibilities. | Critical | Phân công trách nhiệm đúng | 8 | US-ADM-029 | Thay đổi phải được Audit |
| US-ADM-032 | E-ADM | ADM-F06 | As an Admin, I want to assign and revoke permissions so that access remains appropriate. | Critical | Giảm quyền dư thừa | 8 | US-ADM-030 | Thay đổi phải được Audit |
| US-ADM-033 | E-ADM | ADM-F05 | As an Admin, I want to broadcast announcements when allowed so that users receive operational notices. | Low | Truyền thông vận hành | 5 | US-ADM-026 | Announcement là phạm vi cần xác nhận |

### 6.6 Cross-cutting User Stories

| Story ID | Epic | Feature | User Story | Priority | Business Value | Story Point | Dependencies | Assumptions |
|---|---|---|---|---|---|---:|---|---|
| US-CBR-001 | E-CBR | CBR-F01 | As the business, I want global business rules applied consistently so that all modules behave coherently. | Critical | Tính nhất quán toàn hệ thống | 8 | Tất cả Epic | Quy tắc toàn cục kế thừa Volume 7 |
| US-CBR-002 | E-CBR | CBR-F02 | As the business, I want validation standards applied consistently so that invalid business data is prevented. | Critical | Chất lượng dữ liệu nghiệp vụ | 8 | Tất cả Epic | Validation phân loại Mandatory, Optional, Conditional, Derived |
| US-CBR-003 | E-CBR | CBR-F03 | As the business, I want important actions audited so that accountability is maintained. | Critical | Truy vết trách nhiệm | 8 | Tất cả Epic | Danh sách Audit theo Volume 7 |
| US-CBR-004 | E-CBR | CBR-F03 | As a User, I want activity history to show relevant personal actions so that I can review my own changes. | High | Minh bạch cá nhân | 5 | IAM, RCM, TTM, TER | Mức hiển thị lịch sử cần xác nhận |
| US-CBR-005 | E-CBR | CBR-F04 | As an actor, I want notifications for important events so that I can respond in time. | Medium | Nhắc nhở và cảnh báo nghiệp vụ | 5 | IAM, TTM, ADM | Notification là phạm vi cần xác nhận |
| US-CBR-006 | E-CBR | CBR-F05 | As an actor, I want search, filter, sorting, and paging standards so that lists behave consistently. | High | Trải nghiệm nghiệp vụ nhất quán | 5 | Các danh sách nghiệp vụ | Chuẩn áp dụng theo từng phạm vi quyền |
| US-CBR-007 | E-CBR | CBR-F05 | As an authorized actor, I want export standards so that exported information is controlled by permission and purpose. | Medium | Kiểm soát chia sẻ dữ liệu | 5 | Reporting, Admin | Định dạng xuất cần xác nhận |
| US-CBR-008 | E-CBR | CBR-F06 | As an actor, I want clear business error handling so that I understand why an action cannot proceed. | High | Giảm nhầm lẫn và hỗ trợ | 5 | Tất cả Epic | Không mô tả kỹ thuật xử lý lỗi |

## 7. Acceptance Criteria

Acceptance Criteria được trình bày theo dạng Given / When / Then. Mỗi dòng tương ứng một User Story và có thể được mở rộng thành nhiều kịch bản kiểm thử nghiệp vụ chi tiết hơn trong các tài liệu kiểm thử.

### 7.1 Identity & Authorization Acceptance Criteria

| Story ID | Given | When | Then |
|---|---|---|---|
| US-IAM-001 | Guest chưa đăng nhập | Guest truy cập LifeBalance | Guest xem được Landing Page và các hành động Register/Login |
| US-IAM-002 | Guest cung cấp thông tin đăng ký hợp lệ | Guest gửi yêu cầu đăng ký | Tài khoản User được tạo theo quy tắc nghiệp vụ |
| US-IAM-003 | User có tài khoản hoạt động và thông tin đăng nhập hợp lệ | User đăng nhập | User truy cập được các chức năng thuộc quyền |
| US-IAM-004 | Actor đang có phiên hợp lệ | Actor đăng xuất | Phiên làm việc kết thúc và actor không còn truy cập chức năng bảo vệ |
| US-IAM-005 | Actor đang sử dụng phiên làm việc | Phiên hết hạn hoặc không còn hợp lệ | Hệ thống yêu cầu xác thực lại hoặc ngăn thao tác tiếp theo |
| US-IAM-006 | User đã đăng nhập | User mở hồ sơ | Hồ sơ cá nhân của User được hiển thị đúng phạm vi |
| US-IAM-007 | User đã đăng nhập và nhập thông tin hợp lệ | User cập nhật hồ sơ | Hồ sơ được cập nhật và ghi nhận thay đổi phù hợp |
| US-IAM-008 | User đã đăng nhập và đáp ứng chính sách mật khẩu | User đổi mật khẩu | Mật khẩu được thay đổi và sự kiện được ghi nhận |
| US-IAM-009 | User không nhớ mật khẩu | User thực hiện quy trình khôi phục | User có thể đặt lại quyền truy cập sau khi xác minh hợp lệ |
| US-IAM-010 | Tài khoản bị khóa, ngưng hoặc không hợp lệ | Actor cố đăng nhập | Quyền truy cập bị từ chối theo trạng thái tài khoản |
| US-IAM-011 | Actor yêu cầu thực hiện chức năng bảo vệ | Hệ thống kiểm tra quyền | Chỉ actor có Permission phù hợp được tiếp tục |
| US-IAM-012 | Admin có quyền quản lý Role | Admin tạo hoặc cập nhật Role | Role được quản lý theo quy tắc và có Audit |
| US-IAM-013 | Role không còn phù hợp | Admin thu hồi Role | Role được thu hồi và tác động quyền được áp dụng theo chính sách |
| US-IAM-014 | Admin có quyền quản lý Permission | Admin tạo hoặc cập nhật Permission | Permission được quản lý theo phạm vi nghiệp vụ hợp lệ |
| US-IAM-015 | Permission không còn phù hợp | Admin thu hồi Permission | Permission được thu hồi và sự kiện được Audit |
| US-IAM-016 | Tài khoản đủ điều kiện nhận Role | Admin gán Role | Tài khoản nhận Role và có quyền tương ứng theo chính sách |
| US-IAM-017 | Tài khoản đang có Role | Admin gỡ Role | Role bị gỡ và quyền liên quan được điều chỉnh |
| US-IAM-018 | Permission hợp lệ | Admin gán Permission | Permission được gán đúng đối tượng và phạm vi |
| US-IAM-019 | Tài khoản có Permission cần thu hồi | Admin thu hồi Permission | Permission không còn hiệu lực theo chính sách |
| US-IAM-020 | Staff hoặc Admin có quyền tra cứu | Actor tìm User | Danh sách kết quả phù hợp phạm vi quyền được hiển thị |
| US-IAM-021 | User được tìm thấy | Actor mở chi tiết | Chi tiết User được hiển thị theo quyền của actor |
| US-IAM-022 | Admin cập nhật thông tin hợp lệ | Admin lưu thay đổi User | Thông tin User được cập nhật và ghi nhận |
| US-IAM-023 | Staff có lý do khóa hợp lệ | Staff khóa tạm User | Tài khoản bị khóa tạm và không thể đăng nhập |
| US-IAM-024 | Tài khoản đang bị khóa | Admin mở khóa | Tài khoản có thể đăng nhập lại nếu đủ điều kiện |
| US-IAM-025 | Tài khoản cần ngưng hoạt động | Admin deactivate | Tài khoản không còn được sử dụng |
| US-IAM-026 | Tài khoản đủ điều kiện khôi phục | Admin reactivate | Tài khoản được kích hoạt lại |
| US-IAM-027 | Admin có quyền xem Audit | Admin mở Audit danh tính | Các sự kiện thay đổi quyền và tài khoản được hiển thị |
| US-IAM-028 | Actor yêu cầu thay đổi cấu hình | Hệ thống kiểm tra quyền cấu hình | Chỉ Admin đủ quyền được tiếp tục |

### 7.2 Resource Capital Acceptance Criteria

| Story ID | Given | When | Then |
|---|---|---|---|
| US-RCM-001 | User đã đăng nhập | User tạo chu kỳ nguồn vốn hợp lệ | Chu kỳ được tạo với loại Daily, Weekly hoặc Monthly |
| US-RCM-002 | Chu kỳ ở trạng thái cho phép sửa | User cập nhật chu kỳ | Thông tin chu kỳ được thay đổi và lịch sử được giữ |
| US-RCM-003 | User có chu kỳ hợp lệ | User kích hoạt chu kỳ | Chu kỳ trở thành chu kỳ hoạt động theo quy tắc |
| US-RCM-004 | Chu kỳ đủ điều kiện đóng | User đóng chu kỳ | Chu kỳ được chốt để phục vụ tổng kết |
| US-RCM-005 | Chu kỳ đã đóng và chính sách cho phép | User mở lại chu kỳ | Chu kỳ được mở lại để chỉnh sửa hợp lệ |
| US-RCM-006 | Chu kỳ hợp lệ | User nhập Time Capital không âm | Time Capital được thiết lập |
| US-RCM-007 | Chu kỳ hợp lệ | User nhập Money Capital không âm | Money Capital được thiết lập |
| US-RCM-008 | Time Capital tồn tại | User điều chỉnh Time Capital hợp lệ | Time Capital thay đổi và có lịch sử điều chỉnh |
| US-RCM-009 | Money Capital tồn tại | User điều chỉnh Money Capital hợp lệ | Money Capital thay đổi và có lịch sử điều chỉnh |
| US-RCM-010 | User có nguồn lực khả dụng | User phân bổ nguồn lực | Nguồn lực được cấp cho đối tượng kế hoạch hợp lệ |
| US-RCM-011 | Phân bổ hiện hữu được phép thay đổi | User phân bổ lại | Phân bổ mới có hiệu lực và lịch sử cũ được giữ |
| US-RCM-012 | Nguồn lực đã phân bổ chưa dùng hết | User giải phóng | Nguồn lực quay về số dư khả dụng theo chính sách |
| US-RCM-013 | Phân bổ vượt mức phát sinh | User chủ động cho phép vượt mức | Hệ thống ghi nhận ngoại lệ và hiển thị cảnh báo nghiệp vụ |
| US-RCM-014 | User có chu kỳ nguồn vốn | User xem khả dụng | Available Capital được hiển thị đúng phạm vi |
| US-RCM-015 | User có phân bổ nguồn lực | User xem còn lại | Remaining Capital được hiển thị theo trạng thái hiện tại |
| US-RCM-016 | Có sự kiện phân bổ | User xem lịch sử | Lịch sử phân bổ được hiển thị đầy đủ theo phạm vi |
| US-RCM-017 | Có sự kiện điều chỉnh | User xem lịch sử | Lịch sử điều chỉnh được hiển thị đầy đủ theo phạm vi |
| US-RCM-018 | Có nhiều chu kỳ | User tìm kiếm chu kỳ | Kết quả phù hợp tiêu chí được hiển thị |
| US-RCM-019 | Có nhiều sự kiện lịch sử | User lọc lịch sử | Danh sách lịch sử được lọc theo tiêu chí hợp lệ |
| US-RCM-020 | User có dữ liệu nguồn vốn | User xem Summary | Summary phản ánh vốn, phân bổ và số dư |

### 7.3 Task & Timeline Acceptance Criteria

| Story ID | Given | When | Then |
|---|---|---|---|
| US-TTM-001 | User đã đăng nhập | User tạo Task hợp lệ | Task được tạo và thuộc User đó |
| US-TTM-002 | Task ở trạng thái cho phép sửa | User cập nhật Task | Task thay đổi theo thông tin hợp lệ |
| US-TTM-003 | Task đủ điều kiện xóa | User xóa Task | Task không còn trong danh sách hoạt động theo chính sách |
| US-TTM-004 | Task đủ điều kiện lưu trữ | User archive Task | Task được chuyển sang trạng thái lưu trữ |
| US-TTM-005 | Task đang lưu trữ | User restore Task | Task trở lại trạng thái phù hợp |
| US-TTM-006 | Task tồn tại | User duplicate Task | Task mới được tạo dựa trên Task gốc theo chính sách |
| US-TTM-007 | Category hợp lệ | User gán Category | Task được phân loại đúng Category |
| US-TTM-008 | Tag hợp lệ | User gán Tag | Task có Tag được chọn |
| US-TTM-009 | Task đang có Tag | User gỡ Tag | Tag được gỡ khỏi Task |
| US-TTM-010 | Priority thuộc tập giá trị cho phép | User đặt Priority | Priority được cập nhật |
| US-TTM-011 | Deadline hợp lệ | User đặt hoặc đổi Deadline | Deadline được lưu và không vi phạm quy tắc thời gian |
| US-TTM-012 | Task tồn tại | User nhập Estimated Time hợp lệ | Task có ước lượng thời gian |
| US-TTM-013 | Task tồn tại | User nhập Estimated Cost hợp lệ | Task có ước lượng chi phí |
| US-TTM-014 | Task có Time Capital | User đưa Task lên Timeline | Task xuất hiện trên Timeline |
| US-TTM-015 | Task đang trên Timeline | User đổi lịch | Lịch thực hiện được cập nhật |
| US-TTM-016 | Task đang trên Timeline | User kéo thả Task | Lịch thực hiện thay đổi theo vị trí mới nếu hợp lệ |
| US-TTM-017 | User có Task có Time Capital | User xem Timeline | Timeline hiển thị các Task đủ điều kiện |
| US-TTM-018 | User có nhiều Task | User tìm kiếm Task | Task phù hợp được hiển thị |
| US-TTM-019 | User có nhiều Task | User lọc Task | Danh sách Task được thu hẹp theo tiêu chí |
| US-TTM-020 | User có nhiều Task | User sắp xếp hoặc xem chi tiết | Thông tin Task được hiển thị đúng yêu cầu |
| US-TTM-021 | Task đang thực hiện | User cập nhật Progress | Progress được lưu trong khoảng 0 đến 100 |
| US-TTM-022 | Task đủ điều kiện tạm dừng | User pause | Task chuyển sang trạng thái tạm dừng |
| US-TTM-023 | Task đang tạm dừng | User resume | Task trở lại trạng thái thực hiện phù hợp |
| US-TTM-024 | Task đủ điều kiện hoàn thành | User complete | Task chuyển Completed và sẵn sàng đánh giá |
| US-TTM-025 | Task đủ điều kiện hủy | User cancel | Task được hủy theo chính sách |
| US-TTM-026 | Task đã hoàn thành hoặc hủy và chính sách cho phép | User reopen | Task trở lại trạng thái phù hợp để chỉnh sửa |
| US-TTM-027 | Recurring Task thuộc phạm vi | User tạo Task lặp | Các lần lặp được xác định theo quy tắc nghiệp vụ |
| US-TTM-028 | Task có thời gian hoặc Deadline | Sự kiện nhắc việc đến hạn | User nhận thông báo theo chính sách |

### 7.4 Tracking, Evaluation & Reporting Acceptance Criteria

| Story ID | Given | When | Then |
|---|---|---|---|
| US-TER-001 | Task đủ điều kiện ghi Actual | User nhập Actual Time không âm | Actual Time được ghi nhận |
| US-TER-002 | Actual Time được phép sửa | User cập nhật Actual Time | Actual Time mới được lưu và lịch sử phù hợp |
| US-TER-003 | Task đủ điều kiện ghi Actual | User nhập Actual Cost không âm | Actual Cost được ghi nhận |
| US-TER-004 | Actual Cost được phép sửa | User cập nhật Actual Cost | Actual Cost mới được lưu và lịch sử phù hợp |
| US-TER-005 | Task có Planned và Actual | User xem Planned vs Actual | Các giá trị được hiển thị để so sánh |
| US-TER-006 | Có Planned Time và Actual Time | User yêu cầu đánh giá | Time Variance được xác định |
| US-TER-007 | Có Planned Cost và Actual Cost | User yêu cầu đánh giá | Cost Variance được xác định |
| US-TER-008 | Có Variance hợp lệ | User xem Efficiency | Resource Efficiency được đánh giá |
| US-TER-009 | Có Task trong kỳ | User xem Completion Rate | Tỷ lệ hoàn thành được hiển thị |
| US-TER-010 | Có dữ liệu đánh giá | User xem Productivity Summary | Summary thể hiện hiệu quả tổng quan |
| US-TER-011 | Có dữ liệu theo ngày | User xem Daily Statistics | Thống kê ngày được hiển thị |
| US-TER-012 | Có dữ liệu theo tuần | User xem Weekly Statistics | Thống kê tuần được hiển thị |
| US-TER-013 | Có dữ liệu theo tháng | User xem Monthly Statistics | Thống kê tháng được hiển thị |
| US-TER-014 | Có dữ liệu theo năm | User xem Yearly Statistics | Thống kê năm được hiển thị |
| US-TER-015 | Có dữ liệu nguồn lực | User xem Resource Utilization | Mức sử dụng nguồn lực được hiển thị |
| US-TER-016 | User có dữ liệu đánh giá | User xem Dashboard | Dashboard hiển thị chỉ số thuộc phạm vi User |
| US-TER-017 | Có dữ liệu báo cáo | User xem Report | Report hiển thị nội dung đúng loại và kỳ |
| US-TER-018 | User có quyền xuất Report | User xuất Report | Report được xuất theo định dạng được cho phép |
| US-TER-019 | Có nhiều Report | User tìm Report | Kết quả phù hợp tiêu chí được hiển thị |
| US-TER-020 | Có nhiều Report | User lọc Report | Report được lọc theo tiêu chí hợp lệ |
| US-TER-021 | Có lịch sử đánh giá | User xem History | History hiển thị thay đổi và kết quả theo thời gian |
| US-TER-022 | Có ít nhất hai kỳ hợp lệ | User so sánh kỳ | Kết quả so sánh được hiển thị |
| US-TER-023 | Có dữ liệu nhiều kỳ | User xem Trend | Xu hướng được thể hiện theo tiêu chí nghiệp vụ |
| US-TER-024 | Có Category, Tag hoặc Timeline | User xem thống kê theo ngữ cảnh | Thống kê theo phân loại được hiển thị |
| US-TER-025 | Có dữ liệu KPI | User xem Personal KPI | KPI cá nhân được hiển thị theo định nghĩa nghiệp vụ |

### 7.5 Administration & Support Acceptance Criteria

| Story ID | Given | When | Then |
|---|---|---|---|
| US-ADM-001 | Actor có quyền quản trị hoặc hỗ trợ | Actor xem User | Danh sách User hiển thị theo phạm vi quyền |
| US-ADM-002 | Có danh sách User | Actor tìm User | Kết quả phù hợp được hiển thị |
| US-ADM-003 | Có danh sách User | Actor lọc User | Danh sách User được lọc |
| US-ADM-004 | Admin cập nhật thông tin hợp lệ | Admin lưu User | User được cập nhật và sự kiện được ghi nhận |
| US-ADM-005 | User đủ điều kiện ngưng hoạt động | Admin deactivate | User không còn truy cập được |
| US-ADM-006 | User đủ điều kiện kích hoạt lại | Admin reactivate | User truy cập lại theo quyền hợp lệ |
| US-ADM-007 | Có lý do khóa tạm hợp lệ | Staff hoặc Admin khóa tài khoản | Account bị khóa tạm và sự kiện được Audit |
| US-ADM-008 | Account đang khóa tạm | Admin unlock | Account được mở khóa nếu đủ điều kiện |
| US-ADM-009 | Admin có quyền xem Staff | Admin mở danh sách Staff | Danh sách Staff được hiển thị |
| US-ADM-010 | Staff đủ điều kiện thay đổi phân công | Admin quản lý Staff | Trách nhiệm Staff được cập nhật |
| US-ADM-011 | Actor có nhu cầu hỗ trợ | Actor tạo Ticket hợp lệ | Ticket được tạo với trạng thái ban đầu |
| US-ADM-012 | Ticket mới tồn tại | Staff nhận Ticket | Ticket được đưa vào quy trình hỗ trợ |
| US-ADM-013 | Ticket cần owner | Staff hoặc Admin gán Ticket | Ticket có người chịu trách nhiệm |
| US-ADM-014 | Ticket đang xử lý | Staff cập nhật Ticket | Ticket phản ánh tiến độ hiện tại |
| US-ADM-015 | Ticket cần hỗ trợ cấp cao hơn | Staff escalate | Ticket chuyển cấp theo chính sách |
| US-ADM-016 | Ticket có giải pháp | Staff resolve | Ticket được đánh dấu đã xử lý |
| US-ADM-017 | Ticket đã xử lý | Staff close | Ticket được đóng theo chính sách |
| US-ADM-018 | Ticket đóng nhầm hoặc cần xử lý tiếp | Staff hoặc Admin reopen | Ticket được mở lại |
| US-ADM-019 | Có nhiều Ticket | Actor tìm Ticket | Ticket phù hợp được hiển thị |
| US-ADM-020 | Có nhiều Ticket | Actor lọc Ticket | Ticket được lọc theo trạng thái, mức độ hoặc phân loại |
| US-ADM-021 | Actor có quyền xem log | Actor xem Activity Log | Activity Log hiển thị theo phạm vi quyền |
| US-ADM-022 | Có Activity Log | Actor tìm hoặc lọc | Kết quả liên quan được hiển thị |
| US-ADM-023 | Admin có quyền Audit | Admin xem Audit | Audit hiển thị sự kiện quản trị quan trọng |
| US-ADM-024 | Có Audit | Admin tìm Audit | Kết quả phù hợp được hiển thị |
| US-ADM-025 | Admin có quyền xem cấu hình | Admin mở cấu hình | Thiết lập nghiệp vụ được hiển thị |
| US-ADM-026 | Admin cập nhật cấu hình hợp lệ | Admin lưu cấu hình | Cấu hình thay đổi và có Audit |
| US-ADM-027 | Admin có quyền xem Dashboard quản trị | Admin mở Dashboard | Thông tin vận hành được hiển thị |
| US-ADM-028 | Có dữ liệu thống kê vận hành | Admin xem Statistics | Chỉ số vận hành được hiển thị |
| US-ADM-029 | Admin quản lý Role | Admin tạo hoặc cập nhật Role | Role được quản lý hợp lệ |
| US-ADM-030 | Admin quản lý Permission | Admin tạo hoặc cập nhật Permission | Permission được quản lý hợp lệ |
| US-ADM-031 | Account đủ điều kiện đổi Role | Admin gán hoặc thu hồi Role | Trách nhiệm truy cập được cập nhật |
| US-ADM-032 | Account đủ điều kiện đổi Permission | Admin gán hoặc thu hồi Permission | Quyền truy cập được cập nhật |
| US-ADM-033 | Announcement thuộc phạm vi | Admin gửi thông báo | Người nhận phù hợp nhận thông báo nghiệp vụ |

### 7.6 Cross-cutting Acceptance Criteria

| Story ID | Given | When | Then |
|---|---|---|---|
| US-CBR-001 | Một chức năng thuộc LifeBalance | Actor thực hiện hành động | Global Business Rules được áp dụng nhất quán |
| US-CBR-002 | Actor nhập hoặc thay đổi dữ liệu | Dữ liệu được kiểm tra | Dữ liệu không hợp lệ bị từ chối bằng thông báo nghiệp vụ rõ |
| US-CBR-003 | Hành động thuộc nhóm bắt buộc Audit | Actor thực hiện hành động | Audit được ghi nhận theo chính sách |
| US-CBR-004 | User có lịch sử hoạt động | User xem Activity History | Chỉ lịch sử thuộc phạm vi được hiển thị |
| US-CBR-005 | Sự kiện cần thông báo phát sinh | Hệ thống xác định người nhận | Thông báo được gửi theo chính sách nghiệp vụ |
| US-CBR-006 | Danh sách nghiệp vụ được hiển thị | Actor tìm, lọc, sắp xếp hoặc chuyển trang | Danh sách phản hồi nhất quán theo chuẩn |
| US-CBR-007 | Actor có quyền xuất dữ liệu | Actor yêu cầu xuất | Dữ liệu được xuất trong phạm vi được phép |
| US-CBR-008 | Lỗi nghiệp vụ xảy ra | Actor nhận phản hồi | Thông điệp thể hiện nguyên nhân và hành động tiếp theo phù hợp |

## 8. Definition of Ready (DoR)

Một User Story được xem là sẵn sàng đưa vào Sprint khi đáp ứng các điều kiện sau:

| Nhóm điều kiện | Definition of Ready |
|---|---|
| Rõ mục tiêu | Story thể hiện rõ actor, nhu cầu và giá trị theo cấu trúc As a / I want / So that |
| Có phạm vi | Story có Epic, Feature, Priority, Business Value và phạm vi nghiệp vụ rõ ràng |
| Có tiêu chí chấp nhận | Story có Acceptance Criteria theo Given / When / Then đủ để xác nhận hoàn thành |
| Có phụ thuộc | Phụ thuộc với Story khác hoặc điều kiện nghiệp vụ được xác định |
| Có giả định | Assumptions được ghi rõ nếu thông tin chưa đầy đủ |
| Có Open Questions | Các điểm chưa rõ không bị biến thành yêu cầu chính thức |
| Có quy tắc liên quan | Business Rules liên quan được tham chiếu hoặc xác định ở mức phù hợp |
| Có khả năng kiểm thử | QA có thể chuyển Acceptance Criteria thành kịch bản kiểm thử nghiệp vụ |
| Có độ lớn phù hợp | Story đủ nhỏ để hoàn thành trong một Sprint hoặc được tách tiếp nếu quá lớn |
| Có ưu tiên | Priority phản ánh giá trị, rủi ro và phụ thuộc nghiệp vụ |

## 9. Definition of Done (DoD)

Một User Story được xem là hoàn thành ở góc độ nghiệp vụ khi đáp ứng các điều kiện sau:

| Nhóm điều kiện | Definition of Done |
|---|---|
| Đúng nhu cầu | Kết quả đáp ứng User Story và mục tiêu kinh doanh đã mô tả |
| Đủ Acceptance Criteria | Tất cả Acceptance Criteria của Story đều được xác nhận đạt |
| Tuân thủ Business Rules | Các quy tắc nghiệp vụ liên quan được áp dụng đầy đủ |
| Không vượt phạm vi | Không bổ sung hành vi ngoài phạm vi đã thống nhất mà chưa được phê duyệt |
| Dữ liệu nghiệp vụ hợp lệ | Các Validation Rules liên quan được áp dụng đúng |
| Quyền truy cập phù hợp | Actor chỉ thực hiện được hành động trong phạm vi được phép |
| Có truy vết khi cần | Hành động bắt buộc Audit hoặc Activity History được ghi nhận theo chính sách |
| Xử lý ngoại lệ rõ | Lỗi nghiệp vụ, lỗi quyền, lỗi trạng thái và dữ liệu không hợp lệ có phản hồi rõ ràng |
| Sẵn sàng nghiệm thu | Product Owner và stakeholder liên quan có thể nghiệm thu dựa trên tiêu chí đã thống nhất |
| Cập nhật tài liệu | Nếu phạm vi nghiệp vụ thay đổi, backlog và traceability được cập nhật |

## 10. Backlog Prioritization

### 10.1 Tiêu chí ưu tiên

Backlog được ưu tiên theo bốn tiêu chí:

| Tiêu chí | Ý nghĩa |
|---|---|
| Business Value | Mức độ đóng góp vào Vision, MVP và lợi ích người dùng |
| Risk | Mức độ giảm rủi ro nghiệp vụ hoặc vận hành khi triển khai sớm |
| Dependency | Mức độ là tiền đề cho Story khác |
| Priority | Phân loại Critical, High, Medium, Low để phục vụ Release và Sprint |

### 10.2 Backlog ưu tiên theo nhóm

| Priority | Story Group | Rationale |
|---|---|---|
| Critical | US-IAM-002, US-IAM-003, US-IAM-005, US-IAM-010, US-IAM-011 | Không có Identity và quyền truy cập thì các module khác không thể vận hành an toàn |
| Critical | US-RCM-001, US-RCM-003, US-RCM-006, US-RCM-007, US-RCM-010, US-RCM-014, US-RCM-015 | Đây là năng lực cốt lõi của triết lý nguồn lực hữu hạn |
| Critical | US-TTM-001, US-TTM-002, US-TTM-012, US-TTM-014, US-TTM-017, US-TTM-021, US-TTM-024 | Task và Timeline là trung tâm của kế hoạch và thực hiện |
| Critical | US-TER-001, US-TER-003, US-TER-005, US-TER-006, US-TER-007, US-TER-008 | Đánh giá hiệu quả là điểm khác biệt chính của LifeBalance |
| Critical | US-ADM-029, US-ADM-030, US-ADM-031, US-ADM-032 | Quản trị Role và Permission cần có để kiểm soát vận hành |
| Critical | US-CBR-001, US-CBR-002, US-CBR-003 | Quy tắc toàn cục, kiểm tra hợp lệ và Audit là nền tảng dùng chung |
| High | US-IAM-006 đến US-IAM-009, US-IAM-020 đến US-IAM-027 | Tăng khả năng tự phục vụ và quản trị danh tính |
| High | US-RCM-002, US-RCM-004, US-RCM-008, US-RCM-009, US-RCM-011, US-RCM-012, US-RCM-016, US-RCM-017, US-RCM-020 | Hoàn thiện quản lý nguồn vốn |
| High | US-TTM-007 đến US-TTM-011, US-TTM-015, US-TTM-016, US-TTM-018 đến US-TTM-020 | Hoàn thiện lập kế hoạch và tra cứu Task |
| High | US-TER-009 đến US-TER-017, US-TER-021 | Hoàn thiện phân tích và báo cáo nền tảng |
| High | US-ADM-001 đến US-ADM-014, US-ADM-016, US-ADM-017, US-ADM-021 đến US-ADM-024 | Hỗ trợ vận hành và truy vết |
| Medium | US-RCM-005, US-RCM-013, US-RCM-018, US-RCM-019 | Tăng tính linh hoạt nhưng không bắt buộc cho MVP tối thiểu |
| Medium | US-TTM-003 đến US-TTM-006, US-TTM-022, US-TTM-023, US-TTM-025 đến US-TTM-028 | Tối ưu vòng đời Task và tiện ích nâng cao |
| Medium | US-TER-018 đến US-TER-020, US-TER-022 đến US-TER-025 | Phân tích nâng cao và chia sẻ kết quả |
| Medium | US-ADM-015, US-ADM-018, US-ADM-025 đến US-ADM-028 | Nâng cao vận hành quản trị |
| Low | US-ADM-033 | Announcement phụ thuộc xác nhận phạm vi |

## 11. Release Planning

### 11.1 Release Strategy

Release Planning được đề xuất theo giá trị tăng dần:

1. Release 1 tập trung MVP: xác thực người dùng, nguồn lực, Task, Timeline cơ bản và đánh giá cơ bản.
2. Release 2 mở rộng phân tích, báo cáo, lịch sử và quản trị vận hành.
3. Release 3 hoàn thiện hỗ trợ, quản trị nâng cao, cải tiến năng suất và các chức năng tùy chọn.

### 11.2 Proposed Releases

| Release | Scope | Business Objective | Story List |
|---|---|---|---|
| Release 1 - MVP Foundation | Identity nền tảng, Resource Capital nền tảng, Task nền tảng, Timeline cơ bản, Actual và Variance cơ bản | Cho phép người dùng lập kế hoạch nguồn lực, tạo Task, lên lịch, hoàn thành và đánh giá cơ bản | US-IAM-001 đến US-IAM-011; US-RCM-001, 003, 006, 007, 010, 014, 015; US-TTM-001, 002, 007, 010, 011, 012, 013, 014, 017, 021, 024; US-TER-001, 003, 005, 006, 007, 008; US-CBR-001 đến US-CBR-003 |
| Release 2 - Operational Product | Profile, Account Governance, Capital Adjustment, Reallocation, Task Search/Filter, Dashboard, Statistics, Support Ticket cơ bản | Tăng khả năng sử dụng thực tế và hỗ trợ vận hành | US-IAM-006 đến US-IAM-010, 020 đến 027; US-RCM-002, 004, 008, 009, 011, 012, 016, 017, 020; US-TTM-015, 016, 018 đến 020, 022, 023, 025; US-TER-009 đến US-TER-017, 021; US-ADM-001 đến US-ADM-014, 016, 017, 021 đến 024; US-CBR-004, 006, 008 |
| Release 3 - Governance & Improvement | Role/Permission nâng cao, Report nâng cao, Compare, Trend, KPI, Recurring, Reminder, cấu hình, Dashboard quản trị, Ticket nâng cao | Hoàn thiện quản trị, phân tích và cải thiện liên tục | US-IAM-012 đến US-IAM-019, 028; US-RCM-005, 013, 018, 019; US-TTM-003 đến 006, 026 đến 028; US-TER-018 đến 020, 022 đến 025; US-ADM-015, 018, 025 đến 033; US-CBR-005, 007 |

### 11.3 Release Justification

Release 1 ưu tiên năng lực cốt lõi: người dùng đăng nhập, xác định nguồn lực, tạo Task, lên Timeline, hoàn thành và đánh giá Planned vs Actual. Đây là chuỗi giá trị tối thiểu thể hiện khác biệt của LifeBalance.

Release 2 làm sản phẩm vận hành được trong thực tế với hồ sơ, quản trị tài khoản, điều chỉnh nguồn lực, tìm kiếm, thống kê và hỗ trợ người dùng.

Release 3 tập trung nâng cao quản trị, mở rộng phân tích, cải tiến năng suất và các chức năng cần xác nhận phạm vi.

## 12. Sprint Planning Recommendation

### 12.1 Sprint Assumptions

| Assumption | Description |
|---|---|
| Sprint length | 2 tuần |
| Planning basis | Story Point nghiệp vụ và phụ thuộc Backlog |
| Team capacity | Chưa xác định, cần Product Owner và Scrum Master xác nhận |
| Sprint scope | Có thể điều chỉnh sau refinement |

### 12.2 Sprint Plan đề xuất

| Sprint | Sprint Goal | Business Value | Story List |
|---|---|---|---|
| Sprint 1 | Thiết lập truy cập cơ bản | Guest có thể đăng ký, User có thể đăng nhập, đăng xuất và phiên được kiểm soát | US-IAM-001, 002, 003, 004, 005, 010, 011 |
| Sprint 2 | Hoàn thiện hồ sơ và quyền truy cập nền tảng | User tự quản lý hồ sơ và hệ thống kiểm soát truy cập ổn định | US-IAM-006, 007, 008, 009, US-CBR-001, 002, 003 |
| Sprint 3 | Khởi tạo nguồn vốn cá nhân | User tạo chu kỳ và thiết lập Time/Money Capital | US-RCM-001, 003, 006, 007, 014, 015 |
| Sprint 4 | Phân bổ nguồn lực | User phân bổ nguồn lực và xem số dư phục vụ lập kế hoạch | US-RCM-010, 011, 012, 016, 017, 020 |
| Sprint 5 | Tạo và lập kế hoạch Task | User tạo Task, đặt thông tin kế hoạch và phân loại | US-TTM-001, 002, 007, 008, 010, 011, 012, 013 |
| Sprint 6 | Lập lịch Timeline | User đưa Task lên Timeline, đổi lịch, kéo thả và xem lịch | US-TTM-014, 015, 016, 017 |
| Sprint 7 | Thực hiện Task | User cập nhật tiến độ, tạm dừng, tiếp tục, hoàn thành và hủy Task | US-TTM-021, 022, 023, 024, 025 |
| Sprint 8 | Ghi nhận Actual và đánh giá cơ bản | User ghi Actual và xem Planned vs Actual, Variance, Efficiency | US-TER-001, 002, 003, 004, 005, 006, 007, 008 |
| Sprint 9 | Dashboard và thống kê | User xem Summary, Statistics, Utilization và Dashboard | US-TER-009, 010, 011, 012, 013, 014, 015, 016 |
| Sprint 10 | Report và History | User xem, tìm, lọc, xuất Report và lịch sử đánh giá | US-TER-017, 018, 019, 020, 021 |
| Sprint 11 | Quản trị User và Staff | Staff/Admin quản lý User, Staff và khóa/mở tài khoản | US-ADM-001 đến US-ADM-010 |
| Sprint 12 | Support Ticket | Staff nhận, gán, cập nhật, xử lý, đóng và mở lại Ticket | US-ADM-011 đến US-ADM-020 |
| Sprint 13 | Audit, Activity và cấu hình vận hành | Admin xem Audit, Activity, cấu hình và Dashboard quản trị | US-ADM-021 đến US-ADM-028 |
| Sprint 14 | Role, Permission và chuẩn dùng chung nâng cao | Admin quản trị quyền; chuẩn thông báo, xuất dữ liệu và lỗi được áp dụng | US-IAM-012 đến US-IAM-019, US-ADM-029 đến US-ADM-033, US-CBR-005, 007, 008 |
| Sprint 15 | Cải tiến năng suất nâng cao | Recurring, Reminder, Compare, Trend, KPI và các phạm vi tùy chọn được hoàn thiện nếu được xác nhận | US-TTM-003 đến 006, 026 đến 028, US-TER-022 đến 025, US-RCM-005, 013, 018, 019 |

## 13. Traceability Matrix

### 13.1 Traceability theo Business Goal

| Business Goal | Epic | Feature | Story | Use Case | Functional Requirement | Acceptance Criteria |
|---|---|---|---|---|---|---|
| Cho phép truy cập hợp lệ và đúng quyền | E-IAM | IAM-F01 đến IAM-F06 | US-IAM-001 đến US-IAM-028 | IAM-UC-001 đến IAM-UC-028 | IAM-FR-001 đến IAM-FR-045 | AC US-IAM-001 đến US-IAM-028 |
| Quản lý nguồn lực hữu hạn | E-RCM | RCM-F01 đến RCM-F05 | US-RCM-001 đến US-RCM-020 | RCM-UC-001 đến RCM-UC-027 | RCM-FR-001 đến RCM-FR-050 | AC US-RCM-001 đến US-RCM-020 |
| Lập kế hoạch và thực hiện công việc | E-TTM | TTM-F01 đến TTM-F06 | US-TTM-001 đến US-TTM-028 | TTM-UC-001 đến TTM-UC-031 | TTM-FR-001 đến TTM-FR-060 | AC US-TTM-001 đến US-TTM-028 |
| Đánh giá hiệu quả sử dụng nguồn lực | E-TER | TER-F01 đến TER-F05 | US-TER-001 đến US-TER-025 | TER-UC-001 đến TER-UC-025 | TER-FR-001 đến TER-FR-060 | AC US-TER-001 đến US-TER-025 |
| Vận hành và hỗ trợ hệ thống | E-ADM | ADM-F01 đến ADM-F06 | US-ADM-001 đến US-ADM-033 | ADM-UC-001 đến ADM-UC-033 | ADM-FR-001 đến ADM-FR-060 | AC US-ADM-001 đến US-ADM-033 |
| Chuẩn hóa yêu cầu dùng chung | E-CBR | CBR-F01 đến CBR-F06 | US-CBR-001 đến US-CBR-008 | CBR-UC-001 đến CBR-UC-008 | GBR-001 đến GBR-030, VAL-001 đến VAL-028 | AC US-CBR-001 đến US-CBR-008 |

### 13.2 Traceability chi tiết theo Feature

| Feature | Story Range | Related Use Case Range | Related Requirement Range |
|---|---|---|---|
| IAM-F01 | US-IAM-001 đến 002 | IAM-UC-001 đến 002 | IAM-FR-001, IAM-FR-002, IAM-FR-029 |
| IAM-F02 | US-IAM-003 đến 005 | IAM-UC-003 đến 005 | IAM-FR-003, IAM-FR-004, IAM-FR-008, IAM-FR-025 |
| IAM-F03 | US-IAM-006 đến 009 | IAM-UC-006 đến 009 | IAM-FR-005 đến IAM-FR-007 |
| IAM-F04 | US-IAM-010, 011, 028 | IAM-UC-010, 011, 028 | IAM-FR-009 đến IAM-FR-012, IAM-FR-026 đến IAM-FR-028 |
| IAM-F05 | US-IAM-012 đến 019 | IAM-UC-012 đến 019 | IAM-FR-013 đến IAM-FR-024, IAM-FR-030 đến IAM-FR-034 |
| IAM-F06 | US-IAM-020 đến 027 | IAM-UC-020 đến 027 | IAM-FR-035 đến IAM-FR-045 |
| RCM-F01 | US-RCM-001 đến 005 | RCM-UC-001 đến 005 | RCM-FR-001 đến RCM-FR-005 |
| RCM-F02 | US-RCM-006 đến 007 | RCM-UC-006 đến 007 | RCM-FR-006 đến RCM-FR-007 |
| RCM-F03 | US-RCM-008 đến 009 | RCM-UC-008 đến 009 | RCM-FR-008 đến RCM-FR-009 |
| RCM-F04 | US-RCM-010 đến 013 | RCM-UC-010 đến 014 | RCM-FR-010 đến RCM-FR-014 |
| RCM-F05 | US-RCM-014 đến 020 | RCM-UC-015 đến 027 | RCM-FR-015 đến RCM-FR-050 |
| TTM-F01 | US-TTM-001 đến 006 | TTM-UC-001 đến 006 | TTM-FR-001 đến TTM-FR-006 |
| TTM-F02 | US-TTM-007 đến 013 | TTM-UC-007 đến 013 | TTM-FR-007 đến TTM-FR-013 |
| TTM-F03 | US-TTM-014 đến 017 | TTM-UC-014 đến 017 | TTM-FR-014 đến TTM-FR-017 |
| TTM-F04 | US-TTM-018 đến 020 | TTM-UC-018 đến 020 | TTM-FR-018 đến TTM-FR-020 |
| TTM-F05 | US-TTM-021 đến 026 | TTM-UC-021 đến 026 | TTM-FR-021 đến TTM-FR-026 |
| TTM-F06 | US-TTM-027 đến 028 | TTM-UC-027 đến 031 | TTM-FR-027 đến TTM-FR-060 |
| TER-F01 | US-TER-001 đến 004 | TER-UC-001 đến 004 | TER-FR-001 đến TER-FR-004 |
| TER-F02 | US-TER-005 đến 009 | TER-UC-005 đến 009 | TER-FR-005 đến TER-FR-009 |
| TER-F03 | US-TER-010 đến 016 | TER-UC-010 đến 016 | TER-FR-010 đến TER-FR-016 |
| TER-F04 | US-TER-017 đến 020 | TER-UC-017 đến 020 | TER-FR-017 đến TER-FR-020 |
| TER-F05 | US-TER-021 đến 025 | TER-UC-021 đến 025 | TER-FR-021 đến TER-FR-060 |
| ADM-F01 | US-ADM-001 đến 008 | ADM-UC-001 đến 008 | ADM-FR-001 đến ADM-FR-008 |
| ADM-F02 | US-ADM-009 đến 010 | ADM-UC-009 đến 010 | ADM-FR-009 đến ADM-FR-012 |
| ADM-F03 | US-ADM-011 đến 020 | ADM-UC-011 đến 020 | ADM-FR-013 đến ADM-FR-024 |
| ADM-F04 | US-ADM-021 đến 024 | ADM-UC-021 đến 024 | ADM-FR-025 đến ADM-FR-030 |
| ADM-F05 | US-ADM-025 đến 028, 033 | ADM-UC-025 đến 028, 033 | ADM-FR-031 đến ADM-FR-040, ADM-FR-060 |
| ADM-F06 | US-ADM-029 đến 032 | ADM-UC-029 đến 032 | ADM-FR-041 đến ADM-FR-059 |
| CBR-F01 đến F06 | US-CBR-001 đến 008 | CBR-UC-001 đến 008 | GBR-001 đến GBR-030, VAL-001 đến VAL-028 |

## 14. Story Dependency Matrix

### 14.1 Dependency theo Epic

| From Story Group | Depends On | Dependency Type | Business Rationale |
|---|---|---|---|
| RCM Stories | IAM Authentication | Mandatory | User phải được xác thực trước khi quản lý nguồn lực cá nhân |
| TTM Stories | IAM Authentication | Mandatory | Task thuộc phạm vi User |
| TTM Timeline Stories | RCM Time Capital | Mandatory | Timeline chỉ hiển thị Task có Time Capital |
| TER Stories | TTM Completed Task | Mandatory | Đánh giá dựa trên Task đã đủ điều kiện |
| TER Resource Utilization | RCM Capital Summary | Mandatory | Sử dụng nguồn lực cần dữ liệu vốn và phân bổ |
| ADM Stories | IAM Role & Permission | Mandatory | Quản trị cần kiểm soát quyền |
| CBR Stories | All Epic | Cross-cutting | Quy tắc dùng chung áp dụng xuyên suốt |

### 14.2 Dependency chi tiết theo Story

| Story | Depends On | Reason |
|---|---|---|
| US-IAM-003 | US-IAM-002 | Cần tài khoản trước khi đăng nhập |
| US-IAM-005 | US-IAM-003 | Quản lý phiên chỉ phát sinh sau đăng nhập |
| US-IAM-012 đến US-IAM-019 | US-IAM-011 | Quản lý quyền phụ thuộc kiểm tra quyền |
| US-RCM-006, US-RCM-007 | US-RCM-001 | Capital phải thuộc chu kỳ |
| US-RCM-010 | US-RCM-006, US-RCM-007 | Phân bổ cần nguồn lực được thiết lập |
| US-RCM-015 | US-RCM-010 | Remaining Capital phụ thuộc phân bổ |
| US-TTM-014 | US-TTM-012 | Timeline cần Time Capital |
| US-TTM-024 | US-TTM-021 | Hoàn thành Task phụ thuộc tiến độ và trạng thái thực hiện |
| US-TER-001, US-TER-003 | US-TTM-024 | Ghi Actual cuối cùng cần Task đủ điều kiện |
| US-TER-008 | US-TER-006, US-TER-007 | Efficiency phụ thuộc Variance |
| US-TER-016 | US-TER-010 đến US-TER-015 | Dashboard tổng hợp từ các thống kê |
| US-ADM-031, US-ADM-032 | US-ADM-029, US-ADM-030 | Gán/thu hồi cần Role/Permission tồn tại |
| US-CBR-003 | Các hành động quan trọng | Audit áp dụng khi sự kiện phát sinh |

## 15. Story Risk Analysis

| Risk Category | Risk | Affected Stories | Impact | Mitigation ở mức nghiệp vụ |
|---|---|---|---|---|
| Business Risk | Người dùng không quen tư duy “Task là khoản đầu tư nguồn lực” | US-RCM-010, US-TTM-012, US-TTM-014 | Adoption thấp | Cần ngôn ngữ sản phẩm rõ, hướng dẫn nghiệp vụ và onboarding phù hợp |
| Requirement Risk | Recurring Task, Reminder, Export, Announcement chưa xác nhận phạm vi | US-TTM-027, US-TTM-028, US-TER-018, US-ADM-033, US-CBR-005, US-CBR-007 | Dễ phát sinh thay đổi | Đưa vào Open Questions và ưu tiên sau MVP |
| Dependency Risk | Timeline phụ thuộc Time Capital và phân bổ hợp lệ | US-TTM-014 đến US-TTM-017 | Chậm chuỗi kế hoạch | Ưu tiên Resource trước Timeline trong Release 1 |
| Dependency Risk | Tracking phụ thuộc Task Completed | US-TER-001 đến US-TER-008 | Không thể đánh giá nếu Task Lifecycle chưa ổn định | Hoàn thiện Task Completion trước Evaluation |
| Governance Risk | Admin tự thu hồi quyền hoặc xóa quyền quan trọng | US-IAM-017, US-IAM-019, US-ADM-031, US-ADM-032 | Mất khả năng quản trị | Cần chính sách chống mất quyền quản trị cuối cùng |
| Operational Risk | Staff khóa nhầm tài khoản | US-IAM-023, US-ADM-007 | Gián đoạn người dùng | Cần quy trình lý do khóa, giới hạn Staff và Audit |
| Reporting Risk | KPI chưa thống nhất ngưỡng đánh giá | US-TER-008, US-TER-025 | Diễn giải sai hiệu quả | Product Owner xác nhận định nghĩa KPI trước Release có KPI nâng cao |
| Support Risk | Ticket không có Staff xử lý | US-ADM-012, US-ADM-013 | Tồn đọng hỗ trợ | Cần quy tắc ownership và escalation |
| Delivery Risk | Backlog lớn, nhiều Story phụ thuộc nhau | Toàn bộ | Khó lập Sprint ổn định | Dùng MVP rõ và refinement theo Release |

## 16. MVP Definition

### 16.1 MVP Objective

MVP của LifeBalance phải chứng minh được giá trị khác biệt cốt lõi: người dùng có thể lập kế hoạch nguồn lực, phân bổ vào Task, thực hiện Task và đánh giá Planned vs Actual ở mức cơ bản.

### 16.2 Mandatory MVP Features

| MVP Feature | Included Stories | Reason |
|---|---|---|
| Authentication nền tảng | US-IAM-001 đến US-IAM-005, US-IAM-010, US-IAM-011 | Bảo đảm người dùng hợp lệ và phạm vi quyền |
| Capital Cycle và Capital Setup | US-RCM-001, 003, 006, 007, 014, 015 | Cho phép biết nguồn lực khả dụng và còn lại |
| Capital Allocation | US-RCM-010 | Thể hiện triết lý “Task cần được cấp nguồn lực” |
| Task Core | US-TTM-001, 002 | Tạo và quản lý công việc cơ bản |
| Task Planning | US-TTM-007, 010, 011, 012, 013 | Có đủ thông tin lập kế hoạch |
| Timeline cơ bản | US-TTM-014, 017 | Task có Time Capital xuất hiện trên Timeline |
| Task Execution | US-TTM-021, 024 | Có trạng thái thực hiện và hoàn thành |
| Actual Recording | US-TER-001, 003 | Có dữ liệu thực tế |
| Basic Evaluation | US-TER-005, 006, 007, 008 | So sánh Planned vs Actual và đánh giá hiệu quả |
| Global Standards | US-CBR-001, 002, 003 | Quy tắc, kiểm tra hợp lệ và Audit cho hành động quan trọng |

### 16.3 Deferred Features

| Deferred Feature | Stories | Reason |
|---|---|---|
| Recurring Task và Reminder | US-TTM-027, US-TTM-028 | Cần xác nhận chính sách và không bắt buộc để chứng minh MVP |
| Export nâng cao | US-TER-018, US-CBR-007 | Chỉ cần khi báo cáo được sử dụng ngoài hệ thống |
| Trend và KPI nâng cao | US-TER-022 đến US-TER-025 | Cần dữ liệu nhiều kỳ để có giá trị |
| Announcement | US-ADM-033 | Phạm vi vận hành cần xác nhận |
| Configuration nâng cao | US-ADM-025, US-ADM-026 | Cần sau khi chính sách vận hành ổn định |
| Reopen nâng cao | US-RCM-005, US-TTM-026, US-ADM-018 | Cần kiểm soát chặt để không làm sai lịch sử |

## 17. Product Roadmap

| Phase | Product Focus | Business Outcome | Candidate Stories |
|---|---|---|---|
| MVP | Resource planning, Task planning, Timeline basic, Actual and basic Evaluation | Chứng minh giá trị cốt lõi của LifeBalance | Story bắt buộc trong mục 16.2 |
| Version 1 | Hoàn thiện trải nghiệm vận hành cá nhân | Người dùng quản lý Task, nguồn lực, lịch sử và thống kê cơ bản ổn định | Release 1 và phần chính của Release 2 |
| Version 2 | Reporting, Dashboard, Support và Governance | Sản phẩm có khả năng phân tích, hỗ trợ và quản trị tốt hơn | Story còn lại của Release 2 và phần trọng yếu Release 3 |
| Future Vision | Cải thiện liên tục, KPI cá nhân, xu hướng, nhắc việc và chức năng tùy chọn | LifeBalance trở thành công cụ quản trị hiệu quả cá nhân theo dữ liệu | Recurring, Reminder, Trend, Personal KPI, Announcement và cải tiến backlog |

## 18. Open Questions

| ID | Question | Affected Area | Impact if unresolved |
|---|---|---|---|
| OQ-001 | Trường thông tin bắt buộc khi đăng ký gồm những gì? | Identity | Ảnh hưởng US-IAM-002 và Validation |
| OQ-002 | Chính sách mật khẩu và khôi phục mật khẩu được định nghĩa như thế nào? | Identity | Ảnh hưởng US-IAM-008, US-IAM-009 |
| OQ-003 | Thời lượng phiên làm việc và điều kiện hết hạn phiên là gì? | Identity | Ảnh hưởng US-IAM-005 |
| OQ-004 | Admin có được tự thu hồi quyền Admin cuối cùng không? | Governance | Ảnh hưởng US-IAM-017, US-ADM-031 |
| OQ-005 | Điều kiện cho phép Reopen Capital Cycle là gì? | Resource | Ảnh hưởng US-RCM-005 |
| OQ-006 | Khi điều chỉnh nguồn vốn nhỏ hơn số đã phân bổ, chính sách xử lý là gì? | Resource | Ảnh hưởng US-RCM-008, US-RCM-009 |
| OQ-007 | Over Allocation có cần lý do bắt buộc và ngưỡng cảnh báo không? | Resource | Ảnh hưởng US-RCM-013 |
| OQ-008 | Task Delete là xóa khỏi hoạt động hay chỉ chuyển trạng thái không hoạt động? | Task | Ảnh hưởng US-TTM-003 |
| OQ-009 | Bộ giá trị Priority chính thức gồm những mức nào? | Task | Ảnh hưởng US-TTM-010 |
| OQ-010 | Chính sách xử lý Task bị kéo chồng lên Task khác là gì? | Timeline | Ảnh hưởng US-TTM-016 |
| OQ-011 | Recurring Task có thuộc MVP hoặc Version 1 không? | Task | Ảnh hưởng US-TTM-027 |
| OQ-012 | Reminder và Notification có được đưa vào phạm vi chính thức không? | Cross-cutting | Ảnh hưởng US-TTM-028, US-CBR-005 |
| OQ-013 | Task chưa Completed có được ghi Actual tạm thời không? | Tracking | Ảnh hưởng US-TER-001 đến US-TER-004 |
| OQ-014 | Ngưỡng đánh giá Efficiency và Productivity Score là gì? | Evaluation | Ảnh hưởng US-TER-008, US-TER-025 |
| OQ-015 | Định dạng xuất Report nào được chấp nhận chính thức? | Reporting | Ảnh hưởng US-TER-018, US-CBR-007 |
| OQ-016 | Staff có được tạo Ticket thay User không? | Support | Ảnh hưởng US-ADM-011 |
| OQ-017 | Quy tắc phân công Ticket tự động hay thủ công? | Support | Ảnh hưởng US-ADM-012, US-ADM-013 |
| OQ-018 | Announcement và Maintenance Mode có thuộc phạm vi Release đầu không? | Administration | Ảnh hưởng US-ADM-033 |
| OQ-019 | Thời gian lưu giữ Audit và Activity History theo nghiệp vụ là bao lâu? | Cross-cutting | Ảnh hưởng US-CBR-003, US-CBR-004 |
| OQ-020 | Các chỉ số Dashboard quản trị chính thức gồm những gì? | Administration | Ảnh hưởng US-ADM-027, US-ADM-028 |

## 19. Suggested Improvements

| ID | Improvement | Business Rationale | Suggested Timing |
|---|---|---|---|
| SI-001 | Thiết lập Product Backlog refinement định kỳ theo từng Release | Backlog lớn và nhiều phụ thuộc, cần liên tục làm rõ trước Sprint | Trước Sprint 1 và lặp lại hàng Sprint |
| SI-002 | Chuẩn hóa thang Priority, KPI và Status trước MVP | Giảm rủi ro diễn giải khác nhau giữa BA, PO và QA | Trước Sprint 3 |
| SI-003 | Tách các Story có Story Point lớn hơn 8 trong refinement | Giúp Sprint dễ cam kết và nghiệm thu | Trước khi đưa vào Sprint |
| SI-004 | Xây dựng checklist nghiệp vụ cho các hành động bắt buộc Audit | Đảm bảo không bỏ sót truy vết với quyền, nguồn lực, Task và cấu hình | Release 1 |
| SI-005 | Xác nhận chính sách Reopen cho Task, Capital Cycle và Ticket | Reopen tác động lịch sử và kết quả đánh giá | Release 2 |
| SI-006 | Xác nhận định nghĩa KPI và thang đánh giá Efficiency | KPI chỉ có giá trị khi người dùng hiểu cách diễn giải | Trước Release 2 |
| SI-007 | Phân tách MVP và Future Scope bằng tiêu chí giá trị thực nghiệm | Tránh đưa quá nhiều tính năng nâng cao vào giai đoạn đầu | Ngay sau khi tài liệu được phê duyệt |
| SI-008 | Thiết lập quy trình quản lý thay đổi yêu cầu | Các volume có nhiều phạm vi tùy chọn, cần kiểm soát thay đổi | Toàn dự án |
| SI-009 | Bổ sung User Story Workshop với đại diện Student, Office Worker và Freelancer | Đảm bảo backlog phản ánh đúng persona chính | Trước Release 1 |
| SI-010 | Định kỳ rà soát traceability từ Business Goal đến Acceptance Criteria | Bảo đảm mọi Story vẫn bám sát Vision LifeBalance | Sau mỗi Release |

# LifeBalance
# Volume 10 – Business Analysis Reference & Models

## 1. Document Overview

### 1.1 Purpose

Tài liệu này là tài liệu tham chiếu tổng hợp cuối cùng của bộ Business Analysis cho dự án LifeBalance. Mục đích của tài liệu là chuẩn hóa, liên kết và kiểm tra tính đầy đủ của toàn bộ mô hình nghiệp vụ đã được mô tả trong Volume 1 đến Volume 9.

Tài liệu không tạo thêm Functional Requirement mới, không thay đổi yêu cầu đã được phê duyệt và không thay thế các tài liệu chi tiết theo từng module. Thay vào đó, tài liệu đóng vai trò bản đồ tham chiếu giúp các bên liên quan tra cứu quy trình, trạng thái, quyết định nghiệp vụ, ma trận quyền, traceability, glossary và các phát hiện về tính nhất quán.

### 1.2 Scope

Phạm vi tài liệu bao gồm:

| Nhóm nội dung | Phạm vi tổng hợp |
|---|---|
| Business Models | Mô hình quy trình, mô hình hoạt động, mô hình trạng thái và decision table ở mức nghiệp vụ |
| Business Processes | Danh mục quy trình theo Identity, Resource, Task, Timeline, Tracking, Reporting, Administration và Support |
| Business Rules | Ma trận Business Rule dùng chung và theo module |
| Traceability | Liên kết Business Goal, Business Requirement, Functional Requirement, Use Case, User Story, Acceptance Criteria và Module |
| Coverage Review | Kiểm tra mức độ bao phủ giữa Functional Requirement, Use Case, Story và Acceptance Criteria |
| Business Glossary | Chuẩn hóa thuật ngữ nghiệp vụ dùng trong toàn bộ LifeBalance |
| Consistency Review | Ghi nhận phát hiện về trùng lặp, thiếu, chưa rõ hoặc cần xác nhận giữa các volume |

### 1.3 Objectives

1. Cung cấp một tài liệu tham chiếu duy nhất cho toàn bộ mô hình nghiệp vụ của LifeBalance.
2. Đảm bảo mọi nhóm nghiệp vụ đã được liên kết với quy trình, Use Case, User Story và Acceptance Criteria tương ứng.
3. Chuẩn hóa cách hiểu về thuật ngữ, trạng thái, quy tắc, quyền và kết quả nghiệp vụ.
4. Ghi nhận các điểm cần xác nhận mà không tự ý sửa hoặc mở rộng requirement.
5. Hỗ trợ BA, Product Owner, nhóm thiết kế trải nghiệm, nhóm xây dựng sản phẩm và QA tra cứu nhất quán.

### 1.4 Intended Audience

| Audience | Mục đích sử dụng |
|---|---|
| Business Analyst | Tra cứu quy trình, rule, glossary, traceability và consistency review |
| Product Owner | Quản lý phạm vi, backlog, MVP, release và ưu tiên sản phẩm |
| Scrum Master | Theo dõi phụ thuộc, rủi ro và chuẩn bị Sprint Planning |
| Nhóm thiết kế giải pháp | Hiểu mô hình nghiệp vụ và ràng buộc business trước khi thiết kế triển khai |
| UI/UX Designer | Hiểu actor, state, workflow, decision point và glossary để thiết kế trải nghiệm phù hợp |
| Developer | Hiểu hành vi nghiệp vụ, điều kiện xử lý và tiêu chí chấp nhận |
| QA/Tester | Đối chiếu Use Case, Acceptance Criteria, Decision Table và Edge Case |
| Stakeholder vận hành | Rà soát quy trình hỗ trợ, quản trị, Audit và Activity History |

### 1.5 Related Documents

| Volume | Document Name | Vai trò tham chiếu |
|---|---|---|
| Volume 1 | Vision & Business Overview | Nguồn gốc Vision, Goal, Scope và Product Philosophy |
| Volume 2 | Identity & Authorization | SRS cho danh tính, xác thực, phân quyền và trạng thái tài khoản |
| Volume 3 | Resource Capital Management | SRS cho quản lý Time Capital, Money Capital, chu kỳ và phân bổ |
| Volume 4 | Task & Timeline Management | SRS cho Task, Timeline, lập kế hoạch và vòng đời Task |
| Volume 5 | Tracking, Evaluation & Reporting | SRS cho Actual, Variance, Efficiency, Dashboard và Report |
| Volume 6 | Administration & Support | SRS cho quản trị vận hành và Support Ticket |
| Volume 7 | Cross-cutting Business Requirements | Chuẩn nghiệp vụ dùng chung toàn hệ thống |
| Volume 8 | Complete Use Case Specification | Đặc tả Use Case tổng hợp |
| Volume 9 | User Stories & Product Backlog | Product Backlog, Story, Acceptance Criteria, Release và Sprint Planning |

## 2. Business Process Catalog

### 2.1 Identity Processes

| Process ID | Process Name | Description | Related Modules | Related Actors |
|---|---|---|---|---|
| BP-IAM-001 | Guest Access | Guest truy cập Landing Page và xem các hành động Register/Login | Identity | Guest |
| BP-IAM-002 | Account Registration | Guest tạo tài khoản để trở thành User | Identity | Guest |
| BP-IAM-003 | Login | Actor xác thực để truy cập chức năng được phép | Identity | User, Staff, Admin |
| BP-IAM-004 | Logout | Actor kết thúc phiên làm việc chủ động | Identity | User, Staff, Admin |
| BP-IAM-005 | Session Validation | Kiểm tra tính hợp lệ của phiên làm việc trong quá trình sử dụng | Identity | User, Staff, Admin |
| BP-IAM-006 | Profile Management | User xem và cập nhật hồ sơ cá nhân | Identity | User |
| BP-IAM-007 | Credential Management | User đổi hoặc khôi phục mật khẩu theo quy trình | Identity | User |
| BP-IAM-008 | Authorization Check | Kiểm tra quyền trước khi actor thực hiện hành động bảo vệ | Identity, Cross-cutting | User, Staff, Admin |
| BP-IAM-009 | Role Governance | Admin quản lý, gán và thu hồi Role | Identity, Administration | Admin |
| BP-IAM-010 | Permission Governance | Admin quản lý, gán và thu hồi Permission | Identity, Administration | Admin |
| BP-IAM-011 | Account Status Governance | Staff hoặc Admin khóa tạm, mở khóa, ngưng hoặc kích hoạt lại tài khoản theo quyền | Identity, Administration | Staff, Admin |
| BP-IAM-012 | Identity Audit Review | Admin xem Audit liên quan đến tài khoản, Role và Permission | Identity, Administration | Admin |

### 2.2 Resource Processes

| Process ID | Process Name | Description | Related Modules | Related Actors |
|---|---|---|---|---|
| BP-RCM-001 | Create Capital Cycle | User tạo chu kỳ nguồn vốn Daily, Weekly hoặc Monthly | Resource Capital | User |
| BP-RCM-002 | Activate Capital Cycle | User chọn chu kỳ hoạt động để lập kế hoạch nguồn lực | Resource Capital | User |
| BP-RCM-003 | Update Capital Cycle | User cập nhật thông tin chu kỳ khi trạng thái cho phép | Resource Capital | User |
| BP-RCM-004 | Close Capital Cycle | User đóng chu kỳ để chốt kỳ nguồn lực | Resource Capital | User |
| BP-RCM-005 | Reopen Capital Cycle | User mở lại chu kỳ nếu chính sách cho phép | Resource Capital | User |
| BP-RCM-006 | Set Capital | User thiết lập Time Capital và Money Capital | Resource Capital | User |
| BP-RCM-007 | Adjust Capital | User điều chỉnh Time Capital hoặc Money Capital có lịch sử | Resource Capital | User |
| BP-RCM-008 | Allocate Capital | User phân bổ nguồn lực cho kế hoạch hợp lệ | Resource Capital, Task | User |
| BP-RCM-009 | Reallocate Capital | User thay đổi phân bổ nguồn lực theo ưu tiên mới | Resource Capital, Task | User |
| BP-RCM-010 | Release Capital | User giải phóng nguồn lực chưa sử dụng | Resource Capital, Task | User |
| BP-RCM-011 | Allow Over Allocation | User chủ động cho phép vượt mức trong tình huống ngoại lệ | Resource Capital | User |
| BP-RCM-012 | Review Capital Balance and History | User xem khả dụng, còn lại, lịch sử và tổng hợp nguồn vốn | Resource Capital | User |

### 2.3 Task and Timeline Processes

| Process ID | Process Name | Description | Related Modules | Related Actors |
|---|---|---|---|---|
| BP-TTM-001 | Create Task | User tạo Task đại diện cho công việc cần thực hiện | Task | User |
| BP-TTM-002 | Update Task | User cập nhật thông tin Task khi trạng thái cho phép | Task | User |
| BP-TTM-003 | Classify Task | User gán Category và Tag để tổ chức Task | Task, Category, Tag | User |
| BP-TTM-004 | Plan Task | User đặt Priority, Deadline, Estimated Time và Estimated Cost | Task, Resource | User |
| BP-TTM-005 | Schedule Task | User đưa Task có Time Capital lên Timeline | Task, Timeline, Resource | User |
| BP-TTM-006 | Move Timeline Item | User thay đổi lịch Task trên Timeline, bao gồm kéo thả | Timeline, Task | User |
| BP-TTM-007 | Search and Filter Task | User tìm kiếm, lọc, sắp xếp và xem chi tiết Task | Task | User |
| BP-TTM-008 | Update Task Progress | User cập nhật Progress từ 0 đến 100 | Task | User |
| BP-TTM-009 | Pause and Resume Task | User tạm dừng hoặc tiếp tục Task theo trạng thái hợp lệ | Task | User |
| BP-TTM-010 | Complete Task | User hoàn thành Task để đủ điều kiện đánh giá | Task, Tracking | User |
| BP-TTM-011 | Cancel Task | User hủy Task không còn thực hiện | Task, Resource | User |
| BP-TTM-012 | Archive and Restore Task | User lưu trữ hoặc khôi phục Task | Task | User |
| BP-TTM-013 | Recurring and Reminder Planning | User thiết lập công việc lặp lại hoặc nhắc việc nếu phạm vi được xác nhận | Task, Cross-cutting | User |

### 2.4 Tracking and Reporting Processes

| Process ID | Process Name | Description | Related Modules | Related Actors |
|---|---|---|---|---|
| BP-TER-001 | Record Actual Time | User ghi nhận thời gian thực tế cho Task đủ điều kiện | Tracking | User |
| BP-TER-002 | Record Actual Cost | User ghi nhận chi phí thực tế cho Task đủ điều kiện | Tracking | User |
| BP-TER-003 | Update Actual | User cập nhật Actual Time hoặc Actual Cost khi chính sách cho phép | Tracking | User |
| BP-TER-004 | Planned vs Actual Review | User so sánh Planned và Actual | Evaluation | User |
| BP-TER-005 | Variance Analysis | Hệ thống nghiệp vụ xác định chênh lệch Actual - Planned | Evaluation | User |
| BP-TER-006 | Resource Efficiency Evaluation | User xem đánh giá hiệu quả sử dụng nguồn lực | Evaluation | User |
| BP-TER-007 | Productivity Statistics | User xem thống kê ngày, tuần, tháng, năm và utilization | Tracking, Dashboard | User |
| BP-TER-008 | Dashboard Review | User xem Dashboard hiệu quả cá nhân | Dashboard | User |
| BP-TER-009 | Report Review | User xem, tìm kiếm và lọc báo cáo | Reporting | User |
| BP-TER-010 | Report Export | User xuất báo cáo nếu được phép | Reporting, Cross-cutting | User |
| BP-TER-011 | Trend and KPI Review | User xem xu hướng, so sánh kỳ và KPI cá nhân | Reporting, Evaluation | User |

### 2.5 Administration and Support Processes

| Process ID | Process Name | Description | Related Modules | Related Actors |
|---|---|---|---|---|
| BP-ADM-001 | User Administration | Staff hoặc Admin xem, tìm kiếm, lọc và hỗ trợ tài khoản User | Administration, Identity | Staff, Admin |
| BP-ADM-002 | User Status Administration | Admin hoặc Staff theo quyền khóa tạm, mở khóa, ngưng hoặc kích hoạt tài khoản | Administration, Identity | Staff, Admin |
| BP-ADM-003 | Staff Administration | Admin quản lý Staff và trách nhiệm hỗ trợ | Administration | Admin |
| BP-ADM-004 | Ticket Creation | Actor tạo Ticket hỗ trợ | Support | User, Staff |
| BP-ADM-005 | Ticket Intake | Staff tiếp nhận Ticket | Support | Staff |
| BP-ADM-006 | Ticket Assignment | Staff hoặc Admin gán Ticket cho người xử lý | Support | Staff, Admin |
| BP-ADM-007 | Ticket Resolution | Staff cập nhật, chuyển cấp, xử lý và đóng Ticket | Support | Staff |
| BP-ADM-008 | Ticket Reopen | Staff hoặc Admin mở lại Ticket khi hợp lệ | Support | Staff, Admin |
| BP-ADM-009 | Activity Log Review | Staff hoặc Admin xem và tra cứu Activity Log theo quyền | Administration | Staff, Admin |
| BP-ADM-010 | Audit Review | Admin xem và tìm Audit | Administration | Admin |
| BP-ADM-011 | Configuration Governance | Admin xem và cập nhật cấu hình nghiệp vụ | Administration | Admin |
| BP-ADM-012 | Operational Dashboard Review | Admin xem Dashboard và Statistics vận hành | Administration | Admin |
| BP-ADM-013 | Role and Permission Administration | Admin quản lý Role, Permission, gán và thu hồi quyền | Administration, Identity | Admin |
| BP-ADM-014 | Announcement Governance | Admin gửi thông báo vận hành nếu phạm vi được xác nhận | Administration, Cross-cutting | Admin |

### 2.6 Cross-cutting Processes

| Process ID | Process Name | Description | Related Modules | Related Actors |
|---|---|---|---|---|
| BP-CBR-001 | Global Rule Enforcement | Áp dụng Global Business Rules cho mọi hành động liên quan | Cross-cutting | Guest, User, Staff, Admin |
| BP-CBR-002 | Business Validation | Kiểm tra dữ liệu bắt buộc, tùy chọn, có điều kiện và dẫn xuất | Cross-cutting | Guest, User, Staff, Admin |
| BP-CBR-003 | Audit Recording | Ghi nhận Audit cho hành động quan trọng | Cross-cutting | User, Staff, Admin |
| BP-CBR-004 | Activity History Recording | Ghi nhận Activity History theo phạm vi hiển thị | Cross-cutting | User, Staff, Admin |
| BP-CBR-005 | Notification Handling | Gửi thông báo theo sự kiện nghiệp vụ nếu phạm vi được xác nhận | Cross-cutting | User, Staff, Admin |
| BP-CBR-006 | Search, Filter and Sorting | Chuẩn hóa tìm kiếm, lọc, sắp xếp và phân trang ở mức nghiệp vụ | Cross-cutting | User, Staff, Admin |
| BP-CBR-007 | Export Governance | Kiểm soát xuất dữ liệu theo quyền và mục đích nghiệp vụ | Cross-cutting | User, Admin |
| BP-CBR-008 | Business Error Handling | Chuẩn hóa phản hồi khi xảy ra lỗi nghiệp vụ | Cross-cutting | Guest, User, Staff, Admin |

## 3. BPMN Reference

### 3.1 Identity BPMN Reference

| Process ID | Start Event | Activities | Decision Points | End Event | Business Outcome |
|---|---|---|---|---|---|
| BP-IAM-001 | Guest truy cập hệ thống | Hiển thị Landing Page; cung cấp Register/Login | Guest chọn Register hoặc Login? | Guest tiếp tục hành trình hoặc rời hệ thống | Guest hiểu giá trị LifeBalance |
| BP-IAM-002 | Guest chọn Register | Nhập thông tin; kiểm tra hợp lệ; tạo tài khoản | Thông tin hợp lệ? tài khoản đã tồn tại? | Tài khoản User được tạo hoặc bị từ chối | Guest trở thành User hợp lệ |
| BP-IAM-003 | Actor chọn Login | Nhập thông tin; kiểm tra tài khoản; kiểm tra trạng thái; kiểm tra quyền | Thông tin đúng? tài khoản hoạt động? | Actor đăng nhập hoặc bị từ chối | Actor vào phạm vi chức năng được phép |
| BP-IAM-004 | Actor chọn Logout | Xác nhận phiên; kết thúc phiên | Phiên còn hợp lệ? | Phiên kết thúc | Truy cập chủ động được đóng |
| BP-IAM-005 | Actor thực hiện hành động trong phiên | Kiểm tra phiên; kiểm tra trạng thái tài khoản | Phiên hợp lệ? tài khoản còn hoạt động? | Tiếp tục hoặc yêu cầu đăng nhập lại | Chỉ phiên hợp lệ được sử dụng |
| BP-IAM-006 | User mở Profile | Tải hồ sơ; hiển thị; nhận cập nhật; kiểm tra hợp lệ | Dữ liệu hợp lệ? trường được phép sửa? | Hồ sơ được hiển thị hoặc cập nhật | Hồ sơ cá nhân chính xác |
| BP-IAM-007 | User cần đổi hoặc khôi phục mật khẩu | Xác minh người dùng; kiểm tra chính sách; đổi hoặc đặt lại | Có đủ điều kiện xác minh? mật khẩu hợp lệ? | Credential được cập nhật hoặc bị từ chối | User duy trì quyền truy cập hợp lệ |
| BP-IAM-008 | Actor yêu cầu hành động bảo vệ | Xác định actor; xác định quyền cần có; kiểm tra Role/Permission | Có Permission phù hợp? | Cho phép hoặc từ chối | Hành động chỉ diễn ra trong phạm vi quyền |
| BP-IAM-009 | Admin quản lý Role | Xem Role; tạo/cập nhật/gán/thu hồi; ghi Audit | Role hợp lệ? tác động có được phép? | Role được quản lý | Quyền theo vai trò được kiểm soát |
| BP-IAM-010 | Admin quản lý Permission | Xem Permission; tạo/cập nhật/gán/thu hồi; ghi Audit | Permission hợp lệ? phạm vi đúng? | Permission được quản lý | Quyền chi tiết được kiểm soát |
| BP-IAM-011 | Staff/Admin quản lý trạng thái tài khoản | Tìm User; xem trạng thái; khóa/mở/ngưng/kích hoạt; ghi Audit | Actor có quyền? trạng thái chuyển hợp lệ? | Trạng thái tài khoản thay đổi | Rủi ro truy cập được kiểm soát |
| BP-IAM-012 | Admin cần tra cứu Audit | Chọn tiêu chí; xem kết quả; phân tích sự kiện | Có quyền xem? tiêu chí hợp lệ? | Audit được hiển thị | Truy vết thay đổi danh tính |

### 3.2 Resource BPMN Reference

| Process ID | Start Event | Activities | Decision Points | End Event | Business Outcome |
|---|---|---|---|---|---|
| BP-RCM-001 | User bắt đầu kỳ kế hoạch | Chọn loại chu kỳ; nhập thông tin; kiểm tra trùng lặp; tạo chu kỳ | Chu kỳ hợp lệ? có trùng không? | Chu kỳ được tạo | Có khung quản lý nguồn lực |
| BP-RCM-002 | User chọn kích hoạt chu kỳ | Xem chu kỳ; chọn Activate; kiểm tra trạng thái | Có chu kỳ hoạt động khác? | Chu kỳ được kích hoạt | Có kỳ nguồn vốn đang dùng |
| BP-RCM-003 | User cập nhật chu kỳ | Chọn chu kỳ; nhập thay đổi; kiểm tra trạng thái | Chu kỳ có cho sửa không? | Chu kỳ được cập nhật | Kỳ kế hoạch phản ánh đúng nhu cầu |
| BP-RCM-004 | User đóng chu kỳ | Xem tổng hợp; kiểm tra điều kiện đóng; xác nhận đóng | Có Task hoặc phân bổ chưa xử lý? | Chu kỳ đóng hoặc bị chặn | Kỳ nguồn lực được chốt |
| BP-RCM-005 | User mở lại chu kỳ | Chọn chu kỳ đã đóng; kiểm tra chính sách; xác nhận | Reopen có được phép? | Chu kỳ mở lại hoặc bị từ chối | Sửa sai có kiểm soát |
| BP-RCM-006 | User thiết lập nguồn vốn | Nhập Time Capital; nhập Money Capital; kiểm tra không âm | Giá trị hợp lệ? | Capital được thiết lập | Có nguồn lực khả dụng |
| BP-RCM-007 | User điều chỉnh nguồn vốn | Chọn loại vốn; nhập thay đổi; kiểm tra tác động; ghi lịch sử | Có làm số dư không hợp lệ? | Capital được điều chỉnh hoặc bị từ chối | Nguồn lực phản ánh thực tế |
| BP-RCM-008 | User phân bổ nguồn lực | Chọn kế hoạch; nhập vốn phân bổ; kiểm tra khả dụng | Có đủ nguồn lực? có cho vượt mức? | Phân bổ được ghi nhận hoặc bị chặn | Task có nguồn lực trước thực hiện |
| BP-RCM-009 | User phân bổ lại | Chọn phân bổ hiện tại; nhập phân bổ mới; kiểm tra tác động | Trạng thái có cho đổi không? | Phân bổ mới được áp dụng | Kế hoạch thích ứng thay đổi |
| BP-RCM-010 | User giải phóng vốn | Chọn phân bổ; xác định phần chưa dùng; xác nhận release | Nguồn lực có thể release? | Vốn quay lại số dư | Tối ưu nguồn lực còn lại |
| BP-RCM-011 | User cho phép vượt mức | Hệ thống cảnh báo; User xác nhận; ghi ngoại lệ | User có chủ động xác nhận? | Vượt mức được ghi nhận | Ngoại lệ minh bạch |
| BP-RCM-012 | User xem số dư và lịch sử | Chọn chu kỳ; xem khả dụng, còn lại, lịch sử; lọc nếu cần | Có dữ liệu? tiêu chí hợp lệ? | Thông tin được hiển thị | User hiểu tình trạng nguồn lực |

### 3.3 Task and Timeline BPMN Reference

| Process ID | Start Event | Activities | Decision Points | End Event | Business Outcome |
|---|---|---|---|---|---|
| BP-TTM-001 | User có công việc cần ghi nhận | Nhập Task; kiểm tra tên và dữ liệu bắt buộc; tạo Task | Thông tin hợp lệ? | Task được tạo | Công việc được ghi nhận |
| BP-TTM-002 | User cần sửa Task | Chọn Task; nhập thay đổi; kiểm tra trạng thái | Task có cho sửa không? | Task được cập nhật hoặc bị từ chối | Thông tin Task chính xác |
| BP-TTM-003 | User phân loại Task | Chọn Category/Tag; kiểm tra hợp lệ; gán hoặc gỡ | Category/Tag hợp lệ? | Task được phân loại | Task dễ tìm kiếm và phân tích |
| BP-TTM-004 | User lập kế hoạch Task | Đặt Priority, Deadline, Estimated Time, Estimated Cost | Deadline hợp lệ? ước lượng hợp lệ? | Task trở thành Planned | Task có cơ sở phân bổ nguồn lực |
| BP-TTM-005 | User đưa Task lên Timeline | Chọn Task có Time Capital; chọn thời gian; kiểm tra hợp lệ | Task có Time Capital? thời gian hợp lệ? | Task xuất hiện trên Timeline | Lịch thực hiện được xác lập |
| BP-TTM-006 | User di chuyển Task trên Timeline | Chọn Task; kéo thả hoặc đổi lịch; kiểm tra chồng lấn và chu kỳ | Vị trí mới hợp lệ? | Timeline được cập nhật | Lịch thích ứng thay đổi |
| BP-TTM-007 | User tra cứu Task | Nhập tiêu chí; tìm, lọc, sắp xếp; xem chi tiết | Tiêu chí hợp lệ? có kết quả? | Danh sách hoặc chi tiết hiển thị | User kiểm soát danh sách công việc |
| BP-TTM-008 | User cập nhật tiến độ | Chọn Task; nhập Progress; kiểm tra 0-100 | Progress hợp lệ? trạng thái cho phép? | Progress được cập nhật | Tiến độ phản ánh thực tế |
| BP-TTM-009 | User pause/resume | Chọn Task; chọn Pause hoặc Resume; kiểm tra trạng thái | Chuyển trạng thái hợp lệ? | Task tạm dừng hoặc tiếp tục | Vòng đời Task phản ánh thực tế |
| BP-TTM-010 | User hoàn thành Task | Chọn Complete; kiểm tra điều kiện; xác nhận | Task có đủ điều kiện? | Task Completed | Task sẵn sàng đánh giá |
| BP-TTM-011 | User hủy Task | Chọn Cancel; kiểm tra điều kiện; xử lý nguồn lực liên quan | Task có thể hủy? | Task Cancelled | Công việc không thực hiện được đóng |
| BP-TTM-012 | User archive/restore | Chọn Task; kiểm tra trạng thái; archive hoặc restore | Trạng thái cho phép? | Task được lưu trữ hoặc khôi phục | Danh sách hoạt động gọn hơn |
| BP-TTM-013 | User lập công việc lặp/nhắc việc | Chọn quy tắc lặp hoặc nhắc; kiểm tra phạm vi | Chức năng thuộc phạm vi? thông tin hợp lệ? | Lịch lặp hoặc nhắc việc được ghi nhận | Hỗ trợ kế hoạch định kỳ |

### 3.4 Tracking, Evaluation and Reporting BPMN Reference

| Process ID | Start Event | Activities | Decision Points | End Event | Business Outcome |
|---|---|---|---|---|---|
| BP-TER-001 | Task đủ điều kiện ghi Actual | Nhập Actual Time; kiểm tra không âm; lưu kết quả | Task đủ điều kiện? giá trị hợp lệ? | Actual Time được ghi nhận | Có dữ liệu thực tế về thời gian |
| BP-TER-002 | Task đủ điều kiện ghi Actual | Nhập Actual Cost; kiểm tra không âm; lưu kết quả | Task đủ điều kiện? giá trị hợp lệ? | Actual Cost được ghi nhận | Có dữ liệu thực tế về chi phí |
| BP-TER-003 | User cần sửa Actual | Chọn Actual; nhập thay đổi; kiểm tra chính sách | Có được sửa không? | Actual được cập nhật hoặc bị từ chối | Dữ liệu đánh giá chính xác hơn |
| BP-TER-004 | User xem so sánh | Lấy Planned; lấy Actual; hiển thị đối chiếu | Có Planned/Actual? | Planned vs Actual được hiển thị | User nhận biết sai lệch |
| BP-TER-005 | User yêu cầu phân tích Variance | Xác định Actual - Planned; phân loại positive/negative | Dữ liệu đủ không? | Variance được hiển thị | Sai lệch được làm rõ |
| BP-TER-006 | User xem Efficiency | Tổng hợp Variance; áp dụng chính sách đánh giá | Ngưỡng đánh giá đã xác nhận? | Efficiency được đánh giá | User hiểu hiệu quả nguồn lực |
| BP-TER-007 | User xem thống kê | Chọn kỳ; tổng hợp chỉ số; hiển thị | Kỳ hợp lệ? có dữ liệu? | Statistics được hiển thị | User theo dõi hiệu suất |
| BP-TER-008 | User xem Dashboard | Chọn bộ lọc; tổng hợp chỉ số chính | Có quyền xem? dữ liệu có sẵn? | Dashboard hiển thị | Tổng quan hiệu quả cá nhân |
| BP-TER-009 | User xem Report | Chọn loại Report; lọc/tìm kiếm; hiển thị | Tiêu chí hợp lệ? | Report được hiển thị | Phân tích có cấu trúc |
| BP-TER-010 | User xuất Report | Chọn Report; kiểm tra quyền; xác nhận xuất | Actor có quyền? phạm vi phù hợp? | Report được xuất | Kết quả có thể lưu giữ hoặc chia sẻ |
| BP-TER-011 | User xem Trend/KPI | Chọn kỳ so sánh; tổng hợp xu hướng; hiển thị KPI | Có đủ dữ liệu nhiều kỳ? | Trend/KPI được hiển thị | Hỗ trợ cải thiện lập kế hoạch |

### 3.5 Administration and Support BPMN Reference

| Process ID | Start Event | Activities | Decision Points | End Event | Business Outcome |
|---|---|---|---|---|---|
| BP-ADM-001 | Staff/Admin cần hỗ trợ User | Tìm User; xem chi tiết; lọc danh sách | Có quyền xem? | User được tìm thấy hoặc không có kết quả | Hỗ trợ đúng tài khoản |
| BP-ADM-002 | Cần thay đổi trạng thái User | Xem trạng thái; kiểm tra quyền; khóa/mở/ngưng/kích hoạt; Audit | Chuyển trạng thái hợp lệ? | Trạng thái thay đổi | Truy cập tài khoản được kiểm soát |
| BP-ADM-003 | Admin quản lý Staff | Xem Staff; gán hoặc gỡ trách nhiệm | Staff hợp lệ? | Trách nhiệm Staff cập nhật | Năng lực hỗ trợ được quản lý |
| BP-ADM-004 | Actor cần hỗ trợ | Tạo Ticket; nhập tiêu đề/nội dung; phân loại | Thông tin hợp lệ? | Ticket được tạo | Nhu cầu hỗ trợ được ghi nhận |
| BP-ADM-005 | Ticket mới phát sinh | Staff nhận Ticket; xác nhận xử lý | Staff có quyền? | Ticket được tiếp nhận | Quy trình hỗ trợ bắt đầu |
| BP-ADM-006 | Ticket cần owner | Gán Ticket; thông báo trách nhiệm | Staff phù hợp? | Ticket có owner | Trách nhiệm xử lý rõ ràng |
| BP-ADM-007 | Staff xử lý Ticket | Cập nhật; chuyển cấp nếu cần; resolve; close | Có đủ thông tin xử lý? có cần escalate? | Ticket xử lý hoặc đóng | Vấn đề người dùng được giải quyết |
| BP-ADM-008 | Ticket cần mở lại | Kiểm tra lý do; reopen | Chính sách cho phép? | Ticket mở lại | Sửa lỗi đóng nhầm |
| BP-ADM-009 | Cần tra cứu Activity Log | Chọn tiêu chí; tìm/lọc; xem kết quả | Actor có quyền? | Activity hiển thị | Sự kiện vận hành được kiểm tra |
| BP-ADM-010 | Cần tra cứu Audit | Admin chọn tiêu chí; xem kết quả | Có quyền Admin? | Audit hiển thị | Governance được theo dõi |
| BP-ADM-011 | Cần thay đổi cấu hình nghiệp vụ | Admin xem cấu hình; nhập thay đổi; kiểm tra; Audit | Cấu hình hợp lệ? | Cấu hình cập nhật | Chính sách vận hành thay đổi có kiểm soát |
| BP-ADM-012 | Admin xem tình trạng vận hành | Chọn dashboard/statistics; xem chỉ số | Có dữ liệu? | Chỉ số vận hành hiển thị | Admin có thông tin quản trị |
| BP-ADM-013 | Admin quản trị Role/Permission | Tạo/cập nhật/gán/thu hồi; kiểm tra tác động; Audit | Có gây mất quyền trọng yếu? | Quyền được cập nhật | Phân quyền được kiểm soát |
| BP-ADM-014 | Cần thông báo vận hành | Admin soạn thông báo; chọn người nhận; xác nhận | Announcement thuộc phạm vi? | Thông báo được gửi | Người dùng nhận thông tin vận hành |

## 4. Activity Model Catalog

### 4.1 Identity Activities

| Activity Name | Purpose | Input | Output | Preconditions | Postconditions |
|---|---|---|---|---|---|
| View Landing Page | Giới thiệu sản phẩm cho Guest | Yêu cầu truy cập | Landing Page hiển thị | Actor là Guest hoặc chưa đăng nhập | Guest có thể chọn Register/Login |
| Register Account | Tạo tài khoản User | Thông tin đăng ký | Tài khoản mới hoặc lỗi nghiệp vụ | Guest chưa đăng nhập | Tài khoản User được tạo nếu hợp lệ |
| Validate Login | Xác thực actor | Thông tin đăng nhập | Kết quả đăng nhập | Tài khoản tồn tại | Actor có phiên hợp lệ hoặc bị từ chối |
| Validate Session | Kiểm tra phiên làm việc | Phiên hiện tại | Kết quả hợp lệ/không hợp lệ | Actor đã đăng nhập | Actor được tiếp tục hoặc yêu cầu xác thực lại |
| Check Permission | Kiểm tra quyền hành động | Actor, hành động, phạm vi | Cho phép hoặc từ chối | Actor đã xác thực | Hành động chỉ tiếp tục nếu có quyền |
| Manage Profile | Duy trì hồ sơ cá nhân | Thông tin hồ sơ | Hồ sơ cập nhật | User đã đăng nhập | Profile phản ánh thông tin hợp lệ |
| Manage Role | Quản trị vai trò | Role, actor, thay đổi | Role được tạo/cập nhật/thu hồi | Admin có quyền | Audit được ghi nhận |
| Manage Permission | Quản trị quyền | Permission, actor, thay đổi | Permission được tạo/cập nhật/thu hồi | Admin có quyền | Audit được ghi nhận |
| Lock Account | Khóa tạm tài khoản | User, lý do khóa | Account bị khóa | Staff/Admin có quyền | User không thể đăng nhập |
| Unlock Account | Mở khóa tài khoản | Account đang khóa | Account hoạt động lại | Admin có quyền | User có thể đăng nhập nếu đủ điều kiện |

### 4.2 Resource Activities

| Activity Name | Purpose | Input | Output | Preconditions | Postconditions |
|---|---|---|---|---|---|
| Create Capital Cycle | Tạo kỳ quản lý nguồn lực | Loại chu kỳ, thông tin kỳ | Chu kỳ mới | User đã đăng nhập | Chu kỳ tồn tại để thiết lập vốn |
| Activate Cycle | Chọn chu kỳ hoạt động | Chu kỳ hợp lệ | Chu kỳ active | Chu kỳ đã tạo | Chu kỳ được dùng để lập kế hoạch |
| Set Time Capital | Thiết lập vốn thời gian | Giá trị Time Capital | Time Capital được ghi nhận | Chu kỳ hợp lệ | Time Capital khả dụng |
| Set Money Capital | Thiết lập vốn tiền bạc | Giá trị Money Capital | Money Capital được ghi nhận | Chu kỳ hợp lệ | Money Capital khả dụng |
| Adjust Capital | Điều chỉnh nguồn vốn | Loại vốn, giá trị, lý do nếu có | Vốn được điều chỉnh | Capital tồn tại | Lịch sử điều chỉnh được ghi nhận |
| Allocate Capital | Cấp nguồn lực | Task/kế hoạch, số vốn | Allocation được ghi nhận | Có nguồn lực khả dụng hoặc cho phép vượt mức | Nguồn lực được cấp |
| Reallocate Capital | Thay đổi phân bổ | Allocation hiện tại, giá trị mới | Allocation mới | Phân bổ đang hợp lệ | Lịch sử phân bổ được giữ |
| Release Capital | Trả nguồn lực chưa dùng | Allocation, phần release | Remaining Capital tăng | Allocation đủ điều kiện | Nguồn lực quay về khả dụng |
| View Capital Summary | Xem tổng hợp nguồn vốn | Chu kỳ, bộ lọc | Summary | Có dữ liệu nguồn vốn | User hiểu tình trạng vốn |

### 4.3 Task and Timeline Activities

| Activity Name | Purpose | Input | Output | Preconditions | Postconditions |
|---|---|---|---|---|---|
| Create Task | Ghi nhận công việc | Task Name và thông tin liên quan | Task mới | User đã đăng nhập | Task thuộc User |
| Update Task | Sửa thông tin công việc | Task và thay đổi | Task cập nhật | Task tồn tại và cho phép sửa | Task phản ánh thông tin mới |
| Assign Category | Phân loại theo nhóm | Task, Category | Task có Category | Task tồn tại | Category được gán |
| Assign Tag | Gắn nhãn linh hoạt | Task, Tag | Task có Tag | Task tồn tại | Tag được gán |
| Set Priority | Xác định mức ưu tiên | Priority | Priority cập nhật | Task tồn tại | Task có Priority hợp lệ |
| Set Deadline | Xác định hạn hoàn thành | Deadline | Deadline cập nhật | Deadline hợp lệ | Task có hạn phù hợp |
| Estimate Resource | Ước lượng Time/Cost | Estimated Time, Estimated Cost | Planned values | Task tồn tại | Task sẵn sàng phân bổ |
| Schedule Task | Đưa Task lên Timeline | Task, khung thời gian | Timeline item | Task có Time Capital | Task xuất hiện trên Timeline |
| Move Task on Timeline | Điều chỉnh lịch | Task, vị trí mới | Timeline cập nhật | Timeline item tồn tại | Lịch thay đổi hợp lệ |
| Update Progress | Theo dõi tiến độ | Progress | Progress cập nhật | Task đang hoạt động | Tiến độ trong khoảng hợp lệ |
| Complete Task | Kết thúc công việc | Task, xác nhận hoàn thành | Task Completed | Task đủ điều kiện | Task sẵn sàng đánh giá |
| Archive Task | Lưu trữ Task | Task | Task Archived | Task đủ điều kiện | Task không còn trong danh sách hoạt động |

### 4.4 Tracking, Reporting and Administration Activities

| Activity Name | Purpose | Input | Output | Preconditions | Postconditions |
|---|---|---|---|---|---|
| Record Actual | Ghi nhận thực tế | Actual Time/Cost | Actual values | Task đủ điều kiện | Dữ liệu thực tế được ghi nhận |
| Calculate Variance | Xác định sai lệch | Planned và Actual | Variance | Có dữ liệu đủ | Sai lệch được xác định |
| Evaluate Efficiency | Đánh giá hiệu quả | Variance và chính sách đánh giá | Efficiency result | Có dữ liệu đánh giá | Hiệu quả được hiển thị |
| View Dashboard | Xem tổng quan | Kỳ, bộ lọc | Dashboard information | Có quyền xem | Chỉ số tổng quan hiển thị |
| Generate Report | Tạo báo cáo nghiệp vụ | Loại Report, kỳ, bộ lọc | Report | Có dữ liệu phù hợp | Báo cáo hiển thị |
| Create Ticket | Ghi nhận hỗ trợ | Tiêu đề, nội dung, phân loại | Ticket | Actor có quyền tạo | Ticket ở trạng thái ban đầu |
| Assign Ticket | Phân công xử lý | Ticket, Staff | Ticket có owner | Ticket tồn tại | Trách nhiệm xử lý rõ |
| Resolve Ticket | Xử lý Ticket | Ticket, kết quả xử lý | Ticket resolved | Ticket đang xử lý | Ticket có kết quả |
| View Audit | Tra cứu Audit | Tiêu chí tìm kiếm | Audit entries | Admin có quyền | Sự kiện quan trọng được xem |
| Update Configuration | Cập nhật chính sách vận hành | Cấu hình mới | Cấu hình cập nhật | Admin có quyền | Audit được ghi nhận |

## 5. Business State Model

### 5.1 Task Lifecycle

| State Name | Description | Entry Condition | Exit Condition | Allowed Transition |
|---|---|---|---|---|
| Draft | Task mới được ghi nhận nhưng chưa lập kế hoạch đầy đủ | User tạo Task với thông tin tối thiểu | User bổ sung planning attributes | Draft -> Planned, Draft -> Cancelled, Draft -> Archived |
| Planned | Task đã có thông tin kế hoạch cơ bản | Task có Priority, Deadline hoặc ước lượng theo chính sách | Task được cấp Time Capital và đưa lên Timeline hoặc bắt đầu thực hiện | Planned -> Scheduled, Planned -> In Progress, Planned -> Cancelled, Planned -> Archived |
| Scheduled | Task đã có lịch trên Timeline | Task có Time Capital và lịch hợp lệ | User bắt đầu thực hiện hoặc đổi lịch | Scheduled -> In Progress, Scheduled -> Planned, Scheduled -> Cancelled |
| In Progress | Task đang được thực hiện | User bắt đầu hoặc resume Task | User pause, complete hoặc cancel | In Progress -> On Hold, In Progress -> Completed, In Progress -> Cancelled |
| On Hold | Task tạm dừng | User pause Task đang thực hiện | User resume hoặc cancel | On Hold -> In Progress, On Hold -> Cancelled |
| Completed | Task đã hoàn thành | User complete Task đủ điều kiện | Reopen nếu chính sách cho phép hoặc archive | Completed -> Reopened, Completed -> Archived |
| Cancelled | Task bị hủy | User cancel Task đủ điều kiện | Reopen nếu chính sách cho phép hoặc archive | Cancelled -> Reopened, Cancelled -> Archived |
| Reopened | Task được mở lại để chỉnh sửa | Reopen được phê duyệt theo chính sách | User cập nhật Task và đưa về trạng thái phù hợp | Reopened -> Planned, Reopened -> In Progress |
| Archived | Task được lưu trữ | User archive Task đủ điều kiện | User restore nếu được phép | Archived -> Planned, Archived -> Completed |

### 5.2 Ticket Lifecycle

| State Name | Description | Entry Condition | Exit Condition | Allowed Transition |
|---|---|---|---|---|
| New | Ticket vừa được tạo | Actor tạo Ticket hợp lệ | Staff tiếp nhận hoặc gán | New -> Received, New -> Assigned |
| Received | Ticket đã được Staff nhận | Staff nhận Ticket | Ticket được gán owner hoặc xử lý | Received -> Assigned, Received -> In Progress |
| Assigned | Ticket có người chịu trách nhiệm | Ticket được gán Staff | Staff bắt đầu xử lý hoặc chuyển cấp | Assigned -> In Progress, Assigned -> Escalated |
| In Progress | Ticket đang xử lý | Staff cập nhật xử lý | Resolve, escalate hoặc close theo chính sách | In Progress -> Resolved, In Progress -> Escalated |
| Escalated | Ticket chuyển cấp | Staff xác định cần cấp hỗ trợ cao hơn | Ticket được xử lý tiếp hoặc gán lại | Escalated -> Assigned, Escalated -> In Progress |
| Resolved | Ticket đã có kết quả xử lý | Staff ghi kết quả | Ticket được đóng hoặc mở lại | Resolved -> Closed, Resolved -> Reopened |
| Closed | Ticket kết thúc | Ticket resolved và được close | Reopen nếu có lý do hợp lệ | Closed -> Reopened |
| Reopened | Ticket mở lại | Ticket đã đóng hoặc resolved nhưng cần xử lý tiếp | Ticket được gán hoặc xử lý lại | Reopened -> Assigned, Reopened -> In Progress |

### 5.3 Capital Cycle Lifecycle

| State Name | Description | Entry Condition | Exit Condition | Allowed Transition |
|---|---|---|---|---|
| Draft | Chu kỳ mới tạo, chưa hoạt động | User tạo chu kỳ | User kích hoạt hoặc cập nhật | Draft -> Active, Draft -> Cancelled |
| Active | Chu kỳ đang được dùng để lập kế hoạch | User activate chu kỳ | User đóng chu kỳ hoặc chuyển trạng thái theo chính sách | Active -> Closed, Active -> Suspended |
| Suspended | Chu kỳ tạm ngưng sử dụng | Chính sách cho phép tạm ngưng | User kích hoạt lại hoặc đóng | Suspended -> Active, Suspended -> Closed |
| Closed | Chu kỳ đã chốt | User đóng chu kỳ đủ điều kiện | Reopen nếu chính sách cho phép | Closed -> Reopened |
| Reopened | Chu kỳ đã đóng được mở lại | Reopen được phép | User cập nhật và kích hoạt/đóng lại | Reopened -> Active, Reopened -> Closed |
| Cancelled | Chu kỳ bị hủy trước khi sử dụng | User hủy chu kỳ theo chính sách | Không chuyển tiếp trừ khi chính sách cho phép | Cancelled -> Draft nếu được xác nhận |

### 5.4 Account Lifecycle

| State Name | Description | Entry Condition | Exit Condition | Allowed Transition |
|---|---|---|---|---|
| Registered | Tài khoản vừa được tạo | Guest đăng ký thành công | Account được kích hoạt hoặc chờ xác minh nếu có | Registered -> Active, Registered -> Pending Verification |
| Pending Verification | Tài khoản chờ xác minh nếu chính sách áp dụng | Đăng ký cần xác minh bổ sung | Xác minh thành công hoặc thất bại | Pending Verification -> Active, Pending Verification -> Deactivated |
| Active | Tài khoản hoạt động | Tài khoản đủ điều kiện | Bị khóa tạm, ngưng hoặc deactivate | Active -> Temporarily Locked, Active -> Deactivated |
| Temporarily Locked | Tài khoản bị khóa tạm | Staff/Admin khóa tạm | Admin mở khóa hoặc deactivate | Temporarily Locked -> Active, Temporarily Locked -> Deactivated |
| Deactivated | Tài khoản ngưng hoạt động | Admin deactivate | Admin reactivate nếu đủ điều kiện | Deactivated -> Active |

### 5.5 Capital Allocation State

| State Name | Description | Entry Condition | Exit Condition | Allowed Transition |
|---|---|---|---|---|
| Planned | Phân bổ dự kiến | User tạo allocation hợp lệ | Allocation được sử dụng, thay đổi hoặc giải phóng | Planned -> Allocated, Planned -> Released |
| Allocated | Nguồn lực đã cấp cho kế hoạch | Allocation được xác nhận | Reallocate, release hoặc actual consumption | Allocated -> Reallocated, Allocated -> Released, Allocated -> Consumed |
| Reallocated | Nguồn lực được phân bổ lại | User thay đổi allocation | Allocation mới có hiệu lực | Reallocated -> Allocated, Reallocated -> Released |
| Released | Nguồn lực được trả về | User release phần chưa sử dụng | Không chuyển tiếp trừ khi tạo phân bổ mới | Released -> Allocated nếu phân bổ mới |
| Consumed | Nguồn lực được sử dụng thực tế | Task được đánh giá Actual | Evaluation hoàn tất | Consumed -> Evaluated |
| Evaluated | Nguồn lực đã được đánh giá hiệu quả | Planned và Actual được đối chiếu | Không chuyển tiếp trừ khi Reopen theo chính sách | Evaluated -> Reopened nếu được phép |

## 6. Decision Tables

### 6.1 Login Decision Table

| Rule | Credentials Valid | Account Status | Session Policy Valid | Action | Outcome |
|---|---|---|---|---|---|
| L-01 | Yes | Active | Yes | Allow login | Actor truy cập phạm vi được phép |
| L-02 | No | Any | Any | Reject login | Thông báo thông tin đăng nhập không hợp lệ |
| L-03 | Yes | Temporarily Locked | Any | Reject login | Thông báo tài khoản bị khóa |
| L-04 | Yes | Deactivated | Any | Reject login | Thông báo tài khoản không hoạt động |
| L-05 | Yes | Pending Verification | Any | Reject or request verification | Actor cần hoàn tất xác minh nếu chính sách áp dụng |
| L-06 | Yes | Active | No | Reject or require retry | Không tạo phiên hợp lệ |

### 6.2 Register Decision Table

| Rule | Required Data Complete | Data Valid | Account Duplicate | Policy Accepted | Action | Outcome |
|---|---|---|---|---|---|---|
| R-01 | Yes | Yes | No | Yes | Create account | User account được tạo |
| R-02 | No | Any | Any | Any | Reject | Thiếu dữ liệu bắt buộc |
| R-03 | Yes | No | Any | Any | Reject | Dữ liệu không hợp lệ |
| R-04 | Yes | Yes | Yes | Any | Reject | Tài khoản đã tồn tại |
| R-05 | Yes | Yes | No | No | Reject | Chưa chấp nhận chính sách nếu chính sách áp dụng |

### 6.3 Permission Check Decision Table

| Rule | Actor Authenticated | Account Active | Required Permission Present | Resource Ownership Valid | Action | Outcome |
|---|---|---|---|---|---|---|
| P-01 | Yes | Yes | Yes | Yes | Allow action | Hành động được thực hiện |
| P-02 | No | Any | Any | Any | Reject | Yêu cầu đăng nhập |
| P-03 | Yes | No | Any | Any | Reject | Tài khoản không hợp lệ |
| P-04 | Yes | Yes | No | Any | Reject | Permission denied |
| P-05 | Yes | Yes | Yes | No | Reject | Không được thao tác trên dữ liệu ngoài phạm vi |

### 6.4 Capital Allocation Decision Table

| Rule | Cycle Active | Capital Available | Requested Allocation Valid | Over Allocation Allowed | Action | Outcome |
|---|---|---|---|---|---|---|
| CA-01 | Yes | Yes | Yes | Not needed | Allocate | Allocation thành công |
| CA-02 | No | Any | Any | Any | Reject | Không có chu kỳ active |
| CA-03 | Yes | No | Yes | No | Reject | Không đủ nguồn lực |
| CA-04 | Yes | No | Yes | Yes | Allocate with warning | Vượt mức được ghi nhận |
| CA-05 | Yes | Any | No | Any | Reject | Giá trị phân bổ không hợp lệ |

### 6.5 Task Completion Decision Table

| Rule | Task Exists | User Owns Task | Status Allows Completion | Required Planning Complete | Action | Outcome |
|---|---|---|---|---|---|---|
| TC-01 | Yes | Yes | Yes | Yes | Complete Task | Task chuyển Completed |
| TC-02 | No | Any | Any | Any | Reject | Task không tồn tại trong phạm vi |
| TC-03 | Yes | No | Any | Any | Reject | Không có quyền với Task |
| TC-04 | Yes | Yes | No | Any | Reject | Trạng thái không cho hoàn thành |
| TC-05 | Yes | Yes | Yes | No | Reject or require completion | Cần hoàn thiện planning theo chính sách |

### 6.6 Resource Evaluation Decision Table

| Rule | Task Completed | Planned Available | Actual Available | Values Valid | Action | Outcome |
|---|---|---|---|---|---|---|
| RE-01 | Yes | Yes | Yes | Yes | Evaluate | Variance và Efficiency được xác định |
| RE-02 | No | Any | Any | Any | Reject final evaluation | Task chưa đủ điều kiện |
| RE-03 | Yes | No | Yes | Yes | Partial evaluation | Đánh giá giới hạn theo dữ liệu có sẵn |
| RE-04 | Yes | Yes | No | Any | Request Actual | Chưa đủ dữ liệu thực tế |
| RE-05 | Yes | Yes | Yes | No | Reject | Actual hoặc Planned không hợp lệ |

### 6.7 Ticket Resolution Decision Table

| Rule | Ticket Assigned | Status Allows Resolution | Resolution Provided | Actor Authorized | Action | Outcome |
|---|---|---|---|---|---|---|
| TR-01 | Yes | Yes | Yes | Yes | Resolve Ticket | Ticket chuyển Resolved |
| TR-02 | No | Any | Any | Any | Reject | Ticket chưa có owner |
| TR-03 | Yes | No | Any | Any | Reject | Trạng thái không cho resolve |
| TR-04 | Yes | Yes | No | Yes | Reject | Thiếu kết quả xử lý |
| TR-05 | Yes | Yes | Yes | No | Reject | Actor không có quyền |

### 6.8 Account Lock Decision Table

| Rule | Actor Authorized | Target Account Valid | Target Is Self | Lock Reason Provided | Action | Outcome |
|---|---|---|---|---|---|---|
| AL-01 | Yes | Yes | No | Yes | Temporary lock | Account bị khóa tạm |
| AL-02 | No | Any | Any | Any | Reject | Actor không có quyền |
| AL-03 | Yes | No | Any | Any | Reject | Account mục tiêu không hợp lệ |
| AL-04 | Yes | Yes | Yes | Any | Reject or require policy approval | Ngăn tự khóa nếu chính sách chưa cho phép |
| AL-05 | Yes | Yes | No | No | Reject | Thiếu lý do khóa |

## 7. CRUD Matrix (Business Level)

Ma trận này mô tả quyền nghiệp vụ ở mức Create, Read, Update, Delete/Deactivate/Archive và Manage. Đây là ma trận hành vi nghiệp vụ, không phải thiết kế lưu trữ dữ liệu.

| Business Object | Guest | User | Staff | Admin |
|---|---|---|---|---|
| User Account | C: Register | R/U: Own account; limited account actions | R: Support scope; U: temporary lock by policy | R/U/M: Manage users; deactivate/reactivate/unlock |
| Profile | None | R/U: Own profile | R: limited support view if allowed | R/U: by administration policy |
| Resource Capital | None | C/R/U/M: Own capital cycles, allocation, adjustment | R: only if explicitly allowed for support | R: oversight if policy allows; no personal plan change unless defined |
| Task | None | C/R/U/D/A/M: Own tasks by lifecycle | None by default | R: governance view only if defined |
| Timeline | None | R/U/M: Own scheduled tasks | None by default | R: governance view only if defined |
| Category | None | C/R/U/D/M: Own categories if customizable | None by default | M: global categories only if defined |
| Tag | None | C/R/U/D/M: Own tags if customizable | None by default | M: global tags only if defined |
| Report | None | R/Export: Own reports by permission | R: support scope if allowed | R/Export/M: administration reports |
| Ticket | None unless public support is defined | C/R: Own tickets | C/R/U/M: receive, assign, resolve, close by policy | R/U/M: monitor, assign, reopen, govern |
| Configuration | None | None | R: only if allowed | R/U/M: configuration governance |
| Role | None | None | None | C/R/U/M: manage Role |
| Permission | None | None | None | C/R/U/M: manage Permission |
| Audit | None | None | R: limited logs only if allowed | R/Monitor: audit review |
| Activity History | None | R: own activity | R: support scope | R/Monitor: operational scope |

Legend: C = Create, R = Read/View, U = Update, D = Delete where allowed, A = Archive where applicable, M = Manage.

## 8. Business Rule Matrix

### 8.1 Global Business Rules

| Business Rule ID | Description | Applies To | Related Use Case | Related Functional Requirement | Priority |
|---|---|---|---|---|---|
| GBR-001 | Một actor chỉ được thao tác trong phạm vi quyền của mình | All Modules | CBR-UC-001, IAM-UC-011 | GBR-001, IAM-FR-010 | Critical |
| GBR-002 | Một User chỉ quản lý dữ liệu cá nhân thuộc quyền của mình | Identity, Resource, Task, Tracking | IAM-UC-011, RCM-UC-015, TTM-UC-001, TER-UC-016 | GBR-002 | Critical |
| GBR-003 | Hành động quan trọng phải có khả năng truy vết | All Modules | CBR-UC-003 | GBR-003 | Critical |
| GBR-004 | Thay đổi Role và Permission phải được Audit | Identity, Administration | IAM-UC-012 đến IAM-UC-019, ADM-UC-029 đến ADM-UC-032 | IAM-FR-030 đến IAM-FR-034, ADM-FR-041 đến ADM-FR-059 | Critical |
| GBR-005 | Chỉ Admin được thay đổi cấu hình nghiệp vụ | Administration | ADM-UC-025, ADM-UC-026 | ADM-FR-031, ADM-FR-032 | Critical |
| GBR-006 | Dữ liệu nghiệp vụ phải được kiểm tra hợp lệ trước khi ghi nhận | All Modules | CBR-UC-002 | VAL-001 đến VAL-028 | Critical |
| GBR-007 | Lỗi nghiệp vụ phải được phản hồi rõ ràng cho actor | All Modules | CBR-UC-008 | GBR-007 | High |
| GBR-008 | Search, Filter và Sorting phải tuân thủ chuẩn dùng chung | All Modules with lists | CBR-UC-006 | GBR-008 | High |
| GBR-009 | Export chỉ được thực hiện khi actor có quyền và mục đích hợp lệ | Reporting, Administration | CBR-UC-007, TER-UC-018 | TER-FR-018 | Medium |
| GBR-010 | Notification chỉ áp dụng cho sự kiện đã được xác nhận thuộc phạm vi | Cross-cutting | CBR-UC-005 | GBR-010 | Medium |

### 8.2 Module Business Rules

| Business Rule ID | Description | Applies To | Related Use Case | Related Functional Requirement | Priority |
|---|---|---|---|---|---|
| IAM-BR-001 | Guest chỉ được xem Landing Page, Register và Login | Identity | IAM-UC-001, IAM-UC-002, IAM-UC-003 | IAM-FR-001 đến IAM-FR-003 | Critical |
| IAM-BR-002 | Account bị khóa không thể đăng nhập | Identity | IAM-UC-003, IAM-UC-023 | IAM-FR-003, IAM-FR-038 | Critical |
| IAM-BR-003 | Staff không được thay đổi Role hoặc Permission | Identity, Administration | IAM-UC-012 đến IAM-UC-019 | IAM-FR-013 đến IAM-FR-024 | Critical |
| IAM-BR-004 | Admin có toàn quyền quản trị Role và Permission theo chính sách | Identity, Administration | IAM-UC-012 đến IAM-UC-019, ADM-UC-029 đến ADM-UC-032 | IAM-FR-013 đến IAM-FR-024, ADM-FR-041 đến ADM-FR-059 | Critical |
| IAM-BR-005 | Mọi thay đổi tài khoản quan trọng phải được Audit | Identity | IAM-UC-022 đến IAM-UC-027 | IAM-FR-035 đến IAM-FR-045 | High |
| RCM-BR-001 | Một chu kỳ chỉ có một trạng thái hoạt động trong phạm vi xác định | Resource | RCM-UC-003 | RCM-FR-003 | Critical |
| RCM-BR-002 | Không được phân bổ vượt nguồn lực nếu chưa chủ động cho phép | Resource | RCM-UC-010, RCM-UC-013 | RCM-FR-010, RCM-FR-014 | Critical |
| RCM-BR-003 | Điều chỉnh nguồn lực không được làm mất lịch sử | Resource | RCM-UC-008, RCM-UC-009, RCM-UC-017 | RCM-FR-008, RCM-FR-009, RCM-FR-017 | Critical |
| RCM-BR-004 | Chỉ người sở hữu mới được quản lý nguồn lực cá nhân | Resource | RCM-UC-001 đến RCM-UC-020 | RCM-FR-001 đến RCM-FR-050 | Critical |
| RCM-BR-005 | Giá trị nguồn vốn phải không âm | Resource | RCM-UC-006, RCM-UC-007 | RCM-FR-006, RCM-FR-007 | Critical |
| TTM-BR-001 | Task phải thuộc đúng một User | Task | TTM-UC-001 | TTM-FR-001 | Critical |
| TTM-BR-002 | Task có Time Capital mới hiển thị trên Timeline | Timeline | TTM-UC-014, TTM-UC-017 | TTM-FR-014, TTM-FR-017 | Critical |
| TTM-BR-003 | Progress phải trong khoảng 0 đến 100 | Task | TTM-UC-021 | TTM-FR-021 | Critical |
| TTM-BR-004 | Completed Task không được thay đổi Planning nếu không có quy trình Reopen | Task | TTM-UC-024, TTM-UC-026 | TTM-FR-024, TTM-FR-026 | High |
| TTM-BR-005 | Deadline không được nhỏ hơn ngày bắt đầu nếu ngày bắt đầu được xác định | Task | TTM-UC-011 | TTM-FR-011 | High |
| TER-BR-001 | Chỉ Task đủ điều kiện mới được ghi nhận Actual cuối cùng | Tracking | TER-UC-001, TER-UC-003 | TER-FR-001, TER-FR-003 | Critical |
| TER-BR-002 | Variance được hiểu là Actual trừ Planned ở mức nghiệp vụ | Evaluation | TER-UC-006, TER-UC-007 | TER-FR-006, TER-FR-007 | Critical |
| TER-BR-003 | Dashboard chỉ hiển thị dữ liệu thuộc phạm vi quyền của actor | Dashboard | TER-UC-016 | TER-FR-016 | Critical |
| TER-BR-004 | Report phản ánh dữ liệu tại thời điểm truy xuất | Reporting | TER-UC-017 | TER-FR-017 | High |
| TER-BR-005 | Actual Time và Actual Cost phải không âm | Tracking | TER-UC-001 đến TER-UC-004 | TER-FR-001 đến TER-FR-004 | Critical |
| ADM-BR-001 | Staff không được thay đổi Role hoặc Permission | Administration | ADM-UC-029 đến ADM-UC-032 | ADM-FR-041 đến ADM-FR-059 | Critical |
| ADM-BR-002 | Ticket phải có trạng thái hợp lệ | Support | ADM-UC-011 đến ADM-UC-020 | ADM-FR-013 đến ADM-FR-024 | Critical |
| ADM-BR-003 | Ticket đã đóng chỉ được mở lại theo chính sách | Support | ADM-UC-018 | ADM-FR-021 | High |
| ADM-BR-004 | Mọi thao tác quản trị quan trọng phải được Audit | Administration | ADM-UC-023, ADM-UC-026, ADM-UC-029 đến ADM-UC-032 | ADM-FR-029, ADM-FR-032, ADM-FR-041 đến ADM-FR-059 | Critical |
| ADM-BR-005 | Chỉ Admin được thay đổi cấu hình nghiệp vụ | Administration | ADM-UC-025, ADM-UC-026 | ADM-FR-031, ADM-FR-032 | Critical |

## 9. Requirement Traceability Matrix (RTM)

### 9.1 RTM theo Business Goal

| Business Goal | Business Requirement | Functional Requirement | Use Case | User Story | Acceptance Criteria | Module |
|---|---|---|---|---|---|---|
| Đảm bảo truy cập hợp lệ | Guest có thể xem Landing Page, Register và Login | IAM-FR-001 đến IAM-FR-003 | IAM-UC-001 đến IAM-UC-003 | US-IAM-001 đến US-IAM-003 | AC US-IAM-001 đến US-IAM-003 | Identity |
| Đảm bảo truy cập hợp lệ | Actor có thể Logout và phiên được kiểm soát | IAM-FR-004, IAM-FR-008, IAM-FR-025 | IAM-UC-004, IAM-UC-005 | US-IAM-004, US-IAM-005 | AC US-IAM-004, US-IAM-005 | Identity |
| Quản lý danh tính cá nhân | User xem, cập nhật hồ sơ, đổi và khôi phục mật khẩu | IAM-FR-005 đến IAM-FR-007 | IAM-UC-006 đến IAM-UC-009 | US-IAM-006 đến US-IAM-009 | AC US-IAM-006 đến US-IAM-009 | Identity |
| Kiểm soát quyền | Trạng thái tài khoản, Permission và Role được kiểm tra | IAM-FR-009 đến IAM-FR-034 | IAM-UC-010 đến IAM-UC-019, IAM-UC-028 | US-IAM-010 đến US-IAM-019, US-IAM-028 | AC US-IAM-010 đến US-IAM-019, US-IAM-028 | Identity |
| Quản trị tài khoản | User được tìm, xem, cập nhật, khóa, mở, ngưng và kích hoạt | IAM-FR-035 đến IAM-FR-045 | IAM-UC-020 đến IAM-UC-027 | US-IAM-020 đến US-IAM-027 | AC US-IAM-020 đến US-IAM-027 | Identity |
| Quản lý nguồn lực hữu hạn | User tạo, cập nhật, kích hoạt, đóng và mở lại chu kỳ nguồn vốn | RCM-FR-001 đến RCM-FR-005 | RCM-UC-001 đến RCM-UC-005 | US-RCM-001 đến US-RCM-005 | AC US-RCM-001 đến US-RCM-005 | Resource |
| Quản lý nguồn lực hữu hạn | User thiết lập và điều chỉnh Time/Money Capital | RCM-FR-006 đến RCM-FR-009 | RCM-UC-006 đến RCM-UC-009 | US-RCM-006 đến US-RCM-009 | AC US-RCM-006 đến US-RCM-009 | Resource |
| Phân bổ nguồn lực | User phân bổ, phân bổ lại, giải phóng và cho phép vượt mức | RCM-FR-010 đến RCM-FR-014 | RCM-UC-010 đến RCM-UC-014 | US-RCM-010 đến US-RCM-013 | AC US-RCM-010 đến US-RCM-013 | Resource |
| Theo dõi nguồn lực | User xem available, remaining, history, search, filter và summary | RCM-FR-015 đến RCM-FR-050 | RCM-UC-015 đến RCM-UC-027 | US-RCM-014 đến US-RCM-020 | AC US-RCM-014 đến US-RCM-020 | Resource |
| Lập kế hoạch công việc | User tạo, cập nhật, xóa, lưu trữ, khôi phục và nhân bản Task | TTM-FR-001 đến TTM-FR-006 | TTM-UC-001 đến TTM-UC-006 | US-TTM-001 đến US-TTM-006 | AC US-TTM-001 đến US-TTM-006 | Task |
| Lập kế hoạch công việc | User gán Category/Tag, Priority, Deadline, Estimated Time/Cost | TTM-FR-007 đến TTM-FR-013 | TTM-UC-007 đến TTM-UC-013 | US-TTM-007 đến US-TTM-013 | AC US-TTM-007 đến US-TTM-013 | Task |
| Lập lịch thực hiện | User schedule, reschedule, move và view Timeline | TTM-FR-014 đến TTM-FR-017 | TTM-UC-014 đến TTM-UC-017 | US-TTM-014 đến US-TTM-017 | AC US-TTM-014 đến US-TTM-017 | Timeline |
| Tra cứu Task | User search, filter, sort và view Task detail | TTM-FR-018 đến TTM-FR-020 | TTM-UC-018 đến TTM-UC-020 | US-TTM-018 đến US-TTM-020 | AC US-TTM-018 đến US-TTM-020 | Task |
| Thực hiện Task | User update progress, pause, resume, complete, cancel, reopen | TTM-FR-021 đến TTM-FR-026 | TTM-UC-021 đến TTM-UC-026 | US-TTM-021 đến US-TTM-026 | AC US-TTM-021 đến US-TTM-026 | Task |
| Hỗ trợ kế hoạch định kỳ | User dùng Recurring Task và Reminder nếu thuộc phạm vi | TTM-FR-027 đến TTM-FR-060 | TTM-UC-027 đến TTM-UC-031 | US-TTM-027 đến US-TTM-028 | AC US-TTM-027 đến US-TTM-028 | Task |
| Ghi nhận thực tế | User record/update Actual Time và Actual Cost | TER-FR-001 đến TER-FR-004 | TER-UC-001 đến TER-UC-004 | US-TER-001 đến US-TER-004 | AC US-TER-001 đến US-TER-004 | Tracking |
| Đánh giá hiệu quả | User xem Planned vs Actual, Variance, Efficiency và Completion Rate | TER-FR-005 đến TER-FR-009 | TER-UC-005 đến TER-UC-009 | US-TER-005 đến US-TER-009 | AC US-TER-005 đến US-TER-009 | Evaluation |
| Phân tích hiệu suất | User xem Summary, Statistics, Utilization và Dashboard | TER-FR-010 đến TER-FR-016 | TER-UC-010 đến TER-UC-016 | US-TER-010 đến US-TER-016 | AC US-TER-010 đến US-TER-016 | Tracking, Dashboard |
| Báo cáo | User xem, xuất, tìm và lọc Report | TER-FR-017 đến TER-FR-020 | TER-UC-017 đến TER-UC-020 | US-TER-017 đến US-TER-020 | AC US-TER-017 đến US-TER-020 | Reporting |
| Cải thiện liên tục | User xem History, Compare, Trend, Category/Tag/Timeline Statistics và Personal KPI | TER-FR-021 đến TER-FR-060 | TER-UC-021 đến TER-UC-025 | US-TER-021 đến US-TER-025 | AC US-TER-021 đến US-TER-025 | Evaluation, Reporting |
| Quản trị vận hành | Staff/Admin quản lý User, Staff và trạng thái tài khoản | ADM-FR-001 đến ADM-FR-012 | ADM-UC-001 đến ADM-UC-010 | US-ADM-001 đến US-ADM-010 | AC US-ADM-001 đến US-ADM-010 | Administration |
| Hỗ trợ người dùng | Staff/Admin quản lý Ticket từ tạo đến đóng/mở lại | ADM-FR-013 đến ADM-FR-024 | ADM-UC-011 đến ADM-UC-020 | US-ADM-011 đến US-ADM-020 | AC US-ADM-011 đến US-ADM-020 | Support |
| Giám sát vận hành | Staff/Admin xem Activity Log, Audit, Configuration, Dashboard và Statistics | ADM-FR-025 đến ADM-FR-040 | ADM-UC-021 đến ADM-UC-028 | US-ADM-021 đến US-ADM-028 | AC US-ADM-021 đến US-ADM-028 | Administration |
| Quản trị quyền | Admin quản lý Role, Permission, gán và thu hồi | ADM-FR-041 đến ADM-FR-059 | ADM-UC-029 đến ADM-UC-032 | US-ADM-029 đến US-ADM-032 | AC US-ADM-029 đến US-ADM-032 | Administration |
| Truyền thông vận hành | Admin broadcast announcement nếu thuộc phạm vi | ADM-FR-060 | ADM-UC-033 | US-ADM-033 | AC US-ADM-033 | Administration |
| Chuẩn dùng chung | Áp dụng global rule, validation, audit, history, notification, search, export và error handling | GBR-001 đến GBR-030, VAL-001 đến VAL-028 | CBR-UC-001 đến CBR-UC-008 | US-CBR-001 đến US-CBR-008 | AC US-CBR-001 đến US-CBR-008 | Cross-cutting |

## 10. Requirement Coverage Matrix

### 10.1 Coverage Summary

| Module | Functional Requirement Range | Use Case Coverage | Story Coverage | Acceptance Criteria Coverage | Status |
|---|---|---|---|---|---|
| Identity & Authorization | IAM-FR-001 đến IAM-FR-045 | IAM-UC-001 đến IAM-UC-028 | US-IAM-001 đến US-IAM-028 | AC US-IAM-001 đến US-IAM-028 | Covered |
| Resource Capital Management | RCM-FR-001 đến RCM-FR-050 | RCM-UC-001 đến RCM-UC-027 | US-RCM-001 đến US-RCM-020 | AC US-RCM-001 đến US-RCM-020 | Covered |
| Task & Timeline Management | TTM-FR-001 đến TTM-FR-060 | TTM-UC-001 đến TTM-UC-031 | US-TTM-001 đến US-TTM-028 | AC US-TTM-001 đến US-TTM-028 | Covered |
| Tracking, Evaluation & Reporting | TER-FR-001 đến TER-FR-060 | TER-UC-001 đến TER-UC-025 | US-TER-001 đến US-TER-025 | AC US-TER-001 đến US-TER-025 | Covered |
| Administration & Support | ADM-FR-001 đến ADM-FR-060 | ADM-UC-001 đến ADM-UC-033 | US-ADM-001 đến US-ADM-033 | AC US-ADM-001 đến US-ADM-033 | Covered |
| Cross-cutting Requirements | GBR-001 đến GBR-030, VAL-001 đến VAL-028 | CBR-UC-001 đến CBR-UC-008 | US-CBR-001 đến US-CBR-008 | AC US-CBR-001 đến US-CBR-008 | Covered |

### 10.2 Coverage Detail by Requirement Group

| Functional Requirement Group | Use Case | Acceptance Criteria | Story | Module | Coverage |
|---|---|---|---|---|---|
| IAM-FR-001 đến IAM-FR-003 | IAM-UC-001 đến IAM-UC-003 | US-IAM-001 đến US-IAM-003 | US-IAM-001 đến US-IAM-003 | Identity | Covered |
| IAM-FR-004 đến IAM-FR-012 | IAM-UC-004 đến IAM-UC-011, IAM-UC-028 | US-IAM-004 đến US-IAM-011, US-IAM-028 | US-IAM-004 đến US-IAM-011, US-IAM-028 | Identity | Covered |
| IAM-FR-013 đến IAM-FR-034 | IAM-UC-012 đến IAM-UC-019 | US-IAM-012 đến US-IAM-019 | US-IAM-012 đến US-IAM-019 | Identity | Covered |
| IAM-FR-035 đến IAM-FR-045 | IAM-UC-020 đến IAM-UC-027 | US-IAM-020 đến US-IAM-027 | US-IAM-020 đến US-IAM-027 | Identity | Covered |
| RCM-FR-001 đến RCM-FR-009 | RCM-UC-001 đến RCM-UC-009 | US-RCM-001 đến US-RCM-009 | US-RCM-001 đến US-RCM-009 | Resource | Covered |
| RCM-FR-010 đến RCM-FR-014 | RCM-UC-010 đến RCM-UC-014 | US-RCM-010 đến US-RCM-013 | US-RCM-010 đến US-RCM-013 | Resource | Covered |
| RCM-FR-015 đến RCM-FR-050 | RCM-UC-015 đến RCM-UC-027 | US-RCM-014 đến US-RCM-020 | US-RCM-014 đến US-RCM-020 | Resource | Covered |
| TTM-FR-001 đến TTM-FR-013 | TTM-UC-001 đến TTM-UC-013 | US-TTM-001 đến US-TTM-013 | US-TTM-001 đến US-TTM-013 | Task | Covered |
| TTM-FR-014 đến TTM-FR-020 | TTM-UC-014 đến TTM-UC-020 | US-TTM-014 đến US-TTM-020 | US-TTM-014 đến US-TTM-020 | Timeline, Task | Covered |
| TTM-FR-021 đến TTM-FR-060 | TTM-UC-021 đến TTM-UC-031 | US-TTM-021 đến US-TTM-028 | US-TTM-021 đến US-TTM-028 | Task | Covered |
| TER-FR-001 đến TER-FR-009 | TER-UC-001 đến TER-UC-009 | US-TER-001 đến US-TER-009 | US-TER-001 đến US-TER-009 | Tracking, Evaluation | Covered |
| TER-FR-010 đến TER-FR-020 | TER-UC-010 đến TER-UC-020 | US-TER-010 đến US-TER-020 | US-TER-010 đến US-TER-020 | Dashboard, Reporting | Covered |
| TER-FR-021 đến TER-FR-060 | TER-UC-021 đến TER-UC-025 | US-TER-021 đến US-TER-025 | US-TER-021 đến US-TER-025 | Reporting, Evaluation | Covered |
| ADM-FR-001 đến ADM-FR-012 | ADM-UC-001 đến ADM-UC-010 | US-ADM-001 đến US-ADM-010 | US-ADM-001 đến US-ADM-010 | Administration | Covered |
| ADM-FR-013 đến ADM-FR-024 | ADM-UC-011 đến ADM-UC-020 | US-ADM-011 đến US-ADM-020 | US-ADM-011 đến US-ADM-020 | Support | Covered |
| ADM-FR-025 đến ADM-FR-040 | ADM-UC-021 đến ADM-UC-028 | US-ADM-021 đến US-ADM-028 | US-ADM-021 đến US-ADM-028 | Administration | Covered |
| ADM-FR-041 đến ADM-FR-060 | ADM-UC-029 đến ADM-UC-033 | US-ADM-029 đến US-ADM-033 | US-ADM-029 đến US-ADM-033 | Administration | Covered |
| GBR-001 đến GBR-030, VAL-001 đến VAL-028 | CBR-UC-001 đến CBR-UC-008 | US-CBR-001 đến US-CBR-008 | US-CBR-001 đến US-CBR-008 | Cross-cutting | Covered |

## 11. Actor–Feature Matrix

| Feature | Guest | User | Staff | Admin |
|---|---|---|---|---|
| Landing Page Access | View | View | View | View |
| Register | Create account | None | None | None |
| Login/Logout | Login if account exists | Execute | Execute | Execute |
| Profile Management | None | Manage own profile | Limited view if allowed | Manage by policy |
| Password Management | None | Manage own credential | None | Assist by policy if defined |
| Session Management | None | Own session | Own session | Own session |
| Permission Check | Subject to check | Subject to check | Subject to check | Subject to check |
| Role Management | None | None | None | Manage |
| Permission Management | None | None | None | Manage |
| User Search and Detail | None | Own information | View support scope | Manage |
| Temporary Account Lock | None | None | Execute by policy | Manage |
| Unlock Account | None | None | None by default | Manage |
| Capital Cycle | None | Manage own | None | View only if policy defines |
| Time Capital | None | Manage own | None | View only if policy defines |
| Money Capital | None | Manage own | None | View only if policy defines |
| Capital Allocation | None | Manage own | None | View only if policy defines |
| Capital History | None | View own | None | View only if policy defines |
| Task Management | None | Manage own | None | View only if policy defines |
| Category | None | Manage own if customizable | None | Manage global if defined |
| Tag | None | Manage own if customizable | None | Manage global if defined |
| Timeline | None | Manage own | None | View only if policy defines |
| Task Progress | None | Update own | None | None by default |
| Task Completion | None | Execute own | None | None by default |
| Actual Recording | None | Manage own | None | None by default |
| Evaluation | None | View own | None | View aggregate only if policy defines |
| Dashboard User | None | View own | None | None by default |
| Reports User | None | View/export own by permission | None | View aggregate only if policy defines |
| User Administration | None | None | View/support scope | Manage |
| Staff Administration | None | None | None | Manage |
| Ticket Management | None | Create/view own | Manage assigned/support scope | Manage |
| Activity Log | None | View own if enabled | View support scope | Monitor |
| Audit | None | None | Limited only if defined | Monitor |
| Configuration | None | None | View only if allowed | Manage |
| System Dashboard | None | None | None or limited if defined | Monitor |
| Announcement | None | Receive | Receive | Manage if in scope |
| Export Standards | None | Export own reports if allowed | Export support data if allowed | Export administration reports if allowed |

## 12. Module Dependency Matrix

### 12.1 Dependency Matrix

| From / To | Identity | Resource | Task | Timeline | Tracking | Reporting | Administration | Support | Cross-cutting |
|---|---|---|---|---|---|---|---|---|---|
| Identity | Self | Enables ownership | Enables ownership | Enables access | Enables access | Enables access | Provides roles and permissions | Provides account context | Uses global rules |
| Resource | Requires User | Self | Supplies capital planning context | Supplies Time Capital condition | Supplies Planned values | Supplies utilization inputs | May be monitored by policy | None by default | Uses validation and history |
| Task | Requires User | Requires allocation context | Self | Provides scheduled items | Provides completed tasks | Provides task dimensions | May be monitored by policy | None by default | Uses lifecycle rules |
| Timeline | Requires User | Requires Time Capital | Requires Task | Self | Provides schedule context | Provides timeline statistics | None by default | None by default | Uses scheduling policy |
| Tracking | Requires User | Uses Planned capital | Requires Completed Task | Uses schedule context | Self | Provides evaluation data | None by default | None by default | Uses evaluation policy |
| Reporting | Requires User | Uses resource summaries | Uses task data | Uses timeline data | Uses evaluation data | Self | Provides admin reports separately | Uses ticket data for support reports | Uses export policy |
| Administration | Requires Identity | Policy view only | Policy view only | Policy view only | Policy view only | Uses operation reports | Self | Governs support workflow | Uses Audit policy |
| Support | Requires Identity | Not default | Not default | Not default | Not default | Support reports | Uses staff/admin functions | Self | Uses ticket policy |
| Cross-cutting | Applies | Applies | Applies | Applies | Applies | Applies | Applies | Applies | Self |

### 12.2 Dependency Notes

| Dependency | Business Meaning | Risk if Misunderstood |
|---|---|---|
| Identity -> All Modules | Actor identity and access scope must be known before protected work | Unauthorized action or wrong ownership |
| Resource -> Task | Task planning references available Time/Money Capital | Task may be planned without resource discipline |
| Task -> Timeline | Timeline only shows Task with Time Capital | Timeline may show invalid or unscheduled work |
| Task -> Tracking | Final evaluation depends on Task completion eligibility | Actual may be recorded too early |
| Tracking -> Reporting | Reports use evaluated and historical data | Report may reflect incomplete evaluation |
| Administration -> Identity | Administrative actions depend on Role/Permission governance | Staff/Admin actions may exceed authority |
| Cross-cutting -> All Modules | Validation, Audit, History and Error Handling apply globally | Inconsistent behavior across modules |

## 13. Business Glossary

| Name | Definition | Context | Related Modules |
|---|---|---|---|
| Actor | Đối tượng tương tác với hệ thống theo vai trò nghiệp vụ | Guest, User, Staff, Admin | All |
| Guest | Người chưa đăng nhập, có thể xem Landing Page, Register và Login | Access | Identity |
| User | Người dùng cá nhân quản lý nguồn lực, Task và đánh giá hiệu quả của mình | Primary user | Identity, Resource, Task, Tracking |
| Staff | Nhân sự hỗ trợ, xử lý Ticket và một số thao tác hỗ trợ theo quyền | Support operation | Administration, Support |
| Admin | Người quản trị có quyền quản lý User, Staff, Role, Permission, cấu hình và Audit | Governance | Identity, Administration |
| Role | Tập hợp trách nhiệm nghiệp vụ gắn với actor | RBAC | Identity, Administration |
| Permission | Quyền thực hiện một hành động nghiệp vụ cụ thể | Access control | Identity, Administration |
| Authentication | Quá trình xác nhận actor là ai | Login | Identity |
| Authorization | Quá trình xác định actor được làm gì | Permission check | Identity |
| Session | Khoảng thời gian actor được xem là đang đăng nhập hợp lệ | Access continuity | Identity |
| Account Status | Trạng thái tài khoản như Active, Temporarily Locked, Deactivated | Access control | Identity, Administration |
| Resource | Nguồn lực hữu hạn mà User dùng để lập kế hoạch | Time, Money | Resource |
| Capital | Tổng nguồn lực User có trong một chu kỳ | Resource planning | Resource |
| Time Capital | Vốn thời gian có thể sử dụng để lập kế hoạch công việc | Planning | Resource, Task, Timeline |
| Money Capital | Vốn tiền bạc có thể sử dụng để lập kế hoạch công việc | Planning | Resource, Task |
| Cycle | Chu kỳ quản lý nguồn lực: Daily, Weekly hoặc Monthly | Resource period | Resource |
| Allocation | Hành động cấp nguồn lực cho kế hoạch hoặc Task | Planning | Resource, Task |
| Reallocation | Điều chỉnh lại phân bổ nguồn lực | Planning change | Resource |
| Available Capital | Nguồn lực hiện có thể phân bổ | Balance | Resource |
| Remaining Capital | Nguồn lực còn lại sau phân bổ | Balance | Resource |
| Planned | Giá trị dự kiến trước khi thực hiện | Planning baseline | Resource, Task, Tracking |
| Actual | Giá trị thực tế sau khi thực hiện | Evaluation input | Tracking |
| Variance | Chênh lệch giữa Actual và Planned | Evaluation | Tracking, Reporting |
| Efficiency | Mức độ hiệu quả sử dụng nguồn lực | Evaluation | Tracking, Reporting |
| Task | Đơn vị công việc trung tâm của LifeBalance | Work item | Task |
| Task Plan | Tập thông tin lập kế hoạch của Task | Planning | Task |
| Timeline | Biểu diễn lịch thực hiện các Task có Time Capital | Scheduling | Timeline |
| Priority | Mức ưu tiên của Task | Planning | Task |
| Deadline | Thời hạn hoàn thành Task | Planning | Task |
| Progress | Mức độ hoàn thành Task từ 0 đến 100 | Execution | Task |
| Completion | Sự kiện Task hoàn thành và đủ điều kiện đánh giá | Lifecycle | Task, Tracking |
| Category | Phân loại Task theo nhóm nghiệp vụ hoặc lĩnh vực đời sống | Organization | Task, Reporting |
| Tag | Nhãn linh hoạt gắn cho Task | Organization | Task, Reporting |
| Recurring Task | Task lặp lại theo quy tắc nếu phạm vi được xác nhận | Planning | Task |
| Reminder | Nhắc việc theo thời điểm hoặc Deadline nếu phạm vi được xác nhận | Notification | Task, Cross-cutting |
| Tracking | Ghi nhận Actual và lịch sử thực hiện | Execution review | Tracking |
| Evaluation | Đánh giá Planned vs Actual, Variance và Efficiency | Improvement | Tracking |
| Dashboard | Tổng quan chỉ số nghiệp vụ theo phạm vi quyền | Monitoring | Reporting, Administration |
| Report | Tài liệu/tổng hợp dữ liệu nghiệp vụ phục vụ phân tích | Reporting | Reporting, Administration |
| KPI | Chỉ số đo lường hiệu quả nghiệp vụ | Performance | Tracking, Reporting |
| History | Lịch sử thay đổi hoặc hoạt động hiển thị theo phạm vi | Traceability | All |
| Audit | Ghi nhận hành động quan trọng phục vụ kiểm soát và trách nhiệm | Governance | Identity, Administration, Cross-cutting |
| Activity Log | Ghi nhận hoạt động vận hành hoặc cá nhân theo chính sách | Operational trace | Administration, Cross-cutting |
| Support Ticket | Yêu cầu hỗ trợ được ghi nhận và xử lý theo quy trình | Support | Support |
| Ticket Status | Trạng thái vòng đời Ticket | Support workflow | Support |
| Ticket Priority | Mức ưu tiên xử lý Ticket | Support planning | Support |
| Ticket Category | Phân loại Ticket theo nhóm vấn đề | Support organization | Support |
| Configuration | Thiết lập nghiệp vụ ảnh hưởng vận hành hệ thống | Governance | Administration |
| Announcement | Thông báo vận hành nếu thuộc phạm vi | Communication | Administration |
| Maintenance Mode | Trạng thái duy trì vận hành nếu thuộc phạm vi | Operation | Administration |
| Business Rule | Quy tắc ràng buộc hành vi nghiệp vụ | Requirement control | All |
| Validation Rule | Quy tắc kiểm tra dữ liệu nghiệp vụ | Data quality | All |
| Acceptance Criteria | Điều kiện chấp nhận User Story | Agile requirement | All |
| Use Case | Mô tả tương tác giữa actor và hệ thống để đạt mục tiêu | Behavior | All |
| User Story | Mô tả nhu cầu người dùng theo Agile | Product backlog | All |

## 14. Acronyms

| Acronym | Full Term | Meaning in LifeBalance |
|---|---|---|
| BA | Business Analysis / Business Analyst | Hoạt động hoặc vai trò phân tích nghiệp vụ |
| BABOK | Business Analysis Body of Knowledge | Khung tham chiếu thực hành BA |
| BPMN | Business Process Model and Notation | Chuẩn mô tả quy trình nghiệp vụ |
| BRS | Business Requirement Specification | Tài liệu yêu cầu nghiệp vụ |
| CBR | Cross-cutting Business Requirement | Yêu cầu nghiệp vụ dùng chung |
| CRUD | Create, Read, Update, Delete | Ma trận hành động nghiệp vụ cơ bản |
| DoD | Definition of Done | Điều kiện Story được xem là hoàn thành |
| DoR | Definition of Ready | Điều kiện Story sẵn sàng vào Sprint |
| FR | Functional Requirement | Yêu cầu chức năng |
| GBR | Global Business Rule | Quy tắc nghiệp vụ toàn cục |
| IAM | Identity and Access Management | Quản lý danh tính và quyền truy cập |
| IEEE | Institute of Electrical and Electronics Engineers | Tổ chức ban hành chuẩn tham chiếu |
| INVEST | Independent, Negotiable, Valuable, Estimable, Small, Testable | Nguyên tắc viết User Story |
| KPI | Key Performance Indicator | Chỉ số đo lường hiệu quả |
| MVP | Minimum Viable Product | Phiên bản sản phẩm tối thiểu có giá trị |
| NFR | Non-functional Requirement | Yêu cầu phi chức năng |
| PO | Product Owner | Người chịu trách nhiệm giá trị sản phẩm |
| RBAC | Role Based Access Control | Kiểm soát quyền theo vai trò |
| RCM | Resource Capital Management | Quản lý nguồn vốn |
| RTM | Requirement Traceability Matrix | Ma trận truy vết yêu cầu |
| SRS | Software Requirement Specification | Tài liệu đặc tả yêu cầu phần mềm |
| TER | Tracking, Evaluation and Reporting | Theo dõi, đánh giá và báo cáo |
| TTM | Task and Timeline Management | Quản lý Task và Timeline |
| UC | Use Case | Ca sử dụng |
| UI/UX | User Interface / User Experience | Giao diện và trải nghiệm người dùng |
| VAL | Validation Rule | Quy tắc kiểm tra hợp lệ |

## 15. Findings & Consistency Review

### 15.1 Review Summary

| Review Area | Finding | Severity | Action |
|---|---|---|---|
| Requirement Coverage | Các dải FR chính từ Volume 2 đến Volume 6 đã được ánh xạ sang Use Case và User Story trong Volume 8 và Volume 9 | Low | Duy trì RTM khi có thay đổi |
| Cross-cutting Coverage | Volume 7 dùng GBR và VAL thay vì mã FR theo module, cần duy trì mapping riêng | Medium | Giữ nhóm CBR riêng trong RTM |
| Optional Scope | Recurring Task, Reminder, Export, Announcement, Maintenance Mode và Reopen policy xuất hiện như phạm vi cần xác nhận | Medium | Không đưa vào MVP nếu chưa phê duyệt rõ |
| Term Consistency | “Capital”, “Resource”, “Allocation”, “Planned”, “Actual”, “Variance” đã được dùng nhất quán về nghĩa chính | Low | Duy trì glossary làm chuẩn |
| Role Boundary | Staff có quyền hỗ trợ và khóa tạm theo quy trình, nhưng không được thay đổi Role/Permission | Low | Tiếp tục giữ boundary trong acceptance review |
| Admin Boundary | Admin có quyền quản trị, nhưng việc Admin tự thu hồi quyền trọng yếu cần chính sách xác nhận | High | Đưa vào Open Questions và governance policy |
| Reporting Interpretation | KPI và Efficiency đã được mô tả ở mức nghiệp vụ, nhưng ngưỡng đánh giá chưa chốt | Medium | Product Owner cần phê duyệt ngưỡng trước khi triển khai KPI nâng cao |
| Reopen Behavior | Reopen xuất hiện ở Capital Cycle, Task và Ticket với điều kiện “nếu chính sách cho phép” | Medium | Cần chính sách Reopen thống nhất |

### 15.2 Requirement Duplication Review

| Area | Observation | Impact | Recommendation |
|---|---|---|---|
| Role/Permission | Role và Permission xuất hiện trong cả Identity và Administration | Có thể bị hiểu là hai phạm vi khác nhau | Chuẩn hóa: Identity định nghĩa quyền truy cập; Administration vận hành quản trị quyền |
| Account Lock | Temporary Lock xuất hiện trong Identity và Administration | Có thể trùng trách nhiệm Staff/Admin | Chuẩn hóa: Identity là rule truy cập; Administration là quy trình vận hành |
| Dashboard | User Dashboard và Administration Dashboard cùng dùng thuật ngữ Dashboard | Có thể nhầm phạm vi dữ liệu | Dùng “User Dashboard” và “Administration Dashboard” trong tài liệu giao tiếp |
| History/Audit | History, Activity Log và Audit có điểm giao nhau | Có thể nhầm mục đích | History dành cho người dùng/phạm vi hoạt động; Audit dành cho kiểm soát hành động quan trọng |

### 15.3 Missing or Partially Defined Items

| Item | Status | Affected Volume | Comment |
|---|---|---|---|
| Password policy | Partially Defined | Volume 2, Volume 9 | Cần xác nhận độ dài, độ phức tạp, vòng đời |
| Session duration | Partially Defined | Volume 2, Volume 9 | Cần xác nhận thời lượng và điều kiện hết hạn |
| Over Allocation threshold | Partially Defined | Volume 3, Volume 9 | Cần xác nhận ngưỡng cảnh báo và lý do bắt buộc |
| Reopen policy | Partially Defined | Volume 3, 4, 6, 9 | Cần thống nhất theo từng đối tượng |
| Efficiency scale | Partially Defined | Volume 5, Volume 9 | Cần xác nhận thang đánh giá |
| Report export format | Partially Defined | Volume 5, Volume 7, Volume 9 | Cần xác nhận định dạng và quyền xuất |
| Announcement | Optional Scope | Volume 6, Volume 9 | Chưa xác nhận là phạm vi chính thức |
| Maintenance Mode | Optional Scope | Volume 6, Volume 7 | Chưa xác nhận chi tiết vận hành |

### 15.4 Use Case and Story Consistency

| Checkpoint | Result | Notes |
|---|---|---|
| Use Case chưa có Story | Không phát hiện ở mức nhóm requirement | Volume 9 dùng Story theo Feature, một Story có thể bao phủ nhiều Use Case hoặc FR liên quan |
| Story chưa có Acceptance Criteria | Không phát hiện trong Volume 9 | Mỗi Story có AC dạng Given/When/Then |
| Functional Requirement chưa được ánh xạ | Không phát hiện ở mức dải mã module | RTM dùng dải mã FR đã phê duyệt |
| Business Rule mâu thuẫn | Chưa phát hiện mâu thuẫn trực tiếp | Một số policy chưa rõ được ghi nhận là Open Questions |
| Thuật ngữ không thống nhất | Có rủi ro nhẹ giữa Audit, History và Activity Log | Đã chuẩn hóa trong Glossary |

## 16. Recommendations

| ID | Recommendation | Rationale | Owner Suggested |
|---|---|---|---|
| REC-001 | Phê duyệt Business Glossary làm chuẩn dùng chung | Giảm tranh luận thuật ngữ giữa các nhóm | Product Owner, BA |
| REC-002 | Duy trì RTM sau mỗi thay đổi requirement | Tránh mất traceability khi backlog thay đổi | BA |
| REC-003 | Tổ chức workshop riêng cho Reopen policy | Reopen ảnh hưởng lịch sử, đánh giá và trạng thái | Product Owner, BA |
| REC-004 | Chốt chính sách Admin tự thay đổi quyền trọng yếu | Đây là rủi ro governance cao | Product Owner, Admin stakeholder |
| REC-005 | Xác nhận MVP boundary trước khi lập Sprint chi tiết | Tránh đưa optional scope vào giai đoạn đầu | Product Owner |
| REC-006 | Chuẩn hóa KPI và thang Efficiency trước Release liên quan | Báo cáo cần có cách diễn giải thống nhất | Product Owner, BA |
| REC-007 | Rà soát Staff permission bằng ma trận Actor-Feature | Staff có quyền hỗ trợ nhưng không quản trị quyền | BA, Operations stakeholder |
| REC-008 | Tách rõ User Dashboard và Administration Dashboard trong giao tiếp dự án | Giảm nhầm lẫn phạm vi dữ liệu và actor | BA, UI/UX |
| REC-009 | Duy trì Findings như danh sách kiểm soát thay đổi | Không tự sửa requirement khi phát hiện điểm chưa rõ | BA |
| REC-010 | Tạo checklist nghiệm thu theo Decision Table cho nghiệp vụ quan trọng | Login, Permission, Allocation, Completion và Evaluation có nhiều điều kiện | QA, BA |

## 17. Appendix

### 17.1 Module List

| Module ID | Module Name | Volume |
|---|---|---|
| IAM | Identity & Authorization | Volume 2 |
| RCM | Resource Capital Management | Volume 3 |
| TTM | Task & Timeline Management | Volume 4 |
| TER | Tracking, Evaluation & Reporting | Volume 5 |
| ADM | Administration & Support | Volume 6 |
| CBR | Cross-cutting Business Requirements | Volume 7 |
| UCS | Complete Use Case Specification | Volume 8 |
| PBL | User Stories & Product Backlog | Volume 9 |

### 17.2 Actor List

| Actor | Description | Primary Modules |
|---|---|---|
| Guest | Người chưa đăng nhập, có thể xem Landing Page, Register và Login | Identity |
| User | Người dùng cá nhân quản lý nguồn lực, Task, Timeline và đánh giá hiệu quả | Resource, Task, Timeline, Tracking, Reporting |
| Staff | Nhân sự hỗ trợ vận hành, xử lý Ticket và một số thao tác hỗ trợ | Support, Administration |
| Admin | Người quản trị Role, Permission, User, Staff, cấu hình, Audit và Dashboard quản trị | Identity, Administration |

### 17.3 Business Goal List

| Goal ID | Business Goal |
|---|---|
| BG-001 | Cho phép người dùng quản lý nguồn lực hữu hạn là thời gian và tiền bạc |
| BG-002 | Đảm bảo mỗi Task được lập kế hoạch và cấp nguồn lực trước khi thực hiện |
| BG-003 | Hỗ trợ người dùng theo dõi việc sử dụng nguồn lực thực tế |
| BG-004 | Đánh giá hiệu quả dựa trên Planned, Actual, Variance và Efficiency |
| BG-005 | Cung cấp Dashboard và Report để cải thiện lập kế hoạch tương lai |
| BG-006 | Bảo vệ truy cập bằng danh tính, Role và Permission |
| BG-007 | Hỗ trợ vận hành, Ticket, Audit và quản trị hệ thống |
| BG-008 | Chuẩn hóa quy tắc, lịch sử, validation và error handling toàn hệ thống |

### 17.4 Functional Requirement List

| Module | Functional Requirement Range | Description |
|---|---|---|
| Identity | IAM-FR-001 đến IAM-FR-045 | Register, Login, Logout, Profile, Password, Session, Access Control, Role, Permission, User Management, Audit |
| Resource | RCM-FR-001 đến RCM-FR-050 | Capital Cycle, Time/Money Capital, Adjustment, Allocation, Balance, History, Summary |
| Task & Timeline | TTM-FR-001 đến TTM-FR-060 | Task CRUD, Planning, Category, Tag, Priority, Deadline, Timeline, Progress, Completion, Archive, Recurring, Reminder |
| Tracking & Reporting | TER-FR-001 đến TER-FR-060 | Actual, Planned vs Actual, Variance, Efficiency, Statistics, Dashboard, Report, History, Trend, KPI |
| Administration & Support | ADM-FR-001 đến ADM-FR-060 | User, Staff, Ticket, Activity Log, Audit, Configuration, System Dashboard, Role, Permission, Announcement |
| Cross-cutting | GBR-001 đến GBR-030, VAL-001 đến VAL-028 | Global Rules, Validation, Policies, Audit, Activity History, Notification, Search, Export, Error Handling |

### 17.5 Business Rule List

| Rule Group | Range | Summary |
|---|---|---|
| Global Business Rules | GBR-001 đến GBR-030 | Quy tắc dùng chung toàn LifeBalance |
| Identity Business Rules | IAM-BR-001 trở đi | Quy tắc Guest, User, Staff, Admin, Account Status, Role, Permission |
| Resource Business Rules | RCM-BR-001 trở đi | Quy tắc chu kỳ, nguồn lực, phân bổ, điều chỉnh, lịch sử |
| Task Business Rules | TTM-BR-001 trở đi | Quy tắc Task ownership, Timeline, Progress, Deadline, Reopen |
| Evaluation Business Rules | TER-BR-001 trở đi | Quy tắc Actual, Variance, Efficiency, Dashboard, Report |
| Administration Business Rules | ADM-BR-001 trở đi | Quy tắc Staff, Admin, Ticket, Audit, Configuration |
| Validation Rules | VAL-001 đến VAL-028 | Kiểm tra dữ liệu nghiệp vụ bắt buộc, tùy chọn, có điều kiện và dẫn xuất |

### 17.6 Use Case List

| Module | Use Case Range | Count Reference |
|---|---|---|
| Identity | IAM-UC-001 đến IAM-UC-028 | 28 |
| Resource | RCM-UC-001 đến RCM-UC-027 | 27 |
| Task & Timeline | TTM-UC-001 đến TTM-UC-031 | 31 |
| Tracking, Evaluation & Reporting | TER-UC-001 đến TER-UC-025 | 25 |
| Administration & Support | ADM-UC-001 đến ADM-UC-033 | 33 |
| Cross-cutting | CBR-UC-001 đến CBR-UC-008 | 8 |
| Total | All Use Cases | 152 |

### 17.7 User Story List

| Epic | Story Range | Count Reference |
|---|---|---|
| Identity | US-IAM-001 đến US-IAM-028 | 28 |
| Resource | US-RCM-001 đến US-RCM-020 | 20 |
| Task & Timeline | US-TTM-001 đến US-TTM-028 | 28 |
| Tracking, Evaluation & Reporting | US-TER-001 đến US-TER-025 | 25 |
| Administration & Support | US-ADM-001 đến US-ADM-033 | 33 |
| Cross-cutting | US-CBR-001 đến US-CBR-008 | 8 |
| Total | All Stories | 142 |

### 17.8 Report List

| Report Name | Purpose | Primary Actor |
|---|---|---|
| Daily Report | Tổng hợp kết quả theo ngày | User |
| Weekly Report | Tổng hợp kết quả theo tuần | User |
| Monthly Report | Tổng hợp kết quả theo tháng | User |
| Yearly Report | Tổng hợp kết quả theo năm | User |
| Category Report | Phân tích theo Category | User |
| Tag Report | Phân tích theo Tag | User |
| Resource Report | Phân tích sử dụng Time/Money Capital | User |
| Productivity Report | Đánh giá hiệu quả cá nhân | User |
| History Report | Xem lịch sử thay đổi và đánh giá | User |
| Trend Report | Phân tích xu hướng theo kỳ | User |
| Ticket Report | Theo dõi Ticket hỗ trợ | Staff, Admin |
| Support Performance Report | Đánh giá hiệu quả hỗ trợ | Admin |
| User Activity Report | Theo dõi hoạt động User theo phạm vi | Admin |
| Audit Report | Tra cứu sự kiện quan trọng | Admin |
| System Operation Report | Tổng quan vận hành | Admin |
| Role Assignment Report | Theo dõi thay đổi Role | Admin |
| Permission Change Report | Theo dõi thay đổi Permission | Admin |
| Configuration Change Report | Theo dõi thay đổi cấu hình nghiệp vụ | Admin |

### 17.9 Dashboard List

| Dashboard Name | Purpose | Primary Actor |
|---|---|---|
| User Productivity Dashboard | Hiển thị hiệu quả sử dụng nguồn lực cá nhân | User |
| Resource Utilization Dashboard | Hiển thị mức sử dụng Time/Money Capital | User |
| Task Progress Dashboard | Hiển thị tiến độ và trạng thái Task | User |
| Reporting Dashboard | Tổng quan chỉ số báo cáo cá nhân | User |
| Administration Dashboard | Tổng quan vận hành quản trị | Admin |
| Support Dashboard | Theo dõi Ticket và hiệu suất hỗ trợ | Staff, Admin |
| Audit Dashboard | Theo dõi sự kiện kiểm soát quan trọng | Admin |

### 17.10 Business Process List

| Process Group | Process ID Range | Count |
|---|---|---|
| Identity | BP-IAM-001 đến BP-IAM-012 | 12 |
| Resource | BP-RCM-001 đến BP-RCM-012 | 12 |
| Task & Timeline | BP-TTM-001 đến BP-TTM-013 | 13 |
| Tracking & Reporting | BP-TER-001 đến BP-TER-011 | 11 |
| Administration & Support | BP-ADM-001 đến BP-ADM-014 | 14 |
| Cross-cutting | BP-CBR-001 đến BP-CBR-008 | 8 |
| Total | BP-IAM, BP-RCM, BP-TTM, BP-TER, BP-ADM, BP-CBR | 70 |

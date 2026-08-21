# LifeBalance
# Volume 5 - Tracking, Evaluation & Reporting SRS

## 1. Module Overview

### 1.1 Purpose

Tracking, Evaluation & Reporting là module chịu trách nhiệm ghi nhận dữ liệu thực tế sau khi Task đủ điều kiện đánh giá, so sánh dữ liệu Planned và Actual, xác định Variance, đánh giá mức độ hiệu quả sử dụng nguồn lực và cung cấp thông tin tổng hợp thông qua Dashboard, Report, Statistics, Trend Analysis và Personal KPI.

Theo triết lý sản phẩm của LifeBalance, mọi công việc đều tiêu tốn nguồn lực và mọi nguồn lực đều hữu hạn. Vì vậy, việc hoàn thành Task không phải là điểm kết thúc duy nhất. Sau khi Task hoàn thành, User cần biết Task đã sử dụng bao nhiêu thời gian, bao nhiêu chi phí, mức sử dụng thực tế có khác kế hoạch hay không và sai lệch đó phản ánh điều gì về năng lực lập kế hoạch cá nhân.

Module này không quản lý Task và không quản lý Resource Capital. Module này sử dụng dữ liệu từ các module trước như Planned Time, Planned Cost, Task Status, Category, Tag và Timeline information để phục vụ phân tích. Module này chỉ tập trung vào ghi nhận Actual, đánh giá, tổng hợp, thống kê và trình bày kết quả phục vụ cải thiện.

### 1.2 Objectives

| Objective ID | Objective | Description |
|---|---|---|
| TER-OBJ-001 | Ghi nhận Actual Resource Consumption | Cho phép User ghi nhận Actual Time và Actual Cost sau khi Task đủ điều kiện đánh giá. |
| TER-OBJ-002 | So sánh Planned và Actual | Cung cấp khả năng so sánh Planned Time với Actual Time và Planned Cost với Actual Cost. |
| TER-OBJ-003 | Tính Variance | Xác định sai lệch nguồn lực giữa kế hoạch và thực tế theo quy tắc nghiệp vụ. |
| TER-OBJ-004 | Đánh giá Resource Efficiency | Hỗ trợ đánh giá mức độ hiệu quả sử dụng thời gian và tiền bạc. |
| TER-OBJ-005 | Cung cấp Dashboard | Cung cấp góc nhìn tổng quan về hiệu quả, tiến độ và chỉ số nguồn lực trong phạm vi quyền của User. |
| TER-OBJ-006 | Cung cấp Reports | Cung cấp báo cáo theo ngày, tuần, tháng, năm, Category, Tag, Resource, Productivity, History và Trend. |
| TER-OBJ-007 | Hỗ trợ Personal KPI | Cung cấp các KPI cá nhân như Completion Rate, Planning Accuracy, Time Efficiency, Cost Efficiency và Productivity Score. |
| TER-OBJ-008 | Hỗ trợ Trend Analysis | Cho phép User nhìn thấy xu hướng hiệu quả và sai lệch theo thời gian. |
| TER-OBJ-009 | Bảo vệ quyền truy cập dữ liệu | Bảo đảm Dashboard và Report chỉ hiển thị dữ liệu thuộc phạm vi được phép. |
| TER-OBJ-010 | Hỗ trợ cải thiện lập kế hoạch | Cung cấp insight giúp User cải thiện việc ước lượng và phân bổ nguồn lực trong tương lai. |

### 1.3 Business Value

Module này tạo ra giá trị cốt lõi ở giai đoạn sau khi công việc hoàn thành. Nếu Task chỉ được đánh dấu Completed mà không ghi nhận Actual và không đánh giá sai lệch, User không thể biết mình lập kế hoạch chính xác hay chưa. Tracking, Evaluation & Reporting chuyển dữ liệu đã hoàn thành thành insight có thể sử dụng.

Giá trị thứ nhất là giúp User hiểu hiệu quả sử dụng nguồn lực. User có thể biết Task nào vượt thời gian, Task nào vượt chi phí, Task nào hoàn thành tiết kiệm hơn kế hoạch và Task nào thường xuyên gây sai lệch.

Giá trị thứ hai là cải thiện năng lực lập kế hoạch. Khi User nhìn thấy variance theo Task, Category, Tag hoặc period, User có thêm cơ sở để điều chỉnh estimated time, estimated cost và kỳ vọng cho các kế hoạch sau.

Giá trị thứ ba là hỗ trợ ra quyết định cá nhân. Dashboard và Reports giúp User đánh giá productivity, resource utilization và trend thay vì chỉ nhìn vào số lượng Task hoàn thành.

Giá trị thứ tư là tăng tính minh bạch. History và Statistics giúp User truy vết sự thay đổi của Actual, Evaluation và Report theo phạm vi được phép.

Giá trị thứ năm là tạo nền tảng cho cải thiện liên tục. LifeBalance không chỉ là nơi ghi nhận kế hoạch, mà là hệ thống giúp User học từ quá khứ để ra quyết định tốt hơn trong tương lai.

### 1.4 Responsibilities

| Responsibility | Description |
|---|---|
| Actual Recording | Ghi nhận Actual Time và Actual Cost cho Task đủ điều kiện. |
| Actual Update | Cho phép cập nhật Actual nếu policy cho phép và ghi nhận lịch sử thay đổi. |
| Planned vs Actual View | Hiển thị so sánh giữa Planned và Actual theo Task hoặc phạm vi tổng hợp. |
| Variance Analysis | Xác định Time Variance, Cost Variance và Variance Rate ở mức nghiệp vụ. |
| Efficiency Evaluation | Đánh giá Time Efficiency, Cost Efficiency và Overall Efficiency. |
| Productivity Analysis | Hỗ trợ đánh giá productivity cá nhân dựa trên KPI được định nghĩa. |
| Dashboard Generation | Cung cấp Dashboard nghiệp vụ trong phạm vi dữ liệu được phép. |
| Report Generation | Cung cấp báo cáo theo period, Category, Tag, Resource, Productivity, History và Trend. |
| Report Search and Filter | Hỗ trợ tìm kiếm, lọc và so sánh report theo tiêu chí nghiệp vụ. |
| Export Report | Hỗ trợ xuất report nếu phạm vi được phê duyệt. |
| History View | Cho phép User xem lịch sử Actual và Evaluation theo policy. |
| Access Control Alignment | Bảo đảm dữ liệu hiển thị đúng phạm vi quyền từ Identity & Authorization. |

## 2. Business Scope

### 2.1 In Scope

| Scope Area | Description |
|---|---|
| Tracking | Ghi nhận Actual Time và Actual Cost sau khi Task đủ điều kiện đánh giá. |
| Actual Time Recording | Nhập, cập nhật và xem Actual Time. |
| Actual Cost Recording | Nhập, cập nhật và xem Actual Cost. |
| Variance Analysis | So sánh Planned và Actual để xác định sai lệch. |
| Efficiency Evaluation | Đánh giá hiệu quả sử dụng nguồn lực dựa trên Planned, Actual và kết quả hoàn thành. |
| Resource Consumption | Xem mức tiêu thụ thực tế của Time và Money. |
| Dashboard | Cung cấp tổng quan cá nhân về KPI, variance, efficiency và productivity. |
| Reports | Cung cấp báo cáo theo period, Category, Tag, Resource, Productivity, History và Trend. |
| Analytics | Hỗ trợ phân tích nghiệp vụ cá nhân dựa trên dữ liệu đã hoàn thành. |
| History | Xem lịch sử ghi nhận và cập nhật Actual hoặc Evaluation nếu policy yêu cầu. |
| Statistics | Cung cấp thống kê theo ngày, tuần, tháng, năm và phạm vi lọc được phê duyệt. |
| Trend Analysis | So sánh xu hướng giữa các period hoặc nhóm dữ liệu. |
| Personal Productivity Analysis | Cung cấp KPI cá nhân giúp User hiểu hiệu quả lập kế hoạch và thực hiện. |
| Export Report | Xuất báo cáo nếu chính sách sản phẩm phê duyệt, ví dụ PDF, Excel hoặc CSV. |

### 2.2 Out of Scope

| Out of Scope Area | Explanation |
|---|---|
| Task Management | Module không tạo, sửa, xóa, schedule, complete hoặc reopen Task. |
| Resource Capital Management | Module không tạo chu kỳ vốn, điều chỉnh vốn, phân bổ vốn hoặc chuyển số dư vốn. |
| Identity & Authorization Management | Module không quản lý tài khoản, role hoặc permission. |
| Category Management | Module chỉ sử dụng Category để thống kê, không quản lý vòng đời Category. |
| Tag Management | Module chỉ sử dụng Tag để thống kê, không quản lý vòng đời Tag. |
| Professional Financial Advice | Module không đưa ra tư vấn tài chính chuyên môn. |
| Automated Decision Making | Module không tự động quyết định thay User về kế hoạch tương lai nếu chưa có policy. |
| Team Performance Evaluation | Module tập trung vào hiệu quả cá nhân, không đánh giá hiệu suất nhóm hoặc tổ chức. |
| Interface Design | Tài liệu không mô tả bố cục, wireframe hoặc thiết kế màn hình. |

### 2.3 Dependencies

| Dependency | Description | Impact |
|---|---|---|
| Volume 1 - Vision & Business Overview | Cung cấp triết lý Planned vs Actual và cải thiện liên tục. | Module phải tập trung vào đánh giá hiệu quả nguồn lực. |
| Volume 2 - Identity & Authorization | Cung cấp quyền truy cập và ownership. | Dashboard và Report chỉ hiển thị dữ liệu User được phép xem. |
| Volume 3 - Resource Capital Management | Cung cấp khái niệm Time Capital, Money Capital và Planned Capital. | Evaluation cần Planned Time, Planned Cost và resource terminology. |
| Volume 4 - Task & Timeline Management | Cung cấp Task, Status, Category, Tag, Deadline, Scheduled data và Completion. | Actual cuối cùng chỉ được ghi nhận khi Task đủ điều kiện theo status. |
| Category and Tag capabilities | Cung cấp thông tin phân loại để thống kê. | Category Report và Tag Report phụ thuộc dữ liệu phân loại hợp lệ. |
| Report policy | Cần xác nhận loại report, format export và phạm vi dữ liệu. | Ảnh hưởng đến report requirements và acceptance criteria. |
| KPI policy | Cần xác nhận công thức, ngưỡng và cách diễn giải KPI. | Ảnh hưởng đến dashboard, productivity score và efficiency evaluation. |

## 3. Business Concepts

| Concept | Definition |
|---|---|
| Planned Time | Lượng thời gian dự kiến hoặc đã được lập kế hoạch cho Task trước khi thực hiện. |
| Actual Time | Lượng thời gian thực tế User ghi nhận sau khi Task đủ điều kiện đánh giá. |
| Planned Cost | Lượng chi phí hoặc Money Capital dự kiến cho Task trước khi thực hiện. |
| Actual Cost | Lượng chi phí thực tế User ghi nhận sau khi Task đủ điều kiện đánh giá. |
| Variance | Sai lệch giữa Actual và Planned. Theo quy tắc cơ bản: Variance = Actual - Planned. |
| Positive Variance | Trường hợp Actual lớn hơn Planned. Trong ngữ cảnh tiêu thụ nguồn lực, đây thường là vượt kế hoạch. |
| Negative Variance | Trường hợp Actual nhỏ hơn Planned. Trong ngữ cảnh tiêu thụ nguồn lực, đây thường là tiết kiệm so với kế hoạch. |
| Efficiency | Mức độ sử dụng nguồn lực hợp lý so với kế hoạch và kết quả hoàn thành. |
| Productivity | Mức độ User hoàn thành công việc và sử dụng nguồn lực hiệu quả trong một period. |
| Performance | Kết quả tổng hợp phản ánh completion, variance, efficiency và planning accuracy theo phạm vi đánh giá. |
| Completion Rate | Tỷ lệ Task hoàn thành trong một phạm vi thời gian hoặc tập Task được chọn. |
| Resource Utilization | Mức độ nguồn lực đã được sử dụng so với nguồn lực planned hoặc allocated trong phạm vi đánh giá. |
| Trend | Xu hướng thay đổi của KPI hoặc chỉ số qua nhiều period. |
| Statistics | Thống kê định lượng về Task, Actual, Variance, Efficiency hoặc KPI theo phạm vi được chọn. |
| History | Lịch sử ghi nhận, cập nhật hoặc thay đổi dữ liệu Actual, Evaluation hoặc Report-related action nếu policy yêu cầu. |
| Dashboard | Góc nhìn tổng quan cấp cao giúp User theo dõi KPI, variance, efficiency và productivity. |
| Report | Tài liệu hoặc tập thông tin có cấu trúc phản ánh kết quả thống kê, đánh giá và phân tích trong phạm vi được chọn. |

## 4. Actors

### 4.1 User

| Attribute | Description |
|---|---|
| Responsibilities | Ghi nhận Actual Time, Actual Cost; xem Planned vs Actual; xem Dashboard; xem Report; phân tích KPI cá nhân; xem History trong phạm vi dữ liệu của chính mình. |
| Permissions | Record Own Actual; Update Own Actual; View Own Evaluation; View Own Dashboard; View Own Reports; Filter Own Reports; Export Own Reports nếu policy cho phép; View Own History. |
| Limitations | User chỉ được xem và đánh giá dữ liệu của chính mình. User không được xem report hoặc KPI của User khác. User không được thay đổi Planned data trong module này. |

### 4.2 Staff

| Attribute | Description |
|---|---|
| Responsibilities | Hỗ trợ User hiểu dữ liệu Dashboard hoặc Report nếu chính sách hỗ trợ cho phép. |
| Permissions | Không mặc định có quyền xem dữ liệu đánh giá cá nhân của User. Có thể xem thông tin giới hạn nếu được phân quyền rõ ràng. |
| Limitations | Staff không được ghi Actual thay User, không được sửa Evaluation, không được xuất Report cá nhân của User nếu không có chính sách rõ ràng. |

### 4.3 Admin

| Attribute | Description |
|---|---|
| Responsibilities | Quản trị chính sách báo cáo hoặc KPI nếu được phê duyệt; không can thiệp tùy ý vào dữ liệu đánh giá cá nhân. |
| Permissions | Có thể xem thống kê vận hành tổng quát nếu policy cho phép; không mặc định xem chi tiết cá nhân. |
| Limitations | Admin không được chỉnh sửa Actual, Evaluation hoặc Report cá nhân của User nếu không có ngoại lệ được định nghĩa rõ, có lý do và được audit theo policy. |

## 5. Functional Requirements

| Requirement ID | Requirement Name | Description | Primary Actor |
|---|---|---|---|
| TER-FR-001 | Record Actual Time | Hệ thống phải cho phép User ghi nhận Actual Time cho Task đủ điều kiện. | User |
| TER-FR-002 | Update Actual Time | Hệ thống phải cho phép User cập nhật Actual Time nếu policy cho phép. | User |
| TER-FR-003 | Record Actual Cost | Hệ thống phải cho phép User ghi nhận Actual Cost cho Task đủ điều kiện. | User |
| TER-FR-004 | Update Actual Cost | Hệ thống phải cho phép User cập nhật Actual Cost nếu policy cho phép. | User |
| TER-FR-005 | Validate Actual Time | Hệ thống phải kiểm tra Actual Time lớn hơn hoặc bằng 0. | System |
| TER-FR-006 | Validate Actual Cost | Hệ thống phải kiểm tra Actual Cost lớn hơn hoặc bằng 0. | System |
| TER-FR-007 | Validate Evaluation Eligibility | Hệ thống phải kiểm tra Task đủ điều kiện ghi Actual và Evaluation. | System |
| TER-FR-008 | View Planned vs Actual | Hệ thống phải cho phép User xem Planned Time/Cost so với Actual Time/Cost. | User |
| TER-FR-009 | Calculate Time Variance | Hệ thống phải xác định Time Variance theo business rule được phê duyệt. | System |
| TER-FR-010 | Calculate Cost Variance | Hệ thống phải xác định Cost Variance theo business rule được phê duyệt. | System |
| TER-FR-011 | Calculate Resource Efficiency | Hệ thống phải đánh giá Resource Efficiency dựa trên Planned, Actual và policy. | System |
| TER-FR-012 | Calculate Completion Rate | Hệ thống phải xác định Completion Rate trong phạm vi period hoặc filter được chọn. | System |
| TER-FR-013 | View Productivity Summary | Hệ thống phải cho phép User xem tóm tắt productivity cá nhân. | User |
| TER-FR-014 | View Daily Statistics | Hệ thống phải cho phép User xem thống kê theo ngày. | User |
| TER-FR-015 | View Weekly Statistics | Hệ thống phải cho phép User xem thống kê theo tuần. | User |
| TER-FR-016 | View Monthly Statistics | Hệ thống phải cho phép User xem thống kê theo tháng. | User |
| TER-FR-017 | View Yearly Statistics | Hệ thống phải cho phép User xem thống kê theo năm. | User |
| TER-FR-018 | View Resource Utilization | Hệ thống phải cho phép User xem mức sử dụng nguồn lực theo Time và Money. | User |
| TER-FR-019 | View Dashboard | Hệ thống phải cho phép User xem Dashboard trong phạm vi dữ liệu được phép. | User |
| TER-FR-020 | View Reports | Hệ thống phải cho phép User xem Reports theo loại report được phê duyệt. | User |
| TER-FR-021 | Export Report PDF | Hệ thống phải hỗ trợ xuất Report dạng PDF nếu export policy cho phép. | User |
| TER-FR-022 | Export Report Excel | Hệ thống phải hỗ trợ xuất Report dạng Excel nếu export policy cho phép. | User |
| TER-FR-023 | Export Report CSV | Hệ thống phải hỗ trợ xuất Report dạng CSV nếu export policy cho phép. | User |
| TER-FR-024 | Search Report | Hệ thống phải cho phép User tìm kiếm Report hoặc report result theo tiêu chí được phê duyệt. | User |
| TER-FR-025 | Filter Report | Hệ thống phải cho phép User lọc Report theo period, Category, Tag, Resource, status hoặc KPI. | User |
| TER-FR-026 | View History | Hệ thống phải cho phép User xem History liên quan đến Actual và Evaluation của chính mình. | User |
| TER-FR-027 | Compare Periods | Hệ thống phải cho phép User so sánh hai hoặc nhiều period nếu policy cho phép. | User |
| TER-FR-028 | View Trend | Hệ thống phải cho phép User xem Trend của KPI hoặc chỉ số theo thời gian. | User |
| TER-FR-029 | View Category Statistics | Hệ thống phải cho phép User xem thống kê theo Category. | User |
| TER-FR-030 | View Tag Statistics | Hệ thống phải cho phép User xem thống kê theo Tag. | User |
| TER-FR-031 | View Timeline Statistics | Hệ thống phải cho phép User xem thống kê liên quan đến scheduled/completed Task theo Timeline data. | User |
| TER-FR-032 | View Personal KPI | Hệ thống phải cho phép User xem Personal KPI trong phạm vi dữ liệu của chính mình. | User |
| TER-FR-033 | Calculate Planning Accuracy | Hệ thống phải xác định Planning Accuracy dựa trên Planned và Actual theo policy. | System |
| TER-FR-034 | Calculate Time Efficiency | Hệ thống phải xác định Time Efficiency theo policy. | System |
| TER-FR-035 | Calculate Cost Efficiency | Hệ thống phải xác định Cost Efficiency theo policy. | System |
| TER-FR-036 | Calculate Overall Efficiency | Hệ thống phải xác định Overall Efficiency dựa trên Time, Cost và policy đánh giá. | System |
| TER-FR-037 | Calculate Productivity Score | Hệ thống phải xác định Productivity Score nếu KPI policy được phê duyệt. | System |
| TER-FR-038 | View Variance Detail | Hệ thống phải cho phép User xem chi tiết variance theo Task, period, Category hoặc Tag. | User |
| TER-FR-039 | View Resource Consumption History | Hệ thống phải cho phép User xem lịch sử tiêu thụ nguồn lực thực tế. | User |
| TER-FR-040 | Evaluation Status Tracking | Hệ thống phải phân biệt Task đã được đánh giá và chưa được đánh giá nếu policy yêu cầu. | System |
| TER-FR-041 | Re-evaluate Task | Hệ thống phải cho phép User đánh giá lại Task nếu Actual được cập nhật và policy cho phép. | User |
| TER-FR-042 | Lock Evaluation After Finalization | Hệ thống phải ngăn thay đổi Evaluation đã finalized nếu không có policy mở lại. | System |
| TER-FR-043 | Dashboard Period Filter | Hệ thống phải cho phép lọc Dashboard theo daily, weekly, monthly, yearly hoặc custom period nếu policy cho phép. | User |
| TER-FR-044 | Dashboard Category Filter | Hệ thống phải cho phép lọc Dashboard theo Category nếu có dữ liệu. | User |
| TER-FR-045 | Dashboard Tag Filter | Hệ thống phải cho phép lọc Dashboard theo Tag nếu có dữ liệu. | User |
| TER-FR-046 | Report Period Validation | Hệ thống phải kiểm tra khoảng thời gian report hợp lệ. | System |
| TER-FR-047 | Empty Data Handling | Hệ thống phải xử lý trường hợp Dashboard hoặc Report không có dữ liệu. | System |
| TER-FR-048 | Large Report Handling | Hệ thống phải xử lý report có phạm vi dữ liệu lớn theo policy nghiệp vụ. | System |
| TER-FR-049 | Access Scope Validation | Hệ thống phải bảo đảm User chỉ xem dữ liệu thuộc phạm vi quyền. | System |
| TER-FR-050 | Record Evaluation History | Hệ thống phải ghi nhận lịch sử thay đổi Actual và Evaluation nếu policy yêu cầu. | System |
| TER-FR-051 | View Daily Report | Hệ thống phải cho phép User xem Daily Report. | User |
| TER-FR-052 | View Weekly Report | Hệ thống phải cho phép User xem Weekly Report. | User |
| TER-FR-053 | View Monthly Report | Hệ thống phải cho phép User xem Monthly Report. | User |
| TER-FR-054 | View Yearly Report | Hệ thống phải cho phép User xem Yearly Report. | User |
| TER-FR-055 | View Resource Report | Hệ thống phải cho phép User xem Resource Report. | User |
| TER-FR-056 | View Productivity Report | Hệ thống phải cho phép User xem Productivity Report. | User |
| TER-FR-057 | View History Report | Hệ thống phải cho phép User xem History Report. | User |
| TER-FR-058 | View Trend Report | Hệ thống phải cho phép User xem Trend Report. | User |
| TER-FR-059 | Staff Report Access Control | Hệ thống phải ngăn Staff xem report cá nhân của User nếu không có policy cho phép. | Staff |
| TER-FR-060 | Admin Report Access Control | Hệ thống phải ngăn Admin xem chi tiết cá nhân nếu không có policy rõ ràng. | Admin |

## 6. Non-functional Requirements

| NFR ID | Category | Requirement | Description |
|---|---|---|---|
| TER-NFR-001 | Performance | Dashboard Response | Dashboard phải phản hồi trong thời gian phù hợp với nhu cầu xem nhanh KPI cá nhân. |
| TER-NFR-002 | Performance | Report Response | Report phải được sinh trong thời gian phù hợp với phạm vi dữ liệu được chọn. |
| TER-NFR-003 | Availability | Evaluation Availability | User cần có thể ghi Actual và xem Evaluation khi Task đã đủ điều kiện. |
| TER-NFR-004 | Availability | Report Availability | Dashboard và Reports cần khả dụng khi User muốn xem lại kết quả. |
| TER-NFR-005 | Reliability | Calculation Reliability | Variance và KPI phải được tính nhất quán theo policy. |
| TER-NFR-006 | Reliability | Report Consistency | Report cùng phạm vi dữ liệu phải phản ánh kết quả nhất quán theo thời điểm truy xuất. |
| TER-NFR-007 | Security | Ownership Protection | Dữ liệu đánh giá cá nhân chỉ được hiển thị cho actor có quyền. |
| TER-NFR-008 | Security | Export Protection | Export Report phải tuân thủ phạm vi quyền của User. |
| TER-NFR-009 | Usability | Insight Clarity | Dashboard và Report phải trình bày ý nghĩa KPI rõ ràng ở mức nghiệp vụ. |
| TER-NFR-010 | Usability | Empty State Clarity | Khi không có dữ liệu, hệ thống phải giải thích trạng thái rỗng rõ ràng. |
| TER-NFR-011 | Auditability | Evaluation Traceability | Thay đổi Actual hoặc Evaluation quan trọng phải có khả năng truy vết nếu policy yêu cầu. |
| TER-NFR-012 | Auditability | Export Traceability | Hoạt động export report có thể được ghi nhận nếu policy yêu cầu. |
| TER-NFR-013 | Scalability | Historical Growth | Module phải hỗ trợ tăng trưởng dữ liệu lịch sử theo thời gian sử dụng. |
| TER-NFR-014 | Scalability | Report Range Growth | Module phải xử lý các phạm vi report dài hơn theo policy. |
| TER-NFR-015 | Maintainability | KPI Policy Maintainability | KPI definition và ngưỡng đánh giá cần có khả năng điều chỉnh theo quyết định nghiệp vụ. |
| TER-NFR-016 | Maintainability | Report Catalog Maintainability | Danh mục report cần có khả năng mở rộng khi có nhu cầu mới. |
| TER-NFR-017 | Report Accuracy | Data Accuracy | Dashboard và Report phải phản ánh dữ liệu trong phạm vi được chọn tại thời điểm truy xuất. |
| TER-NFR-018 | Report Accuracy | Formula Transparency | Ý nghĩa KPI và cách đánh giá phải được định nghĩa rõ ở mức nghiệp vụ. |

## 7. Business Rules

| Business Rule ID | Business Rule |
|---|---|
| TER-BR-001 | Chỉ Task Completed mới được ghi nhận Actual cuối cùng nếu policy không cho phép ngoại lệ. |
| TER-BR-002 | Task chưa Completed không được final evaluation nếu chưa đủ điều kiện. |
| TER-BR-003 | Actual Time phải lớn hơn hoặc bằng 0. |
| TER-BR-004 | Actual Cost phải lớn hơn hoặc bằng 0. |
| TER-BR-005 | Planned Time không bị thay đổi trong module này. |
| TER-BR-006 | Planned Cost không bị thay đổi trong module này. |
| TER-BR-007 | Planned không bị thay đổi sau evaluation nếu không có quy trình Reopen từ module liên quan. |
| TER-BR-008 | Variance = Actual - Planned ở mức business rule cơ bản. |
| TER-BR-009 | Positive Variance nghĩa là Actual lớn hơn Planned. |
| TER-BR-010 | Negative Variance nghĩa là Actual nhỏ hơn Planned. |
| TER-BR-011 | Time Variance chỉ được tính khi Planned Time và Actual Time có dữ liệu hợp lệ. |
| TER-BR-012 | Cost Variance chỉ được tính khi Planned Cost và Actual Cost có dữ liệu hợp lệ. |
| TER-BR-013 | Task không có Planned Time phải được xử lý theo Missing Planned Time policy khi tính Time Efficiency. |
| TER-BR-014 | Task không có Planned Cost phải được xử lý theo Missing Planned Cost policy khi tính Cost Efficiency. |
| TER-BR-015 | Actual Cost bị thiếu không được xem mặc định là 0 nếu policy chưa xác nhận. |
| TER-BR-016 | Actual Time bị thiếu không được xem mặc định là 0 nếu policy chưa xác nhận. |
| TER-BR-017 | Dashboard chỉ hiển thị dữ liệu thuộc phạm vi quyền của User. |
| TER-BR-018 | Report chỉ hiển thị dữ liệu thuộc phạm vi quyền của User. |
| TER-BR-019 | Staff không được xem report cá nhân của User nếu không có policy cho phép. |
| TER-BR-020 | Admin không được xem chi tiết cá nhân nếu không có policy rõ ràng. |
| TER-BR-021 | Report phải phản ánh dữ liệu tại thời điểm truy xuất. |
| TER-BR-022 | Export Report phải tuân thủ cùng phạm vi dữ liệu như Report đang xem. |
| TER-BR-023 | Khoảng thời gian thống kê phải hợp lệ. |
| TER-BR-024 | Start period không được sau end period. |
| TER-BR-025 | Empty Dashboard phải được xử lý bằng trạng thái không có dữ liệu, không được hiển thị KPI gây hiểu nhầm. |
| TER-BR-026 | Empty Report phải thể hiện rõ không có dữ liệu trong phạm vi chọn. |
| TER-BR-027 | Completion Rate phải chỉ tính trên tập Task thuộc phạm vi lọc hợp lệ. |
| TER-BR-028 | Category Statistics chỉ tính Task có Category phù hợp trong phạm vi quyền. |
| TER-BR-029 | Tag Statistics chỉ tính Task có Tag phù hợp trong phạm vi quyền. |
| TER-BR-030 | Timeline Statistics chỉ sử dụng Task có thông tin Timeline phù hợp từ module liên quan. |
| TER-BR-031 | Productivity Score không được hiển thị như kết luận tuyệt đối nếu KPI policy chưa xác định ngưỡng. |
| TER-BR-032 | Efficiency phải được diễn giải theo ngữ cảnh, không chỉ là dùng ít nguồn lực hơn. |
| TER-BR-033 | Task tiết kiệm nguồn lực nhưng không hoàn thành đúng điều kiện không tự động được xem là hiệu quả cao. |
| TER-BR-034 | Task vượt Planned nhưng có lý do hợp lệ vẫn phải ghi variance; diễn giải thuộc policy evaluation. |
| TER-BR-035 | Actual update sau evaluation phải kích hoạt re-evaluation nếu policy cho phép. |
| TER-BR-036 | Finalized Evaluation không được sửa nếu chưa có policy mở lại. |
| TER-BR-037 | History phải ghi nhận thay đổi Actual nếu History Policy yêu cầu. |
| TER-BR-038 | History phải ghi nhận thay đổi Evaluation nếu History Policy yêu cầu. |
| TER-BR-039 | Compare Periods phải dùng period hợp lệ và cùng phạm vi so sánh được định nghĩa. |
| TER-BR-040 | Trend Analysis chỉ được hiển thị khi có đủ dữ liệu tối thiểu theo policy. |
| TER-BR-041 | Daily, Weekly, Monthly và Yearly statistics phải được phân biệt rõ theo period. |
| TER-BR-042 | KPI không đủ dữ liệu phải được hiển thị là không đủ dữ liệu, không tự suy diễn. |
| TER-BR-043 | Search Report và Filter Report không được trả dữ liệu ngoài phạm vi quyền. |
| TER-BR-044 | Large Report phải được xử lý theo policy để tránh gây hiểu nhầm hoặc thiếu dữ liệu. |
| TER-BR-045 | Report export format chỉ được hỗ trợ nếu nằm trong Export Policy được phê duyệt. |
| TER-BR-046 | Dashboard drill-down chỉ được hiển thị dữ liệu mà User có quyền xem. |
| TER-BR-047 | Personal KPI là chỉ số hỗ trợ cải thiện cá nhân, không phải đánh giá pháp lý hoặc chuyên môn. |
| TER-BR-048 | Resource Utilization không được cộng gộp Time và Money thành một đơn vị duy nhất nếu không có policy. |
| TER-BR-049 | Time Efficiency và Cost Efficiency phải được đánh giá riêng trước khi tổng hợp Overall Efficiency. |
| TER-BR-050 | Dữ liệu từ Task bị Reopen sau evaluation phải được xử lý theo Re-evaluation Policy. |

## 8. Evaluation Policies

### 8.1 Resource Evaluation Policy

Resource Evaluation Policy định nghĩa cách đánh giá việc sử dụng Time và Money sau khi Task hoàn thành.

Nguyên tắc chính:

- Evaluation chỉ thực hiện khi Task đủ điều kiện.
- Actual Time và Actual Cost phải được kiểm tra hợp lệ.
- Planned Time và Planned Cost được dùng làm baseline.
- Nếu Planned thiếu, hệ thống không được tự suy diễn baseline khi chưa có policy.
- Evaluation phải tách Time và Money để tránh diễn giải sai.

### 8.2 Efficiency Evaluation Policy

Efficiency Evaluation Policy định nghĩa cách hiểu hiệu quả sử dụng nguồn lực.

Nguyên tắc chính:

- Efficiency không chỉ là dùng ít thời gian hoặc ít tiền.
- Efficiency cần xét Task có hoàn thành hay không, variance và context.
- Time Efficiency và Cost Efficiency nên được đánh giá riêng.
- Overall Efficiency chỉ nên hiển thị khi policy tổng hợp được phê duyệt.
- Nếu thiếu dữ liệu, chỉ số efficiency phải thể hiện trạng thái không đủ dữ liệu.

### 8.3 Variance Policy

Variance Policy định nghĩa cách xác định và diễn giải sai lệch.

Nguyên tắc chính:

- Variance = Actual - Planned.
- Positive Variance thể hiện vượt kế hoạch trong ngữ cảnh tiêu thụ nguồn lực.
- Negative Variance thể hiện tiết kiệm so với kế hoạch trong ngữ cảnh tiêu thụ nguồn lực.
- Variance phải được hiển thị riêng cho Time và Cost.
- Variance không tự động kết luận tốt hoặc xấu nếu policy chưa định nghĩa ngưỡng.

### 8.4 Performance Policy

Performance Policy định nghĩa cách đánh giá kết quả tổng thể.

Nguyên tắc chính:

- Performance có thể bao gồm Completion Rate, Planning Accuracy, Efficiency và Productivity.
- Performance phải được giới hạn trong phạm vi period hoặc filter được chọn.
- Performance không được dùng để so sánh User với User khác trong phạm vi hiện tại.
- Performance phải là thông tin hỗ trợ cải thiện cá nhân.

### 8.5 Reporting Policy

Reporting Policy định nghĩa loại report, phạm vi dữ liệu, filter, export và cách xử lý dữ liệu rỗng.

Nguyên tắc chính:

- Report chỉ hiển thị dữ liệu User được phép xem.
- Report phải phản ánh dữ liệu tại thời điểm truy xuất.
- Report phải thể hiện rõ period và filter áp dụng.
- Export chỉ được hỗ trợ với format được phê duyệt.
- Report không có dữ liệu phải hiển thị trạng thái rõ ràng.

### 8.6 History Policy

History Policy định nghĩa các thay đổi cần được ghi nhận.

Các hành động có thể cần ghi nhận:

- Record Actual Time.
- Update Actual Time.
- Record Actual Cost.
- Update Actual Cost.
- Evaluate Task.
- Re-evaluate Task.
- Finalize Evaluation.
- Export Report nếu policy yêu cầu.

### 8.7 Trend Analysis Policy

Trend Analysis Policy định nghĩa cách xem xu hướng theo thời gian.

Nguyên tắc chính:

- Trend cần có dữ liệu đủ qua nhiều period.
- Trend phải thể hiện period so sánh rõ ràng.
- Trend không được suy diễn nguyên nhân nếu không có dữ liệu hỗ trợ.
- Trend Score nếu có phải được định nghĩa ở mức KPI policy.

## 9. Workflows

### 9.1 Record Actual

#### Main Flow

1. User chọn Task đã đủ điều kiện ghi Actual.
2. User nhập Actual Time và Actual Cost nếu có.
3. Hệ thống kiểm tra Task đủ điều kiện.
4. Hệ thống kiểm tra Actual Time và Actual Cost hợp lệ.
5. Hệ thống ghi nhận Actual.
6. Hệ thống ghi History nếu policy yêu cầu.

#### Alternative Flow

- User chỉ nhập Actual Time.
- User chỉ nhập Actual Cost.
- User lưu Actual nhưng chưa finalize Evaluation nếu policy cho phép.

#### Exception Flow

- Task chưa Completed và policy không cho ghi Actual cuối cùng: hệ thống từ chối.
- Actual nhỏ hơn 0: hệ thống từ chối.
- User không có quyền với Task: hệ thống từ chối.

### 9.2 Evaluate Task

#### Main Flow

1. User yêu cầu đánh giá Task đã ghi Actual.
2. Hệ thống lấy Planned và Actual hợp lệ.
3. Hệ thống tính Time Variance và Cost Variance nếu đủ dữ liệu.
4. Hệ thống đánh giá Efficiency theo policy.
5. Hệ thống hiển thị kết quả Evaluation.
6. Hệ thống ghi nhận Evaluation History nếu policy yêu cầu.

#### Alternative Flow

- Nếu thiếu Planned Time hoặc Planned Cost, hệ thống đánh giá phần dữ liệu đủ điều kiện và hiển thị phần còn thiếu.
- Nếu User cập nhật Actual, hệ thống re-evaluate nếu policy cho phép.

#### Exception Flow

- Không đủ dữ liệu tối thiểu để evaluation: hệ thống hiển thị trạng thái không đủ dữ liệu.
- Evaluation đã finalized và không cho sửa: hệ thống từ chối re-evaluation.

### 9.3 Calculate Variance

#### Main Flow

1. Hệ thống xác định Planned value.
2. Hệ thống xác định Actual value.
3. Hệ thống kiểm tra cả hai giá trị hợp lệ.
4. Hệ thống xác định Variance = Actual - Planned.
5. Hệ thống phân loại Positive, Negative hoặc Zero Variance.

#### Alternative Flow

- Nếu Planned thiếu, hệ thống không tính variance và thông báo thiếu baseline.
- Nếu Actual thiếu, hệ thống không tính variance và thông báo thiếu actual.

#### Exception Flow

- Giá trị không hợp lệ: hệ thống không tính variance.

### 9.4 Generate Dashboard

#### Main Flow

1. User mở Dashboard.
2. User chọn period hoặc filter nếu cần.
3. Hệ thống kiểm tra phạm vi quyền.
4. Hệ thống tổng hợp KPI, statistics, variance và trend đủ dữ liệu.
5. Hệ thống hiển thị Dashboard.

#### Alternative Flow

- Dashboard hiển thị trạng thái rỗng nếu không có dữ liệu.
- User drill-down nếu policy cho phép.

#### Exception Flow

- Period không hợp lệ: hệ thống yêu cầu điều chỉnh.
- Dữ liệu không đủ để tính một KPI: hệ thống hiển thị không đủ dữ liệu cho KPI đó.

### 9.5 Generate Report

#### Main Flow

1. User chọn loại Report.
2. User chọn period và filter.
3. Hệ thống kiểm tra period và filter hợp lệ.
4. Hệ thống tổng hợp dữ liệu trong phạm vi quyền.
5. Hệ thống hiển thị Report.

#### Alternative Flow

- Report không có dữ liệu, hệ thống hiển thị trạng thái không có dữ liệu.
- User thay đổi filter để xem phạm vi khác.

#### Exception Flow

- Period không hợp lệ: hệ thống từ chối.
- Report quá lớn: hệ thống xử lý theo large report policy.

### 9.6 Export Report

#### Main Flow

1. User mở Report hợp lệ.
2. User chọn format export được hỗ trợ.
3. Hệ thống kiểm tra export policy và phạm vi quyền.
4. Hệ thống tạo bản export theo nội dung Report.
5. Hệ thống ghi History nếu policy yêu cầu.

#### Alternative Flow

- User chọn format khác nếu format đầu tiên không được hỗ trợ.

#### Exception Flow

- Format chưa được phê duyệt: hệ thống từ chối.
- Report không có dữ liệu và policy không cho export empty report: hệ thống từ chối.

### 9.7 View History

#### Main Flow

1. User truy cập History.
2. User chọn filter hoặc period.
3. Hệ thống kiểm tra phạm vi quyền.
4. Hệ thống hiển thị History liên quan đến Actual và Evaluation.

#### Alternative Flow

- Không có History phù hợp, hệ thống hiển thị trạng thái rỗng.

#### Exception Flow

- User yêu cầu History ngoài phạm vi quyền: hệ thống từ chối.

### 9.8 Compare Periods

#### Main Flow

1. User chọn hai hoặc nhiều period để so sánh.
2. Hệ thống kiểm tra period hợp lệ.
3. Hệ thống tổng hợp KPI cho từng period.
4. Hệ thống hiển thị khác biệt và trend nếu đủ dữ liệu.

#### Alternative Flow

- Một period không có dữ liệu, hệ thống hiển thị trạng thái thiếu dữ liệu cho period đó.

#### Exception Flow

- Period không hợp lệ hoặc không cùng phạm vi so sánh: hệ thống từ chối.

## 10. Use Case List

| Use Case ID | Use Case Name | Primary Actor | Summary |
|---|---|---|---|
| TER-UC-001 | Record Actual Time | User | Ghi Actual Time cho Task đủ điều kiện. |
| TER-UC-002 | Update Actual Time | User | Cập nhật Actual Time nếu policy cho phép. |
| TER-UC-003 | Record Actual Cost | User | Ghi Actual Cost cho Task đủ điều kiện. |
| TER-UC-004 | Update Actual Cost | User | Cập nhật Actual Cost nếu policy cho phép. |
| TER-UC-005 | View Planned vs Actual | User | Xem so sánh Planned và Actual. |
| TER-UC-006 | Calculate Variance | System | Tính Time và Cost Variance. |
| TER-UC-007 | Evaluate Resource Efficiency | System | Đánh giá efficiency theo policy. |
| TER-UC-008 | View Productivity Summary | User | Xem tóm tắt productivity. |
| TER-UC-009 | View Statistics | User | Xem daily, weekly, monthly, yearly statistics. |
| TER-UC-010 | View Resource Utilization | User | Xem mức sử dụng nguồn lực. |
| TER-UC-011 | View Dashboard | User | Xem Dashboard. |
| TER-UC-012 | View Report | User | Xem Report. |
| TER-UC-013 | Export Report | User | Xuất Report nếu policy cho phép. |
| TER-UC-014 | Search Report | User | Tìm kiếm report result. |
| TER-UC-015 | Filter Report | User | Lọc Report. |
| TER-UC-016 | View History | User | Xem lịch sử Actual và Evaluation. |
| TER-UC-017 | Compare Periods | User | So sánh các period. |
| TER-UC-018 | View Trend | User | Xem trend KPI hoặc variance. |
| TER-UC-019 | View Category Statistics | User | Xem thống kê theo Category. |
| TER-UC-020 | View Tag Statistics | User | Xem thống kê theo Tag. |
| TER-UC-021 | View Timeline Statistics | User | Xem thống kê theo Timeline data. |
| TER-UC-022 | View Personal KPI | User | Xem KPI cá nhân. |
| TER-UC-023 | Re-evaluate Task | User | Đánh giá lại sau khi Actual thay đổi. |
| TER-UC-024 | Validate Access Scope | System | Kiểm tra phạm vi quyền dữ liệu. |
| TER-UC-025 | Handle Empty Dashboard or Report | System | Xử lý trường hợp không có dữ liệu. |

## 11. Use Case Specification

### TER-UC-001 - Record Actual Time

| Field | Description |
|---|---|
| ID | TER-UC-001 |
| Description | User ghi nhận Actual Time cho Task đủ điều kiện. |
| Primary Actor | User |
| Trigger | User nhập Actual Time sau khi Task hoàn thành hoặc đủ điều kiện. |
| Preconditions | Task thuộc User và đáp ứng Evaluation Eligibility. |
| Main Flow | 1. User chọn Task. 2. User nhập Actual Time. 3. Hệ thống kiểm tra quyền và eligibility. 4. Hệ thống validate Actual Time. 5. Hệ thống ghi nhận Actual Time. |
| Alternative Flow | User lưu Actual Time nhưng chưa đánh giá nếu policy cho phép. |
| Exception Flow | Actual Time âm hoặc Task chưa đủ điều kiện dẫn đến từ chối. |
| Postconditions | Actual Time được ghi nhận nếu hợp lệ. |
| Business Rules | TER-BR-001, TER-BR-003, TER-BR-017 |

### TER-UC-002 - Update Actual Time

| Field | Description |
|---|---|
| ID | TER-UC-002 |
| Description | User cập nhật Actual Time đã ghi nếu policy cho phép. |
| Primary Actor | User |
| Trigger | User chỉnh sửa Actual Time. |
| Preconditions | Actual Time tồn tại và Evaluation chưa bị khóa hoặc policy cho phép mở lại. |
| Main Flow | 1. User nhập Actual Time mới. 2. Hệ thống validate. 3. Hệ thống cập nhật. 4. Hệ thống re-evaluate nếu policy yêu cầu. |
| Alternative Flow | Hệ thống yêu cầu lý do cập nhật nếu policy yêu cầu. |
| Exception Flow | Evaluation finalized hoặc Actual Time không hợp lệ dẫn đến từ chối. |
| Postconditions | Actual Time và Evaluation liên quan được cập nhật nếu hợp lệ. |
| Business Rules | TER-BR-003, TER-BR-035, TER-BR-036, TER-BR-037 |

### TER-UC-003 - Record Actual Cost

| Field | Description |
|---|---|
| ID | TER-UC-003 |
| Description | User ghi nhận Actual Cost cho Task đủ điều kiện. |
| Primary Actor | User |
| Trigger | User nhập Actual Cost. |
| Preconditions | Task thuộc User và đủ điều kiện ghi Actual. |
| Main Flow | 1. User chọn Task. 2. User nhập Actual Cost. 3. Hệ thống kiểm tra eligibility. 4. Hệ thống validate Actual Cost. 5. Hệ thống ghi nhận Actual Cost. |
| Alternative Flow | User không nhập Actual Cost nếu Task không phát sinh chi phí và policy cho phép. |
| Exception Flow | Actual Cost âm hoặc User không có quyền dẫn đến từ chối. |
| Postconditions | Actual Cost được ghi nhận nếu hợp lệ. |
| Business Rules | TER-BR-001, TER-BR-004, TER-BR-015 |

### TER-UC-004 - Update Actual Cost

| Field | Description |
|---|---|
| ID | TER-UC-004 |
| Description | User cập nhật Actual Cost nếu policy cho phép. |
| Primary Actor | User |
| Trigger | User chỉnh sửa Actual Cost. |
| Preconditions | Actual Cost tồn tại hoặc Task đủ điều kiện cập nhật. |
| Main Flow | 1. User nhập Actual Cost mới. 2. Hệ thống validate. 3. Hệ thống cập nhật. 4. Hệ thống re-evaluate nếu policy yêu cầu. |
| Alternative Flow | Hệ thống yêu cầu lý do nếu policy yêu cầu. |
| Exception Flow | Evaluation bị khóa hoặc Actual Cost không hợp lệ dẫn đến từ chối. |
| Postconditions | Actual Cost được cập nhật nếu hợp lệ. |
| Business Rules | TER-BR-004, TER-BR-035, TER-BR-036, TER-BR-037 |

### TER-UC-005 - View Planned vs Actual

| Field | Description |
|---|---|
| ID | TER-UC-005 |
| Description | User xem Planned Time/Cost so với Actual Time/Cost. |
| Primary Actor | User |
| Trigger | User mở phần Evaluation hoặc Report detail. |
| Preconditions | User có quyền xem dữ liệu. |
| Main Flow | 1. Hệ thống lấy Planned. 2. Hệ thống lấy Actual. 3. Hệ thống hiển thị so sánh. |
| Alternative Flow | Nếu thiếu Planned hoặc Actual, hệ thống hiển thị trạng thái thiếu dữ liệu. |
| Exception Flow | User không có quyền dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-005, TER-BR-006, TER-BR-017 |

### TER-UC-006 - Calculate Variance

| Field | Description |
|---|---|
| ID | TER-UC-006 |
| Description | Hệ thống tính Time Variance và Cost Variance khi có đủ dữ liệu. |
| Primary Actor | System |
| Trigger | User xem Evaluation, Dashboard hoặc Report. |
| Preconditions | Planned và Actual hợp lệ theo loại variance. |
| Main Flow | 1. Hệ thống xác định Planned. 2. Hệ thống xác định Actual. 3. Hệ thống tính Variance. 4. Hệ thống phân loại Positive, Negative hoặc Zero. |
| Alternative Flow | Nếu thiếu dữ liệu, hệ thống đánh dấu không đủ dữ liệu. |
| Exception Flow | Giá trị không hợp lệ dẫn đến không tính variance. |
| Postconditions | Variance được xác định hoặc đánh dấu thiếu dữ liệu. |
| Business Rules | TER-BR-008, TER-BR-009, TER-BR-010, TER-BR-011, TER-BR-012 |

### TER-UC-007 - Evaluate Resource Efficiency

| Field | Description |
|---|---|
| ID | TER-UC-007 |
| Description | Hệ thống đánh giá hiệu quả sử dụng nguồn lực theo policy. |
| Primary Actor | System |
| Trigger | User yêu cầu Evaluation hoặc xem KPI. |
| Preconditions | Có dữ liệu Planned/Actual đủ theo policy. |
| Main Flow | 1. Hệ thống xác định variance. 2. Hệ thống áp dụng Efficiency Policy. 3. Hệ thống xác định Time, Cost hoặc Overall Efficiency nếu đủ dữ liệu. |
| Alternative Flow | Hệ thống chỉ đánh giá một phần nếu thiếu Cost hoặc Time. |
| Exception Flow | Dữ liệu không đủ, hệ thống hiển thị không đủ dữ liệu. |
| Postconditions | Efficiency được hiển thị theo policy. |
| Business Rules | TER-BR-031, TER-BR-032, TER-BR-033, TER-BR-049 |

### TER-UC-008 - View Productivity Summary

| Field | Description |
|---|---|
| ID | TER-UC-008 |
| Description | User xem tóm tắt productivity cá nhân. |
| Primary Actor | User |
| Trigger | User mở productivity summary. |
| Preconditions | User có dữ liệu trong phạm vi được chọn hoặc hệ thống xử lý empty state. |
| Main Flow | 1. User chọn period. 2. Hệ thống tổng hợp KPI. 3. Hệ thống hiển thị summary. |
| Alternative Flow | Không đủ dữ liệu, hệ thống hiển thị trạng thái thiếu dữ liệu. |
| Exception Flow | Period không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-023, TER-BR-027, TER-BR-042 |

### TER-UC-009 - View Statistics

| Field | Description |
|---|---|
| ID | TER-UC-009 |
| Description | User xem Daily, Weekly, Monthly hoặc Yearly Statistics. |
| Primary Actor | User |
| Trigger | User chọn statistics period. |
| Preconditions | Period hợp lệ. |
| Main Flow | 1. User chọn period type. 2. Hệ thống kiểm tra period. 3. Hệ thống tổng hợp statistics. 4. Hệ thống hiển thị kết quả. |
| Alternative Flow | Không có dữ liệu, hệ thống hiển thị empty state. |
| Exception Flow | Period không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-023, TER-BR-024, TER-BR-041 |

### TER-UC-010 - View Resource Utilization

| Field | Description |
|---|---|
| ID | TER-UC-010 |
| Description | User xem mức sử dụng Time và Money. |
| Primary Actor | User |
| Trigger | User chọn Resource Utilization. |
| Preconditions | Có dữ liệu resource phù hợp hoặc empty state. |
| Main Flow | 1. User chọn period hoặc filter. 2. Hệ thống tổng hợp utilization theo Time và Money. 3. Hệ thống hiển thị kết quả. |
| Alternative Flow | Chỉ có Time hoặc chỉ có Money, hệ thống hiển thị phần đủ dữ liệu. |
| Exception Flow | Filter không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-048 |

### TER-UC-011 - View Dashboard

| Field | Description |
|---|---|
| ID | TER-UC-011 |
| Description | User xem Dashboard cá nhân. |
| Primary Actor | User |
| Trigger | User mở Dashboard. |
| Preconditions | User đã xác thực và có quyền xem dữ liệu của chính mình. |
| Main Flow | 1. User chọn period/filter. 2. Hệ thống kiểm tra access scope. 3. Hệ thống tổng hợp KPI và statistics. 4. Hệ thống hiển thị Dashboard. |
| Alternative Flow | Không có dữ liệu, hệ thống hiển thị empty dashboard. |
| Exception Flow | Period/filter không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-017, TER-BR-025, TER-BR-046 |

### TER-UC-012 - View Report

| Field | Description |
|---|---|
| ID | TER-UC-012 |
| Description | User xem Report theo loại được chọn. |
| Primary Actor | User |
| Trigger | User chọn loại Report. |
| Preconditions | User có quyền xem report và period hợp lệ. |
| Main Flow | 1. User chọn report type. 2. User chọn period/filter. 3. Hệ thống kiểm tra. 4. Hệ thống hiển thị Report. |
| Alternative Flow | Report không có dữ liệu, hệ thống hiển thị trạng thái rỗng. |
| Exception Flow | Report type hoặc period không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-018, TER-BR-021, TER-BR-026 |

### TER-UC-013 - Export Report

| Field | Description |
|---|---|
| ID | TER-UC-013 |
| Description | User xuất Report nếu export policy cho phép. |
| Primary Actor | User |
| Trigger | User chọn export. |
| Preconditions | Report hợp lệ và format export được phê duyệt. |
| Main Flow | 1. User chọn format. 2. Hệ thống kiểm tra policy và quyền. 3. Hệ thống tạo export theo nội dung report. |
| Alternative Flow | User chọn format khác nếu format không được hỗ trợ. |
| Exception Flow | Format không được phê duyệt hoặc report vượt policy dẫn đến từ chối. |
| Postconditions | Report được export nếu hợp lệ. |
| Business Rules | TER-BR-022, TER-BR-045 |

### TER-UC-014 - Search Report

| Field | Description |
|---|---|
| ID | TER-UC-014 |
| Description | User tìm kiếm report result hoặc report history theo tiêu chí. |
| Primary Actor | User |
| Trigger | User nhập tiêu chí tìm kiếm. |
| Preconditions | User có quyền xem report. |
| Main Flow | 1. User nhập tiêu chí. 2. Hệ thống kiểm tra phạm vi quyền. 3. Hệ thống trả kết quả phù hợp. |
| Alternative Flow | Không có kết quả, hệ thống hiển thị empty state. |
| Exception Flow | Tiêu chí không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-043 |

### TER-UC-015 - Filter Report

| Field | Description |
|---|---|
| ID | TER-UC-015 |
| Description | User lọc report theo period, Category, Tag, Resource hoặc KPI. |
| Primary Actor | User |
| Trigger | User chọn filter. |
| Preconditions | Filter thuộc tập tiêu chí được phê duyệt. |
| Main Flow | 1. User chọn filter. 2. Hệ thống kiểm tra filter. 3. Hệ thống cập nhật report theo filter. |
| Alternative Flow | User kết hợp nhiều filter nếu policy cho phép. |
| Exception Flow | Filter không hợp lệ dẫn đến từ chối. |
| Postconditions | Report hiển thị theo filter. |
| Business Rules | TER-BR-043 |

### TER-UC-016 - View History

| Field | Description |
|---|---|
| ID | TER-UC-016 |
| Description | User xem lịch sử Actual và Evaluation. |
| Primary Actor | User |
| Trigger | User mở History. |
| Preconditions | History policy cho phép xem và User có quyền. |
| Main Flow | 1. User chọn period/filter. 2. Hệ thống kiểm tra quyền. 3. Hệ thống hiển thị History. |
| Alternative Flow | Không có History, hệ thống hiển thị empty state. |
| Exception Flow | User truy cập ngoài phạm vi quyền dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-037, TER-BR-038 |

### TER-UC-017 - Compare Periods

| Field | Description |
|---|---|
| ID | TER-UC-017 |
| Description | User so sánh KPI và statistics giữa các period. |
| Primary Actor | User |
| Trigger | User chọn Compare Periods. |
| Preconditions | Period hợp lệ và có thể so sánh. |
| Main Flow | 1. User chọn periods. 2. Hệ thống kiểm tra. 3. Hệ thống tổng hợp KPI từng period. 4. Hệ thống hiển thị khác biệt. |
| Alternative Flow | Một period thiếu dữ liệu, hệ thống hiển thị thiếu dữ liệu cho period đó. |
| Exception Flow | Period không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-039 |

### TER-UC-018 - View Trend

| Field | Description |
|---|---|
| ID | TER-UC-018 |
| Description | User xem xu hướng KPI hoặc variance theo thời gian. |
| Primary Actor | User |
| Trigger | User chọn Trend. |
| Preconditions | Có đủ dữ liệu tối thiểu theo Trend Analysis Policy. |
| Main Flow | 1. User chọn chỉ số. 2. User chọn period range. 3. Hệ thống kiểm tra dữ liệu. 4. Hệ thống hiển thị trend. |
| Alternative Flow | Không đủ dữ liệu, hệ thống hiển thị trạng thái không đủ dữ liệu. |
| Exception Flow | Period range không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-040 |

### TER-UC-019 - View Category Statistics

| Field | Description |
|---|---|
| ID | TER-UC-019 |
| Description | User xem statistics theo Category. |
| Primary Actor | User |
| Trigger | User chọn Category Statistics. |
| Preconditions | Có dữ liệu Category hoặc empty state. |
| Main Flow | 1. User chọn period/filter. 2. Hệ thống tổng hợp theo Category. 3. Hệ thống hiển thị statistics. |
| Alternative Flow | Task không có Category được xử lý theo policy. |
| Exception Flow | Filter không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-028 |

### TER-UC-020 - View Tag Statistics

| Field | Description |
|---|---|
| ID | TER-UC-020 |
| Description | User xem statistics theo Tag. |
| Primary Actor | User |
| Trigger | User chọn Tag Statistics. |
| Preconditions | Có dữ liệu Tag hoặc empty state. |
| Main Flow | 1. User chọn period/filter. 2. Hệ thống tổng hợp theo Tag. 3. Hệ thống hiển thị statistics. |
| Alternative Flow | Task có nhiều Tag được tính theo Tag policy. |
| Exception Flow | Filter không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-029 |

### TER-UC-021 - View Timeline Statistics

| Field | Description |
|---|---|
| ID | TER-UC-021 |
| Description | User xem statistics liên quan đến Timeline. |
| Primary Actor | User |
| Trigger | User chọn Timeline Statistics. |
| Preconditions | Có dữ liệu Timeline từ module liên quan. |
| Main Flow | 1. User chọn period. 2. Hệ thống lấy Task có Timeline data trong phạm vi quyền. 3. Hệ thống tổng hợp statistics. |
| Alternative Flow | Không có Timeline data, hệ thống hiển thị empty state. |
| Exception Flow | Period không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-030 |

### TER-UC-022 - View Personal KPI

| Field | Description |
|---|---|
| ID | TER-UC-022 |
| Description | User xem KPI cá nhân. |
| Primary Actor | User |
| Trigger | User mở KPI view. |
| Preconditions | KPI policy được định nghĩa và User có dữ liệu hoặc empty state. |
| Main Flow | 1. User chọn KPI hoặc period. 2. Hệ thống kiểm tra dữ liệu. 3. Hệ thống hiển thị KPI. |
| Alternative Flow | KPI không đủ dữ liệu, hệ thống hiển thị trạng thái không đủ dữ liệu. |
| Exception Flow | KPI chưa được định nghĩa trong policy dẫn đến không hiển thị. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | TER-BR-031, TER-BR-042, TER-BR-047 |

### TER-UC-023 - Re-evaluate Task

| Field | Description |
|---|---|
| ID | TER-UC-023 |
| Description | User đánh giá lại Task sau khi Actual thay đổi nếu policy cho phép. |
| Primary Actor | User |
| Trigger | Actual Time hoặc Actual Cost được cập nhật. |
| Preconditions | Evaluation chưa bị khóa hoặc policy cho phép mở lại. |
| Main Flow | 1. Hệ thống nhận thay đổi Actual. 2. Hệ thống tính lại variance. 3. Hệ thống cập nhật efficiency. 4. Hệ thống ghi History nếu policy yêu cầu. |
| Alternative Flow | User xác nhận re-evaluation nếu policy yêu cầu. |
| Exception Flow | Evaluation finalized không cho sửa dẫn đến từ chối. |
| Postconditions | Evaluation được cập nhật nếu hợp lệ. |
| Business Rules | TER-BR-035, TER-BR-036, TER-BR-050 |

### TER-UC-024 - Validate Access Scope

| Field | Description |
|---|---|
| ID | TER-UC-024 |
| Description | Hệ thống kiểm tra phạm vi quyền trước khi hiển thị Dashboard, Report hoặc History. |
| Primary Actor | System |
| Trigger | Actor yêu cầu xem dữ liệu đánh giá. |
| Preconditions | Actor đã xác thực. |
| Main Flow | 1. Hệ thống xác định actor. 2. Hệ thống xác định phạm vi dữ liệu. 3. Hệ thống cho phép nếu actor có quyền. |
| Alternative Flow | Staff/Admin có quyền giới hạn nếu policy cho phép. |
| Exception Flow | Không có quyền dẫn đến từ chối. |
| Postconditions | Chỉ dữ liệu hợp lệ được hiển thị. |
| Business Rules | TER-BR-017, TER-BR-018, TER-BR-019, TER-BR-020 |

### TER-UC-025 - Handle Empty Dashboard or Report

| Field | Description |
|---|---|
| ID | TER-UC-025 |
| Description | Hệ thống xử lý Dashboard hoặc Report không có dữ liệu. |
| Primary Actor | System |
| Trigger | Dashboard hoặc Report được yêu cầu nhưng phạm vi không có dữ liệu. |
| Preconditions | Period/filter hợp lệ. |
| Main Flow | 1. Hệ thống xác định không có dữ liệu. 2. Hệ thống không tính KPI gây hiểu nhầm. 3. Hệ thống hiển thị trạng thái không có dữ liệu. |
| Alternative Flow | Hệ thống gợi ý User điều chỉnh period/filter nếu policy cho phép. |
| Exception Flow | Không áp dụng. |
| Postconditions | User hiểu rằng phạm vi chọn không có dữ liệu. |
| Business Rules | TER-BR-025, TER-BR-026, TER-BR-042 |

## 12. User Stories

| Story ID | User Story |
|---|---|
| TER-US-001 | As a User, I want to record Actual Time so that I can compare effort against my plan. |
| TER-US-002 | As a User, I want to record Actual Cost so that I can compare spending against my plan. |
| TER-US-003 | As a User, I want to update Actual values when allowed so that my evaluation remains accurate. |
| TER-US-004 | As a User, I want to view Planned vs Actual so that I can understand resource variance. |
| TER-US-005 | As a User, I want to see Time Variance so that I know whether I spent more or less time than planned. |
| TER-US-006 | As a User, I want to see Cost Variance so that I know whether I spent more or less money than planned. |
| TER-US-007 | As a User, I want to see Resource Efficiency so that I can understand whether my resource use was reasonable. |
| TER-US-008 | As a User, I want to see Completion Rate so that I know how much planned work I completed. |
| TER-US-009 | As a User, I want to see Productivity Summary so that I can review my performance quickly. |
| TER-US-010 | As a User, I want daily, weekly, monthly and yearly statistics so that I can review different periods. |
| TER-US-011 | As a User, I want to see Resource Utilization so that I understand how much Time and Money I consumed. |
| TER-US-012 | As a User, I want to view Dashboard so that I can monitor key indicators in one place. |
| TER-US-013 | As a User, I want to view Reports so that I can analyze my resource usage in detail. |
| TER-US-014 | As a User, I want to export Reports when allowed so that I can store or share my own analysis. |
| TER-US-015 | As a User, I want to search and filter Reports so that I can find relevant insights. |
| TER-US-016 | As a User, I want to view History so that I can trace changes in Actual and Evaluation. |
| TER-US-017 | As a User, I want to compare periods so that I can see whether I improved over time. |
| TER-US-018 | As a User, I want to view trends so that I can understand long-term patterns. |
| TER-US-019 | As a User, I want Category Statistics so that I can see which life areas consume resources. |
| TER-US-020 | As a User, I want Tag Statistics so that I can analyze flexible groupings of work. |
| TER-US-021 | As a User, I want Personal KPI so that I can improve my planning behavior. |
| TER-US-022 | As the system, I want to validate access scope so that users only see permitted data. |

## 13. Acceptance Criteria

| AC ID | Given | When | Then |
|---|---|---|---|
| TER-AC-001 | Given a Task is Completed and belongs to User | When User records Actual Time greater than or equal to 0 | Then the system accepts Actual Time. |
| TER-AC-002 | Given User records Actual Time | When Actual Time is negative | Then the system rejects the value. |
| TER-AC-003 | Given a Task is Completed and belongs to User | When User records Actual Cost greater than or equal to 0 | Then the system accepts Actual Cost. |
| TER-AC-004 | Given User records Actual Cost | When Actual Cost is negative | Then the system rejects the value. |
| TER-AC-005 | Given Task is not eligible for final Actual | When User attempts final Actual recording | Then the system rejects the action. |
| TER-AC-006 | Given Actual Time exists and policy allows update | When User updates Actual Time with a valid value | Then the system updates Actual Time. |
| TER-AC-007 | Given Actual Cost exists and policy allows update | When User updates Actual Cost with a valid value | Then the system updates Actual Cost. |
| TER-AC-008 | Given Evaluation is finalized and policy does not allow reopen | When User updates Actual | Then the system rejects the update. |
| TER-AC-009 | Given Planned and Actual are available | When User views Planned vs Actual | Then the system displays both values. |
| TER-AC-010 | Given Planned or Actual is missing | When User views Planned vs Actual | Then the system indicates insufficient data for the missing part. |
| TER-AC-011 | Given Planned Time and Actual Time are valid | When Time Variance is calculated | Then the system applies Variance = Actual - Planned. |
| TER-AC-012 | Given Planned Cost and Actual Cost are valid | When Cost Variance is calculated | Then the system applies Variance = Actual - Planned. |
| TER-AC-013 | Given Actual is greater than Planned | When Variance is shown | Then the system identifies Positive Variance. |
| TER-AC-014 | Given Actual is less than Planned | When Variance is shown | Then the system identifies Negative Variance. |
| TER-AC-015 | Given sufficient data exists | When User views Resource Efficiency | Then the system displays efficiency according to policy. |
| TER-AC-016 | Given insufficient data exists | When User views Resource Efficiency | Then the system displays insufficient data. |
| TER-AC-017 | Given User selects a valid period | When User views Daily Statistics | Then the system displays daily statistics. |
| TER-AC-018 | Given User selects a valid week | When User views Weekly Statistics | Then the system displays weekly statistics. |
| TER-AC-019 | Given User selects a valid month | When User views Monthly Statistics | Then the system displays monthly statistics. |
| TER-AC-020 | Given User selects a valid year | When User views Yearly Statistics | Then the system displays yearly statistics. |
| TER-AC-021 | Given User opens Dashboard | When data exists in selected scope | Then the system displays KPI and statistics within User scope. |
| TER-AC-022 | Given User opens Dashboard | When no data exists | Then the system displays an empty dashboard state. |
| TER-AC-023 | Given User selects a valid Report type and period | When User views Report | Then the system displays Report content. |
| TER-AC-024 | Given Report scope has no data | When User views Report | Then the system displays no-data state. |
| TER-AC-025 | Given Export Policy allows PDF | When User exports PDF | Then the system exports the Report in PDF format. |
| TER-AC-026 | Given Export Policy allows Excel | When User exports Excel | Then the system exports the Report in Excel format. |
| TER-AC-027 | Given Export Policy allows CSV | When User exports CSV | Then the system exports the Report in CSV format. |
| TER-AC-028 | Given export format is not approved | When User attempts export | Then the system rejects export. |
| TER-AC-029 | Given User searches Report with valid criteria | When matching results exist | Then the system displays matching results. |
| TER-AC-030 | Given User filters Report by Category | When data exists | Then the system displays Category-filtered results. |
| TER-AC-031 | Given User filters Report by Tag | When data exists | Then the system displays Tag-filtered results. |
| TER-AC-032 | Given User views History | When History exists in scope | Then the system displays History. |
| TER-AC-033 | Given User compares valid periods | When data exists | Then the system displays comparison results. |
| TER-AC-034 | Given Trend has enough data | When User views Trend | Then the system displays trend. |
| TER-AC-035 | Given Trend lacks enough data | When User views Trend | Then the system displays insufficient data. |
| TER-AC-036 | Given User views Category Statistics | When Category data exists | Then the system displays Category Statistics. |
| TER-AC-037 | Given User views Tag Statistics | When Tag data exists | Then the system displays Tag Statistics. |
| TER-AC-038 | Given User views Timeline Statistics | When Timeline data exists | Then the system displays Timeline Statistics. |
| TER-AC-039 | Given User views Personal KPI | When KPI data is sufficient | Then the system displays KPI values. |
| TER-AC-040 | Given User attempts to view another User's report | When request is submitted | Then the system denies access. |
| TER-AC-041 | Given Staff lacks policy permission | When Staff attempts to view User report | Then the system denies access. |
| TER-AC-042 | Given Admin lacks defined exception | When Admin attempts to view personal detail | Then the system denies access. |

## 14. Business Scenarios

| Scenario | Description | Expected Result |
|---|---|---|
| Task hoàn thành đúng kế hoạch | Planned Time bằng Actual Time và Planned Cost bằng Actual Cost. | Variance bằng 0 và efficiency được đánh giá theo policy. |
| Task vượt thời gian | Actual Time lớn hơn Planned Time. | Time Variance là Positive Variance và được hiển thị là vượt kế hoạch. |
| Task vượt chi phí | Actual Cost lớn hơn Planned Cost. | Cost Variance là Positive Variance và được hiển thị là vượt kế hoạch. |
| Task tiết kiệm thời gian | Actual Time nhỏ hơn Planned Time. | Time Variance là Negative Variance và được hiển thị là tiết kiệm thời gian. |
| Task tiết kiệm chi phí | Actual Cost nhỏ hơn Planned Cost. | Cost Variance là Negative Variance và được hiển thị là tiết kiệm chi phí. |
| Xem Dashboard | User mở Dashboard theo tháng hiện tại. | Dashboard hiển thị KPI và statistics trong phạm vi tháng. |
| So sánh tháng này với tháng trước | User chọn Compare Periods giữa hai tháng. | Hệ thống hiển thị khác biệt KPI nếu đủ dữ liệu. |
| Xem báo cáo theo Category | User chọn Category Report. | Hệ thống tổng hợp theo Category trong phạm vi quyền. |
| Xem báo cáo theo Tag | User chọn Tag Report. | Hệ thống tổng hợp theo Tag trong phạm vi quyền. |
| Xuất báo cáo | User mở Report và chọn export format được phê duyệt. | Report được export theo format được chọn. |
| Dashboard không có dữ liệu | User chọn period chưa có Task Completed. | Dashboard hiển thị trạng thái không có dữ liệu. |
| Task bị Reopen sau đánh giá | Task đã evaluated nhưng được reopen từ module Task. | Evaluation được xử lý theo Re-evaluation Policy. |

## 15. Edge Cases

| Edge Case ID | Scenario | Expected Business Handling |
|---|---|---|
| TER-EC-001 | Task chưa Completed nhưng nhập Actual. | Hệ thống từ chối final Actual nếu policy không cho phép. |
| TER-EC-002 | Actual Time nhỏ hơn 0. | Hệ thống từ chối. |
| TER-EC-003 | Actual Cost nhỏ hơn 0. | Hệ thống từ chối. |
| TER-EC-004 | Task không có Planned Time. | Hệ thống không tính Time Variance và hiển thị thiếu Planned Time. |
| TER-EC-005 | Task không có Planned Cost. | Hệ thống không tính Cost Variance và hiển thị thiếu Planned Cost. |
| TER-EC-006 | Task không có Actual Cost. | Hệ thống không tính Cost Variance nếu policy không xem thiếu Actual Cost là 0. |
| TER-EC-007 | Task không có Actual Time. | Hệ thống không tính Time Variance. |
| TER-EC-008 | Task bị Reopen sau khi đã đánh giá. | Hệ thống xử lý theo Re-evaluation Policy. |
| TER-EC-009 | Dashboard không có dữ liệu. | Hệ thống hiển thị empty state. |
| TER-EC-010 | Khoảng thời gian thống kê không có Task. | Report/Statistics hiển thị không có dữ liệu. |
| TER-EC-011 | Báo cáo quá lớn. | Hệ thống xử lý theo Large Report Policy. |
| TER-EC-012 | User chọn start period sau end period. | Hệ thống từ chối period. |
| TER-EC-013 | User export report không có dữ liệu. | Hệ thống xử lý theo Export Policy. |
| TER-EC-014 | User xem trend nhưng chỉ có một period. | Hệ thống hiển thị không đủ dữ liệu. |
| TER-EC-015 | Task có nhiều Tag. | Tag Statistics xử lý theo Tag Policy cần xác nhận. |
| TER-EC-016 | Task không có Category. | Category Statistics xử lý nhóm uncategorized nếu policy cho phép. |
| TER-EC-017 | Actual được cập nhật sau report đã xem. | Report mới phản ánh dữ liệu tại thời điểm truy xuất mới. |
| TER-EC-018 | Staff cố xem report cá nhân. | Hệ thống từ chối nếu không có policy. |
| TER-EC-019 | Admin cố sửa Actual của User. | Hệ thống từ chối nếu không có ngoại lệ rõ. |
| TER-EC-020 | Planned Time bằng 0. | Time Efficiency xử lý theo policy để tránh diễn giải sai. |
| TER-EC-021 | Planned Cost bằng 0. | Cost Efficiency xử lý theo policy để tránh diễn giải sai. |
| TER-EC-022 | Actual Time bằng 0 cho Task Completed. | Hệ thống chấp nhận nếu policy cho phép và đánh giá theo context. |
| TER-EC-023 | Actual Cost bằng 0. | Hệ thống chấp nhận nếu Task không phát sinh chi phí hoặc policy cho phép. |
| TER-EC-024 | KPI thiếu dữ liệu. | Hệ thống hiển thị không đủ dữ liệu, không tự suy diễn. |
| TER-EC-025 | Export format chưa được phê duyệt. | Hệ thống từ chối export. |

## 16. Validation Rules

| Validation Rule ID | Rule |
|---|---|
| TER-VR-001 | Actual Time phải lớn hơn hoặc bằng 0. |
| TER-VR-002 | Actual Cost phải lớn hơn hoặc bằng 0. |
| TER-VR-003 | Không được ghi nhận Actual cuối cùng nếu Task chưa đủ điều kiện theo nghiệp vụ. |
| TER-VR-004 | User chỉ được ghi Actual cho Task thuộc phạm vi quyền. |
| TER-VR-005 | Planned Time phải tồn tại và hợp lệ để tính Time Variance. |
| TER-VR-006 | Actual Time phải tồn tại và hợp lệ để tính Time Variance. |
| TER-VR-007 | Planned Cost phải tồn tại và hợp lệ để tính Cost Variance. |
| TER-VR-008 | Actual Cost phải tồn tại và hợp lệ để tính Cost Variance. |
| TER-VR-009 | Khoảng thời gian thống kê phải hợp lệ. |
| TER-VR-010 | Start period không được sau end period. |
| TER-VR-011 | Report type phải thuộc danh sách được phê duyệt. |
| TER-VR-012 | Export format phải thuộc danh sách được phê duyệt. |
| TER-VR-013 | Filter Category phải hợp lệ nếu được sử dụng. |
| TER-VR-014 | Filter Tag phải hợp lệ nếu được sử dụng. |
| TER-VR-015 | Compare Periods phải dùng các period có thể so sánh theo policy. |
| TER-VR-016 | Trend Analysis phải có đủ số period tối thiểu theo policy. |
| TER-VR-017 | KPI chỉ được hiển thị khi đủ dữ liệu tối thiểu theo policy. |
| TER-VR-018 | Hành động bị từ chối không được thay đổi Actual hoặc Evaluation. |
| TER-VR-019 | User không được truy cập report ngoài phạm vi quyền. |
| TER-VR-020 | Staff/Admin không được truy cập dữ liệu cá nhân nếu không có policy rõ. |

## 17. KPI Definitions

| KPI | Business Definition | Business Interpretation |
|---|---|---|
| Resource Utilization Rate | Mức độ nguồn lực Time hoặc Money được sử dụng trong phạm vi đánh giá so với nguồn lực planned hoặc allocated theo policy. | Giúp User hiểu mức độ sử dụng nguồn lực đã lên kế hoạch. |
| Task Completion Rate | Tỷ lệ Task Completed trên tổng Task trong phạm vi đánh giá được chọn. | Cho biết mức độ hoàn thành kế hoạch công việc. |
| Planning Accuracy | Mức độ Planned gần với Actual đối với Time hoặc Cost. | Cho biết User ước lượng nguồn lực chính xác đến đâu. |
| Time Efficiency | Mức độ sử dụng thời gian hợp lý so với Planned Time và trạng thái hoàn thành. | Giúp User hiểu việc sử dụng thời gian có phù hợp kế hoạch không. |
| Cost Efficiency | Mức độ sử dụng tiền bạc hợp lý so với Planned Cost và trạng thái hoàn thành. | Giúp User hiểu việc sử dụng chi phí có phù hợp kế hoạch không. |
| Overall Efficiency | Chỉ số tổng hợp từ Time Efficiency, Cost Efficiency và policy đánh giá. | Cung cấp góc nhìn tổng quát nhưng cần diễn giải cẩn trọng. |
| Productivity Score | Chỉ số tổng hợp phản ánh completion, planning accuracy, efficiency và trend nếu policy được phê duyệt. | Hỗ trợ User tự đánh giá năng suất cá nhân, không phải phán xét tuyệt đối. |
| Variance Rate | Mức độ sai lệch giữa Actual và Planned được chuẩn hóa theo policy. | Giúp so sánh sai lệch giữa các Task hoặc period. |
| Trend Score | Chỉ số phản ánh hướng cải thiện hoặc suy giảm của KPI theo nhiều period nếu policy được phê duyệt. | Giúp User nhận biết xu hướng cải thiện kế hoạch theo thời gian. |

## 18. Dashboard Requirements

### 18.1 Purpose

Dashboard cung cấp góc nhìn tổng quan giúp User nhanh chóng hiểu hiệu quả sử dụng nguồn lực và productivity trong phạm vi thời gian được chọn. Dashboard không thay thế Report chi tiết mà đóng vai trò điểm vào để User phát hiện vấn đề, xu hướng và khu vực cần xem sâu hơn.

### 18.2 Target User

Đối tượng sử dụng chính là User. Staff hoặc Admin chỉ được xem Dashboard cá nhân của User nếu có policy quyền truy cập rõ ràng.

### 18.3 Information to Display

| Information Group | Description |
|---|---|
| Completion Summary | Completion Rate, số Task completed, số Task chưa completed nếu thuộc phạm vi dữ liệu. |
| Planned vs Actual Summary | Tổng quan Planned Time/Cost và Actual Time/Cost nếu đủ dữ liệu. |
| Variance Summary | Time Variance, Cost Variance và phân loại vượt hoặc tiết kiệm. |
| Efficiency Summary | Time Efficiency, Cost Efficiency và Overall Efficiency nếu policy cho phép. |
| Resource Utilization | Mức sử dụng Time và Money theo period. |
| Productivity Summary | Productivity Score hoặc thông tin productivity nếu KPI policy được phê duyệt. |
| Trend Snapshot | Xu hướng chính so với period trước nếu đủ dữ liệu. |
| Attention Items | Các khu vực cần chú ý như variance cao, thiếu actual hoặc không đủ dữ liệu. |

### 18.4 Filters

Dashboard có thể hỗ trợ các bộ lọc sau nếu policy cho phép:

- Period: daily, weekly, monthly, yearly, custom.
- Category.
- Tag.
- Resource type: Time, Money hoặc cả hai.
- Task status liên quan đến evaluation.
- Evaluation status.

### 18.5 Statistical Periods

Dashboard phải phân biệt rõ period thống kê:

- Daily.
- Weekly.
- Monthly.
- Yearly.
- Custom period nếu policy cho phép.

### 18.6 Drill-down

Drill-down là khả năng đi từ chỉ số tổng quan xuống dữ liệu chi tiết hơn. Nếu được phê duyệt, drill-down có thể áp dụng cho:

- KPI.
- Variance.
- Category.
- Tag.
- Resource utilization.
- Trend.

Drill-down chỉ được hiển thị dữ liệu trong phạm vi quyền của User.

## 19. Report Requirements

| Report Type | Purpose | Main Content |
|---|---|---|
| Daily Report | Giúp User xem kết quả sử dụng nguồn lực trong một ngày. | Task completed, Actual Time, Actual Cost, variance, efficiency, missing actual nếu có. |
| Weekly Report | Giúp User xem hiệu quả theo tuần. | Completion Rate, resource utilization, planned vs actual, category/tag summary, trend trong tuần. |
| Monthly Report | Giúp User đánh giá kế hoạch và hiệu quả theo tháng. | KPI tháng, variance tổng hợp, category/tag statistics, comparison với tháng trước nếu chọn. |
| Yearly Report | Giúp User xem bức tranh dài hạn. | KPI năm, trend theo tháng, resource utilization dài hạn, planning accuracy. |
| Category Report | Giúp User hiểu resource consumption theo nhóm hoạt động. | Planned/Actual theo Category, variance, efficiency, completion. |
| Tag Report | Giúp User phân tích Task theo nhãn linh hoạt. | Planned/Actual theo Tag, variance, completion, distribution. |
| Resource Report | Tập trung vào Time và Money usage. | Time consumption, cost consumption, utilization, variance, efficiency. |
| Productivity Report | Đánh giá productivity cá nhân. | Completion Rate, Planning Accuracy, Efficiency, Productivity Score nếu có. |
| History Report | Cung cấp lịch sử Actual và Evaluation. | Actual changes, evaluation changes, timestamps ở mức nghiệp vụ nếu policy yêu cầu. |
| Trend Report | Phân tích xu hướng qua nhiều period. | KPI trend, variance trend, resource utilization trend, improvement signals. |

## 20. Risks

### 20.1 Business Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| User không nhập Actual | Evaluation và report thiếu dữ liệu. | Dashboard kém giá trị. | Cần truyền đạt lợi ích của Actual recording. |
| KPI bị hiểu như phán xét tuyệt đối | User có thể hiểu Productivity Score là đánh giá cá nhân toàn diện. | Gây áp lực hoặc diễn giải sai. | Giải thích KPI là công cụ hỗ trợ cải thiện. |
| Efficiency bị hiểu sai | User nghĩ dùng ít nguồn lực luôn tốt. | Đánh giá sai Task quan trọng. | Efficiency Policy phải nhấn mạnh context. |
| Planned thiếu nhiều | Variance không tính được. | Report không đầy đủ. | Hiển thị thiếu dữ liệu rõ ràng. |

### 20.2 Reporting Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Report không phản ánh filter rõ | User hiểu sai phạm vi dữ liệu. | Quyết định sai. | Report phải hiển thị period và filter áp dụng. |
| Report quá lớn | User khó đọc hoặc report bị giới hạn. | Giảm usability. | Large Report Policy cần được xác định. |
| Category/Tag không nhất quán | Thống kê bị phân tán. | Insight yếu. | Cần hướng dẫn phân loại ở module liên quan. |
| Trend thiếu dữ liệu | Trend dễ gây hiểu nhầm. | Kết luận sai. | Chỉ hiển thị trend khi đủ dữ liệu. |

### 20.3 Decision-making Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| User ra quyết định dựa trên dữ liệu thiếu | KPI thiếu Actual hoặc Planned. | Điều chỉnh kế hoạch sai. | Hiển thị data completeness. |
| So sánh period không tương đương | Tháng có ít Task so với tháng khác. | Diễn giải sai improvement. | Compare Periods phải nêu rõ phạm vi so sánh. |
| Negative Variance bị xem luôn là tốt | Dùng ít nguồn lực có thể do Task làm chưa đầy đủ. | Đánh giá sai hiệu quả. | Policy cần kết hợp completion và context. |

### 20.4 Operational Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Staff/Admin truy cập dữ liệu cá nhân quá rộng | Ảnh hưởng quyền riêng tư. | Giảm niềm tin User. | Áp dụng access scope chặt chẽ. |
| History không đủ để giải thích thay đổi | User không biết vì sao KPI thay đổi. | Giảm tin cậy. | Ghi nhận thay đổi Actual/Evaluation theo policy. |
| Export report chứa dữ liệu ngoài phạm vi | Rủi ro quyền riêng tư. | Vi phạm access control. | Export phải tuân thủ cùng scope với report. |

## 21. Open Questions

| Question ID | Open Question | Impact Area |
|---|---|---|
| TER-OQ-001 | Task chưa Completed có được ghi Actual tạm thời không? | Tracking |
| TER-OQ-002 | Actual Time bằng 0 có hợp lệ trong mọi trường hợp không? | Validation |
| TER-OQ-003 | Actual Cost thiếu có được hiểu là 0 không, hay là thiếu dữ liệu? | Cost Evaluation |
| TER-OQ-004 | Planned Time bằng 0 được xử lý thế nào khi tính variance hoặc efficiency? | Variance |
| TER-OQ-005 | Planned Cost bằng 0 được xử lý thế nào khi tính variance hoặc efficiency? | Variance |
| TER-OQ-006 | Evaluation có trạng thái Draft/Finalized không? | Evaluation Lifecycle |
| TER-OQ-007 | User có được sửa Actual sau khi finalized không? | Evaluation Policy |
| TER-OQ-008 | Reopen Task sau evaluation sẽ giữ, hủy hay đánh dấu lại Evaluation? | Re-evaluation |
| TER-OQ-009 | Công thức chính thức cho Productivity Score là gì? | KPI |
| TER-OQ-010 | Ngưỡng đánh giá Efficiency gồm những mức nào? | KPI |
| TER-OQ-011 | Completion Rate tính trên all planned Task hay chỉ Task đến hạn trong period? | KPI |
| TER-OQ-012 | Trend cần tối thiểu bao nhiêu period để hiển thị? | Trend |
| TER-OQ-013 | Report export có thuộc phạm vi release hiện tại không? | Export |
| TER-OQ-014 | Các format export được phê duyệt là gì? | Export |
| TER-OQ-015 | Staff có được xem report cá nhân trong trường hợp hỗ trợ không? | Access |
| TER-OQ-016 | Admin được xem dữ liệu tổng hợp ở mức nào? | Access |
| TER-OQ-017 | History cần ghi nhận những thông tin tối thiểu nào? | History |
| TER-OQ-018 | Large Report được định nghĩa theo số lượng Task, period hay dung lượng nội dung? | Reporting |
| TER-OQ-019 | Category/Tag Statistics xử lý Task không có Category/Tag như thế nào? | Statistics |
| TER-OQ-020 | Dashboard có hỗ trợ drill-down trong release hiện tại không? | Dashboard |

## 22. Suggested Improvements

| Improvement ID | Suggested Improvement | Business Rationale |
|---|---|---|
| TER-SI-001 | Hiển thị mức độ đầy đủ dữ liệu trước khi tính KPI. | Giúp User hiểu KPI đáng tin đến đâu. |
| TER-SI-002 | Gợi ý User ghi Actual sau khi Task Completed. | Tăng chất lượng Evaluation và Report. |
| TER-SI-003 | Cung cấp nhãn diễn giải variance theo ngữ cảnh. | Tránh hiểu sai Positive/Negative Variance. |
| TER-SI-004 | Cho phép User thêm ghi chú khi Actual khác Planned nhiều. | Hỗ trợ học hỏi và cải thiện kế hoạch. |
| TER-SI-005 | Tách rõ Time Efficiency và Cost Efficiency trước khi tổng hợp Overall Efficiency. | Tránh đánh giá gộp gây hiểu nhầm. |
| TER-SI-006 | Cung cấp trend về Planning Accuracy. | Giúp User biết khả năng ước lượng có cải thiện không. |
| TER-SI-007 | Cung cấp report về Task thiếu Actual. | Giúp User hoàn thiện dữ liệu đánh giá. |
| TER-SI-008 | Cung cấp so sánh period có cảnh báo khác biệt phạm vi dữ liệu. | Tránh so sánh thiếu công bằng. |
| TER-SI-009 | Xem xét export chỉ cho dữ liệu đã lọc hiện tại. | Giảm rủi ro xuất dữ liệu ngoài ý muốn. |
| TER-SI-010 | Cung cấp glossary KPI trong Dashboard và Report. | Giúp User hiểu đúng ý nghĩa chỉ số. |
| TER-SI-011 | Cung cấp insight theo Category và Tag thường gây variance cao. | Hỗ trợ cải thiện lập kế hoạch theo nhóm công việc. |
| TER-SI-012 | Định nghĩa confidence level cho KPI nếu dữ liệu thiếu. | Giúp User không ra quyết định quá mức dựa trên dữ liệu yếu. |

## Appendix A. Traceability Summary

| Source | Related TER Content |
|---|---|
| Volume 1 - Product Philosophy | Planned vs Actual, efficiency và continuous improvement là nền tảng của TER. |
| Volume 2 - Identity & Authorization | Dashboard, Report và History phải tuân thủ access scope. |
| Volume 3 - Resource Capital Management | Planned Time/Cost và resource terminology được sử dụng làm baseline đánh giá. |
| Volume 4 - Task & Timeline Management | Completed Task, Category, Tag, Timeline data là đầu vào cho TER. |

## Appendix B. TER Glossary

| Term | Definition |
|---|---|
| Actual Cost | Chi phí thực tế được ghi nhận cho Task đủ điều kiện. |
| Actual Time | Thời gian thực tế được ghi nhận cho Task đủ điều kiện. |
| Dashboard | Góc nhìn tổng quan về KPI, statistics, variance và efficiency. |
| Efficiency | Mức độ sử dụng nguồn lực hợp lý so với kế hoạch và kết quả. |
| History | Lịch sử thay đổi Actual, Evaluation hoặc report-related action nếu policy yêu cầu. |
| KPI | Chỉ số nghiệp vụ dùng để đánh giá completion, accuracy, utilization hoặc productivity. |
| Planned Cost | Chi phí dự kiến của Task trước khi thực hiện. |
| Planned Time | Thời gian dự kiến của Task trước khi thực hiện. |
| Report | Tập thông tin có cấu trúc phục vụ phân tích và đánh giá. |
| Resource Utilization | Mức độ sử dụng Time hoặc Money trong phạm vi đánh giá. |
| Statistics | Thống kê định lượng theo period hoặc filter. |
| Trend | Xu hướng thay đổi của KPI hoặc chỉ số qua thời gian. |
| Variance | Sai lệch giữa Actual và Planned. |

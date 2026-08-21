# LifeBalance
# Volume 4 - Task & Timeline Management SRS

## 1. Module Overview

### 1.1 Purpose

Task & Timeline Management là module chịu trách nhiệm quản lý công việc và lịch thực hiện công việc của người dùng trong LifeBalance. Trong định hướng sản phẩm của LifeBalance, Task là đơn vị trung tâm của hệ thống vì mọi hoạt động lập kế hoạch, cấp nguồn lực, thực hiện và đánh giá đều bắt đầu từ một công việc cụ thể. Một Task đại diện cho một công việc, hoạt động hoặc cam kết cá nhân mà User dự định thực hiện.

Module này hỗ trợ User tạo Task, lập kế hoạch cho Task, gán mức ưu tiên, gán deadline, gán Category, gắn Tag, ước lượng Time Capital và Money Capital cần thiết, đưa Task lên Timeline khi có Time Capital, thay đổi lịch thực hiện trên Timeline, cập nhật tiến độ, hoàn thành, hủy, mở lại, lưu trữ và khôi phục Task theo các quy tắc nghiệp vụ được xác định.

Timeline trong phạm vi tài liệu này được hiểu là góc nhìn theo thời gian đối với các Task đã có Time Capital hoặc đã được lên lịch thực hiện. Timeline không hiển thị mọi Task trong hệ thống; Timeline chỉ hiển thị các Task đủ điều kiện xuất hiện trên lịch, đặc biệt là Task có Time Capital hoặc lịch thực hiện hợp lệ theo chính sách được phê duyệt.

Tài liệu này không mô tả Dashboard, Reporting, Tracking, Evaluation, quản lý nguồn vốn chi tiết hoặc quản trị hệ thống. Các thuật ngữ Time Capital và Money Capital được sử dụng như điều kiện nghiệp vụ liên quan đến Task Planning và Scheduling, nhưng chi tiết quản lý nguồn vốn thuộc Volume 3.

### 1.2 Objectives

| Objective ID | Objective | Description |
|---|---|---|
| TTM-OBJ-001 | Quản lý Task cá nhân | Cho phép User tạo, cập nhật, xem, tìm kiếm, lọc và quản lý Task thuộc sở hữu của chính mình. |
| TTM-OBJ-002 | Hỗ trợ Task Planning | Cho phép User xác định thông tin kế hoạch của Task như deadline, priority, category, tag, estimated time và estimated cost. |
| TTM-OBJ-003 | Hỗ trợ Task Scheduling | Cho phép User lên lịch thực hiện Task khi Task có điều kiện thời gian phù hợp. |
| TTM-OBJ-004 | Quản lý Timeline | Cho phép User xem Timeline và điều chỉnh lịch thực hiện của Task theo quy tắc nghiệp vụ. |
| TTM-OBJ-005 | Hỗ trợ Drag & Drop Timeline | Cho phép User kéo thả Task trên Timeline để thay đổi lịch thực hiện nếu hành động hợp lệ. |
| TTM-OBJ-006 | Quản lý Task Progress | Cho phép User cập nhật tiến độ Task từ 0% đến 100%. |
| TTM-OBJ-007 | Quản lý trạng thái Task | Hỗ trợ vòng đời Task từ Draft đến Planned, Scheduled, In Progress, On Hold, Completed, Cancelled và Archived nếu phù hợp. |
| TTM-OBJ-008 | Bảo vệ tính nhất quán kế hoạch | Bảo đảm các thay đổi về deadline, schedule, progress và completion tuân thủ business rule. |
| TTM-OBJ-009 | Chuẩn bị đầu vào cho Tracking & Evaluation | Bảo đảm Task hoàn thành có đủ dữ liệu nghiệp vụ nền tảng để các module sau xử lý, nhưng không mô tả chi tiết Tracking & Evaluation. |
| TTM-OBJ-010 | Bảo vệ quyền sở hữu dữ liệu | Bảo đảm User chỉ quản lý Task và Timeline của chính mình, trừ khi có quyền truy cập khác được định nghĩa rõ. |

### 1.3 Business Value

Task & Timeline Management mang lại giá trị nghiệp vụ trực tiếp cho LifeBalance vì Task là điểm kết nối giữa mục tiêu cá nhân, nguồn lực dự kiến và hành động thực tế. Nếu không có Task được định nghĩa rõ, người dùng không thể lập kế hoạch nguồn lực có ý nghĩa.

Giá trị thứ nhất là giúp User chuyển ý định thành kế hoạch có cấu trúc. Một ý định như "học tiếng Anh" hoặc "chuẩn bị hồ sơ" trở thành Task có tên, mô tả, deadline, priority, category, tag và ước lượng nguồn lực.

Giá trị thứ hai là hỗ trợ User nhìn thấy cam kết thời gian. Khi Task có Time Capital và được lên lịch, Task xuất hiện trên Timeline để User hiểu công việc sẽ diễn ra vào thời điểm nào, có bị chồng lịch hay không và có cần điều chỉnh hay không.

Giá trị thứ ba là tăng kỷ luật thực hiện. Việc cập nhật progress, pause, resume, complete hoặc cancel Task giúp User duy trì trạng thái công việc rõ ràng thay vì chỉ có danh sách việc cần làm không có vòng đời.

Giá trị thứ tư là hỗ trợ ra quyết định ưu tiên. Priority, deadline, category và tag giúp User tổ chức Task theo mức độ quan trọng, thời hạn và ngữ cảnh sử dụng.

Giá trị thứ năm là tạo dữ liệu đầu vào có cấu trúc cho các module khác. Task sau khi hoàn thành có thể trở thành đầu vào cho Tracking & Evaluation, nhưng việc đánh giá hiệu quả nằm ngoài phạm vi tài liệu này.

### 1.4 Responsibilities

| Responsibility | Description |
|---|---|
| Task Creation | Cho phép User tạo Task mới với thông tin bắt buộc và thông tin kế hoạch tùy chọn. |
| Task Planning | Hỗ trợ xác định priority, deadline, estimated time, estimated cost, category và tag. |
| Task Scheduling | Hỗ trợ đặt lịch thực hiện Task khi Task đủ điều kiện xuất hiện trên Timeline. |
| Timeline Management | Hỗ trợ xem và điều chỉnh Task theo thời gian. |
| Drag & Drop Handling | Hỗ trợ kéo thả Task trên Timeline và kiểm tra tính hợp lệ của thay đổi lịch. |
| Task Progress Management | Hỗ trợ cập nhật tiến độ Task và trạng thái liên quan. |
| Task Status Management | Hỗ trợ thay đổi trạng thái Task theo lifecycle được phê duyệt. |
| Task Classification | Hỗ trợ gán Category và nhiều Tag cho Task. |
| Task Search and Filter | Hỗ trợ tìm kiếm, lọc và sắp xếp Task theo tiêu chí nghiệp vụ. |
| Task Completion Control | Hỗ trợ hoàn thành Task và khóa một số thay đổi kế hoạch sau khi hoàn thành nếu không reopen. |
| Task Archive Control | Hỗ trợ archive và restore Task theo chính sách. |
| Ownership Validation | Bảo đảm User chỉ thao tác với Task thuộc quyền sở hữu của mình. |

## 2. Business Scope

### 2.1 In Scope

| Scope Area | Description |
|---|---|
| Task | Quản lý đơn vị công việc cá nhân của User. |
| Task Planning | Lập kế hoạch Task với deadline, priority, estimated time, estimated cost, category và tag. |
| Task Scheduling | Đặt lịch thực hiện Task dựa trên Time Capital hoặc thông tin thời gian hợp lệ. |
| Timeline | Hiển thị Task đủ điều kiện theo thời gian. |
| Drag & Drop | Cho phép User kéo thả Task trên Timeline để thay đổi lịch thực hiện. |
| Task Progress | Cập nhật tiến độ Task theo phần trăm hoặc quy tắc được phê duyệt. |
| Priority | Gán và cập nhật mức ưu tiên của Task. |
| Category | Gán Task vào một Category nếu Category được sử dụng trong phạm vi sản phẩm. |
| Tag | Gắn một hoặc nhiều Tag cho Task để phân loại linh hoạt. |
| Task Status | Quản lý trạng thái Task trong lifecycle. |
| Task Completion | Hoàn thành Task và chuẩn bị dữ liệu cho giai đoạn sau. |
| Task Search | Tìm kiếm Task theo từ khóa hoặc tiêu chí được phê duyệt. |
| Task Filter | Lọc Task theo status, priority, deadline, category, tag, schedule hoặc tiêu chí khác trong phạm vi module. |
| Task Sort | Sắp xếp Task theo deadline, priority, status, created time, scheduled time hoặc tiêu chí được phê duyệt. |
| Recurring Task | Hỗ trợ Task lặp lại nếu chính sách được xác nhận. |
| Task Archive | Lưu trữ và khôi phục Task để giảm nhiễu trong danh sách làm việc hiện tại. |
| Task Reminder | Được xem là phạm vi tùy chọn nếu chính sách nhắc việc được phê duyệt. |

### 2.2 Out of Scope

| Out of Scope Area | Explanation |
|---|---|
| Dashboard | Không mô tả màn hình tổng quan, chỉ số tổng hợp hoặc widget. |
| Reporting | Không mô tả báo cáo phân tích, thống kê hoặc biểu đồ. |
| Tracking & Evaluation | Không mô tả ghi nhận thực tế và đánh giá hiệu quả sau hoàn thành. |
| Resource Capital Management chi tiết | Không mô tả tạo chu kỳ vốn, điều chỉnh vốn, số dư vốn hoặc lịch sử vốn. |
| Administration | Không mô tả quản trị hệ thống, quản lý người dùng hoặc vận hành nền tảng. |
| Quản lý Category chi tiết | Chỉ mô tả việc gán Category cho Task, không mô tả vòng đời Category. |
| Quản lý Tag chi tiết | Chỉ mô tả việc gắn và gỡ Tag khỏi Task, không mô tả vòng đời Tag. |
| Tự động tối ưu lịch | Không bao gồm tự động sắp xếp lịch thông minh nếu chưa được phê duyệt. |
| Cộng tác nhóm | Task trong phạm vi này là Task cá nhân, không mô tả phân công nhóm. |
| Giao diện cụ thể | Không mô tả wireframe, layout hoặc thiết kế màn hình. |

### 2.3 Dependencies

| Dependency | Description | Impact |
|---|---|---|
| Volume 1 - Vision & Business Overview | Cung cấp triết lý Task là khoản đầu tư nguồn lực và nguồn lực hữu hạn. | Task phải hỗ trợ lập kế hoạch nguồn lực và lifecycle phù hợp. |
| Volume 2 - Identity & Authorization | Cung cấp xác thực, ownership và phân quyền actor. | User chỉ được quản lý Task của chính mình. |
| Volume 3 - Resource Capital Management | Cung cấp khái niệm Time Capital và Money Capital. | Task Scheduling và Timeline phụ thuộc vào Time Capital ở mức điều kiện nghiệp vụ. |
| Category capability | Cung cấp danh sách Category hợp lệ nếu Task cần gán Category. | Task chỉ được gán Category hợp lệ theo ownership và policy. |
| Tag capability | Cung cấp Tag hợp lệ nếu User sử dụng tag. | Task có thể gắn nhiều Tag hợp lệ theo policy. |
| Tracking & Evaluation | Nhận Task hoàn thành làm đầu vào ở giai đoạn sau. | Module này chỉ chuẩn bị trạng thái Completed và dữ liệu nền tảng. |
| Reminder policy | Cần xác nhận Task Reminder có thuộc release hiện tại hay không. | Ảnh hưởng đến yêu cầu reminder và acceptance criteria liên quan. |
| Recurring policy | Cần xác nhận Task lặp lại có được hỗ trợ chính thức hay không. | Ảnh hưởng đến lifecycle, scheduling và edge case. |

## 3. Business Concepts

### 3.1 Task

Task là đơn vị công việc cá nhân mà User dự định thực hiện. Một Task có thể đại diện cho một việc ngắn hạn, một hoạt động có deadline, một cam kết sử dụng nguồn lực hoặc một nhiệm vụ cần được theo dõi tiến độ. Task là điểm khởi đầu cho việc lập kế hoạch, cấp nguồn lực, thực hiện và đánh giá trong LifeBalance.

Trong phạm vi tài liệu này, Task có thể tồn tại ở nhiều trạng thái khác nhau như Draft, Planned, Scheduled, In Progress, On Hold, Completed, Cancelled và Archived. Task phải thuộc một User cụ thể và không được quản lý bởi User khác nếu không có quyền hợp lệ.

### 3.2 Task Plan

Task Plan là tập hợp thông tin kế hoạch của Task trước khi thực hiện. Task Plan có thể bao gồm tên Task, mô tả, priority, deadline, estimated time, estimated cost, category, tag và các thông tin khác được phê duyệt trong phạm vi module.

Task Plan giúp User hiểu công việc cần làm, mức độ quan trọng, thời hạn và nguồn lực dự kiến. Task Plan không đồng nghĩa với Task đã được lên Timeline. Một Task có thể được Planned nhưng chưa Scheduled.

### 3.3 Task Schedule

Task Schedule là thông tin xác định thời điểm Task dự kiến được thực hiện. Task Schedule có thể bao gồm ngày, khoảng thời gian hoặc vị trí trên Timeline theo chính sách sản phẩm. Task chỉ nên được scheduled khi có đủ điều kiện thời gian, đặc biệt là Time Capital hoặc estimated time hợp lệ.

Task Schedule giúp Task xuất hiện trên Timeline và cho phép User điều chỉnh lịch thực hiện.

### 3.4 Timeline

Timeline là góc nhìn theo thời gian hiển thị các Task đủ điều kiện scheduling. Theo nguyên tắc nghiệp vụ của LifeBalance, Timeline chỉ hiển thị Task có Time Capital hoặc thông tin thời gian tương đương được chính sách cho phép. Timeline hỗ trợ User hiểu lịch thực hiện, nhận diện chồng lịch và thay đổi lịch bằng thao tác hợp lệ như drag & drop.

Timeline không phải dashboard và không phải reporting. Timeline chỉ phục vụ quản lý lịch thực hiện Task.

### 3.5 Priority

Priority là mức độ ưu tiên của Task, giúp User xác định Task nào cần được chú ý trước. Priority có thể được phân loại theo tập giá trị được phê duyệt, ví dụ Low, Medium, High, Critical hoặc hệ thống giá trị khác.

Priority không tự động thay đổi deadline hoặc schedule nếu chưa có policy. Priority là thông tin kế hoạch hỗ trợ lọc, sắp xếp và ra quyết định.

### 3.6 Deadline

Deadline là thời hạn mà Task cần được hoàn thành hoặc xử lý. Deadline có thể tồn tại độc lập với schedule. Một Task có deadline nhưng chưa được scheduled vẫn có thể là Planned Task.

Deadline không được nhỏ hơn ngày bắt đầu hoặc thời điểm scheduled start nếu quy tắc nghiệp vụ yêu cầu. Nếu deadline đã qua, Task cần được thể hiện như quá hạn hoặc cần hành động xử lý theo policy.

### 3.7 Estimated Time

Estimated Time là lượng Time Capital hoặc thời lượng dự kiến cần để thực hiện Task. Estimated Time là điều kiện quan trọng để Task xuất hiện trên Timeline. Nếu Task không có Time Capital hoặc estimated time hợp lệ, Task không được hiển thị trên Timeline theo business context đã xác định.

Estimated Time là giá trị kế hoạch, không phải thời gian thực tế. Việc ghi nhận thời gian thực tế thuộc phạm vi khác.

### 3.8 Estimated Cost

Estimated Cost là lượng Money Capital hoặc chi phí dự kiến cần cho Task. Estimated Cost giúp User nhận thức nguồn lực tiền bạc cần thiết trước khi thực hiện Task.

Estimated Cost là giá trị kế hoạch, không phải chi phí thực tế. Việc ghi nhận chi phí thực tế và đánh giá sai lệch thuộc phạm vi khác.

### 3.9 Progress

Progress là mức độ hoàn thành của Task trong quá trình thực hiện. Progress được biểu diễn theo phần trăm từ 0% đến 100% nếu chính sách không quy định cách khác.

Progress bằng 100% có thể là điều kiện để hoàn thành Task, nhưng việc tự động chuyển sang Completed hay yêu cầu User xác nhận cần được xác định theo policy.

### 3.10 Completion

Completion là trạng thái Task đã được User xác nhận hoàn thành. Completed Task được xem là kết thúc giai đoạn thực hiện trong phạm vi Task & Timeline Management và có thể trở thành đầu vào cho Tracking & Evaluation.

Sau khi Task Completed, việc thay đổi thông tin planning cần bị hạn chế. Nếu User muốn sửa kế hoạch sau completion, Task nên được reopen theo quy trình được phê duyệt.

### 3.11 Category

Category là phân loại có cấu trúc dùng để nhóm Task theo ý nghĩa nghiệp vụ như học tập, công việc, sức khỏe, tài chính hoặc phát triển cá nhân. Trong module này, Category chỉ được phân tích ở khía cạnh gán vào Task.

Một Task có thể không có Category nếu policy cho phép. Nếu policy yêu cầu Category bắt buộc, Task không được chuyển sang Planned hoặc Scheduled nếu chưa có Category hợp lệ.

### 3.12 Tag

Tag là nhãn linh hoạt dùng để phân loại, lọc và tìm kiếm Task. Một Task có thể có nhiều Tag. Tag có thể hỗ trợ User mô tả ngữ cảnh, mức năng lượng, địa điểm, dự án cá nhân hoặc nhóm ý nghĩa khác.

Trong module này, Tag chỉ được phân tích ở khía cạnh gắn và gỡ khỏi Task.

### 3.13 Recurring Task

Recurring Task là Task lặp lại theo quy luật nhất định, ví dụ hằng ngày, hằng tuần hoặc hằng tháng. Recurring Task có thể tạo ra các occurrence theo chính sách được phê duyệt.

Recurring Task là phạm vi tùy chọn. Nếu chưa được phê duyệt, Recurring Task chỉ được ghi nhận như Open Question hoặc Future Improvement, không được hiểu là yêu cầu bắt buộc.

### 3.14 Task Status

Task Status là trạng thái nghiệp vụ của Task trong lifecycle. Task Status phản ánh vị trí của Task trong quá trình từ khởi tạo đến hoàn thành, hủy hoặc lưu trữ. Status giúp User và hệ thống xác định hành động nào được phép.

### 3.15 Milestone

Milestone là điểm mốc quan trọng trong một kế hoạch hoặc chuỗi Task. Trong phạm vi hiện tại, Milestone chưa được xác định là yêu cầu chính thức. Nếu LifeBalance cần hỗ trợ Milestone, cần xác nhận trong Open Questions và volume sau.

## 4. Actors

### 4.1 User

| Attribute | Description |
|---|---|
| Responsibilities | Tạo, lập kế hoạch, lên lịch, cập nhật tiến độ, hoàn thành, hủy, archive, restore, tìm kiếm, lọc và quản lý Task của chính mình. |
| Permissions | Create Task; Update Own Task; Delete Own Task; Archive Own Task; Restore Own Task; Duplicate Own Task; Plan Own Task; Schedule Own Task; Move Own Timeline Task; Update Own Progress; Complete Own Task; Cancel Own Task; Reopen Own Task; View Own Timeline; Search Own Task; Filter Own Task. |
| Limitations | User chỉ được quản lý Task và Timeline của chính mình. User không được thay đổi Task của người khác. User không được bypass điều kiện Time Capital để đưa Task lên Timeline nếu policy không cho phép. |

User là actor chính của module. Mọi hành động nghiệp vụ trong Task & Timeline Management chủ yếu phục vụ User quản lý công việc cá nhân và lịch thực hiện của chính mình.

### 4.2 Staff

| Attribute | Description |
|---|---|
| Responsibilities | Hỗ trợ User khi có vấn đề sử dụng liên quan đến Task hoặc Timeline nếu chính sách hỗ trợ cho phép. |
| Permissions | Không mặc định có quyền tạo, sửa, xóa, schedule hoặc hoàn thành Task của User. Có thể xem thông tin giới hạn phục vụ hỗ trợ nếu được phân quyền rõ ràng. |
| Limitations | Staff không được chỉnh sửa Task, Timeline, deadline, progress hoặc status của User nếu không có trường hợp ngoại lệ được định nghĩa rõ. |

Staff không phải chủ sở hữu Task của User. Quyền của Staff trong module này cần được giới hạn để tránh can thiệp vào kế hoạch cá nhân.

### 4.3 Admin

| Attribute | Description |
|---|---|
| Responsibilities | Quản trị chính sách chung nếu được phê duyệt; bảo đảm module vận hành theo rule; không can thiệp tùy ý vào Task cá nhân của User. |
| Permissions | Không mặc định quản lý nội dung Task cá nhân của User. Có thể xem hoặc xử lý thông tin ở phạm vi chính sách nếu được định nghĩa rõ. |
| Limitations | Admin không thay đổi kế hoạch, schedule, progress hoặc completion của Task cá nhân nếu không có chính sách ngoại lệ, lý do hợp lệ và quyền rõ ràng. |

Admin trong module này được hiểu là actor quản trị chính sách và vận hành, không phải người quản lý công việc cá nhân của User.

## 5. Task Lifecycle

### 5.1 Lifecycle Overview

Task Lifecycle mô tả các trạng thái nghiệp vụ mà một Task có thể trải qua. Lifecycle giúp xác định hành động nào được phép tại từng thời điểm và bảo đảm quá trình quản lý Task nhất quán.

Lifecycle tham chiếu:

Draft

↓

Planned

↓

Scheduled

↓

In Progress

↓

On Hold

↓

Completed

↓

Archived

Ngoài luồng chính, Task có thể chuyển sang Cancelled từ một số trạng thái nếu User quyết định không tiếp tục thực hiện. Task đã Completed hoặc Cancelled có thể được Reopen nếu policy cho phép.

### 5.2 Draft

Draft là trạng thái Task mới được tạo nhưng chưa đủ thông tin kế hoạch để được xem là Planned. Task ở trạng thái Draft có thể chỉ có Task Name và một số thông tin ban đầu.

Hành động thường được phép:

- Cập nhật Task.
- Thêm mô tả.
- Gán priority, deadline, category hoặc tag.
- Ước lượng Time Capital hoặc Money Capital.
- Xóa Task nếu policy cho phép.

Điều kiện chuyển sang Planned:

- Task có đủ thông tin bắt buộc theo Task Planning Policy.
- Task không vi phạm validation rule.

### 5.3 Planned

Planned là trạng thái Task đã có thông tin kế hoạch đủ để User hiểu cần làm gì, tại sao quan trọng và cần nguồn lực dự kiến nào. Task Planned có thể chưa được đặt lịch trên Timeline.

Hành động thường được phép:

- Cập nhật kế hoạch.
- Gán hoặc thay đổi priority.
- Gán deadline.
- Gán category và tag.
- Ước lượng time hoặc cost.
- Schedule Task nếu có Time Capital hợp lệ.
- Cancel Task nếu không còn cần thực hiện.

Điều kiện chuyển sang Scheduled:

- Task có Time Capital hoặc estimated time hợp lệ.
- Task có lịch thực hiện hợp lệ.

### 5.4 Scheduled

Scheduled là trạng thái Task đã được đặt vào lịch thực hiện và đủ điều kiện xuất hiện trên Timeline. Scheduled Task phải có thông tin thời gian phù hợp, bao gồm scheduled start hoặc scheduled period theo policy.

Hành động thường được phép:

- Reschedule Task.
- Move Task trên Timeline.
- Drag & Drop Task nếu thay đổi hợp lệ.
- Cập nhật một số thông tin planning nếu policy cho phép.
- Start Task để chuyển sang In Progress.
- Cancel Task nếu User không tiếp tục.

Điều kiện chuyển sang In Progress:

- User bắt đầu thực hiện Task.
- Task chưa bị cancel, archive hoặc vi phạm trạng thái.

### 5.5 In Progress

In Progress là trạng thái Task đang được thực hiện. User có thể cập nhật progress, pause, complete hoặc cancel Task theo rule.

Hành động thường được phép:

- Update Progress.
- Pause Task.
- Complete Task.
- Cancel Task nếu không tiếp tục.
- Cập nhật một số thông tin không phá vỡ lịch sử kế hoạch nếu policy cho phép.

Điều kiện chuyển sang On Hold:

- User tạm dừng Task.

Điều kiện chuyển sang Completed:

- User xác nhận hoàn thành.
- Progress đạt điều kiện completion nếu policy yêu cầu.

### 5.6 On Hold

On Hold là trạng thái Task bị tạm dừng. Task chưa hoàn thành nhưng User tạm thời không tiếp tục thực hiện. On Hold khác Cancelled vì Task vẫn có thể được resume.

Hành động thường được phép:

- Resume Task.
- Cập nhật kế hoạch nếu policy cho phép.
- Reschedule Task nếu cần.
- Cancel Task nếu không còn thực hiện.

Điều kiện chuyển sang In Progress:

- User resume Task.

### 5.7 Completed

Completed là trạng thái Task đã được User xác nhận hoàn thành. Task Completed có thể trở thành đầu vào cho module Tracking & Evaluation. Trong phạm vi tài liệu này, Completed chỉ xác định trạng thái kết thúc thực hiện, không mô tả đánh giá sau đó.

Hành động thường được phép:

- View Task Detail.
- Archive Task.
- Reopen Task nếu policy cho phép.

Hạn chế:

- Không được thay đổi planning quan trọng nếu không reopen.
- Không được kéo thả trên Timeline như Task đang lên lịch nếu policy không cho phép.

### 5.8 Cancelled

Cancelled là trạng thái Task bị hủy trước khi hoàn thành. Cancelled Task thể hiện User quyết định không tiếp tục thực hiện Task.

Hành động thường được phép:

- View Task Detail.
- Archive Task.
- Reopen Task nếu policy cho phép.

Hạn chế:

- Không được tiếp tục cập nhật progress như Task đang thực hiện.
- Không được scheduled lại nếu chưa reopen.

### 5.9 Archived

Archived là trạng thái Task được lưu trữ để không còn xuất hiện trong danh sách làm việc chính hoặc Timeline chính theo policy. Archive không đồng nghĩa xóa vĩnh viễn.

Hành động thường được phép:

- View archived Task.
- Restore Task nếu policy cho phép.

Hạn chế:

- Archived Task không nên xuất hiện trên Timeline chính.
- Archived Task không nên được cập nhật planning nếu chưa restore.

### 5.10 Transition Conditions

| From Status | To Status | Condition |
|---|---|---|
| Draft | Planned | Task đáp ứng thông tin planning bắt buộc. |
| Planned | Scheduled | Task có Time Capital hoặc estimated time hợp lệ và schedule hợp lệ. |
| Scheduled | In Progress | User bắt đầu thực hiện Task. |
| In Progress | On Hold | User tạm dừng Task. |
| On Hold | In Progress | User tiếp tục Task. |
| In Progress | Completed | User xác nhận hoàn thành và progress đáp ứng rule. |
| Draft/Planned/Scheduled/In Progress/On Hold | Cancelled | User hủy Task theo policy. |
| Completed/Cancelled | Archived | User lưu trữ Task. |
| Archived | Restored Status | User khôi phục Task theo policy về trạng thái trước archive hoặc trạng thái được xác định. |
| Completed/Cancelled | Reopened Status | User reopen Task nếu policy cho phép. |

## 6. Functional Requirements

| Requirement ID | Requirement Name | Description | Primary Actor |
|---|---|---|---|
| TTM-FR-001 | Create Task | Hệ thống phải cho phép User tạo Task mới thuộc sở hữu của chính mình. | User |
| TTM-FR-002 | Validate Task Name | Hệ thống phải kiểm tra Task Name là thông tin bắt buộc và hợp lệ. | System |
| TTM-FR-003 | Update Task | Hệ thống phải cho phép User cập nhật Task của chính mình khi trạng thái Task cho phép. | User |
| TTM-FR-004 | Delete Task | Hệ thống phải cho phép User xóa Task của chính mình nếu Task ở trạng thái được phép xóa theo policy. | User |
| TTM-FR-005 | Archive Task | Hệ thống phải cho phép User archive Task của chính mình theo policy. | User |
| TTM-FR-006 | Restore Task | Hệ thống phải cho phép User restore Task đã archived nếu policy cho phép. | User |
| TTM-FR-007 | Duplicate Task | Hệ thống phải cho phép User duplicate Task của chính mình để tạo Task mới dựa trên thông tin Task hiện có. | User |
| TTM-FR-008 | View Task Detail | Hệ thống phải cho phép User xem chi tiết Task của chính mình. | User |
| TTM-FR-009 | Search Task | Hệ thống phải cho phép User tìm kiếm Task của chính mình theo từ khóa hoặc tiêu chí được phê duyệt. | User |
| TTM-FR-010 | Filter Task | Hệ thống phải cho phép User lọc Task theo status, priority, deadline, category, tag, schedule hoặc tiêu chí được phê duyệt. | User |
| TTM-FR-011 | Sort Task | Hệ thống phải cho phép User sắp xếp Task theo tiêu chí được phê duyệt. | User |
| TTM-FR-012 | Assign Category | Hệ thống phải cho phép User gán một Category hợp lệ cho Task của chính mình. | User |
| TTM-FR-013 | Change Category | Hệ thống phải cho phép User thay đổi Category của Task khi Task ở trạng thái cho phép. | User |
| TTM-FR-014 | Remove Category | Hệ thống phải cho phép User gỡ Category khỏi Task nếu policy cho phép Task không có Category. | User |
| TTM-FR-015 | Assign Tag | Hệ thống phải cho phép User gắn một hoặc nhiều Tag hợp lệ cho Task. | User |
| TTM-FR-016 | Remove Tag | Hệ thống phải cho phép User gỡ Tag khỏi Task. | User |
| TTM-FR-017 | Validate Tag Limit | Hệ thống phải kiểm tra số lượng Tag trên Task không vượt giới hạn nếu policy có quy định. | System |
| TTM-FR-018 | Set Priority | Hệ thống phải cho phép User gán priority cho Task. | User |
| TTM-FR-019 | Update Priority | Hệ thống phải cho phép User cập nhật priority khi Task ở trạng thái cho phép. | User |
| TTM-FR-020 | Validate Priority | Hệ thống phải kiểm tra priority thuộc tập giá trị được phê duyệt. | System |
| TTM-FR-021 | Set Deadline | Hệ thống phải cho phép User đặt deadline cho Task. | User |
| TTM-FR-022 | Change Deadline | Hệ thống phải cho phép User thay đổi deadline khi Task ở trạng thái cho phép. | User |
| TTM-FR-023 | Validate Deadline | Hệ thống phải kiểm tra deadline hợp lệ theo rule. | System |
| TTM-FR-024 | Estimate Time | Hệ thống phải cho phép User nhập estimated time hoặc Time Capital dự kiến cho Task. | User |
| TTM-FR-025 | Estimate Cost | Hệ thống phải cho phép User nhập estimated cost hoặc Money Capital dự kiến cho Task. | User |
| TTM-FR-026 | Validate Estimated Time | Hệ thống phải kiểm tra estimated time hợp lệ và lớn hơn 0 nếu dùng để schedule. | System |
| TTM-FR-027 | Validate Estimated Cost | Hệ thống phải kiểm tra estimated cost không âm. | System |
| TTM-FR-028 | Plan Task | Hệ thống phải cho phép User chuyển Task sang Planned khi Task đáp ứng thông tin planning bắt buộc. | User |
| TTM-FR-029 | Schedule Task | Hệ thống phải cho phép User schedule Task có Time Capital hoặc estimated time hợp lệ. | User |
| TTM-FR-030 | Reschedule Task | Hệ thống phải cho phép User thay đổi lịch thực hiện của Scheduled Task. | User |
| TTM-FR-031 | View Timeline | Hệ thống phải cho phép User xem Timeline chứa Task đủ điều kiện của chính mình. | User |
| TTM-FR-032 | Timeline Eligibility Validation | Hệ thống phải bảo đảm chỉ Task có Time Capital hoặc estimated time hợp lệ mới xuất hiện trên Timeline. | System |
| TTM-FR-033 | Move Timeline Task | Hệ thống phải cho phép User di chuyển Task trên Timeline nếu thay đổi hợp lệ. | User |
| TTM-FR-034 | Drag & Drop Timeline | Hệ thống phải cho phép User kéo thả Task trên Timeline để thay đổi schedule nếu rule cho phép. | User |
| TTM-FR-035 | Validate Timeline Move | Hệ thống phải kiểm tra việc di chuyển Task trên Timeline không vi phạm deadline, cycle, ownership hoặc policy khác. | System |
| TTM-FR-036 | Detect Timeline Conflict | Hệ thống phải nhận diện trường hợp Task bị kéo chồng lên Task khác nếu policy không cho phép chồng lịch. | System |
| TTM-FR-037 | Handle Timeline Conflict | Hệ thống phải xử lý xung đột Timeline theo policy: cảnh báo, từ chối hoặc cho phép có xác nhận. | System |
| TTM-FR-038 | Update Progress | Hệ thống phải cho phép User cập nhật progress của Task đang ở trạng thái phù hợp. | User |
| TTM-FR-039 | Validate Progress | Hệ thống phải kiểm tra progress nằm trong khoảng 0% đến 100%. | System |
| TTM-FR-040 | Pause Task | Hệ thống phải cho phép User tạm dừng Task đang In Progress. | User |
| TTM-FR-041 | Resume Task | Hệ thống phải cho phép User tiếp tục Task đang On Hold. | User |
| TTM-FR-042 | Complete Task | Hệ thống phải cho phép User hoàn thành Task khi điều kiện completion được đáp ứng. | User |
| TTM-FR-043 | Completion Validation | Hệ thống phải kiểm tra Task đủ điều kiện chuyển sang Completed. | System |
| TTM-FR-044 | Cancel Task | Hệ thống phải cho phép User hủy Task theo policy. | User |
| TTM-FR-045 | Reopen Task | Hệ thống phải cho phép User reopen Task đã Completed hoặc Cancelled nếu policy cho phép. | User |
| TTM-FR-046 | Lock Completed Task Planning | Hệ thống phải ngăn thay đổi planning quan trọng của Completed Task nếu Task chưa được reopen. | System |
| TTM-FR-047 | Recurring Task | Hệ thống phải hỗ trợ tạo Recurring Task nếu chính sách recurring được phê duyệt. | User |
| TTM-FR-048 | Generate Recurring Occurrence | Hệ thống phải hỗ trợ tạo occurrence từ Recurring Task theo policy nếu recurring được phê duyệt. | System |
| TTM-FR-049 | Validate Recurring Rule | Hệ thống phải kiểm tra recurring rule hợp lệ nếu recurring được sử dụng. | System |
| TTM-FR-050 | Task Reminder | Hệ thống phải hỗ trợ reminder cho Task nếu reminder thuộc phạm vi được phê duyệt. | User |
| TTM-FR-051 | Validate Reminder | Hệ thống phải kiểm tra reminder time hợp lệ và gắn với Task hợp lệ nếu reminder được sử dụng. | System |
| TTM-FR-052 | Task Ownership Validation | Hệ thống phải kiểm tra User chỉ thao tác với Task thuộc sở hữu của chính mình. | System |
| TTM-FR-053 | Task Status Validation | Hệ thống phải kiểm tra trạng thái Task trước khi cho phép cập nhật, schedule, progress, complete, cancel, archive hoặc restore. | System |
| TTM-FR-054 | Staff Access Control | Hệ thống phải ngăn Staff chỉnh sửa Task cá nhân của User nếu không có policy cho phép rõ ràng. | Staff |
| TTM-FR-055 | Admin Access Control | Hệ thống phải ngăn Admin thay đổi Task cá nhân của User nếu không có policy ngoại lệ được định nghĩa rõ. | Admin |
| TTM-FR-056 | Task Change History | Hệ thống phải ghi nhận các thay đổi quan trọng của Task để phục vụ truy vết nghiệp vụ nếu policy yêu cầu. | System |
| TTM-FR-057 | Timeline Change History | Hệ thống phải ghi nhận các thay đổi lịch thực hiện quan trọng nếu policy yêu cầu. | System |
| TTM-FR-058 | Cancel Timeline Placement | Hệ thống phải xử lý vị trí Timeline của Task khi Task bị cancel theo policy. | System |
| TTM-FR-059 | Archive Timeline Placement | Hệ thống phải xử lý việc Task archived không xuất hiện trên Timeline chính theo policy. | System |
| TTM-FR-060 | Restore Timeline Placement | Hệ thống phải xử lý Task restored có quay lại Timeline hay không theo policy. | System |

## 7. Non-functional Requirements

### 7.1 Performance

| NFR ID | Requirement | Description |
|---|---|---|
| TTM-NFR-PER-001 | Task List Response | Danh sách Task phải phản hồi trong thời gian phù hợp với nhu cầu quản lý cá nhân. |
| TTM-NFR-PER-002 | Timeline Response | Timeline phải hiển thị Task đủ điều kiện trong thời gian phù hợp để User lập lịch. |
| TTM-NFR-PER-003 | Drag & Drop Feedback | Khi User kéo thả Task, hệ thống phải phản hồi đủ nhanh để tránh nhầm lẫn thao tác. |
| TTM-NFR-PER-004 | Search and Filter Response | Tìm kiếm, lọc và sắp xếp Task phải có thời gian phản hồi phù hợp với khối lượng Task cá nhân thông thường. |

### 7.2 Availability

| NFR ID | Requirement | Description |
|---|---|---|
| TTM-NFR-AVL-001 | Task Access Availability | User cần có khả năng truy cập Task khi lập kế hoạch hoặc cập nhật công việc. |
| TTM-NFR-AVL-002 | Timeline Availability | Timeline cần khả dụng khi User sắp xếp lịch thực hiện. |
| TTM-NFR-AVL-003 | Status Update Availability | Cập nhật progress, pause, resume, complete và cancel cần khả dụng trong quá trình thực hiện. |

### 7.3 Usability

| NFR ID | Requirement | Description |
|---|---|---|
| TTM-NFR-USA-001 | Clear Status Meaning | User phải hiểu rõ ý nghĩa từng trạng thái Task. |
| TTM-NFR-USA-002 | Clear Timeline Eligibility | User phải hiểu vì sao một Task xuất hiện hoặc không xuất hiện trên Timeline. |
| TTM-NFR-USA-003 | Drag & Drop Clarity | Khi kéo thả Timeline, User phải thấy rõ thay đổi lịch và cảnh báo nếu có xung đột. |
| TTM-NFR-USA-004 | Planning Clarity | Các trường planning như deadline, priority, estimated time và estimated cost phải có ý nghĩa rõ ràng. |
| TTM-NFR-USA-005 | Error Clarity | Lỗi validation phải được truyền đạt rõ nhưng không gây rối hoặc hiểu sai. |

### 7.4 Security

| NFR ID | Requirement | Description |
|---|---|---|
| TTM-NFR-SEC-001 | Ownership Protection | Task và Timeline cá nhân phải được bảo vệ theo ownership của User. |
| TTM-NFR-SEC-002 | Authorized Access | Chỉ actor có quyền mới được xem hoặc thao tác Task. |
| TTM-NFR-SEC-003 | Staff Limitation | Staff không được can thiệp Task cá nhân nếu chưa có policy rõ. |
| TTM-NFR-SEC-004 | Admin Limitation | Admin không được thay đổi kế hoạch Task cá nhân nếu không có ngoại lệ được phê duyệt. |

### 7.5 Reliability

| NFR ID | Requirement | Description |
|---|---|---|
| TTM-NFR-REL-001 | Status Reliability | Trạng thái Task phải phản ánh đúng lifecycle và hành động đã thực hiện. |
| TTM-NFR-REL-002 | Timeline Reliability | Timeline phải phản ánh đúng Task đủ điều kiện và schedule hợp lệ. |
| TTM-NFR-REL-003 | Progress Reliability | Progress phải nhất quán với trạng thái Task và completion rule. |
| TTM-NFR-REL-004 | Recurring Reliability | Nếu recurring được hỗ trợ, occurrence phải tuân thủ recurring rule đã được phê duyệt. |

### 7.6 Auditability

| NFR ID | Requirement | Description |
|---|---|---|
| TTM-NFR-AUD-001 | Task Change Traceability | Các thay đổi quan trọng của Task phải có khả năng truy vết nếu policy yêu cầu. |
| TTM-NFR-AUD-002 | Timeline Change Traceability | Thay đổi schedule qua drag & drop hoặc reschedule phải có khả năng truy vết nếu policy yêu cầu. |
| TTM-NFR-AUD-003 | Completion Traceability | Hoàn thành, hủy, reopen, archive và restore Task nên được ghi nhận để phục vụ kiểm tra. |
| TTM-NFR-AUD-004 | Actor Attribution | History nên cho biết actor thực hiện thay đổi ở mức nghiệp vụ. |

### 7.7 Maintainability

| NFR ID | Requirement | Description |
|---|---|---|
| TTM-NFR-MNT-001 | Lifecycle Maintainability | Task lifecycle phải có khả năng được điều chỉnh theo chính sách sản phẩm. |
| TTM-NFR-MNT-002 | Policy Maintainability | Các policy về priority, timeline, archive, recurring và completion cần có thể được quản lý nhất quán. |
| TTM-NFR-MNT-003 | Terminology Consistency | Các thuật ngữ Task, Planned, Scheduled, Progress, Completed và Archived phải được sử dụng nhất quán. |
| TTM-NFR-MNT-004 | Rule Consistency | Business rule phải được áp dụng nhất quán giữa Task list và Timeline. |

### 7.8 Accessibility

| NFR ID | Requirement | Description |
|---|---|---|
| TTM-NFR-ACC-001 | Non-drag Alternative | Các hành động Timeline quan trọng không nên chỉ phụ thuộc vào drag & drop nếu User không thể sử dụng thao tác này. |
| TTM-NFR-ACC-002 | Status Perceivability | Trạng thái Task phải có thể nhận biết bằng thông tin rõ ràng, không chỉ dựa vào màu sắc. |
| TTM-NFR-ACC-003 | Keyboard-friendly Planning | Các hành động planning và cập nhật Task nên có thể thực hiện bằng phương thức nhập liệu phù hợp với accessibility. |
| TTM-NFR-ACC-004 | Clear Validation Feedback | Thông báo validation phải đủ rõ để User biết cần sửa thông tin nào. |

## 8. Business Rules

| Business Rule ID | Business Rule |
|---|---|
| TTM-BR-001 | Mỗi Task phải thuộc đúng một User sở hữu. |
| TTM-BR-002 | User chỉ được quản lý Task của chính mình. |
| TTM-BR-003 | Staff không được chỉnh sửa Task cá nhân của User nếu không có policy rõ ràng. |
| TTM-BR-004 | Admin không được thay đổi Task cá nhân của User trừ trường hợp ngoại lệ được định nghĩa rõ. |
| TTM-BR-005 | Task Name là thông tin bắt buộc. |
| TTM-BR-006 | Task Name không được rỗng hoặc chỉ chứa khoảng trắng. |
| TTM-BR-007 | Task ở trạng thái Draft có thể thiếu thông tin planning không bắt buộc. |
| TTM-BR-008 | Task chỉ được chuyển sang Planned khi đáp ứng thông tin planning bắt buộc theo policy. |
| TTM-BR-009 | Task có Time Capital hoặc estimated time hợp lệ mới được hiển thị trên Timeline. |
| TTM-BR-010 | Timeline không nhận Task không có Time Capital hoặc estimated time hợp lệ nếu policy không cho phép ngoại lệ. |
| TTM-BR-011 | Estimated Time dùng để schedule phải lớn hơn 0. |
| TTM-BR-012 | Estimated Cost không được âm. |
| TTM-BR-013 | Deadline không được nhỏ hơn ngày bắt đầu nếu Task có scheduled start. |
| TTM-BR-014 | Deadline phải là giá trị thời gian hợp lệ. |
| TTM-BR-015 | Priority phải thuộc tập giá trị được phê duyệt. |
| TTM-BR-016 | Progress phải nằm trong khoảng 0% đến 100%. |
| TTM-BR-017 | Progress 100% không nhất thiết tự động Completed nếu policy yêu cầu User xác nhận. |
| TTM-BR-018 | Completed Task không được thay đổi planning quan trọng nếu không có quy trình Reopen. |
| TTM-BR-019 | Cancelled Task không được tiếp tục cập nhật progress nếu chưa reopen. |
| TTM-BR-020 | Archived Task không xuất hiện trên Timeline chính nếu policy không quy định khác. |
| TTM-BR-021 | Task đã archived không được chỉnh sửa planning nếu chưa restore. |
| TTM-BR-022 | Task bị delete không được xuất hiện trong Task list chính hoặc Timeline. |
| TTM-BR-023 | Delete Task chỉ được phép ở trạng thái được policy cho phép. |
| TTM-BR-024 | Archive Task không đồng nghĩa xóa vĩnh viễn. |
| TTM-BR-025 | Restore Task phải tuân thủ trạng thái trước archive hoặc trạng thái được policy xác định. |
| TTM-BR-026 | Một Task có thể có nhiều Tag nếu Tag policy cho phép. |
| TTM-BR-027 | Tag được gắn vào Task phải hợp lệ theo quyền sở hữu hoặc phạm vi sử dụng. |
| TTM-BR-028 | Task có thể không có Category nếu policy cho phép. |
| TTM-BR-029 | Nếu Category bắt buộc, Task không được Planned hoặc Scheduled khi thiếu Category hợp lệ. |
| TTM-BR-030 | Một Task chỉ được gán một Category chính nếu policy không cho phép nhiều Category. |
| TTM-BR-031 | Task có thể được duplicate, nhưng Task mới phải có identity nghiệp vụ riêng và trạng thái theo policy. |
| TTM-BR-032 | Duplicate Task không được sao chép history như history của Task gốc nếu policy không cho phép. |
| TTM-BR-033 | Scheduled Task phải có schedule hợp lệ. |
| TTM-BR-034 | Không được kéo Task sang chu kỳ không hợp lệ nếu policy không cho phép. |
| TTM-BR-035 | Drag & Drop Timeline phải kiểm tra deadline và schedule validity. |
| TTM-BR-036 | Nếu Task bị kéo chồng lên Task khác, hệ thống phải xử lý theo Timeline Policy. |
| TTM-BR-037 | Timeline conflict có thể bị từ chối, cảnh báo hoặc cho phép có xác nhận tùy policy. |
| TTM-BR-038 | Task đang In Progress có thể pause. |
| TTM-BR-039 | Task On Hold có thể resume. |
| TTM-BR-040 | Task Completed có thể archive. |
| TTM-BR-041 | Task Cancelled có thể archive. |
| TTM-BR-042 | Task Completed hoặc Cancelled chỉ được reopen nếu policy cho phép. |
| TTM-BR-043 | Reopen Task phải ghi nhận lý do nếu policy yêu cầu. |
| TTM-BR-044 | Cancel Task phải ghi nhận lý do nếu policy yêu cầu. |
| TTM-BR-045 | Complete Task phải kiểm tra trạng thái hiện tại hợp lệ. |
| TTM-BR-046 | Task ở Draft không nên complete trực tiếp nếu chưa đáp ứng planning policy, trừ khi policy cho phép quick completion. |
| TTM-BR-047 | Recurring Task chỉ được sử dụng nếu recurring policy được phê duyệt. |
| TTM-BR-048 | Recurring rule phải hợp lệ trước khi tạo occurrence. |
| TTM-BR-049 | Recurring occurrence phải tuân thủ deadline và schedule policy. |
| TTM-BR-050 | Reminder chỉ được sử dụng nếu reminder policy được phê duyệt. |
| TTM-BR-051 | Reminder time phải hợp lệ so với deadline hoặc schedule theo policy. |
| TTM-BR-052 | Mọi thay đổi quan trọng về status nên được ghi nhận nếu Task Change History Policy yêu cầu. |
| TTM-BR-053 | Mọi thay đổi schedule quan trọng nên được ghi nhận nếu Timeline Change History Policy yêu cầu. |
| TTM-BR-054 | Hành động bị validation từ chối không được làm thay đổi Task. |
| TTM-BR-055 | Search và Filter chỉ trả về Task mà User có quyền xem. |
| TTM-BR-056 | Sort không được làm thay đổi dữ liệu Task. |
| TTM-BR-057 | Task overdue phải được xác định khi deadline đã qua và Task chưa Completed hoặc Cancelled. |
| TTM-BR-058 | Task không có Money Capital vẫn có thể tồn tại nếu policy không yêu cầu estimated cost. |
| TTM-BR-059 | Task không có Time Capital có thể tồn tại trong Task list nhưng không xuất hiện trên Timeline. |
| TTM-BR-060 | Task Completion không thực hiện đánh giá hiệu quả trong phạm vi module này. |

## 9. Workflows

### 9.1 Create Task

#### Main Flow

1. User yêu cầu tạo Task mới.
2. User nhập Task Name và các thông tin tùy chọn ban đầu.
3. Hệ thống kiểm tra Task Name hợp lệ.
4. Hệ thống gán Task cho User sở hữu.
5. Hệ thống tạo Task ở trạng thái Draft hoặc trạng thái được policy xác định.
6. Hệ thống ghi nhận thay đổi nếu policy yêu cầu.

#### Alternative Flow

- User nhập đầy đủ thông tin planning ngay khi tạo, Task có thể chuyển sang Planned nếu đáp ứng policy.
- User duplicate từ Task hiện có để tạo Task mới.

#### Exception Flow

- Task Name thiếu hoặc không hợp lệ: hệ thống từ chối.
- User không có quyền tạo Task: hệ thống từ chối.

### 9.2 Edit Task

#### Main Flow

1. User chọn Task của chính mình.
2. User cập nhật thông tin được phép.
3. Hệ thống kiểm tra ownership.
4. Hệ thống kiểm tra trạng thái Task.
5. Hệ thống kiểm tra validation rule cho thông tin thay đổi.
6. Hệ thống cập nhật Task.
7. Hệ thống ghi nhận history nếu policy yêu cầu.

#### Alternative Flow

- Nếu Task Completed, hệ thống yêu cầu Reopen trước khi sửa planning quan trọng.
- Nếu chỉ thay đổi tag hoặc category, hệ thống xử lý theo policy phân loại.

#### Exception Flow

- Task không thuộc User: hệ thống từ chối.
- Task ở trạng thái không cho phép sửa: hệ thống từ chối.
- Thông tin thay đổi không hợp lệ: hệ thống từ chối.

### 9.3 Delete Task

#### Main Flow

1. User chọn Task cần delete.
2. Hệ thống kiểm tra ownership và trạng thái Task.
3. Hệ thống kiểm tra policy delete.
4. User xác nhận delete.
5. Hệ thống xóa Task theo chính sách.

#### Alternative Flow

- Nếu Task không được delete, hệ thống có thể đề xuất archive nếu policy cho phép.

#### Exception Flow

- Task đang In Progress hoặc Completed và policy không cho delete: hệ thống từ chối.
- Task không thuộc User: hệ thống từ chối.

### 9.4 Plan Task

#### Main Flow

1. User chọn Task ở trạng thái Draft hoặc trạng thái cho phép planning.
2. User nhập thông tin planning như priority, deadline, estimated time, estimated cost, category và tag.
3. Hệ thống kiểm tra thông tin planning.
4. Nếu Task đáp ứng planning policy, hệ thống chuyển Task sang Planned.
5. Hệ thống ghi nhận thay đổi nếu policy yêu cầu.

#### Alternative Flow

- Task có thể vẫn ở Draft nếu thiếu thông tin chưa bắt buộc.
- User có thể plan mà chưa schedule.

#### Exception Flow

- Priority không hợp lệ, deadline không hợp lệ hoặc estimated time/cost không hợp lệ: hệ thống từ chối phần thay đổi liên quan.

### 9.5 Schedule Task

#### Main Flow

1. User chọn Task Planned hoặc trạng thái cho phép scheduling.
2. User xác định thời điểm thực hiện.
3. Hệ thống kiểm tra Task có Time Capital hoặc estimated time hợp lệ.
4. Hệ thống kiểm tra schedule không vi phạm deadline và policy.
5. Hệ thống đặt Task vào Timeline.
6. Task chuyển sang Scheduled nếu policy yêu cầu.

#### Alternative Flow

- Hệ thống phát hiện xung đột Timeline và cảnh báo User.
- User chọn vẫn schedule nếu policy cho phép chồng lịch có xác nhận.

#### Exception Flow

- Task không có Time Capital: hệ thống từ chối đưa lên Timeline.
- Schedule vượt deadline hoặc chu kỳ không hợp lệ: hệ thống từ chối.

### 9.6 Move Timeline

#### Main Flow

1. User chọn Task trên Timeline.
2. User di chuyển Task sang thời điểm mới.
3. Hệ thống kiểm tra ownership, Time Capital và trạng thái Task.
4. Hệ thống kiểm tra deadline, conflict và policy liên quan.
5. Hệ thống cập nhật schedule nếu hợp lệ.
6. Hệ thống ghi nhận timeline history nếu policy yêu cầu.

#### Alternative Flow

- Nếu có conflict, hệ thống cảnh báo và yêu cầu xác nhận nếu policy cho phép.

#### Exception Flow

- Task không hợp lệ để move: hệ thống từ chối.
- Vị trí mới vi phạm deadline hoặc cycle policy: hệ thống từ chối.

### 9.7 Update Progress

#### Main Flow

1. User chọn Task đang ở trạng thái cho phép cập nhật progress.
2. User nhập progress mới.
3. Hệ thống kiểm tra progress trong khoảng 0% đến 100%.
4. Hệ thống cập nhật progress.
5. Nếu progress đạt 100%, hệ thống xử lý theo completion policy.

#### Alternative Flow

- Progress 100% có thể đề xuất Complete Task nhưng chưa tự complete nếu policy yêu cầu xác nhận.

#### Exception Flow

- Progress nhỏ hơn 0 hoặc lớn hơn 100: hệ thống từ chối.
- Task Completed, Cancelled hoặc Archived: hệ thống từ chối nếu chưa reopen/restore.

### 9.8 Complete Task

#### Main Flow

1. User chọn Task cần complete.
2. Hệ thống kiểm tra trạng thái hiện tại.
3. Hệ thống kiểm tra completion rule.
4. User xác nhận hoàn thành.
5. Hệ thống chuyển Task sang Completed.
6. Hệ thống xử lý vị trí Timeline theo policy.
7. Hệ thống ghi nhận status history nếu policy yêu cầu.

#### Alternative Flow

- Nếu progress chưa đạt mức yêu cầu, hệ thống yêu cầu User cập nhật progress hoặc xác nhận theo policy.

#### Exception Flow

- Task ở trạng thái không cho complete: hệ thống từ chối.

### 9.9 Cancel Task

#### Main Flow

1. User chọn Task cần cancel.
2. Hệ thống kiểm tra trạng thái Task có cho phép cancel.
3. User nhập lý do nếu policy yêu cầu.
4. User xác nhận cancel.
5. Hệ thống chuyển Task sang Cancelled.
6. Hệ thống xử lý schedule hoặc Timeline placement theo policy.

#### Alternative Flow

- User hủy thao tác trước khi xác nhận.

#### Exception Flow

- Task Completed hoặc Archived không cho cancel nếu chưa reopen/restore.

### 9.10 Archive Task

#### Main Flow

1. User chọn Task cần archive.
2. Hệ thống kiểm tra ownership và trạng thái.
3. User xác nhận archive.
4. Hệ thống chuyển Task sang Archived.
5. Hệ thống loại Task khỏi Timeline chính nếu policy yêu cầu.

#### Alternative Flow

- User archive nhiều Task nếu bulk action được policy cho phép.

#### Exception Flow

- Task không thuộc User hoặc trạng thái không cho archive: hệ thống từ chối.

### 9.11 Restore Task

#### Main Flow

1. User chọn Archived Task.
2. Hệ thống kiểm tra ownership.
3. Hệ thống xác định trạng thái khôi phục theo policy.
4. User xác nhận restore.
5. Hệ thống restore Task.
6. Hệ thống xử lý Task có quay lại Timeline hay không theo policy.

#### Alternative Flow

- Task được restore về trạng thái trước archive.
- Task được restore về Draft hoặc Planned nếu policy quy định.

#### Exception Flow

- Task không thuộc User hoặc không ở Archived: hệ thống từ chối.

## 10. Use Case List

| Use Case ID | Use Case Name | Primary Actor | Summary |
|---|---|---|---|
| TTM-UC-001 | Create Task | User | Tạo Task mới. |
| TTM-UC-002 | Update Task | User | Cập nhật thông tin Task. |
| TTM-UC-003 | Delete Task | User | Xóa Task theo policy. |
| TTM-UC-004 | Archive Task | User | Lưu trữ Task. |
| TTM-UC-005 | Restore Task | User | Khôi phục Task đã archive. |
| TTM-UC-006 | Duplicate Task | User | Tạo Task mới từ Task hiện có. |
| TTM-UC-007 | View Task Detail | User | Xem chi tiết Task. |
| TTM-UC-008 | Search Task | User | Tìm kiếm Task. |
| TTM-UC-009 | Filter Task | User | Lọc Task. |
| TTM-UC-010 | Sort Task | User | Sắp xếp Task. |
| TTM-UC-011 | Assign Category | User | Gán Category cho Task. |
| TTM-UC-012 | Assign Tag | User | Gắn Tag cho Task. |
| TTM-UC-013 | Remove Tag | User | Gỡ Tag khỏi Task. |
| TTM-UC-014 | Set Priority | User | Gán hoặc cập nhật priority. |
| TTM-UC-015 | Set Deadline | User | Đặt hoặc thay đổi deadline. |
| TTM-UC-016 | Estimate Task Resources | User | Nhập estimated time và estimated cost. |
| TTM-UC-017 | Plan Task | User | Chuyển Task sang Planned. |
| TTM-UC-018 | Schedule Task | User | Đưa Task lên Timeline. |
| TTM-UC-019 | Reschedule Task | User | Thay đổi lịch thực hiện. |
| TTM-UC-020 | View Timeline | User | Xem Timeline. |
| TTM-UC-021 | Drag & Drop Timeline Task | User | Kéo thả Task trên Timeline. |
| TTM-UC-022 | Update Progress | User | Cập nhật tiến độ Task. |
| TTM-UC-023 | Pause Task | User | Tạm dừng Task. |
| TTM-UC-024 | Resume Task | User | Tiếp tục Task. |
| TTM-UC-025 | Complete Task | User | Hoàn thành Task. |
| TTM-UC-026 | Cancel Task | User | Hủy Task. |
| TTM-UC-027 | Reopen Task | User | Mở lại Task Completed hoặc Cancelled. |
| TTM-UC-028 | Manage Recurring Task | User | Quản lý Task lặp lại nếu policy cho phép. |
| TTM-UC-029 | Manage Task Reminder | User | Quản lý reminder nếu policy cho phép. |
| TTM-UC-030 | Validate Task Ownership | System | Kiểm tra ownership trước thao tác. |
| TTM-UC-031 | Validate Timeline Eligibility | System | Kiểm tra điều kiện Task xuất hiện trên Timeline. |

## 11. Use Case Specification

### TTM-UC-001 - Create Task

| Field | Description |
|---|---|
| ID | TTM-UC-001 |
| Description | User tạo Task mới thuộc sở hữu của chính mình. |
| Primary Actor | User |
| Trigger | User yêu cầu tạo Task. |
| Preconditions | User đã xác thực và có quyền tạo Task. |
| Main Flow | 1. User nhập Task Name. 2. User nhập thông tin tùy chọn. 3. Hệ thống validate Task Name. 4. Hệ thống tạo Task và gán ownership cho User. |
| Alternative Flow | User nhập đủ thông tin planning để Task chuyển sang Planned nếu policy cho phép. |
| Exception Flow | Task Name không hợp lệ hoặc User không có quyền dẫn đến từ chối. |
| Postconditions | Task mới được tạo. |
| Business Rules | TTM-BR-001, TTM-BR-005, TTM-BR-006 |

### TTM-UC-002 - Update Task

| Field | Description |
|---|---|
| ID | TTM-UC-002 |
| Description | User cập nhật Task của chính mình. |
| Primary Actor | User |
| Trigger | User chọn cập nhật Task. |
| Preconditions | Task thuộc User và trạng thái cho phép cập nhật. |
| Main Flow | 1. User chọn Task. 2. User nhập thay đổi. 3. Hệ thống kiểm tra ownership và status. 4. Hệ thống validate thay đổi. 5. Hệ thống cập nhật Task. |
| Alternative Flow | Nếu Task Completed, hệ thống yêu cầu Reopen trước khi sửa planning quan trọng. |
| Exception Flow | Task không thuộc User, trạng thái không cho phép hoặc dữ liệu không hợp lệ dẫn đến từ chối. |
| Postconditions | Task được cập nhật nếu hợp lệ. |
| Business Rules | TTM-BR-002, TTM-BR-018, TTM-BR-054 |

### TTM-UC-003 - Delete Task

| Field | Description |
|---|---|
| ID | TTM-UC-003 |
| Description | User xóa Task theo policy. |
| Primary Actor | User |
| Trigger | User chọn delete Task. |
| Preconditions | Task thuộc User và trạng thái cho phép delete. |
| Main Flow | 1. User chọn Task. 2. Hệ thống kiểm tra ownership và policy. 3. User xác nhận. 4. Hệ thống xóa Task theo policy. |
| Alternative Flow | Nếu delete không được phép, User có thể archive nếu policy cho phép. |
| Exception Flow | Task không được delete hoặc không thuộc User dẫn đến từ chối. |
| Postconditions | Task không còn xuất hiện trong danh sách chính nếu delete thành công. |
| Business Rules | TTM-BR-022, TTM-BR-023 |

### TTM-UC-004 - Archive Task

| Field | Description |
|---|---|
| ID | TTM-UC-004 |
| Description | User archive Task để lưu trữ. |
| Primary Actor | User |
| Trigger | User chọn archive Task. |
| Preconditions | Task thuộc User và trạng thái cho phép archive. |
| Main Flow | 1. User chọn Task. 2. Hệ thống kiểm tra status. 3. User xác nhận archive. 4. Hệ thống chuyển Task sang Archived. |
| Alternative Flow | User archive Task Completed hoặc Cancelled. |
| Exception Flow | Task không thuộc User hoặc không cho archive dẫn đến từ chối. |
| Postconditions | Task ở trạng thái Archived và không xuất hiện trên Timeline chính theo policy. |
| Business Rules | TTM-BR-020, TTM-BR-024, TTM-BR-040, TTM-BR-041 |

### TTM-UC-005 - Restore Task

| Field | Description |
|---|---|
| ID | TTM-UC-005 |
| Description | User restore Archived Task. |
| Primary Actor | User |
| Trigger | User chọn restore. |
| Preconditions | Task thuộc User và đang Archived. |
| Main Flow | 1. User chọn Archived Task. 2. Hệ thống kiểm tra ownership. 3. Hệ thống xác định trạng thái restore. 4. User xác nhận. 5. Hệ thống restore Task. |
| Alternative Flow | Task được restore về trạng thái trước archive hoặc trạng thái theo policy. |
| Exception Flow | Task không ở Archived hoặc không thuộc User dẫn đến từ chối. |
| Postconditions | Task được khôi phục theo policy. |
| Business Rules | TTM-BR-021, TTM-BR-025 |

### TTM-UC-006 - Duplicate Task

| Field | Description |
|---|---|
| ID | TTM-UC-006 |
| Description | User tạo Task mới dựa trên Task hiện có. |
| Primary Actor | User |
| Trigger | User chọn duplicate Task. |
| Preconditions | Task gốc thuộc User. |
| Main Flow | 1. User chọn Task gốc. 2. Hệ thống kiểm tra ownership. 3. Hệ thống tạo Task mới dựa trên thông tin được phép sao chép. 4. Hệ thống đặt trạng thái Task mới theo policy. |
| Alternative Flow | User chỉnh sửa thông tin Task mới trước khi xác nhận. |
| Exception Flow | Task gốc không thuộc User dẫn đến từ chối. |
| Postconditions | Task mới được tạo, độc lập với Task gốc. |
| Business Rules | TTM-BR-031, TTM-BR-032 |

### TTM-UC-007 - View Task Detail

| Field | Description |
|---|---|
| ID | TTM-UC-007 |
| Description | User xem chi tiết Task. |
| Primary Actor | User |
| Trigger | User mở Task Detail. |
| Preconditions | Task thuộc User. |
| Main Flow | 1. User chọn Task. 2. Hệ thống kiểm tra ownership. 3. Hệ thống hiển thị thông tin Task trong phạm vi module. |
| Alternative Flow | User chuyển sang edit nếu status cho phép. |
| Exception Flow | Task không thuộc User dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu Task. |
| Business Rules | TTM-BR-002 |

### TTM-UC-008 - Search Task

| Field | Description |
|---|---|
| ID | TTM-UC-008 |
| Description | User tìm kiếm Task của chính mình. |
| Primary Actor | User |
| Trigger | User nhập từ khóa hoặc tiêu chí tìm kiếm. |
| Preconditions | User đã xác thực. |
| Main Flow | 1. User nhập tiêu chí. 2. Hệ thống giới hạn phạm vi theo ownership. 3. Hệ thống trả Task phù hợp. |
| Alternative Flow | Không có kết quả, hệ thống thông báo không có Task phù hợp. |
| Exception Flow | Tiêu chí không hợp lệ dẫn đến từ chối hoặc yêu cầu sửa. |
| Postconditions | Không thay đổi dữ liệu Task. |
| Business Rules | TTM-BR-055 |

### TTM-UC-009 - Filter Task

| Field | Description |
|---|---|
| ID | TTM-UC-009 |
| Description | User lọc Task theo tiêu chí nghiệp vụ. |
| Primary Actor | User |
| Trigger | User chọn filter. |
| Preconditions | User đã xác thực. |
| Main Flow | 1. User chọn filter. 2. Hệ thống kiểm tra tiêu chí. 3. Hệ thống hiển thị Task phù hợp trong phạm vi ownership. |
| Alternative Flow | User kết hợp nhiều tiêu chí nếu policy cho phép. |
| Exception Flow | Filter không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu Task. |
| Business Rules | TTM-BR-055 |

### TTM-UC-010 - Sort Task

| Field | Description |
|---|---|
| ID | TTM-UC-010 |
| Description | User sắp xếp Task theo tiêu chí được phê duyệt. |
| Primary Actor | User |
| Trigger | User chọn sort. |
| Preconditions | User đang xem danh sách Task. |
| Main Flow | 1. User chọn tiêu chí sort. 2. Hệ thống sắp xếp Task được phép xem. |
| Alternative Flow | User đổi hướng sort tăng dần hoặc giảm dần nếu policy cho phép. |
| Exception Flow | Tiêu chí sort không hợp lệ dẫn đến từ chối. |
| Postconditions | Dữ liệu Task không thay đổi. |
| Business Rules | TTM-BR-056 |

### TTM-UC-011 - Assign Category

| Field | Description |
|---|---|
| ID | TTM-UC-011 |
| Description | User gán Category cho Task. |
| Primary Actor | User |
| Trigger | User chọn Category cho Task. |
| Preconditions | Task thuộc User và Category hợp lệ. |
| Main Flow | 1. User chọn Task. 2. User chọn Category. 3. Hệ thống kiểm tra ownership và Category. 4. Hệ thống gán Category. |
| Alternative Flow | User thay đổi Category hiện tại. |
| Exception Flow | Category không hợp lệ hoặc Task không thuộc User dẫn đến từ chối. |
| Postconditions | Task có Category được gán nếu thành công. |
| Business Rules | TTM-BR-028, TTM-BR-029, TTM-BR-030 |

### TTM-UC-012 - Assign Tag

| Field | Description |
|---|---|
| ID | TTM-UC-012 |
| Description | User gắn Tag cho Task. |
| Primary Actor | User |
| Trigger | User chọn Tag. |
| Preconditions | Task thuộc User và Tag hợp lệ. |
| Main Flow | 1. User chọn Task. 2. User chọn một hoặc nhiều Tag. 3. Hệ thống kiểm tra Tag hợp lệ và giới hạn số lượng. 4. Hệ thống gắn Tag. |
| Alternative Flow | User tạo hoặc chọn Tag theo capability liên quan nếu được hỗ trợ. |
| Exception Flow | Tag không hợp lệ hoặc vượt giới hạn dẫn đến từ chối. |
| Postconditions | Task có Tag được gắn. |
| Business Rules | TTM-BR-026, TTM-BR-027 |

### TTM-UC-013 - Remove Tag

| Field | Description |
|---|---|
| ID | TTM-UC-013 |
| Description | User gỡ Tag khỏi Task. |
| Primary Actor | User |
| Trigger | User chọn remove Tag. |
| Preconditions | Task thuộc User và Tag đang gắn với Task. |
| Main Flow | 1. User chọn Task. 2. User chọn Tag cần gỡ. 3. Hệ thống kiểm tra ownership. 4. Hệ thống gỡ Tag. |
| Alternative Flow | User gỡ nhiều Tag nếu policy cho phép. |
| Exception Flow | Tag không tồn tại trên Task dẫn đến thông báo không có thay đổi. |
| Postconditions | Tag không còn gắn với Task. |
| Business Rules | TTM-BR-026, TTM-BR-027 |

### TTM-UC-014 - Set Priority

| Field | Description |
|---|---|
| ID | TTM-UC-014 |
| Description | User gán hoặc cập nhật priority cho Task. |
| Primary Actor | User |
| Trigger | User chọn priority. |
| Preconditions | Task thuộc User và trạng thái cho phép cập nhật priority. |
| Main Flow | 1. User chọn Task. 2. User chọn priority. 3. Hệ thống validate priority. 4. Hệ thống cập nhật priority. |
| Alternative Flow | User thay đổi priority nhiều lần trước khi Task Completed. |
| Exception Flow | Priority không thuộc tập giá trị cho phép dẫn đến từ chối. |
| Postconditions | Task có priority mới. |
| Business Rules | TTM-BR-015 |

### TTM-UC-015 - Set Deadline

| Field | Description |
|---|---|
| ID | TTM-UC-015 |
| Description | User đặt hoặc thay đổi deadline cho Task. |
| Primary Actor | User |
| Trigger | User nhập deadline. |
| Preconditions | Task thuộc User và trạng thái cho phép cập nhật deadline. |
| Main Flow | 1. User chọn Task. 2. User nhập deadline. 3. Hệ thống validate deadline. 4. Hệ thống cập nhật deadline. |
| Alternative Flow | Deadline đã qua có thể được chấp nhận hoặc cảnh báo theo policy. |
| Exception Flow | Deadline không hợp lệ hoặc nhỏ hơn scheduled start dẫn đến từ chối. |
| Postconditions | Task có deadline được cập nhật. |
| Business Rules | TTM-BR-013, TTM-BR-014, TTM-BR-057 |

### TTM-UC-016 - Estimate Task Resources

| Field | Description |
|---|---|
| ID | TTM-UC-016 |
| Description | User nhập estimated time và estimated cost cho Task. |
| Primary Actor | User |
| Trigger | User lập kế hoạch nguồn lực cho Task. |
| Preconditions | Task thuộc User. |
| Main Flow | 1. User nhập estimated time. 2. User nhập estimated cost nếu cần. 3. Hệ thống validate giá trị. 4. Hệ thống cập nhật thông tin ước lượng. |
| Alternative Flow | User chỉ nhập estimated time hoặc chỉ estimated cost. |
| Exception Flow | Estimated time không hợp lệ hoặc estimated cost âm dẫn đến từ chối. |
| Postconditions | Task có thông tin ước lượng hợp lệ. |
| Business Rules | TTM-BR-011, TTM-BR-012, TTM-BR-058, TTM-BR-059 |

### TTM-UC-017 - Plan Task

| Field | Description |
|---|---|
| ID | TTM-UC-017 |
| Description | User chuyển Task sang Planned khi đáp ứng planning policy. |
| Primary Actor | User |
| Trigger | User hoàn tất thông tin planning. |
| Preconditions | Task thuộc User và đang ở trạng thái cho phép planning. |
| Main Flow | 1. User cập nhật thông tin planning. 2. Hệ thống kiểm tra planning policy. 3. Hệ thống chuyển Task sang Planned. |
| Alternative Flow | Task vẫn ở Draft nếu chưa đủ điều kiện. |
| Exception Flow | Thiếu thông tin bắt buộc hoặc dữ liệu không hợp lệ dẫn đến từ chối chuyển trạng thái. |
| Postconditions | Task ở trạng thái Planned nếu đáp ứng policy. |
| Business Rules | TTM-BR-007, TTM-BR-008 |

### TTM-UC-018 - Schedule Task

| Field | Description |
|---|---|
| ID | TTM-UC-018 |
| Description | User đưa Task lên Timeline bằng cách schedule Task. |
| Primary Actor | User |
| Trigger | User chọn schedule. |
| Preconditions | Task thuộc User, có Time Capital hoặc estimated time hợp lệ, và trạng thái cho phép scheduling. |
| Main Flow | 1. User chọn Task. 2. User chọn thời điểm thực hiện. 3. Hệ thống validate Timeline eligibility. 4. Hệ thống validate deadline và conflict. 5. Hệ thống schedule Task. |
| Alternative Flow | Nếu conflict, hệ thống cảnh báo hoặc yêu cầu xác nhận theo policy. |
| Exception Flow | Task không có Time Capital hoặc schedule không hợp lệ dẫn đến từ chối. |
| Postconditions | Task xuất hiện trên Timeline nếu thành công. |
| Business Rules | TTM-BR-009, TTM-BR-010, TTM-BR-033, TTM-BR-036 |

### TTM-UC-019 - Reschedule Task

| Field | Description |
|---|---|
| ID | TTM-UC-019 |
| Description | User thay đổi lịch thực hiện của Scheduled Task. |
| Primary Actor | User |
| Trigger | User chọn reschedule. |
| Preconditions | Task đang Scheduled hoặc trạng thái cho phép reschedule. |
| Main Flow | 1. User chọn lịch mới. 2. Hệ thống kiểm tra schedule validity. 3. Hệ thống cập nhật schedule. |
| Alternative Flow | Hệ thống cảnh báo conflict nếu có. |
| Exception Flow | Lịch mới vi phạm deadline hoặc policy dẫn đến từ chối. |
| Postconditions | Task có schedule mới. |
| Business Rules | TTM-BR-034, TTM-BR-035, TTM-BR-037 |

### TTM-UC-020 - View Timeline

| Field | Description |
|---|---|
| ID | TTM-UC-020 |
| Description | User xem Timeline của chính mình. |
| Primary Actor | User |
| Trigger | User mở Timeline. |
| Preconditions | User đã xác thực. |
| Main Flow | 1. User yêu cầu xem Timeline. 2. Hệ thống xác định Task đủ điều kiện. 3. Hệ thống hiển thị Task theo thời gian. |
| Alternative Flow | Không có Task đủ điều kiện, Timeline hiển thị trạng thái rỗng. |
| Exception Flow | User không có quyền truy cập dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu Task. |
| Business Rules | TTM-BR-009, TTM-BR-010, TTM-BR-055 |

### TTM-UC-021 - Drag & Drop Timeline Task

| Field | Description |
|---|---|
| ID | TTM-UC-021 |
| Description | User kéo thả Task trên Timeline để đổi lịch. |
| Primary Actor | User |
| Trigger | User kéo Task sang vị trí mới. |
| Preconditions | Task thuộc User, đang trên Timeline và cho phép move. |
| Main Flow | 1. User kéo Task. 2. Hệ thống kiểm tra vị trí mới. 3. Hệ thống kiểm tra deadline và conflict. 4. Hệ thống cập nhật schedule nếu hợp lệ. |
| Alternative Flow | Nếu có conflict, hệ thống xử lý theo policy. |
| Exception Flow | Vị trí mới không hợp lệ dẫn đến từ chối và Task giữ lịch cũ. |
| Postconditions | Schedule được thay đổi nếu hợp lệ. |
| Business Rules | TTM-BR-034, TTM-BR-035, TTM-BR-036, TTM-BR-037 |

### TTM-UC-022 - Update Progress

| Field | Description |
|---|---|
| ID | TTM-UC-022 |
| Description | User cập nhật tiến độ Task. |
| Primary Actor | User |
| Trigger | User nhập progress. |
| Preconditions | Task thuộc User và trạng thái cho phép update progress. |
| Main Flow | 1. User nhập progress. 2. Hệ thống validate progress. 3. Hệ thống cập nhật progress. |
| Alternative Flow | Progress 100% đề xuất completion theo policy. |
| Exception Flow | Progress ngoài 0-100 hoặc Task không cho update dẫn đến từ chối. |
| Postconditions | Progress được cập nhật. |
| Business Rules | TTM-BR-016, TTM-BR-017 |

### TTM-UC-023 - Pause Task

| Field | Description |
|---|---|
| ID | TTM-UC-023 |
| Description | User tạm dừng Task đang thực hiện. |
| Primary Actor | User |
| Trigger | User chọn pause. |
| Preconditions | Task đang In Progress. |
| Main Flow | 1. User chọn Task. 2. Hệ thống kiểm tra trạng thái. 3. Hệ thống chuyển Task sang On Hold. |
| Alternative Flow | User nhập lý do pause nếu policy yêu cầu. |
| Exception Flow | Task không In Progress dẫn đến từ chối. |
| Postconditions | Task ở trạng thái On Hold. |
| Business Rules | TTM-BR-038 |

### TTM-UC-024 - Resume Task

| Field | Description |
|---|---|
| ID | TTM-UC-024 |
| Description | User tiếp tục Task đang On Hold. |
| Primary Actor | User |
| Trigger | User chọn resume. |
| Preconditions | Task đang On Hold. |
| Main Flow | 1. User chọn Task. 2. Hệ thống kiểm tra trạng thái. 3. Hệ thống chuyển Task sang In Progress. |
| Alternative Flow | User reschedule trước khi resume nếu policy cho phép. |
| Exception Flow | Task không On Hold dẫn đến từ chối. |
| Postconditions | Task ở trạng thái In Progress. |
| Business Rules | TTM-BR-039 |

### TTM-UC-025 - Complete Task

| Field | Description |
|---|---|
| ID | TTM-UC-025 |
| Description | User hoàn thành Task. |
| Primary Actor | User |
| Trigger | User chọn complete. |
| Preconditions | Task thuộc User và trạng thái cho phép completion. |
| Main Flow | 1. User chọn Task. 2. Hệ thống kiểm tra completion rule. 3. User xác nhận. 4. Hệ thống chuyển Task sang Completed. |
| Alternative Flow | Hệ thống yêu cầu progress đạt ngưỡng nếu policy quy định. |
| Exception Flow | Task không đủ điều kiện complete dẫn đến từ chối. |
| Postconditions | Task ở trạng thái Completed. |
| Business Rules | TTM-BR-018, TTM-BR-045, TTM-BR-046, TTM-BR-060 |

### TTM-UC-026 - Cancel Task

| Field | Description |
|---|---|
| ID | TTM-UC-026 |
| Description | User hủy Task không tiếp tục thực hiện. |
| Primary Actor | User |
| Trigger | User chọn cancel. |
| Preconditions | Task thuộc User và trạng thái cho phép cancel. |
| Main Flow | 1. User chọn Task. 2. Hệ thống kiểm tra trạng thái. 3. User nhập lý do nếu cần. 4. Hệ thống chuyển Task sang Cancelled. |
| Alternative Flow | User hủy thao tác trước xác nhận. |
| Exception Flow | Task không cho cancel dẫn đến từ chối. |
| Postconditions | Task ở trạng thái Cancelled. |
| Business Rules | TTM-BR-019, TTM-BR-044 |

### TTM-UC-027 - Reopen Task

| Field | Description |
|---|---|
| ID | TTM-UC-027 |
| Description | User mở lại Task đã Completed hoặc Cancelled nếu policy cho phép. |
| Primary Actor | User |
| Trigger | User chọn reopen. |
| Preconditions | Task thuộc User, đang Completed hoặc Cancelled, và policy cho phép reopen. |
| Main Flow | 1. User chọn Task. 2. Hệ thống kiểm tra policy. 3. User nhập lý do nếu cần. 4. Hệ thống chuyển Task sang trạng thái được policy xác định. |
| Alternative Flow | Task trở về Planned, Scheduled hoặc In Progress theo policy. |
| Exception Flow | Policy không cho reopen dẫn đến từ chối. |
| Postconditions | Task được mở lại theo policy. |
| Business Rules | TTM-BR-042, TTM-BR-043 |

### TTM-UC-028 - Manage Recurring Task

| Field | Description |
|---|---|
| ID | TTM-UC-028 |
| Description | User tạo hoặc quản lý Task lặp lại nếu recurring policy được phê duyệt. |
| Primary Actor | User |
| Trigger | User chọn recurring option. |
| Preconditions | Recurring Task thuộc phạm vi được phê duyệt. |
| Main Flow | 1. User xác định recurring rule. 2. Hệ thống validate rule. 3. Hệ thống ghi nhận Recurring Task hoặc occurrence theo policy. |
| Alternative Flow | User chỉnh sửa rule trước khi xác nhận. |
| Exception Flow | Recurring rule không hợp lệ dẫn đến từ chối. |
| Postconditions | Recurring Task hoặc occurrence được xử lý theo policy. |
| Business Rules | TTM-BR-047, TTM-BR-048, TTM-BR-049 |

### TTM-UC-029 - Manage Task Reminder

| Field | Description |
|---|---|
| ID | TTM-UC-029 |
| Description | User quản lý reminder cho Task nếu reminder policy được phê duyệt. |
| Primary Actor | User |
| Trigger | User đặt reminder. |
| Preconditions | Reminder thuộc phạm vi được phê duyệt và Task hợp lệ. |
| Main Flow | 1. User chọn Task. 2. User nhập reminder time. 3. Hệ thống validate reminder. 4. Hệ thống ghi nhận reminder. |
| Alternative Flow | User thay đổi hoặc gỡ reminder nếu policy cho phép. |
| Exception Flow | Reminder time không hợp lệ dẫn đến từ chối. |
| Postconditions | Reminder được ghi nhận theo policy. |
| Business Rules | TTM-BR-050, TTM-BR-051 |

### TTM-UC-030 - Validate Task Ownership

| Field | Description |
|---|---|
| ID | TTM-UC-030 |
| Description | Hệ thống kiểm tra ownership trước khi cho phép thao tác Task. |
| Primary Actor | System |
| Trigger | Actor yêu cầu xem hoặc thay đổi Task. |
| Preconditions | Actor đã xác thực. |
| Main Flow | 1. Hệ thống xác định actor. 2. Hệ thống xác định owner của Task. 3. Hệ thống cho phép nếu actor là owner hoặc có quyền hợp lệ. |
| Alternative Flow | Staff/Admin có quyền xem giới hạn nếu policy cho phép. |
| Exception Flow | Không có quyền dẫn đến từ chối. |
| Postconditions | Chỉ yêu cầu hợp lệ được xử lý tiếp. |
| Business Rules | TTM-BR-001, TTM-BR-002, TTM-BR-003, TTM-BR-004 |

### TTM-UC-031 - Validate Timeline Eligibility

| Field | Description |
|---|---|
| ID | TTM-UC-031 |
| Description | Hệ thống kiểm tra Task có đủ điều kiện xuất hiện trên Timeline. |
| Primary Actor | System |
| Trigger | User schedule, reschedule hoặc view Timeline. |
| Preconditions | Task tồn tại và actor có quyền. |
| Main Flow | 1. Hệ thống kiểm tra Time Capital hoặc estimated time. 2. Hệ thống kiểm tra status. 3. Hệ thống kiểm tra schedule. 4. Hệ thống xác định Task có được hiển thị trên Timeline hay không. |
| Alternative Flow | Task không có Time Capital vẫn tồn tại trong Task list. |
| Exception Flow | Task không hợp lệ bị loại khỏi Timeline. |
| Postconditions | Timeline chỉ chứa Task đủ điều kiện. |
| Business Rules | TTM-BR-009, TTM-BR-010, TTM-BR-059 |

## 12. User Stories

| Story ID | User Story |
|---|---|
| TTM-US-001 | As a User, I want to create a Task so that I can record work I need to do. |
| TTM-US-002 | As a User, I want to update a Task so that my plan stays accurate. |
| TTM-US-003 | As a User, I want to delete a Task when allowed so that irrelevant work is removed. |
| TTM-US-004 | As a User, I want to archive a Task so that completed or inactive work does not distract me. |
| TTM-US-005 | As a User, I want to restore an archived Task so that I can bring it back when needed. |
| TTM-US-006 | As a User, I want to duplicate a Task so that I can quickly create a similar one. |
| TTM-US-007 | As a User, I want to assign Category so that I can organize my work. |
| TTM-US-008 | As a User, I want to assign multiple Tags so that I can classify Tasks flexibly. |
| TTM-US-009 | As a User, I want to set priority so that I can focus on important work. |
| TTM-US-010 | As a User, I want to set deadline so that I know when work should be completed. |
| TTM-US-011 | As a User, I want to estimate time so that the Task can be scheduled appropriately. |
| TTM-US-012 | As a User, I want to estimate cost so that I understand expected Money Capital. |
| TTM-US-013 | As a User, I want to schedule a Task so that it appears on my Timeline. |
| TTM-US-014 | As a User, I want to drag and drop a Task on Timeline so that I can adjust my schedule easily. |
| TTM-US-015 | As a User, I want to view Timeline so that I understand when my scheduled Tasks occur. |
| TTM-US-016 | As a User, I want to search Tasks so that I can find work quickly. |
| TTM-US-017 | As a User, I want to filter Tasks so that I can focus on relevant work. |
| TTM-US-018 | As a User, I want to sort Tasks so that I can review work in a useful order. |
| TTM-US-019 | As a User, I want to update progress so that I can track how far a Task has advanced. |
| TTM-US-020 | As a User, I want to pause a Task so that I can temporarily stop work without cancelling it. |
| TTM-US-021 | As a User, I want to resume a Task so that I can continue paused work. |
| TTM-US-022 | As a User, I want to complete a Task so that I can mark work as finished. |
| TTM-US-023 | As a User, I want to cancel a Task so that I can stop work that is no longer needed. |
| TTM-US-024 | As a User, I want to reopen a Task so that I can correct or continue a Task after completion or cancellation when allowed. |
| TTM-US-025 | As a User, I want recurring Tasks when approved so that repeated work can be planned consistently. |
| TTM-US-026 | As a User, I want reminders when approved so that I do not miss scheduled work or deadlines. |
| TTM-US-027 | As the system, I want to validate Timeline eligibility so that only Tasks with Time Capital appear on Timeline. |
| TTM-US-028 | As the system, I want to protect Task ownership so that Users cannot access each other's Tasks. |

## 13. Acceptance Criteria

### 13.1 Task Creation and Basic Management

| AC ID | Given | When | Then |
|---|---|---|---|
| TTM-AC-001 | Given a User is authenticated | When the User creates a Task with a valid Task Name | Then the system creates the Task under that User. |
| TTM-AC-002 | Given a User creates a Task | When Task Name is missing or blank | Then the system rejects the creation. |
| TTM-AC-003 | Given a Task belongs to the User | When the User updates valid editable information | Then the system updates the Task. |
| TTM-AC-004 | Given a Task is Completed | When the User attempts to change planning information without reopening | Then the system rejects the change. |
| TTM-AC-005 | Given a Task is in a deletable status | When the User confirms delete | Then the system removes the Task according to policy. |
| TTM-AC-006 | Given a Task is not in a deletable status | When the User attempts delete | Then the system rejects the delete. |
| TTM-AC-007 | Given a Task belongs to the User | When the User duplicates it | Then the system creates a new independent Task according to duplicate policy. |
| TTM-AC-008 | Given a User opens Task detail | When the Task belongs to the User | Then the system displays Task details. |

### 13.2 Classification, Priority and Deadline

| AC ID | Given | When | Then |
|---|---|---|---|
| TTM-AC-009 | Given a valid Category exists | When the User assigns it to a Task | Then the Task is associated with the Category. |
| TTM-AC-010 | Given Category is mandatory | When the User attempts to plan a Task without Category | Then the system rejects the state transition. |
| TTM-AC-011 | Given valid Tags exist | When the User assigns multiple Tags within allowed limit | Then the Tags are associated with the Task. |
| TTM-AC-012 | Given a Task has a Tag | When the User removes the Tag | Then the Tag is no longer associated with the Task. |
| TTM-AC-013 | Given a User sets Priority | When the selected Priority is in the allowed set | Then the system updates Priority. |
| TTM-AC-014 | Given a User sets Priority | When the value is not allowed | Then the system rejects the value. |
| TTM-AC-015 | Given a User sets Deadline | When Deadline is valid | Then the system updates Deadline. |
| TTM-AC-016 | Given a Task has scheduled start | When Deadline is earlier than scheduled start | Then the system rejects the Deadline. |

### 13.3 Resource Estimation and Planning

| AC ID | Given | When | Then |
|---|---|---|---|
| TTM-AC-017 | Given a User estimates time | When estimated time is greater than 0 | Then the system accepts it for scheduling eligibility. |
| TTM-AC-018 | Given a User estimates time for Timeline | When estimated time is zero or negative | Then the system rejects Timeline eligibility. |
| TTM-AC-019 | Given a User estimates cost | When estimated cost is greater than or equal to zero | Then the system accepts it. |
| TTM-AC-020 | Given a User estimates cost | When estimated cost is negative | Then the system rejects it. |
| TTM-AC-021 | Given a Draft Task has required planning information | When the User plans the Task | Then the system changes status to Planned. |
| TTM-AC-022 | Given a Draft Task lacks required planning information | When the User attempts to plan it | Then the system rejects the transition. |

### 13.4 Timeline and Scheduling

| AC ID | Given | When | Then |
|---|---|---|---|
| TTM-AC-023 | Given a Planned Task has Time Capital or valid estimated time | When the User schedules it | Then the Task appears on Timeline. |
| TTM-AC-024 | Given a Task has no Time Capital or valid estimated time | When the User attempts to schedule it | Then the system rejects Timeline placement. |
| TTM-AC-025 | Given a Task is scheduled | When the User reschedules it to a valid time | Then the system updates the schedule. |
| TTM-AC-026 | Given a Task is on Timeline | When the User drags it to a valid position | Then the system updates the schedule. |
| TTM-AC-027 | Given a Task is dragged beyond allowed deadline or cycle | When the User drops it | Then the system rejects the move. |
| TTM-AC-028 | Given a moved Task conflicts with another Task | When Timeline policy does not allow conflict | Then the system rejects the move or warns according to policy. |
| TTM-AC-029 | Given a User opens Timeline | When scheduled eligible Tasks exist | Then the system displays those Tasks. |
| TTM-AC-030 | Given a Task has no Time Capital | When Timeline is displayed | Then the Task is not shown on Timeline. |

### 13.5 Progress and Status

| AC ID | Given | When | Then |
|---|---|---|---|
| TTM-AC-031 | Given a Task allows progress update | When the User sets progress between 0 and 100 | Then the system updates progress. |
| TTM-AC-032 | Given a User sets progress below 0 or above 100 | When the update is submitted | Then the system rejects the update. |
| TTM-AC-033 | Given a Task is In Progress | When the User pauses it | Then the system changes status to On Hold. |
| TTM-AC-034 | Given a Task is On Hold | When the User resumes it | Then the system changes status to In Progress. |
| TTM-AC-035 | Given a Task meets completion conditions | When the User confirms completion | Then the system changes status to Completed. |
| TTM-AC-036 | Given a Task does not meet completion conditions | When the User attempts completion | Then the system rejects completion or requests required action. |
| TTM-AC-037 | Given a Task is cancellable | When the User confirms cancel | Then the system changes status to Cancelled. |
| TTM-AC-038 | Given a Task is Completed or Cancelled and reopen is allowed | When the User reopens it | Then the system changes status according to reopen policy. |

### 13.6 Archive, Recurring, Reminder and Access

| AC ID | Given | When | Then |
|---|---|---|---|
| TTM-AC-039 | Given a Task can be archived | When the User archives it | Then the system changes status to Archived. |
| TTM-AC-040 | Given a Task is Archived | When Timeline is displayed | Then the Task is not shown on the main Timeline unless policy says otherwise. |
| TTM-AC-041 | Given an Archived Task can be restored | When the User restores it | Then the system restores it according to policy. |
| TTM-AC-042 | Given recurring policy is approved | When the User creates a valid recurring rule | Then the system accepts the recurring Task. |
| TTM-AC-043 | Given recurring rule is invalid | When the User submits it | Then the system rejects the recurring setup. |
| TTM-AC-044 | Given reminder policy is approved | When the User sets a valid reminder | Then the system accepts the reminder. |
| TTM-AC-045 | Given a User attempts to access another User's Task | When the request is submitted | Then the system denies access. |
| TTM-AC-046 | Given Staff or Admin lacks an approved exception | When they attempt to change a User's Task | Then the system denies the action. |

## 14. Business Scenarios

### 14.1 Tạo Task

User có một công việc mới cần ghi nhận. User tạo Task với tên hợp lệ. Hệ thống tạo Task ở trạng thái Draft và gán Task cho User. Nếu User nhập thêm thông tin planning, Task có thể chuyển sang Planned theo policy.

Kết quả kỳ vọng: Task được tạo và thuộc quyền sở hữu của User.

### 14.2 Lập kế hoạch

User mở Task Draft và bổ sung deadline, priority, estimated time, estimated cost, category và tag. Hệ thống kiểm tra các giá trị hợp lệ. Khi thông tin đáp ứng planning policy, Task chuyển sang Planned.

Kết quả kỳ vọng: Task có kế hoạch rõ ràng và sẵn sàng để schedule nếu có Time Capital.

### 14.3 Đặt Deadline

User đặt deadline cho Task. Nếu Task đã có scheduled start, hệ thống kiểm tra deadline không nhỏ hơn scheduled start. Nếu deadline đã qua, hệ thống xử lý theo policy overdue.

Kết quả kỳ vọng: Deadline hợp lệ được ghi nhận; deadline không hợp lệ bị từ chối.

### 14.4 Gán Priority

User gán priority High cho Task. Hệ thống kiểm tra High thuộc tập giá trị cho phép và cập nhật Task.

Kết quả kỳ vọng: Task được ưu tiên đúng để phục vụ lọc và sắp xếp.

### 14.5 Gán Category

User gán Task vào Category "Study" hoặc category hợp lệ khác. Hệ thống kiểm tra Category thuộc phạm vi của User hoặc phạm vi dùng chung được cho phép.

Kết quả kỳ vọng: Task được phân loại theo Category.

### 14.6 Gán Tag

User gắn nhiều Tag cho Task như "urgent", "home", "focus". Hệ thống kiểm tra Tag hợp lệ và không vượt giới hạn nếu có.

Kết quả kỳ vọng: Task có nhiều Tag hỗ trợ tìm kiếm và lọc.

### 14.7 Đưa Task lên Timeline

User có Task Planned với Time Capital hợp lệ. User chọn thời điểm thực hiện. Hệ thống kiểm tra Timeline eligibility, deadline và conflict. Nếu hợp lệ, Task xuất hiện trên Timeline.

Kết quả kỳ vọng: Task được schedule và hiển thị trên Timeline.

### 14.8 Kéo thả Timeline

User kéo Task trên Timeline sang khung giờ khác. Hệ thống kiểm tra vị trí mới, deadline, conflict và policy chu kỳ. Nếu hợp lệ, schedule được cập nhật.

Kết quả kỳ vọng: Task đổi lịch thực hiện mà vẫn tuân thủ rule.

### 14.9 Tạm dừng Task

User đang thực hiện Task nhưng cần tạm dừng. User chọn Pause. Hệ thống kiểm tra Task đang In Progress và chuyển Task sang On Hold.

Kết quả kỳ vọng: Task tạm dừng nhưng chưa bị hủy.

### 14.10 Tiếp tục Task

User quay lại Task đang On Hold và chọn Resume. Hệ thống chuyển Task sang In Progress.

Kết quả kỳ vọng: Task tiếp tục được thực hiện.

### 14.11 Hoàn thành Task

User hoàn tất công việc và chọn Complete. Hệ thống kiểm tra trạng thái và completion rule. Nếu hợp lệ, Task chuyển sang Completed.

Kết quả kỳ vọng: Task hoàn thành và sẵn sàng làm đầu vào cho giai đoạn sau.

### 14.12 Hủy Task

User quyết định không thực hiện Task nữa. User chọn Cancel và nhập lý do nếu policy yêu cầu. Hệ thống chuyển Task sang Cancelled.

Kết quả kỳ vọng: Task không còn là công việc đang thực hiện.

### 14.13 Mở lại Task

User phát hiện Task Completed cần chỉnh sửa hoặc tiếp tục. User chọn Reopen nếu policy cho phép. Hệ thống yêu cầu lý do nếu cần và chuyển Task về trạng thái phù hợp.

Kết quả kỳ vọng: Task được mở lại theo policy và có thể chỉnh sửa hoặc tiếp tục.

## 15. Edge Cases

| Edge Case ID | Scenario | Expected Business Handling |
|---|---|---|
| TTM-EC-001 | Deadline đã qua khi User tạo Task. | Hệ thống cảnh báo hoặc từ chối theo deadline policy. |
| TTM-EC-002 | Task không có Time Capital. | Task có thể tồn tại trong Task list nhưng không xuất hiện trên Timeline. |
| TTM-EC-003 | Task không có Money Capital. | Task vẫn có thể hợp lệ nếu estimated cost không bắt buộc. |
| TTM-EC-004 | Task bị kéo chồng lên Task khác. | Hệ thống xử lý theo Timeline conflict policy. |
| TTM-EC-005 | Task hoàn thành nhưng User muốn sửa planning. | Hệ thống yêu cầu Reopen nếu planning quan trọng bị thay đổi. |
| TTM-EC-006 | Task đang thực hiện nhưng bị hủy. | Hệ thống chuyển sang Cancelled nếu policy cho phép và xử lý Timeline placement. |
| TTM-EC-007 | Task có nhiều Tag. | Hệ thống cho phép nếu không vượt tag limit. |
| TTM-EC-008 | Task không có Category. | Hệ thống cho phép nếu Category không bắt buộc; nếu bắt buộc thì không cho Planned/Scheduled. |
| TTM-EC-009 | Recurring Task trùng Deadline. | Hệ thống xử lý theo recurring và deadline policy. |
| TTM-EC-010 | Recurring occurrence rơi vào ngày đã có Task khác. | Hệ thống xử lý theo Timeline conflict policy. |
| TTM-EC-011 | User kéo Task sang thời điểm sau deadline. | Hệ thống từ chối hoặc cảnh báo theo policy; mặc định không cho nếu rule cấm. |
| TTM-EC-012 | User kéo Task sang chu kỳ nguồn lực không hợp lệ. | Hệ thống từ chối nếu policy không cho phép. |
| TTM-EC-013 | User đặt progress 150%. | Hệ thống từ chối. |
| TTM-EC-014 | User đặt progress -10%. | Hệ thống từ chối. |
| TTM-EC-015 | Progress 100% nhưng User chưa complete. | Hệ thống xử lý theo completion policy, có thể đề xuất complete. |
| TTM-EC-016 | User complete Task ở Draft. | Hệ thống từ chối nếu quick completion không được policy cho phép. |
| TTM-EC-017 | User cancel Task Completed. | Hệ thống yêu cầu Reopen trước nếu policy không cho cancel trực tiếp. |
| TTM-EC-018 | User restore Archived Task có schedule cũ đã qua. | Hệ thống xử lý theo restore policy, có thể không đưa Task về Timeline ngay. |
| TTM-EC-019 | User duplicate Archived Task. | Hệ thống xử lý theo duplicate policy; Task mới không nên tự archived nếu policy không quy định. |
| TTM-EC-020 | User remove Category khi Category bắt buộc. | Hệ thống từ chối hoặc chuyển Task về trạng thái cần bổ sung planning. |
| TTM-EC-021 | User gắn Tag không thuộc phạm vi của mình. | Hệ thống từ chối. |
| TTM-EC-022 | User schedule Task có estimated time bằng 0. | Hệ thống từ chối Timeline placement. |
| TTM-EC-023 | User đặt estimated cost âm. | Hệ thống từ chối. |
| TTM-EC-024 | User tìm kiếm Task đã archived. | Hệ thống trả kết quả chỉ khi filter bao gồm archived hoặc policy cho phép. |
| TTM-EC-025 | User filter theo nhiều Tag đồng thời. | Hệ thống áp dụng logic filter theo policy cần xác nhận. |
| TTM-EC-026 | User thao tác Task trên nhiều thiết bị đồng thời. | Kết quả phải tuân thủ status và validation tại thời điểm hành động. |
| TTM-EC-027 | Task đang On Hold bị reschedule. | Hệ thống cho phép hoặc từ chối theo scheduling policy. |
| TTM-EC-028 | Reminder đặt sau deadline. | Hệ thống từ chối nếu reminder policy không cho phép. |
| TTM-EC-029 | Reminder đặt cho Cancelled Task. | Hệ thống từ chối nếu policy không cho reminder trên Task không còn active. |
| TTM-EC-030 | Staff cố sửa Task của User. | Hệ thống từ chối nếu không có policy ngoại lệ. |
| TTM-EC-031 | Admin cố complete Task của User. | Hệ thống từ chối nếu không có policy ngoại lệ. |
| TTM-EC-032 | Task bị archive khi đang trên Timeline. | Hệ thống loại khỏi Timeline chính theo archive policy. |
| TTM-EC-033 | Task reopened sau completed có schedule cũ đã qua. | Hệ thống yêu cầu xử lý schedule theo reopen policy. |
| TTM-EC-034 | Task cancelled nhưng vẫn còn trên Timeline. | Hệ thống phải xử lý theo cancellation policy để tránh hiểu nhầm là còn cần thực hiện. |
| TTM-EC-035 | Task có deadline nhưng không có schedule. | Task vẫn là Planned, chưa xuất hiện Timeline nếu không có Time Capital. |

## 16. Validation Rules

| Validation Rule ID | Rule |
|---|---|
| TTM-VR-001 | Task Name là bắt buộc. |
| TTM-VR-002 | Task Name không được rỗng hoặc chỉ chứa khoảng trắng. |
| TTM-VR-003 | Task phải thuộc một User sở hữu. |
| TTM-VR-004 | User chỉ được thao tác Task thuộc sở hữu của mình. |
| TTM-VR-005 | Priority phải thuộc tập giá trị được phê duyệt. |
| TTM-VR-006 | Deadline phải là giá trị thời gian hợp lệ. |
| TTM-VR-007 | Deadline không được nhỏ hơn scheduled start nếu Task đã có scheduled start. |
| TTM-VR-008 | Estimated Time phải lớn hơn 0 nếu Task được schedule. |
| TTM-VR-009 | Estimated Cost phải lớn hơn hoặc bằng 0. |
| TTM-VR-010 | Progress phải nằm trong khoảng 0 đến 100. |
| TTM-VR-011 | Task không có Time Capital hoặc estimated time hợp lệ không được xuất hiện trên Timeline. |
| TTM-VR-012 | Schedule phải hợp lệ theo Timeline Policy. |
| TTM-VR-013 | Drag & Drop phải kiểm tra schedule mới trước khi áp dụng. |
| TTM-VR-014 | Timeline conflict phải xử lý theo policy. |
| TTM-VR-015 | Category phải hợp lệ nếu được gán. |
| TTM-VR-016 | Category không được gỡ nếu Category bắt buộc cho trạng thái hiện tại. |
| TTM-VR-017 | Tag phải hợp lệ nếu được gán. |
| TTM-VR-018 | Số lượng Tag không được vượt giới hạn nếu có. |
| TTM-VR-019 | Task Completed không được sửa planning quan trọng nếu chưa reopen. |
| TTM-VR-020 | Task Cancelled không được update progress nếu chưa reopen. |
| TTM-VR-021 | Task Archived không được xuất hiện trên Timeline chính nếu policy không cho phép. |
| TTM-VR-022 | Restore Task phải xác định trạng thái sau restore theo policy. |
| TTM-VR-023 | Reopen Task chỉ được thực hiện nếu policy cho phép. |
| TTM-VR-024 | Delete Task chỉ được thực hiện ở trạng thái được policy cho phép. |
| TTM-VR-025 | Recurring rule phải hợp lệ nếu recurring được sử dụng. |
| TTM-VR-026 | Reminder time phải hợp lệ nếu reminder được sử dụng. |
| TTM-VR-027 | Search criteria phải hợp lệ theo policy. |
| TTM-VR-028 | Filter criteria phải hợp lệ theo policy. |
| TTM-VR-029 | Sort criteria phải thuộc tập tiêu chí được phê duyệt. |
| TTM-VR-030 | Hành động bị từ chối không được làm thay đổi Task. |

## 17. Business Policies

### 17.1 Task Planning Policy

Task Planning Policy định nghĩa các điều kiện để một Task được xem là Planned. Chính sách này cần xác định trường nào là bắt buộc, trường nào là tùy chọn và trạng thái nào được phép cập nhật planning.

Nguyên tắc chính:

- Task Name luôn bắt buộc.
- Priority, deadline, estimated time, estimated cost, category và tag có thể bắt buộc hoặc tùy chọn theo policy.
- Task chỉ chuyển sang Planned khi đáp ứng thông tin planning tối thiểu.
- Completed Task không được sửa planning quan trọng nếu chưa reopen.

### 17.2 Timeline Policy

Timeline Policy định nghĩa điều kiện Task xuất hiện trên Timeline và cách xử lý thay đổi lịch.

Nguyên tắc chính:

- Timeline chỉ hiển thị Task có Time Capital hoặc estimated time hợp lệ.
- Task phải có schedule hợp lệ để xuất hiện trên Timeline.
- Drag & Drop phải được kiểm tra trước khi áp dụng.
- Timeline conflict phải được xử lý theo policy: từ chối, cảnh báo hoặc cho phép có xác nhận.
- Archived hoặc Cancelled Task không nên xuất hiện trên Timeline chính nếu policy không quy định khác.

### 17.3 Priority Policy

Priority Policy định nghĩa tập giá trị priority và ý nghĩa của từng mức.

Nguyên tắc chính:

- Priority phải thuộc tập giá trị được phê duyệt.
- Priority hỗ trợ lọc, sắp xếp và ra quyết định.
- Priority không tự động thay đổi deadline hoặc schedule nếu chưa có policy.

### 17.4 Task Completion Policy

Task Completion Policy định nghĩa điều kiện để Task được chuyển sang Completed.

Nguyên tắc chính:

- Task phải ở trạng thái cho phép completion.
- Progress có thể cần đạt 100% nếu policy yêu cầu.
- User có thể cần xác nhận completion.
- Completed Task trở thành đầu vào cho giai đoạn sau.
- Completed Task bị hạn chế sửa planning nếu chưa reopen.

### 17.5 Task Cancellation Policy

Task Cancellation Policy định nghĩa khi nào User được hủy Task.

Nguyên tắc chính:

- Task có thể được cancel từ Draft, Planned, Scheduled, In Progress hoặc On Hold nếu policy cho phép.
- Cancelled Task không còn là công việc đang thực hiện.
- Lý do cancellation có thể bắt buộc.
- Cancelled Task có thể được archive hoặc reopen nếu policy cho phép.

### 17.6 Task Archive Policy

Task Archive Policy định nghĩa cách lưu trữ và khôi phục Task.

Nguyên tắc chính:

- Archive không đồng nghĩa delete.
- Archived Task không xuất hiện trong danh sách làm việc chính hoặc Timeline chính nếu policy không quy định khác.
- Restore Task phải xác định trạng thái sau restore.
- Archived Task không nên được chỉnh sửa planning nếu chưa restore.

### 17.7 Recurring Task Policy

Recurring Task Policy định nghĩa việc hỗ trợ Task lặp lại nếu được phê duyệt.

Nguyên tắc chính:

- Recurring rule phải hợp lệ.
- Occurrence phải tuân thủ deadline, schedule, Time Capital và Timeline policy.
- Recurring Task trùng lịch phải xử lý theo conflict policy.
- Nếu recurring chưa được phê duyệt, nội dung liên quan chỉ là phạm vi tùy chọn và Open Question.

## 18. Risks

### 18.1 Business Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Task bị hiểu như to-do đơn giản | User có thể bỏ qua planning và nguồn lực. | Giảm khác biệt của LifeBalance. | Nhấn mạnh Task là cam kết có kế hoạch và nguồn lực. |
| Quá nhiều trạng thái Task | Lifecycle phức tạp có thể gây khó hiểu. | Giảm usability và adoption. | Giải thích rõ trạng thái và chỉ dùng trạng thái có giá trị. |
| Recurring Task quá phức tạp | Quy tắc lặp có nhiều ngoại lệ. | Tăng rủi ro sai lịch. | Đưa recurring vào phạm vi tùy chọn nếu chưa đủ policy. |
| Completed Task bị sửa tùy tiện | Dữ liệu kế hoạch sau hoàn thành mất ý nghĩa. | Ảnh hưởng đầu vào cho đánh giá. | Yêu cầu reopen trước khi sửa planning quan trọng. |

### 18.2 Operational Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Staff bị kỳ vọng sửa Task cho User | Người dùng có thể yêu cầu Staff can thiệp kế hoạch cá nhân. | Rủi ro quyền riêng tư và trách nhiệm. | Giới hạn rõ quyền Staff. |
| Admin can thiệp Task cá nhân | Quyền quản trị bị hiểu là quyền sửa mọi Task. | Giảm niềm tin của User. | Tách quản trị policy khỏi dữ liệu Task cá nhân. |
| Không có history cho thay đổi quan trọng | Khó giải thích vì sao lịch thay đổi. | Giảm khả năng truy vết. | Ghi nhận history cho status và schedule theo policy. |

### 18.3 Planning Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| User không nhập estimated time | Task không xuất hiện Timeline. | Lịch thực hiện không đầy đủ. | Truyền đạt rõ điều kiện Timeline. |
| Deadline không thực tế | User đặt deadline quá gần hoặc đã qua. | Kế hoạch thiếu khả thi. | Cảnh báo deadline và overdue. |
| Priority bị lạm dụng | User đặt quá nhiều Task priority cao. | Mất ý nghĩa ưu tiên. | Cân nhắc hướng dẫn priority policy. |
| Category/Tag thiếu nhất quán | User phân loại không đồng nhất. | Tìm kiếm và lọc kém hiệu quả. | Đề xuất chuẩn hóa category/tag ở volume liên quan. |

### 18.4 Scheduling Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Timeline conflict không được xử lý rõ | Task bị chồng lịch không kiểm soát. | User lập lịch thiếu thực tế. | Xác định conflict policy rõ ràng. |
| Drag & Drop gây thay đổi ngoài ý muốn | User vô tình đổi lịch. | Sai kế hoạch. | Cần xác nhận hoặc undo policy nếu được phê duyệt. |
| Task archived vẫn xuất hiện Timeline | User hiểu nhầm Task vẫn cần thực hiện. | Nhiễu lịch. | Archive policy phải loại khỏi Timeline chính. |
| Recurring occurrence dày đặc | Lịch bị quá tải. | Giảm tính khả thi. | Cần recurring validation và cảnh báo. |

## 19. Open Questions

| Question ID | Open Question | Impact Area |
|---|---|---|
| TTM-OQ-001 | Trường nào là bắt buộc để Task chuyển từ Draft sang Planned? | Task Planning |
| TTM-OQ-002 | Priority dùng tập giá trị nào: Low/Medium/High/Critical hay tập khác? | Priority |
| TTM-OQ-003 | Category có bắt buộc cho Task không? | Category |
| TTM-OQ-004 | Một Task có được gán nhiều Category không, hay chỉ một Category chính? | Category |
| TTM-OQ-005 | Số lượng Tag tối đa trên một Task là bao nhiêu? | Tag |
| TTM-OQ-006 | Task không có Time Capital có được schedule bằng cách khác không? | Timeline |
| TTM-OQ-007 | Timeline có cho phép Task chồng lịch không? | Timeline Conflict |
| TTM-OQ-008 | Nếu cho phép chồng lịch, có cần User xác nhận không? | Timeline Conflict |
| TTM-OQ-009 | Drag & Drop có cần xác nhận trước khi lưu thay đổi không? | Timeline |
| TTM-OQ-010 | Có cần hỗ trợ undo cho thay đổi Timeline không? | Timeline |
| TTM-OQ-011 | Deadline đã qua có được tạo hoặc cập nhật không? | Deadline |
| TTM-OQ-012 | Progress 100% có tự động chuyển Task sang Completed không? | Completion |
| TTM-OQ-013 | Task Draft có được complete trực tiếp không? | Completion |
| TTM-OQ-014 | Completed Task có được reopen không? | Reopen |
| TTM-OQ-015 | Cancelled Task có được reopen không? | Reopen |
| TTM-OQ-016 | Khi restore Archived Task, Task quay về trạng thái trước archive hay trạng thái mặc định? | Archive |
| TTM-OQ-017 | Delete Task có được hỗ trợ hay chỉ archive? | Delete/Archive |
| TTM-OQ-018 | Recurring Task có thuộc phạm vi release hiện tại không? | Recurring |
| TTM-OQ-019 | Nếu có recurring, rule lặp được hỗ trợ gồm những loại nào? | Recurring |
| TTM-OQ-020 | Reminder có thuộc phạm vi module hiện tại không? | Reminder |
| TTM-OQ-021 | Reminder dựa trên deadline, schedule hay cả hai? | Reminder |
| TTM-OQ-022 | Staff có được xem Task detail để hỗ trợ User không? | Access |
| TTM-OQ-023 | Admin có được xem Task cá nhân ở mức nào? | Access |
| TTM-OQ-024 | Các thay đổi nào của Task bắt buộc ghi history? | Auditability |
| TTM-OQ-025 | Các thay đổi Timeline nào bắt buộc ghi history? | Auditability |

## 20. Suggested Improvements

| Improvement ID | Suggested Improvement | Business Rationale |
|---|---|---|
| TTM-SI-001 | Chuẩn hóa bộ Priority và giải thích ý nghĩa từng mức. | Giúp User dùng priority nhất quán. |
| TTM-SI-002 | Cung cấp cảnh báo khi Task deadline gần đến nhưng chưa scheduled. | Giúp User chủ động đưa Task vào Timeline. |
| TTM-SI-003 | Cảnh báo khi User có quá nhiều Task cùng priority cao. | Giữ ý nghĩa của mức ưu tiên. |
| TTM-SI-004 | Cho phép User xem Task không có Time Capital trong một danh sách riêng. | Giúp User biết Task nào chưa đủ điều kiện Timeline. |
| TTM-SI-005 | Đề xuất User bổ sung estimated time cho Task có deadline. | Tăng khả năng lập lịch thực tế. |
| TTM-SI-006 | Thiết lập policy xác nhận cho drag & drop ảnh hưởng lớn. | Giảm rủi ro đổi lịch ngoài ý muốn. |
| TTM-SI-007 | Cung cấp trạng thái Overdue rõ ràng cho Task quá deadline. | Giúp User ưu tiên xử lý. |
| TTM-SI-008 | Tách Cancelled và Archived trong cách hiển thị. | Tránh nhầm giữa Task bị hủy và Task được lưu trữ. |
| TTM-SI-009 | Bắt buộc reason khi cancel hoặc reopen Task quan trọng. | Tăng khả năng tự đánh giá và truy vết. |
| TTM-SI-010 | Xem xét recurring task sau khi lifecycle cơ bản ổn định. | Giảm độ phức tạp release đầu. |
| TTM-SI-011 | Cung cấp non-drag scheduling option cho accessibility. | Giúp User không phụ thuộc vào kéo thả. |
| TTM-SI-012 | Gợi ý archive Task Completed sau một thời gian. | Giữ danh sách Task hiện tại gọn gàng. |
| TTM-SI-013 | Xây dựng rule chống chồng lịch theo preference của User. | Tăng tính linh hoạt nhưng vẫn kiểm soát scheduling. |
| TTM-SI-014 | Cung cấp quick filter cho unscheduled Task. | Giúp User hoàn thiện kế hoạch. |
| TTM-SI-015 | Cung cấp history cho thay đổi schedule quan trọng. | Giúp User hiểu vì sao Timeline thay đổi. |

## Appendix A. Traceability Summary

| Source | Related TTM Content |
|---|---|
| Volume 1 - Product Philosophy | Task được xem là cam kết tiêu tốn nguồn lực và cần được lập kế hoạch trước khi thực hiện. |
| Volume 1 - Business Scope | Task Management và Timeline được triển khai thành module SRS riêng trong Volume 4. |
| Volume 2 - Identity & Authorization | Ownership validation và actor permission được kế thừa để bảo vệ Task cá nhân. |
| Volume 3 - Resource Capital Management | Time Capital và Money Capital được tham chiếu để lập kế hoạch và xác định điều kiện Timeline. |

## Appendix B. TTM Glossary

| Term | Definition |
|---|---|
| Archived | Trạng thái Task được lưu trữ và không còn xuất hiện trong danh sách làm việc chính theo policy. |
| Cancelled | Trạng thái Task bị hủy và không tiếp tục thực hiện. |
| Category | Phân loại có cấu trúc dùng để nhóm Task theo ý nghĩa nghiệp vụ. |
| Completed | Trạng thái Task đã được User xác nhận hoàn thành. |
| Deadline | Thời hạn Task cần được hoàn thành hoặc xử lý. |
| Draft | Trạng thái Task mới tạo hoặc chưa đủ thông tin planning. |
| Estimated Cost | Money Capital dự kiến cần cho Task. |
| Estimated Time | Time Capital hoặc thời lượng dự kiến cần cho Task. |
| In Progress | Trạng thái Task đang được thực hiện. |
| On Hold | Trạng thái Task tạm dừng nhưng chưa hủy. |
| Planned | Trạng thái Task đã có thông tin kế hoạch đủ theo policy. |
| Priority | Mức độ ưu tiên của Task. |
| Progress | Mức độ hoàn thành của Task, thường từ 0% đến 100%. |
| Recurring Task | Task lặp lại theo quy luật được phê duyệt. |
| Scheduled | Trạng thái Task đã được đặt lịch và có thể xuất hiện trên Timeline. |
| Tag | Nhãn linh hoạt dùng để phân loại hoặc tìm kiếm Task. |
| Task | Đơn vị công việc cá nhân mà User dự định thực hiện. |
| Task Plan | Tập hợp thông tin kế hoạch của Task. |
| Task Schedule | Thông tin thời điểm Task dự kiến được thực hiện. |
| Timeline | Góc nhìn theo thời gian hiển thị các Task đủ điều kiện scheduling. |

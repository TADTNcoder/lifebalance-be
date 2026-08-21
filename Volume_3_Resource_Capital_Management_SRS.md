# LifeBalance
# Volume 3 - Resource Capital Management SRS

## 1. Module Overview

### 1.1 Purpose

Resource Capital Management là module chịu trách nhiệm quản lý nguồn vốn cá nhân của người dùng trong LifeBalance. Nguồn vốn ở đây được hiểu là tổng năng lực nguồn lực mà người dùng có thể sử dụng để lập kế hoạch, phân bổ, theo dõi và đánh giá việc sử dụng nguồn lực trong một chu kỳ xác định. Theo định hướng của LifeBalance, hai nguồn lực cốt lõi được quản lý là Time Capital và Money Capital.

Module này được xây dựng dựa trên triết lý sản phẩm đã được xác lập ở Volume 1:

1. Mọi công việc đều tiêu tốn nguồn lực.
2. Mọi nguồn lực đều hữu hạn.

Do đó, trước khi một hoạt động hoặc hạng mục công việc được thực hiện, người dùng cần có khả năng xác định nguồn lực khả dụng, phân bổ một phần nguồn lực cho mục đích sử dụng, điều chỉnh nguồn vốn khi điều kiện thay đổi, theo dõi số dư nguồn lực và xem lại lịch sử thay đổi. Module này không mô tả cách quản lý công việc, không mô tả quy trình thực hiện công việc và không mô tả báo cáo phân tích. Trọng tâm duy nhất là quản lý nguồn vốn dùng làm cơ sở cho các quyết định lập kế hoạch và phân bổ.

Resource Capital Management trả lời các câu hỏi nghiệp vụ sau:

- Người dùng đang có bao nhiêu Time Capital và Money Capital trong chu kỳ hiện tại?
- Nguồn vốn nào đã được phân bổ?
- Nguồn vốn nào còn lại để tiếp tục lập kế hoạch?
- Khi nguồn vốn thay đổi, thay đổi đó được ghi nhận như thế nào?
- Người dùng có được phép phân bổ vượt nguồn lực khả dụng hay không?
- Số dư cuối chu kỳ được xử lý như thế nào nếu chính sách cho phép?
- Lịch sử thay đổi nguồn vốn có thể được truy vết ở mức nghiệp vụ hay không?

### 1.2 Business Value

Resource Capital Management tạo ra giá trị nghiệp vụ cốt lõi cho LifeBalance vì hệ thống không chỉ hỗ trợ ghi nhận việc cần làm, mà còn hỗ trợ người dùng ra quyết định dựa trên năng lực nguồn lực hữu hạn.

Thứ nhất, module giúp người dùng nhận thức rõ về năng lực nguồn lực. Thay vì lập kế hoạch dựa trên cảm tính, người dùng có thể nhìn thấy tổng thời gian và tiền bạc có thể sử dụng trong ngày, tuần hoặc tháng.

Thứ hai, module hỗ trợ phân bổ có trách nhiệm. Khi nguồn lực được cấp cho một mục đích sử dụng, phần nguồn lực đó không còn được xem như hoàn toàn tự do. Điều này giúp người dùng hạn chế tình trạng cam kết quá mức hoặc sử dụng cùng một nguồn lực cho nhiều mục tiêu mâu thuẫn.

Thứ ba, module hỗ trợ kiểm soát sai lệch giữa kế hoạch và thực tế. Mặc dù đánh giá chi tiết planned và actual có thể được khai thác ở các module khác, Resource Capital Management cung cấp nền tảng để xác định planned capital, allocated capital, remaining capital và actual resource consumption ở mức nguồn vốn.

Thứ tư, module giúp hình thành kỷ luật tài chính và thời gian cá nhân. Khi người dùng thấy rõ số dư nguồn lực, lịch sử điều chỉnh và tác động của từng quyết định phân bổ, họ có thêm cơ sở để ra quyết định phù hợp hơn.

Thứ năm, module tạo nền tảng dữ liệu nghiệp vụ cho truy vết và kiểm tra. Mọi thay đổi quan trọng về vốn nguồn lực cần được ghi nhận để người dùng có thể hiểu vì sao số dư thay đổi và các bên có thẩm quyền có thể kiểm tra khi cần theo phạm vi được phép.

### 1.3 Objectives

| Objective ID | Objective | Description |
|---|---|---|
| RCM-OBJ-001 | Quản lý chu kỳ nguồn vốn | Hỗ trợ người dùng tạo, kích hoạt, cập nhật, đóng và xem chu kỳ nguồn vốn theo ngày, tuần hoặc tháng. |
| RCM-OBJ-002 | Quản lý Time Capital | Cho phép người dùng xác định, điều chỉnh và theo dõi vốn thời gian trong một chu kỳ. |
| RCM-OBJ-003 | Quản lý Money Capital | Cho phép người dùng xác định, điều chỉnh và theo dõi vốn tiền bạc trong một chu kỳ. |
| RCM-OBJ-004 | Hỗ trợ phân bổ nguồn vốn | Cho phép người dùng phân bổ một phần nguồn vốn khả dụng cho mục đích sử dụng đã xác định. |
| RCM-OBJ-005 | Kiểm soát vượt mức | Ngăn phân bổ vượt nguồn lực khả dụng nếu người dùng chưa chủ động cho phép vượt mức. |
| RCM-OBJ-006 | Hỗ trợ điều chỉnh nguồn vốn | Cho phép người dùng điều chỉnh nguồn vốn khi kế hoạch hoặc điều kiện thực tế thay đổi. |
| RCM-OBJ-007 | Quản lý số dư nguồn lực | Cung cấp khả năng xem available capital, allocated capital và remaining capital. |
| RCM-OBJ-008 | Ghi nhận lịch sử thay đổi | Bảo đảm các thay đổi quan trọng về nguồn vốn, phân bổ và điều chỉnh có thể được truy vết ở mức nghiệp vụ. |
| RCM-OBJ-009 | Hỗ trợ chuyển số dư nếu được phê duyệt | Cho phép xử lý remaining capital cuối chu kỳ theo chính sách nghiệp vụ nếu được xác nhận. |
| RCM-OBJ-010 | Bảo vệ quyền sở hữu dữ liệu cá nhân | Bảo đảm chỉ người sở hữu hoặc actor được phân quyền hợp lệ mới có thể xem hoặc quản lý nguồn vốn cá nhân. |

### 1.4 Responsibilities

Module Resource Capital Management chịu trách nhiệm đối với các năng lực nghiệp vụ sau:

| Responsibility | Description |
|---|---|
| Capital Cycle Management | Quản lý vòng đời chu kỳ nguồn vốn gồm daily, weekly và monthly. |
| Capital Initialization | Ghi nhận nguồn vốn ban đầu của Time Capital và Money Capital cho một chu kỳ. |
| Capital Update | Cập nhật thông tin chu kỳ hoặc giá trị nguồn vốn trong phạm vi chính sách cho phép. |
| Capital Adjustment | Điều chỉnh tăng hoặc giảm nguồn vốn và ghi nhận lý do thay đổi nếu chính sách yêu cầu. |
| Capital Allocation | Phân bổ nguồn vốn cho mục đích sử dụng đã xác định ở mức nghiệp vụ. |
| Reallocation | Điều chỉnh lại lượng nguồn vốn đã phân bổ khi kế hoạch thay đổi. |
| Release Allocation | Giải phóng nguồn vốn đã phân bổ nhưng không còn cần giữ lại. |
| Balance Calculation | Xác định available capital, allocated capital và remaining capital theo quy tắc nghiệp vụ. |
| Over Allocation Control | Kiểm soát trường hợp phân bổ vượt khả dụng và yêu cầu người dùng chủ động cho phép nếu chính sách áp dụng. |
| Capital History | Cung cấp lịch sử thay đổi, điều chỉnh và phân bổ nguồn vốn để phục vụ truy vết. |
| Ownership Control | Bảo đảm người dùng chỉ quản lý nguồn vốn của chính mình trừ trường hợp actor khác được định nghĩa rõ ràng. |

## 2. Business Scope

### 2.1 In Scope

| Scope Area | Description |
|---|---|
| Capital Cycle | Quản lý chu kỳ nguồn vốn theo daily, weekly và monthly. |
| Time Capital | Thiết lập, điều chỉnh, phân bổ và theo dõi vốn thời gian. |
| Money Capital | Thiết lập, điều chỉnh, phân bổ và theo dõi vốn tiền bạc. |
| Planned Capital | Ghi nhận nguồn vốn dự kiến được dùng cho lập kế hoạch trong chu kỳ. |
| Available Capital | Xác định nguồn lực có thể sử dụng trước khi phân bổ. |
| Allocated Capital | Theo dõi phần nguồn lực đã được phân bổ cho mục đích sử dụng. |
| Remaining Capital | Theo dõi phần nguồn lực còn lại sau khi xét nguồn vốn và phân bổ. |
| Capital Adjustment | Điều chỉnh tăng hoặc giảm nguồn vốn khi điều kiện thay đổi. |
| Capital Allocation | Phân bổ Time Capital hoặc Money Capital cho mục đích sử dụng. |
| Reallocation | Thay đổi lượng nguồn vốn đã phân bổ khi kế hoạch thay đổi. |
| Release Allocated Capital | Giải phóng nguồn vốn đã phân bổ nhưng không còn cần giữ. |
| Allow Over Allocation | Cho phép người dùng chủ động chấp nhận phân bổ vượt khả dụng theo chính sách. |
| Capital Balance View | Xem số dư nguồn vốn theo loại nguồn lực và chu kỳ. |
| Capital History | Xem lịch sử phân bổ, điều chỉnh, đóng chu kỳ và thay đổi quan trọng. |
| Capital Summary | Xem tóm tắt trạng thái nguồn vốn trong chu kỳ ở mức module. |
| Search and Filter | Tìm kiếm chu kỳ và lọc lịch sử nguồn vốn theo tiêu chí nghiệp vụ. |

### 2.2 Out of Scope

| Out of Scope Area | Explanation |
|---|---|
| Quản lý chi tiết công việc | Module này không mô tả cách tạo, sửa, phân loại, theo dõi hoặc hoàn thành công việc. |
| Quản lý timeline nghiệp vụ | Module này không mô tả lịch, trình tự thời gian thực hiện hoặc hiển thị timeline. |
| Dashboard nghiệp vụ | Module này không mô tả dashboard tổng hợp hoặc chỉ số trực quan ngoài tóm tắt nguồn vốn trong phạm vi module. |
| Reporting phân tích | Module này không mô tả báo cáo phân tích nâng cao, biểu đồ, xu hướng hoặc phân tích hiệu quả theo nhiều chiều. |
| Quản trị nền tảng | Module này không mô tả quy trình vận hành hệ thống hoặc quản lý người dùng ở cấp quản trị. |
| Chi tiêu kế toán | Money Capital không phải là hệ thống kế toán, sổ sách tài chính, thuế hoặc quản lý giao dịch tài chính chuyên nghiệp. |
| Tư vấn tài chính | Hệ thống không đưa ra lời khuyên tài chính chuyên môn, không thay thế chuyên gia tài chính. |
| Tự động đồng bộ nguồn tiền bên ngoài | Việc lấy dữ liệu tài chính từ bên ngoài không thuộc phạm vi hiện tại nếu chưa được phê duyệt. |
| Đánh giá chi tiết hiệu quả công việc | Module chỉ cung cấp dữ liệu nguồn vốn nền tảng; đánh giá chi tiết theo công việc thuộc phạm vi khác. |

### 2.3 Dependencies

| Dependency | Description | Impact |
|---|---|---|
| Volume 1 - Vision & Business Overview | Cung cấp triết lý nguồn lực hữu hạn, thuật ngữ, actor và phạm vi tổng thể. | Resource Capital Management phải tuân thủ nguyên tắc resource-aware planning. |
| Volume 2 - Identity & Authorization | Cung cấp kiểm soát đăng nhập, quyền sở hữu và phân quyền actor. | Chỉ actor hợp lệ mới được xem hoặc quản lý nguồn vốn. |
| Chính sách chu kỳ | Cần xác định quy tắc daily, weekly, monthly, thời điểm bắt đầu và kết thúc chu kỳ. | Ảnh hưởng đến tạo, đóng, kích hoạt và tìm kiếm chu kỳ. |
| Chính sách vượt mức | Cần xác định khi nào người dùng được phép phân bổ vượt nguồn lực khả dụng. | Ảnh hưởng đến allocation validation và cảnh báo nghiệp vụ. |
| Chính sách chuyển số dư | Cần xác định remaining capital có được chuyển sang chu kỳ sau hay không. | Ảnh hưởng đến close cycle và transfer remaining capital. |
| Chính sách lịch sử | Cần xác định loại thay đổi nào phải được ghi nhận và thời hạn truy vết nghiệp vụ. | Ảnh hưởng đến auditability và history view. |
| Chính sách quyền sở hữu dữ liệu | Cần xác nhận Staff hoặc Admin có được xem nguồn vốn cá nhân của User hay không. | Ảnh hưởng đến quyền truy cập, hỗ trợ và bảo vệ dữ liệu cá nhân. |

## 3. Business Concepts

### 3.1 Resource

Resource là nguồn lực hữu hạn mà người dùng có thể sử dụng để thực hiện các hoạt động trong LifeBalance. Trong phạm vi hiện tại, Resource bao gồm Time và Money. Resource được xem là yếu tố đầu vào của quá trình lập kế hoạch và là cơ sở để đánh giá mức độ hiệu quả sau khi thực tế diễn ra.

Resource không được hiểu là một tài sản vô hạn. Khi một phần Resource được phân bổ, phần đó làm giảm khả năng sử dụng cho các mục đích khác trong cùng chu kỳ, trừ khi người dùng điều chỉnh, giải phóng hoặc chủ động cho phép vượt mức.

### 3.2 Capital

Capital là tổng năng lực nguồn lực mà người dùng xác định cho một chu kỳ nhất định. Capital là khái niệm nền tảng dùng để trả lời câu hỏi: người dùng có bao nhiêu nguồn lực để lập kế hoạch và phân bổ trong một khoảng thời gian.

Capital không chỉ là số liệu tĩnh. Capital có thể được tạo, cập nhật, điều chỉnh, phân bổ, giải phóng và đóng lại theo chu kỳ. Mọi thay đổi quan trọng của Capital cần có khả năng truy vết ở mức nghiệp vụ.

### 3.3 Time Capital

Time Capital là lượng thời gian mà người dùng xác định có thể sử dụng trong một chu kỳ. Time Capital có thể được biểu diễn bằng đơn vị thời gian phù hợp, ví dụ phút hoặc giờ, tùy chính sách sản phẩm được xác nhận.

Time Capital có thể dùng để lập kế hoạch và phân bổ cho các mục đích sử dụng khác nhau. Khi Time Capital đã được phân bổ, phần thời gian đó được xem như đã có cam kết kế hoạch. Nếu kế hoạch thay đổi, người dùng có thể điều chỉnh, phân bổ lại hoặc giải phóng phần Time Capital đã phân bổ theo business rule.

### 3.4 Money Capital

Money Capital là lượng tiền mà người dùng xác định có thể sử dụng trong một chu kỳ. Money Capital phản ánh năng lực tài chính dự kiến dành cho hoạt động cá nhân trong phạm vi LifeBalance.

Money Capital không phải là sổ kế toán và không thay thế hệ thống quản lý tài chính chuyên nghiệp. Trong LifeBalance, Money Capital phục vụ mục đích lập kế hoạch nguồn lực, phân bổ ngân sách cá nhân cho mục đích sử dụng và so sánh với mức tiêu thụ thực tế ở các giai đoạn sau.

### 3.5 Capital Cycle

Capital Cycle là khoảng thời gian mà nguồn vốn được xác định, phân bổ, theo dõi và đóng lại. LifeBalance quản lý các loại chu kỳ:

- Daily: chu kỳ theo ngày.
- Weekly: chu kỳ theo tuần.
- Monthly: chu kỳ theo tháng.

Mỗi Capital Cycle có trạng thái nghiệp vụ. Các trạng thái cụ thể cần được xác nhận trong chính sách sản phẩm, nhưng ở mức SRS, tài liệu này giả định các trạng thái nghiệp vụ tối thiểu gồm Draft, Active, Closed và Reopened nếu reopen được phê duyệt.

### 3.6 Planned Capital

Planned Capital là lượng nguồn vốn được xác định ban đầu hoặc được người dùng dự kiến sử dụng trong một chu kỳ. Planned Capital đóng vai trò là đường cơ sở để người dùng so sánh với allocated capital, remaining capital và actual resource consumption sau này.

Planned Capital có thể thay đổi thông qua điều chỉnh được ghi nhận rõ ràng. Việc thay đổi Planned Capital không được làm mất lịch sử đã có.

### 3.7 Available Capital

Available Capital là lượng nguồn vốn có thể dùng để phân bổ trong một chu kỳ tại thời điểm xem xét. Available Capital được xác định dựa trên tổng vốn, các điều chỉnh đã có hiệu lực, phần đã phân bổ và các quy tắc nghiệp vụ liên quan.

Available Capital giúp người dùng biết liệu họ còn đủ nguồn lực để cấp cho một mục đích sử dụng mới hay không.

### 3.8 Allocated Capital

Allocated Capital là lượng nguồn vốn đã được người dùng gán cho một mục đích sử dụng cụ thể. Allocated Capital thể hiện cam kết kế hoạch. Khi nguồn vốn đã được phân bổ, nó làm giảm nguồn lực còn lại trong chu kỳ, trừ khi phần phân bổ được giải phóng hoặc điều chỉnh lại.

Allocated Capital có thể bao gồm allocated time và allocated money.

### 3.9 Remaining Capital

Remaining Capital là phần nguồn vốn còn lại sau khi xét tổng capital, điều chỉnh và allocated capital. Remaining Capital cho biết người dùng còn bao nhiêu nguồn lực chưa bị ràng buộc bởi phân bổ hiện tại.

Remaining Capital có thể âm nếu người dùng chủ động cho phép over allocation theo chính sách được phê duyệt. Trường hợp remaining capital âm cần được thể hiện như một tình trạng vượt mức, không được hiểu là nguồn lực thực sự tăng lên.

### 3.10 Actual Resource Consumption

Actual Resource Consumption là lượng nguồn lực thực tế đã được sử dụng. Trong phạm vi Resource Capital Management, khái niệm này được định nghĩa để phục vụ so sánh planned và actual ở mức nguồn vốn, nhưng module này không mô tả chi tiết quy trình ghi nhận kết quả thực hiện của các module khác.

Actual Resource Consumption có ý nghĩa khi so sánh với allocated capital hoặc planned capital. Nếu actual usage lớn hơn allocated capital, người dùng có thể nhận diện sai lệch và điều chỉnh kế hoạch tương lai.

### 3.11 Resource Efficiency

Resource Efficiency là mức độ sử dụng nguồn lực hợp lý so với kế hoạch và kết quả kỳ vọng. Trong phạm vi module này, Resource Efficiency được hiểu là khái niệm nền tảng, không phải phân tích báo cáo nâng cao.

Resource Efficiency có thể được suy luận từ các yếu tố như:

- Planned Capital so với Actual Resource Consumption.
- Allocated Capital so với Actual Resource Consumption.
- Remaining Capital cuối chu kỳ.
- Mức độ over allocation.
- Tần suất điều chỉnh nguồn vốn.

## 4. Actors

### 4.1 User

| Attribute | Description |
|---|---|
| Responsibilities | Tạo và quản lý capital cycle của chính mình; thiết lập Time Capital và Money Capital; điều chỉnh nguồn vốn; phân bổ, phân bổ lại và giải phóng nguồn vốn; xem số dư và lịch sử nguồn vốn của chính mình. |
| Permissions | Create Capital Cycle; Update Own Capital Cycle; Activate Own Capital Cycle; Close Own Capital Cycle; Reopen Own Capital Cycle nếu chính sách cho phép; Set Own Time Capital; Set Own Money Capital; Adjust Own Capital; Allocate Own Capital; Reallocate Own Capital; Release Own Allocation; View Own Capital Balance; View Own Capital History. |
| Limitations | User chỉ được quản lý nguồn vốn của chính mình. User không được xem hoặc thay đổi nguồn vốn của người dùng khác. User không được thay đổi chính sách chu kỳ, chính sách vượt mức hoặc quyền truy cập của actor khác. |

User là actor chính của Resource Capital Management. Toàn bộ giá trị nghiệp vụ của module tập trung vào việc giúp User hiểu và kiểm soát vốn nguồn lực cá nhân. Quyền của User cần được giới hạn theo ownership để bảo vệ dữ liệu riêng tư.

### 4.2 Staff

| Attribute | Description |
|---|---|
| Responsibilities | Hỗ trợ người dùng về mặt vận hành nếu được cấp quyền; hướng dẫn người dùng hiểu trạng thái nguồn vốn hoặc lịch sử trong phạm vi hỗ trợ được phê duyệt. |
| Permissions | View limited support-related capital information nếu được chính sách cho phép; không mặc định có quyền chỉnh sửa nguồn vốn cá nhân của User. |
| Limitations | Staff không được tự ý tạo, điều chỉnh, phân bổ, giải phóng, đóng hoặc mở lại capital cycle của User. Staff không được thay đổi số dư nguồn vốn của User nếu không có chính sách nghiệp vụ rõ ràng. |

Staff trong module này có vai trò hạn chế. Theo nguyên tắc bảo vệ dữ liệu cá nhân và quyền sở hữu của User, Staff không nên can thiệp vào nguồn vốn cá nhân của User. Nếu hệ thống cần hỗ trợ Staff xem thông tin, phạm vi xem phải được xác định rõ trong chính sách quyền truy cập.

### 4.3 Admin

| Attribute | Description |
|---|---|
| Responsibilities | Quản trị chính sách hoặc cấu hình nghiệp vụ liên quan đến Resource Capital Management nếu được phê duyệt; bảo đảm module vận hành theo quy tắc đã định; xem thông tin ở phạm vi quản trị nếu chính sách cho phép. |
| Permissions | Manage capital-related policy settings nếu được phê duyệt; view operational status nếu cần; không mặc định chỉnh sửa kế hoạch nguồn vốn cá nhân của User. |
| Limitations | Admin chỉ quản trị hệ thống ở mức chính sách và vận hành. Admin không thay đổi kế hoạch nguồn vốn cá nhân của User trừ khi trường hợp ngoại lệ được định nghĩa rõ, có lý do, có audit và có quyền hợp lệ. |

Admin không phải là chủ sở hữu nguồn vốn cá nhân của User. Trong module này, quyền Admin cần được hiểu là quyền quản trị chính sách và kiểm soát vận hành, không phải quyền can thiệp tùy ý vào dữ liệu cá nhân.

## 5. Functional Requirements

### 5.1 Functional Requirement List

| Requirement ID | Requirement Name | Description | Primary Actor |
|---|---|---|---|
| RCM-FR-001 | Create Capital Cycle | Hệ thống phải cho phép User tạo Capital Cycle cho daily, weekly hoặc monthly theo chính sách được phê duyệt. | User |
| RCM-FR-002 | Validate Capital Cycle Type | Hệ thống phải kiểm tra loại chu kỳ được chọn là daily, weekly hoặc monthly, hoặc loại khác nếu sau này được phê duyệt. | System |
| RCM-FR-003 | Validate Capital Cycle Period | Hệ thống phải kiểm tra kỳ thời gian của chu kỳ không vi phạm quy tắc trùng lặp hoặc chồng lấn theo chính sách. | System |
| RCM-FR-004 | Update Capital Cycle | Hệ thống phải cho phép User cập nhật thông tin chu kỳ của chính mình khi chu kỳ còn ở trạng thái cho phép chỉnh sửa. | User |
| RCM-FR-005 | Activate Capital Cycle | Hệ thống phải cho phép User kích hoạt một Capital Cycle hợp lệ để sử dụng cho phân bổ nguồn vốn. | User |
| RCM-FR-006 | Close Capital Cycle | Hệ thống phải cho phép User đóng Capital Cycle theo điều kiện nghiệp vụ được phê duyệt. | User |
| RCM-FR-007 | Reopen Capital Cycle | Hệ thống phải cho phép User mở lại Capital Cycle đã đóng nếu chính sách cho phép và điều kiện được đáp ứng. | User |
| RCM-FR-008 | Search Capital Cycle | Hệ thống phải cho phép User tìm kiếm Capital Cycle của chính mình theo loại chu kỳ, kỳ thời gian, trạng thái hoặc tiêu chí được phê duyệt. | User |
| RCM-FR-009 | View Capital Cycle Detail | Hệ thống phải cho phép User xem chi tiết Capital Cycle của chính mình. | User |
| RCM-FR-010 | Set Time Capital | Hệ thống phải cho phép User thiết lập Time Capital cho Capital Cycle của chính mình. | User |
| RCM-FR-011 | Set Money Capital | Hệ thống phải cho phép User thiết lập Money Capital cho Capital Cycle của chính mình. | User |
| RCM-FR-012 | Validate Time Capital | Hệ thống phải kiểm tra Time Capital đáp ứng quy tắc giá trị hợp lệ. | System |
| RCM-FR-013 | Validate Money Capital | Hệ thống phải kiểm tra Money Capital đáp ứng quy tắc giá trị hợp lệ. | System |
| RCM-FR-014 | Adjust Time Capital | Hệ thống phải cho phép User điều chỉnh tăng hoặc giảm Time Capital của chu kỳ theo chính sách. | User |
| RCM-FR-015 | Adjust Money Capital | Hệ thống phải cho phép User điều chỉnh tăng hoặc giảm Money Capital của chu kỳ theo chính sách. | User |
| RCM-FR-016 | Capture Adjustment Reason | Hệ thống phải hỗ trợ ghi nhận lý do điều chỉnh nguồn vốn nếu chính sách yêu cầu. | User |
| RCM-FR-017 | Validate Capital Adjustment | Hệ thống phải kiểm tra việc điều chỉnh không vi phạm giới hạn nghiệp vụ, bao gồm không làm nguồn vốn không hợp lệ. | System |
| RCM-FR-018 | Allocate Time Capital | Hệ thống phải cho phép User phân bổ một phần Time Capital khả dụng cho mục đích sử dụng đã xác định. | User |
| RCM-FR-019 | Allocate Money Capital | Hệ thống phải cho phép User phân bổ một phần Money Capital khả dụng cho mục đích sử dụng đã xác định. | User |
| RCM-FR-020 | Validate Allocation Target | Hệ thống phải kiểm tra mục tiêu phân bổ là hợp lệ ở mức nghiệp vụ trước khi ghi nhận phân bổ. | System |
| RCM-FR-021 | Validate Available Capital Before Allocation | Hệ thống phải kiểm tra nguồn vốn khả dụng trước khi cho phép phân bổ. | System |
| RCM-FR-022 | Allow Over Allocation | Hệ thống phải cho phép User chủ động cho phép phân bổ vượt nguồn lực khả dụng nếu chính sách over allocation cho phép. | User |
| RCM-FR-023 | Block Unauthorized Over Allocation | Hệ thống phải từ chối phân bổ vượt nguồn lực khả dụng nếu User chưa cho phép vượt mức hoặc chính sách không cho phép. | System |
| RCM-FR-024 | Reallocate Capital | Hệ thống phải cho phép User điều chỉnh lại phần nguồn vốn đã phân bổ khi điều kiện thay đổi. | User |
| RCM-FR-025 | Validate Reallocation | Hệ thống phải kiểm tra reallocation không vi phạm quy tắc nguồn vốn, ownership và trạng thái chu kỳ. | System |
| RCM-FR-026 | Release Allocated Capital | Hệ thống phải cho phép User giải phóng phần nguồn vốn đã phân bổ nhưng không còn cần giữ lại. | User |
| RCM-FR-027 | Validate Release Amount | Hệ thống phải kiểm tra lượng nguồn vốn được giải phóng không vượt quá lượng đã phân bổ còn hiệu lực. | System |
| RCM-FR-028 | Transfer Remaining Capital | Hệ thống phải hỗ trợ chuyển remaining capital sang chu kỳ khác nếu chính sách cho phép. | User |
| RCM-FR-029 | Validate Transfer Remaining Capital | Hệ thống phải kiểm tra điều kiện chuyển số dư trước khi thực hiện. | System |
| RCM-FR-030 | View Available Capital | Hệ thống phải cho phép User xem Available Capital của chính mình theo loại nguồn lực và chu kỳ. | User |
| RCM-FR-031 | View Allocated Capital | Hệ thống phải cho phép User xem Allocated Capital của chính mình theo loại nguồn lực và chu kỳ. | User |
| RCM-FR-032 | View Remaining Capital | Hệ thống phải cho phép User xem Remaining Capital của chính mình theo loại nguồn lực và chu kỳ. | User |
| RCM-FR-033 | View Capital Summary | Hệ thống phải cho phép User xem tóm tắt nguồn vốn của chu kỳ gồm planned, allocated, remaining và trạng thái chu kỳ. | User |
| RCM-FR-034 | View Allocation History | Hệ thống phải cho phép User xem lịch sử phân bổ, phân bổ lại và giải phóng nguồn vốn của chính mình. | User |
| RCM-FR-035 | View Adjustment History | Hệ thống phải cho phép User xem lịch sử điều chỉnh Time Capital và Money Capital của chính mình. | User |
| RCM-FR-036 | Filter Capital History | Hệ thống phải cho phép User lọc lịch sử nguồn vốn theo loại nguồn lực, loại hành động, chu kỳ, khoảng thời gian hoặc trạng thái nếu được phê duyệt. | User |
| RCM-FR-037 | Record Capital Change History | Hệ thống phải ghi nhận lịch sử cho các thay đổi quan trọng liên quan đến nguồn vốn. | System |
| RCM-FR-038 | Record Allocation History | Hệ thống phải ghi nhận lịch sử cho allocate, reallocate và release capital. | System |
| RCM-FR-039 | Record Cycle Status History | Hệ thống phải ghi nhận lịch sử thay đổi trạng thái của Capital Cycle. | System |
| RCM-FR-040 | Ownership Validation | Hệ thống phải kiểm tra User chỉ xem hoặc quản lý nguồn vốn của chính mình. | System |
| RCM-FR-041 | Staff Access Control | Hệ thống phải ngăn Staff thay đổi nguồn vốn cá nhân của User nếu không có chính sách cho phép rõ ràng. | Staff |
| RCM-FR-042 | Admin Access Control | Hệ thống phải ngăn Admin thay đổi nguồn vốn cá nhân của User trừ trường hợp được định nghĩa rõ và có quyền hợp lệ. | Admin |
| RCM-FR-043 | Capital Status Validation | Hệ thống phải kiểm tra trạng thái chu kỳ trước khi cho phép set, adjust, allocate, reallocate, release, transfer hoặc close. | System |
| RCM-FR-044 | Capital Balance Calculation | Hệ thống phải xác định balance theo quy tắc nghiệp vụ được phê duyệt cho từng loại nguồn lực. | System |
| RCM-FR-045 | Negative Remaining Capital Indicator | Hệ thống phải thể hiện tình trạng remaining capital âm như over allocation nếu chính sách cho phép. | System |
| RCM-FR-046 | Prevent History Loss | Hệ thống không được làm mất lịch sử thay đổi khi nguồn vốn được điều chỉnh hoặc chu kỳ được cập nhật. | System |
| RCM-FR-047 | Capital Cycle State Awareness | Hệ thống phải phân biệt rõ chu kỳ draft, active, closed và reopened nếu các trạng thái này được phê duyệt. | System |
| RCM-FR-048 | Active Cycle Rule Enforcement | Hệ thống phải bảo đảm quy tắc về chu kỳ hoạt động được áp dụng nhất quán theo loại chu kỳ và người dùng. | System |
| RCM-FR-049 | Capital Unit Consistency | Hệ thống phải bảo đảm đơn vị đo Time Capital và Money Capital được sử dụng nhất quán trong một chu kỳ. | System |
| RCM-FR-050 | Capital Summary for Authorized Support | Hệ thống chỉ cho phép Staff hoặc Admin xem thông tin tóm tắt nguồn vốn của User nếu chính sách quyền truy cập đã phê duyệt. | Staff, Admin |

### 5.2 Requirement Notes

Các yêu cầu về allocation trong tài liệu này chỉ mô tả việc phân bổ nguồn vốn. Tài liệu không mô tả đối tượng nghiệp vụ nhận phân bổ ở cấp chi tiết. Mọi tham chiếu đến mục đích sử dụng chỉ nhằm xác định rằng nguồn vốn được gán cho một nhu cầu hợp lệ, không phải mô tả chức năng quản lý công việc.

Các yêu cầu về history chỉ xác định nhu cầu truy vết nghiệp vụ. Tài liệu không xác định cách lưu trữ, cấu trúc kỹ thuật hoặc cơ chế thực thi.

Các yêu cầu về Admin và Staff chỉ liên quan đến quyền xem hoặc can thiệp trong phạm vi Resource Capital Management. Quy trình quản trị tổng thể không thuộc phạm vi tài liệu này.

## 6. Non-functional Requirements

### 6.1 Security

| NFR ID | Requirement | Description |
|---|---|---|
| RCM-NFR-SEC-001 | Ownership Protection | Dữ liệu nguồn vốn cá nhân phải được bảo vệ theo quyền sở hữu của User. |
| RCM-NFR-SEC-002 | Authorized Access | Chỉ actor đã xác thực và được phân quyền mới được xem hoặc thao tác với nguồn vốn. |
| RCM-NFR-SEC-003 | Sensitive Change Control | Các thay đổi quan trọng như adjust, over allocation, close, reopen và transfer phải được kiểm soát và ghi nhận. |
| RCM-NFR-SEC-004 | Staff Limitation | Staff không được chỉnh sửa nguồn vốn cá nhân nếu không có quyền được phê duyệt rõ ràng. |
| RCM-NFR-SEC-005 | Admin Limitation | Admin không được can thiệp tùy ý vào kế hoạch nguồn vốn cá nhân của User nếu chưa có căn cứ nghiệp vụ. |

### 6.2 Performance

| NFR ID | Requirement | Description |
|---|---|---|
| RCM-NFR-PER-001 | Balance View Response | Việc xem available, allocated và remaining capital phải phản hồi trong thời gian phù hợp với nhu cầu lập kế hoạch. |
| RCM-NFR-PER-002 | Allocation Validation Response | Kiểm tra phân bổ nguồn vốn phải đủ nhanh để không làm gián đoạn quá trình lập kế hoạch. |
| RCM-NFR-PER-003 | History Filtering Response | Lọc lịch sử nguồn vốn phải đáp ứng thời gian phản hồi phù hợp với khối lượng dữ liệu cá nhân thông thường. |
| RCM-NFR-PER-004 | Cycle Search Response | Tìm kiếm chu kỳ phải phản hồi phù hợp với nhu cầu tra cứu của User. |

### 6.3 Availability

| NFR ID | Requirement | Description |
|---|---|---|
| RCM-NFR-AVL-001 | Capital Access Availability | User cần có khả năng truy cập thông tin nguồn vốn khi lập kế hoạch. |
| RCM-NFR-AVL-002 | Allocation Availability | Chức năng phân bổ và xem số dư nguồn vốn cần khả dụng trong thời điểm User chuẩn bị sử dụng nguồn lực. |
| RCM-NFR-AVL-003 | History Availability | Lịch sử thay đổi cần khả dụng để User kiểm tra khi có sai lệch hoặc thắc mắc. |

### 6.4 Reliability

| NFR ID | Requirement | Description |
|---|---|---|
| RCM-NFR-REL-001 | Balance Reliability | Số dư nguồn lực phải phản ánh nhất quán theo các quy tắc nghiệp vụ đã được phê duyệt. |
| RCM-NFR-REL-002 | Adjustment Reliability | Điều chỉnh nguồn vốn phải tạo ra kết quả dự đoán được và không làm mất lịch sử. |
| RCM-NFR-REL-003 | Allocation Reliability | Khi phân bổ, phân bổ lại hoặc giải phóng nguồn vốn, trạng thái nguồn vốn phải nhất quán ở góc độ nghiệp vụ. |
| RCM-NFR-REL-004 | Cycle State Reliability | Trạng thái chu kỳ phải được diễn giải nhất quán khi quyết định cho phép hay từ chối hành động. |

### 6.5 Auditability

| NFR ID | Requirement | Description |
|---|---|---|
| RCM-NFR-AUD-001 | Capital Change Traceability | Các thay đổi quan trọng về nguồn vốn phải có khả năng truy vết. |
| RCM-NFR-AUD-002 | Allocation Traceability | Allocate, reallocate và release phải có khả năng được xem lại trong lịch sử. |
| RCM-NFR-AUD-003 | Adjustment Reason Traceability | Nếu chính sách yêu cầu lý do điều chỉnh, lý do đó phải có thể xem lại bởi actor được phép. |
| RCM-NFR-AUD-004 | Cycle Status Traceability | Thay đổi trạng thái chu kỳ như activate, close và reopen phải có khả năng truy vết. |
| RCM-NFR-AUD-005 | Over Allocation Traceability | Trường hợp User cho phép over allocation phải được ghi nhận như một quyết định nghiệp vụ quan trọng. |

### 6.6 Scalability

| NFR ID | Requirement | Description |
|---|---|---|
| RCM-NFR-SCL-001 | Multi-cycle Growth | Module phải hỗ trợ số lượng chu kỳ tăng dần theo thời gian sử dụng của User. |
| RCM-NFR-SCL-002 | History Growth | Module phải hỗ trợ lịch sử thay đổi nguồn vốn tăng theo thời gian mà vẫn phục vụ tra cứu nghiệp vụ. |
| RCM-NFR-SCL-003 | Resource Type Extensibility | Mô hình nghiệp vụ nên có khả năng mở rộng thêm loại nguồn lực trong tương lai nếu được phê duyệt, dù hiện tại chỉ có Time và Money. |
| RCM-NFR-SCL-004 | User Growth | Module phải phù hợp khi số lượng User sử dụng quản lý nguồn vốn tăng lên. |

### 6.7 Maintainability

| NFR ID | Requirement | Description |
|---|---|---|
| RCM-NFR-MNT-001 | Policy Maintainability | Các chính sách như allocation, adjustment, over allocation và history cần có khả năng được cập nhật theo quyết định nghiệp vụ. |
| RCM-NFR-MNT-002 | Rule Consistency | Business rule phải được quản lý nhất quán để tránh hành vi khác nhau giữa các hành động nguồn vốn. |
| RCM-NFR-MNT-003 | Terminology Consistency | Thuật ngữ capital, available, allocated, remaining, planned và actual phải được sử dụng nhất quán. |
| RCM-NFR-MNT-004 | Change Impact Clarity | Khi thay đổi rule về nguồn vốn, tác động đến chu kỳ, allocation và history phải được đánh giá. |

### 6.8 Usability

| NFR ID | Requirement | Description |
|---|---|---|
| RCM-NFR-USA-001 | Clear Capital Meaning | User phải hiểu rõ ý nghĩa của Time Capital, Money Capital, Available Capital, Allocated Capital và Remaining Capital. |
| RCM-NFR-USA-002 | Clear Over Allocation Warning | Khi User phân bổ vượt khả dụng, hệ thống phải truyền đạt rõ đây là tình trạng vượt mức. |
| RCM-NFR-USA-003 | Adjustment Clarity | Khi điều chỉnh nguồn vốn, User phải hiểu tác động của thay đổi đến số dư và phân bổ hiện tại. |
| RCM-NFR-USA-004 | History Understandability | Lịch sử nguồn vốn phải đủ rõ để User hiểu thay đổi nào đã xảy ra và vì sao nếu lý do được ghi nhận. |
| RCM-NFR-USA-005 | Cycle Clarity | User phải phân biệt được chu kỳ daily, weekly và monthly, cũng như trạng thái của từng chu kỳ. |

## 7. Business Rules

| Business Rule ID | Business Rule |
|---|---|
| RCM-BR-001 | Mỗi User chỉ được quản lý nguồn vốn thuộc quyền sở hữu của chính mình. |
| RCM-BR-002 | Staff không được thay đổi nguồn vốn cá nhân của User nếu không có chính sách cho phép rõ ràng. |
| RCM-BR-003 | Admin không được thay đổi kế hoạch nguồn vốn cá nhân của User trừ trường hợp ngoại lệ được định nghĩa rõ và có truy vết. |
| RCM-BR-004 | Capital Cycle phải thuộc loại daily, weekly hoặc monthly trong phạm vi hiện tại. |
| RCM-BR-005 | Một Capital Cycle phải có kỳ thời gian xác định. |
| RCM-BR-006 | Quy tắc trùng hoặc chồng lấn chu kỳ phải được áp dụng theo loại chu kỳ và người sở hữu. |
| RCM-BR-007 | Một chu kỳ chỉ có một trạng thái hoạt động tại một thời điểm theo chính sách được phê duyệt. |
| RCM-BR-008 | Nếu chính sách không cho phép nhiều chu kỳ active cùng loại, User chỉ được có một active cycle cho mỗi loại chu kỳ trong cùng kỳ thời gian. |
| RCM-BR-009 | Chu kỳ ở trạng thái closed không được chỉnh sửa, phân bổ, điều chỉnh hoặc giải phóng nguồn vốn nếu chưa được reopen theo chính sách. |
| RCM-BR-010 | Chu kỳ ở trạng thái draft chưa được dùng cho phân bổ nếu chính sách yêu cầu active cycle. |
| RCM-BR-011 | Time Capital phải lớn hơn hoặc bằng 0. |
| RCM-BR-012 | Money Capital phải lớn hơn hoặc bằng 0. |
| RCM-BR-013 | Allocated Time Capital phải lớn hơn 0 khi tạo phân bổ thời gian. |
| RCM-BR-014 | Allocated Money Capital phải lớn hơn 0 khi tạo phân bổ tiền bạc. |
| RCM-BR-015 | Tổng nguồn lực phân bổ không được vượt nguồn lực khả dụng nếu User chưa chủ động cho phép over allocation. |
| RCM-BR-016 | Over allocation chỉ được chấp nhận khi chính sách cho phép và User xác nhận rõ ràng. |
| RCM-BR-017 | Remaining Capital âm phải được hiểu là tình trạng vượt mức, không phải nguồn vốn bổ sung. |
| RCM-BR-018 | Điều chỉnh nguồn vốn không được làm mất lịch sử trước đó. |
| RCM-BR-019 | Điều chỉnh giảm nguồn vốn không được làm cho trạng thái nguồn vốn trở nên không hợp lệ nếu chính sách không cho phép. |
| RCM-BR-020 | Nếu điều chỉnh làm allocated capital vượt available capital, hệ thống phải xử lý theo over allocation policy. |
| RCM-BR-021 | Mọi điều chỉnh nguồn vốn quan trọng phải được ghi nhận trong lịch sử. |
| RCM-BR-022 | Mọi phân bổ nguồn vốn phải được ghi nhận trong lịch sử. |
| RCM-BR-023 | Mọi phân bổ lại nguồn vốn phải được ghi nhận trong lịch sử. |
| RCM-BR-024 | Mọi giải phóng nguồn vốn đã phân bổ phải được ghi nhận trong lịch sử. |
| RCM-BR-025 | Mọi thay đổi trạng thái chu kỳ phải được ghi nhận trong lịch sử. |
| RCM-BR-026 | Lý do điều chỉnh phải được ghi nhận nếu chính sách yêu cầu. |
| RCM-BR-027 | Lý do over allocation phải được ghi nhận nếu chính sách yêu cầu. |
| RCM-BR-028 | Không được giải phóng nhiều hơn lượng nguồn vốn đã phân bổ còn hiệu lực. |
| RCM-BR-029 | Không được reallocate nguồn vốn từ một phân bổ không tồn tại hoặc không còn hiệu lực. |
| RCM-BR-030 | Reallocation phải tuân thủ available capital và over allocation policy. |
| RCM-BR-031 | Transfer remaining capital chỉ được thực hiện nếu chính sách cho phép. |
| RCM-BR-032 | Remaining capital chỉ được chuyển sang chu kỳ mục tiêu hợp lệ. |
| RCM-BR-033 | Transfer remaining capital phải được ghi nhận trong lịch sử nếu được thực hiện. |
| RCM-BR-034 | Khi close cycle, hệ thống phải kiểm tra trạng thái chu kỳ và các điều kiện đóng chu kỳ. |
| RCM-BR-035 | Close cycle không được làm mất thông tin planned, allocated, remaining và history. |
| RCM-BR-036 | Reopen cycle chỉ được thực hiện nếu chính sách cho phép. |
| RCM-BR-037 | Reopened cycle phải thể hiện rõ trạng thái hoặc dấu vết đã từng được đóng. |
| RCM-BR-038 | Capital summary phải phản ánh nhất quán planned, allocated, remaining và trạng thái chu kỳ. |
| RCM-BR-039 | Available capital phải được tính theo quy tắc nghiệp vụ đã phê duyệt. |
| RCM-BR-040 | Remaining capital phải phản ánh nguồn vốn sau khi xét phân bổ và điều chỉnh. |
| RCM-BR-041 | Time Capital và Money Capital không được cộng gộp thành một nguồn lực duy nhất khi tính balance. |
| RCM-BR-042 | Đơn vị đo của Time Capital phải nhất quán trong cùng một chu kỳ. |
| RCM-BR-043 | Đơn vị tiền tệ của Money Capital phải nhất quán trong cùng một chu kỳ trừ khi chính sách đa tiền tệ được phê duyệt. |
| RCM-BR-044 | User phải được cảnh báo khi hành động điều chỉnh hoặc phân bổ có thể dẫn đến over allocation. |
| RCM-BR-045 | User phải được cảnh báo khi close cycle còn remaining capital chưa xử lý nếu chính sách yêu cầu. |
| RCM-BR-046 | User phải được cảnh báo khi close cycle còn allocation chưa được giải phóng hoặc chưa có trạng thái hoàn tất nếu chính sách yêu cầu. |
| RCM-BR-047 | Staff chỉ được xem thông tin nguồn vốn của User nếu có quyền hỗ trợ được phê duyệt. |
| RCM-BR-048 | Admin chỉ được xem thông tin nguồn vốn cá nhân nếu chính sách quyền truy cập cho phép và có mục đích hợp lệ. |
| RCM-BR-049 | Lịch sử nguồn vốn phải phân biệt loại hành động: create, update, activate, adjust, allocate, reallocate, release, transfer, close và reopen nếu áp dụng. |
| RCM-BR-050 | Mọi hành động bị từ chối do vi phạm rule phải không làm thay đổi nguồn vốn hiện tại. |
| RCM-BR-051 | Nếu có nhiều thao tác phân bổ đồng thời, kết quả cuối cùng phải tuân thủ available capital và over allocation policy. |
| RCM-BR-052 | Capital Cycle không hợp lệ không được dùng để phân bổ nguồn vốn. |
| RCM-BR-053 | Không được chuyển remaining capital từ chu kỳ chưa đóng nếu chính sách chỉ cho phép chuyển sau khi close. |
| RCM-BR-054 | Không được chuyển remaining capital âm như một số dư dương. |
| RCM-BR-055 | Actual Resource Consumption không được làm thay đổi Planned Capital nếu không có hành động điều chỉnh được ghi nhận. |

## 8. Workflows

### 8.1 Create Capital Cycle

#### Main Flow

1. User yêu cầu tạo Capital Cycle mới.
2. User chọn loại chu kỳ: daily, weekly hoặc monthly.
3. User xác định kỳ thời gian của chu kỳ.
4. Hệ thống kiểm tra loại chu kỳ hợp lệ.
5. Hệ thống kiểm tra kỳ thời gian không vi phạm quy tắc trùng hoặc chồng lấn.
6. User thiết lập Time Capital và Money Capital ban đầu nếu muốn.
7. Hệ thống kiểm tra giá trị nguồn vốn hợp lệ.
8. Hệ thống tạo Capital Cycle ở trạng thái phù hợp theo chính sách.
9. Hệ thống ghi nhận lịch sử tạo chu kỳ.

#### Alternative Flow

- User tạo chu kỳ ở trạng thái draft trước, sau đó kích hoạt khi sẵn sàng.
- User chỉ thiết lập một loại nguồn vốn ban đầu và bổ sung loại còn lại sau nếu chính sách cho phép.
- Hệ thống đề xuất kỳ thời gian dựa trên loại chu kỳ, nhưng User vẫn cần xác nhận.

#### Exception Flow

- Loại chu kỳ không hợp lệ: hệ thống từ chối tạo.
- Kỳ thời gian trùng hoặc chồng lấn trái chính sách: hệ thống từ chối tạo.
- Time Capital hoặc Money Capital không hợp lệ: hệ thống yêu cầu sửa.
- User không có quyền quản lý nguồn vốn: hệ thống từ chối.

### 8.2 Update Capital

#### Main Flow

1. User chọn Capital Cycle của chính mình.
2. User yêu cầu cập nhật Time Capital hoặc Money Capital.
3. Hệ thống kiểm tra trạng thái chu kỳ cho phép cập nhật.
4. User nhập giá trị mới hoặc thay đổi được phép.
5. Hệ thống kiểm tra giá trị hợp lệ.
6. Hệ thống xác định tác động đến available, allocated và remaining capital.
7. User xác nhận thay đổi nếu có tác động quan trọng.
8. Hệ thống cập nhật capital theo chính sách.
9. Hệ thống ghi nhận lịch sử thay đổi.

#### Alternative Flow

- Nếu thay đổi là điều chỉnh tăng hoặc giảm thay vì cập nhật trực tiếp, workflow Adjust Capital được áp dụng.
- Nếu chu kỳ đang active, hệ thống có thể yêu cầu xác nhận bổ sung.

#### Exception Flow

- Chu kỳ đã closed: hệ thống từ chối nếu chưa reopen.
- Giá trị mới nhỏ hơn mức được phép: hệ thống từ chối.
- Thay đổi làm phát sinh over allocation nhưng User chưa cho phép: hệ thống từ chối hoặc yêu cầu xác nhận theo policy.

### 8.3 Allocate Capital

#### Main Flow

1. User chọn Capital Cycle active.
2. User chọn loại nguồn lực cần phân bổ: Time Capital, Money Capital hoặc cả hai nếu chính sách cho phép.
3. User nhập lượng nguồn vốn cần phân bổ.
4. User xác định mục tiêu phân bổ ở mức nghiệp vụ.
5. Hệ thống kiểm tra ownership và trạng thái chu kỳ.
6. Hệ thống kiểm tra nguồn vốn khả dụng.
7. Nếu phân bổ nằm trong khả dụng, hệ thống ghi nhận allocation.
8. Hệ thống cập nhật balance theo quy tắc nghiệp vụ.
9. Hệ thống ghi nhận allocation history.

#### Alternative Flow

- Nếu phân bổ vượt khả dụng, hệ thống yêu cầu User xác nhận over allocation nếu chính sách cho phép.
- User có thể phân bổ chỉ Time Capital hoặc chỉ Money Capital.
- User có thể lưu phân bổ nháp nếu chính sách hỗ trợ trạng thái nháp.

#### Exception Flow

- Chu kỳ không active: hệ thống từ chối.
- Lượng phân bổ không hợp lệ: hệ thống từ chối.
- Mục tiêu phân bổ không hợp lệ: hệ thống từ chối.
- Vượt khả dụng nhưng không được phép over allocation: hệ thống từ chối.

### 8.4 Adjust Capital

#### Main Flow

1. User chọn Capital Cycle cần điều chỉnh.
2. User chọn loại điều chỉnh: tăng hoặc giảm Time Capital, Money Capital hoặc cả hai nếu chính sách cho phép.
3. User nhập lượng điều chỉnh.
4. User cung cấp lý do điều chỉnh nếu chính sách yêu cầu.
5. Hệ thống kiểm tra trạng thái chu kỳ.
6. Hệ thống kiểm tra giới hạn điều chỉnh.
7. Hệ thống xác định tác động đến available và remaining capital.
8. User xác nhận nếu điều chỉnh có tác động quan trọng.
9. Hệ thống ghi nhận điều chỉnh.
10. Hệ thống ghi nhận adjustment history.

#### Alternative Flow

- Điều chỉnh tăng làm tăng available capital nếu không có ràng buộc khác.
- Điều chỉnh giảm có thể làm remaining capital âm nếu User cho phép over allocation và chính sách cho phép.

#### Exception Flow

- Điều chỉnh làm nguồn vốn không hợp lệ: hệ thống từ chối.
- Thiếu lý do điều chỉnh khi policy yêu cầu: hệ thống từ chối.
- Chu kỳ không cho phép điều chỉnh: hệ thống từ chối.

### 8.5 Release Capital

#### Main Flow

1. User chọn allocation còn hiệu lực.
2. User yêu cầu giải phóng một phần hoặc toàn bộ nguồn vốn đã phân bổ.
3. Hệ thống kiểm tra allocation thuộc quyền sở hữu của User.
4. Hệ thống kiểm tra lượng release không vượt lượng còn hiệu lực.
5. User xác nhận hành động.
6. Hệ thống giải phóng nguồn vốn.
7. Hệ thống cập nhật remaining capital.
8. Hệ thống ghi nhận release history.

#### Alternative Flow

- User giải phóng toàn bộ allocation.
- User giải phóng một phần allocation và phần còn lại vẫn giữ trạng thái phân bổ.

#### Exception Flow

- Allocation không tồn tại hoặc không thuộc User: hệ thống từ chối.
- Lượng release vượt lượng đã phân bổ: hệ thống từ chối.
- Chu kỳ đã closed và không cho phép release: hệ thống từ chối.

### 8.6 Close Cycle

#### Main Flow

1. User chọn Capital Cycle cần đóng.
2. Hệ thống kiểm tra chu kỳ thuộc User.
3. Hệ thống kiểm tra trạng thái chu kỳ có thể đóng.
4. Hệ thống xác định remaining capital và allocation còn hiệu lực.
5. Hệ thống hiển thị cảnh báo nghiệp vụ nếu còn nguồn lực chưa xử lý hoặc over allocation.
6. User chọn cách xử lý remaining capital nếu chính sách yêu cầu.
7. User xác nhận đóng chu kỳ.
8. Hệ thống chuyển chu kỳ sang trạng thái closed.
9. Hệ thống ghi nhận cycle status history.

#### Alternative Flow

- Nếu chính sách cho phép transfer remaining capital, User chọn chu kỳ đích hợp lệ.
- Nếu User chưa muốn đóng vì còn allocation chưa xử lý, User hủy thao tác.

#### Exception Flow

- Chu kỳ đã closed: hệ thống thông báo trạng thái hiện tại.
- Chu kỳ có điều kiện chưa thỏa mãn để đóng: hệ thống từ chối.
- Transfer remaining capital không hợp lệ: hệ thống từ chối phần chuyển số dư.

### 8.7 View History

#### Main Flow

1. User truy cập lịch sử nguồn vốn của chính mình.
2. User chọn loại lịch sử cần xem: adjustment, allocation, cycle status hoặc toàn bộ.
3. User áp dụng bộ lọc nếu cần.
4. Hệ thống kiểm tra quyền sở hữu.
5. Hệ thống hiển thị lịch sử trong phạm vi được phép.

#### Alternative Flow

- User lọc theo loại nguồn lực: Time Capital hoặc Money Capital.
- User lọc theo chu kỳ, kỳ thời gian, loại hành động hoặc trạng thái.
- Staff hoặc Admin xem thông tin lịch sử giới hạn nếu có chính sách cho phép.

#### Exception Flow

- User yêu cầu xem lịch sử của người khác: hệ thống từ chối.
- Không có dữ liệu phù hợp: hệ thống hiển thị trạng thái không có kết quả.
- Actor không có quyền: hệ thống từ chối.

## 9. Use Case List

| Use Case ID | Use Case Name | Primary Actor | Summary |
|---|---|---|---|
| RCM-UC-001 | Create Capital Cycle | User | Tạo chu kỳ nguồn vốn daily, weekly hoặc monthly. |
| RCM-UC-002 | Update Capital Cycle | User | Cập nhật thông tin chu kỳ khi được phép. |
| RCM-UC-003 | Activate Capital Cycle | User | Kích hoạt chu kỳ để sử dụng cho quản lý nguồn vốn. |
| RCM-UC-004 | Close Capital Cycle | User | Đóng chu kỳ nguồn vốn khi đáp ứng điều kiện. |
| RCM-UC-005 | Reopen Capital Cycle | User | Mở lại chu kỳ đã đóng nếu chính sách cho phép. |
| RCM-UC-006 | Search Capital Cycle | User | Tìm kiếm chu kỳ nguồn vốn của chính mình. |
| RCM-UC-007 | View Capital Cycle Detail | User | Xem chi tiết chu kỳ nguồn vốn. |
| RCM-UC-008 | Set Time Capital | User | Thiết lập vốn thời gian cho chu kỳ. |
| RCM-UC-009 | Set Money Capital | User | Thiết lập vốn tiền bạc cho chu kỳ. |
| RCM-UC-010 | Adjust Time Capital | User | Điều chỉnh vốn thời gian. |
| RCM-UC-011 | Adjust Money Capital | User | Điều chỉnh vốn tiền bạc. |
| RCM-UC-012 | Allocate Time Capital | User | Phân bổ vốn thời gian. |
| RCM-UC-013 | Allocate Money Capital | User | Phân bổ vốn tiền bạc. |
| RCM-UC-014 | Reallocate Capital | User | Điều chỉnh lại phân bổ nguồn vốn. |
| RCM-UC-015 | Release Allocated Capital | User | Giải phóng nguồn vốn đã phân bổ. |
| RCM-UC-016 | Allow Over Allocation | User | Xác nhận cho phép phân bổ vượt khả dụng. |
| RCM-UC-017 | Transfer Remaining Capital | User | Chuyển số dư còn lại sang chu kỳ khác nếu chính sách cho phép. |
| RCM-UC-018 | View Available Capital | User | Xem nguồn vốn khả dụng. |
| RCM-UC-019 | View Allocated Capital | User | Xem nguồn vốn đã phân bổ. |
| RCM-UC-020 | View Remaining Capital | User | Xem nguồn vốn còn lại. |
| RCM-UC-021 | View Capital Summary | User | Xem tóm tắt nguồn vốn trong chu kỳ. |
| RCM-UC-022 | View Allocation History | User | Xem lịch sử phân bổ nguồn vốn. |
| RCM-UC-023 | View Adjustment History | User | Xem lịch sử điều chỉnh nguồn vốn. |
| RCM-UC-024 | Filter Capital History | User | Lọc lịch sử nguồn vốn. |
| RCM-UC-025 | Validate Capital Ownership | System | Kiểm tra quyền sở hữu trước khi cho phép thao tác. |
| RCM-UC-026 | Validate Capital Balance | System | Kiểm tra số dư trước các hành động ảnh hưởng nguồn vốn. |
| RCM-UC-027 | View Authorized Capital Summary | Staff, Admin | Xem thông tin tóm tắt nguồn vốn nếu chính sách cho phép. |

## 10. Use Case Specification

### RCM-UC-001 - Create Capital Cycle

| Field | Description |
|---|---|
| ID | RCM-UC-001 |
| Description | User tạo một Capital Cycle mới để quản lý Time Capital và Money Capital. |
| Primary Actor | User |
| Trigger | User yêu cầu tạo chu kỳ nguồn vốn mới. |
| Preconditions | User đã xác thực; User có quyền quản lý nguồn vốn của chính mình. |
| Main Flow | 1. User chọn tạo chu kỳ. 2. User chọn loại chu kỳ. 3. User xác định kỳ thời gian. 4. Hệ thống kiểm tra hợp lệ. 5. User nhập nguồn vốn ban đầu nếu có. 6. Hệ thống tạo chu kỳ. 7. Hệ thống ghi nhận lịch sử tạo chu kỳ. |
| Alternative Flow | User tạo chu kỳ ở trạng thái draft nếu chính sách cho phép. |
| Exception Flow | Loại chu kỳ, kỳ thời gian hoặc giá trị nguồn vốn không hợp lệ dẫn đến từ chối. |
| Postconditions | Capital Cycle mới được tạo theo trạng thái được phê duyệt. |
| Business Rules | RCM-BR-001, RCM-BR-004, RCM-BR-005, RCM-BR-006, RCM-BR-011, RCM-BR-012 |

### RCM-UC-002 - Update Capital Cycle

| Field | Description |
|---|---|
| ID | RCM-UC-002 |
| Description | User cập nhật thông tin chu kỳ nguồn vốn khi trạng thái chu kỳ cho phép. |
| Primary Actor | User |
| Trigger | User chọn cập nhật chu kỳ. |
| Preconditions | Chu kỳ thuộc User và chưa bị khóa chỉnh sửa bởi trạng thái closed hoặc policy tương đương. |
| Main Flow | 1. User chọn chu kỳ. 2. User nhập thay đổi. 3. Hệ thống kiểm tra quyền sở hữu. 4. Hệ thống kiểm tra trạng thái chu kỳ. 5. Hệ thống cập nhật thay đổi hợp lệ. 6. Hệ thống ghi nhận lịch sử nếu thay đổi quan trọng. |
| Alternative Flow | User hủy thao tác trước khi xác nhận. |
| Exception Flow | Chu kỳ closed, không thuộc User hoặc thay đổi không hợp lệ dẫn đến từ chối. |
| Postconditions | Chu kỳ được cập nhật nếu hợp lệ. |
| Business Rules | RCM-BR-001, RCM-BR-009, RCM-BR-018, RCM-BR-025 |

### RCM-UC-003 - Activate Capital Cycle

| Field | Description |
|---|---|
| ID | RCM-UC-003 |
| Description | User kích hoạt một Capital Cycle hợp lệ. |
| Primary Actor | User |
| Trigger | User chọn activate cycle. |
| Preconditions | Chu kỳ thuộc User, hợp lệ và ở trạng thái cho phép kích hoạt. |
| Main Flow | 1. User chọn chu kỳ. 2. Hệ thống kiểm tra quyền sở hữu. 3. Hệ thống kiểm tra quy tắc active cycle. 4. User xác nhận. 5. Hệ thống chuyển chu kỳ sang active. 6. Hệ thống ghi nhận lịch sử trạng thái. |
| Alternative Flow | Nếu đã có active cycle cùng loại, hệ thống xử lý theo chính sách: từ chối, thay thế hoặc yêu cầu đóng chu kỳ cũ. |
| Exception Flow | Chu kỳ không hợp lệ hoặc vi phạm active cycle rule dẫn đến từ chối. |
| Postconditions | Chu kỳ được active nếu hợp lệ. |
| Business Rules | RCM-BR-007, RCM-BR-008, RCM-BR-025, RCM-BR-048 |

### RCM-UC-004 - Close Capital Cycle

| Field | Description |
|---|---|
| ID | RCM-UC-004 |
| Description | User đóng chu kỳ nguồn vốn khi kết thúc kỳ quản lý. |
| Primary Actor | User |
| Trigger | User chọn close cycle. |
| Preconditions | Chu kỳ thuộc User và có trạng thái cho phép đóng. |
| Main Flow | 1. User chọn chu kỳ. 2. Hệ thống kiểm tra điều kiện đóng. 3. Hệ thống hiển thị remaining capital và allocation còn hiệu lực nếu có. 4. User xử lý số dư nếu chính sách yêu cầu. 5. User xác nhận đóng. 6. Hệ thống chuyển chu kỳ sang closed. 7. Hệ thống ghi nhận lịch sử. |
| Alternative Flow | User chọn transfer remaining capital nếu chính sách cho phép. |
| Exception Flow | Chu kỳ không đủ điều kiện đóng hoặc transfer không hợp lệ dẫn đến từ chối. |
| Postconditions | Chu kỳ ở trạng thái closed nếu thành công. |
| Business Rules | RCM-BR-034, RCM-BR-035, RCM-BR-045, RCM-BR-046 |

### RCM-UC-005 - Reopen Capital Cycle

| Field | Description |
|---|---|
| ID | RCM-UC-005 |
| Description | User mở lại chu kỳ đã đóng nếu chính sách cho phép. |
| Primary Actor | User |
| Trigger | User chọn reopen cycle. |
| Preconditions | Chu kỳ thuộc User, đang closed và chính sách cho phép reopen. |
| Main Flow | 1. User chọn chu kỳ closed. 2. Hệ thống kiểm tra chính sách reopen. 3. User xác nhận. 4. Hệ thống chuyển chu kỳ sang reopened hoặc trạng thái tương đương. 5. Hệ thống ghi nhận lịch sử. |
| Alternative Flow | Hệ thống yêu cầu lý do reopen nếu chính sách quy định. |
| Exception Flow | Chính sách không cho phép reopen hoặc chu kỳ không đủ điều kiện dẫn đến từ chối. |
| Postconditions | Chu kỳ được mở lại theo trạng thái được phê duyệt. |
| Business Rules | RCM-BR-036, RCM-BR-037, RCM-BR-025 |

### RCM-UC-006 - Search Capital Cycle

| Field | Description |
|---|---|
| ID | RCM-UC-006 |
| Description | User tìm kiếm các chu kỳ nguồn vốn của chính mình. |
| Primary Actor | User |
| Trigger | User nhập tiêu chí tìm kiếm. |
| Preconditions | User đã xác thực và có quyền xem nguồn vốn của chính mình. |
| Main Flow | 1. User nhập tiêu chí. 2. Hệ thống kiểm tra ownership. 3. Hệ thống trả về danh sách chu kỳ phù hợp. |
| Alternative Flow | Không có kết quả phù hợp, hệ thống thông báo không có dữ liệu. |
| Exception Flow | User cố tìm chu kỳ của người khác, hệ thống từ chối. |
| Postconditions | Không thay đổi dữ liệu nguồn vốn. |
| Business Rules | RCM-BR-001 |

### RCM-UC-007 - View Capital Cycle Detail

| Field | Description |
|---|---|
| ID | RCM-UC-007 |
| Description | User xem chi tiết một Capital Cycle của chính mình. |
| Primary Actor | User |
| Trigger | User chọn một chu kỳ để xem. |
| Preconditions | Chu kỳ thuộc User. |
| Main Flow | 1. User chọn chu kỳ. 2. Hệ thống kiểm tra quyền sở hữu. 3. Hệ thống hiển thị thông tin chi tiết trong phạm vi module. |
| Alternative Flow | User chuyển sang xem summary hoặc history. |
| Exception Flow | Chu kỳ không tồn tại hoặc không thuộc User dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | RCM-BR-001, RCM-BR-038 |

### RCM-UC-008 - Set Time Capital

| Field | Description |
|---|---|
| ID | RCM-UC-008 |
| Description | User thiết lập Time Capital cho chu kỳ. |
| Primary Actor | User |
| Trigger | User nhập Time Capital. |
| Preconditions | Chu kỳ thuộc User và trạng thái cho phép thiết lập nguồn vốn. |
| Main Flow | 1. User chọn chu kỳ. 2. User nhập Time Capital. 3. Hệ thống kiểm tra giá trị. 4. Hệ thống ghi nhận Time Capital. 5. Hệ thống cập nhật balance. |
| Alternative Flow | User thiết lập Time Capital khi tạo chu kỳ. |
| Exception Flow | Giá trị âm, sai đơn vị hoặc chu kỳ không cho phép cập nhật dẫn đến từ chối. |
| Postconditions | Time Capital được thiết lập nếu hợp lệ. |
| Business Rules | RCM-BR-011, RCM-BR-042 |

### RCM-UC-009 - Set Money Capital

| Field | Description |
|---|---|
| ID | RCM-UC-009 |
| Description | User thiết lập Money Capital cho chu kỳ. |
| Primary Actor | User |
| Trigger | User nhập Money Capital. |
| Preconditions | Chu kỳ thuộc User và trạng thái cho phép thiết lập nguồn vốn. |
| Main Flow | 1. User chọn chu kỳ. 2. User nhập Money Capital. 3. Hệ thống kiểm tra giá trị. 4. Hệ thống ghi nhận Money Capital. 5. Hệ thống cập nhật balance. |
| Alternative Flow | User thiết lập Money Capital khi tạo chu kỳ. |
| Exception Flow | Giá trị âm, sai đơn vị tiền tệ hoặc chu kỳ không cho phép cập nhật dẫn đến từ chối. |
| Postconditions | Money Capital được thiết lập nếu hợp lệ. |
| Business Rules | RCM-BR-012, RCM-BR-043 |

### RCM-UC-010 - Adjust Time Capital

| Field | Description |
|---|---|
| ID | RCM-UC-010 |
| Description | User điều chỉnh tăng hoặc giảm Time Capital. |
| Primary Actor | User |
| Trigger | User chọn điều chỉnh Time Capital. |
| Preconditions | Chu kỳ thuộc User và cho phép điều chỉnh. |
| Main Flow | 1. User chọn chu kỳ. 2. User nhập lượng điều chỉnh. 3. User nhập lý do nếu cần. 4. Hệ thống kiểm tra điều chỉnh. 5. User xác nhận. 6. Hệ thống cập nhật Time Capital. 7. Hệ thống ghi nhận lịch sử. |
| Alternative Flow | Điều chỉnh tạo over allocation và User xác nhận nếu chính sách cho phép. |
| Exception Flow | Điều chỉnh làm nguồn vốn không hợp lệ hoặc thiếu lý do bắt buộc dẫn đến từ chối. |
| Postconditions | Time Capital và balance được cập nhật nếu hợp lệ. |
| Business Rules | RCM-BR-018, RCM-BR-019, RCM-BR-020, RCM-BR-021, RCM-BR-026 |

### RCM-UC-011 - Adjust Money Capital

| Field | Description |
|---|---|
| ID | RCM-UC-011 |
| Description | User điều chỉnh tăng hoặc giảm Money Capital. |
| Primary Actor | User |
| Trigger | User chọn điều chỉnh Money Capital. |
| Preconditions | Chu kỳ thuộc User và cho phép điều chỉnh. |
| Main Flow | 1. User chọn chu kỳ. 2. User nhập lượng điều chỉnh. 3. User nhập lý do nếu cần. 4. Hệ thống kiểm tra điều chỉnh. 5. User xác nhận. 6. Hệ thống cập nhật Money Capital. 7. Hệ thống ghi nhận lịch sử. |
| Alternative Flow | Điều chỉnh tạo over allocation và User xác nhận nếu chính sách cho phép. |
| Exception Flow | Điều chỉnh làm nguồn vốn không hợp lệ hoặc thiếu lý do bắt buộc dẫn đến từ chối. |
| Postconditions | Money Capital và balance được cập nhật nếu hợp lệ. |
| Business Rules | RCM-BR-018, RCM-BR-019, RCM-BR-020, RCM-BR-021, RCM-BR-026 |

### RCM-UC-012 - Allocate Time Capital

| Field | Description |
|---|---|
| ID | RCM-UC-012 |
| Description | User phân bổ một phần Time Capital cho mục đích sử dụng hợp lệ. |
| Primary Actor | User |
| Trigger | User yêu cầu phân bổ Time Capital. |
| Preconditions | Chu kỳ active; User có Time Capital khả dụng hoặc cho phép over allocation theo policy. |
| Main Flow | 1. User chọn chu kỳ. 2. User nhập lượng thời gian phân bổ. 3. Hệ thống kiểm tra available time. 4. Hệ thống ghi nhận allocation. 5. Hệ thống cập nhật remaining time. 6. Hệ thống ghi nhận history. |
| Alternative Flow | Nếu vượt khả dụng, User xác nhận over allocation nếu được phép. |
| Exception Flow | Lượng phân bổ không hợp lệ hoặc không đủ nguồn lực và không cho phép over allocation dẫn đến từ chối. |
| Postconditions | Time Capital được phân bổ nếu hợp lệ. |
| Business Rules | RCM-BR-013, RCM-BR-015, RCM-BR-016, RCM-BR-022 |

### RCM-UC-013 - Allocate Money Capital

| Field | Description |
|---|---|
| ID | RCM-UC-013 |
| Description | User phân bổ một phần Money Capital cho mục đích sử dụng hợp lệ. |
| Primary Actor | User |
| Trigger | User yêu cầu phân bổ Money Capital. |
| Preconditions | Chu kỳ active; User có Money Capital khả dụng hoặc cho phép over allocation theo policy. |
| Main Flow | 1. User chọn chu kỳ. 2. User nhập lượng tiền phân bổ. 3. Hệ thống kiểm tra available money. 4. Hệ thống ghi nhận allocation. 5. Hệ thống cập nhật remaining money. 6. Hệ thống ghi nhận history. |
| Alternative Flow | Nếu vượt khả dụng, User xác nhận over allocation nếu được phép. |
| Exception Flow | Lượng phân bổ không hợp lệ hoặc không đủ nguồn lực và không cho phép over allocation dẫn đến từ chối. |
| Postconditions | Money Capital được phân bổ nếu hợp lệ. |
| Business Rules | RCM-BR-014, RCM-BR-015, RCM-BR-016, RCM-BR-022 |

### RCM-UC-014 - Reallocate Capital

| Field | Description |
|---|---|
| ID | RCM-UC-014 |
| Description | User điều chỉnh lượng nguồn vốn đã phân bổ. |
| Primary Actor | User |
| Trigger | User chọn một allocation để reallocate. |
| Preconditions | Allocation tồn tại, thuộc User và còn hiệu lực. |
| Main Flow | 1. User chọn allocation. 2. User nhập giá trị phân bổ mới. 3. Hệ thống kiểm tra ownership, trạng thái và balance. 4. User xác nhận. 5. Hệ thống cập nhật allocation. 6. Hệ thống ghi nhận history. |
| Alternative Flow | Giá trị mới nhỏ hơn giá trị cũ thì phần chênh lệch được trả về remaining capital. |
| Exception Flow | Allocation không hợp lệ hoặc reallocation vi phạm over allocation policy dẫn đến từ chối. |
| Postconditions | Allocation và balance được cập nhật nếu hợp lệ. |
| Business Rules | RCM-BR-023, RCM-BR-029, RCM-BR-030 |

### RCM-UC-015 - Release Allocated Capital

| Field | Description |
|---|---|
| ID | RCM-UC-015 |
| Description | User giải phóng nguồn vốn đã phân bổ nhưng không còn cần giữ lại. |
| Primary Actor | User |
| Trigger | User chọn release allocation. |
| Preconditions | Allocation thuộc User và còn lượng nguồn vốn có thể giải phóng. |
| Main Flow | 1. User chọn allocation. 2. User nhập lượng release. 3. Hệ thống kiểm tra lượng release. 4. User xác nhận. 5. Hệ thống release capital. 6. Hệ thống cập nhật remaining capital. 7. Hệ thống ghi nhận history. |
| Alternative Flow | User release toàn bộ allocation. |
| Exception Flow | Lượng release vượt allocated capital còn hiệu lực dẫn đến từ chối. |
| Postconditions | Remaining capital tăng tương ứng nếu release thành công. |
| Business Rules | RCM-BR-024, RCM-BR-028 |

### RCM-UC-016 - Allow Over Allocation

| Field | Description |
|---|---|
| ID | RCM-UC-016 |
| Description | User chủ động cho phép phân bổ vượt nguồn vốn khả dụng theo chính sách. |
| Primary Actor | User |
| Trigger | User thực hiện allocation hoặc adjustment dẫn đến remaining capital âm. |
| Preconditions | Over allocation policy cho phép và User có quyền với chu kỳ. |
| Main Flow | 1. Hệ thống phát hiện phân bổ vượt khả dụng. 2. Hệ thống hiển thị cảnh báo nghiệp vụ. 3. User xác nhận cho phép vượt mức. 4. Hệ thống ghi nhận quyết định over allocation. 5. Hệ thống hoàn tất hành động nếu không vi phạm rule khác. |
| Alternative Flow | User hủy thao tác để không vượt mức. |
| Exception Flow | Chính sách không cho phép over allocation dẫn đến từ chối. |
| Postconditions | Remaining capital có thể âm và được đánh dấu là over allocation. |
| Business Rules | RCM-BR-016, RCM-BR-017, RCM-BR-027, RCM-BR-044 |

### RCM-UC-017 - Transfer Remaining Capital

| Field | Description |
|---|---|
| ID | RCM-UC-017 |
| Description | User chuyển remaining capital sang chu kỳ khác nếu chính sách cho phép. |
| Primary Actor | User |
| Trigger | User đóng chu kỳ hoặc yêu cầu chuyển số dư. |
| Preconditions | Chu kỳ nguồn và chu kỳ đích hợp lệ; policy cho phép transfer. |
| Main Flow | 1. User chọn remaining capital cần chuyển. 2. User chọn chu kỳ đích. 3. Hệ thống kiểm tra điều kiện transfer. 4. User xác nhận. 5. Hệ thống ghi nhận transfer. 6. Hệ thống ghi nhận history. |
| Alternative Flow | User không chuyển số dư và chỉ đóng chu kỳ. |
| Exception Flow | Remaining capital âm, chu kỳ đích không hợp lệ hoặc policy không cho phép dẫn đến từ chối. |
| Postconditions | Remaining capital được xử lý theo chính sách. |
| Business Rules | RCM-BR-031, RCM-BR-032, RCM-BR-033, RCM-BR-054 |

### RCM-UC-018 - View Available Capital

| Field | Description |
|---|---|
| ID | RCM-UC-018 |
| Description | User xem nguồn vốn khả dụng trong chu kỳ. |
| Primary Actor | User |
| Trigger | User yêu cầu xem available capital. |
| Preconditions | User có quyền xem chu kỳ của chính mình. |
| Main Flow | 1. User chọn chu kỳ. 2. Hệ thống kiểm tra ownership. 3. Hệ thống xác định available capital. 4. Hệ thống hiển thị available time và available money. |
| Alternative Flow | User xem theo từng loại nguồn lực. |
| Exception Flow | Chu kỳ không thuộc User dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | RCM-BR-039, RCM-BR-041 |

### RCM-UC-019 - View Allocated Capital

| Field | Description |
|---|---|
| ID | RCM-UC-019 |
| Description | User xem nguồn vốn đã phân bổ. |
| Primary Actor | User |
| Trigger | User yêu cầu xem allocated capital. |
| Preconditions | User có quyền xem chu kỳ. |
| Main Flow | 1. User chọn chu kỳ. 2. Hệ thống kiểm tra ownership. 3. Hệ thống hiển thị allocated time và allocated money. |
| Alternative Flow | User lọc theo mục tiêu phân bổ hoặc loại nguồn lực nếu được hỗ trợ. |
| Exception Flow | Chu kỳ không hợp lệ hoặc không thuộc User dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | RCM-BR-001, RCM-BR-041 |

### RCM-UC-020 - View Remaining Capital

| Field | Description |
|---|---|
| ID | RCM-UC-020 |
| Description | User xem nguồn vốn còn lại trong chu kỳ. |
| Primary Actor | User |
| Trigger | User yêu cầu xem remaining capital. |
| Preconditions | User có quyền xem chu kỳ. |
| Main Flow | 1. User chọn chu kỳ. 2. Hệ thống kiểm tra ownership. 3. Hệ thống xác định remaining capital. 4. Hệ thống hiển thị remaining time và remaining money. |
| Alternative Flow | Nếu remaining capital âm, hệ thống thể hiện như over allocation. |
| Exception Flow | Chu kỳ không hợp lệ hoặc không thuộc User dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | RCM-BR-017, RCM-BR-040, RCM-BR-041 |

### RCM-UC-021 - View Capital Summary

| Field | Description |
|---|---|
| ID | RCM-UC-021 |
| Description | User xem tóm tắt trạng thái nguồn vốn trong chu kỳ. |
| Primary Actor | User |
| Trigger | User yêu cầu xem summary. |
| Preconditions | User có quyền xem chu kỳ. |
| Main Flow | 1. User chọn chu kỳ. 2. Hệ thống kiểm tra ownership. 3. Hệ thống hiển thị planned, allocated, remaining và trạng thái chu kỳ. |
| Alternative Flow | User chọn xem chi tiết theo Time Capital hoặc Money Capital. |
| Exception Flow | Actor không có quyền dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | RCM-BR-038, RCM-BR-041 |

### RCM-UC-022 - View Allocation History

| Field | Description |
|---|---|
| ID | RCM-UC-022 |
| Description | User xem lịch sử phân bổ, phân bổ lại và giải phóng nguồn vốn. |
| Primary Actor | User |
| Trigger | User truy cập allocation history. |
| Preconditions | User có quyền xem lịch sử nguồn vốn của chính mình. |
| Main Flow | 1. User chọn chu kỳ hoặc bộ lọc. 2. Hệ thống kiểm tra quyền sở hữu. 3. Hệ thống hiển thị allocation history. |
| Alternative Flow | Không có lịch sử phù hợp, hệ thống thông báo không có kết quả. |
| Exception Flow | User yêu cầu lịch sử của người khác dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | RCM-BR-022, RCM-BR-023, RCM-BR-024, RCM-BR-049 |

### RCM-UC-023 - View Adjustment History

| Field | Description |
|---|---|
| ID | RCM-UC-023 |
| Description | User xem lịch sử điều chỉnh nguồn vốn. |
| Primary Actor | User |
| Trigger | User truy cập adjustment history. |
| Preconditions | User có quyền xem lịch sử nguồn vốn của chính mình. |
| Main Flow | 1. User chọn chu kỳ hoặc bộ lọc. 2. Hệ thống kiểm tra ownership. 3. Hệ thống hiển thị adjustment history. |
| Alternative Flow | User lọc theo Time Capital hoặc Money Capital. |
| Exception Flow | Actor không có quyền dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | RCM-BR-021, RCM-BR-026, RCM-BR-049 |

### RCM-UC-024 - Filter Capital History

| Field | Description |
|---|---|
| ID | RCM-UC-024 |
| Description | User lọc lịch sử nguồn vốn theo tiêu chí nghiệp vụ. |
| Primary Actor | User |
| Trigger | User chọn bộ lọc history. |
| Preconditions | User có quyền xem history. |
| Main Flow | 1. User chọn tiêu chí lọc. 2. Hệ thống kiểm tra tiêu chí hợp lệ. 3. Hệ thống hiển thị kết quả phù hợp. |
| Alternative Flow | Không có kết quả phù hợp, hệ thống thông báo không có dữ liệu. |
| Exception Flow | Tiêu chí không hợp lệ hoặc actor không có quyền dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu nguồn vốn. |
| Business Rules | RCM-BR-049 |

### RCM-UC-025 - Validate Capital Ownership

| Field | Description |
|---|---|
| ID | RCM-UC-025 |
| Description | Hệ thống kiểm tra quyền sở hữu trước khi cho phép xem hoặc thao tác nguồn vốn. |
| Primary Actor | System |
| Trigger | Actor yêu cầu truy cập nguồn vốn. |
| Preconditions | Actor đã xác thực. |
| Main Flow | 1. Hệ thống xác định actor. 2. Hệ thống xác định chủ sở hữu nguồn vốn. 3. Hệ thống so khớp quyền sở hữu hoặc quyền được cấp. 4. Hệ thống cho phép tiếp tục nếu hợp lệ. |
| Alternative Flow | Staff hoặc Admin có quyền xem giới hạn theo chính sách. |
| Exception Flow | Actor không có quyền dẫn đến từ chối. |
| Postconditions | Chỉ yêu cầu hợp lệ được tiếp tục xử lý. |
| Business Rules | RCM-BR-001, RCM-BR-002, RCM-BR-003, RCM-BR-047, RCM-BR-048 |

### RCM-UC-026 - Validate Capital Balance

| Field | Description |
|---|---|
| ID | RCM-UC-026 |
| Description | Hệ thống kiểm tra số dư nguồn vốn trước khi thực hiện hành động ảnh hưởng đến capital. |
| Primary Actor | System |
| Trigger | Allocation, reallocation, release, adjustment hoặc transfer được yêu cầu. |
| Preconditions | Chu kỳ hợp lệ và actor có quyền. |
| Main Flow | 1. Hệ thống xác định loại nguồn lực. 2. Hệ thống xác định planned, allocated và remaining. 3. Hệ thống kiểm tra rule liên quan. 4. Hệ thống cho phép hoặc từ chối hành động. |
| Alternative Flow | Nếu over allocation được cho phép, hệ thống yêu cầu User xác nhận. |
| Exception Flow | Giá trị không hợp lệ hoặc vi phạm policy dẫn đến từ chối. |
| Postconditions | Hành động chỉ được tiếp tục nếu balance hợp lệ theo rule. |
| Business Rules | RCM-BR-015, RCM-BR-016, RCM-BR-017, RCM-BR-039, RCM-BR-040 |

### RCM-UC-027 - View Authorized Capital Summary

| Field | Description |
|---|---|
| ID | RCM-UC-027 |
| Description | Staff hoặc Admin xem thông tin tóm tắt nguồn vốn của User nếu chính sách cho phép. |
| Primary Actor | Staff, Admin |
| Trigger | Staff hoặc Admin yêu cầu xem thông tin hỗ trợ hoặc vận hành. |
| Preconditions | Actor có quyền được phê duyệt và mục đích truy cập hợp lệ. |
| Main Flow | 1. Actor yêu cầu xem summary. 2. Hệ thống kiểm tra quyền. 3. Hệ thống hiển thị thông tin trong phạm vi được phép. |
| Alternative Flow | Actor chỉ được xem metadata hoặc trạng thái giới hạn theo chính sách. |
| Exception Flow | Không có quyền hoặc mục đích không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi nguồn vốn cá nhân của User. |
| Business Rules | RCM-BR-002, RCM-BR-003, RCM-BR-047, RCM-BR-048 |

## 11. User Stories

| Story ID | User Story |
|---|---|
| RCM-US-001 | As a User, I want to create a capital cycle so that I can manage my resources for a specific day, week or month. |
| RCM-US-002 | As a User, I want to update a capital cycle so that the cycle information remains accurate. |
| RCM-US-003 | As a User, I want to activate a capital cycle so that I can use it for resource allocation. |
| RCM-US-004 | As a User, I want to close a capital cycle so that I can finish resource management for that period. |
| RCM-US-005 | As a User, I want to reopen a closed cycle when allowed so that I can correct or continue resource handling when necessary. |
| RCM-US-006 | As a User, I want to set Time Capital so that I know how much time I can plan within a cycle. |
| RCM-US-007 | As a User, I want to set Money Capital so that I know how much money I can plan within a cycle. |
| RCM-US-008 | As a User, I want to adjust Time Capital so that my resource plan reflects changes in available time. |
| RCM-US-009 | As a User, I want to adjust Money Capital so that my resource plan reflects changes in available money. |
| RCM-US-010 | As a User, I want to allocate Time Capital so that I can reserve time for a planned purpose. |
| RCM-US-011 | As a User, I want to allocate Money Capital so that I can reserve money for a planned purpose. |
| RCM-US-012 | As a User, I want to reallocate capital so that I can respond when my plan changes. |
| RCM-US-013 | As a User, I want to release allocated capital so that unused resources become available again. |
| RCM-US-014 | As a User, I want to allow over allocation intentionally so that I can make a conscious exception when necessary. |
| RCM-US-015 | As a User, I want to see available capital so that I know what resources can still be allocated. |
| RCM-US-016 | As a User, I want to see allocated capital so that I know what resources are already committed. |
| RCM-US-017 | As a User, I want to see remaining capital so that I understand my resource balance. |
| RCM-US-018 | As a User, I want to see capital summary so that I can quickly understand my cycle status. |
| RCM-US-019 | As a User, I want to view allocation history so that I can understand how my resources were committed. |
| RCM-US-020 | As a User, I want to view adjustment history so that I can understand why my capital changed. |
| RCM-US-021 | As a User, I want to filter capital history so that I can find relevant changes more easily. |
| RCM-US-022 | As a User, I want to transfer remaining capital when allowed so that unused resources can support a future cycle. |
| RCM-US-023 | As the system, I want to validate ownership so that users can only manage their own capital. |
| RCM-US-024 | As the system, I want to validate balance before allocation so that resource rules are enforced. |
| RCM-US-025 | As a Staff member, I want to view limited capital information only when authorized so that I can support users without violating ownership boundaries. |
| RCM-US-026 | As an Admin, I want capital policy boundaries to be respected so that personal resource plans are protected. |

## 12. Acceptance Criteria

### 12.1 Capital Cycle

| AC ID | Given | When | Then |
|---|---|---|---|
| RCM-AC-001 | Given a User is authenticated | When the User creates a daily, weekly or monthly capital cycle with valid period information | Then the system creates the capital cycle. |
| RCM-AC-002 | Given a User creates a capital cycle | When the selected cycle type is not permitted | Then the system rejects the creation. |
| RCM-AC-003 | Given a User creates a capital cycle | When the cycle period violates overlap or duplication rules | Then the system rejects the creation. |
| RCM-AC-004 | Given a capital cycle belongs to the User | When the User updates editable cycle information in an editable status | Then the system updates the cycle. |
| RCM-AC-005 | Given a capital cycle is closed | When the User attempts to update it without reopening | Then the system rejects the update. |
| RCM-AC-006 | Given a capital cycle is valid for activation | When the User activates it | Then the system changes the cycle status according to policy. |
| RCM-AC-007 | Given an active cycle rule exists | When activation would violate that rule | Then the system rejects or handles activation according to policy. |
| RCM-AC-008 | Given a capital cycle can be closed | When the User confirms close | Then the system closes the cycle and records status history. |
| RCM-AC-009 | Given a capital cycle is closed and reopen is allowed | When the User confirms reopen | Then the system reopens the cycle and records history. |
| RCM-AC-010 | Given reopen is not allowed | When the User attempts to reopen a closed cycle | Then the system rejects the request. |

### 12.2 Time and Money Capital

| AC ID | Given | When | Then |
|---|---|---|---|
| RCM-AC-011 | Given a User has an editable cycle | When the User sets Time Capital with a value greater than or equal to zero | Then the system accepts the value. |
| RCM-AC-012 | Given a User sets Time Capital | When the value is negative | Then the system rejects the value. |
| RCM-AC-013 | Given a User has an editable cycle | When the User sets Money Capital with a value greater than or equal to zero | Then the system accepts the value. |
| RCM-AC-014 | Given a User sets Money Capital | When the value is negative | Then the system rejects the value. |
| RCM-AC-015 | Given Time Capital has been set | When the User adjusts Time Capital according to policy | Then the system updates capital and records adjustment history. |
| RCM-AC-016 | Given Money Capital has been set | When the User adjusts Money Capital according to policy | Then the system updates capital and records adjustment history. |
| RCM-AC-017 | Given an adjustment requires a reason | When the User submits adjustment without a reason | Then the system rejects the adjustment. |
| RCM-AC-018 | Given an adjustment would make capital invalid | When the User submits the adjustment | Then the system rejects the adjustment. |

### 12.3 Allocation, Reallocation and Release

| AC ID | Given | When | Then |
|---|---|---|---|
| RCM-AC-019 | Given a User has an active cycle with available Time Capital | When the User allocates a valid amount within available capital | Then the system records the allocation and updates balance. |
| RCM-AC-020 | Given a User has an active cycle with available Money Capital | When the User allocates a valid amount within available capital | Then the system records the allocation and updates balance. |
| RCM-AC-021 | Given allocation exceeds available capital | When over allocation is not allowed | Then the system rejects the allocation. |
| RCM-AC-022 | Given allocation exceeds available capital and policy allows over allocation | When the User explicitly confirms over allocation | Then the system records the allocation and marks remaining capital as over allocated. |
| RCM-AC-023 | Given allocation amount is zero or negative | When the User submits allocation | Then the system rejects the allocation. |
| RCM-AC-024 | Given an allocation belongs to the User | When the User reallocates it with a valid amount | Then the system updates allocation and records history. |
| RCM-AC-025 | Given reallocation violates balance policy | When the User submits reallocation | Then the system rejects the reallocation unless over allocation is allowed and confirmed. |
| RCM-AC-026 | Given an allocation has remaining allocated value | When the User releases a valid amount | Then the system releases capital and updates remaining capital. |
| RCM-AC-027 | Given release amount exceeds allocated amount | When the User submits release | Then the system rejects the release. |

### 12.4 Balance and History

| AC ID | Given | When | Then |
|---|---|---|---|
| RCM-AC-028 | Given a User has a capital cycle | When the User views available capital | Then the system displays available Time Capital and Money Capital according to business rules. |
| RCM-AC-029 | Given a User has allocations | When the User views allocated capital | Then the system displays allocated Time Capital and Money Capital. |
| RCM-AC-030 | Given a User has a capital cycle | When the User views remaining capital | Then the system displays remaining Time Capital and Money Capital. |
| RCM-AC-031 | Given remaining capital is negative due to allowed over allocation | When the User views remaining capital | Then the system identifies the state as over allocation. |
| RCM-AC-032 | Given a User views capital summary | When the cycle exists | Then the system displays planned, allocated, remaining and cycle status. |
| RCM-AC-033 | Given a User has allocation history | When the User views allocation history | Then the system displays relevant allocation, reallocation and release events. |
| RCM-AC-034 | Given a User has adjustment history | When the User views adjustment history | Then the system displays relevant adjustment events. |
| RCM-AC-035 | Given a User applies valid filters to capital history | When the User submits filter criteria | Then the system displays matching history records. |
| RCM-AC-036 | Given no history matches the filters | When the User filters history | Then the system displays a no-result state. |

### 12.5 Transfer, Ownership and Access

| AC ID | Given | When | Then |
|---|---|---|---|
| RCM-AC-037 | Given transfer remaining capital is allowed | When the User transfers positive remaining capital to a valid target cycle | Then the system records the transfer and updates related balances according to policy. |
| RCM-AC-038 | Given remaining capital is negative | When the User attempts to transfer it as positive balance | Then the system rejects the transfer. |
| RCM-AC-039 | Given a User attempts to access another user's capital | When the request is submitted | Then the system denies access. |
| RCM-AC-040 | Given Staff attempts to modify User capital without approved permission | When the request is submitted | Then the system denies the action. |
| RCM-AC-041 | Given Admin attempts to modify User capital without a defined exception | When the request is submitted | Then the system denies the action. |
| RCM-AC-042 | Given Staff or Admin has approved limited view permission | When the actor requests capital summary | Then the system displays only information within the allowed scope. |
| RCM-AC-043 | Given a capital-changing action succeeds | When the action completes | Then the system records corresponding history. |
| RCM-AC-044 | Given a capital-changing action is rejected | When validation fails | Then the system does not change current capital. |

## 13. Business Scenarios

### 13.1 Khởi tạo chu kỳ mới

User bắt đầu một tuần mới và muốn quản lý nguồn lực trong tuần. User tạo một weekly capital cycle, nhập Time Capital dự kiến và Money Capital dự kiến. Hệ thống kiểm tra chu kỳ không trùng với chu kỳ hiện có theo chính sách, kiểm tra các giá trị nguồn vốn hợp lệ và tạo chu kỳ.

Kết quả kỳ vọng: chu kỳ mới được tạo, nguồn vốn ban đầu được ghi nhận và User có thể kích hoạt chu kỳ nếu cần.

### 13.2 Điều chỉnh nguồn vốn

Trong chu kỳ đang active, User nhận thấy mình có thêm thời gian rảnh hoặc có khoản tiền bổ sung. User điều chỉnh tăng Time Capital hoặc Money Capital và nhập lý do nếu chính sách yêu cầu. Hệ thống kiểm tra điều chỉnh hợp lệ, cập nhật balance và ghi nhận adjustment history.

Kết quả kỳ vọng: nguồn vốn mới phản ánh điều kiện hiện tại và lịch sử điều chỉnh được lưu lại ở mức nghiệp vụ.

### 13.3 Phân bổ nguồn lực

User có một active cycle và muốn dành một phần Time Capital hoặc Money Capital cho một mục đích sử dụng. Hệ thống kiểm tra nguồn vốn khả dụng. Nếu đủ nguồn lực, hệ thống ghi nhận allocation, cập nhật allocated capital và remaining capital.

Kết quả kỳ vọng: nguồn vốn được phân bổ và User thấy số dư còn lại giảm tương ứng.

### 13.4 Giải phóng nguồn lực

User đã phân bổ một phần nguồn vốn nhưng sau đó không còn cần giữ toàn bộ phần đó. User chọn release một phần allocation. Hệ thống kiểm tra lượng release không vượt phần còn hiệu lực và sau đó trả phần nguồn vốn đó về remaining capital.

Kết quả kỳ vọng: nguồn vốn được giải phóng, remaining capital tăng lên và lịch sử release được ghi nhận.

### 13.5 Cho phép vượt mức

User cố gắng phân bổ Money Capital lớn hơn remaining capital. Hệ thống phát hiện vượt mức và hiển thị cảnh báo. Nếu policy cho phép và User xác nhận rõ ràng, hệ thống ghi nhận allocation, đánh dấu trạng thái over allocation và ghi nhận quyết định này trong history.

Kết quả kỳ vọng: allocation được thực hiện có ý thức, remaining capital có thể âm và tình trạng vượt mức được thể hiện rõ.

### 13.6 Đóng chu kỳ

Cuối tháng, User đóng monthly capital cycle. Hệ thống kiểm tra trạng thái chu kỳ, remaining capital và allocation còn hiệu lực. Nếu còn số dư và policy cho phép transfer, User có thể chọn chuyển số dư sang chu kỳ tiếp theo. Sau khi xác nhận, hệ thống đóng chu kỳ và ghi nhận lịch sử.

Kết quả kỳ vọng: chu kỳ được closed, dữ liệu nguồn vốn không bị mất và lịch sử trạng thái được giữ lại.

### 13.7 Xem lịch sử thay đổi

User muốn biết vì sao remaining capital thấp hơn dự kiến. User mở capital history và lọc theo adjustment và allocation trong tuần hiện tại. Hệ thống hiển thị các thay đổi liên quan, bao gồm thời điểm, loại hành động và lý do nếu được ghi nhận.

Kết quả kỳ vọng: User hiểu được các thay đổi đã làm số dư nguồn vốn biến động.

### 13.8 Xem số dư nguồn lực

User đang chuẩn bị lập kế hoạch cho các hoạt động tiếp theo và cần biết còn bao nhiêu nguồn lực. User xem available capital và remaining capital của chu kỳ active. Hệ thống hiển thị Time Capital và Money Capital riêng biệt, không cộng gộp hai loại nguồn lực.

Kết quả kỳ vọng: User biết nguồn lực nào còn có thể sử dụng và nguồn lực nào đã gần hết.

## 14. Edge Cases

| Edge Case ID | Scenario | Expected Business Handling |
|---|---|---|
| RCM-EC-001 | Chu kỳ chưa khởi tạo nhưng User cố phân bổ nguồn lực. | Hệ thống từ chối vì không có Capital Cycle hợp lệ. |
| RCM-EC-002 | Chu kỳ ở trạng thái draft nhưng chính sách yêu cầu active mới được phân bổ. | Hệ thống từ chối allocation. |
| RCM-EC-003 | Chu kỳ đã closed nhưng User cố adjust capital. | Hệ thống từ chối nếu chưa reopen. |
| RCM-EC-004 | User điều chỉnh nguồn vốn nhỏ hơn số đã phân bổ. | Hệ thống xử lý theo over allocation policy hoặc từ chối nếu không cho phép. |
| RCM-EC-005 | User giảm Time Capital làm remaining time âm. | Hệ thống cảnh báo và yêu cầu xác nhận over allocation nếu policy cho phép. |
| RCM-EC-006 | User giảm Money Capital làm remaining money âm. | Hệ thống cảnh báo và yêu cầu xác nhận over allocation nếu policy cho phép. |
| RCM-EC-007 | User phân bổ đồng thời trên nhiều thiết bị. | Kết quả cuối cùng phải tuân thủ available capital và over allocation policy. |
| RCM-EC-008 | User chỉnh sửa nguồn vốn khi đang có kế hoạch sử dụng đã phân bổ. | Hệ thống phải xác định tác động đến allocated và remaining capital trước khi chấp nhận. |
| RCM-EC-009 | User đóng chu kỳ khi vẫn còn allocation chưa được giải phóng. | Hệ thống cảnh báo hoặc từ chối theo chính sách close cycle. |
| RCM-EC-010 | User đóng chu kỳ khi remaining capital còn dương. | Hệ thống xử lý theo transfer hoặc close policy. |
| RCM-EC-011 | User đóng chu kỳ khi remaining capital âm. | Hệ thống cảnh báo over allocation và xử lý theo chính sách. |
| RCM-EC-012 | User cố transfer remaining capital âm. | Hệ thống từ chối. |
| RCM-EC-013 | User cố transfer remaining capital sang chu kỳ không hợp lệ. | Hệ thống từ chối. |
| RCM-EC-014 | User cố release nhiều hơn lượng đã phân bổ. | Hệ thống từ chối. |
| RCM-EC-015 | User cố reallocate allocation không còn hiệu lực. | Hệ thống từ chối. |
| RCM-EC-016 | User cố tạo chu kỳ trùng kỳ thời gian nếu policy không cho phép. | Hệ thống từ chối. |
| RCM-EC-017 | User tạo daily và weekly cycle có cùng ngày. | Hệ thống xử lý theo chính sách chồng lấn giữa loại chu kỳ. |
| RCM-EC-018 | User nhập Time Capital bằng 0. | Hệ thống chấp nhận nếu giá trị 0 được chính sách cho phép. |
| RCM-EC-019 | User nhập Money Capital bằng 0. | Hệ thống chấp nhận nếu giá trị 0 được chính sách cho phép. |
| RCM-EC-020 | User nhập allocation bằng 0. | Hệ thống từ chối vì allocation phải lớn hơn 0. |
| RCM-EC-021 | User nhập giá trị âm cho capital. | Hệ thống từ chối. |
| RCM-EC-022 | User nhập giá trị âm cho adjustment. | Hệ thống chỉ chấp nhận nếu đó là cách biểu diễn điều chỉnh giảm được chính sách cho phép và không vi phạm rule. |
| RCM-EC-023 | User thay đổi đơn vị Time Capital trong cùng chu kỳ. | Hệ thống từ chối hoặc yêu cầu quy đổi theo chính sách được phê duyệt. |
| RCM-EC-024 | User thay đổi đơn vị tiền tệ trong cùng chu kỳ. | Hệ thống từ chối nếu chưa có chính sách đa tiền tệ. |
| RCM-EC-025 | Staff cố điều chỉnh nguồn vốn của User. | Hệ thống từ chối nếu Staff không có quyền rõ ràng. |
| RCM-EC-026 | Admin cố thay đổi nguồn vốn cá nhân của User mà không có ngoại lệ được định nghĩa. | Hệ thống từ chối. |
| RCM-EC-027 | User cố xem history của User khác. | Hệ thống từ chối. |
| RCM-EC-028 | Reopen cycle sau khi đã transfer remaining capital. | Hệ thống xử lý theo chính sách để tránh tính số dư hai lần. |
| RCM-EC-029 | Close cycle nhiều lần. | Hệ thống thông báo chu kỳ đã closed, không tạo thêm thay đổi trùng. |
| RCM-EC-030 | Activate một chu kỳ khi đã có active cycle cùng loại. | Hệ thống xử lý theo active cycle policy. |
| RCM-EC-031 | Allocation target không hợp lệ. | Hệ thống từ chối phân bổ. |
| RCM-EC-032 | Adjustment reason quá mơ hồ hoặc bị bỏ trống khi bắt buộc. | Hệ thống từ chối hoặc yêu cầu bổ sung theo policy. |
| RCM-EC-033 | Lịch sử quá dài và User lọc không có tiêu chí. | Hệ thống hiển thị theo chính sách phân trang hoặc giới hạn nghiệp vụ nếu được định nghĩa. |
| RCM-EC-034 | User đang thao tác thì quyền truy cập bị thu hồi. | Hệ thống phải áp dụng kiểm soát quyền theo chính sách hiệu lực từ Volume 2. |
| RCM-EC-035 | Capital summary hiển thị số liệu không khớp với history. | Cần xử lý như vấn đề nhất quán nghiệp vụ và ưu tiên kiểm tra nguồn thay đổi. |

## 15. Validation Rules

| Validation Rule ID | Rule |
|---|---|
| RCM-VR-001 | Capital Cycle type phải thuộc danh sách được phê duyệt: daily, weekly, monthly. |
| RCM-VR-002 | Capital Cycle phải có kỳ thời gian xác định. |
| RCM-VR-003 | Capital Cycle không được trùng hoặc chồng lấn nếu chính sách không cho phép. |
| RCM-VR-004 | User chỉ được tạo, cập nhật hoặc xem Capital Cycle của chính mình. |
| RCM-VR-005 | Chu kỳ closed không được cập nhật nếu chưa reopen theo chính sách. |
| RCM-VR-006 | Chu kỳ không hợp lệ không được activate. |
| RCM-VR-007 | Active cycle phải tuân thủ quy tắc số lượng active cycle theo loại chu kỳ. |
| RCM-VR-008 | Time Capital phải lớn hơn hoặc bằng 0. |
| RCM-VR-009 | Money Capital phải lớn hơn hoặc bằng 0. |
| RCM-VR-010 | Allocation amount phải lớn hơn 0. |
| RCM-VR-011 | Release amount phải lớn hơn 0. |
| RCM-VR-012 | Release amount không được vượt allocation còn hiệu lực. |
| RCM-VR-013 | Reallocation amount phải hợp lệ theo loại nguồn lực. |
| RCM-VR-014 | Adjustment không được làm capital trở nên không hợp lệ. |
| RCM-VR-015 | Điều chỉnh giảm không được vượt giới hạn nghiệp vụ đã phê duyệt. |
| RCM-VR-016 | Allocation không được vượt available capital nếu over allocation chưa được xác nhận. |
| RCM-VR-017 | Over allocation phải được User xác nhận rõ ràng nếu policy cho phép. |
| RCM-VR-018 | Over allocation không được thực hiện nếu policy không cho phép. |
| RCM-VR-019 | Remaining capital âm phải được đánh dấu là over allocation. |
| RCM-VR-020 | Transfer remaining capital chỉ áp dụng cho remaining capital dương nếu chính sách không quy định khác. |
| RCM-VR-021 | Không được transfer remaining capital âm như một số dư hợp lệ. |
| RCM-VR-022 | Chu kỳ đích của transfer phải hợp lệ và thuộc User. |
| RCM-VR-023 | Lý do adjustment là bắt buộc nếu policy yêu cầu. |
| RCM-VR-024 | Lý do over allocation là bắt buộc nếu policy yêu cầu. |
| RCM-VR-025 | Lý do close hoặc reopen là bắt buộc nếu policy yêu cầu. |
| RCM-VR-026 | Time Capital và Money Capital phải được kiểm tra riêng biệt. |
| RCM-VR-027 | Không được cộng gộp Time Capital và Money Capital để bù trừ cho nhau. |
| RCM-VR-028 | Đơn vị Time Capital phải nhất quán trong một chu kỳ. |
| RCM-VR-029 | Đơn vị tiền tệ của Money Capital phải nhất quán trong một chu kỳ nếu chưa có policy đa tiền tệ. |
| RCM-VR-030 | Filter history phải sử dụng tiêu chí hợp lệ. |
| RCM-VR-031 | Staff chỉ được xem dữ liệu nguồn vốn nếu có quyền được phê duyệt. |
| RCM-VR-032 | Admin chỉ được xem hoặc can thiệp dữ liệu nguồn vốn theo chính sách được định nghĩa rõ. |
| RCM-VR-033 | Mọi thay đổi thành công ảnh hưởng đến capital phải tạo history event tương ứng theo policy. |
| RCM-VR-034 | Hành động bị từ chối không được làm thay đổi balance. |
| RCM-VR-035 | Capital summary phải kiểm tra nhất quán giữa planned, allocated và remaining theo business rule. |

## 16. Business Policies

### 16.1 Capital Allocation Policy

Capital Allocation Policy định nghĩa cách User phân bổ nguồn vốn trong một chu kỳ. Chính sách này nhằm bảo đảm nguồn lực được cấp phát có ý thức và không vượt quá khả năng hiện tại nếu chưa có quyết định chủ động.

Các nguyên tắc chính:

- Allocation chỉ được thực hiện trên Capital Cycle hợp lệ.
- Allocation phải xác định rõ loại nguồn lực: Time Capital, Money Capital hoặc cả hai nếu chính sách cho phép.
- Allocation amount phải lớn hơn 0.
- Allocation phải được kiểm tra với available capital.
- Nếu allocation vượt available capital, hệ thống phải áp dụng Over Allocation Policy.
- Allocation phải được ghi nhận trong lịch sử.
- Allocation không được làm mất hoặc ghi đè lịch sử nguồn vốn trước đó.

### 16.2 Capital Adjustment Policy

Capital Adjustment Policy định nghĩa cách User điều chỉnh Time Capital và Money Capital khi điều kiện thay đổi.

Các nguyên tắc chính:

- Adjustment có thể là tăng hoặc giảm nguồn vốn.
- Adjustment phải gắn với một Capital Cycle hợp lệ.
- Adjustment không được làm nguồn vốn trở nên không hợp lệ.
- Adjustment giảm phải xem xét tác động đến allocated capital và remaining capital.
- Nếu adjustment tạo remaining capital âm, hệ thống phải xử lý theo Over Allocation Policy.
- Lý do adjustment có thể bắt buộc tùy chính sách được phê duyệt.
- Adjustment phải được ghi nhận trong lịch sử.

### 16.3 Over Allocation Policy

Over Allocation Policy định nghĩa điều kiện cho phép User phân bổ vượt nguồn lực khả dụng.

Các nguyên tắc chính:

- Over allocation không phải là hành vi mặc định.
- Over allocation chỉ được thực hiện nếu chính sách cho phép.
- User phải xác nhận rõ ràng trước khi over allocation được áp dụng.
- Hệ thống phải cảnh báo rằng remaining capital sẽ âm hoặc vượt mức.
- Over allocation phải được ghi nhận trong lịch sử như một quyết định nghiệp vụ quan trọng.
- Over allocation không được hiểu là tăng nguồn vốn thực tế.
- Over allocation có thể được yêu cầu lý do nếu chính sách quy định.

### 16.4 Capital History Policy

Capital History Policy định nghĩa những hành động cần được ghi nhận để phục vụ truy vết và kiểm tra.

Các hành động nên được ghi nhận:

- Create Capital Cycle.
- Update Capital Cycle.
- Activate Capital Cycle.
- Close Capital Cycle.
- Reopen Capital Cycle.
- Set Time Capital.
- Set Money Capital.
- Adjust Time Capital.
- Adjust Money Capital.
- Allocate Capital.
- Reallocate Capital.
- Release Allocated Capital.
- Allow Over Allocation.
- Transfer Remaining Capital.

Các nguyên tắc chính:

- History phải phản ánh loại hành động, thời điểm, actor thực hiện và đối tượng nguồn vốn bị tác động ở mức nghiệp vụ.
- History không được bị mất khi capital được điều chỉnh.
- History chỉ được xem bởi actor có quyền.
- History phải hỗ trợ User hiểu vì sao số dư nguồn vốn thay đổi.

## 17. Risks

### 17.1 Business Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Người dùng không hiểu khái niệm capital | User có thể nhầm capital với số dư tài chính hoặc thời gian rảnh tuyệt đối. | Giảm adoption và sử dụng sai. | Thuật ngữ và hướng dẫn nghiệp vụ cần rõ ràng. |
| Over allocation bị lạm dụng | User thường xuyên cho phép vượt mức, làm giảm giá trị kiểm soát nguồn lực. | Kế hoạch mất tính thực tế. | Cảnh báo rõ và ghi nhận over allocation history. |
| Chu kỳ quá phức tạp | Daily, weekly và monthly có thể gây nhầm lẫn nếu quy tắc chồng lấn không rõ. | User không biết nên dùng chu kỳ nào. | Xác định policy chu kỳ và giải thích rõ. |
| Điều chỉnh capital quá thường xuyên | User liên tục điều chỉnh nguồn vốn để hợp thức hóa kế hoạch. | Giảm ý nghĩa của planned capital. | Ghi nhận adjustment history và lý do điều chỉnh. |
| Chuyển số dư không rõ policy | Remaining capital cuối chu kỳ có thể bị hiểu sai nếu chuyển sang chu kỳ sau. | Sai lệch kế hoạch và double counting. | Chỉ cho phép transfer khi policy rõ ràng. |

### 17.2 Operational Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Staff can thiệp quá mức | Staff có thể được kỳ vọng hỗ trợ bằng cách sửa dữ liệu User. | Rủi ro quyền riêng tư và trách nhiệm. | Giới hạn Staff, không cho sửa nguồn vốn nếu không có chính sách rõ. |
| Admin can thiệp kế hoạch cá nhân | Admin có quyền rộng nhưng không nên thay đổi dữ liệu cá nhân tùy ý. | Giảm niềm tin người dùng. | Tách quyền quản trị policy khỏi quyền chỉnh sửa dữ liệu cá nhân. |
| Không có lý do thay đổi | Adjustment hoặc over allocation không có lý do có thể khó kiểm tra. | Giảm giá trị history. | Xem xét bắt buộc reason cho thay đổi quan trọng. |
| Close cycle không nhất quán | User đóng chu kỳ khi còn allocation hoặc remaining capital chưa xử lý. | Dữ liệu khó diễn giải. | Xác định close cycle policy rõ ràng. |

### 17.3 Data Consistency Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Balance không khớp history | Số dư hiện tại không giải thích được từ lịch sử thay đổi. | User mất niềm tin vào hệ thống. | Bảo đảm mọi thay đổi quan trọng được ghi nhận. |
| Phân bổ đồng thời gây vượt mức ngoài ý muốn | User thao tác trên nhiều thiết bị hoặc nhiều phiên. | Remaining capital không như kỳ vọng. | Áp dụng validation tại thời điểm hành động và policy xử lý đồng thời. |
| Reopen sau transfer gây tính số dư hai lần | Chu kỳ đã chuyển remaining capital rồi được mở lại. | Số dư tương lai bị sai lệch ở góc độ nghiệp vụ. | Xác định policy reopen sau transfer. |
| Điều chỉnh giảm làm allocation vượt capital | Capital giảm nhưng allocation đã có vẫn giữ nguyên. | Phát sinh over allocation không rõ ràng. | Cảnh báo và áp dụng over allocation policy. |
| Đơn vị không nhất quán | Time hoặc Money dùng nhiều đơn vị không được kiểm soát. | Người dùng hiểu sai số dư. | Áp dụng unit consistency validation. |

## 18. Open Questions

| Question ID | Open Question | Impact Area |
|---|---|---|
| RCM-OQ-001 | Daily, weekly và monthly cycle có được tồn tại đồng thời cho cùng một khoảng thời gian không? | Capital Cycle |
| RCM-OQ-002 | Một User có được có nhiều active cycle cùng loại không? | Active Cycle Policy |
| RCM-OQ-003 | Chu kỳ bắt đầu và kết thúc theo múi giờ nào nếu User thay đổi vị trí? | Cycle Period |
| RCM-OQ-004 | Time Capital sử dụng đơn vị phút, giờ hay cho phép nhiều đơn vị? | Unit Policy |
| RCM-OQ-005 | Money Capital sử dụng một đơn vị tiền tệ cố định hay hỗ trợ nhiều đơn vị tiền tệ? | Money Policy |
| RCM-OQ-006 | Giá trị Time Capital bằng 0 có được chấp nhận trong mọi trường hợp không? | Validation |
| RCM-OQ-007 | Giá trị Money Capital bằng 0 có được chấp nhận trong mọi trường hợp không? | Validation |
| RCM-OQ-008 | Reopen closed cycle có được hỗ trợ chính thức không? | Cycle Lifecycle |
| RCM-OQ-009 | Nếu reopen được hỗ trợ, có giới hạn thời gian reopen sau khi close không? | Cycle Lifecycle |
| RCM-OQ-010 | Có bắt buộc nhập lý do khi adjust capital không? | Adjustment Policy |
| RCM-OQ-011 | Có bắt buộc nhập lý do khi allow over allocation không? | Over Allocation |
| RCM-OQ-012 | Over allocation có giới hạn tối đa không? | Over Allocation |
| RCM-OQ-013 | User có được tắt hoàn toàn cảnh báo over allocation không? | Usability, Risk |
| RCM-OQ-014 | Transfer remaining capital có được hỗ trợ ở release đầu tiên không? | Transfer Policy |
| RCM-OQ-015 | Remaining Time Capital có được chuyển sang chu kỳ sau hay chỉ Money Capital được chuyển? | Transfer Policy |
| RCM-OQ-016 | Nếu transfer remaining capital xong rồi reopen cycle, hệ thống cần xử lý số dư đã chuyển như thế nào? | Consistency |
| RCM-OQ-017 | Close cycle có được phép khi còn allocation chưa giải phóng không? | Close Policy |
| RCM-OQ-018 | Close cycle có tự động release allocation còn hiệu lực không, hay yêu cầu User xử lý thủ công? | Close Policy |
| RCM-OQ-019 | Staff có được xem capital summary của User để hỗ trợ không? | Access Policy |
| RCM-OQ-020 | Admin có được xem capital detail của User không, hay chỉ xem trạng thái tổng quát? | Privacy |
| RCM-OQ-021 | Có trường hợp nào Admin được chỉnh sửa capital của User không? | Governance |
| RCM-OQ-022 | History cần lưu lý do, actor, thời điểm và giá trị trước-sau ở mức nào? | History Policy |
| RCM-OQ-023 | History có được chỉnh sửa hoặc xóa bởi User không? | Auditability |
| RCM-OQ-024 | Actual Resource Consumption được ghi nhận trong module này hay chỉ được nhận từ module khác? | Scope Boundary |
| RCM-OQ-025 | Resource Efficiency trong module này chỉ hiển thị chỉ báo nền tảng hay tính toán chi tiết ở module khác? | Scope Boundary |

## 19. Suggested Improvements

Các đề xuất dưới đây là cải tiến nghiệp vụ tiềm năng. Chúng không phải là yêu cầu chính thức nếu chưa được stakeholder phê duyệt.

| Improvement ID | Suggested Improvement | Business Rationale |
|---|---|---|
| RCM-SI-001 | Thiết lập hướng dẫn chọn chu kỳ daily, weekly hoặc monthly. | Giúp User chọn đúng chu kỳ phù hợp với cách lập kế hoạch. |
| RCM-SI-002 | Cung cấp cảnh báo theo mức độ khi remaining capital thấp. | Giúp User nhận biết sớm nguy cơ thiếu nguồn lực. |
| RCM-SI-003 | Bắt buộc lý do cho adjustment lớn hoặc over allocation. | Tăng khả năng tự đánh giá và truy vết. |
| RCM-SI-004 | Thiết lập giới hạn over allocation tùy loại nguồn lực. | Tránh việc User vượt mức quá xa làm mất ý nghĩa kiểm soát. |
| RCM-SI-005 | Phân biệt adjustment do kế hoạch thay đổi và adjustment do ghi nhận sai. | Giúp history có ý nghĩa hơn khi phân tích sau này. |
| RCM-SI-006 | Cho phép User đặt mục tiêu sử dụng tối đa cho từng chu kỳ. | Hỗ trợ kỷ luật nguồn lực mà không cần khóa cứng mọi phân bổ. |
| RCM-SI-007 | Thiết lập policy chuyển số dư riêng cho Time Capital và Money Capital. | Time và Money có bản chất khác nhau, không nhất thiết xử lý giống nhau. |
| RCM-SI-008 | Cung cấp mô tả nghiệp vụ rõ cho trạng thái over allocation. | Tránh User hiểu remaining capital âm như nguồn vốn thực. |
| RCM-SI-009 | Định nghĩa access policy riêng cho Staff khi hỗ trợ User. | Cân bằng giữa khả năng hỗ trợ và quyền riêng tư cá nhân. |
| RCM-SI-010 | Thiết lập lịch sử thay đổi có phân loại mức độ quan trọng. | Giúp User dễ tìm thay đổi ảnh hưởng lớn đến nguồn vốn. |
| RCM-SI-011 | Cung cấp review cuối chu kỳ ở mức nguồn vốn. | Giúp User xem lại planned, allocated và remaining trước khi đóng chu kỳ. |
| RCM-SI-012 | Cho phép User cấu hình preference cảnh báo nguồn vốn. | Tăng tính cá nhân hóa mà vẫn giữ nguyên tắc kiểm soát. |
| RCM-SI-013 | Thiết lập policy không cho xóa history nguồn vốn. | Bảo vệ khả năng truy vết và giải thích biến động số dư. |
| RCM-SI-014 | Định nghĩa rõ cách xử lý cycle overlap giữa daily, weekly và monthly. | Giảm nhầm lẫn khi User dùng nhiều cấp chu kỳ. |
| RCM-SI-015 | Xem xét access review định kỳ cho quyền Staff/Admin liên quan đến dữ liệu nguồn vốn. | Bảo vệ dữ liệu cá nhân và giảm rủi ro vận hành. |

## Appendix A. Traceability Summary

| Source | Related RCM Content |
|---|---|
| Volume 1 - Product Philosophy | Mọi công việc tiêu tốn nguồn lực và mọi nguồn lực hữu hạn được phản ánh trong capital, allocation và over allocation. |
| Volume 1 - Business Objectives | Resource Capital Management hỗ trợ visibility, allocation, planned vs actual foundation và continuous improvement. |
| Volume 1 - Actors | User, Staff và Admin được kế thừa với ranh giới trách nhiệm phù hợp. |
| Volume 2 - Identity & Authorization | Ownership validation và access control trong RCM phụ thuộc vào xác thực và phân quyền. |

## Appendix B. RCM Glossary

| Term | Definition |
|---|---|
| Allocated Capital | Lượng nguồn vốn đã được phân bổ cho một mục đích sử dụng cụ thể. |
| Available Capital | Lượng nguồn vốn có thể sử dụng để phân bổ tại thời điểm xem xét. |
| Capital | Tổng năng lực nguồn lực của User trong một chu kỳ. |
| Capital Cycle | Khoảng thời gian daily, weekly hoặc monthly dùng để quản lý nguồn vốn. |
| Capital History | Lịch sử thay đổi liên quan đến nguồn vốn, phân bổ, điều chỉnh và trạng thái chu kỳ. |
| Money Capital | Lượng tiền User xác định có thể sử dụng trong một chu kỳ. |
| Over Allocation | Trạng thái phân bổ vượt nguồn vốn khả dụng được User chủ động cho phép theo chính sách. |
| Planned Capital | Nguồn vốn dự kiến hoặc được thiết lập ban đầu cho chu kỳ. |
| Reallocation | Việc điều chỉnh lại lượng nguồn vốn đã phân bổ. |
| Release | Việc giải phóng nguồn vốn đã phân bổ để đưa về trạng thái có thể sử dụng lại. |
| Remaining Capital | Lượng nguồn vốn còn lại sau khi xét capital, adjustment và allocation. |
| Resource | Nguồn lực hữu hạn mà User có thể dùng để lập kế hoạch và thực hiện hoạt động. |
| Resource Efficiency | Mức độ sử dụng nguồn lực hợp lý so với kế hoạch và kết quả kỳ vọng. |
| Time Capital | Lượng thời gian User xác định có thể sử dụng trong một chu kỳ. |
| Transfer Remaining Capital | Việc chuyển số dư còn lại sang chu kỳ khác nếu chính sách cho phép. |

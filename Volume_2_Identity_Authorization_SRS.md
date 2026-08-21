# LifeBalance
# Volume 2 - Identity & Authorization SRS

## 1. Module Overview

### 1.1 Giới thiệu module

Identity & Authorization là module chịu trách nhiệm xác định danh tính người dùng, kiểm soát trạng thái truy cập, xác thực phiên làm việc, phân quyền theo vai trò và bảo đảm mỗi tác nhân chỉ được thực hiện những hành động phù hợp với vai trò được cấp. Trong phạm vi LifeBalance, module này đóng vai trò là lớp kiểm soát nghiệp vụ nền tảng trước khi người dùng hoặc nhân sự vận hành có thể truy cập các khu vực chức năng khác của hệ thống.

Tài liệu này là Software Requirement Specification cho module Identity & Authorization, được xây dựng kế thừa từ Volume 1 - Tầm nhìn & Tổng quan nghiệp vụ. Theo định hướng của Volume 1, LifeBalance là hệ thống quản lý nguồn lực cá nhân, trong đó dữ liệu người dùng có tính riêng tư và cần được bảo vệ bằng cơ chế nhận diện, xác thực và phân quyền phù hợp. Module Identity & Authorization không trực tiếp xử lý nghiệp vụ lập kế hoạch, phân bổ, theo dõi hoặc đánh giá nguồn lực; module này chỉ xác định ai được truy cập, truy cập trong điều kiện nào, và được phép thực hiện nhóm hành động nào.

Hệ thống sử dụng mô hình Role Based Access Control (RBAC). Theo mô hình này, quyền truy cập không được cấp tùy tiện theo từng cá nhân ở mọi tình huống, mà được quản lý thông qua vai trò. Một actor được gán một hoặc nhiều vai trò phù hợp, và vai trò đó bao gồm các quyền được phê duyệt. RBAC giúp đơn giản hóa quản trị, tăng tính nhất quán, hỗ trợ audit và giảm rủi ro cấp quyền sai.

### 1.2 Vai trò của module trong LifeBalance

Module Identity & Authorization có các vai trò nghiệp vụ chính sau:

- Xác định danh tính của người truy cập hệ thống.
- Phân biệt giữa Guest, User, Staff và Admin.
- Hỗ trợ đăng ký, đăng nhập, đăng xuất và quản lý phiên làm việc ở mức nghiệp vụ.
- Hỗ trợ quản lý thông tin hồ sơ cá nhân trong phạm vi danh tính người dùng.
- Kiểm soát quyền truy cập dựa trên vai trò và permission.
- Hỗ trợ Admin quản lý người dùng, nhân sự Staff, role và permission.
- Hỗ trợ Staff thực hiện các quyền vận hành được cấp, bao gồm hỗ trợ người dùng và khóa tạm thời tài khoản theo phạm vi được cho phép.
- Hỗ trợ ghi nhận và xem audit đối với các thay đổi nhạy cảm liên quan đến tài khoản, role, permission và trạng thái truy cập.
- Xử lý các tình huống truy cập không hợp lệ, không xác thực hoặc không đủ quyền theo quy tắc nghiệp vụ thống nhất.

### 1.3 Mục tiêu của module

| Mục tiêu | Mô tả |
|---|---|
| Bảo đảm định danh người dùng | Mọi hoạt động yêu cầu danh tính phải được thực hiện bởi actor đã được xác thực hợp lệ. |
| Kiểm soát truy cập theo vai trò | Hệ thống phải xác định quyền truy cập dựa trên role và permission được gán. |
| Bảo vệ tài khoản | Trạng thái tài khoản, mật khẩu, phiên làm việc và quyền truy cập phải được kiểm soát theo quy tắc nghiệp vụ. |
| Hỗ trợ quản trị IAM | Admin có thể quản lý User, Staff, Role, Permission và các cấu hình liên quan đến phân quyền ở mức nghiệp vụ. |
| Hỗ trợ vận hành an toàn | Staff có thể hỗ trợ người dùng và thực hiện các hành động vận hành giới hạn, không vượt quá phạm vi quyền được cấp. |
| Hỗ trợ audit | Những thay đổi nhạy cảm về tài khoản, role, permission và trạng thái truy cập phải có khả năng được xem xét lại. |
| Tăng tính nhất quán | Các quyết định cho phép, từ chối, khóa, mở khóa, gán quyền và thu hồi quyền phải tuân theo business rule thống nhất. |

### 1.4 Business Value

Module Identity & Authorization mang lại giá trị nghiệp vụ quan trọng cho LifeBalance.

Thứ nhất, module bảo vệ quyền riêng tư của người dùng. LifeBalance có thể chứa thông tin cá nhân liên quan đến hồ sơ, lịch sử hoạt động và quyền truy cập vào các khu vực chức năng của hệ thống. Nếu không có kiểm soát danh tính và phân quyền, người dùng có thể bị truy cập trái phép hoặc dữ liệu nhạy cảm có thể bị lộ.

Thứ hai, module hỗ trợ vận hành có kiểm soát. Staff cần có quyền hỗ trợ người dùng, nhưng không được có quyền quản trị cao như thay đổi role hoặc permission. Admin cần có quyền quản trị rộng hơn, nhưng các thay đổi quan trọng vẫn phải được audit để bảo đảm trách nhiệm giải trình.

Thứ ba, module tạo nền tảng cho các volume yêu cầu khác. Các module nghiệp vụ khác của LifeBalance cần dựa vào Identity & Authorization để xác định actor, quyền truy cập và trạng thái tài khoản trước khi xử lý chức năng chuyên biệt.

Thứ tư, module giảm rủi ro vận hành. RBAC giúp tránh tình trạng cấp quyền trực tiếp không kiểm soát, giảm khả năng nhân sự vận hành có quyền vượt quá trách nhiệm và hỗ trợ quản lý vòng đời tài khoản rõ ràng hơn.

Thứ năm, module hỗ trợ tuân thủ ở mức nghiệp vụ. Việc ghi nhận audit, kiểm soát truy cập, xử lý tài khoản khóa và quản lý permission giúp hệ thống có nền tảng phù hợp cho các yêu cầu về bảo vệ dữ liệu cá nhân, trách nhiệm giải trình và an toàn vận hành.

### 1.5 Định nghĩa phạm vi tài liệu

Tài liệu này chỉ tập trung vào Identity & Authorization. Các quyền liên quan đến khu vực chức năng khác chỉ được nhắc đến như permission label trong mô hình phân quyền, không phân tích nghiệp vụ chi tiết của các module đó. Tài liệu không mô tả quy trình nghiệp vụ của Task, Resource, Timeline, Category, Tag, Dashboard, Reporting hoặc Support ngoài khía cạnh quyền truy cập IAM.

Tài liệu không bao gồm thiết kế cơ sở dữ liệu, ERD, bảng dữ liệu, API, SQL, kiến trúc hệ thống, microservice, giao diện người dùng hoặc mã nguồn.

## 2. Scope

### 2.1 In Scope

| Phạm vi | Mô tả |
|---|---|
| Guest Access | Xác định các quyền truy cập công khai cơ bản của Guest như xem landing page, đăng ký và đăng nhập. |
| User Registration | Hỗ trợ người dùng tạo tài khoản ở mức yêu cầu nghiệp vụ. |
| Authentication | Hỗ trợ đăng nhập, đăng xuất, xác thực trạng thái phiên và xác thực token ở mức nghiệp vụ. |
| Profile Management | Cho phép User, Staff và Admin quản lý thông tin hồ sơ của chính mình trong phạm vi cho phép. |
| Password Management | Hỗ trợ đổi mật khẩu và khôi phục mật khẩu ở mức nghiệp vụ. |
| Session Management | Quản lý trạng thái phiên làm việc, hết hạn phiên, đăng xuất và xử lý phiên không hợp lệ ở mức nghiệp vụ. |
| Account Status Validation | Kiểm tra trạng thái tài khoản trước khi cho phép đăng nhập hoặc tiếp tục truy cập. |
| Access Control | Xác định người dùng đã xác thực có được truy cập một chức năng hay không. |
| Authorization | Kiểm tra role và permission trước khi cho phép thực hiện hành động. |
| RBAC Management | Quản lý role, permission, gán role, thu hồi role, gán permission và thu hồi permission ở mức nghiệp vụ. |
| User Management | Cho phép Admin tìm kiếm, xem chi tiết, cập nhật trạng thái, deactivate và reactivate User theo quy tắc. |
| Staff Management | Cho phép Admin quản lý Staff theo phạm vi quyền quản trị. |
| Temporary Lock and Unlock | Cho phép Staff hoặc Admin khóa tạm thời tài khoản theo phạm vi được cấp, và cho phép Admin mở khóa theo quy tắc. |
| Audit View | Cho phép actor được phân quyền xem thông tin audit liên quan đến hoạt động IAM. |
| System Configuration Authorization | Kiểm soát quyền truy cập vào cấu hình hệ thống ở mức authorization. |
| Unauthorized Handling | Xử lý tình huống chưa xác thực nhưng truy cập khu vực yêu cầu đăng nhập. |
| Forbidden Handling | Xử lý tình huống đã xác thực nhưng không đủ quyền. |

### 2.2 Out of Scope

| Ngoài phạm vi | Giải thích |
|---|---|
| Thiết kế database | Không xác định bảng, cột, khóa, index, quan hệ hoặc mô hình lưu trữ. |
| Thiết kế ERD | Không mô tả thực thể dữ liệu hoặc quan hệ dữ liệu dưới dạng ERD. |
| Thiết kế API | Không xác định endpoint, request, response, status code kỹ thuật hoặc contract tích hợp. |
| Thiết kế kiến trúc | Không xác định layer, service, deployment, infrastructure hoặc microservice. |
| Thiết kế giao diện | Không mô tả wireframe, layout, screen flow hoặc thành phần UI. |
| Quy trình nghiệp vụ của module khác | Không mô tả chi tiết Task, Resource, Timeline, Category, Tag, Dashboard, Reporting hoặc Support. |
| Authentication nâng cao chưa được xác nhận | MFA, SSO, social login, biometric login và enterprise identity federation chưa được xem là yêu cầu chính thức. |
| Chính sách pháp lý chi tiết | Các yêu cầu compliance cụ thể theo luật hoặc khu vực cần được xác minh riêng. |
| Phân quyền cấp dữ liệu chi tiết | Tài liệu không định nghĩa row-level access, field-level access hoặc masking theo thiết kế kỹ thuật. |

### 2.3 Dependencies

| Dependency | Mô tả | Ảnh hưởng |
|---|---|---|
| Volume 1 - Vision & Business Overview | Cung cấp tầm nhìn, actor, business principle và phạm vi tổng thể. | Tài liệu IAM phải nhất quán với actor Guest, User, Staff và Admin. |
| Business Policy về tài khoản | Cần xác định chính sách mật khẩu, khóa tài khoản, khôi phục tài khoản và vòng đời tài khoản. | Một số business rule cần được xác nhận thêm trong Open Questions. |
| Operational Policy | Cần xác định quyền hạn của Staff khi hỗ trợ và khóa tạm thời tài khoản. | Ảnh hưởng đến giới hạn trách nhiệm của Staff. |
| Audit Policy | Cần xác định loại hoạt động IAM nào phải được audit và thời hạn lưu vết nghiệp vụ. | Ảnh hưởng đến auditability và compliance. |
| Security Policy | Cần xác định chuẩn bảo vệ tài khoản, quản lý phiên, token và xử lý truy cập không hợp lệ. | Ảnh hưởng đến NFR về security. |
| System Configuration Policy | Cần xác định cấu hình hệ thống nào yêu cầu Admin authorization. | Ảnh hưởng đến quản trị cấu hình. |
| Legal and Compliance Review | Cần xác minh yêu cầu về dữ liệu cá nhân nếu hệ thống triển khai thực tế. | Ảnh hưởng đến điều khoản bảo vệ dữ liệu, quyền người dùng và audit. |

## 3. Actors

### 3.1 Guest

| Thuộc tính | Mô tả |
|---|---|
| Description | Guest là người truy cập chưa đăng nhập hoặc chưa có tài khoản hợp lệ trong LifeBalance. |
| Responsibilities | Tìm hiểu thông tin công khai, thực hiện đăng ký nếu muốn trở thành User, thực hiện đăng nhập nếu đã có tài khoản. |
| Permissions | View Landing Page; Register; Login. |
| Limitations | Không được truy cập hồ sơ cá nhân, khu vực người dùng đã xác thực, khu vực vận hành, khu vực quản trị, audit, role, permission hoặc bất kỳ chức năng yêu cầu xác thực nào. |

Guest là actor có quyền hạn tối thiểu. Guest chỉ được thực hiện các hành động phục vụ việc tiếp cận hệ thống ban đầu. Nếu Guest cố gắng truy cập chức năng yêu cầu xác thực, hệ thống phải xử lý theo cơ chế Unauthorized Handling.

### 3.2 User

| Thuộc tính | Mô tả |
|---|---|
| Description | User là người dùng đã đăng ký và đăng nhập hợp lệ, sử dụng LifeBalance cho mục đích cá nhân. |
| Responsibilities | Quản lý thông tin hồ sơ của chính mình, bảo vệ thông tin đăng nhập, sử dụng hệ thống trong phạm vi quyền được cấp. |
| Permissions | Manage Profile; Manage Task; Manage Timeline; Manage Resource Capital; Manage Category; Manage Tag; View Dashboard; View Reports; View Activity History. |
| Limitations | Không được quản lý User khác, Staff, Role, Permission, cấu hình hệ thống, audit hệ thống hoặc thực hiện khóa tài khoản. User chỉ được quản lý dữ liệu và quyền truy cập của chính mình trong phạm vi được cấp. |

Trong tài liệu này, các permission liên quan đến chức năng ngoài Identity chỉ được xem là quyền truy cập ở cấp IAM. Tài liệu không mô tả nghiệp vụ chi tiết của các khu vực đó.

### 3.3 Staff

| Thuộc tính | Mô tả |
|---|---|
| Description | Staff là actor vận hành được cấp quyền hỗ trợ người dùng và xử lý một số tình huống truy cập theo phạm vi giới hạn. |
| Responsibilities | Tiếp nhận yêu cầu hỗ trợ, quản lý ticket, hỗ trợ User, xem log trong phạm vi được cấp và khóa tạm thời User khi có căn cứ nghiệp vụ. |
| Permissions | Receive Support Ticket; Manage Ticket; Support User; View Log; Temporary Lock User. |
| Limitations | Không được quản lý Role, Permission, System Configuration hoặc tự ý thay đổi quyền truy cập. Staff không được cấp quyền Admin trừ khi được Admin gán role theo quy trình được phê duyệt. Staff không được khóa chính mình nếu quy tắc nghiệp vụ không cho phép. |

Staff là vai trò vận hành có quyền cao hơn User nhưng thấp hơn Admin. Quyền của Staff phải được giới hạn để hỗ trợ vận hành mà không tạo rủi ro quản trị quá mức.

### 3.4 Admin

| Thuộc tính | Mô tả |
|---|---|
| Description | Admin là actor quản trị có quyền quản lý người dùng, Staff, Role, Permission, cấu hình hệ thống liên quan đến authorization, audit và dashboard hệ thống ở mức được phân quyền. |
| Responsibilities | Quản lý vòng đời tài khoản, quản lý role, permission, cấu hình phân quyền, xem audit, kiểm soát trạng thái truy cập và duy trì governance của hệ thống. |
| Permissions | Manage User; Manage Staff; Manage Role; Manage Permission; Manage System Configuration; Manage Audit; View System Dashboard. |
| Limitations | Admin vẫn phải tuân thủ business rule, audit, chính sách bảo vệ dữ liệu và nguyên tắc trách nhiệm giải trình. Một số hành động tự tác động đến quyền Admin của chính mình cần được kiểm soát bằng rule hoặc chính sách bổ sung. |

Admin có phạm vi quyền rộng nhất trong mô hình RBAC hiện tại. Tuy nhiên, "Admin có toàn quyền" trong tài liệu này được hiểu là toàn quyền trong phạm vi hệ thống và role được phê duyệt, không có nghĩa là được bỏ qua audit, compliance, security rule hoặc business constraint.

## 4. Functional Requirements

### 4.1 Danh sách Functional Requirements

| Requirement ID | Requirement Name | Description | Primary Actor |
|---|---|---|---|
| IAM-FR-001 | Landing Page Access | Hệ thống phải cho phép Guest truy cập landing page công khai của LifeBalance. | Guest |
| IAM-FR-002 | Register | Hệ thống phải cho phép Guest đăng ký tài khoản mới theo thông tin bắt buộc được xác định bởi chính sách nghiệp vụ. | Guest |
| IAM-FR-003 | Registration Validation | Hệ thống phải kiểm tra tính hợp lệ của thông tin đăng ký trước khi tạo tài khoản. | Guest |
| IAM-FR-004 | Login | Hệ thống phải cho phép actor có tài khoản hợp lệ đăng nhập bằng thông tin định danh và thông tin xác thực được chấp nhận. | User, Staff, Admin |
| IAM-FR-005 | Login Failure Handling | Hệ thống phải xử lý đăng nhập thất bại theo quy tắc nghiệp vụ, bao gồm thông tin không đúng, tài khoản không tồn tại, tài khoản bị khóa hoặc tài khoản không còn hoạt động. | User, Staff, Admin |
| IAM-FR-006 | Logout | Hệ thống phải cho phép actor đã đăng nhập kết thúc phiên làm việc hiện tại. | User, Staff, Admin |
| IAM-FR-007 | View Profile | Hệ thống phải cho phép actor đã đăng nhập xem hồ sơ của chính mình. | User, Staff, Admin |
| IAM-FR-008 | Update Profile | Hệ thống phải cho phép actor đã đăng nhập cập nhật thông tin hồ sơ của chính mình trong phạm vi được phép. | User, Staff, Admin |
| IAM-FR-009 | Change Password | Hệ thống phải cho phép actor đã đăng nhập đổi mật khẩu khi đáp ứng điều kiện xác minh hiện tại và chính sách mật khẩu. | User, Staff, Admin |
| IAM-FR-010 | Forgot Password | Hệ thống phải hỗ trợ quy trình khôi phục mật khẩu cho tài khoản hợp lệ theo chính sách xác minh danh tính được phê duyệt. | User, Staff, Admin |
| IAM-FR-011 | Session Management | Hệ thống phải quản lý trạng thái phiên làm việc, bao gồm phiên hợp lệ, hết hạn, bị kết thúc và không hợp lệ. | User, Staff, Admin |
| IAM-FR-012 | Token Validation | Hệ thống phải kiểm tra tính hợp lệ của token hoặc thông tin chứng minh phiên ở mức nghiệp vụ trước khi cho phép truy cập chức năng yêu cầu xác thực. | User, Staff, Admin |
| IAM-FR-013 | Account Status Validation | Hệ thống phải kiểm tra trạng thái tài khoản trước khi cho phép đăng nhập hoặc tiếp tục truy cập. | User, Staff, Admin |
| IAM-FR-014 | Access Control | Hệ thống phải xác định actor đã xác thực có được phép truy cập khu vực hoặc chức năng được yêu cầu hay không. | User, Staff, Admin |
| IAM-FR-015 | Authorization | Hệ thống phải kiểm tra role và permission của actor trước khi cho phép thực hiện hành động được bảo vệ. | User, Staff, Admin |
| IAM-FR-016 | Permission Validation | Hệ thống phải xác thực rằng actor có permission phù hợp với hành động được yêu cầu. | User, Staff, Admin |
| IAM-FR-017 | Unauthorized Handling | Hệ thống phải xử lý trường hợp actor chưa xác thực cố gắng truy cập khu vực yêu cầu đăng nhập. | Guest |
| IAM-FR-018 | Forbidden Handling | Hệ thống phải xử lý trường hợp actor đã xác thực nhưng không có permission cần thiết. | User, Staff, Admin |
| IAM-FR-019 | Search User | Hệ thống phải cho phép Admin tìm kiếm User hoặc Staff theo tiêu chí được phê duyệt ở mức nghiệp vụ. | Admin |
| IAM-FR-020 | View User Detail | Hệ thống phải cho phép Admin xem thông tin chi tiết tài khoản của User hoặc Staff trong phạm vi được phép. | Admin |
| IAM-FR-021 | Update User | Hệ thống phải cho phép Admin cập nhật thông tin tài khoản người dùng trong phạm vi quản trị được phê duyệt. | Admin |
| IAM-FR-022 | Deactivate User | Hệ thống phải cho phép Admin vô hiệu hóa tài khoản User hoặc Staff theo quy tắc nghiệp vụ. | Admin |
| IAM-FR-023 | Reactivate User | Hệ thống phải cho phép Admin kích hoạt lại tài khoản đã bị vô hiệu hóa nếu đáp ứng điều kiện nghiệp vụ. | Admin |
| IAM-FR-024 | Temporary Lock User | Hệ thống phải cho phép Staff hoặc Admin khóa tạm thời tài khoản User theo phạm vi quyền được cấp. | Staff, Admin |
| IAM-FR-025 | Unlock User | Hệ thống phải cho phép Admin mở khóa tài khoản bị khóa tạm thời theo quy tắc nghiệp vụ. | Admin |
| IAM-FR-026 | Staff Management | Hệ thống phải cho phép Admin quản lý Staff ở mức nghiệp vụ, bao gồm tạo, cập nhật trạng thái, gán vai trò phù hợp hoặc thu hồi vai trò phù hợp nếu được phê duyệt. | Admin |
| IAM-FR-027 | Role Management | Hệ thống phải cho phép Admin quản lý role theo chính sách RBAC được phê duyệt. | Admin |
| IAM-FR-028 | Permission Management | Hệ thống phải cho phép Admin quản lý permission theo chính sách RBAC được phê duyệt. | Admin |
| IAM-FR-029 | Role Assignment | Hệ thống phải cho phép Admin gán role cho tài khoản phù hợp. | Admin |
| IAM-FR-030 | Role Revocation | Hệ thống phải cho phép Admin thu hồi role khỏi tài khoản phù hợp. | Admin |
| IAM-FR-031 | Permission Assignment | Hệ thống phải cho phép Admin gán permission cho role hoặc actor theo chính sách RBAC được phê duyệt. | Admin |
| IAM-FR-032 | Permission Revocation | Hệ thống phải cho phép Admin thu hồi permission khỏi role hoặc actor theo chính sách RBAC được phê duyệt. | Admin |
| IAM-FR-033 | Audit View | Hệ thống phải cho phép actor có quyền xem audit liên quan đến các hoạt động IAM được ghi nhận. | Admin, Staff nếu được cấp |
| IAM-FR-034 | Manage Audit Authorization | Hệ thống phải kiểm soát quyền truy cập vào hoạt động xem và quản lý audit theo permission. | Admin |
| IAM-FR-035 | System Configuration Authorization | Hệ thống phải kiểm tra authorization trước khi cho phép truy cập hoặc thay đổi cấu hình hệ thống thuộc phạm vi IAM. | Admin |
| IAM-FR-036 | View Log Authorization | Hệ thống phải cho phép Staff hoặc Admin xem log trong phạm vi quyền được cấp. | Staff, Admin |
| IAM-FR-037 | Activity History Access Authorization | Hệ thống phải kiểm soát quyền xem lịch sử hoạt động theo role và ownership dữ liệu. | User, Staff, Admin |
| IAM-FR-038 | Account Self-Protection | Hệ thống phải kiểm soát các hành động actor tự tác động đến tài khoản hoặc quyền của chính mình nếu hành động đó có thể làm mất quyền truy cập quản trị hoặc gây rủi ro vận hành. | Staff, Admin |
| IAM-FR-039 | Concurrent Session Awareness | Hệ thống phải có khả năng xử lý nghiệp vụ khi một tài khoản đăng nhập trên nhiều phiên hoặc nhiều thiết bị theo chính sách được phê duyệt. | User, Staff, Admin |
| IAM-FR-040 | Effective Permission Refresh | Hệ thống phải bảo đảm thay đổi role, permission hoặc trạng thái tài khoản có hiệu lực theo chính sách được phê duyệt đối với phiên đang hoạt động. | User, Staff, Admin |
| IAM-FR-041 | Account Status Display | Hệ thống phải cho phép actor có quyền xem trạng thái tài khoản liên quan như active, locked, deactivated hoặc trạng thái tương đương được phê duyệt. | Admin, Staff nếu được cấp |
| IAM-FR-042 | Sensitive Action Confirmation | Hệ thống phải yêu cầu xác nhận nghiệp vụ đối với hành động nhạy cảm như gán role, thu hồi role, khóa, mở khóa, deactivate hoặc reactivate. | Admin, Staff |
| IAM-FR-043 | IAM Audit Logging Requirement | Hệ thống phải ghi nhận các hoạt động IAM quan trọng để phục vụ audit ở mức nghiệp vụ. | System |
| IAM-FR-044 | Default Role Assignment | Hệ thống phải gán vai trò mặc định phù hợp cho tài khoản mới sau khi đăng ký thành công theo chính sách được phê duyệt. | System |
| IAM-FR-045 | Invalid Token Handling | Hệ thống phải xử lý trường hợp token hoặc thông tin phiên không hợp lệ bằng cách từ chối truy cập và yêu cầu xác thực lại nếu cần. | User, Staff, Admin |

### 4.2 Ghi chú phạm vi Functional Requirements

Các requirement liên quan đến permission truy cập những khu vực ngoài IAM chỉ xác định quyền truy cập ở mức kiểm soát danh tính. Ví dụ, permission "Manage Task" hoặc "View Reports" được xem như nhãn quyền trong RBAC, không phải mô tả nghiệp vụ của module tương ứng.

Các requirement về token được mô tả ở mức nghiệp vụ. Tài liệu không xác định loại token, thuật toán, format, thời lượng kỹ thuật hoặc cơ chế lưu trữ.

Các requirement về audit được mô tả ở mức khả năng nghiệp vụ. Tài liệu không xác định cấu trúc audit log, schema, nơi lưu trữ hoặc cơ chế đồng bộ.

## 5. Non-functional Requirements

### 5.1 Security

| NFR ID | Requirement | Description |
|---|---|---|
| IAM-NFR-SEC-001 | Confidentiality | Thông tin định danh, hồ sơ, trạng thái tài khoản và thông tin xác thực phải được bảo vệ khỏi truy cập trái phép. |
| IAM-NFR-SEC-002 | Authentication Protection | Quy trình đăng nhập, đổi mật khẩu và khôi phục mật khẩu phải bảo đảm người yêu cầu là chủ thể hợp lệ hoặc actor được ủy quyền. |
| IAM-NFR-SEC-003 | Authorization Enforcement | Mọi chức năng được bảo vệ phải kiểm tra quyền trước khi cho phép thực hiện. |
| IAM-NFR-SEC-004 | Least Privilege | Actor chỉ được cấp quyền tối thiểu cần thiết để thực hiện trách nhiệm nghiệp vụ. |
| IAM-NFR-SEC-005 | Account Lock Protection | Tài khoản bị khóa, vô hiệu hóa hoặc không hợp lệ không được đăng nhập hoặc tiếp tục thực hiện hành động được bảo vệ. |
| IAM-NFR-SEC-006 | Sensitive Action Control | Các hành động như gán role, thu hồi role, thay đổi permission, khóa hoặc mở khóa tài khoản phải được kiểm soát và audit. |
| IAM-NFR-SEC-007 | Session Security | Phiên làm việc không hợp lệ, hết hạn hoặc bị thu hồi phải bị từ chối truy cập. |
| IAM-NFR-SEC-008 | Credential Privacy | Hệ thống không được hiển thị thông tin xác thực nhạy cảm cho actor không có thẩm quyền. |

### 5.2 Availability

| NFR ID | Requirement | Description |
|---|---|---|
| IAM-NFR-AVL-001 | Login Availability | Chức năng đăng nhập phải có độ sẵn sàng phù hợp để không cản trở người dùng truy cập hệ thống. |
| IAM-NFR-AVL-002 | Authorization Availability | Kiểm tra quyền phải khả dụng khi người dùng truy cập chức năng được bảo vệ. |
| IAM-NFR-AVL-003 | Admin Availability | Chức năng quản trị IAM phải khả dụng cho Admin khi cần xử lý vấn đề tài khoản hoặc phân quyền. |
| IAM-NFR-AVL-004 | Recovery Availability | Quy trình khôi phục mật khẩu phải khả dụng ở mức phù hợp để người dùng có thể lấy lại quyền truy cập. |

### 5.3 Performance

| NFR ID | Requirement | Description |
|---|---|---|
| IAM-NFR-PER-001 | Login Response | Đăng nhập hợp lệ hoặc không hợp lệ phải được xử lý trong thời gian chấp nhận được theo tiêu chuẩn sản phẩm. |
| IAM-NFR-PER-002 | Permission Check Response | Kiểm tra permission không được gây chậm đáng kể cho trải nghiệm sử dụng chức năng được bảo vệ. |
| IAM-NFR-PER-003 | User Search Response | Tìm kiếm người dùng trong phạm vi quản trị phải trả kết quả trong thời gian phù hợp với nhu cầu vận hành. |
| IAM-NFR-PER-004 | Audit View Response | Việc xem audit trong phạm vi IAM phải đáp ứng thời gian phản hồi phù hợp với hoạt động kiểm tra và vận hành. |

### 5.4 Auditability

| NFR ID | Requirement | Description |
|---|---|---|
| IAM-NFR-AUD-001 | Sensitive Action Traceability | Các hành động nhạy cảm liên quan đến role, permission, trạng thái tài khoản và cấu hình IAM phải có khả năng truy vết. |
| IAM-NFR-AUD-002 | Actor Attribution | Audit phải cho phép xác định actor thực hiện hành động ở mức nghiệp vụ. |
| IAM-NFR-AUD-003 | Change Context | Audit nên phản ánh loại thay đổi, đối tượng bị tác động, thời điểm và lý do nếu chính sách yêu cầu. |
| IAM-NFR-AUD-004 | Audit Access Control | Chỉ actor có quyền mới được xem thông tin audit. |
| IAM-NFR-AUD-005 | Audit Integrity | Audit phải được bảo vệ khỏi sửa đổi trái phép ở mức yêu cầu nghiệp vụ. |

### 5.5 Usability

| NFR ID | Requirement | Description |
|---|---|---|
| IAM-NFR-USA-001 | Clear Error Communication | Thông báo lỗi đăng nhập, unauthorized hoặc forbidden phải rõ ràng nhưng không tiết lộ thông tin nhạy cảm. |
| IAM-NFR-USA-002 | Understandable Role Management | Admin phải có khả năng hiểu ý nghĩa role và permission trước khi gán hoặc thu hồi. |
| IAM-NFR-USA-003 | Profile Usability | Người dùng phải có thể xem và cập nhật thông tin hồ sơ của chính mình theo cách dễ hiểu. |
| IAM-NFR-USA-004 | Recovery Guidance | Quy trình khôi phục mật khẩu phải cung cấp hướng dẫn đủ rõ để người dùng thực hiện. |

### 5.6 Maintainability

| NFR ID | Requirement | Description |
|---|---|---|
| IAM-NFR-MNT-001 | RBAC Maintainability | Cấu trúc role và permission phải có khả năng được duy trì theo thay đổi chính sách nghiệp vụ. |
| IAM-NFR-MNT-002 | Rule Consistency | Các business rule IAM phải được quản lý nhất quán để tránh hành vi phân quyền không đồng bộ. |
| IAM-NFR-MNT-003 | Audit Review Support | Dữ liệu audit phải có khả năng được xem xét phục vụ điều tra nghiệp vụ hoặc vận hành. |
| IAM-NFR-MNT-004 | Configuration Governability | Cấu hình liên quan đến IAM phải có quy trình quản trị và quyền truy cập rõ ràng. |

### 5.7 Scalability

| NFR ID | Requirement | Description |
|---|---|---|
| IAM-NFR-SCL-001 | User Growth | Module IAM phải hỗ trợ tăng số lượng User, Staff và Admin theo nhu cầu phát triển sản phẩm. |
| IAM-NFR-SCL-002 | Role and Permission Growth | Mô hình RBAC phải hỗ trợ bổ sung role hoặc permission mới khi có capability mới được phê duyệt. |
| IAM-NFR-SCL-003 | Audit Volume Growth | Năng lực audit phải phù hợp với tăng trưởng hoạt động IAM theo thời gian. |
| IAM-NFR-SCL-004 | Session Growth | Quản lý phiên phải hỗ trợ tăng số lượng phiên hoạt động đồng thời theo mức sử dụng thực tế. |

### 5.8 Compliance

| NFR ID | Requirement | Description |
|---|---|---|
| IAM-NFR-CMP-001 | Personal Data Protection | Module IAM phải hỗ trợ nguyên tắc bảo vệ dữ liệu cá nhân ở mức nghiệp vụ. |
| IAM-NFR-CMP-002 | Access Accountability | Truy cập vào chức năng quản trị và dữ liệu nhạy cảm phải có trách nhiệm giải trình. |
| IAM-NFR-CMP-003 | User Rights Awareness | Các yêu cầu về trạng thái tài khoản, vô hiệu hóa và khôi phục cần được xem xét theo quyền người dùng nếu áp dụng. |
| IAM-NFR-CMP-004 | Policy Alignment | Chính sách mật khẩu, khóa tài khoản, audit và phân quyền cần được xác nhận phù hợp với môi trường triển khai. |

## 6. Business Rules

| Business Rule ID | Business Rule |
|---|---|
| IAM-BR-001 | Guest chỉ được truy cập landing page, đăng ký và đăng nhập. |
| IAM-BR-002 | Guest không được truy cập chức năng yêu cầu xác thực. |
| IAM-BR-003 | Tài khoản mới đăng ký phải được gán role mặc định theo chính sách được phê duyệt. |
| IAM-BR-004 | User chỉ được quản lý hồ sơ và quyền truy cập dữ liệu của chính mình trong phạm vi được cấp. |
| IAM-BR-005 | User không được quản lý User khác, Staff, Role, Permission, Audit hoặc System Configuration. |
| IAM-BR-006 | Staff chỉ được thực hiện quyền vận hành được cấp, bao gồm receive support ticket, manage ticket, support user, view log và temporary lock user. |
| IAM-BR-007 | Staff không được thay đổi Role hoặc Permission. |
| IAM-BR-008 | Staff không được deactivate hoặc reactivate tài khoản nếu không có permission được phê duyệt rõ ràng. |
| IAM-BR-009 | Staff không được tự khóa tài khoản của chính mình nếu hành động đó tạo rủi ro vận hành hoặc chưa có chính sách cho phép. |
| IAM-BR-010 | Admin có quyền quản trị User, Staff, Role, Permission, System Configuration, Audit và System Dashboard trong phạm vi LifeBalance. |
| IAM-BR-011 | Admin vẫn phải tuân thủ audit và business rule khi thực hiện hành động quản trị. |
| IAM-BR-012 | Tài khoản bị khóa không thể đăng nhập. |
| IAM-BR-013 | Tài khoản bị deactivate không thể đăng nhập hoặc thực hiện hành động được bảo vệ. |
| IAM-BR-014 | Tài khoản không hợp lệ hoặc không tồn tại không được đăng nhập. |
| IAM-BR-015 | Mật khẩu không hợp lệ phải dẫn đến đăng nhập thất bại. |
| IAM-BR-016 | Hệ thống không được tiết lộ chi tiết nhạy cảm khi đăng nhập thất bại nếu thông tin đó có thể hỗ trợ tấn công tài khoản. |
| IAM-BR-017 | Actor đã đăng nhập phải có session hợp lệ để truy cập chức năng được bảo vệ. |
| IAM-BR-018 | Token hoặc thông tin phiên không hợp lệ phải bị từ chối. |
| IAM-BR-019 | Token hết hạn phải yêu cầu xác thực lại hoặc xử lý theo chính sách phiên được phê duyệt. |
| IAM-BR-020 | Permission phải được kiểm tra trước khi actor thực hiện hành động được bảo vệ. |
| IAM-BR-021 | Role phải được kiểm tra trước khi xác định effective permission. |
| IAM-BR-022 | Nếu actor không có permission cần thiết, hệ thống phải từ chối bằng Forbidden Handling. |
| IAM-BR-023 | Nếu actor chưa xác thực, hệ thống phải từ chối bằng Unauthorized Handling. |
| IAM-BR-024 | Mọi thay đổi Role phải được audit. |
| IAM-BR-025 | Mọi thay đổi Permission phải được audit. |
| IAM-BR-026 | Mọi hành động khóa, mở khóa, deactivate hoặc reactivate tài khoản phải được audit. |
| IAM-BR-027 | Gán role phải được thực hiện bởi Admin hoặc actor có permission quản trị tương đương được phê duyệt. |
| IAM-BR-028 | Thu hồi role phải được thực hiện bởi Admin hoặc actor có permission quản trị tương đương được phê duyệt. |
| IAM-BR-029 | Gán permission phải được thực hiện bởi Admin hoặc actor có permission quản trị permission được phê duyệt. |
| IAM-BR-030 | Thu hồi permission phải được thực hiện bởi Admin hoặc actor có permission quản trị permission được phê duyệt. |
| IAM-BR-031 | Không được gán role hoặc permission không tồn tại hoặc không còn hiệu lực. |
| IAM-BR-032 | Role hoặc permission đang bị thu hồi phải không còn được sử dụng cho quyết định authorization sau khi thay đổi có hiệu lực theo chính sách. |
| IAM-BR-033 | Admin không nên tự thu hồi quyền Admin cuối cùng của hệ thống nếu điều đó làm hệ thống mất khả năng quản trị. |
| IAM-BR-034 | Hành động nhạy cảm cần có xác nhận nghiệp vụ trước khi hoàn tất. |
| IAM-BR-035 | Hồ sơ cá nhân chỉ được cập nhật bởi chủ tài khoản hoặc Admin trong phạm vi được phê duyệt. |
| IAM-BR-036 | Đổi mật khẩu yêu cầu actor đã xác thực và đáp ứng chính sách xác minh hiện tại. |
| IAM-BR-037 | Khôi phục mật khẩu yêu cầu xác minh danh tính theo chính sách được phê duyệt. |
| IAM-BR-038 | Một tài khoản đang bị khóa hoặc deactivate không được khôi phục quyền truy cập thông qua forgot password nếu chính sách không cho phép. |
| IAM-BR-039 | Search User và View User Detail chỉ được thực hiện bởi Admin hoặc actor được cấp quyền tương đương. |
| IAM-BR-040 | Staff có thể xem log nếu có permission View Log, nhưng không được xem audit quản trị nếu không được cấp quyền. |
| IAM-BR-041 | System Configuration chỉ được quản lý bởi Admin có permission phù hợp. |
| IAM-BR-042 | Thay đổi trạng thái tài khoản phải phản ánh trong quyết định access control theo chính sách hiệu lực được phê duyệt. |
| IAM-BR-043 | Actor không được sử dụng quyền đã bị thu hồi sau khi thay đổi quyền có hiệu lực. |
| IAM-BR-044 | Tài khoản Staff phải được quản lý bởi Admin. |
| IAM-BR-045 | Permission Matrix là nguồn tham chiếu nghiệp vụ cho quyền truy cập cấp cao của actor. |

## 7. Workflows

### 7.1 Register Workflow

#### Main Flow

1. Guest truy cập khu vực đăng ký.
2. Guest cung cấp thông tin đăng ký bắt buộc theo chính sách nghiệp vụ.
3. Hệ thống kiểm tra tính đầy đủ và hợp lệ của thông tin.
4. Hệ thống xác định thông tin đăng ký chưa vi phạm quy tắc tài khoản hiện có.
5. Hệ thống tạo tài khoản mới ở trạng thái hợp lệ theo chính sách được phê duyệt.
6. Hệ thống gán role mặc định cho tài khoản mới.
7. Hệ thống thông báo đăng ký thành công theo cách phù hợp.

#### Alternative Flow

- Nếu chính sách yêu cầu xác minh sau đăng ký, tài khoản có thể được đặt ở trạng thái chờ xác minh trước khi được đăng nhập đầy đủ.
- Nếu Guest đã có tài khoản, Guest có thể chuyển sang đăng nhập thay vì đăng ký mới.

#### Exception Flow

- Thông tin bắt buộc bị thiếu: hệ thống từ chối đăng ký và yêu cầu bổ sung.
- Thông tin không hợp lệ: hệ thống từ chối đăng ký và thông báo lỗi phù hợp.
- Thông tin định danh đã được sử dụng: hệ thống từ chối đăng ký theo chính sách chống trùng lặp.
- Chính sách đăng ký chưa được đáp ứng: hệ thống không tạo tài khoản.

### 7.2 Login Workflow

#### Main Flow

1. Actor truy cập chức năng đăng nhập.
2. Actor cung cấp thông tin định danh và thông tin xác thực.
3. Hệ thống kiểm tra thông tin đăng nhập.
4. Hệ thống kiểm tra trạng thái tài khoản.
5. Hệ thống xác định role và permission hiệu lực.
6. Hệ thống tạo hoặc xác nhận phiên làm việc hợp lệ ở mức nghiệp vụ.
7. Actor được truy cập khu vực phù hợp với role và permission.

#### Alternative Flow

- Nếu actor đã có phiên hợp lệ, hệ thống có thể tiếp tục phiên hoặc yêu cầu xác thực lại theo chính sách.
- Nếu tài khoản có nhiều role, hệ thống xác định effective permission theo chính sách RBAC được phê duyệt.

#### Exception Flow

- Sai thông tin đăng nhập: hệ thống từ chối đăng nhập.
- Tài khoản bị khóa: hệ thống từ chối đăng nhập.
- Tài khoản bị deactivate: hệ thống từ chối đăng nhập.
- Tài khoản không tồn tại: hệ thống từ chối đăng nhập theo thông báo an toàn.
- Trạng thái tài khoản không hợp lệ: hệ thống không cho phép truy cập.

### 7.3 Logout Workflow

#### Main Flow

1. Actor đã đăng nhập yêu cầu đăng xuất.
2. Hệ thống xác định phiên hiện tại.
3. Hệ thống kết thúc phiên hiện tại theo chính sách.
4. Hệ thống xác nhận actor đã đăng xuất.
5. Actor không còn được truy cập chức năng yêu cầu phiên đã đăng xuất.

#### Alternative Flow

- Nếu actor có nhiều phiên, hệ thống xử lý theo chính sách: chỉ đăng xuất phiên hiện tại hoặc toàn bộ phiên.

#### Exception Flow

- Phiên đã hết hạn trước khi đăng xuất: hệ thống xác nhận trạng thái không còn phiên hợp lệ.
- Phiên không hợp lệ: hệ thống xử lý như chưa đăng nhập.

### 7.4 Profile Update Workflow

#### Main Flow

1. Actor đã đăng nhập truy cập hồ sơ của chính mình.
2. Actor cập nhật thông tin được phép thay đổi.
3. Hệ thống kiểm tra quyền cập nhật và tính hợp lệ của thông tin.
4. Hệ thống lưu thay đổi hồ sơ ở mức nghiệp vụ.
5. Hệ thống thông báo cập nhật thành công.

#### Alternative Flow

- Admin cập nhật một số thông tin tài khoản người dùng trong phạm vi quản trị được phê duyệt.

#### Exception Flow

- Actor không có quyền cập nhật hồ sơ được yêu cầu: hệ thống từ chối.
- Thông tin cập nhật không hợp lệ: hệ thống yêu cầu sửa.
- Tài khoản đang bị khóa hoặc deactivate: hệ thống xử lý theo chính sách trạng thái tài khoản.

### 7.5 Role Assignment Workflow

#### Main Flow

1. Admin truy cập chức năng quản lý role assignment.
2. Admin chọn tài khoản mục tiêu.
3. Admin chọn role cần gán.
4. Hệ thống kiểm tra quyền của Admin.
5. Hệ thống kiểm tra trạng thái tài khoản mục tiêu và trạng thái role.
6. Admin xác nhận hành động nhạy cảm.
7. Hệ thống gán role cho tài khoản.
8. Hệ thống ghi nhận audit.
9. Hệ thống áp dụng thay đổi theo chính sách hiệu lực.

#### Alternative Flow

- Nếu tài khoản đã có role đó, hệ thống có thể không thực hiện thay đổi và thông báo trạng thái hiện tại.
- Nếu role yêu cầu điều kiện bổ sung, hệ thống yêu cầu Admin đáp ứng điều kiện trước khi gán.

#### Exception Flow

- Actor không phải Admin hoặc không có quyền quản lý role: hệ thống từ chối.
- Role không tồn tại hoặc không còn hiệu lực: hệ thống từ chối.
- Tài khoản mục tiêu không hợp lệ: hệ thống từ chối.
- Hành động có nguy cơ vi phạm rule tự bảo vệ quản trị: hệ thống chặn hoặc yêu cầu quy trình phê duyệt bổ sung.

### 7.6 Temporary Lock Workflow

#### Main Flow

1. Staff hoặc Admin truy cập chức năng khóa tạm thời tài khoản.
2. Actor chọn tài khoản User mục tiêu.
3. Actor cung cấp hoặc chọn lý do khóa theo chính sách nghiệp vụ nếu được yêu cầu.
4. Hệ thống kiểm tra permission Temporary Lock User.
5. Hệ thống kiểm tra tài khoản mục tiêu có thể bị khóa bởi actor hiện tại hay không.
6. Actor xác nhận hành động.
7. Hệ thống đặt tài khoản vào trạng thái khóa tạm thời.
8. Hệ thống ghi nhận audit.
9. Hệ thống áp dụng trạng thái khóa đối với đăng nhập và truy cập.

#### Alternative Flow

- Admin có thể khóa tài khoản Staff hoặc User nếu chính sách cho phép.
- Staff chỉ được khóa User trong phạm vi được cấp.

#### Exception Flow

- Actor không có permission: hệ thống từ chối.
- Staff cố khóa chính mình: hệ thống xử lý theo business rule.
- Tài khoản mục tiêu là Admin: hệ thống từ chối nếu Staff thực hiện hoặc nếu không có policy cho phép.
- Tài khoản đã bị khóa hoặc deactivate: hệ thống thông báo trạng thái hiện tại.

### 7.7 Unlock Workflow

#### Main Flow

1. Admin truy cập chức năng mở khóa.
2. Admin tìm và chọn tài khoản bị khóa.
3. Hệ thống kiểm tra quyền mở khóa của Admin.
4. Hệ thống kiểm tra trạng thái tài khoản mục tiêu.
5. Admin xác nhận hành động.
6. Hệ thống chuyển tài khoản khỏi trạng thái khóa tạm thời theo chính sách.
7. Hệ thống ghi nhận audit.
8. Tài khoản có thể đăng nhập lại nếu không bị ràng buộc bởi trạng thái khác.

#### Alternative Flow

- Nếu chính sách cho phép khóa tự hết hạn, hệ thống có thể mở khóa theo thời điểm kết thúc khóa mà không cần Admin thao tác thủ công.

#### Exception Flow

- Actor không có quyền mở khóa: hệ thống từ chối.
- Tài khoản không ở trạng thái khóa: hệ thống thông báo không có hành động cần thực hiện.
- Tài khoản đang deactivate: mở khóa không đồng nghĩa với reactivate.

### 7.8 Permission Update Workflow

#### Main Flow

1. Admin truy cập chức năng quản lý permission.
2. Admin chọn role hoặc đối tượng phù hợp cần cập nhật permission.
3. Admin chọn permission cần gán hoặc thu hồi.
4. Hệ thống kiểm tra quyền quản lý permission của Admin.
5. Hệ thống kiểm tra trạng thái role, permission và ràng buộc nghiệp vụ liên quan.
6. Admin xác nhận hành động nhạy cảm.
7. Hệ thống cập nhật permission theo yêu cầu.
8. Hệ thống ghi nhận audit.
9. Hệ thống áp dụng thay đổi theo chính sách hiệu lực.

#### Alternative Flow

- Nếu permission đã tồn tại trong role, hệ thống thông báo không có thay đổi.
- Nếu thu hồi permission có thể ảnh hưởng đến vận hành, hệ thống yêu cầu xác nhận bổ sung theo chính sách.

#### Exception Flow

- Actor không có quyền: hệ thống từ chối.
- Permission không tồn tại hoặc không còn hiệu lực: hệ thống từ chối.
- Việc thu hồi permission làm mất khả năng quản trị tối thiểu: hệ thống chặn hoặc yêu cầu quy trình phê duyệt bổ sung.

## 8. Use Case List

| Use Case ID | Use Case Name | Primary Actor | Summary |
|---|---|---|---|
| IAM-UC-001 | View Landing Page | Guest | Guest xem nội dung công khai trước khi đăng ký hoặc đăng nhập. |
| IAM-UC-002 | Register Account | Guest | Guest đăng ký tài khoản mới. |
| IAM-UC-003 | Login | User, Staff, Admin | Actor đăng nhập bằng thông tin hợp lệ. |
| IAM-UC-004 | Logout | User, Staff, Admin | Actor kết thúc phiên làm việc. |
| IAM-UC-005 | View Own Profile | User, Staff, Admin | Actor xem hồ sơ của chính mình. |
| IAM-UC-006 | Update Own Profile | User, Staff, Admin | Actor cập nhật hồ sơ của chính mình. |
| IAM-UC-007 | Change Password | User, Staff, Admin | Actor đổi mật khẩu khi đã đăng nhập. |
| IAM-UC-008 | Forgot Password | User, Staff, Admin | Actor yêu cầu khôi phục mật khẩu. |
| IAM-UC-009 | Validate Session and Token | System | Hệ thống kiểm tra phiên và token ở mức nghiệp vụ. |
| IAM-UC-010 | Validate Authorization | System | Hệ thống kiểm tra role và permission trước khi cho phép hành động. |
| IAM-UC-011 | Search User | Admin | Admin tìm kiếm tài khoản User hoặc Staff. |
| IAM-UC-012 | View User Detail | Admin | Admin xem chi tiết tài khoản trong phạm vi được phép. |
| IAM-UC-013 | Update User Account | Admin | Admin cập nhật thông tin hoặc trạng thái tài khoản theo quyền. |
| IAM-UC-014 | Deactivate User | Admin | Admin vô hiệu hóa tài khoản. |
| IAM-UC-015 | Reactivate User | Admin | Admin kích hoạt lại tài khoản. |
| IAM-UC-016 | Temporary Lock User | Staff, Admin | Staff hoặc Admin khóa tạm thời tài khoản theo phạm vi quyền. |
| IAM-UC-017 | Unlock User | Admin | Admin mở khóa tài khoản. |
| IAM-UC-018 | Manage Staff | Admin | Admin quản lý tài khoản Staff. |
| IAM-UC-019 | Manage Role | Admin | Admin quản lý role. |
| IAM-UC-020 | Assign Role | Admin | Admin gán role cho tài khoản. |
| IAM-UC-021 | Revoke Role | Admin | Admin thu hồi role khỏi tài khoản. |
| IAM-UC-022 | Manage Permission | Admin | Admin quản lý permission. |
| IAM-UC-023 | Assign Permission | Admin | Admin gán permission. |
| IAM-UC-024 | Revoke Permission | Admin | Admin thu hồi permission. |
| IAM-UC-025 | View Audit | Admin, Staff if authorized | Actor có quyền xem audit hoặc log trong phạm vi được cấp. |
| IAM-UC-026 | Authorize System Configuration | Admin | Hệ thống kiểm tra quyền trước khi Admin truy cập cấu hình. |
| IAM-UC-027 | Handle Unauthorized Access | System | Hệ thống xử lý truy cập chưa xác thực. |
| IAM-UC-028 | Handle Forbidden Access | System | Hệ thống xử lý truy cập không đủ quyền. |

## 9. Use Case Specification

### IAM-UC-001 - View Landing Page

| Field | Description |
|---|---|
| ID | IAM-UC-001 |
| Description | Guest xem nội dung công khai của LifeBalance trước khi đăng ký hoặc đăng nhập. |
| Primary Actor | Guest |
| Trigger | Guest truy cập điểm vào công khai của hệ thống. |
| Preconditions | Không yêu cầu xác thực. |
| Main Flow | 1. Guest truy cập landing page. 2. Hệ thống xác định nội dung được phép công khai. 3. Hệ thống cho phép Guest xem landing page. |
| Alternative Flow | Guest chọn đăng ký hoặc đăng nhập từ landing page. |
| Exception Flow | Nếu nội dung yêu cầu đăng nhập, hệ thống chuyển sang Unauthorized Handling. |
| Postconditions | Guest vẫn ở trạng thái chưa xác thực. |
| Business Rules | IAM-BR-001, IAM-BR-002 |

### IAM-UC-002 - Register Account

| Field | Description |
|---|---|
| ID | IAM-UC-002 |
| Description | Guest đăng ký tài khoản mới để trở thành User. |
| Primary Actor | Guest |
| Trigger | Guest chọn đăng ký. |
| Preconditions | Guest chưa đăng nhập; chính sách đăng ký cho phép tạo tài khoản mới. |
| Main Flow | 1. Guest cung cấp thông tin bắt buộc. 2. Hệ thống kiểm tra tính hợp lệ. 3. Hệ thống kiểm tra trùng lặp theo chính sách. 4. Hệ thống tạo tài khoản. 5. Hệ thống gán role mặc định. 6. Hệ thống thông báo đăng ký thành công. |
| Alternative Flow | Nếu chính sách yêu cầu xác minh, tài khoản được đặt ở trạng thái chờ xác minh. |
| Exception Flow | Thông tin thiếu, không hợp lệ hoặc đã tồn tại dẫn đến từ chối đăng ký. |
| Postconditions | Tài khoản được tạo theo trạng thái được phê duyệt; role mặc định được gán nếu đăng ký thành công. |
| Business Rules | IAM-BR-003, IAM-BR-031, IAM-BR-044 |

### IAM-UC-003 - Login

| Field | Description |
|---|---|
| ID | IAM-UC-003 |
| Description | Actor có tài khoản đăng nhập vào hệ thống. |
| Primary Actor | User, Staff, Admin |
| Trigger | Actor gửi yêu cầu đăng nhập. |
| Preconditions | Actor có tài khoản đã đăng ký; tài khoản không ở trạng thái cấm đăng nhập. |
| Main Flow | 1. Actor nhập thông tin đăng nhập. 2. Hệ thống kiểm tra thông tin xác thực. 3. Hệ thống kiểm tra trạng thái tài khoản. 4. Hệ thống xác định role và permission. 5. Hệ thống tạo phiên hợp lệ. 6. Actor được truy cập theo quyền. |
| Alternative Flow | Actor đã có phiên hợp lệ và được xử lý theo chính sách session. |
| Exception Flow | Sai mật khẩu, tài khoản bị khóa, tài khoản deactivate, tài khoản không tồn tại hoặc trạng thái không hợp lệ dẫn đến đăng nhập thất bại. |
| Postconditions | Actor có phiên hợp lệ nếu đăng nhập thành công. |
| Business Rules | IAM-BR-012, IAM-BR-013, IAM-BR-014, IAM-BR-015, IAM-BR-016, IAM-BR-017 |

### IAM-UC-004 - Logout

| Field | Description |
|---|---|
| ID | IAM-UC-004 |
| Description | Actor đã đăng nhập kết thúc phiên làm việc. |
| Primary Actor | User, Staff, Admin |
| Trigger | Actor chọn đăng xuất. |
| Preconditions | Actor có phiên đang hoạt động hoặc từng có phiên trong ngữ cảnh hiện tại. |
| Main Flow | 1. Actor yêu cầu logout. 2. Hệ thống xác định phiên hiện tại. 3. Hệ thống kết thúc phiên. 4. Hệ thống xác nhận logout. |
| Alternative Flow | Nếu có nhiều phiên, hệ thống xử lý theo chính sách logout được phê duyệt. |
| Exception Flow | Phiên đã hết hạn hoặc không hợp lệ được xử lý như không còn đăng nhập. |
| Postconditions | Phiên hiện tại không còn được sử dụng để truy cập chức năng bảo vệ. |
| Business Rules | IAM-BR-017, IAM-BR-018, IAM-BR-019 |

### IAM-UC-005 - View Own Profile

| Field | Description |
|---|---|
| ID | IAM-UC-005 |
| Description | Actor đã đăng nhập xem hồ sơ của chính mình. |
| Primary Actor | User, Staff, Admin |
| Trigger | Actor truy cập hồ sơ cá nhân. |
| Preconditions | Actor đã đăng nhập và có session hợp lệ. |
| Main Flow | 1. Actor yêu cầu xem hồ sơ. 2. Hệ thống kiểm tra session. 3. Hệ thống kiểm tra quyền xem hồ sơ. 4. Hệ thống hiển thị thông tin hồ sơ được phép xem. |
| Alternative Flow | Admin xem hồ sơ tài khoản khác thông qua use case View User Detail nếu có quyền. |
| Exception Flow | Session không hợp lệ hoặc actor không có quyền dẫn đến từ chối. |
| Postconditions | Không thay đổi dữ liệu hồ sơ. |
| Business Rules | IAM-BR-004, IAM-BR-017, IAM-BR-020 |

### IAM-UC-006 - Update Own Profile

| Field | Description |
|---|---|
| ID | IAM-UC-006 |
| Description | Actor cập nhật thông tin hồ sơ cá nhân trong phạm vi được phép. |
| Primary Actor | User, Staff, Admin |
| Trigger | Actor gửi yêu cầu cập nhật hồ sơ. |
| Preconditions | Actor đã đăng nhập, session hợp lệ và thông tin thuộc phạm vi được phép cập nhật. |
| Main Flow | 1. Actor nhập thông tin cập nhật. 2. Hệ thống kiểm tra quyền. 3. Hệ thống kiểm tra tính hợp lệ thông tin. 4. Hệ thống cập nhật hồ sơ. 5. Hệ thống thông báo thành công. |
| Alternative Flow | Actor hủy thao tác trước khi xác nhận. |
| Exception Flow | Thông tin không hợp lệ, session hết hạn hoặc actor cập nhật ngoài phạm vi dẫn đến từ chối. |
| Postconditions | Hồ sơ được cập nhật nếu thành công. |
| Business Rules | IAM-BR-004, IAM-BR-035 |

### IAM-UC-007 - Change Password

| Field | Description |
|---|---|
| ID | IAM-UC-007 |
| Description | Actor đã đăng nhập đổi mật khẩu. |
| Primary Actor | User, Staff, Admin |
| Trigger | Actor chọn đổi mật khẩu. |
| Preconditions | Actor đã đăng nhập; tài khoản đang ở trạng thái cho phép đổi mật khẩu. |
| Main Flow | 1. Actor cung cấp thông tin xác minh hiện tại. 2. Actor cung cấp mật khẩu mới. 3. Hệ thống kiểm tra chính sách mật khẩu. 4. Hệ thống cập nhật mật khẩu. 5. Hệ thống thông báo thành công. |
| Alternative Flow | Hệ thống yêu cầu xác thực lại nếu chính sách quy định. |
| Exception Flow | Xác minh hiện tại thất bại, mật khẩu mới không đạt chính sách hoặc session không hợp lệ dẫn đến từ chối. |
| Postconditions | Mật khẩu được thay đổi nếu thành công; các phiên liên quan được xử lý theo chính sách. |
| Business Rules | IAM-BR-036, IAM-BR-017 |

### IAM-UC-008 - Forgot Password

| Field | Description |
|---|---|
| ID | IAM-UC-008 |
| Description | Actor yêu cầu khôi phục mật khẩu khi không thể đăng nhập. |
| Primary Actor | User, Staff, Admin |
| Trigger | Actor chọn quên mật khẩu. |
| Preconditions | Tài khoản tồn tại và chính sách khôi phục cho phép. |
| Main Flow | 1. Actor cung cấp thông tin nhận diện tài khoản. 2. Hệ thống kiểm tra chính sách khôi phục. 3. Actor hoàn tất xác minh danh tính theo chính sách. 4. Actor đặt mật khẩu mới. 5. Hệ thống thông báo hoàn tất khôi phục. |
| Alternative Flow | Nếu tài khoản cần hỗ trợ vận hành, actor được hướng dẫn liên hệ Staff theo chính sách. |
| Exception Flow | Tài khoản bị khóa, deactivate hoặc không đủ điều kiện khôi phục dẫn đến từ chối theo chính sách. |
| Postconditions | Mật khẩu được cập nhật nếu khôi phục thành công. |
| Business Rules | IAM-BR-037, IAM-BR-038 |

### IAM-UC-009 - Validate Session and Token

| Field | Description |
|---|---|
| ID | IAM-UC-009 |
| Description | Hệ thống kiểm tra session và token ở mức nghiệp vụ trước khi cho phép truy cập. |
| Primary Actor | System |
| Trigger | Actor truy cập chức năng yêu cầu xác thực. |
| Preconditions | Actor cung cấp thông tin phiên hoặc token. |
| Main Flow | 1. Hệ thống nhận yêu cầu truy cập. 2. Hệ thống kiểm tra session hoặc token. 3. Hệ thống kiểm tra trạng thái tài khoản. 4. Hệ thống cho phép tiếp tục kiểm tra authorization nếu hợp lệ. |
| Alternative Flow | Nếu session gần hết hạn, hệ thống xử lý theo chính sách phiên. |
| Exception Flow | Token hết hạn, token không hợp lệ hoặc tài khoản không hợp lệ dẫn đến từ chối. |
| Postconditions | Chỉ session hợp lệ được tiếp tục xử lý. |
| Business Rules | IAM-BR-017, IAM-BR-018, IAM-BR-019, IAM-BR-042 |

### IAM-UC-010 - Validate Authorization

| Field | Description |
|---|---|
| ID | IAM-UC-010 |
| Description | Hệ thống kiểm tra role và permission trước khi cho phép hành động được bảo vệ. |
| Primary Actor | System |
| Trigger | Actor yêu cầu thực hiện hành động cần permission. |
| Preconditions | Actor đã xác thực và session hợp lệ. |
| Main Flow | 1. Hệ thống xác định hành động yêu cầu. 2. Hệ thống xác định permission cần thiết. 3. Hệ thống xác định role và effective permission của actor. 4. Hệ thống cho phép hành động nếu permission phù hợp. |
| Alternative Flow | Nếu nhiều role tồn tại, hệ thống xác định quyền hiệu lực theo chính sách RBAC. |
| Exception Flow | Không có permission dẫn đến Forbidden Handling. |
| Postconditions | Hành động được cho phép hoặc bị từ chối rõ ràng. |
| Business Rules | IAM-BR-020, IAM-BR-021, IAM-BR-022 |

### IAM-UC-011 - Search User

| Field | Description |
|---|---|
| ID | IAM-UC-011 |
| Description | Admin tìm kiếm tài khoản User hoặc Staff. |
| Primary Actor | Admin |
| Trigger | Admin nhập tiêu chí tìm kiếm. |
| Preconditions | Admin đã đăng nhập và có permission quản lý người dùng. |
| Main Flow | 1. Admin nhập tiêu chí. 2. Hệ thống kiểm tra quyền. 3. Hệ thống trả danh sách tài khoản phù hợp trong phạm vi được phép. |
| Alternative Flow | Không có kết quả phù hợp, hệ thống thông báo không tìm thấy. |
| Exception Flow | Actor không đủ quyền hoặc session không hợp lệ dẫn đến từ chối. |
| Postconditions | Không thay đổi trạng thái tài khoản. |
| Business Rules | IAM-BR-039, IAM-BR-020 |

### IAM-UC-012 - View User Detail

| Field | Description |
|---|---|
| ID | IAM-UC-012 |
| Description | Admin xem chi tiết tài khoản trong phạm vi được phép. |
| Primary Actor | Admin |
| Trigger | Admin chọn một tài khoản từ kết quả tìm kiếm hoặc danh sách quản trị. |
| Preconditions | Admin có permission xem chi tiết tài khoản. |
| Main Flow | 1. Admin chọn tài khoản. 2. Hệ thống kiểm tra quyền. 3. Hệ thống hiển thị thông tin tài khoản được phép xem. |
| Alternative Flow | Nếu tài khoản không còn tồn tại hoặc không còn trong phạm vi, hệ thống thông báo. |
| Exception Flow | Actor không đủ quyền dẫn đến Forbidden Handling. |
| Postconditions | Không thay đổi dữ liệu. |
| Business Rules | IAM-BR-039, IAM-BR-040 |

### IAM-UC-013 - Update User Account

| Field | Description |
|---|---|
| ID | IAM-UC-013 |
| Description | Admin cập nhật thông tin tài khoản trong phạm vi quản trị. |
| Primary Actor | Admin |
| Trigger | Admin yêu cầu cập nhật tài khoản. |
| Preconditions | Admin có permission quản lý tài khoản; tài khoản mục tiêu hợp lệ. |
| Main Flow | 1. Admin chọn tài khoản. 2. Admin nhập thay đổi. 3. Hệ thống kiểm tra quyền và rule. 4. Admin xác nhận nếu thay đổi nhạy cảm. 5. Hệ thống cập nhật. 6. Hệ thống audit nếu cần. |
| Alternative Flow | Admin hủy thao tác trước khi xác nhận. |
| Exception Flow | Thay đổi vượt quyền, vi phạm rule hoặc tài khoản không hợp lệ dẫn đến từ chối. |
| Postconditions | Tài khoản được cập nhật nếu thành công. |
| Business Rules | IAM-BR-010, IAM-BR-034, IAM-BR-035 |

### IAM-UC-014 - Deactivate User

| Field | Description |
|---|---|
| ID | IAM-UC-014 |
| Description | Admin vô hiệu hóa tài khoản. |
| Primary Actor | Admin |
| Trigger | Admin chọn deactivate tài khoản. |
| Preconditions | Admin có quyền; tài khoản mục tiêu đang ở trạng thái có thể deactivate. |
| Main Flow | 1. Admin chọn tài khoản. 2. Hệ thống kiểm tra quyền. 3. Admin cung cấp hoặc chọn lý do nếu chính sách yêu cầu. 4. Admin xác nhận. 5. Hệ thống deactivate tài khoản. 6. Hệ thống audit. |
| Alternative Flow | Tài khoản đã deactivate, hệ thống thông báo trạng thái hiện tại. |
| Exception Flow | Deactivate vi phạm rule tự bảo vệ quản trị hoặc actor không đủ quyền. |
| Postconditions | Tài khoản không thể đăng nhập hoặc thực hiện hành động được bảo vệ. |
| Business Rules | IAM-BR-013, IAM-BR-026, IAM-BR-034 |

### IAM-UC-015 - Reactivate User

| Field | Description |
|---|---|
| ID | IAM-UC-015 |
| Description | Admin kích hoạt lại tài khoản đã deactivate. |
| Primary Actor | Admin |
| Trigger | Admin chọn reactivate tài khoản. |
| Preconditions | Tài khoản đang deactivate và đủ điều kiện reactivate. |
| Main Flow | 1. Admin chọn tài khoản. 2. Hệ thống kiểm tra quyền. 3. Hệ thống kiểm tra điều kiện reactivate. 4. Admin xác nhận. 5. Hệ thống reactivate tài khoản. 6. Hệ thống audit. |
| Alternative Flow | Nếu tài khoản còn bị khóa tạm thời, reactivate không tự động unlock trừ khi chính sách cho phép. |
| Exception Flow | Tài khoản không đủ điều kiện hoặc actor không đủ quyền dẫn đến từ chối. |
| Postconditions | Tài khoản có thể đăng nhập nếu không bị ràng buộc bởi trạng thái khác. |
| Business Rules | IAM-BR-026, IAM-BR-034, IAM-BR-042 |

### IAM-UC-016 - Temporary Lock User

| Field | Description |
|---|---|
| ID | IAM-UC-016 |
| Description | Staff hoặc Admin khóa tạm thời tài khoản theo quyền được cấp. |
| Primary Actor | Staff, Admin |
| Trigger | Actor chọn khóa tạm thời tài khoản. |
| Preconditions | Actor có permission Temporary Lock User; tài khoản mục tiêu đủ điều kiện bị khóa. |
| Main Flow | 1. Actor chọn tài khoản. 2. Hệ thống kiểm tra permission. 3. Actor cung cấp lý do nếu cần. 4. Actor xác nhận. 5. Hệ thống khóa tài khoản. 6. Hệ thống audit. |
| Alternative Flow | Admin có phạm vi khóa rộng hơn Staff nếu chính sách cho phép. |
| Exception Flow | Staff tự khóa chính mình, khóa Admin hoặc khóa ngoài phạm vi sẽ bị từ chối theo rule. |
| Postconditions | Tài khoản bị khóa không thể đăng nhập. |
| Business Rules | IAM-BR-006, IAM-BR-009, IAM-BR-012, IAM-BR-026 |

### IAM-UC-017 - Unlock User

| Field | Description |
|---|---|
| ID | IAM-UC-017 |
| Description | Admin mở khóa tài khoản bị khóa tạm thời. |
| Primary Actor | Admin |
| Trigger | Admin chọn unlock tài khoản. |
| Preconditions | Admin có quyền; tài khoản đang bị khóa. |
| Main Flow | 1. Admin chọn tài khoản. 2. Hệ thống kiểm tra quyền. 3. Admin xác nhận unlock. 4. Hệ thống mở khóa. 5. Hệ thống audit. |
| Alternative Flow | Khóa tạm thời tự hết hạn nếu chính sách cho phép. |
| Exception Flow | Tài khoản không bị khóa hoặc actor không đủ quyền dẫn đến từ chối. |
| Postconditions | Tài khoản không còn trạng thái locked nếu không có trạng thái cấm truy cập khác. |
| Business Rules | IAM-BR-026, IAM-BR-034, IAM-BR-042 |

### IAM-UC-018 - Manage Staff

| Field | Description |
|---|---|
| ID | IAM-UC-018 |
| Description | Admin quản lý tài khoản Staff theo phạm vi được phê duyệt. |
| Primary Actor | Admin |
| Trigger | Admin truy cập chức năng quản lý Staff. |
| Preconditions | Admin có permission Manage Staff. |
| Main Flow | 1. Admin tìm hoặc chọn Staff. 2. Hệ thống kiểm tra quyền. 3. Admin thực hiện thay đổi được phép. 4. Hệ thống xác nhận và audit nếu thay đổi nhạy cảm. |
| Alternative Flow | Admin hủy thao tác. |
| Exception Flow | Thay đổi vi phạm rule RBAC hoặc self-protection dẫn đến từ chối. |
| Postconditions | Thông tin hoặc trạng thái Staff được cập nhật nếu thành công. |
| Business Rules | IAM-BR-010, IAM-BR-044 |

### IAM-UC-019 - Manage Role

| Field | Description |
|---|---|
| ID | IAM-UC-019 |
| Description | Admin quản lý role trong mô hình RBAC. |
| Primary Actor | Admin |
| Trigger | Admin truy cập quản lý role. |
| Preconditions | Admin có permission Manage Role. |
| Main Flow | 1. Admin xem danh sách role. 2. Admin tạo, cập nhật hoặc thay đổi trạng thái role theo chính sách. 3. Hệ thống kiểm tra rule. 4. Hệ thống ghi nhận thay đổi và audit. |
| Alternative Flow | Admin chỉ xem role mà không thay đổi. |
| Exception Flow | Thay đổi làm mất khả năng quản trị tối thiểu hoặc vi phạm rule dẫn đến từ chối. |
| Postconditions | Role được quản lý theo thay đổi hợp lệ. |
| Business Rules | IAM-BR-024, IAM-BR-027, IAM-BR-031, IAM-BR-033 |

### IAM-UC-020 - Assign Role

| Field | Description |
|---|---|
| ID | IAM-UC-020 |
| Description | Admin gán role cho tài khoản. |
| Primary Actor | Admin |
| Trigger | Admin chọn gán role. |
| Preconditions | Admin có quyền; role và tài khoản mục tiêu hợp lệ. |
| Main Flow | 1. Admin chọn tài khoản. 2. Admin chọn role. 3. Hệ thống kiểm tra quyền và rule. 4. Admin xác nhận. 5. Hệ thống gán role. 6. Hệ thống audit. |
| Alternative Flow | Tài khoản đã có role, hệ thống thông báo không có thay đổi. |
| Exception Flow | Role không hợp lệ hoặc actor không đủ quyền dẫn đến từ chối. |
| Postconditions | Effective role của tài khoản được cập nhật theo chính sách hiệu lực. |
| Business Rules | IAM-BR-024, IAM-BR-027, IAM-BR-031, IAM-BR-042 |

### IAM-UC-021 - Revoke Role

| Field | Description |
|---|---|
| ID | IAM-UC-021 |
| Description | Admin thu hồi role khỏi tài khoản. |
| Primary Actor | Admin |
| Trigger | Admin chọn thu hồi role. |
| Preconditions | Admin có quyền; tài khoản đang có role cần thu hồi. |
| Main Flow | 1. Admin chọn tài khoản. 2. Admin chọn role cần thu hồi. 3. Hệ thống kiểm tra rule. 4. Admin xác nhận. 5. Hệ thống thu hồi role. 6. Hệ thống audit. |
| Alternative Flow | Nếu tài khoản không có role đó, hệ thống thông báo không có thay đổi. |
| Exception Flow | Thu hồi role làm mất quyền Admin cuối cùng hoặc vi phạm rule dẫn đến chặn hoặc yêu cầu xử lý bổ sung. |
| Postconditions | Role bị thu hồi không còn hiệu lực theo chính sách. |
| Business Rules | IAM-BR-024, IAM-BR-028, IAM-BR-032, IAM-BR-033, IAM-BR-043 |

### IAM-UC-022 - Manage Permission

| Field | Description |
|---|---|
| ID | IAM-UC-022 |
| Description | Admin quản lý permission trong mô hình RBAC. |
| Primary Actor | Admin |
| Trigger | Admin truy cập quản lý permission. |
| Preconditions | Admin có permission Manage Permission. |
| Main Flow | 1. Admin xem permission. 2. Admin tạo, cập nhật hoặc thay đổi trạng thái permission theo chính sách. 3. Hệ thống kiểm tra rule. 4. Hệ thống lưu thay đổi và audit. |
| Alternative Flow | Admin chỉ xem permission mà không thay đổi. |
| Exception Flow | Thay đổi vi phạm rule hoặc làm mất khả năng vận hành tối thiểu dẫn đến từ chối. |
| Postconditions | Permission được quản lý theo thay đổi hợp lệ. |
| Business Rules | IAM-BR-025, IAM-BR-029, IAM-BR-031 |

### IAM-UC-023 - Assign Permission

| Field | Description |
|---|---|
| ID | IAM-UC-023 |
| Description | Admin gán permission cho role hoặc đối tượng được chính sách cho phép. |
| Primary Actor | Admin |
| Trigger | Admin chọn gán permission. |
| Preconditions | Admin có quyền; permission và role mục tiêu hợp lệ. |
| Main Flow | 1. Admin chọn role hoặc đối tượng. 2. Admin chọn permission. 3. Hệ thống kiểm tra quyền và ràng buộc. 4. Admin xác nhận. 5. Hệ thống gán permission. 6. Hệ thống audit. |
| Alternative Flow | Permission đã tồn tại, hệ thống thông báo không có thay đổi. |
| Exception Flow | Permission không hợp lệ hoặc actor không đủ quyền dẫn đến từ chối. |
| Postconditions | Permission được bổ sung vào quyền hiệu lực theo chính sách. |
| Business Rules | IAM-BR-025, IAM-BR-029, IAM-BR-031 |

### IAM-UC-024 - Revoke Permission

| Field | Description |
|---|---|
| ID | IAM-UC-024 |
| Description | Admin thu hồi permission khỏi role hoặc đối tượng được chính sách cho phép. |
| Primary Actor | Admin |
| Trigger | Admin chọn thu hồi permission. |
| Preconditions | Admin có quyền; permission đang được gán. |
| Main Flow | 1. Admin chọn role hoặc đối tượng. 2. Admin chọn permission cần thu hồi. 3. Hệ thống kiểm tra ràng buộc. 4. Admin xác nhận. 5. Hệ thống thu hồi permission. 6. Hệ thống audit. |
| Alternative Flow | Permission không tồn tại trên role, hệ thống thông báo không có thay đổi. |
| Exception Flow | Thu hồi permission làm mất khả năng quản trị tối thiểu hoặc vi phạm rule dẫn đến từ chối. |
| Postconditions | Permission bị thu hồi không còn hiệu lực theo chính sách. |
| Business Rules | IAM-BR-025, IAM-BR-030, IAM-BR-032, IAM-BR-043 |

### IAM-UC-025 - View Audit

| Field | Description |
|---|---|
| ID | IAM-UC-025 |
| Description | Actor có quyền xem audit hoặc log liên quan đến IAM trong phạm vi được cấp. |
| Primary Actor | Admin, Staff if authorized |
| Trigger | Actor truy cập audit hoặc log view. |
| Preconditions | Actor đã đăng nhập và có permission phù hợp. |
| Main Flow | 1. Actor yêu cầu xem audit hoặc log. 2. Hệ thống kiểm tra quyền. 3. Hệ thống hiển thị thông tin trong phạm vi được phép. |
| Alternative Flow | Staff chỉ xem log nếu có permission View Log. |
| Exception Flow | Không đủ quyền dẫn đến Forbidden Handling. |
| Postconditions | Không thay đổi dữ liệu audit. |
| Business Rules | IAM-BR-040, IAM-BR-024, IAM-BR-025, IAM-BR-026 |

### IAM-UC-026 - Authorize System Configuration

| Field | Description |
|---|---|
| ID | IAM-UC-026 |
| Description | Hệ thống kiểm tra quyền trước khi cho phép truy cập hoặc thay đổi cấu hình hệ thống thuộc phạm vi IAM. |
| Primary Actor | Admin |
| Trigger | Admin yêu cầu xem hoặc thay đổi cấu hình. |
| Preconditions | Admin đã đăng nhập và có permission Manage System Configuration. |
| Main Flow | 1. Admin yêu cầu truy cập cấu hình. 2. Hệ thống kiểm tra permission. 3. Hệ thống cho phép truy cập nếu hợp lệ. 4. Hệ thống audit thay đổi nếu có. |
| Alternative Flow | Admin chỉ xem cấu hình mà không thay đổi. |
| Exception Flow | Không đủ quyền hoặc session không hợp lệ dẫn đến từ chối. |
| Postconditions | Cấu hình được truy cập hoặc thay đổi theo quyền. |
| Business Rules | IAM-BR-041, IAM-BR-034 |

### IAM-UC-027 - Handle Unauthorized Access

| Field | Description |
|---|---|
| ID | IAM-UC-027 |
| Description | Hệ thống xử lý actor chưa xác thực truy cập chức năng yêu cầu đăng nhập. |
| Primary Actor | System |
| Trigger | Guest hoặc session không hợp lệ truy cập khu vực bảo vệ. |
| Preconditions | Chức năng yêu cầu xác thực. |
| Main Flow | 1. Hệ thống phát hiện actor chưa xác thực. 2. Hệ thống từ chối truy cập. 3. Hệ thống hướng actor đến đăng nhập hoặc thông báo phù hợp. |
| Alternative Flow | Nếu actor có thể tiếp tục với chức năng công khai, hệ thống chỉ hiển thị nội dung công khai. |
| Exception Flow | Không áp dụng. |
| Postconditions | Chức năng bảo vệ không được truy cập. |
| Business Rules | IAM-BR-002, IAM-BR-023 |

### IAM-UC-028 - Handle Forbidden Access

| Field | Description |
|---|---|
| ID | IAM-UC-028 |
| Description | Hệ thống xử lý actor đã xác thực nhưng không đủ permission. |
| Primary Actor | System |
| Trigger | Actor yêu cầu hành động ngoài quyền được cấp. |
| Preconditions | Actor đã xác thực nhưng thiếu permission. |
| Main Flow | 1. Hệ thống kiểm tra authorization. 2. Hệ thống xác định actor thiếu permission. 3. Hệ thống từ chối hành động. 4. Hệ thống hiển thị thông báo phù hợp. |
| Alternative Flow | Actor có thể quay lại khu vực được phép truy cập. |
| Exception Flow | Nếu session cũng không hợp lệ, hệ thống xử lý theo Unauthorized Handling. |
| Postconditions | Hành động không được thực hiện. |
| Business Rules | IAM-BR-020, IAM-BR-022 |

## 10. User Stories

| Story ID | User Story |
|---|---|
| IAM-US-001 | As a Guest, I want to view the landing page so that I can understand the purpose of LifeBalance before registering. |
| IAM-US-002 | As a Guest, I want to register an account so that I can become a LifeBalance user. |
| IAM-US-003 | As a registered actor, I want to log in so that I can access functions permitted to my role. |
| IAM-US-004 | As a logged-in actor, I want to log out so that I can end my session securely. |
| IAM-US-005 | As a User, I want to view my profile so that I can confirm my identity information. |
| IAM-US-006 | As a User, I want to update my profile so that my account information remains accurate. |
| IAM-US-007 | As a logged-in actor, I want to change my password so that I can maintain account security. |
| IAM-US-008 | As an account owner, I want to recover my password so that I can regain access when I forget it. |
| IAM-US-009 | As the system, I want to validate sessions so that only authenticated actors can access protected areas. |
| IAM-US-010 | As the system, I want to validate permissions so that actors can only perform authorized actions. |
| IAM-US-011 | As an Admin, I want to search users so that I can locate accounts for support or governance purposes. |
| IAM-US-012 | As an Admin, I want to view user details so that I can understand account status and assigned roles. |
| IAM-US-013 | As an Admin, I want to update user accounts so that account information and status remain governed. |
| IAM-US-014 | As an Admin, I want to deactivate a user so that an account can be prevented from accessing the system when required. |
| IAM-US-015 | As an Admin, I want to reactivate a user so that access can be restored when conditions are met. |
| IAM-US-016 | As a Staff member, I want to temporarily lock a user so that urgent operational risks can be controlled. |
| IAM-US-017 | As an Admin, I want to unlock a user so that access can be restored after review. |
| IAM-US-018 | As an Admin, I want to manage Staff so that operational roles are controlled. |
| IAM-US-019 | As an Admin, I want to manage roles so that access responsibilities can be governed consistently. |
| IAM-US-020 | As an Admin, I want to assign roles so that actors receive appropriate permissions. |
| IAM-US-021 | As an Admin, I want to revoke roles so that actors no longer retain inappropriate access. |
| IAM-US-022 | As an Admin, I want to manage permissions so that access rights remain aligned with business policy. |
| IAM-US-023 | As an Admin, I want to assign permissions so that roles can support approved responsibilities. |
| IAM-US-024 | As an Admin, I want to revoke permissions so that excessive access can be removed. |
| IAM-US-025 | As an Admin, I want to view audit so that IAM changes can be reviewed and traced. |
| IAM-US-026 | As a Staff member, I want to view permitted logs so that I can support users within my operational scope. |
| IAM-US-027 | As the system, I want to reject unauthorized access so that protected functions are not exposed to Guests. |
| IAM-US-028 | As the system, I want to reject forbidden actions so that authenticated actors cannot exceed their permissions. |

## 11. Acceptance Criteria

### 11.1 Registration and Login

| AC ID | Given | When | Then |
|---|---|---|---|
| IAM-AC-001 | Given a Guest is on a public entry point | When the Guest accesses the landing page | Then the system allows access without authentication. |
| IAM-AC-002 | Given a Guest provides all required registration information | When the information is valid and not duplicated | Then the system creates an account according to registration policy. |
| IAM-AC-003 | Given a Guest submits incomplete registration information | When the Guest attempts to register | Then the system rejects the registration and identifies missing required information in a safe manner. |
| IAM-AC-004 | Given a Guest submits invalid registration information | When the Guest attempts to register | Then the system rejects the registration. |
| IAM-AC-005 | Given account registration succeeds | When the account is created | Then the system assigns the default role according to approved policy. |
| IAM-AC-006 | Given an actor has a valid active account | When the actor provides correct login credentials | Then the system authenticates the actor and establishes a valid session. |
| IAM-AC-007 | Given an actor provides an incorrect password | When the actor attempts to log in | Then the system rejects the login. |
| IAM-AC-008 | Given an account is locked | When the account owner attempts to log in | Then the system rejects the login. |
| IAM-AC-009 | Given an account is deactivated | When the account owner attempts to log in | Then the system rejects the login. |
| IAM-AC-010 | Given the account does not exist or is not valid | When login is attempted | Then the system rejects the login without exposing sensitive details. |

### 11.2 Session, Token and Logout

| AC ID | Given | When | Then |
|---|---|---|---|
| IAM-AC-011 | Given an actor has a valid session | When the actor accesses a protected function | Then the system proceeds to authorization validation. |
| IAM-AC-012 | Given an actor has an expired session | When the actor accesses a protected function | Then the system rejects access and requires re-authentication according to policy. |
| IAM-AC-013 | Given an actor presents an invalid token | When the actor accesses a protected function | Then the system denies access. |
| IAM-AC-014 | Given an actor is logged in | When the actor logs out | Then the current session is ended according to policy. |
| IAM-AC-015 | Given an actor has logged out | When the actor attempts to reuse the ended session | Then the system denies access. |
| IAM-AC-016 | Given an account status changes to locked or deactivated | When an active session attempts a protected action | Then the system handles access according to the approved effective status policy. |

### 11.3 Profile and Password

| AC ID | Given | When | Then |
|---|---|---|---|
| IAM-AC-017 | Given an actor is logged in | When the actor requests to view their own profile | Then the system displays permitted profile information. |
| IAM-AC-018 | Given an actor is logged in | When the actor updates permitted profile fields with valid information | Then the system updates the profile. |
| IAM-AC-019 | Given an actor attempts to update restricted profile information | When the update is submitted | Then the system rejects the update. |
| IAM-AC-020 | Given an actor is logged in | When the actor changes password and satisfies verification and policy | Then the system changes the password. |
| IAM-AC-021 | Given password change verification fails | When the actor submits the change request | Then the system rejects the password change. |
| IAM-AC-022 | Given an account owner requests password recovery | When the owner completes approved identity verification | Then the system allows setting a new password. |
| IAM-AC-023 | Given an account is not eligible for password recovery | When recovery is attempted | Then the system rejects recovery according to policy. |

### 11.4 Authorization and Access Control

| AC ID | Given | When | Then |
|---|---|---|---|
| IAM-AC-024 | Given a Guest accesses a protected function | When authentication is required | Then the system handles the request as unauthorized. |
| IAM-AC-025 | Given an authenticated actor lacks required permission | When the actor attempts a protected action | Then the system handles the request as forbidden. |
| IAM-AC-026 | Given an authenticated actor has required permission | When the actor attempts a protected action | Then the system allows the action to proceed. |
| IAM-AC-027 | Given a permission has been revoked and is effective | When the actor attempts the related action | Then the system denies the action. |
| IAM-AC-028 | Given a role has been revoked and is effective | When the actor attempts an action dependent on that role | Then the system denies the action if no other valid permission exists. |

### 11.5 User, Staff, Role and Permission Administration

| AC ID | Given | When | Then |
|---|---|---|---|
| IAM-AC-029 | Given an Admin has Manage User permission | When the Admin searches users | Then the system returns matching accounts within allowed scope. |
| IAM-AC-030 | Given an Admin selects a user account | When the Admin views details | Then the system displays permitted account details. |
| IAM-AC-031 | Given an Admin updates a user account within scope | When the update is valid | Then the system applies the update and audits if required. |
| IAM-AC-032 | Given an Admin deactivates an eligible account | When the Admin confirms | Then the account becomes deactivated and the action is audited. |
| IAM-AC-033 | Given an Admin reactivates an eligible account | When the Admin confirms | Then the account becomes active according to policy and the action is audited. |
| IAM-AC-034 | Given Staff has Temporary Lock User permission | When Staff locks an eligible User | Then the account becomes temporarily locked and the action is audited. |
| IAM-AC-035 | Given Staff attempts to lock an account outside permitted scope | When the action is submitted | Then the system rejects the action. |
| IAM-AC-036 | Given an Admin unlocks a locked account | When the Admin confirms | Then the account is unlocked if no other restriction applies and the action is audited. |
| IAM-AC-037 | Given an Admin assigns a valid role to an eligible account | When the Admin confirms | Then the role is assigned and the action is audited. |
| IAM-AC-038 | Given an Admin revokes a role from an account | When the Admin confirms and no rule is violated | Then the role is revoked and the action is audited. |
| IAM-AC-039 | Given an Admin assigns a permission according to policy | When the Admin confirms | Then the permission is assigned and the action is audited. |
| IAM-AC-040 | Given an Admin revokes a permission according to policy | When the Admin confirms | Then the permission is revoked and the action is audited. |
| IAM-AC-041 | Given a non-Admin attempts to manage roles | When the action is submitted | Then the system rejects the action. |
| IAM-AC-042 | Given a non-Admin attempts to manage permissions | When the action is submitted | Then the system rejects the action. |

### 11.6 Audit and Configuration Authorization

| AC ID | Given | When | Then |
|---|---|---|---|
| IAM-AC-043 | Given an Admin has Manage Audit permission | When the Admin views audit | Then the system displays audit information within allowed scope. |
| IAM-AC-044 | Given Staff has only View Log permission | When Staff accesses permitted logs | Then the system allows log access within scope. |
| IAM-AC-045 | Given Staff lacks Manage Audit permission | When Staff accesses restricted audit | Then the system denies access. |
| IAM-AC-046 | Given an Admin has Manage System Configuration permission | When the Admin accesses IAM-related configuration | Then the system allows access. |
| IAM-AC-047 | Given an actor lacks configuration permission | When the actor attempts configuration access | Then the system denies access. |
| IAM-AC-048 | Given a sensitive IAM action occurs | When the action completes | Then the system records audit information according to audit policy. |

## 12. Business Scenarios

### 12.1 Successful Login

A User enters valid login credentials for an active account. The system validates the credentials, confirms the account is active, determines the User role, establishes a valid session and allows access to functions covered by User permissions.

Expected result: login succeeds, session is valid, and access is limited to User permissions.

### 12.2 Wrong Password

A registered actor enters a valid account identifier but an incorrect password. The system rejects the login. The message must not expose sensitive details that would help determine whether the account exists or which part of the credential failed beyond what policy allows.

Expected result: login fails and no session is created.

### 12.3 Locked Account

A User account has been temporarily locked by Staff or Admin. The User attempts to log in. The system checks account status and rejects login because locked accounts cannot access the system.

Expected result: login fails until the account is unlocked or the lock expires according to policy.

### 12.4 Unauthorized Access

A Guest attempts to access a protected function requiring authentication. The system detects that no valid session exists and handles the request as unauthorized.

Expected result: access is denied and Guest is directed to authenticate or shown an appropriate message.

### 12.5 Permission Denied

A User is logged in and attempts to access a function restricted to Admin. The system validates the session, checks permission, determines that the User lacks required permission and denies the action as forbidden.

Expected result: action is not performed and forbidden handling is applied.

### 12.6 Assign Role

An Admin selects a valid User account and assigns a Staff role. The system confirms Admin permission, validates the target account and role, requests confirmation for the sensitive action, applies the assignment and records audit.

Expected result: the target account receives the assigned role according to effective permission policy.

### 12.7 Remove Role

An Admin removes a role from a Staff account. The system validates that the Admin has permission, checks whether removing the role violates governance rules, requests confirmation, revokes the role and records audit.

Expected result: the role is removed if no rule is violated.

### 12.8 Temporary Lock

Staff receives a support-related reason to temporarily lock a User account. Staff selects the User, provides a reason if required, confirms the lock, and the system applies temporary lock if Staff has permission and scope.

Expected result: the User account cannot log in while locked, and the lock action is audited.

### 12.9 Unlock

Admin reviews a locked account and decides to restore access. Admin selects the account, confirms unlock and the system removes the temporary lock if no other access restriction exists.

Expected result: the account can log in if active and otherwise eligible.

### 12.10 Update Profile

A User logs in and updates permitted profile information. The system checks session and profile update permission, validates the submitted information and updates the profile.

Expected result: profile is updated for permitted fields only.

## 13. Edge Cases

| Edge Case ID | Scenario | Expected Business Handling |
|---|---|---|
| IAM-EC-001 | User bị khóa giữa phiên làm việc. | Hệ thống phải áp dụng chính sách account status validation cho phiên đang hoạt động; hành động tiếp theo có thể bị từ chối. |
| IAM-EC-002 | Role bị thu hồi khi actor đang đăng nhập. | Effective permission phải được cập nhật theo chính sách hiệu lực; actor không được tiếp tục dùng quyền đã bị thu hồi sau khi thay đổi có hiệu lực. |
| IAM-EC-003 | Permission bị thu hồi khi actor đang thực hiện thao tác. | Nếu thao tác chưa hoàn tất và permission đã hết hiệu lực, hệ thống phải từ chối hoặc yêu cầu thực hiện lại theo quyền hiện tại. |
| IAM-EC-004 | Token hết hạn. | Hệ thống phải từ chối truy cập và yêu cầu xác thực lại hoặc xử lý theo chính sách session. |
| IAM-EC-005 | Token không hợp lệ. | Hệ thống phải từ chối truy cập. |
| IAM-EC-006 | Actor đăng nhập nhiều thiết bị. | Hệ thống phải xử lý theo chính sách concurrent session được phê duyệt. |
| IAM-EC-007 | Actor logout ở một thiết bị nhưng còn thiết bị khác. | Hệ thống phải xác định logout áp dụng cho phiên hiện tại hay toàn bộ phiên theo chính sách. |
| IAM-EC-008 | Staff tự khóa chính mình. | Hệ thống phải chặn hoặc xử lý theo rule tự bảo vệ vận hành. |
| IAM-EC-009 | Staff khóa Admin. | Hệ thống phải từ chối nếu Staff không có quyền đối với Admin. |
| IAM-EC-010 | Admin tự thu hồi quyền Admin của chính mình. | Hệ thống phải chặn hoặc yêu cầu quy trình bảo vệ nếu hành động làm mất quyền quản trị cần thiết. |
| IAM-EC-011 | Admin cuối cùng bị deactivate. | Hệ thống phải có rule bảo vệ để tránh mất khả năng quản trị. |
| IAM-EC-012 | Role không còn hiệu lực nhưng vẫn được gán cho tài khoản. | Hệ thống không được sử dụng role không hiệu lực cho authorization. |
| IAM-EC-013 | Permission không còn hiệu lực nhưng vẫn nằm trong role. | Hệ thống không được sử dụng permission không hiệu lực cho authorization. |
| IAM-EC-014 | User đã deactivate yêu cầu forgot password. | Hệ thống phải xử lý theo chính sách; mặc định không nên khôi phục truy cập nếu tài khoản không đủ điều kiện. |
| IAM-EC-015 | Locked User yêu cầu forgot password. | Hệ thống phải kiểm tra chính sách xem lock có cho phép khôi phục mật khẩu hay không. |
| IAM-EC-016 | Guest cố truy cập audit. | Hệ thống xử lý Unauthorized Handling. |
| IAM-EC-017 | User cố quản lý role. | Hệ thống xử lý Forbidden Handling. |
| IAM-EC-018 | Staff cố quản lý permission. | Hệ thống xử lý Forbidden Handling. |
| IAM-EC-019 | Admin gán role không tồn tại. | Hệ thống từ chối. |
| IAM-EC-020 | Admin gán permission không tồn tại. | Hệ thống từ chối. |
| IAM-EC-021 | Gán trùng role cho cùng tài khoản. | Hệ thống thông báo không có thay đổi hoặc xử lý theo chính sách chống trùng. |
| IAM-EC-022 | Thu hồi role mà tài khoản không có. | Hệ thống thông báo không có thay đổi. |
| IAM-EC-023 | Khóa tài khoản đã deactivate. | Hệ thống thông báo trạng thái hiện tại hoặc từ chối nếu không có ý nghĩa nghiệp vụ. |
| IAM-EC-024 | Unlock tài khoản không bị khóa. | Hệ thống thông báo không có hành động cần thực hiện. |
| IAM-EC-025 | Reactivate tài khoản vẫn đang bị temporary lock. | Reactivate không tự động unlock trừ khi chính sách cho phép. |
| IAM-EC-026 | Password mới không đạt chính sách. | Hệ thống từ chối đổi hoặc khôi phục mật khẩu. |
| IAM-EC-027 | Actor cố cập nhật profile của người khác. | Hệ thống từ chối trừ khi actor là Admin có quyền phù hợp. |
| IAM-EC-028 | Session hợp lệ nhưng account bị deactivate sau đó. | Hệ thống phải áp dụng account status validation theo chính sách hiệu lực. |
| IAM-EC-029 | Audit view bị truy cập bởi actor không có quyền. | Hệ thống xử lý Forbidden Handling. |
| IAM-EC-030 | System configuration bị truy cập bởi User. | Hệ thống xử lý Forbidden Handling. |
| IAM-EC-031 | Role assignment hoàn tất nhưng audit ghi nhận thất bại. | Cần chính sách nghiệp vụ xác định hành động có được xem là hợp lệ hay phải xử lý ngoại lệ. |
| IAM-EC-032 | Permission update làm Staff mất quyền xử lý ticket đang mở. | Hệ thống phải áp dụng effective permission policy và có thể yêu cầu phân công lại theo quy trình vận hành. |
| IAM-EC-033 | User có nhiều role với permission chồng lấn. | Hệ thống xác định effective permission theo chính sách RBAC. |
| IAM-EC-034 | Role có permission xung đột. | Cần chính sách giải quyết xung đột quyền trong Open Questions. |
| IAM-EC-035 | Admin cố xóa hoặc vô hiệu hóa role mặc định. | Hệ thống phải kiểm tra tác động đến registration và governance trước khi cho phép. |

## 14. Permission Matrix

Ghi chú:

- View: được xem thông tin hoặc truy cập đọc.
- Create: được tạo mới đối tượng trong phạm vi permission.
- Update: được cập nhật thông tin trong phạm vi permission.
- Delete: được xóa hoặc loại bỏ nếu chính sách cho phép.
- Manage: bao gồm quản lý toàn diện trong phạm vi quyền được cấp, có thể bao gồm view, create, update, delete, assign, revoke hoặc thay đổi trạng thái tùy chính sách.
- Ký hiệu "-" nghĩa là không có quyền theo mô hình hiện tại.

| Feature | Guest | User | Staff | Admin |
|---|---|---|---|---|
| Landing Page | View | View | View | View |
| Register | Create | - | - | - |
| Login | View/Create Session | View/Create Session | View/Create Session | View/Create Session |
| Logout | - | Manage Own Session | Manage Own Session | Manage Own Session |
| Own Profile | - | View, Update | View, Update | View, Update |
| Change Own Password | - | Update | Update | Update |
| Forgot Password | View/Update Credential Recovery | View/Update Credential Recovery | View/Update Credential Recovery | View/Update Credential Recovery |
| Session Validation | - | View Own Session State | View Own Session State | View Own Session State |
| Access Control Validation | - | Subject to Permission | Subject to Permission | Subject to Permission |
| Manage Task Permission Entitlement | - | Manage | - | Manage if assigned |
| Manage Timeline Permission Entitlement | - | Manage | - | Manage if assigned |
| Manage Resource Capital Permission Entitlement | - | Manage | - | Manage if assigned |
| Manage Category Permission Entitlement | - | Manage | - | Manage if assigned |
| Manage Tag Permission Entitlement | - | Manage | - | Manage if assigned |
| View Dashboard Permission Entitlement | - | View | - | View if assigned |
| View Reports Permission Entitlement | - | View | - | View if assigned |
| View Activity History Permission Entitlement | - | View Own | View if assigned | View/Manage if assigned |
| Receive Support Ticket Permission | - | - | View, Manage | Manage |
| Manage Ticket Permission | - | - | Manage | Manage |
| Support User Permission | - | - | Manage within scope | Manage |
| View Log Permission | - | - | View within scope | View/Manage |
| Temporary Lock User | - | - | Update Status within scope | Manage |
| Unlock User | - | - | - | Update Status |
| Search User | - | - | - | View |
| View User Detail | - | - | - | View |
| Update User | - | - | - | Update |
| Deactivate User | - | - | - | Update Status |
| Reactivate User | - | - | - | Update Status |
| Manage User | - | - | - | Manage |
| Manage Staff | - | - | - | Manage |
| Manage Role | - | - | - | Manage |
| Assign Role | - | - | - | Manage |
| Revoke Role | - | - | - | Manage |
| Manage Permission | - | - | - | Manage |
| Assign Permission | - | - | - | Manage |
| Revoke Permission | - | - | - | Manage |
| Manage System Configuration Authorization | - | - | - | Manage |
| Manage Audit | - | - | - | Manage |
| View IAM Audit | - | - | View if explicitly assigned | View/Manage |
| View System Dashboard Permission Entitlement | - | - | - | View |

## 15. Risks

### 15.1 Business Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| RBAC không được hiểu thống nhất | Stakeholder có thể hiểu role và permission khác nhau. | Dẫn đến yêu cầu phân quyền mâu thuẫn. | Duy trì glossary, permission matrix và business rule rõ ràng. |
| Phân quyền quá rộng | Actor được cấp quyền vượt quá trách nhiệm. | Tăng rủi ro truy cập sai và vận hành sai. | Áp dụng least privilege và audit thay đổi quyền. |
| Phân quyền quá hẹp | Actor không đủ quyền thực hiện trách nhiệm. | Gây gián đoạn vận hành và hỗ trợ. | Xác nhận trách nhiệm actor với stakeholder. |
| Chính sách trạng thái tài khoản chưa rõ | Lock, deactivate, reactivate và unlock có thể bị hiểu lẫn. | Xử lý tài khoản không nhất quán. | Xác định chính sách vòng đời tài khoản ở volume hoặc policy liên quan. |
| Scope creep | IAM có thể bị mở rộng sang SSO, MFA hoặc identity federation khi chưa phê duyệt. | Tăng độ phức tạp và lệch phạm vi. | Đưa các tính năng chưa xác nhận vào Open Questions hoặc Suggested Improvements. |

### 15.2 Security Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Tài khoản bị truy cập trái phép | Thông tin đăng nhập bị lộ hoặc xác thực yếu. | Mất quyền riêng tư và rủi ro dữ liệu cá nhân. | Chính sách mật khẩu, session, recovery và audit cần được xác nhận. |
| Session bị lạm dụng | Phiên không hợp lệ hoặc hết hạn vẫn được sử dụng. | Actor có thể truy cập trái phép. | Kiểm tra session và token trước hành động được bảo vệ. |
| Permission bị giữ sau khi thu hồi | Actor tiếp tục sử dụng quyền đã bị revoke. | Vi phạm authorization. | Effective permission refresh phải được định nghĩa rõ. |
| Staff lạm dụng quyền khóa | Staff khóa tài khoản không đúng phạm vi. | Gián đoạn truy cập và ảnh hưởng niềm tin. | Giới hạn scope Staff, yêu cầu lý do và audit. |
| Admin tự làm mất quyền quản trị | Admin cuối cùng bị deactivate hoặc thu hồi quyền. | Hệ thống mất khả năng vận hành quản trị. | Áp dụng self-protection rule cho quyền Admin tối thiểu. |

### 15.3 Operational Risks

| Risk | Description | Impact | Mitigation Direction |
|---|---|---|---|
| Thiếu quy trình xử lý locked account | Không rõ ai được unlock và khi nào. | Chậm khôi phục truy cập. | Xác định quy trình unlock và điều kiện mở khóa. |
| Audit quá ít thông tin | Không đủ dữ liệu để điều tra thay đổi IAM. | Giảm accountability. | Xác định audit event và context tối thiểu. |
| Audit quá rộng | Ghi nhận quá nhiều thông tin không cần thiết. | Tăng gánh nặng vận hành và rủi ro privacy. | Xác định phạm vi audit hợp lý. |
| Quản lý role thiếu kiểm soát | Role được tạo hoặc sửa thiếu governance. | Quyền truy cập trở nên khó kiểm soát. | Role Management phải có rule, audit và review. |
| Khôi phục mật khẩu thiếu quy trình | User không thể lấy lại quyền truy cập hoặc quy trình không an toàn. | Giảm trải nghiệm hoặc tăng rủi ro bảo mật. | Xác định chính sách identity verification cho forgot password. |

## 16. Open Questions

| Question ID | Open Question | Impact Area |
|---|---|---|
| IAM-OQ-001 | Chính sách mật khẩu cụ thể là gì, bao gồm độ dài, độ phức tạp, thời hạn, lịch sử mật khẩu và điều kiện thay đổi? | Security, Usability |
| IAM-OQ-002 | Registration có yêu cầu xác minh email, số điện thoại hoặc phương thức xác minh nào khác không? | Registration, Compliance |
| IAM-OQ-003 | Forgot Password sẽ xác minh danh tính bằng cơ chế nghiệp vụ nào? | Recovery, Security |
| IAM-OQ-004 | Có áp dụng khóa tài khoản tự động sau nhiều lần đăng nhập sai không? Nếu có, ngưỡng và thời gian khóa là bao nhiêu? | Security |
| IAM-OQ-005 | Temporary lock có thời hạn mặc định không, hay chỉ Admin mới được unlock? | Account Status |
| IAM-OQ-006 | Staff có được unlock User không, hay unlock chỉ thuộc Admin? | Operational Policy |
| IAM-OQ-007 | Staff có được khóa tài khoản Staff khác không, hay chỉ khóa User? | Operational Scope |
| IAM-OQ-008 | Admin có được tự thu hồi quyền Admin của chính mình không? Nếu có, điều kiện bảo vệ là gì? | Governance |
| IAM-OQ-009 | Hệ thống có yêu cầu luôn tồn tại ít nhất một Admin active không? | Governance |
| IAM-OQ-010 | Role mặc định sau đăng ký là gì? | Registration, RBAC |
| IAM-OQ-011 | Một tài khoản có thể có nhiều role đồng thời không? Nếu có, effective permission được tính như thế nào? | RBAC |
| IAM-OQ-012 | Có tồn tại permission được gán trực tiếp cho user ngoài role không, hay permission chỉ gán qua role? | RBAC |
| IAM-OQ-013 | Khi role hoặc permission bị thu hồi, thay đổi có hiệu lực ngay với session đang hoạt động không? | Session, Security |
| IAM-OQ-014 | Policy cho đăng nhập nhiều thiết bị là gì? | Session |
| IAM-OQ-015 | Logout áp dụng cho phiên hiện tại hay toàn bộ phiên của tài khoản? | Session |
| IAM-OQ-016 | Audit cần ghi nhận những trường thông tin nghiệp vụ tối thiểu nào? | Audit |
| IAM-OQ-017 | Staff được xem loại log nào và có được xem thông tin cá nhân của User trong log không? | Privacy, Support |
| IAM-OQ-018 | Admin được cập nhật những trường hồ sơ nào của User? | User Management |
| IAM-OQ-019 | Deactivate khác Temporary Lock ở điểm nào về thời hạn, lý do và khả năng khôi phục? | Account Lifecycle |
| IAM-OQ-020 | Reactivate có tự động unlock tài khoản không nếu tài khoản vừa deactivated vừa locked? | Account Lifecycle |
| IAM-OQ-021 | Có cần approval nhiều cấp cho thay đổi role hoặc permission nhạy cảm không? | Governance |
| IAM-OQ-022 | Có yêu cầu periodic access review cho Staff và Admin không? | Compliance, Governance |
| IAM-OQ-023 | Có yêu cầu MFA cho Admin hoặc Staff không? | Security |
| IAM-OQ-024 | Có cần phân biệt Admin cấp cao và Admin vận hành không? | RBAC |
| IAM-OQ-025 | Chính sách retention đối với audit IAM là gì? | Compliance |

## 17. Suggested Improvements

Các đề xuất dưới đây là cải tiến nghiệp vụ tiềm năng. Chúng chưa phải là yêu cầu chính thức nếu chưa được stakeholder phê duyệt.

| Improvement ID | Suggested Improvement | Business Rationale |
|---|---|---|
| IAM-SI-001 | Áp dụng nguyên tắc least privilege một cách chính thức trong policy phân quyền. | Giảm rủi ro cấp quyền vượt trách nhiệm. |
| IAM-SI-002 | Thiết lập periodic access review cho Staff và Admin. | Bảo đảm quyền vận hành còn phù hợp theo thời gian. |
| IAM-SI-003 | Yêu cầu lý do bắt buộc cho temporary lock, unlock, deactivate, reactivate, assign role và revoke role. | Tăng tính giải trình và hỗ trợ audit. |
| IAM-SI-004 | Xây dựng quy trình phê duyệt bổ sung cho role hoặc permission nhạy cảm. | Giảm rủi ro thay đổi quyền không được kiểm soát. |
| IAM-SI-005 | Định nghĩa rõ account lifecycle gồm active, pending, locked, deactivated và trạng thái khôi phục. | Giúp vận hành nhất quán và giảm hiểu nhầm giữa lock và deactivate. |
| IAM-SI-006 | Xem xét MFA cho Admin và Staff. | Tăng bảo vệ cho tài khoản có quyền cao. |
| IAM-SI-007 | Xây dựng access review report ở mức nghiệp vụ cho Admin. | Hỗ trợ rà soát quyền và phát hiện quyền bất thường. |
| IAM-SI-008 | Thiết lập quy tắc bảo vệ Admin cuối cùng. | Tránh tình huống hệ thống mất khả năng quản trị. |
| IAM-SI-009 | Xác định chính sách concurrent session theo risk level của role. | Có thể cho phép User linh hoạt hơn nhưng kiểm soát chặt hơn với Admin. |
| IAM-SI-010 | Chuẩn hóa thông báo unauthorized và forbidden. | Cải thiện trải nghiệm người dùng và tránh rò rỉ thông tin nhạy cảm. |
| IAM-SI-011 | Xây dựng permission catalog được quản trị chính thức. | Giúp Admin hiểu ý nghĩa permission trước khi gán hoặc thu hồi. |
| IAM-SI-012 | Phân loại audit event theo mức độ nghiêm trọng. | Hỗ trợ ưu tiên điều tra và vận hành. |
| IAM-SI-013 | Định nghĩa policy khôi phục tài khoản bị khóa hoặc deactivate. | Giảm rủi ro xử lý không nhất quán trong hỗ trợ. |
| IAM-SI-014 | Xem xét separation of duties cho các hành động nhạy cảm. | Một actor không nên tự tạo, phê duyệt và áp dụng thay đổi quyền đặc biệt nếu sản phẩm mở rộng. |
| IAM-SI-015 | Xây dựng hướng dẫn nghiệp vụ cho Staff khi sử dụng quyền temporary lock. | Giúp Staff sử dụng quyền khóa đúng mục đích và đúng phạm vi. |

## Appendix A. Traceability Summary

| Source | Related IAM Content |
|---|---|
| Volume 1 - Actors | Guest, User, Staff và Admin được kế thừa làm actor chính của IAM. |
| Volume 1 - Business Principles | Privacy Respect, Operational Accountability, Scope Discipline và Measurability được phản ánh trong Security, Audit và RBAC. |
| Volume 1 - Business Scope | Identity and Authorization là capability nền tảng được tách thành Volume 2. |
| Volume 1 - Business Glossary | Actor, Permission, Role, Audit, User và Admin được mở rộng ở mức IAM. |

## Appendix B. IAM Glossary

| Term | Definition |
|---|---|
| Access Control | Hoạt động xác định actor có được truy cập một khu vực hoặc chức năng được bảo vệ hay không. |
| Account Status | Trạng thái nghiệp vụ của tài khoản, ví dụ active, locked, deactivated hoặc trạng thái khác được phê duyệt. |
| Authentication | Quá trình xác minh actor là chủ thể hợp lệ của tài khoản. |
| Authorization | Quá trình xác định actor đã xác thực có được phép thực hiện hành động cụ thể hay không. |
| Effective Permission | Tập quyền có hiệu lực thực tế của actor sau khi xét role, permission, trạng thái tài khoản và rule liên quan. |
| Forbidden | Tình huống actor đã xác thực nhưng không có quyền thực hiện hành động được yêu cầu. |
| Identity | Thông tin đại diện cho một actor trong hệ thống. |
| Permission | Quyền cho phép thực hiện một hành động hoặc truy cập một khu vực chức năng cụ thể. |
| RBAC | Role Based Access Control, mô hình kiểm soát quyền dựa trên vai trò. |
| Role | Nhóm quyền đại diện cho một trách nhiệm hoặc phạm vi truy cập nghiệp vụ. |
| Session | Trạng thái đăng nhập hợp lệ của actor trong một khoảng thời gian hoặc ngữ cảnh sử dụng. |
| Token | Bằng chứng phiên hoặc thông tin đại diện cho trạng thái xác thực ở mức nghiệp vụ. |
| Unauthorized | Tình huống actor chưa xác thực truy cập chức năng yêu cầu đăng nhập. |

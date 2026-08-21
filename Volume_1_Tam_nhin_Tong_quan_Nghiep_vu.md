# LifeBalance
# Volume 1 - Tầm nhìn & Tổng quan nghiệp vụ

## 1. Document Information

| Trường thông tin | Nội dung |
|---|---|
| Document Title | Volume 1 - Tầm nhìn & Tổng quan nghiệp vụ |
| Project Name | LifeBalance |
| Version | 1.0 |
| Author | Nhóm Phân tích nghiệp vụ |
| Reviewer | Chưa xác định |
| Status | Draft |
| Date | 04/07/2026 |

Tài liệu này xác định tầm nhìn sản phẩm, bối cảnh nghiệp vụ, vấn đề cần giải quyết, mục tiêu kinh doanh, phạm vi nghiệp vụ, các bên liên quan, nhóm người dùng, tác nhân, năng lực nghiệp vụ cấp cao, nguyên tắc vận hành và các chỉ số đánh giá thành công cho dự án LifeBalance.

LifeBalance là hệ thống quản lý nguồn lực cá nhân, giúp người dùng lập kế hoạch, phân bổ, theo dõi và đánh giá việc sử dụng hai nguồn lực cốt lõi là thời gian và tiền bạc trong các công việc hằng ngày. Tài liệu này đóng vai trò định hướng nền tảng cho toàn bộ dự án. Các tài liệu yêu cầu chi tiết ở các volume sau phải kế thừa và tuân thủ các định nghĩa, phạm vi, giả định, thuật ngữ và nguyên tắc đã được mô tả trong tài liệu này.

Tài liệu được xây dựng theo góc nhìn Phân tích nghiệp vụ. Nội dung không bao gồm thiết kế cơ sở dữ liệu, thiết kế ERD, thiết kế bảng dữ liệu, thiết kế API, thiết kế microservice, thiết kế kiến trúc hệ thống, thiết kế màn hình hoặc mã nguồn.

## 2. Revision History

| Version | Date | Author | Description |
|---|---|---|---|
| 0.1 | 04/07/2026 | Nhóm Phân tích nghiệp vụ | Khởi tạo cấu trúc tài liệu và các mục nội dung chính. |
| 1.0 | 04/07/2026 | Nhóm Phân tích nghiệp vụ | Hoàn thiện bản dự thảo đầu tiên của tài liệu Tầm nhìn & Tổng quan nghiệp vụ cho LifeBalance. |

## 3. Purpose

### 3.1 Mục đích tài liệu

Mục đích của tài liệu này là thiết lập một nhận thức nghiệp vụ thống nhất về dự án LifeBalance trước khi đi vào đặc tả yêu cầu chi tiết. Tài liệu giải thích lý do hệ thống cần được xây dựng, các vấn đề nghiệp vụ mà hệ thống hướng đến giải quyết, các nhóm người dùng và bên liên quan, phạm vi nghiệp vụ, năng lực nghiệp vụ cấp cao, các nguyên tắc vận hành và tiêu chí đánh giá thành công.

LifeBalance không được định vị như một ứng dụng danh sách việc cần làm truyền thống. Trọng tâm của LifeBalance là quản lý công việc dựa trên nguồn lực. Mỗi công việc được xem là một khoản đầu tư nguồn lực cần được lập kế hoạch, phân bổ, thực hiện, theo dõi và đánh giá. Hai nguồn lực trọng tâm của hệ thống là thời gian và tiền bạc.

Tài liệu này là tài liệu định hướng ban đầu cho toàn bộ dự án. Các volume sau như Business Requirement Specification, Software Requirement Specification, Use Case Specification, Business Rule Catalog, Reporting Requirement hoặc Acceptance Criteria phải có khả năng truy vết về tầm nhìn và phạm vi được xác lập trong tài liệu này.

### 3.2 Đối tượng sử dụng tài liệu

| Đối tượng | Cách sử dụng tài liệu |
|---|---|
| Nhà tài trợ dự án | Hiểu lý do đầu tư, giá trị kinh doanh và kết quả kỳ vọng của dự án. |
| Product Owner hoặc Business Owner | Sử dụng tài liệu để định hướng ưu tiên sản phẩm và xác nhận mục tiêu nghiệp vụ. |
| Business Analyst | Sử dụng tài liệu làm cơ sở cho việc khai thác, phân tích, xác minh và quản lý yêu cầu chi tiết. |
| Nhóm phát triển | Hiểu ý nghĩa nghiệp vụ và phạm vi cấp cao trước khi triển khai kỹ thuật ở các giai đoạn sau. |
| Nhóm kiểm thử | Hiểu tiêu chí thành công và kỳ vọng nghiệp vụ để chuẩn bị kiểm thử yêu cầu sau này. |
| Giảng viên, hội đồng hoặc người phản biện học thuật | Đánh giá tính hợp lý, tính ứng dụng và phương pháp phân tích yêu cầu của dự án. |
| Nhóm vận hành và hỗ trợ | Hiểu vai trò nghiệp vụ của Staff và Admin trong vận hành hệ thống. |

### 3.3 Giá trị của tài liệu

Tài liệu mang lại các giá trị chính sau:

Thứ nhất, tài liệu giúp giảm sự mơ hồ ở giai đoạn đầu dự án. LifeBalance được xác định rõ là hệ thống quản lý nguồn lực cá nhân, không đơn thuần là ứng dụng ghi nhớ công việc, lịch cá nhân hoặc sổ ghi chép chi tiêu.

Thứ hai, tài liệu tạo ra một hệ thống thuật ngữ chung cho các bên liên quan. Các thuật ngữ như nguồn lực, vốn nguồn lực, phân bổ, giá trị kế hoạch, giá trị thực tế, sai lệch, hiệu quả, timeline và đánh giá được định nghĩa nhất quán nhằm hạn chế hiểu sai trong các giai đoạn sau.

Thứ ba, tài liệu thiết lập ranh giới nghiệp vụ. Việc xác định In Scope, Out of Scope và Future Scope giúp kiểm soát phạm vi, hạn chế mở rộng yêu cầu không kiểm soát và hỗ trợ quyết định ưu tiên.

Thứ tư, tài liệu hỗ trợ truy vết yêu cầu. Các mục tiêu nghiệp vụ, năng lực nghiệp vụ, tiêu chí thành công và chỉ số đo lường trong tài liệu này là cơ sở để xây dựng các yêu cầu chức năng và phi chức năng trong các volume sau.

Thứ năm, tài liệu cung cấp nền tảng trình bày chuyên nghiệp, phù hợp với báo cáo tốt nghiệp ngành Công nghệ thông tin và phù hợp với phong cách tài liệu yêu cầu trong môi trường doanh nghiệp.

## 4. Business Context

### 4.1 Bối cảnh

Trong đời sống hiện đại, mỗi cá nhân phải quản lý đồng thời nhiều nhóm công việc và trách nhiệm khác nhau. Các hoạt động này có thể bao gồm học tập, công việc văn phòng, dự án cá nhân, công việc tự do, trách nhiệm gia đình, chăm sóc sức khỏe, phát triển bản thân, quản lý tài chính cá nhân và các cam kết xã hội.

Mỗi hoạt động đều tiêu tốn nguồn lực. Một buổi học tiêu tốn thời gian. Một cuộc họp tiêu tốn thời gian và có thể kéo theo chi phí di chuyển. Một khóa học tiêu tốn tiền học phí và thời gian tham gia. Một dự án freelance tiêu tốn thời gian thực hiện và có thể phát sinh chi phí công cụ, phần mềm hoặc dịch vụ hỗ trợ. Tuy nhiên, trong thực tế, người dùng thường chỉ ghi nhận công việc như một việc cần làm, mà không gắn công việc đó với lượng nguồn lực cần thiết.

Nhiều người hiện sử dụng các công cụ rời rạc để quản lý cuộc sống cá nhân. Ứng dụng to-do list dùng để ghi nhớ việc cần làm. Lịch cá nhân dùng để đặt lịch sự kiện. Ứng dụng quản lý chi tiêu dùng để ghi nhận thu nhập và chi phí. Công cụ ghi chú dùng để lưu ý tưởng, kế hoạch hoặc nhận xét. Tuy nhiên, các công cụ này thường không cung cấp một góc nhìn thống nhất về việc nguồn lực cá nhân được lập kế hoạch, phân bổ, sử dụng và đánh giá như thế nào trong mối liên hệ với công việc.

LifeBalance xuất hiện trong bối cảnh đó với định hướng quản lý công việc dựa trên nguồn lực. Hệ thống xem thời gian và tiền bạc là vốn nguồn lực cá nhân cần được quản trị có ý thức. Công việc không chỉ là một mục cần hoàn thành, mà là một cam kết tiêu tốn nguồn lực và cần tạo ra giá trị tương xứng.

### 4.2 Vấn đề nghiệp vụ

Vấn đề nghiệp vụ cốt lõi là người dùng thường quản lý công việc tách rời khỏi năng lực nguồn lực thực tế. Người dùng có thể biết mình cần làm gì, nhưng không biết rõ liệu mình có đủ thời gian hay tiền bạc để thực hiện hay không. Người dùng cũng thường không đánh giá được công việc sau khi hoàn thành có tiêu tốn nguồn lực hợp lý hay không.

Một số biểu hiện phổ biến của vấn đề bao gồm:

- Người dùng tạo quá nhiều công việc so với lượng thời gian thực tế có thể sử dụng.
- Người dùng cam kết chi tiêu cho một hoạt động mà chưa đánh giá khả năng tài chính.
- Người dùng không ước lượng trước thời gian hoặc chi phí cần thiết cho công việc.
- Người dùng hoàn thành công việc nhưng không biết công việc đó đã tiêu tốn bao nhiêu nguồn lực.
- Người dùng không so sánh được nguồn lực dự kiến và nguồn lực thực tế.
- Người dùng không rút ra được kinh nghiệm định lượng cho các kế hoạch tương lai.

### 4.3 Khó khăn hiện nay

| Nhóm khó khăn | Mô tả |
|---|---|
| Khó khăn trong lập kế hoạch | Người dùng thường liệt kê công việc nhưng không xác định nguồn lực cần thiết để thực hiện. |
| Khó khăn trong phân bổ nguồn lực | Người dùng không có cơ chế rõ ràng để dành trước thời gian hoặc tiền bạc cho từng công việc. |
| Khó khăn trong theo dõi thực tế | Người dùng có thể thực hiện công việc nhưng không ghi nhận thời gian và tiền bạc thực tế đã sử dụng. |
| Khó khăn trong đánh giá hiệu quả | Người dùng hiếm khi so sánh kế hoạch và thực tế, dẫn đến thiếu cơ sở cải thiện năng lực lập kế hoạch. |

Các khó khăn này không chỉ là vấn đề công cụ. Đây là vấn đề về cách cá nhân quản trị nguồn lực. Nếu không có một quy trình hỗ trợ từ lập kế hoạch đến đánh giá, người dùng dễ rơi vào trạng thái quá tải, chi tiêu thiếu kiểm soát, hoặc không biết nguyên nhân khiến kế hoạch thường xuyên thất bại.

### 4.4 Lý do cần xây dựng hệ thống

LifeBalance cần được xây dựng vì năng suất cá nhân không nên chỉ được đánh giá bằng số lượng công việc hoàn thành. Một người có thể hoàn thành nhiều việc nhưng tiêu tốn quá nhiều thời gian, vượt ngân sách hoặc bỏ qua các ưu tiên quan trọng hơn. Ngược lại, một người có thể hoàn thành ít việc hơn nhưng sử dụng nguồn lực hiệu quả hơn và đạt được kết quả có ý nghĩa hơn.

Hệ thống được xây dựng dựa trên các lý do nghiệp vụ sau:

- Nguồn lực cá nhân là hữu hạn và cần được quản lý có chủ đích.
- Công việc cần được xem xét dưới góc độ tiêu tốn nguồn lực.
- Việc thực hiện công việc nên được đặt sau bước lập kế hoạch và phân bổ nguồn lực.
- Dữ liệu thực tế về thời gian và tiền bạc cần được ghi nhận để hỗ trợ đánh giá.
- Người dùng cần thấy được sai lệch giữa kế hoạch và thực tế để cải thiện.
- Hiệu quả cá nhân cần được hiểu thông qua mối quan hệ giữa nguồn lực đã dùng và kết quả đạt được.

## 5. Problem Statement

### 5.1 Tổng quan vấn đề

Vấn đề trung tâm mà LifeBalance giải quyết là sự tách rời giữa quản lý công việc và quản lý nguồn lực. Người dùng thường có danh sách công việc, nhưng danh sách đó không phản ánh đầy đủ khả năng thực hiện dựa trên thời gian và tiền bạc sẵn có. Điều này tạo ra khoảng cách giữa mong muốn và năng lực thực tế.

LifeBalance hướng đến giải quyết vấn đề bằng cách thiết lập một chu trình nghiệp vụ:

Planning -> Resource Allocation -> Execution -> Tracking -> Evaluation -> Improvement

Chu trình này giúp người dùng không chỉ biết việc cần làm, mà còn biết việc đó cần bao nhiêu nguồn lực, đã được cấp nguồn lực ra sao, được thực hiện như thế nào, tiêu tốn nguồn lực thực tế bao nhiêu và có hiệu quả hay không.

### 5.2 Pain Point

| Pain Point | Phân tích |
|---|---|
| Thiếu khả năng nhìn thấy nguồn lực hiện có | Người dùng không có cái nhìn rõ ràng về lượng thời gian và tiền bạc có thể sử dụng cho các kế hoạch sắp tới. |
| Quá tải công việc | Người dùng tạo nhiều công việc hơn khả năng nguồn lực thực tế có thể đáp ứng. |
| Lập kế hoạch thiếu kỷ luật | Công việc thường được tạo mà không có ước lượng thời gian, chi phí, mức ưu tiên, giá trị kỳ vọng hoặc thời hạn hợp lý. |
| Phân bổ nguồn lực không nhất quán | Người dùng có thể dự định dành thời gian hoặc tiền cho một việc, nhưng cam kết đó không được ghi nhận và theo dõi. |
| Ghi nhận thực tế yếu | Người dùng không thường xuyên ghi lại thời gian và tiền bạc thực tế đã sử dụng cho công việc. |
| Không đánh giá hiệu quả | Công việc thường chỉ được đánh dấu hoàn thành mà không xem xét mức độ hiệu quả của nguồn lực đã tiêu tốn. |
| Công cụ bị phân mảnh | Quản lý thời gian, công việc và tiền bạc thường diễn ra trên nhiều công cụ khác nhau. |
| Thiếu phản hồi để cải thiện | Người dùng không có dữ liệu có cấu trúc để rút kinh nghiệm từ các sai lệch trong quá khứ. |

### 5.3 Root Cause

Các pain point trên xuất phát từ nhiều nguyên nhân gốc rễ.

Thứ nhất, các ứng dụng quản lý công việc truyền thống thường tập trung vào trạng thái hoàn thành. Một công việc có thể được đánh dấu là chưa làm, đang làm hoặc đã hoàn thành, nhưng không yêu cầu người dùng xác định lượng thời gian và tiền bạc dự kiến cần tiêu tốn.

Thứ hai, các ứng dụng quản lý tài chính cá nhân thường tập trung vào giao dịch thu chi. Chúng có thể cho biết người dùng đã chi bao nhiêu tiền, nhưng không nhất thiết cho biết khoản chi đó gắn với công việc nào, mục tiêu nào hoặc kết quả nào.

Thứ ba, người dùng thường lập kế hoạch dựa trên kỳ vọng lạc quan hơn là năng lực nguồn lực thực tế. Điều này dẫn đến tình trạng lịch làm việc bị quá tải, công việc bị trì hoãn hoặc chi phí phát sinh ngoài dự kiến.

Thứ tư, dữ liệu sau khi thực hiện công việc không được phản hồi vào kế hoạch tương lai. Nếu người dùng không biết vì sao một công việc mất nhiều thời gian hơn dự kiến hoặc vì sao chi phí vượt kế hoạch, người dùng khó cải thiện năng lực ước lượng.

Thứ năm, năng suất cá nhân thường được hiểu như vấn đề động lực hoặc kỷ luật cá nhân. LifeBalance tiếp cận năng suất như một vấn đề quản trị nguồn lực: người dùng cần có khả năng nhìn thấy, phân bổ, sử dụng và đánh giá nguồn lực của mình.

### 5.4 Current Limitation

| Hạn chế hiện tại | Giải thích |
|---|---|
| Góc nhìn thiên về hoàn thành | Nhiều công cụ chỉ quan tâm công việc đã xong hay chưa, không đánh giá nguồn lực đã tiêu tốn. |
| Thiếu lập kế hoạch nguồn lực | Người dùng không được hỗ trợ đầy đủ để ước lượng thời gian và chi phí trước khi bắt đầu công việc. |
| Thiếu so sánh planned và actual | Người dùng không có cơ chế nhất quán để so sánh kế hoạch và thực tế. |
| Đánh giá chưa đa chiều | Công việc ít khi được đánh giá theo thời gian, chi phí, mức ưu tiên, loại công việc và kết quả. |
| Thiếu insight lịch sử | Dữ liệu cũ nếu có thường không được tổ chức để phục vụ phân tích hiệu quả nguồn lực. |
| Thiếu quản trị vận hành | Một số công cụ cá nhân không xác định rõ vai trò hỗ trợ và quản trị nền tảng như Staff và Admin. |

### 5.5 Business Impact

Tác động nghiệp vụ của vấn đề bao gồm:

- Giảm năng suất cá nhân do lập kế hoạch không thực tế.
- Tăng căng thẳng vì người dùng cam kết nhiều hơn khả năng thực hiện.
- Giảm khả năng kiểm soát tài chính khi tiền được sử dụng mà không gắn với đánh giá công việc.
- Giảm kỷ luật thời gian khi công việc được nhận hoặc tạo mà không xem xét năng lực thời gian.
- Giảm độ tin cậy của kế hoạch vì thiếu dữ liệu lịch sử.
- Hạn chế khả năng cải thiện vì người dùng không biết sai lệch đến từ đâu.
- Làm giảm giá trị cảm nhận của công cụ năng suất nếu công cụ chỉ lưu trữ công việc mà không hỗ trợ quyết định.

Đối với LifeBalance, việc giải quyết các tác động này tạo ra cơ hội xây dựng một sản phẩm có giá trị khác biệt: không chỉ hỗ trợ người dùng làm việc, mà còn hỗ trợ người dùng quyết định việc nào xứng đáng được đầu tư thời gian và tiền bạc.

## 6. Vision Statement

### 6.1 Tuyên bố tầm nhìn

LifeBalance hướng tới trở thành hệ thống quản lý nguồn lực cá nhân giúp mỗi người lập kế hoạch, phân bổ, thực hiện, theo dõi và đánh giá công việc hằng ngày dựa trên việc sử dụng có trách nhiệm hai nguồn lực hữu hạn là thời gian và tiền bạc.

Hệ thống hướng đến một trải nghiệm trong đó mỗi công việc được nhìn nhận như một khoản đầu tư nguồn lực; mỗi khoản phân bổ nguồn lực đều có thể được nhìn thấy; mỗi mức sử dụng thực tế đều có thể so sánh với kế hoạch; và mỗi công việc đã thực hiện đều tạo ra dữ liệu giúp cải thiện kế hoạch trong tương lai.

### 6.2 Giải thích tầm nhìn

Tầm nhìn của LifeBalance dựa trên quan điểm rằng năng suất cá nhân hiệu quả không chỉ đến từ việc ghi nhớ công việc. Người dùng cần hiểu mối quan hệ giữa mục tiêu cá nhân, nguồn lực hiện có, cam kết công việc, nguồn lực thực tế đã sử dụng và kết quả đạt được.

Tầm nhìn này bao gồm năm ý tưởng chính.

Thứ nhất, lập kế hoạch phải có chủ đích. Người dùng cần hiểu việc gì cần làm và vì sao việc đó quan trọng.

Thứ hai, nguồn lực cần được phân bổ trước hoặc trong quá trình chuẩn bị thực hiện. Thời gian và tiền bạc không nên được xem là vô hạn hoặc được sử dụng một cách cảm tính.

Thứ ba, quá trình thực hiện cần có khả năng quan sát. Người dùng cần biết công việc đang ở trạng thái nào và nguồn lực thực tế đang được sử dụng ra sao.

Thứ tư, đánh giá cần dựa trên dữ liệu. Người dùng cần so sánh được giá trị kế hoạch và giá trị thực tế.

Thứ năm, cải thiện cần diễn ra liên tục. Dữ liệu lịch sử phải hỗ trợ người dùng lập kế hoạch tốt hơn trong tương lai.

### 6.3 Định hướng chiến lược

Ở cấp chiến lược, LifeBalance cần phát triển theo các định hướng sau:

- Hỗ trợ lập kế hoạch công việc có xét đến nguồn lực.
- Cung cấp khả năng nhìn thấy năng lực nguồn lực cá nhân.
- Hỗ trợ phân bổ thời gian và tiền bạc cho từng công việc.
- Hỗ trợ ghi nhận nguồn lực thực tế đã sử dụng.
- Hỗ trợ so sánh planned và actual.
- Cung cấp dashboard và báo cáo phục vụ ra quyết định.
- Hỗ trợ vai trò vận hành và quản trị thông qua Staff và Admin.

Hệ thống cần duy trì bản sắc sản phẩm rõ ràng: LifeBalance không phải chỉ là checklist, mà là công cụ hỗ trợ quyết định trong quản lý nguồn lực cá nhân.

## 7. Product Philosophy

### 7.1 Triết lý cốt lõi

Triết lý sản phẩm của LifeBalance được thể hiện qua hai mệnh đề:

1. Mọi công việc đều tiêu tốn nguồn lực.
2. Mọi nguồn lực đều hữu hạn.

Hai mệnh đề này là nền tảng tư duy cho toàn bộ yêu cầu nghiệp vụ của hệ thống.

### 7.2 Mọi công việc đều tiêu tốn nguồn lực

Một công việc dù đơn giản vẫn cần ít nhất một loại nguồn lực. Một buổi học cần thời gian. Một cuộc họp cần thời gian và có thể phát sinh chi phí di chuyển. Một hoạt động rèn luyện sức khỏe cần thời gian và có thể cần chi phí hội viên, thiết bị hoặc dịch vụ. Một dự án freelance cần thời gian thực hiện, chi phí phần mềm, chi phí công cụ hoặc chi phí thuê ngoài.

Các ứng dụng to-do truyền thống thường xem công việc như một mục độc lập trong danh sách. LifeBalance xem công việc như một cam kết tiêu tốn nguồn lực. Do đó, công việc không chỉ được đánh giá bằng tên gọi, trạng thái hoặc deadline, mà còn bằng lượng nguồn lực cần thiết và mức độ hiệu quả khi sử dụng nguồn lực đó.

Từ góc nhìn phân tích nghiệp vụ, triết lý này ảnh hưởng đến hệ thống như sau:

- Lập kế hoạch công việc cần bao gồm cân nhắc về nguồn lực.
- Phân bổ nguồn lực cần gắn với công việc cụ thể.
- Thực hiện công việc cần có khả năng ghi nhận nguồn lực thực tế.
- Đánh giá công việc cần so sánh nguồn lực dự kiến và nguồn lực thực tế.
- Báo cáo cần thể hiện cách nguồn lực được phân bổ và tiêu thụ theo công việc, thời gian, category hoặc tag.

### 7.3 Mọi nguồn lực đều hữu hạn

Thời gian và tiền bạc đều có giới hạn. Người dùng không thể phân bổ nhiều thời gian hơn lượng thời gian thực tế có trong ngày, tuần hoặc kỳ kế hoạch. Người dùng cũng không thể duy trì việc chi tiêu vượt quá vốn tiền bạc hoặc ngân sách cá nhân.

Nguyên tắc này thúc đẩy việc lập kế hoạch có trách nhiệm. Nếu một công việc nhận nhiều thời gian hơn, công việc khác có thể phải nhận ít thời gian hơn. Nếu một hoạt động tiêu tốn nhiều tiền hơn kế hoạch, nguồn lực tài chính dành cho hoạt động khác có thể bị ảnh hưởng.

Triết lý này ảnh hưởng đến mô hình nghiệp vụ của LifeBalance như sau:

- Hệ thống cần thúc đẩy nhận thức về nguồn lực hiện có.
- Cam kết công việc cần được xem xét trong quan hệ với năng lực nguồn lực.
- Tình trạng phân bổ vượt năng lực cần được xem là một vấn đề nghiệp vụ cần nhận diện.
- Sai lệch giữa kế hoạch và thực tế cần được xem là tín hiệu học hỏi.
- Hiệu quả cần trở thành một phần trong đánh giá kết quả công việc.

### 7.4 Ảnh hưởng tới thiết kế nghiệp vụ

Triết lý sản phẩm không phải là thiết kế kỹ thuật. Tuy nhiên, nó định hướng mạnh mẽ cách xác định yêu cầu nghiệp vụ.

| Nhóm nghiệp vụ | Ảnh hưởng của triết lý sản phẩm |
|---|---|
| Resource Capital Management | Người dùng cần hiểu và duy trì vốn nguồn lực thời gian, tiền bạc. |
| Task Management | Công việc cần bao gồm thông tin kế hoạch liên quan đến nguồn lực dự kiến. |
| Resource Allocation | Người dùng cần có khả năng phân bổ thời gian và tiền bạc cho công việc. |
| Execution Tracking | Người dùng cần ghi nhận nguồn lực thực tế đã sử dụng. |
| Evaluation | Hệ thống cần hỗ trợ đánh giá sai lệch và hiệu quả. |
| Dashboard | Dashboard cần thể hiện tình trạng nguồn lực, phân bổ, sử dụng và hiệu quả. |
| Reporting | Báo cáo cần giúp người dùng nhận diện xu hướng và cải thiện lập kế hoạch. |

### 7.5 Hàm ý nghiệp vụ

Hàm ý nghiệp vụ quan trọng là LifeBalance cần khuyến khích người dùng suy nghĩ trước khi hành động, phân bổ trước khi tiêu dùng, ghi nhận sau khi thực hiện và học hỏi sau khi hoàn thành. Sản phẩm cần hỗ trợ một chu trình sử dụng nguồn lực có ý thức, thay vì chỉ ghi nhận công việc một cách thụ động.

## 8. Core Value Proposition

### 8.1 Tuyên bố giá trị cốt lõi

LifeBalance giúp người dùng quản lý công việc hằng ngày như các khoản đầu tư nguồn lực bằng cách kết nối công việc với lập kế hoạch thời gian, lập kế hoạch tiền bạc, phân bổ nguồn lực, theo dõi thực tế và đánh giá hiệu quả.

### 8.2 So sánh với To-do App

| Khía cạnh | To-do App truyền thống | LifeBalance |
|---|---|---|
| Trọng tâm chính | Ghi nhớ và hoàn thành công việc. | Quản lý công việc như cam kết tiêu tốn nguồn lực. |
| Nhận thức nguồn lực | Thường hạn chế hoặc không có. | Là khái niệm nghiệp vụ trung tâm. |
| Độ sâu lập kế hoạch | Thường gồm tên việc, deadline, ưu tiên, checklist. | Bao gồm định hướng lập kế hoạch và phân bổ nguồn lực. |
| Ý nghĩa hoàn thành | Công việc được đánh dấu xong. | Công việc được đánh giá theo kế hoạch, thực tế và hiệu quả. |
| Phản hồi học hỏi | Hạn chế. | Hỗ trợ cải thiện thông qua so sánh planned và actual. |

To-do App hữu ích trong việc ghi nhớ việc cần làm. Tuy nhiên, các ứng dụng này thường không trả lời được câu hỏi người dùng có đủ thời gian hoặc tiền bạc để hoàn thành công việc một cách hợp lý hay không. LifeBalance mở rộng quản lý công việc bằng cách đặt năng lực nguồn lực và hiệu quả vào trung tâm.

### 8.3 So sánh với Task Manager

| Khía cạnh | Task Manager thông thường | LifeBalance |
|---|---|---|
| Tổ chức công việc | Tổ chức theo danh sách, bảng, dự án, trạng thái hoặc người phụ trách. | Tổ chức công việc cùng với vốn nguồn lực, phân bổ, sử dụng thực tế và đánh giá. |
| Cộng tác | Có thể hỗ trợ phân công và luồng công việc nhóm. | Tập trung chủ yếu vào quản lý nguồn lực cá nhân. |
| Đo lường | Thường theo trạng thái và deadline. | Theo dõi kế hoạch nguồn lực, phân bổ, thực tế, sai lệch và hiệu quả. |
| Hỗ trợ quyết định | Giúp tổ chức việc cần làm. | Giúp quyết định việc sử dụng nguồn lực có hợp lý và bền vững hay không. |

Task Manager thường được thiết kế cho điều phối công việc hoặc quản lý tiến độ. LifeBalance tập trung vào quyết định cá nhân về việc phân bổ thời gian và tiền bạc hữu hạn.

### 8.4 So sánh với Productivity App

| Khía cạnh | Productivity App | LifeBalance |
|---|---|---|
| Cam kết giá trị | Cải thiện tập trung, thói quen, ghi chú, lịch hoặc hiệu suất cá nhân. | Cải thiện lập kế hoạch và đánh giá dựa trên nguồn lực. |
| Phạm vi | Có thể tập trung vào một phương pháp năng suất cụ thể. | Cung cấp mô hình quản trị nguồn lực xuyên suốt từ lập kế hoạch đến đánh giá. |
| Yếu tố tiền bạc | Thường không phải trọng tâm. | Tiền bạc là nguồn lực cốt lõi cùng với thời gian. |
| Góc nhìn hiệu quả | Có thể dựa trên streak, số việc hoàn thành hoặc thời lượng tập trung. | Nhấn mạnh sai lệch planned-actual và hiệu quả nguồn lực. |

Productivity App có thể giúp người dùng tập trung hoặc xây dựng thói quen. LifeBalance tạo ra giá trị khác biệt bằng cách xem năng suất là kết quả của phân bổ nguồn lực và đánh giá hiệu quả có cơ sở.

### 8.5 Điểm khác biệt chính

Các đặc điểm khác biệt của LifeBalance gồm:

- Góc nhìn ưu tiên nguồn lực.
- Tích hợp thời gian và tiền bạc như hai nguồn lực lập kế hoạch cốt lõi.
- Vòng đời công việc từ Planning đến Evaluation.
- So sánh planned và actual.
- Báo cáo định hướng hiệu quả.
- Hỗ trợ cải thiện liên tục dựa trên dữ liệu lịch sử.
- Nhận diện vai trò vận hành theo hướng doanh nghiệp gồm Guest, User, Staff và Admin.

## 9. Business Goals

### 9.1 Short-term Goals

Các mục tiêu ngắn hạn dự kiến đạt được trong giai đoạn phát hành ban đầu hoặc giai đoạn ứng dụng sớm.

| Goal | Mô tả | KPI gợi ý |
|---|---|---|
| Xác lập định vị sản phẩm | Định vị LifeBalance là hệ thống quản lý công việc dựa trên nguồn lực. | Thông điệp sản phẩm và thuật ngữ nhất quán trong tài liệu yêu cầu. |
| Hỗ trợ nhận thức nguồn lực cơ bản | Giúp người dùng hiểu thời gian và tiền bạc hiện có ở mức nghiệp vụ. | Tỷ lệ người dùng chủ động khai báo hoặc thiết lập vốn nguồn lực ban đầu. |
| Hỗ trợ lập kế hoạch công việc có nguồn lực | Khuyến khích người dùng lập kế hoạch công việc kèm thời gian và tiền bạc dự kiến. | Tỷ lệ công việc có giá trị planned time hoặc planned money. |
| Hỗ trợ đánh giá planned và actual | Cho phép so sánh nguồn lực dự kiến và nguồn lực thực tế. | Tỷ lệ công việc hoàn thành có ghi nhận actual usage. |
| Cung cấp dashboard nền tảng | Hiển thị tổng quan trạng thái công việc, phân bổ nguồn lực và chỉ báo hiệu quả. | Tỷ lệ người dùng hoạt động truy cập dashboard. |

### 9.2 Mid-term Goals

Các mục tiêu trung hạn dự kiến đạt được sau khi hành vi cốt lõi của sản phẩm đã được thiết lập.

| Goal | Mô tả | KPI gợi ý |
|---|---|---|
| Cải thiện độ chính xác lập kế hoạch | Giúp người dùng ước lượng nguồn lực chính xác hơn theo thời gian. | Giảm sai lệch trung bình giữa planned và actual. |
| Tăng khả năng giữ chân người dùng | Làm cho mô hình lập kế hoạch nguồn lực đủ hữu ích để người dùng quay lại thường xuyên. | Tỷ lệ retention 30 ngày, tần suất sử dụng chủ động. |
| Tăng giá trị báo cáo | Cung cấp insight lịch sử theo category, tag, kỳ thời gian và loại nguồn lực. | Tỷ lệ sử dụng báo cáo và truy cập báo cáo lặp lại. |
| Củng cố vận hành và quản trị | Hỗ trợ Staff và Admin trong hoạt động hỗ trợ và quản trị nền tảng ở mức nghiệp vụ. | Thời gian xử lý yêu cầu hỗ trợ, thời gian xử lý nghiệp vụ quản trị. |
| Cải thiện nhận thức cá nhân | Giúp người dùng hiểu mô hình tiêu thụ thời gian và tiền bạc của mình. | Tỷ lệ người dùng xem đánh giá hoặc báo cáo định kỳ. |

### 9.3 Long-term Goals

Các mục tiêu dài hạn thể hiện định hướng chiến lược của sản phẩm.

| Goal | Mô tả | KPI gợi ý |
|---|---|---|
| Trở thành nền tảng quản trị nguồn lực cá nhân | Định vị LifeBalance như hệ thống quản lý nguồn lực cá nhân lâu dài. | Retention dài hạn và tần suất sử dụng lặp lại. |
| Hỗ trợ cải thiện cá nhân liên tục | Giúp người dùng cải thiện lập kế hoạch, thực thi và đánh giá qua nhiều chu kỳ. | Xu hướng giảm variance và tăng tỷ lệ hoàn thành có đánh giá. |
| Mở rộng năng lực insight | Cung cấp phân tích sâu hơn về hành vi sử dụng nguồn lực cá nhân. | Tỷ lệ sử dụng báo cáo nâng cao hoặc tính năng insight. |
| Phục vụ nhiều phân khúc người dùng | Hỗ trợ student, office worker, freelancer và các nhóm cá nhân khác. | Retention và satisfaction theo từng phân khúc. |
| Xây dựng quản trị vận hành đáng tin cậy | Duy trì vai trò, quy trình hỗ trợ và quản trị rõ ràng. | Mức hài lòng hỗ trợ và giảm sự cố vận hành. |

## 10. Business Objectives

Business Objectives chuyển hóa tầm nhìn và mục tiêu kinh doanh thành các kết quả nghiệp vụ cụ thể. Các objective này chưa phải là use case chi tiết và không mô tả thiết kế kỹ thuật.

| Objective ID | Business Objective | Mô tả |
|---|---|---|
| BO-01 | Thiết lập khả năng nhìn thấy vốn nguồn lực | Hệ thống cần giúp người dùng hiểu lượng thời gian và tiền bạc có thể sử dụng cho lập kế hoạch và thực hiện. |
| BO-02 | Hỗ trợ lập kế hoạch công việc có xét nguồn lực | Hệ thống cần hỗ trợ quan điểm nghiệp vụ rằng công việc nên được lập kế hoạch với nguồn lực dự kiến. |
| BO-03 | Hỗ trợ phân bổ nguồn lực cho công việc | Hệ thống cần cho phép người dùng gắn cam kết nguồn lực với công việc để nỗ lực và chi phí dự kiến trở nên rõ ràng. |
| BO-04 | Ghi nhận nguồn lực thực tế đã sử dụng | Hệ thống cần hỗ trợ ghi nhận hoặc phản ánh thời gian và tiền bạc thực tế đã dùng trong hoặc sau khi thực hiện công việc. |
| BO-05 | So sánh planned và actual | Hệ thống cần giúp người dùng nhận diện chênh lệch giữa nguồn lực dự kiến và nguồn lực thực tế. |
| BO-06 | Đánh giá hiệu quả công việc | Hệ thống cần hỗ trợ đánh giá nghiệp vụ về việc nguồn lực được sử dụng hiệu quả, chấp nhận được hay có vấn đề. |
| BO-07 | Cung cấp khả năng nhìn thấy timeline | Hệ thống cần giúp người dùng hiểu thời điểm công việc được lập kế hoạch, thực hiện, trì hoãn, hoàn thành hoặc đánh giá. |
| BO-08 | Hỗ trợ category và tag | Hệ thống cần giúp người dùng tổ chức công việc và nguồn lực theo các phân loại có ý nghĩa. |
| BO-09 | Cung cấp dashboard tổng quan | Hệ thống cần cung cấp góc nhìn tổng quan về nguồn lực, tiến độ công việc và chỉ số hiệu quả. |
| BO-10 | Cung cấp năng lực reporting | Hệ thống cần hỗ trợ phân tích lịch sử phục vụ cải thiện và ra quyết định. |
| BO-11 | Hỗ trợ identity và authorization ở mức nghiệp vụ | Hệ thống cần phân biệt quyền hạn và trách nhiệm giữa Guest, User, Staff và Admin. |
| BO-12 | Hỗ trợ administration | Hệ thống cần có năng lực nghiệp vụ cho quản trị vận hành nền tảng. |
| BO-13 | Hỗ trợ user support | Hệ thống cần hỗ trợ quy trình trợ giúp, hướng dẫn và xử lý vấn đề của người dùng. |
| BO-14 | Thúc đẩy cải thiện liên tục | Hệ thống cần khuyến khích người dùng học từ lịch sử sử dụng nguồn lực để lập kế hoạch tốt hơn. |

## 11. Success Criteria

Thành công của LifeBalance cần được đánh giá theo cả kết quả nghiệp vụ, hành vi người dùng và chất lượng dự án.

### 11.1 Tiêu chí thành công về nghiệp vụ

| Tiêu chí | Mô tả |
|---|---|
| Định vị sản phẩm rõ ràng | Các bên liên quan hiểu LifeBalance là hệ thống quản lý nguồn lực cá nhân, không chỉ là to-do list. |
| Người dùng áp dụng quy trình nguồn lực | Người dùng thường xuyên lập kế hoạch, phân bổ, theo dõi và đánh giá nguồn lực cho công việc. |
| Độ chính xác lập kế hoạch được cải thiện | Sai lệch giữa planned và actual có xu hướng giảm theo thời gian. |
| Dashboard được sử dụng có ý nghĩa | Người dùng dùng dashboard để hiểu tình trạng nguồn lực và hiệu quả công việc. |
| Báo cáo hỗ trợ ra quyết định | Người dùng có thể diễn giải báo cáo và điều chỉnh kế hoạch tương lai. |
| Vai trò vận hành rõ ràng | Staff và Admin có trách nhiệm nghiệp vụ đủ rõ để hỗ trợ vận hành. |
| Phạm vi được kiểm soát | Các volume sau không đưa thêm mục tiêu ngoài định hướng của tài liệu này nếu chưa được phê duyệt. |

### 11.2 Tiêu chí thành công đối với người dùng

| Tiêu chí | Mô tả |
|---|---|
| Người dùng biết nguồn lực hiện có | Người dùng có thể xác định lượng thời gian và tiền bạc khả dụng cho kế hoạch. |
| Người dùng phân bổ nguồn lực trước khi thực hiện | Người dùng có thể gán nguồn lực dự kiến cho công việc trước hoặc trong quá trình chuẩn bị. |
| Người dùng hiểu mức sử dụng thực tế | Người dùng biết công việc đã tiêu tốn bao nhiêu thời gian và tiền bạc. |
| Người dùng hiểu variance | Người dùng thấy được chênh lệch giữa kế hoạch và thực tế. |
| Người dùng cải thiện kế hoạch tương lai | Người dùng sử dụng insight lịch sử để lập kế hoạch thực tế hơn. |
| Người dùng cảm thấy kiểm soát tốt hơn | Người dùng giảm mơ hồ và tăng khả năng kiểm soát cam kết cá nhân. |

### 11.3 Tiêu chí thành công của dự án

| Tiêu chí | Mô tả |
|---|---|
| Truy vết yêu cầu | Yêu cầu chi tiết trong các volume sau có thể truy vết về mục tiêu và capability trong tài liệu này. |
| Đồng thuận stakeholder | Các bên liên quan thống nhất về tầm nhìn, phạm vi, nguyên tắc, actor và thuật ngữ. |
| Phù hợp học thuật | Tài liệu thể hiện phương pháp phân tích yêu cầu chuyên nghiệp, phù hợp báo cáo tốt nghiệp. |
| Sẵn sàng cho môi trường doanh nghiệp | Cấu trúc và nội dung tài liệu phù hợp với thực hành phân tích nghiệp vụ doanh nghiệp. |

## 12. Stakeholders

Stakeholder là cá nhân hoặc nhóm có lợi ích, trách nhiệm, ảnh hưởng hoặc phụ thuộc đối với hệ thống LifeBalance. Mô tả dưới đây ở cấp nghiệp vụ và không định nghĩa quyền kỹ thuật chi tiết.

| Stakeholder | Description | Responsibility | Expectation |
|---|---|---|---|
| Nhà tài trợ dự án | Cá nhân hoặc nhóm hỗ trợ mục tiêu dự án và xác nhận tính phù hợp nghiệp vụ. | Phê duyệt định hướng, xem xét giá trị kinh doanh, cung cấp ưu tiên cấp cao. | Hệ thống có giá trị rõ ràng, phạm vi hợp lý và khả năng triển khai. |
| Product Owner hoặc Business Owner | Đại diện nhu cầu nghiệp vụ và định hướng sản phẩm. | Xác định ưu tiên, xác nhận objective, phê duyệt định hướng yêu cầu. | Sản phẩm hỗ trợ quản lý nguồn lực cá nhân và có thể phát triển lâu dài. |
| End User | Cá nhân sử dụng LifeBalance để quản lý công việc và nguồn lực. | Cung cấp phản hồi, duy trì dữ liệu kế hoạch cá nhân, sử dụng hệ thống có trách nhiệm. | Hệ thống giúp tăng rõ ràng, kỷ luật lập kế hoạch và kiểm soát nguồn lực. |
| Student User Segment | Người dùng là học sinh, sinh viên quản lý học tập, bài tập, kỳ thi, việc làm thêm và chi tiêu cá nhân. | Sử dụng LifeBalance để lập kế hoạch học tập và sinh hoạt. | Quản lý thời gian học tốt hơn và kiểm soát chi phí liên quan. |
| Office Worker Segment | Người dùng đi làm văn phòng cần cân bằng công việc, họp, phát triển bản thân và đời sống cá nhân. | Sử dụng LifeBalance để cân bằng công việc và trách nhiệm cá nhân. | Có cái nhìn rõ về workload, thời gian và ưu tiên cá nhân. |
| Freelancer Segment | Người dùng làm việc tự do, quản lý dự án khách hàng, chi phí và lịch cá nhân. | Sử dụng LifeBalance để phân bổ nguồn lực cho công việc tạo thu nhập. | Kiểm soát tốt hơn nỗ lực, chi phí và hiệu quả dự án. |
| Staff | Nhân sự vận hành hỗ trợ người dùng và xử lý vấn đề nghiệp vụ. | Tiếp nhận hỗ trợ, phân loại vấn đề, hướng dẫn người dùng, theo dõi xử lý. | Có quy trình rõ ràng và đủ thông tin để hỗ trợ hiệu quả. |
| Admin | Tác nhân quản trị chịu trách nhiệm giám sát và quản trị nền tảng ở mức nghiệp vụ. | Quản lý giám sát, chính sách vận hành, người dùng và staff ở mức phù hợp. | Có quyền hạn rõ ràng, kiểm soát tốt và khả năng quan sát vận hành. |
| Business Analyst | Người chịu trách nhiệm khai thác, phân tích, tài liệu hóa và xác minh yêu cầu. | Đảm bảo chất lượng yêu cầu, quản lý phạm vi, duy trì truy vết. | Có tầm nhìn ổn định và thuật ngữ nghiệp vụ thống nhất. |
| Development Team | Nhóm triển khai các yêu cầu đã được phê duyệt trong giai đoạn sau. | Diễn giải yêu cầu nhất quán với ý nghĩa nghiệp vụ. | Yêu cầu rõ ràng, có cấu trúc và có thể kiểm chứng. |
| Quality Assurance Team | Nhóm xác minh hệ thống đáp ứng yêu cầu đã phê duyệt. | Chuẩn bị chiến lược kiểm thử dựa trên yêu cầu và tiêu chí chấp nhận. | Hành vi mong đợi và tiêu chí thành công không mơ hồ. |
| Academic Reviewer | Người đánh giá dự án trong bối cảnh học thuật hoặc tốt nghiệp. | Đánh giá tính rõ ràng, khả thi và phương pháp tài liệu hóa. | Dự án thể hiện năng lực phân tích yêu cầu chuyên nghiệp. |

## 13. User Personas

Persona là mô hình đại diện cho các nhóm người dùng điển hình nhằm hỗ trợ phân tích nhu cầu, động lực, khó khăn và hành vi. Persona không phải là danh sách đầy đủ tất cả người dùng có thể có.

### 13.1 Persona: Student

| Thuộc tính | Mô tả |
|---|---|
| Persona Name | Minh, sinh viên đại học |
| Context | Minh học ngành Công nghệ thông tin, làm thêm bán thời gian và phải quản lý bài tập, thi cử, dự án nhóm và chi tiêu cá nhân. |

| Dimension | Detail |
|---|---|
| Goals | Lập kế hoạch học tập thực tế; phân bổ thời gian cho bài tập và ôn thi; kiểm soát chi phí học tập, đi lại và sinh hoạt; giảm áp lực deadline. |
| Frustrations | Thường đánh giá thấp thời gian học; quên các việc nhỏ; chi tiêu không gắn với ưu tiên học tập; bị quá tải vào mùa thi. |
| Behaviors | Dùng lịch, nhóm chat, ghi chú và danh sách việc cần làm; thường điều chỉnh kế hoạch; chỉ xem lại tiến độ khi gần deadline. |
| Needs | Cách đơn giản để lập kế hoạch kèm ước lượng thời gian; nhìn thấy thời gian học khả dụng; so sánh planned và actual study time; phân loại theo môn học hoặc hoạt động; theo dõi chi phí liên quan học tập. |

### 13.2 Persona: Office Worker

| Thuộc tính | Mô tả |
|---|---|
| Persona Name | Lan, nhân viên văn phòng |
| Context | Lan làm việc toàn thời gian, đồng thời phải cân bằng họp hành, công việc dự án, trách nhiệm gia đình, sức khỏe và phát triển bản thân. |

| Dimension | Detail |
|---|---|
| Goals | Duy trì kế hoạch ngày và tuần thực tế; tránh quá tải; hiểu thời gian dành cho công việc và cá nhân; giữ thời gian cho mục tiêu quan trọng nhưng không khẩn cấp. |
| Frustrations | Công việc phát sinh kéo dài hơn dự kiến; việc cá nhân bị trì hoãn; họp chiếm nhiều thời gian; chi tiêu hằng ngày không gắn với kế hoạch. |
| Behaviors | Dùng lịch công việc và nhắc việc cá nhân riêng; theo dõi một phần chi tiêu bằng ứng dụng tài chính; thường tổng kết vào cuối ngày. |
| Needs | Góc nhìn thống nhất về cam kết công việc; so sánh planned time và actual time; insight theo work, family, health, learning; dashboard thể hiện workload và capacity. |

### 13.3 Persona: Freelancer

| Thuộc tính | Mô tả |
|---|---|
| Persona Name | Huy, freelancer |
| Context | Huy quản lý dự án khách hàng, báo giá, công việc hành chính, học kỹ năng mới và trách nhiệm tài chính cá nhân. |

| Dimension | Detail |
|---|---|
| Goals | Phân bổ thời gian cho công việc tạo thu nhập; kiểm soát chi phí dự án; đánh giá công việc có đáng với nỗ lực hay không; cải thiện khả năng ước lượng cho dự án tương lai. |
| Frustrations | Công việc khách hàng thường kéo dài hơn dự kiến; công việc gián tiếp không được ghi nhận; chi phí dự án phân tán; hoạt động cá nhân và công việc chồng lấn. |
| Behaviors | Dùng spreadsheet, to-do list, time tracker và hóa đơn riêng lẻ; ước lượng effort theo kinh nghiệm; thiếu dữ liệu lịch sử nhất quán. |
| Needs | Lập kế hoạch thời gian và tiền theo từng task; theo dõi actual usage; báo cáo theo client, category hoặc tag; phân tích variance; nhìn thấy non-billable effort. |

### 13.4 Persona: Admin

| Thuộc tính | Mô tả |
|---|---|
| Persona Name | System Admin |
| Context | Admin chịu trách nhiệm quản trị và giám sát vận hành nền tảng LifeBalance ở mức nghiệp vụ. |

| Dimension | Detail |
|---|---|
| Goals | Đảm bảo vận hành có kiểm soát; giám sát người dùng và staff; duy trì tính nhất quán quản trị; hỗ trợ tính liên tục của dịch vụ. |
| Frustrations | Thiếu khả năng quan sát vấn đề vận hành; ranh giới trách nhiệm không rõ; xử lý hỗ trợ thiếu nhất quán. |
| Behaviors | Xem thông tin quản trị; theo dõi hoạt động cấp nền tảng ở mức phù hợp; phối hợp với Staff khi cần xử lý vấn đề leo thang. |
| Needs | Định nghĩa rõ vai trò Admin; giám sát người dùng và Staff; chỉ báo vận hành; quy tắc quản trị không can thiệp quá mức vào dữ liệu cá nhân của người dùng. |

### 13.5 Persona: Staff

| Thuộc tính | Mô tả |
|---|---|
| Persona Name | Support Staff |
| Context | Staff hỗ trợ người dùng thông qua câu hỏi, yêu cầu hỗ trợ, hướng dẫn sử dụng và xử lý vấn đề vận hành. |

| Dimension | Detail |
|---|---|
| Goals | Giải quyết vấn đề người dùng hiệu quả; cung cấp hướng dẫn chính xác; leo thang khi cần; duy trì chất lượng hỗ trợ. |
| Frustrations | Thiếu ngữ cảnh cho yêu cầu hỗ trợ; tiêu chí leo thang chưa rõ; câu hỏi lặp lại; khó phân biệt lỗi hệ thống và hiểu nhầm của người dùng. |
| Behaviors | Xem yêu cầu hỗ trợ; giao tiếp với người dùng; tuân thủ quy trình hỗ trợ; phối hợp với Admin cho vấn đề cần quyết định quản trị. |
| Needs | Quy trình hỗ trợ rõ ràng; phân loại yêu cầu; trạng thái xử lý; quy tắc leo thang; tài liệu hướng dẫn nhất quán. |

## 14. Actors

Actor là vai trò bên ngoài tương tác với hệ thống ở mức nghiệp vụ. Phần này không mô tả use case chi tiết.

### 14.1 Guest

Guest là cá nhân chưa xác thực hoặc chưa đăng ký, có thể tiếp cận các thông tin công khai hoặc giới thiệu về LifeBalance. Actor này đại diện cho người dùng tiềm năng đang tìm hiểu hệ thống trước khi trở thành User.

Ở mức nghiệp vụ, Guest cần hiểu mục đích của LifeBalance, giá trị của lập kế hoạch nguồn lực và điều kiện để trở thành người dùng đã đăng ký. Guest chưa phải là chủ thể quản lý nguồn lực cá nhân đầy đủ vì việc quản lý resource capital, task, allocation, execution và evaluation cần gắn với danh tính người dùng.

### 14.2 User

User là cá nhân đã đăng ký sử dụng LifeBalance để quản lý nguồn lực và công việc cá nhân. Đây là actor nghiệp vụ chính của hệ thống.

User chịu trách nhiệm duy trì thông tin kế hoạch cá nhân, xác định hoặc cập nhật vốn nguồn lực, lập kế hoạch công việc, phân bổ nguồn lực, theo dõi sử dụng thực tế, xem timeline và đánh giá hiệu quả. User kỳ vọng hệ thống cung cấp sự rõ ràng, tổ chức và insight về cách thời gian và tiền bạc được sử dụng.

### 14.3 Staff

Staff là nhân sự vận hành hỗ trợ việc sử dụng nền tảng LifeBalance. Staff có thể hỗ trợ câu hỏi của người dùng, tiếp nhận yêu cầu hỗ trợ, phân loại vấn đề, cung cấp hướng dẫn và theo dõi xử lý.

Staff không phải là người quyết định mục tiêu năng suất cá nhân của User. Vai trò của Staff là hỗ trợ chất lượng dịch vụ và quy trình vận hành ở mức nghiệp vụ.

### 14.4 Admin

Admin là actor quản trị chịu trách nhiệm giám sát, kiểm soát và quản trị nền tảng ở mức nghiệp vụ. Trách nhiệm của Admin có thể bao gồm giám sát người dùng, Staff, chính sách vận hành và các hoạt động quản trị.

Admin được kỳ vọng duy trì khả năng kiểm soát vận hành và bảo đảm LifeBalance được quản lý một cách đáng tin cậy. Tài liệu này không định nghĩa chi tiết quyền kỹ thuật, thiết kế phân quyền hoặc thiết kế màn hình quản trị.

## 15. Business Scope

### 15.1 In Scope

| Scope Area | Mô tả |
|---|---|
| Identity and Authorization | Nhận diện nghiệp vụ các actor Guest, User, Staff và Admin. |
| Resource Capital Management | Quản lý thời gian và tiền bạc cá nhân như vốn nguồn lực dùng cho lập kế hoạch. |
| Task Management | Quản lý công việc như các hạng mục tiêu tốn nguồn lực. |
| Resource Allocation | Phân bổ thời gian và tiền bạc dự kiến cho công việc. |
| Execution Tracking | Theo dõi tiến độ công việc và nguồn lực thực tế đã sử dụng ở mức nghiệp vụ. |
| Timeline | Thể hiện thời điểm lập kế hoạch, thực hiện, trì hoãn, hoàn thành và đánh giá công việc. |
| Category | Phân loại công việc và nguồn lực theo nhóm nghiệp vụ có ý nghĩa. |
| Tag | Gắn nhãn linh hoạt để tổ chức và phân tích công việc. |
| Dashboard | Cung cấp tổng quan về nguồn lực, trạng thái công việc và chỉ số hiệu quả. |
| Reporting | Cung cấp insight lịch sử về kế hoạch, phân bổ, sử dụng thực tế, variance và efficiency. |
| Administration | Hỗ trợ quản trị vận hành ở mức nghiệp vụ bởi Admin. |
| Support | Hỗ trợ yêu cầu trợ giúp, hướng dẫn và giao tiếp vận hành với người dùng. |

### 15.2 Out of Scope

| Out-of-Scope Area | Giải thích |
|---|---|
| Database Design | Tài liệu không định nghĩa bảng, cột, khóa, quan hệ hoặc cấu trúc lưu trữ. |
| ERD | Không bao gồm sơ đồ thực thể liên kết trong volume này. |
| API Design | Không định nghĩa endpoint, payload, giao thức hoặc hợp đồng tích hợp. |
| Microservice Design | Không định nghĩa tách dịch vụ, mô hình triển khai hoặc service boundary. |
| System Architecture | Không định nghĩa kiến trúc, hạ tầng, lớp ứng dụng, thành phần hoặc hosting. |
| User Interface Design | Không định nghĩa màn hình, layout, wireframe hoặc thiết kế giao diện. |
| Source Code | Không bao gồm mã nguồn hoặc hướng dẫn triển khai. |
| Detailed Use Case Specification | Use case chi tiết sẽ thuộc các volume sau. |
| Financial Accounting System | LifeBalance không phải hệ thống kế toán, thuế hoặc sổ sách tài chính đầy đủ. |
| Enterprise Project Management Suite | LifeBalance không phải nền tảng quản lý dự án doanh nghiệp theo nhóm ở phạm vi đầy đủ. |
| Professional Advisory | Hệ thống không cung cấp tư vấn chuyên môn về tài chính, pháp lý, y tế hoặc tâm lý. |

### 15.3 Future Scope

| Future Scope Area | Mô tả |
|---|---|
| Advanced Recommendation | Gợi ý điều chỉnh kế hoạch dựa trên lịch sử variance và hành vi người dùng. |
| Habit and Goal Integration | Kết nối thói quen lặp lại hoặc mục tiêu dài hạn với lập kế hoạch nguồn lực. |
| External Calendar Integration | Đồng bộ công việc hoặc timeline với dịch vụ lịch bên ngoài. |
| Expense Source Integration | Kết nối nguồn dữ liệu tài chính bên ngoài nếu đáp ứng yêu cầu bảo mật và pháp lý. |
| Team or Household Planning | Mở rộng quản lý nguồn lực cho nhóm nhỏ, gia đình hoặc hộ gia đình. |
| Advanced Analytics | Phân tích xu hướng, dự báo và insight cá nhân hóa ở mức sâu hơn. |
| Gamification | Bổ sung cơ chế động lực nếu không làm suy yếu triết lý quản trị nguồn lực. |

## 16. Business Capability Map

Business Capability Map mô tả các năng lực nghiệp vụ cấp cao mà LifeBalance cần hỗ trợ. Đây không phải là mô tả module kỹ thuật, kiến trúc hệ thống, bảng dữ liệu hoặc dịch vụ phần mềm.

### 16.1 Identity

| Capability | Mô tả |
|---|---|
| Actor Recognition | Nhận diện các actor nghiệp vụ gồm Guest, User, Staff và Admin. |
| User Registration Orientation | Hỗ trợ nhu cầu nghiệp vụ để cá nhân trở thành User trước khi quản lý nguồn lực cá nhân. |
| Authentication Orientation | Bảo đảm thông tin nguồn lực và công việc cá nhân gắn với một danh tính người dùng. |
| Authorization Orientation | Hỗ trợ ranh giới trách nhiệm khác nhau giữa User, Staff và Admin. |
| Account Governance | Hỗ trợ quản lý trạng thái tài khoản và giám sát quản trị ở mức nghiệp vụ. |

Identity là năng lực cần thiết vì dữ liệu nguồn lực cá nhân có tính nhạy cảm và trách nhiệm giữa các actor khác nhau. Hệ thống phải tạo nền tảng tin cậy trước khi người dùng quản lý thời gian, tiền bạc, task và yêu cầu hỗ trợ.

### 16.2 Resource

| Capability | Mô tả |
|---|---|
| Resource Capital Definition | Hỗ trợ khái niệm thời gian và tiền bạc khả dụng như vốn nguồn lực. |
| Resource Availability Visibility | Giúp người dùng thấy nguồn lực khả dụng cho từng kỳ kế hoạch. |
| Resource Allocation | Hỗ trợ phân bổ nguồn lực dự kiến cho công việc. |
| Actual Resource Recording | Hỗ trợ ghi nhận thời gian và tiền bạc thực tế đã tiêu tốn. |
| Resource Variance Awareness | Hỗ trợ so sánh giữa planned resource usage và actual resource usage. |
| Resource Efficiency Evaluation | Hỗ trợ đánh giá mức độ hiệu quả hoặc hợp lý của việc sử dụng nguồn lực. |

Resource là capability khác biệt cốt lõi của LifeBalance. Năng lực này biến task từ mục ghi nhớ thành cam kết nguồn lực có thể theo dõi và đánh giá.

### 16.3 Task

| Capability | Mô tả |
|---|---|
| Task Definition | Hỗ trợ biểu diễn công việc hoặc hoạt động cần lập kế hoạch và thực hiện. |
| Task Planning | Hỗ trợ xác định kết quả kỳ vọng, ưu tiên, thời điểm và nguồn lực dự kiến ở mức nghiệp vụ. |
| Task Status Tracking | Hỗ trợ nhìn thấy tiến độ công việc qua các trạng thái vòng đời phù hợp. |
| Task Classification | Hỗ trợ phân loại qua category và tag. |
| Task Evaluation | Hỗ trợ đánh giá sau thực hiện dựa trên planned và actual resource usage. |
| Task History | Hỗ trợ xem lại công việc đã hoàn thành, trì hoãn, hủy hoặc đã đánh giá để học hỏi. |

Task capability phải luôn gắn với triết lý nguồn lực. Công việc không chỉ là checkbox, mà là cam kết có chi phí, nỗ lực, thời điểm và kết quả.

### 16.4 Timeline

| Capability | Mô tả |
|---|---|
| Planning Period View | Hỗ trợ hiểu các công việc đã lập kế hoạch theo ngày, tuần, tháng hoặc kỳ phù hợp. |
| Execution Time Visibility | Hỗ trợ nhìn thấy thời điểm công việc được thực hiện. |
| Historical Activity View | Hỗ trợ xem lại công việc và nguồn lực trong quá khứ. |
| Delay Awareness | Hỗ trợ nhận diện công việc vượt quá thời điểm kỳ vọng. |
| Evaluation Timing | Hỗ trợ nhận biết khi công việc đã hoàn thành và cần được đánh giá. |

Timeline giúp người dùng hiểu chiều thời gian của việc sử dụng nguồn lực. Vì thời gian là một nguồn lực cốt lõi, khả năng nhìn thấy timeline là cần thiết cho lập kế hoạch và đánh giá.

### 16.5 Reporting

| Capability | Mô tả |
|---|---|
| Summary Reporting | Cung cấp tổng hợp về hoàn thành công việc, phân bổ nguồn lực và sử dụng thực tế. |
| Variance Reporting | Cung cấp thông tin về chênh lệch giữa kế hoạch và thực tế. |
| Category-Based Reporting | Hỗ trợ phân tích theo category như học tập, công việc, sức khỏe, tài chính hoặc phát triển cá nhân. |
| Tag-Based Reporting | Hỗ trợ phân tích linh hoạt theo tag. |
| Period-Based Reporting | Hỗ trợ phân tích theo kỳ thời gian. |
| Efficiency Reporting | Hỗ trợ insight về mức độ hiệu quả trong sử dụng nguồn lực. |

Reporting là năng lực quan trọng vì LifeBalance hướng đến cải thiện liên tục. Nếu không có báo cáo, dữ liệu có thể được ghi nhận nhưng không tạo ra học hỏi.

### 16.6 Administration

| Capability | Mô tả |
|---|---|
| User Oversight | Hỗ trợ giám sát tài khoản và sự tham gia của người dùng ở mức nghiệp vụ. |
| Staff Oversight | Hỗ trợ quản lý trách nhiệm và hoạt động của Staff. |
| Governance Support | Hỗ trợ chính sách quản trị và kiểm soát vận hành. |
| Operational Monitoring | Hỗ trợ nhìn thấy các hoạt động nền tảng có liên quan đến quản trị. |
| Issue Escalation Oversight | Hỗ trợ Admin xem xét các vấn đề hỗ trợ cần quyết định cấp cao hơn. |

Administration bảo đảm nền tảng có thể được vận hành có trách nhiệm, không chỉ được sử dụng ở cấp cá nhân.

### 16.7 Support

| Capability | Mô tả |
|---|---|
| User Assistance | Hỗ trợ người dùng tìm kiếm trợ giúp hoặc hướng dẫn. |
| Support Request Management | Hỗ trợ phân loại, theo dõi trạng thái và xử lý yêu cầu hỗ trợ ở mức nghiệp vụ. |
| Issue Communication | Hỗ trợ giao tiếp giữa người dùng và Staff. |
| Escalation | Hỗ trợ leo thang vấn đề phức tạp hoặc liên quan chính sách lên Admin. |
| Knowledge Guidance | Hỗ trợ hướng dẫn nhất quán cho câu hỏi hoặc khó khăn thường gặp. |

Support cần thiết cho việc chấp nhận sản phẩm và chất lượng vận hành. Người dùng có thể cần hỗ trợ để hiểu khái niệm lập kế hoạch nguồn lực hoặc xử lý vấn đề tài khoản và sử dụng.

## 17. High-Level Business Process

LifeBalance tuân theo một quy trình nghiệp vụ cấp cao phản ánh vòng đời của công việc có xét đến nguồn lực.

Planning

↓

Allocation

↓

Execution

↓

Tracking

↓

Evaluation

↓

Improvement

### 17.1 Planning

Planning là giai đoạn người dùng xác định việc cần làm, lý do cần làm và thời điểm cần thực hiện. Ở giai đoạn này, task được định nghĩa như một cam kết tương lai. Planning có thể bao gồm mức ưu tiên, category, tag, deadline, kết quả kỳ vọng và ước lượng nguồn lực cần thiết.

Mục đích nghiệp vụ của Planning là chuyển hóa ý định thành cam kết có cấu trúc. Một ý định mơ hồ như "học nhiều hơn" trở nên dễ quản lý hơn khi được gắn với phạm vi, thời điểm và nguồn lực dự kiến.

### 17.2 Allocation

Allocation là giai đoạn người dùng gán nguồn lực dự kiến cho task. Hai nguồn lực chính là time và money. Allocation trả lời câu hỏi: "Công việc này dự kiến cần bao nhiêu nguồn lực?"

Allocation hỗ trợ ra quyết định có trách nhiệm. Nếu người dùng có thời gian hoặc tiền bạc hạn chế, giai đoạn này giúp nhận diện kế hoạch có thực tế hay không. Allocation cũng tạo đường cơ sở để so sánh với actual usage ở giai đoạn sau.

### 17.3 Execution

Execution là giai đoạn người dùng thực hiện task. Khi đó task chuyển từ kế hoạch sang hành động. Vai trò nghiệp vụ của hệ thống là hỗ trợ người dùng nhìn thấy tiến độ và nguồn lực đang được sử dụng.

Execution quan trọng vì thực tế không phải lúc nào cũng khớp với kế hoạch. Một công việc có thể mất nhiều thời gian hơn, cần chi phí cao hơn, bị gián đoạn hoặc không còn phù hợp. Hệ thống cần hỗ trợ người dùng duy trì nhận thức trong giai đoạn này.

### 17.4 Tracking

Tracking là giai đoạn ghi nhận tiến độ và nguồn lực thực tế. Tracking trả lời các câu hỏi:

- Thực tế đã sử dụng bao nhiêu thời gian?
- Thực tế đã chi bao nhiêu tiền?
- Công việc đã hoàn thành, trì hoãn, hủy hay đang tiếp tục?
- Quá trình thực hiện có khác kế hoạch ban đầu hay không?

Tracking tạo cơ sở dữ liệu thực tế cho Evaluation. Nếu không có actual usage, người dùng không thể xác định kế hoạch có chính xác hay nguồn lực có được dùng hiệu quả hay không.

### 17.5 Evaluation

Evaluation là giai đoạn người dùng xem xét kết quả công việc và so sánh planned resource usage với actual resource usage. Evaluation có thể xem xét trạng thái hoàn thành, time variance, money variance, priority, category và mức độ hiệu quả cảm nhận.

Mục đích của Evaluation không phải là phán xét người dùng. Mục đích là tạo insight. Một task vượt planned time có thể cho thấy ước lượng ban đầu quá thấp. Một task dùng ít tiền hơn dự kiến có thể cho thấy hiệu quả tốt. Một task bị hủy có thể phản ánh ưu tiên chưa phù hợp hoặc hoàn cảnh thay đổi.

### 17.6 Improvement

Improvement là giai đoạn người dùng áp dụng bài học từ lịch sử vào kế hoạch tương lai. Dữ liệu lịch sử giúp người dùng ước lượng chính xác hơn, tránh quá tải lặp lại, ưu tiên nguồn lực cho hoạt động có giá trị cao hơn và giảm lãng phí.

Improvement khép lại chu trình nghiệp vụ. LifeBalance không chỉ hỗ trợ thực hiện từng công việc riêng lẻ mà còn hỗ trợ hình thành năng lực lập kế hoạch tốt hơn theo thời gian.

## 18. Business Principles

Business Principles là các nguyên tắc vận hành định hướng quyết định yêu cầu nghiệp vụ.

| Principle | Định nghĩa | Hàm ý nghiệp vụ |
|---|---|---|
| Resource Awareness | Thời gian và tiền bạc phải được xem là nguồn lực hữu hạn. | Lập kế hoạch công việc cần xét đến nguồn lực khả dụng và nguồn lực dự kiến tiêu tốn. |
| Task as Investment | Công việc là cam kết tiêu tốn nguồn lực và cần tạo ra giá trị. | Hoàn thành công việc chưa đủ để đánh giá hiệu quả. |
| Planned vs Actual Discipline | Planned value và actual value cần được phân biệt rõ. | Hệ thống cần hỗ trợ variance analysis và học hỏi. |
| User Ownership | Người dùng chịu trách nhiệm với kế hoạch, nguồn lực và đánh giá cá nhân. | Hệ thống hỗ trợ quyết định nhưng không thay thế trách nhiệm cá nhân. |
| Clarity Over Complexity | Khái niệm nghiệp vụ cần dễ hiểu với người dùng thông thường. | Lập kế hoạch nguồn lực cần thực tế, tránh gây gánh nặng quá mức. |
| Continuous Improvement | Kết quả lịch sử cần hỗ trợ quyết định tương lai tốt hơn. | Báo cáo và đánh giá cần hướng đến cải thiện. |
| Privacy Respect | Dữ liệu công việc, thời gian và tiền bạc là thông tin nhạy cảm. | Quyền truy cập và xử lý vận hành cần được kiểm soát trong các yêu cầu sau. |
| Operational Accountability | Staff và Admin cần có trách nhiệm rõ ràng. | Support và Administration cần có quy trình và khả năng truy vết. |
| Scope Discipline | LifeBalance cần duy trì trọng tâm quản lý nguồn lực cá nhân. | Các tính năng ngoài định hướng cần đưa vào future scope hoặc sáng kiến riêng. |
| Measurability | Kết quả quan trọng nên có khả năng đo lường nếu phù hợp. | KPI và success metrics cần được xác định cho adoption, accuracy, completion và efficiency. |

## 19. Business Constraints

### 19.1 Business Constraint

| Constraint | Mô tả | Tác động |
|---|---|---|
| Trọng tâm cá nhân | Hệ thống tập trung vào quản lý nguồn lực cá nhân thay vì quản lý dự án doanh nghiệp đầy đủ. | Yêu cầu nên ưu tiên lập kế hoạch cá nhân, không mở rộng quá sớm sang workflow nhóm phức tạp. |
| Hai loại nguồn lực cốt lõi | Mô hình ban đầu tập trung vào time và money. | Các loại nguồn lực khác nên được xem là future scope nếu chưa được phê duyệt. |
| Phụ thuộc dữ liệu người dùng | Planned và actual value phụ thuộc vào thông tin người dùng cung cấp. | Chất lượng insight phụ thuộc vào kỷ luật và độ đầy đủ dữ liệu. |
| Bối cảnh đồ án tốt nghiệp | Tài liệu cần phù hợp với yêu cầu học thuật và phương pháp phân tích yêu cầu. | Nội dung phải có cấu trúc, lập luận rõ và tránh thiết kế kỹ thuật không thuộc phạm vi. |
| Ranh giới Business Analysis | Volume này không bao gồm thiết kế kỹ thuật. | Tài liệu kỹ thuật cần được tách riêng khỏi tài liệu nghiệp vụ. |

### 19.2 Operational Constraint

| Constraint | Mô tả | Tác động |
|---|---|---|
| Năng lực hỗ trợ có giới hạn | Staff có thể bị giới hạn về số lượng và thời gian xử lý. | Quy trình hỗ trợ cần rõ ràng, ưu tiên được phân loại. |
| Kiểm soát vai trò Admin | Quyền hạn Admin cần được kiểm soát để tránh truy cập hoặc can thiệp không cần thiết. | Các volume sau cần định nghĩa nguyên tắc quản trị và ranh giới trách nhiệm. |
| Nhu cầu hướng dẫn người dùng | Người dùng có thể chưa quen với tư duy quản lý công việc dựa trên nguồn lực. | Onboarding, hướng dẫn và thuật ngữ cần rõ ràng. |
| Biến động chất lượng dữ liệu | Người dùng có thể nhập thiếu hoặc nhập không chính xác dữ liệu kế hoạch. | Reporting cần xử lý hoặc thể hiện hạn chế dữ liệu trong yêu cầu sau. |
| Thay đổi hành vi | Người dùng có thể ngại theo dõi chi tiết nếu cảm thấy tốn công. | Quy trình nghiệp vụ cần cân bằng giữa kỷ luật và tính dễ dùng. |

### 19.3 Regulatory Constraint

Các ràng buộc pháp lý dưới đây được ghi nhận ở mức nhận thức nghiệp vụ. Việc diễn giải pháp lý chi tiết nằm ngoài phạm vi tài liệu này và cần được xác nhận bởi chuyên gia pháp lý hoặc compliance nếu sản phẩm được triển khai công khai.

| Constraint | Mô tả | Tác động |
|---|---|---|
| Bảo vệ dữ liệu cá nhân | Tài khoản, task, thời gian sử dụng, thông tin chi tiêu và yêu cầu hỗ trợ có thể chứa dữ liệu cá nhân. | Yêu cầu sau cần xem xét quyền riêng tư, đồng ý, kiểm soát truy cập, lưu giữ và quyền người dùng. |
| Tính nhạy cảm của dữ liệu tài chính | Thông tin money có thể phản ánh hành vi cá nhân riêng tư. | Dữ liệu tài chính cần được xem là dữ liệu nhạy cảm. |
| Độ tuổi và điều kiện sử dụng | Nếu người dùng là người chưa thành niên, có thể cần chính sách đồng ý hoặc điều kiện sử dụng phù hợp theo khu vực. | Giả định về đối tượng sử dụng cần được xác minh trước triển khai công khai. |
| Bảo vệ người tiêu dùng | Thông điệp sản phẩm không nên cam kết kết quả tài chính, năng suất, y tế hoặc tâm lý một cách tuyệt đối. | Nội dung truyền thông và hướng dẫn cần chính xác, không gây hiểu nhầm. |

## 20. Assumptions

Assumption là giả định được chấp nhận tạm thời cho mục đích xây dựng tài liệu này. Assumption không phải là yêu cầu chính thức nếu chưa được xác minh và phê duyệt trong các hoạt động yêu cầu sau.

| Assumption ID | Assumption | Rationale |
|---|---|---|
| A-01 | Người dùng chính là cá nhân quản lý công việc và nguồn lực cá nhân. | Bối cảnh dự án mô tả quản lý nguồn lực cá nhân, không phải quản lý doanh nghiệp theo nhóm. |
| A-02 | Time và Money là hai nguồn lực cốt lõi của phạm vi ban đầu. | Bối cảnh dự án xác định rõ hai nguồn lực này. |
| A-03 | Người dùng sẵn sàng cung cấp tối thiểu thông tin planned và actual. | Đánh giá planned vs actual cần dữ liệu người dùng. |
| A-04 | Hiệu quả công việc có thể được đánh giá có ý nghĩa thông qua so sánh planned và actual. | Triết lý sản phẩm nhấn mạnh hiệu quả nguồn lực. |
| A-05 | Category và tag hữu ích cho tổ chức và phân tích nguồn lực. | Nhóm nghiệp vụ của dự án bao gồm Category và Tag. |
| A-06 | Staff và Admin là cần thiết cho vận hành và quản trị nền tảng. | Danh sách actor bao gồm Staff và Admin. |
| A-07 | Hệ thống không thay thế công cụ kế toán hoặc tư vấn tài chính chuyên nghiệp. | Dự án tập trung vào quản lý nguồn lực cá nhân. |
| A-08 | Hệ thống có thể phục vụ nhiều phân khúc như student, office worker và freelancer. | Các persona này được xác định trong yêu cầu tài liệu. |
| A-09 | Các volume sau sẽ định nghĩa yêu cầu chức năng, use case, business rule và acceptance criteria chi tiết. | Volume này là tài liệu tầm nhìn và tổng quan nghiệp vụ. |
| A-10 | Yêu cầu pháp lý sẽ được xác minh trong giai đoạn sau nếu hệ thống triển khai ngoài phạm vi học thuật hoặc prototype. | Nghĩa vụ pháp lý phụ thuộc khu vực và mô hình triển khai. |

## 21. Risks

### 21.1 Business Risks

| Risk | Mô tả | Tác động tiềm tàng | Hướng giảm thiểu |
|---|---|---|---|
| Định vị sản phẩm không rõ | Người dùng có thể xem LifeBalance như một to-do list khác. | Giảm adoption và giảm khác biệt cạnh tranh. | Duy trì thông điệp quản lý nguồn lực nhất quán và ưu tiên tính năng phù hợp. |
| Quy trình quá phức tạp | Người dùng có thể thấy planning, allocation, tracking và evaluation quá nặng. | Bỏ cuộc hoặc giảm sử dụng. | Thiết kế nghiệp vụ theo hướng thực tế và cho phép áp dụng từng bước. |
| Dữ liệu không đầy đủ | Người dùng không ghi nhận actual time hoặc actual money thường xuyên. | Báo cáo và evaluation kém giá trị. | Yêu cầu sau cần xem xét nhắc nhở, nhập liệu đơn giản và xử lý dữ liệu thiếu. |
| Hiểu sai về efficiency | Người dùng có thể hiểu hiệu quả chỉ là dùng ít thời gian hoặc ít tiền. | Công việc quan trọng nhưng tốn nguồn lực có thể bị đánh giá sai. | Định nghĩa efficiency theo ngữ cảnh, không phải phán xét tuyệt đối. |
| Mở rộng phạm vi | Stakeholder có thể yêu cầu kế toán, quản lý dự án nhóm hoặc mạng xã hội quá sớm. | Trễ tiến độ, tăng phức tạp và mất trọng tâm. | Dùng tài liệu này để kiểm soát scope và phân loại future scope. |

### 21.2 Adoption Risks

| Risk | Mô tả | Tác động tiềm tàng | Hướng giảm thiểu |
|---|---|---|---|
| Kỷ luật lập kế hoạch thấp | Người dùng không hình thành thói quen ước lượng nguồn lực trước khi làm. | Giảm giá trị allocation và evaluation. | Cung cấp luồng lập kế hoạch đơn giản và làm rõ lợi ích. |
| Tracking fatigue | Người dùng dừng ghi nhận actual usage vì thấy bất tiện. | Thiếu dữ liệu thực tế và variance analysis yếu. | Thiết kế sau cần giảm effort nhập liệu và hỗ trợ tracking linh hoạt. |
| Lo ngại quyền riêng tư | Người dùng ngại nhập thông tin thời gian và tiền bạc. | Giảm niềm tin và adoption. | Truyền đạt nguyên tắc privacy và xác định ranh giới truy cập trong yêu cầu sau. |
| Không phù hợp từng phân khúc | Student, office worker và freelancer có kỳ vọng khác nhau. | Một số nhóm có thể thấy hệ thống không liên quan. | Xác minh yêu cầu với từng persona đại diện. |
| Giá trị đến chậm | Người dùng có thể chưa thấy lợi ích ngay khi chưa có đủ lịch sử dữ liệu. | Rời bỏ sớm. | Cung cấp dashboard và insight cơ bản ngay từ giai đoạn đầu. |

### 21.3 Operational Risks

| Risk | Mô tả | Tác động tiềm tàng | Hướng giảm thiểu |
|---|---|---|---|
| Quá tải hỗ trợ | Staff nhận nhiều câu hỏi lặp lại hoặc không rõ. | Xử lý chậm và giảm hài lòng người dùng. | Định nghĩa nhóm yêu cầu hỗ trợ, nội dung hướng dẫn và quy tắc leo thang. |
| Quyền Admin mơ hồ | Trách nhiệm Admin không rõ. | Quản trị không nhất quán và tăng rủi ro vận hành. | Định nghĩa ranh giới quản trị trong các volume sau. |
| Thuật ngữ không nhất quán | Stakeholder dùng resource, allocation, budget và capital khác nghĩa. | Hiểu sai yêu cầu. | Duy trì và áp dụng business glossary. |
| Diễn giải báo cáo sai | Người dùng hiểu báo cáo mà không xét hạn chế dữ liệu. | Quyết định sai hoặc thất vọng. | Yêu cầu reporting cần thể hiện assumption và limitation. |
| Xử lý dữ liệu nhạy cảm không phù hợp | Dữ liệu thời gian và tiền bạc cá nhân có thể bị xử lý sai. | Ảnh hưởng niềm tin, compliance và danh tiếng. | Định nghĩa nguyên tắc privacy và access trong yêu cầu sau. |

## 22. Dependencies

| Dependency | Mô tả | Loại phụ thuộc |
|---|---|---|
| Stakeholder Validation | Mục tiêu, phạm vi và actor cần được stakeholder xem xét và xác nhận. | Business |
| User Research | Persona và nhu cầu cần được kiểm chứng với người dùng đại diện. | Business |
| Requirement Elicitation | Các volume sau phụ thuộc vào phỏng vấn, workshop, khảo sát hoặc phiên phân tích. | Business Analysis |
| Regulatory Review | Giả định về bảo vệ dữ liệu và quyền riêng tư có thể cần xác minh theo khu vực. | Compliance |
| Product Prioritization | Yêu cầu chi tiết phụ thuộc vào ưu tiên của Product Owner hoặc sponsor. | Governance |
| Operational Policy Definition | Capability của Staff và Admin phụ thuộc vào chính sách hỗ trợ và quản trị. | Operational |
| Measurement Definition | Success metrics phụ thuộc vào thống nhất về dữ liệu có thể thu thập và diễn giải. | Business Measurement |
| Terminology Governance | Tài liệu nhất quán phụ thuộc vào việc duy trì glossary xuyên suốt các volume. | Documentation |
| User Adoption Strategy | Thành công sản phẩm phụ thuộc vào onboarding, hướng dẫn và hình thành thói quen. | Business Adoption |

## 23. Success Metrics

Success Metrics là các chỉ số đo lường dùng để đánh giá adoption, engagement, chất lượng lập kế hoạch và giá trị nghiệp vụ. Mục tiêu định lượng cụ thể cần được xác nhận trong kế hoạch dự án và có thể thay đổi theo từng release.

### 23.1 Adoption Metrics

| Metric | Định nghĩa | Ý nghĩa nghiệp vụ |
|---|---|---|
| Registration Conversion Rate | Tỷ lệ Guest trở thành User đã đăng ký. | Cho biết giá trị sản phẩm có đủ rõ và hấp dẫn hay không. |
| Activation Rate | Tỷ lệ User tạo resource capital ban đầu và ít nhất một planned task. | Cho biết người dùng có đạt được lần sử dụng có ý nghĩa đầu tiên hay không. |
| Active User Rate | Tỷ lệ User sử dụng hệ thống trong một kỳ xác định. | Cho biết mức độ phù hợp liên tục của sản phẩm. |
| User Retention | Tỷ lệ người dùng tiếp tục sử dụng sau 7, 30 hoặc 90 ngày. | Cho biết LifeBalance có trở thành thói quen hay không. |

### 23.2 Task and Resource Metrics

| Metric | Định nghĩa | Ý nghĩa nghiệp vụ |
|---|---|---|
| Task Planning Rate | Tỷ lệ task có planned time hoặc planned money. | Cho biết mức độ áp dụng lập kế hoạch dựa trên nguồn lực. |
| Resource Allocation Rate | Tỷ lệ task được phân bổ nguồn lực trước execution. | Cho biết người dùng có tuân theo quy trình cốt lõi hay không. |
| Actual Recording Rate | Tỷ lệ task hoàn thành có ghi nhận actual time hoặc actual money. | Cho biết độ đầy đủ dữ liệu cho evaluation. |
| Task Completion Rate | Tỷ lệ planned task được hoàn thành trong một kỳ xác định. | Cho biết hiệu quả thực hiện. |
| Task Delay Rate | Tỷ lệ task hoàn thành trễ hoặc chưa hoàn thành sau thời điểm dự kiến. | Cho biết tính thực tế của kế hoạch và áp lực workload. |

### 23.3 Accuracy and Efficiency Metrics

| Metric | Định nghĩa | Ý nghĩa nghiệp vụ |
|---|---|---|
| Time Allocation Accuracy | Mức độ gần nhau giữa planned time và actual time. | Cho biết khả năng ước lượng thời gian. |
| Money Allocation Accuracy | Mức độ gần nhau giữa planned money và actual money. | Cho biết khả năng ước lượng chi phí cho task. |
| Resource Variance Rate | Chênh lệch giữa planned và actual resource usage theo giá trị tuyệt đối hoặc phần trăm. | Cho biết mức độ lệch kế hoạch. |
| Resource Allocation Accuracy | Mức độ chính xác tổng thể của việc phân bổ nguồn lực so với tiêu thụ thực tế. | Cho biết độ trưởng thành trong hành vi lập kế hoạch. |
| Efficiency Indicator | Đánh giá sử dụng nguồn lực trong quan hệ với kết quả và planned value. | Cho biết công việc có được hoàn thành với mức tiêu thụ nguồn lực hợp lý hay không. |

### 23.4 Engagement and Insight Metrics

| Metric | Định nghĩa | Ý nghĩa nghiệp vụ |
|---|---|---|
| Dashboard Usage Rate | Tỷ lệ active users truy cập dashboard trong kỳ xác định. | Cho biết tổng quan cấp cao có hữu ích hay không. |
| Report Usage Rate | Tỷ lệ người dùng truy cập reporting features. | Cho biết nhu cầu phân tích lịch sử. |
| Evaluation Completion Rate | Tỷ lệ completed task được review hoặc đánh giá. | Cho biết mức độ áp dụng improvement cycle. |
| Category Usage Rate | Tỷ lệ task được phân loại bằng category. | Cho biết chất lượng tổ chức dữ liệu. |
| Tag Usage Rate | Tỷ lệ task sử dụng tag. | Cho biết hành vi phân loại linh hoạt. |

### 23.5 Support and Administration Metrics

| Metric | Định nghĩa | Ý nghĩa nghiệp vụ |
|---|---|---|
| Support Request Volume | Số lượng yêu cầu hỗ trợ trong một kỳ. | Cho biết nhu cầu hỗ trợ và khả năng có vấn đề usability. |
| Support Resolution Time | Thời gian trung bình để xử lý yêu cầu hỗ trợ. | Cho biết hiệu quả vận hành hỗ trợ. |
| Escalation Rate | Tỷ lệ yêu cầu hỗ trợ được leo thang lên Admin. | Cho biết độ phức tạp vấn đề và độ rõ của quy trình hỗ trợ. |
| User Support Satisfaction | Mức đánh giá hoặc phản hồi của người dùng về hỗ trợ. | Cho biết chất lượng dịch vụ vận hành. |
| Administrative Processing Time | Thời gian xử lý các hoạt động quản trị phổ biến. | Cho biết hiệu quả quản trị. |

## 24. Business Glossary

Glossary định nghĩa các thuật ngữ nghiệp vụ quan trọng được sử dụng trong tài liệu này và cần được duy trì nhất quán trong các volume sau.

| Thuật ngữ | Định nghĩa |
|---|---|
| Actual | Giá trị thực tế được ghi nhận trong hoặc sau quá trình thực hiện, ví dụ thời gian thực tế hoặc tiền thực tế đã sử dụng. |
| Actual Money | Số tiền thực tế đã chi cho một task hoặc activity. |
| Actual Time | Lượng thời gian thực tế đã sử dụng để thực hiện task hoặc activity. |
| Admin | Actor quản trị chịu trách nhiệm giám sát và quản trị nền tảng ở mức nghiệp vụ. |
| Allocation | Việc gán nguồn lực dự kiến cho một task trước hoặc trong quá trình thực hiện. |
| Available Resource | Lượng nguồn lực còn có thể sử dụng cho lập kế hoạch hoặc phân bổ trong một kỳ phù hợp. |
| Business Capability | Năng lực cấp cao mà hệ thống cần hỗ trợ để đáp ứng mục tiêu nghiệp vụ. |
| Business Constraint | Ràng buộc ảnh hưởng đến phạm vi, vận hành hoặc định hướng yêu cầu. |
| Business Goal | Kết quả kinh doanh tổng quát mà dự án hướng đến. |
| Business Objective | Kết quả nghiệp vụ cụ thể hơn nhằm hỗ trợ một hoặc nhiều business goal. |
| Capital | Tổng năng lực nguồn lực mà người dùng có thể dùng để lập kế hoạch và phân bổ. Trong LifeBalance, capital chủ yếu là time và money. |
| Category | Phân loại có cấu trúc dùng để nhóm task hoặc resource usage theo ý nghĩa nghiệp vụ như học tập, công việc, sức khỏe, tài chính hoặc phát triển cá nhân. |
| Completion | Trạng thái trong đó task đã được hoàn thành theo ý định hoặc kết quả được người dùng chấp nhận. |
| Dashboard | Góc nhìn tổng quan cấp cao trình bày chỉ báo quan trọng như tình trạng nguồn lực, tiến độ task và tín hiệu hiệu quả. |
| Efficiency | Mức độ sử dụng nguồn lực hợp lý so với planned value và kết quả kỳ vọng. Efficiency cần được hiểu theo ngữ cảnh, không chỉ là dùng ít thời gian hoặc ít tiền. |
| Evaluation | Hoạt động nghiệp vụ xem xét kết quả task và so sánh planned resource usage với actual resource usage. |
| Execution | Giai đoạn người dùng thực hiện task đã được lập kế hoạch. |
| Future Scope | Năng lực hoặc cải tiến có thể được xem xét trong tương lai nhưng chưa thuộc phạm vi hiện tại. |
| Guest | Actor chưa xác thực hoặc chưa đăng ký, có thể tiếp cận thông tin công khai hoặc giới thiệu về LifeBalance. |
| Improvement | Hoạt động sử dụng insight lịch sử và kết quả evaluation để cải thiện kế hoạch và phân bổ nguồn lực trong tương lai. |
| In Scope | Các khu vực nghiệp vụ thuộc phạm vi hiện tại và có thể được phân tích chi tiết ở volume sau. |
| KPI | Key Performance Indicator, chỉ số đo lường dùng để đánh giá mức độ đạt mục tiêu. |
| LifeBalance | Hệ thống quản lý nguồn lực cá nhân giúp người dùng lập kế hoạch, phân bổ, thực hiện, theo dõi và đánh giá việc sử dụng time và money trong task. |
| Money | Nguồn lực cốt lõi biểu thị năng lực tài chính có thể dùng cho lập kế hoạch và thực hiện task. |
| Out of Scope | Các khu vực nghiệp vụ bị loại khỏi phạm vi hiện tại hoặc khỏi tài liệu này. |
| Pain Point | Vấn đề, khó khăn hoặc điểm gây bất mãn của người dùng hoặc stakeholder. |
| Persona | Hồ sơ đại diện cho một nhóm người dùng nhằm mô tả goal, frustration, behavior và need. |
| Planned | Giá trị dự kiến hoặc ý định được xác định trước hoặc trong quá trình lập kế hoạch. |
| Planned Money | Số tiền dự kiến hoặc được phân bổ cho task trước khi phát sinh chi tiêu thực tế. |
| Planned Time | Lượng thời gian dự kiến hoặc được phân bổ cho task trước khi thực hiện. |
| Planning | Giai đoạn người dùng xác định task, kết quả kỳ vọng, thời điểm, ưu tiên và nguồn lực dự kiến. |
| Product Philosophy | Tập hợp niềm tin nền tảng định hướng cách sản phẩm hiểu vấn đề người dùng và hình thành yêu cầu nghiệp vụ. |
| Reporting | Năng lực cung cấp thông tin lịch sử và phân tích có cấu trúc để hỗ trợ quyết định và cải thiện. |
| Resource | Tài sản hữu hạn được sử dụng để thực hiện task. Trong LifeBalance, hai resource cốt lõi là time và money. |
| Resource Allocation Accuracy | Mức độ mà nguồn lực được phân bổ khớp với nguồn lực thực tế đã tiêu thụ. |
| Resource Capital | Nguồn time và money khả dụng mà người dùng có thể lập kế hoạch, phân bổ và sử dụng. |
| Resource Management | Hoạt động hiểu, phân bổ, theo dõi và đánh giá nguồn lực hữu hạn. |
| Resource Variance | Chênh lệch giữa planned resource usage và actual resource usage. |
| Root Cause | Nguyên nhân nền tảng tạo ra vấn đề hoặc pain point. |
| Staff | Actor vận hành chịu trách nhiệm hỗ trợ người dùng và xử lý yêu cầu hỗ trợ. |
| Stakeholder | Cá nhân hoặc nhóm có lợi ích, trách nhiệm, ảnh hưởng hoặc phụ thuộc đối với LifeBalance. |
| Success Criteria | Điều kiện dùng để xác định hệ thống hoặc dự án đã đạt kết quả nghiệp vụ mong muốn hay chưa. |
| Success Metric | Chỉ số đo lường dùng để đánh giá hiệu suất, adoption, chất lượng hoặc giá trị. |
| Support | Năng lực nghiệp vụ hỗ trợ người dùng, xử lý câu hỏi, quản lý vấn đề và leo thang khi cần. |
| Tag | Nhãn linh hoạt dùng để tổ chức, lọc hoặc phân tích task và resource usage ngoài category chính thức. |
| Task | Đơn vị công việc hoặc hoạt động mà người dùng dự kiến thực hiện và tiêu tốn time, money hoặc cả hai. |
| Task Lifecycle | Vòng đời của task qua các giai đoạn planning, allocation, execution, tracking, evaluation và improvement. |
| Time | Nguồn lực cốt lõi biểu thị giờ, phút hoặc kỳ thời gian có thể được lập kế hoạch và tiêu thụ bởi task. |
| Timeline | Khái niệm hoặc góc nhìn theo thời gian giúp người dùng hiểu khi nào task được lập kế hoạch, thực hiện, trì hoãn, hoàn thành hoặc đánh giá. |
| Tracking | Hoạt động ghi nhận hoặc quan sát tiến độ thực tế và nguồn lực thực tế đã sử dụng. |
| User | Actor đã đăng ký sử dụng LifeBalance để quản lý nguồn lực và task cá nhân. |
| User Retention | Mức độ người dùng tiếp tục sử dụng hệ thống theo thời gian. |
| Variance | Chênh lệch giữa planned value và actual value. Variance có thể áp dụng cho time, money, schedule hoặc yếu tố đo lường khác. |
| Vision | Tuyên bố ngắn gọn về định hướng tương lai và giá trị dự kiến của sản phẩm. |

## Appendix A. Tóm tắt liên kết nội dung

| Chủ đề nghiệp vụ | Các mục liên quan |
|---|---|
| Quản lý nguồn lực cá nhân | Mục 4, 5, 6, 7, 8, 16 |
| Công việc như khoản đầu tư nguồn lực | Mục 7, 8, 10, 17 |
| Planned vs Actual Evaluation | Mục 5, 10, 11, 17, 23, 24 |
| Phân khúc người dùng | Mục 12, 13, 14 |
| Quản trị vận hành | Mục 12, 14, 16, 19, 21 |
| Đo lường thành công | Mục 9, 11, 23 |
| Kiểm soát phạm vi | Mục 15, 18, 20 |

## Appendix B. Ghi chú cho các Volume sau

Các volume tiếp theo cần sử dụng tài liệu này như baseline nghiệp vụ. Cụ thể:

- Business requirements chi tiết cần truy vết về các objective tại Mục 10.
- Functional requirements cần nhất quán với capability tại Mục 16.
- Use case specification không được mâu thuẫn với actor definition tại Mục 14.
- Reporting requirements cần sử dụng thuật ngữ và metric tại Mục 23 và Mục 24.
- Business rules cần phản ánh các principle tại Mục 18.
- Mọi mở rộng ngoài time và money như resource cốt lõi cần được xác định là future scope nếu chưa có phê duyệt.
- Technical design cần được tài liệu hóa riêng và không đưa vào tài liệu nghiệp vụ nếu không có ranh giới rõ ràng.

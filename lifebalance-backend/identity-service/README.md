# 🛡️ Identity Service Module

Module chịu trách nhiệm quản lý User, Role, Permission và tích hợp xác thực bảo mật tập trung qua Keycloak cho hệ thống LifeBalance.

## 📋 Yêu cầu môi trường (Prerequisites)
Để chạy được module này ở máy local, bạn cần cài đặt sẵn:
- **Java 23** (hoặc tương thích JDK 21+)
- **PostgreSQL** (Database chính để lưu thông tin phân quyền)
- **Keycloak Server** (Xử lý Authentication & Authorization)
- **Maven**

## ⚙️ Hướng dẫn cài đặt (Setup Instructions)

**1. Clone dự án và cấu hình biến môi trường**
Đảm bảo bạn đã clone nhánh `main` mới nhất về máy. Sau đó, kiểm tra và cập nhật các thông số kết nối Database và Keycloak trong file `application.yml` (nằm ở `src/main/resources`):
- `spring.datasource.url`
- `spring.datasource.username` / `password`
- `spring.security.oauth2.resourceserver.jwt.issuer-uri`

**2. Cài đặt các thư viện (Dependencies)**
Mở Terminal tại thư mục `identity-service` và chạy lệnh:
```bash
mvn clean install -DskipTests
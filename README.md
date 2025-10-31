🍰 **Website Thương Mại Điện Tử – CakeStore**

🚀 Dự án xây dựng nền tảng bán hàng trực tuyến chuyên dụng cho cửa hàng bánh, với hệ thống **quản lý kho theo chi nhánh**, tích hợp **thanh toán VNPAY**, và hỗ trợ đa vai trò: **User**, **Admin**, **Staff**.

---

🧭 **I. Giới thiệu tổng quan**

### 1️⃣ Tên đề tài  
**Website Thương Mại Điện Tử – CakeStore**

### 2️⃣ Mô tả ngắn gọn  
**CakeStore** là hệ thống web thương mại điện tử tập trung vào ngành bánh kẹo và thực phẩm tươi. Hệ thống cho phép:

- **Khách hàng (User)**: Mua sắm trực tuyến, quản lý giỏ hàng, thanh toán (COD hoặc VNPAY), theo dõi đơn hàng.  
- **Quản trị viên (Admin/Staff)**: Kiểm soát sản phẩm, tồn kho theo **chi nhánh** (Branch Inventory), quản lý đơn hàng, người dùng và mã giảm giá toàn hệ thống.

### 3️⃣ Mục tiêu dự án  
- Xây dựng nền tảng bán hàng trực tuyến chuyên biệt, tập trung vào **giao diện và trải nghiệm mua bánh tốt nhất** (sử dụng Thymeleaf).  
- Tích hợp **quản lý tồn kho theo chi nhánh** (Branch Inventory) để tối ưu hóa phân phối.  
- Hỗ trợ **bảo mật cao** với Spring Security và mã hóa mật khẩu BCrypt.  
- Tích hợp **thanh toán VNPAY** (Sandbox & Production).

---

👥 **II. Đối tượng sử dụng & phạm vi hệ thống**

| Vai trò | Mô tả |
|--------|-------|
| 👤 **User (Khách hàng)** | Đăng ký, mua hàng, quản lý giỏ hàng, thanh toán, theo dõi đơn, quản lý địa chỉ, và đánh giá sản phẩm. |
| 🛠 **Admin (Quản trị viên)** | Quản lý người dùng, danh mục, sản phẩm, mã giảm giá, và thống kê doanh thu. |
| 🏬 **Staff / Branch Manager (Quản lý chi nhánh)** | Quản lý đơn hàng, tồn kho (Inventory), và theo dõi vận hành chi nhánh. |
| 👀 **Guest (Khách vãng lai)** | Xem sản phẩm, danh mục, tìm kiếm, đăng ký/đăng nhập. |

---

⚙️ **III. Phân tích chức năng theo từng vai trò**

### 👤 1. User (Khách hàng)

| Chức năng | Mô tả chi tiết |
|----------|----------------|
| **Đăng ký / Đăng nhập** | Bảo mật bằng Spring Security (BCrypt), xác thực OTP qua Email |
| **Giỏ hàng** | Thêm, xóa, cập nhật số lượng, hỗ trợ lưu theo **Session Cart** hoặc **DB Cart** |
| **Thanh toán** | Hỗ trợ **COD** và **VNPAY API** (Sandbox/Production) |
| **Theo dõi đơn hàng** | Xem lịch sử, chi tiết, trạng thái đơn hàng |
| **Yêu thích (Favorite)** | Lưu trữ sản phẩm yêu thích |
| **Chat realtime** | Tính năng chat hỗ trợ qua **Spring WebSocket** |

### 🛠 2. Admin (Quản trị hệ thống)

| Chức năng | Mô tả chi tiết |
|----------|----------------|
| **Dashboard** | Tổng quan thống kê: Doanh thu, đơn hàng, sản phẩm bán chạy |
| **Quản lý người dùng** | CRUD user, phân quyền (User/Admin/Staff) |
| **Quản lý sản phẩm** | CRUD sản phẩm, quản lý **biến thể (Product Variant)** |
| **Quản lý tồn kho** | Quản lý số lượng theo từng **Chi nhánh (BranchInventory)** |
| **Quản lý đơn hàng** | Xem, lọc, cập nhật trạng thái, in hóa đơn |
| **Quản lý mã giảm giá** | Tạo và quản lý mã giảm giá toàn cục |

### 🏬 3. Staff / Branch Manager

*(Tùy chọn mở rộng)*  
- Xem và xử lý đơn hàng tại chi nhánh  
- Cập nhật tồn kho thực tế  
- Báo cáo doanh thu chi nhánh  

---

💡 **IV. Tính năng nổi bật & Cấu hình**

| Tính năng | Mô tả |
|----------|-------|
| **Quản lý kho đa chi nhánh** | Hỗ trợ lưu trữ và quản lý tồn kho sản phẩm (`BranchProduct`, `BranchInventory`) theo từng **chi nhánh vật lý (Branch)** |
| **Thanh toán VNPAY** | Tích hợp cổng thanh toán **VNPAY Sandbox** thông qua cấu hình: `vnp_TmnCode`, `vnp_HashSecret`, `vnp_PayUrl` |
| **Lưu trữ Cloudinary** | Sử dụng **Cloudinary** để lưu trữ hình ảnh sản phẩm, tối ưu băng thông và quản lý tài nguyên |
| **Chat Realtime** | Sử dụng **Spring WebSocket** để cung cấp hỗ trợ trực tuyến tức thời |

---

🧱 **Công nghệ sử dụng**

| Thành phần | Công nghệ | Chi tiết |
|-----------|----------|---------|
| **Backend** | Spring Boot | Phiên bản **3.5.6** (Starter Parent) |
| **View/Frontend** | Thymeleaf | Template Engine chính |
| **Database** | SQL Server / H2 | SQL Server (Production), H2 (Dev/Test) |
| **ORM** | Spring Data JPA (Hibernate) | Quản lý cơ sở dữ liệu |
| **Authentication** | Spring Security | Tích hợp **JWT** (`jjwt.version 0.11.5`) và **BCrypt** |
| **Deployment** | Maven | Quản lý dependencies và build project |
| **Cloud Storage** | Cloudinary | Phiên bản **1.38.0** |

---

📂 **Cấu trúc thư mục chính**

```plaintext
 | Cấu trúc thư mục chính
src/
├── main/java/com/cakestore/cakestore/
│   ├── controller/          # Xử lý request: Auth, Admin, User
│   ├── entity/              # Các đối tượng ORM: User, Order, Product, Branch, Inventory...
│   ├── repository/          # Các Interface truy cập DB
│   ├── service/             # Logic nghiệp vụ: Cart, Checkout, Vnpay, Admin...
│   └── config/              # Cấu hình: Security, Vnpay, Cloudinary, WebSocket
└── resources/
    ├── templates/           # Các file HTML (Thymeleaf): user/, admin/, checkout/
    ├── static/              # Assets tĩnh: CSS, JS, Images
    ├── application.properties # Cấu hình: DB, Mail, Cloudinary, VNPAY
    └── data.sql             # Dữ liệu khởi tạo

Tài khoản MK đăng nhập
Admin account: admin@cakestore.com / 1234
Staff account: staff@cakestore.com / 1234
Customer account: user@cakestore.com / 1234

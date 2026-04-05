# Authentication API Documentation

## Tổng quan
Hệ thống IAM (Identity and Access Management) với JWT-based authentication đã được triển khai cho backend.

## Cấu trúc Database

### Users Table
- `id`: Primary key, auto increment
- `username`: Unique, required (3-50 ký tự)
- `email`: Unique, required (email hợp lệ)
- `password`: BCrypt encrypted, required (min 6 ký tự)
- `full_name`: Tên đầy đủ (optional)
- `phone_number`: Số điện thoại (optional)
- `enabled`: Boolean, default = true
- `created_at`: Timestamp
- `updated_at`: Timestamp

### User Roles
- `ROLE_USER`: Người dùng thông thường
- `ROLE_ADMIN`: Quản trị viên (quản lý sản phẩm)

## API Endpoints

### 1. Đăng ký tài khoản mới
**POST** `/api/auth/register`

**Request Body:**
```json
{
  "username": "customer1",
  "email": "customer1@gmail.com",
  "password": "123456",
  "fullName": "Nguyễn Văn A",
  "phoneNumber": "0912345678"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "customer1",
  "email": "customer1@gmail.com",
  "fullName": "Nguyễn Văn A",
  "roles": ["ROLE_USER"]
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Username already exists"
}
```
hoặc
```json
{
  "message": "Email already exists"
}
```

---

### 2. Đăng nhập
**POST** `/api/auth/login`

**Request Body:**
```json
{
  "username": "customer1",
  "password": "123456"
}
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "customer1",
  "email": "customer1@gmail.com",
  "fullName": "Nguyễn Văn A",
  "roles": ["ROLE_USER"]
}
```

**Error Response (401 Unauthorized):**
```json
{
  "message": "Invalid username or password"
}
```

---

### 3. Lấy thông tin người dùng hiện tại
**GET** `/api/auth/me`

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

**Response (200 OK):**
```json
{
  "id": 1,
  "username": "customer1",
  "email": "customer1@gmail.com",
  "fullName": "Nguyễn Văn A",
  "phoneNumber": "0912345678",
  "roles": ["ROLE_USER"]
}
```

**Error Response (401 Unauthorized):**
Token không hợp lệ hoặc đã hết hạn

---

### 4. Đăng xuất
**POST** `/api/auth/logout`

**Response (200 OK):**
```json
{
  "message": "Logged out successfully"
}
```

**Lưu ý:** JWT là stateless, logout thực tế được xử lý ở phía client bằng cách xóa token.

---

## Bảo mật API Products

### Public Endpoints (không cần đăng nhập):
- `GET /api/products` - Xem tất cả sản phẩm
- `GET /api/products/{id}` - Xem chi tiết sản phẩm
- `GET /api/products/category/{category}` - Xem sản phẩm theo danh mục
- `GET /api/products/bestsellers` - Xem sản phẩm bán chạy
- `GET /api/products/search?q={keyword}` - Tìm kiếm sản phẩm

### Admin Endpoints (chỉ ROLE_ADMIN):
- `POST /api/products` - Tạo sản phẩm mới
- `PUT /api/products/{id}` - Cập nhật sản phẩm
- `DELETE /api/products/{id}` - Xóa sản phẩm

**Headers cho Admin:**
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

---

## JWT Configuration

### Token Expiration
- **Thời gian hết hạn:** 24 giờ (86400000 ms)
- Sau khi hết hạn, user cần đăng nhập lại

### Token Format
```
Authorization: Bearer <token>
```

### JWT Secret Key
Được cấu hình trong `application.properties`:
```properties
app.jwt.secret=5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437
app.jwt.expiration-ms=86400000
```

**⚠️ QUAN TRỌNG:** Đổi secret key trong production!

---

## Testing với Postman/curl

### 1. Đăng ký user mới:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "123456",
    "fullName": "Test User"
  }'
```

### 2. Đăng nhập:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "123456"
  }'
```

### 3. Sử dụng token để access protected endpoints:
```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

### 4. Tạo sản phẩm (Admin only):
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bánh chưng đặc biệt",
    "description": "Bánh chưng truyền thống",
    "price": 150000,
    "category": "banh-chung",
    "isBestSeller": true
  }'
```

---

## Tạo Admin User

Để tạo admin user, bạn cần:

1. Đăng ký user thông thường
2. Vào MySQL database và update role:

```sql
-- Lấy user_id
SELECT id FROM users WHERE username = 'admin';

-- Thêm role ADMIN
INSERT INTO user_roles (user_id, role) VALUES (1, 'ROLE_ADMIN');
```

Hoặc tạo service method để upgrade user thành admin.

---

## Luồng hoạt động Frontend

### 1. Đăng ký/Đăng nhập
```javascript
// Register
const response = await fetch('http://localhost:8080/api/auth/register', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'user1',
    email: 'user1@gmail.com',
    password: '123456'
  })
});

const data = await response.json();
// Lưu token vào localStorage
localStorage.setItem('token', data.token);
localStorage.setItem('user', JSON.stringify(data));
```

### 2. Gọi API với Authentication
```javascript
const token = localStorage.getItem('token');

const response = await fetch('http://localhost:8080/api/auth/me', {
  headers: {
    'Authorization': `Bearer ${token}`
  }
});
```

### 3. Đăng xuất
```javascript
// Xóa token khỏi localStorage
localStorage.removeItem('token');
localStorage.removeItem('user');

// Optional: Gọi logout endpoint
await fetch('http://localhost:8080/api/auth/logout', {
  method: 'POST'
});
```

---

## Error Handling

### 400 Bad Request
- Username hoặc email đã tồn tại
- Validation error (email không hợp lệ, password quá ngắn, etc.)

### 401 Unauthorized
- Username/password sai
- Token không hợp lệ hoặc hết hạn
- Thiếu Authorization header

### 403 Forbidden
- User không có quyền truy cập (không phải ADMIN)

### 404 Not Found
- User không tìm thấy

---

## Security Features

✅ **Password Encryption:** BCrypt với strength 10
✅ **JWT Authentication:** Stateless token-based auth
✅ **Role-based Authorization:** USER và ADMIN roles
✅ **CORS Configuration:** Chỉ cho phép localhost:3000
✅ **Session Management:** Stateless (không lưu session)
✅ **Input Validation:** Jakarta Validation annotations
✅ **SQL Injection Protection:** JPA/Hibernate parameterized queries

---

## Next Steps

1. ✅ Tạo admin user trong database
2. ⬜ Tích hợp authentication vào React frontend
3. ⬜ Tạo Login/Register components
4. ⬜ Implement protected routes
5. ⬜ Add cart và order management với user authentication

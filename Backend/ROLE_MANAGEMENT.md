# Hướng Dẫn Phân Quyền Theo ID

## Tổng Quan
Hệ thống hiện đã được cấu hình để tự động phân quyền dựa trên ID người dùng:
- **ID = 1**: Tự động được gán role ADMIN
- **ID = 2**: Tự động được gán role USER
- **ID khác**: Mặc định được gán role USER

## Cách Thức Hoạt Động

### 1. Đăng Ký Người Dùng Mới
Khi đăng ký người dùng mới qua endpoint `/api/auth/register`, hệ thống sẽ:
1. Tạo user mới với role mặc định là USER
2. Lưu vào database (ID được tự động tạo)
3. Kiểm tra ID của user vừa tạo
4. Cập nhật role tương ứng với ID:
   - ID 1 → ROLE_ADMIN
   - ID 2 → ROLE_USER
   - ID khác → ROLE_USER (giữ nguyên)

### 2. Endpoints API

#### Đăng Ký (Public)
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "admin",
  "email": "admin@example.com",
  "password": "password123",
  "fullName": "Admin User",
  "phoneNumber": "0123456789"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "fullName": "Admin User",
  "roles": ["ROLE_ADMIN"]
}
```

#### Đăng Nhập (Public)
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password123"
}
```

#### Lấy Danh Sách Users (Admin Only)
```bash
GET /api/auth/admin/users
Authorization: Bearer {token}
```

Response:
```json
[
  {
    "id": 1,
    "username": "admin",
    "email": "admin@example.com",
    "fullName": "Admin User",
    "phoneNumber": "0123456789",
    "roles": ["ROLE_ADMIN"],
    "enabled": true,
    "createdAt": "2026-02-04T10:00:00",
    "updatedAt": "2026-02-04T10:00:00"
  },
  {
    "id": 2,
    "username": "user",
    "email": "user@example.com",
    "fullName": "Regular User",
    "phoneNumber": "0987654321",
    "roles": ["ROLE_USER"],
    "enabled": true,
    "createdAt": "2026-02-04T11:00:00",
    "updatedAt": "2026-02-04T11:00:00"
  }
]
```

#### Cập Nhật Role (Admin Only)
```bash
PUT /api/auth/admin/users/role
Authorization: Bearer {token}
Content-Type: application/json

{
  "userId": 3,
  "roles": ["ROLE_ADMIN"]
}
```

Response:
```json
{
  "message": "User role updated successfully",
  "userId": 3,
  "username": "newadmin",
  "roles": ["ROLE_ADMIN"]
}
```

## Phân Quyền Endpoints

### Public (Không cần đăng nhập)
- `POST /api/auth/register` - Đăng ký
- `POST /api/auth/login` - Đăng nhập
- `POST /api/auth/refresh-token` - Làm mới token
- `GET /api/products/**` - Xem sản phẩm

### Authenticated (Cần đăng nhập)
- `GET /api/auth/me` - Thông tin user hiện tại
- `POST /api/cart/**` - Quản lý giỏ hàng
- `GET /api/orders/**` - Xem đơn hàng
- `POST /api/orders/checkout` - Thanh toán

### Admin Only (Chỉ ADMIN)
- `POST /api/products/**` - Tạo sản phẩm
- `PUT /api/products/**` - Cập nhật sản phẩm
- `DELETE /api/products/**` - Xóa sản phẩm
- `GET /api/auth/admin/users` - Xem danh sách users
- `PUT /api/auth/admin/users/role` - Cập nhật role

## Kiểm Thử

### 1. Tạo Admin User (ID = 1)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@example.com",
    "password": "admin123",
    "fullName": "Admin User"
  }'
```

### 2. Tạo Regular User (ID = 2)
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "user",
    "email": "user@example.com",
    "password": "user123",
    "fullName": "Regular User"
  }'
```

### 3. Kiểm Tra Role
```bash
# Đăng nhập với admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Sử dụng token để xem thông tin
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer {token_from_login}"
```

### 4. Test Admin Permissions
```bash
# Admin có thể tạo sản phẩm
curl -X POST http://localhost:8080/api/products/with-image \
  -H "Authorization: Bearer {admin_token}" \
  -F "name=Bánh Chưng" \
  -F "price=50000" \
  -F "category=Traditional" \
  -F "image=@/path/to/image.jpg"

# User không thể tạo sản phẩm (sẽ bị 403 Forbidden)
curl -X POST http://localhost:8080/api/products/with-image \
  -H "Authorization: Bearer {user_token}" \
  -F "name=Bánh Chưng" \
  -F "price=50000"
```

## Lưu Ý Quan Trọng

### 1. Database Setup
Nếu database đã có data, ID có thể không bắt đầu từ 1. Để đảm bảo:
- Xóa toàn bộ data trong table `users` và `user_roles`
- Reset auto-increment về 1:
```sql
-- PostgreSQL
TRUNCATE TABLE user_roles, users RESTART IDENTITY CASCADE;

-- MySQL
TRUNCATE TABLE user_roles;
TRUNCATE TABLE users;
ALTER TABLE users AUTO_INCREMENT = 1;
```

### 2. Security Best Practices
- **Không hardcode ID trong production**: Logic hiện tại chỉ phù hợp cho development/testing
- **Sử dụng feature flags hoặc config**: Trong production, nên dùng biến môi trường
- **Admin đầu tiên**: Nên tạo admin thông qua database script hoặc migration

### 3. Thay Đổi Role Sau
Admin có thể thay đổi role của bất kỳ user nào thông qua endpoint:
```bash
PUT /api/auth/admin/users/role
```

## Cấu Trúc Code

### Files Được Thay Đổi
1. **AuthService.java**
   - Thêm method `assignRoleBasedOnId()` - Tự động gán role theo ID
   - Thêm method `updateUserRole()` - Admin cập nhật role
   - Thêm method `getAllUsers()` - Admin xem danh sách users

2. **AuthController.java**
   - Thêm endpoint `GET /api/auth/admin/users` - Xem users
   - Thêm endpoint `PUT /api/auth/admin/users/role` - Cập nhật role

3. **UpdateRoleRequest.java** (New)
   - DTO cho việc cập nhật role

4. **SecurityConfig.java**
   - Đã có `@EnableMethodSecurity` - Cho phép `@PreAuthorize`
   - Đã có rule cho admin endpoints

## Frontend Integration

### React Example
```typescript
// Check user role
const isAdmin = user.roles.includes('ROLE_ADMIN');

// Conditional rendering
{isAdmin && (
  <button onClick={createProduct}>Create Product</button>
)}

// Update user role (Admin only)
const updateUserRole = async (userId: number, roles: string[]) => {
  const response = await fetch('/api/auth/admin/users/role', {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({ userId, roles })
  });
  return response.json();
};
```

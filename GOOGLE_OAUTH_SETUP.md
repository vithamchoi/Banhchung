# Hướng dẫn tích hợp Google OAuth

## Bước 1: Lấy Google Client ID

1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project có sẵn
3. Vào menu "APIs & Services" > "Credentials"
4. Click "Create Credentials" > "OAuth client ID"
5. Nếu chưa cấu hình OAuth consent screen:
   - Click "Configure Consent Screen"
   - Chọn "External"
   - Điền tên ứng dụng: "Quán nhà bà Ninh"
   - Thêm email hỗ trợ và developer contact
   - Save và tiếp tục
6. Quay lại tạo OAuth Client ID:
   - Application type: "Web application"
   - Name: "Banhchung Web Client"
   - Authorized JavaScript origins:
     - `http://localhost:5173` (development)
     - `https://yourdomain.com` (production)
   - Authorized redirect URIs:
     - `http://localhost:5173` (development)
     - `https://yourdomain.com` (production)
7. Click "Create"
8. Copy "Client ID" (dạng: `xxxxx.apps.googleusercontent.com`)

## Bước 2: Cấu hình Frontend

1. Mở file `.env` trong thư mục `Frontend/`
2. Thay thế `YOUR_GOOGLE_CLIENT_ID_HERE` bằng Client ID vừa lấy:
   ```
   VITE_GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
   ```
3. Lưu file

## Bước 3: Chạy ứng dụng

### Frontend:
```bash
cd Frontend
npm install
npm run dev
```

### Backend:
```bash
cd Backend
mvnw spring-boot:run
```

## Cách hoạt động

1. Người dùng click "Đăng nhập với Google"
2. Google OAuth popup hiện lên
3. Người dùng chọn tài khoản Google
4. Frontend nhận được access token từ Google
5. Frontend gọi Google API để lấy thông tin user (email, tên, ảnh)
6. Frontend gửi thông tin này đến Backend endpoint `/api/auth/google`
7. Backend:
   - Kiểm tra xem user đã tồn tại (qua Google ID)
   - Nếu chưa: Tạo user mới hoặc link Google với email hiện có
   - Nếu rồi: Đăng nhập user
8. Backend trả về JWT token
9. User được đăng nhập thành công

## Lưu ý

- Nút Facebook đã được xóa
- Chỉ còn nút Google OAuth
- Google OAuth chỉ hoạt động khi đã cấu hình đúng Client ID
- Trong môi trường production, cần thêm domain thật vào authorized origins

## Troubleshooting

### Lỗi: "idpiframe_initialization_failed"
- Kiểm tra Google Client ID đã đúng chưa
- Kiểm tra domain trong authorized JavaScript origins

### Lỗi: "redirect_uri_mismatch"
- Thêm redirect URI vào Google Console

### Lỗi backend: "User not found"
- Kiểm tra database connection
- Kiểm tra User entity đã có trường `googleId`

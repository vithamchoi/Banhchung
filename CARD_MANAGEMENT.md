# Hệ Thống Quản Lý Thẻ Thành Viên

## Tổng Quan

Hệ thống quản lý thẻ thành viên cho phép admin tạo, chỉnh sửa, xóa và quản lý các loại thẻ thành viên của cửa hàng. Hệ thống bao gồm backend API (Spring Boot) và frontend UI (React/TypeScript).

## Tính Năng Chính

### 1. Quản Lý Thẻ (CRUD Operations)
- ✅ **Xem danh sách thẻ**: Hiển thị tất cả thẻ với đầy đủ thông tin
- ✅ **Thêm thẻ mới**: Tạo thẻ thành viên mới với thông tin chi tiết
- ✅ **Chỉnh sửa thẻ**: Cập nhật thông tin thẻ đã có
- ✅ **Xóa thẻ**: Xóa thẻ khỏi hệ thống
- ✅ **Bật/Tắt trạng thái**: Kích hoạt hoặc tạm ngừng thẻ

### 2. Thông Tin Thẻ
Mỗi thẻ bao gồm các thông tin:
- **Tên thẻ**: Tên hiển thị của thẻ (bắt buộc)
- **Mô tả**: Mô tả ngắn về thẻ
- **Giá**: Giá thẻ (VNĐ) - bắt buộc
- **Phần trăm giảm giá**: Giảm giá khi mua hàng (0-100%)
- **Thời hạn**: Thời gian hiệu lực (tháng)
- **Quyền lợi**: Các quyền lợi của thẻ
- **Màu sắc**: Màu hiển thị của thẻ (8 màu)
- **Icon**: Biểu tượng của thẻ
- **Trạng thái**: Đang hoạt động/Tạm ngừng
- **Thứ tự hiển thị**: Sắp xếp hiển thị

### 3. Thống Kê
- Tổng số thẻ
- Số thẻ đang hoạt động
- Số thẻ ngừng hoạt động
- Giá trung bình các thẻ

### 4. Tìm Kiếm & Lọc
- Tìm kiếm theo tên thẻ
- Tìm kiếm theo mô tả
- Hiển thị kết quả real-time

## Backend API

### Database Schema

```sql
CREATE TABLE membership_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    discount_percentage INT,
    validity_months INT,
    benefits TEXT,
    color VARCHAR(20),
    icon VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    display_order INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### API Endpoints

#### 1. GET /api/cards
Lấy tất cả thẻ (Public)

**Response:**
```json
[
  {
    "id": 1,
    "name": "Thẻ Bạc",
    "description": "Thẻ thành viên bạc",
    "price": 100000,
    "discountPercentage": 5,
    "validityMonths": 12,
    "benefits": "- Giảm 5% mọi đơn hàng\n- Ưu tiên đặt hàng",
    "color": "#3b82f6",
    "icon": "CreditCard",
    "isActive": true,
    "displayOrder": 1,
    "createdAt": "2026-02-04T10:00:00",
    "updatedAt": "2026-02-04T10:00:00"
  }
]
```

#### 2. GET /api/cards/active
Lấy các thẻ đang hoạt động (Public)

**Response:** Tương tự GET /api/cards nhưng chỉ có thẻ isActive=true

#### 3. GET /api/cards/{id}
Lấy thông tin 1 thẻ theo ID (Public)

**Response:**
```json
{
  "id": 1,
  "name": "Thẻ Bạc",
  ...
}
```

#### 4. POST /api/cards
Tạo thẻ mới (Admin only)

**Headers:**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Thẻ Vàng",
  "description": "Thẻ thành viên vàng",
  "price": 200000,
  "discountPercentage": 10,
  "validityMonths": 12,
  "benefits": "- Giảm 10% mọi đơn hàng\n- Ưu tiên đặt hàng\n- Freeship",
  "color": "#f59e0b",
  "icon": "CreditCard",
  "isActive": true,
  "displayOrder": 2
}
```

**Response:** Thông tin thẻ vừa tạo

#### 5. PUT /api/cards/{id}
Cập nhật thẻ (Admin only)

**Headers:**
```
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:** Tương tự POST

**Response:** Thông tin thẻ sau khi cập nhật

#### 6. DELETE /api/cards/{id}
Xóa thẻ (Admin only)

**Headers:**
```
Authorization: Bearer {token}
```

**Response:**
```json
{
  "message": "Xóa thẻ thành công",
  "id": "1"
}
```

#### 7. PATCH /api/cards/{id}/toggle-status
Bật/Tắt trạng thái thẻ (Admin only)

**Headers:**
```
Authorization: Bearer {token}
```

**Response:** Thông tin thẻ với trạng thái đã đổi

### Validation Rules

- **name**: Không được để trống, duy nhất
- **price**: Phải ≥ 0
- **discountPercentage**: 0-100 nếu có
- **validityMonths**: > 0 nếu có
- **displayOrder**: Số nguyên ≥ 0

### Error Responses

**400 Bad Request:**
```json
{
  "error": "Tên thẻ đã tồn tại: Thẻ Bạc"
}
```

**404 Not Found:**
```json
{
  "error": "Card Not Found",
  "message": "Không tìm thấy thẻ với ID: 999",
  "status": 404
}
```

**403 Forbidden:**
```json
{
  "error": "Unauthorized",
  "message": "Access Denied"
}
```

## Frontend UI

### Component: CardManagementPage

**Location:** `Frontend/features/admin/CardManagementPage.tsx`

### Tính Năng UI

#### 1. Statistics Dashboard
4 card hiển thị thống kê:
- Tổng số thẻ
- Đang hoạt động (màu xanh)
- Ngừng hoạt động (màu đỏ)
- Giá trung bình

#### 2. Toolbar
- Search input với icon Search
- Button "Thêm Thẻ Mới" với icon Plus

#### 3. Cards Grid
Hiển thị grid 3 cột (responsive):
- **Header**: Màu nền theo color, tên + mô tả, badge trạng thái
- **Body**: 
  - Giá (icon DollarSign)
  - Giảm giá % (icon Tag)
  - Thời hạn tháng (icon Calendar)
  - Quyền lợi (background xám)
  - Ngày tạo/cập nhật
- **Actions**:
  - Button Sửa (Edit icon)
  - Button Bật/Tắt (Power icon)
  - Button Xóa (Trash icon)

#### 4. Create/Edit Dialog
Modal form với:
- Tên thẻ *
- Mô tả (textarea)
- Giá thẻ * | Giảm giá %
- Thời hạn | Thứ tự hiển thị
- Màu sắc (8 color buttons)
- Quyền lợi (textarea)
- Checkbox kích hoạt
- Buttons: Hủy | Tạo Mới/Cập Nhật

#### 5. Delete Confirmation Dialog
- Tiêu đề: "Xác nhận xóa"
- Mô tả: "Bạn có chắc chắn muốn xóa thẻ này?"
- Buttons: Hủy | Xóa (đỏ)

### State Management

```typescript
interface MembershipCard {
  id: number;
  name: string;
  description: string;
  price: number;
  discountPercentage: number;
  validityMonths: number;
  benefits: string;
  color: string;
  icon: string;
  isActive: boolean;
  displayOrder: number;
  createdAt: string;
  updatedAt: string;
}
```

### Color Options

8 màu được định nghĩa sẵn:
- Xanh dương (#3b82f6)
- Tím (#8b5cf6)
- Xanh lá (#10b981)
- Vàng (#f59e0b)
- Đỏ (#ef4444)
- Hồng (#ec4899)
- Indigo (#6366f1)
- Xanh ngọc (#14b8a6)

## Routing & Navigation

### App.tsx Integration

```typescript
// Lazy load
const CardManagementPage = lazy(() => import('./features/admin/CardManagementPage'));

// Route
case 'cards':
  return <CardManagementPage onNavigate={setCurrentPage} />;
```

### AdminDashboard Integration

```typescript
import { CreditCard } from 'lucide-react';

// Menu item
{ id: 'cards', name: 'Quản lý thẻ', icon: CreditCard, onClick: () => onNavigate('cards') }
```

## Hướng Dẫn Sử Dụng

### 1. Khởi Động Backend

```bash
cd Backend
mvn spring-boot:run
```

Backend chạy tại: http://localhost:8080

### 2. Khởi Động Frontend

```bash
cd Frontend
npm run dev
```

Frontend chạy tại: http://localhost:3000

### 3. Truy Cập Trang Quản Lý Thẻ

1. Đăng nhập với tài khoản admin (ID=1)
2. Click "Admin Dashboard" trên navbar
3. Click "Quản lý thẻ" trong sidebar
4. Trang quản lý thẻ hiển thị

### 4. Thêm Thẻ Mới

1. Click button "Thêm Thẻ Mới"
2. Điền thông tin:
   - Tên thẻ (bắt buộc): "Thẻ VIP"
   - Mô tả: "Thẻ thành viên VIP"
   - Giá: 500000
   - Giảm giá %: 20
   - Thời hạn: 12
   - Quyền lợi: "- Giảm 20%\n- Freeship\n- Ưu tiên"
   - Chọn màu: Vàng
   - Thứ tự: 1
3. Click "Tạo Mới"

### 5. Chỉnh Sửa Thẻ

1. Click button "Sửa" trên thẻ cần sửa
2. Cập nhật thông tin trong form
3. Click "Cập Nhật"

### 6. Bật/Tắt Thẻ

1. Click button icon Power (⚡)
2. Trạng thái thẻ sẽ đổi ngay lập tức

### 7. Xóa Thẻ

1. Click button icon Trash (🗑️)
2. Confirm trong dialog xác nhận
3. Click "Xóa"

### 8. Tìm Kiếm Thẻ

1. Nhập từ khóa vào search box
2. Kết quả lọc tự động hiển thị

## Testing

### Test API với curl

#### Lấy tất cả thẻ
```bash
curl http://localhost:8080/api/cards
```

#### Tạo thẻ mới
```bash
curl -X POST http://localhost:8080/api/cards \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Thẻ Đồng",
    "description": "Thẻ thành viên đồng",
    "price": 50000,
    "discountPercentage": 3,
    "validityMonths": 6,
    "benefits": "Giảm 3% mọi đơn hàng",
    "color": "#f59e0b",
    "isActive": true,
    "displayOrder": 0
  }'
```

#### Cập nhật thẻ
```bash
curl -X PUT http://localhost:8080/api/cards/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Thẻ Bạc Pro",
    "price": 150000,
    ...
  }'
```

#### Xóa thẻ
```bash
curl -X DELETE http://localhost:8080/api/cards/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Test Frontend

1. **Kiểm tra hiển thị danh sách**
   - Load trang, verify API call
   - Kiểm tra statistics cards hiển thị đúng
   - Verify grid hiển thị đúng layout

2. **Kiểm tra form validation**
   - Submit form rỗng → hiển thị lỗi
   - Nhập giá âm → hiển thị lỗi
   - Nhập discount > 100 → hiển thị lỗi

3. **Kiểm tra CRUD operations**
   - Tạo thẻ mới → verify trong database
   - Sửa thẻ → verify cập nhật
   - Xóa thẻ → verify bị xóa
   - Toggle status → verify trạng thái đổi

4. **Kiểm tra search**
   - Nhập "Bạc" → chỉ hiển thị thẻ có "Bạc"
   - Clear search → hiển thị tất cả

## File Structure

```
Backend/
├── src/main/java/com/quannhabaninh/
│   ├── entity/
│   │   └── MembershipCard.java          # Entity class
│   ├── repository/
│   │   └── MembershipCardRepository.java # JPA Repository
│   ├── service/
│   │   └── MembershipCardService.java   # Business logic
│   ├── controller/
│   │   └── MembershipCardController.java # REST Controller
│   └── dto/
│       ├── CreateCardRequest.java        # DTO for create
│       ├── UpdateCardRequest.java        # DTO for update
│       └── CardResponse.java             # DTO for response

Frontend/
├── features/admin/
│   └── CardManagementPage.tsx            # Main component
└── App.tsx                               # Routing integration
```

## Security

- **Authentication**: JWT Bearer token
- **Authorization**: 
  - GET endpoints: Public (ai cũng xem được)
  - POST/PUT/DELETE/PATCH: Admin only (@PreAuthorize("hasRole('ADMIN')"))
- **Validation**: Input validation ở cả backend và frontend
- **CORS**: Enabled cho localhost:3000

## Performance

- **Backend**:
  - JPA lazy loading
  - Index trên name column
  - Optimized queries với findAllByOrderByDisplayOrderAsc

- **Frontend**:
  - Lazy loading component
  - React.memo cho expensive components
  - Debounce search input (nếu cần)

## Troubleshooting

### Lỗi 403 Forbidden
- **Nguyên nhân**: Không có quyền admin
- **Giải pháp**: Đăng nhập với user ID=1

### Lỗi "Tên thẻ đã tồn tại"
- **Nguyên nhân**: Duplicate name
- **Giải pháp**: Đổi tên thẻ

### Không load được danh sách
- **Nguyên nhân**: Backend không chạy hoặc CORS
- **Giải pháp**: 
  1. Kiểm tra backend: `mvn spring-boot:run`
  2. Kiểm tra console log lỗi

### Form validation không hoạt động
- **Nguyên nhân**: State không update
- **Giải pháp**: Clear cache và reload

## Future Enhancements

- [ ] Upload icon/image cho thẻ
- [ ] Drag & drop để sắp xếp displayOrder
- [ ] Export/Import thẻ (CSV/Excel)
- [ ] Duplicate thẻ
- [ ] Lịch sử thay đổi thẻ
- [ ] Gán thẻ cho user
- [ ] Thống kê user theo thẻ
- [ ] Email marketing cho cardholders
- [ ] QR code cho mỗi thẻ
- [ ] Mobile app quản lý thẻ

## Support

Nếu gặp vấn đề, kiểm tra:
1. Backend logs: `Backend/logs/`
2. Frontend console: Browser DevTools → Console
3. Network tab: Kiểm tra API requests/responses
4. Database: Kiểm tra dữ liệu trong MySQL

---

**Version**: 1.0.0  
**Last Updated**: 04/02/2026  
**Author**: Banhchung Development Team

# API Quản Lý Câu Chuyện (Story Sections)

## Tổng Quan

API này cho phép quản trị viên chỉnh sửa nội dung của trang "Câu chuyện" trên frontend. Hệ thống cho phép tạo, sửa, xóa và sắp xếp các phần (sections) khác nhau của câu chuyện với hình ảnh, nội dung và thứ tự hiển thị tùy chỉnh.

## Cấu Trúc Database

### Bảng: `story_sections`

| Cột | Kiểu | Mô tả |
|-----|------|-------|
| id | BIGINT | Primary key, auto increment |
| title | VARCHAR(255) | Tiêu đề section (bắt buộc) |
| content | TEXT | Nội dung section (bắt buộc) |
| image_url | VARCHAR(500) | URL hình ảnh chính |
| image_alt | VARCHAR(200) | Mô tả hình ảnh chính |
| display_order | INT | Thứ tự hiển thị (bắt buộc) |
| is_active | BOOLEAN | Trạng thái hiển thị (default: true) |
| section_type | VARCHAR(50) | Loại section: HEADER, STORY, PROCESS, CALL_TO_ACTION |
| subtitle | VARCHAR(500) | Phụ đề hoặc tag line |
| highlighted_text | TEXT | Văn bản nổi bật (quote) |
| second_image_url | VARCHAR(500) | URL hình ảnh thứ 2 (cho sections có 2 ảnh) |
| second_image_alt | VARCHAR(200) | Mô tả hình ảnh thứ 2 |
| created_at | TIMESTAMP | Thời gian tạo |
| updated_at | TIMESTAMP | Thời gian cập nhật |

## API Endpoints

### 1. Public Endpoints (Không yêu cầu authentication)

#### 1.1. Lấy tất cả sections công khai (chỉ active)
```
GET /api/story-sections/public
```

**Response:**
```json
[
  {
    "id": 1,
    "title": "Khởi nguồn từ căn bếp nhỏ của Bà Ninh",
    "content": "Những năm 90, mỗi độ xuân về...",
    "imageUrl": "https://...",
    "imageAlt": "Bà Ninh đang gói bánh",
    "displayOrder": 1,
    "isActive": true,
    "sectionType": "STORY",
    "subtitle": null,
    "highlightedText": "Làm cho khách cũng như làm cho con cháu mình ăn...",
    "secondImageUrl": null,
    "secondImageAlt": null,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
]
```

#### 1.2. Lấy sections theo loại (chỉ active)
```
GET /api/story-sections/public/type/{type}
```

**Path Parameters:**
- `type`: HEADER, STORY, PROCESS, CALL_TO_ACTION

**Example:**
```
GET /api/story-sections/public/type/PROCESS
```

#### 1.3. Lấy section theo ID
```
GET /api/story-sections/{id}
```

**Response:**
```json
{
  "id": 1,
  "title": "Khởi nguồn từ căn bếp nhỏ của Bà Ninh",
  "content": "...",
  "imageUrl": "https://...",
  "displayOrder": 1,
  "isActive": true,
  "sectionType": "STORY"
}
```

### 2. Admin Endpoints (Yêu cầu ROLE_ADMIN)

#### 2.1. Lấy tất cả sections (bao gồm inactive)
```
GET /api/story-sections/admin
Authorization: Bearer {token}
```

#### 2.2. Lấy sections theo loại (admin)
```
GET /api/story-sections/admin/type/{type}?activeOnly=false
Authorization: Bearer {token}
```

**Query Parameters:**
- `activeOnly` (optional): true/false - Lọc chỉ active sections

#### 2.3. Tạo section mới
```
POST /api/story-sections
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "title": "Nguyên liệu thượng hạng",
  "content": "Để có được chiếc bánh ngon, khâu chọn nguyên liệu là quan trọng nhất...",
  "imageUrl": "https://images.unsplash.com/photo-1606315983891-469221f05c89",
  "imageAlt": "Nguyên liệu tươi ngon",
  "displayOrder": 3,
  "isActive": true,
  "sectionType": "PROCESS",
  "subtitle": "01. Tuyển chọn",
  "highlightedText": null,
  "secondImageUrl": null,
  "secondImageAlt": null
}
```

**Validation Rules:**
- `title`: Bắt buộc, tối đa 255 ký tự
- `content`: Bắt buộc
- `displayOrder`: Bắt buộc
- `sectionType`: Bắt buộc, tối đa 50 ký tự
- `imageUrl`: Tối đa 500 ký tự
- `imageAlt`: Tối đa 200 ký tự
- `subtitle`: Tối đa 500 ký tự

**Response:** 201 Created
```json
{
  "id": 5,
  "title": "Nguyên liệu thượng hạng",
  "content": "...",
  "displayOrder": 3,
  "isActive": true,
  "sectionType": "PROCESS",
  "createdAt": "2024-01-15T14:30:00",
  "updatedAt": "2024-01-15T14:30:00"
}
```

#### 2.4. Cập nhật section
```
PUT /api/story-sections/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:** Giống như tạo mới

**Response:** 200 OK

#### 2.5. Xóa section
```
DELETE /api/story-sections/{id}
Authorization: Bearer {token}
```

**Response:** 200 OK
```json
{
  "message": "Xóa section thành công"
}
```

#### 2.6. Bật/Tắt trạng thái hiển thị
```
PATCH /api/story-sections/{id}/toggle-status
Authorization: Bearer {token}
```

**Response:** 200 OK (trả về section đã cập nhật)

#### 2.7. Sắp xếp lại thứ tự sections
```
PUT /api/story-sections/reorder
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
[1, 3, 2, 5, 4]
```
(Danh sách IDs theo thứ tự mong muốn)

**Response:** 200 OK
```json
{
  "message": "Sắp xếp lại thứ tự thành công"
}
```

## Loại Section (Section Types)

### HEADER
Phần tiêu đề chính của trang, thường ở đầu trang với background và slogan.

**Ví dụ:**
```json
{
  "title": "Gìn giữ hương vị Tết xưa trong nếp nhà nay",
  "content": "Hơn cả một món ăn, đó là ký ức...",
  "sectionType": "HEADER",
  "subtitle": "Về chúng tôi"
}
```

### STORY
Các câu chuyện, lịch sử, nguồn gốc của thương hiệu.

**Ví dụ:**
```json
{
  "title": "Khởi nguồn từ căn bếp nhỏ của Bà Ninh",
  "content": "Những năm 90, mỗi độ xuân về...",
  "sectionType": "STORY",
  "highlightedText": "Làm cho khách cũng như làm cho con cháu mình ăn"
}
```

### PROCESS
Quy trình chế biến, các bước làm sản phẩm.

**Ví dụ:**
```json
{
  "title": "Nguyên liệu thượng hạng",
  "content": "Để có được chiếc bánh ngon...",
  "sectionType": "PROCESS",
  "subtitle": "01. Tuyển chọn",
  "imageUrl": "...",
  "displayOrder": 3
}
```

### CALL_TO_ACTION
Kêu gọi hành động, chuyển hướng đến trang sản phẩm.

**Ví dụ:**
```json
{
  "title": "Trọn vẹn vị Tết",
  "content": "Đặt hàng ngay để nhận ưu đãi",
  "sectionType": "CALL_TO_ACTION",
  "imageUrl": "banner.jpg"
}
```

## Ví Dụ Sử Dụng

### Frontend - Lấy và hiển thị sections công khai

```typescript
// services/storyApi.ts
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

export interface StorySection {
  id: number;
  title: string;
  content: string;
  imageUrl?: string;
  imageAlt?: string;
  displayOrder: number;
  isActive: boolean;
  sectionType: string;
  subtitle?: string;
  highlightedText?: string;
  secondImageUrl?: string;
  secondImageAlt?: string;
  createdAt: string;
  updatedAt: string;
}

// Lấy tất cả sections công khai
export const getPublicStorySections = async (): Promise<StorySection[]> => {
  const response = await axios.get(`${API_BASE_URL}/story-sections/public`);
  return response.data;
};

// Lấy sections theo loại
export const getStorySectionsByType = async (type: string): Promise<StorySection[]> => {
  const response = await axios.get(`${API_BASE_URL}/story-sections/public/type/${type}`);
  return response.data;
};
```

### Frontend - Hiển thị sections trong StoryPage

```typescript
// pages/StoryPage.tsx
import { useEffect, useState } from 'react';
import { getPublicStorySections, StorySection } from '../services/storyApi';

export const StoryPage = () => {
  const [sections, setSections] = useState<StorySection[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    loadSections();
  }, []);

  const loadSections = async () => {
    try {
      const data = await getPublicStorySections();
      setSections(data);
    } catch (error) {
      console.error('Failed to load story sections:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Đang tải...</div>;

  return (
    <div className="min-h-screen bg-[#FAF9F6] pt-20">
      {sections.map((section) => (
        <div key={section.id} className="max-w-7xl mx-auto px-4 py-16">
          {section.sectionType === 'HEADER' && (
            <div className="bg-[#E8F5E9] py-20 text-center">
              {section.subtitle && (
                <span className="text-green-700 uppercase text-sm">
                  {section.subtitle}
                </span>
              )}
              <h1 className="text-5xl font-serif font-bold text-green-900">
                {section.title}
              </h1>
              <p className="text-gray-600 max-w-2xl mx-auto text-lg mt-4">
                {section.content}
              </p>
            </div>
          )}

          {section.sectionType === 'STORY' && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
              {section.imageUrl && (
                <div className="relative">
                  <img
                    src={section.imageUrl}
                    alt={section.imageAlt}
                    className="rounded-2xl shadow-xl"
                  />
                </div>
              )}
              <div className="space-y-6">
                <h2 className="text-4xl font-serif font-bold text-green-900">
                  {section.title}
                </h2>
                <div className="w-20 h-1 bg-green-600 rounded-full" />
                <p className="text-gray-600 text-lg leading-relaxed">
                  {section.content}
                </p>
                {section.highlightedText && (
                  <p className="italic font-serif text-green-800 text-xl border-l-4 border-green-300 pl-4 py-2 bg-green-50">
                    "{section.highlightedText}"
                  </p>
                )}
              </div>
            </div>
          )}

          {section.sectionType === 'PROCESS' && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-12 items-center">
              {section.imageUrl && (
                <div className="relative">
                  {section.subtitle && (
                    <div className="absolute top-4 left-4 bg-white/90 px-4 py-2 rounded-lg shadow-sm font-serif font-bold">
                      {section.subtitle}
                    </div>
                  )}
                  <img
                    src={section.imageUrl}
                    alt={section.imageAlt}
                    className="rounded-2xl shadow-lg h-[400px] object-cover"
                  />
                </div>
              )}
              <div className="space-y-6">
                <h3 className="text-2xl font-serif font-bold text-gray-900">
                  {section.title}
                </h3>
                <p className="text-gray-600 leading-relaxed">
                  {section.content}
                </p>
              </div>
            </div>
          )}

          {section.sectionType === 'CALL_TO_ACTION' && (
            <div className="relative rounded-3xl overflow-hidden h-[400px]">
              {section.imageUrl && (
                <img
                  src={section.imageUrl}
                  alt={section.imageAlt}
                  className="w-full h-full object-cover"
                />
              )}
              <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
                <div className="text-center text-white">
                  <h2 className="text-5xl font-serif font-bold mb-6">
                    {section.title}
                  </h2>
                  <p className="text-xl mb-8">{section.content}</p>
                  <button className="px-8 py-3 bg-white text-green-900 rounded-full font-bold">
                    Đặt hàng ngay
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      ))}
    </div>
  );
};
```

### Frontend - Admin quản lý sections

```typescript
// features/admin/StoryManagementPage.tsx
import { useState, useEffect } from 'react';
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

export const StoryManagementPage = () => {
  const [sections, setSections] = useState([]);
  const [editingSection, setEditingSection] = useState(null);

  useEffect(() => {
    loadSections();
  }, []);

  const loadSections = async () => {
    const token = localStorage.getItem('token');
    const response = await axios.get(`${API_BASE_URL}/story-sections/admin`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    setSections(response.data);
  };

  const handleCreate = async (data) => {
    const token = localStorage.getItem('token');
    await axios.post(`${API_BASE_URL}/story-sections`, data, {
      headers: { Authorization: `Bearer ${token}` }
    });
    loadSections();
  };

  const handleUpdate = async (id, data) => {
    const token = localStorage.getItem('token');
    await axios.put(`${API_BASE_URL}/story-sections/${id}`, data, {
      headers: { Authorization: `Bearer ${token}` }
    });
    loadSections();
  };

  const handleDelete = async (id) => {
    if (!confirm('Bạn có chắc muốn xóa section này?')) return;
    
    const token = localStorage.getItem('token');
    await axios.delete(`${API_BASE_URL}/story-sections/${id}`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    loadSections();
  };

  const handleToggleStatus = async (id) => {
    const token = localStorage.getItem('token');
    await axios.patch(`${API_BASE_URL}/story-sections/${id}/toggle-status`, null, {
      headers: { Authorization: `Bearer ${token}` }
    });
    loadSections();
  };

  const handleReorder = async (newOrder) => {
    const token = localStorage.getItem('token');
    await axios.put(`${API_BASE_URL}/story-sections/reorder`, newOrder, {
      headers: { Authorization: `Bearer ${token}` }
    });
    loadSections();
  };

  return (
    <div className="p-8">
      <h1 className="text-3xl font-bold mb-8">Quản lý Câu chuyện</h1>
      
      <button
        onClick={() => setEditingSection({ isNew: true })}
        className="mb-6 px-6 py-2 bg-green-600 text-white rounded-lg"
      >
        Thêm Section Mới
      </button>

      <div className="space-y-4">
        {sections.map((section) => (
          <div key={section.id} className="bg-white p-6 rounded-lg shadow">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-xl font-bold">{section.title}</h3>
                <p className="text-gray-600 mt-2">{section.content.substring(0, 100)}...</p>
                <div className="flex gap-2 mt-2">
                  <span className="px-2 py-1 bg-green-100 text-green-800 text-xs rounded">
                    {section.sectionType}
                  </span>
                  <span className="px-2 py-1 bg-blue-100 text-blue-800 text-xs rounded">
                    Thứ tự: {section.displayOrder}
                  </span>
                  <span className={`px-2 py-1 text-xs rounded ${
                    section.isActive ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                  }`}>
                    {section.isActive ? 'Hiển thị' : 'Ẩn'}
                  </span>
                </div>
              </div>
              <div className="flex gap-2">
                <button
                  onClick={() => setEditingSection(section)}
                  className="px-4 py-2 bg-blue-500 text-white rounded"
                >
                  Sửa
                </button>
                <button
                  onClick={() => handleToggleStatus(section.id)}
                  className="px-4 py-2 bg-yellow-500 text-white rounded"
                >
                  {section.isActive ? 'Ẩn' : 'Hiện'}
                </button>
                <button
                  onClick={() => handleDelete(section.id)}
                  className="px-4 py-2 bg-red-500 text-white rounded"
                >
                  Xóa
                </button>
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};
```

## Testing với cURL

### Lấy sections công khai
```bash
curl http://localhost:8080/api/story-sections/public
```

### Tạo section mới (Admin)
```bash
curl -X POST http://localhost:8080/api/story-sections \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Khởi nguồn từ căn bếp nhỏ",
    "content": "Những năm 90, mỗi độ xuân về...",
    "imageUrl": "https://example.com/image.jpg",
    "imageAlt": "Bà Ninh gói bánh",
    "displayOrder": 1,
    "isActive": true,
    "sectionType": "STORY"
  }'
```

### Cập nhật section (Admin)
```bash
curl -X PUT http://localhost:8080/api/story-sections/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Khởi nguồn từ căn bếp của Bà Ninh (Updated)",
    "content": "Nội dung mới...",
    "displayOrder": 1,
    "sectionType": "STORY",
    "isActive": true
  }'
```

### Xóa section (Admin)
```bash
curl -X DELETE http://localhost:8080/api/story-sections/1 \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Bật/Tắt trạng thái
```bash
curl -X PATCH http://localhost:8080/api/story-sections/1/toggle-status \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### Sắp xếp lại sections
```bash
curl -X PUT http://localhost:8080/api/story-sections/reorder \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '[1, 3, 2, 5, 4]'
```

## Lưu Ý Quan Trọng

1. **Authentication**: Tất cả endpoints admin yêu cầu JWT token với ROLE_ADMIN
2. **Thứ tự hiển thị**: `displayOrder` nên là số nguyên liên tiếp (1, 2, 3...) để dễ sắp xếp
3. **Section Types**: Nên sử dụng các giá trị chuẩn: HEADER, STORY, PROCESS, CALL_TO_ACTION
4. **Images**: Hỗ trợ URL từ CDN hoặc upload service, không lưu file trực tiếp
5. **Active Status**: Sections inactive sẽ không hiển thị trên trang công khai
6. **Validation**: Frontend nên validate dữ liệu trước khi gửi để tránh lỗi 400 Bad Request

## Migration Script

Khi triển khai lần đầu, cần tạo bảng database:

```sql
CREATE TABLE story_sections (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    image_url VARCHAR(500),
    image_alt VARCHAR(200),
    display_order INT NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    section_type VARCHAR(50) NOT NULL DEFAULT 'STORY',
    subtitle VARCHAR(500),
    highlighted_text TEXT,
    second_image_url VARCHAR(500),
    second_image_alt VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_display_order (display_order),
    INDEX idx_section_type (section_type),
    INDEX idx_is_active (is_active)
);
```

## Kết Luận

API này cung cấp đầy đủ tính năng CRUD để quản lý nội dung trang "Câu chuyện", giúp admin dễ dàng:
- Thêm/sửa/xóa các phần nội dung
- Sắp xếp thứ tự hiển thị
- Bật/tắt hiển thị từng phần
- Tổ chức nội dung theo loại (header, story, process, CTA)
- Hỗ trợ nhiều hình ảnh và văn bản nổi bật

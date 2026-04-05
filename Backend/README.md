# Banhchung Backend API

Backend cho hệ thống quán nhà bà Ninh - Bán bánh chưng, bánh tét và đặc sản Tết.

## Yêu cầu hệ thống

- Java 17+
- Maven 3.6+
- MySQL 8.0+

## Cài đặt

### 1. Tạo database MySQL

```sql
CREATE DATABASE banhchung_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Cấu hình database

File `src/main/resources/application.properties` đã được cấu hình với:
- Username: root
- Password: 12345
- Database: banhchung_db

### 3. Cài đặt dependencies

```bash
cd Backend
mvn clean install
```

### 4. Chạy ứng dụng

```bash
mvn spring-boot:run
```

Server sẽ chạy tại: `http://localhost:8080`

## API Endpoints

### Products

- `GET /api/products` - Lấy tất cả sản phẩm
- `GET /api/products/{id}` - Lấy sản phẩm theo ID
- `GET /api/products/category/{category}` - Lấy sản phẩm theo danh mục
- `GET /api/products/bestsellers` - Lấy sản phẩm best seller
- `GET /api/products/search?q={keyword}` - Tìm kiếm sản phẩm
- `POST /api/products` - Tạo sản phẩm mới
- `PUT /api/products/{id}` - Cập nhật sản phẩm
- `DELETE /api/products/{id}` - Xóa sản phẩm

## Cấu trúc thư mục

```
Backend/
├── src/
│   └── main/
│       ├── java/com/quannhabaninh/
│       │   ├── BanhchungApplication.java
│       │   ├── config/
│       │   │   └── CorsConfig.java
│       │   ├── controller/
│       │   │   └── ProductController.java
│       │   ├── entity/
│       │   │   └── Product.java
│       │   ├── repository/
│       │   │   └── ProductRepository.java
│       │   └── service/
│       │       └── ProductService.java
│       └── resources/
│           └── application.properties
└── pom.xml
```

## Test API

Sử dụng Postman hoặc curl để test:

```bash
# Lấy tất cả sản phẩm
curl http://localhost:8080/api/products

# Tạo sản phẩm mới
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bánh Chưng Đặc Biệt",
    "description": "Gạo nếp cái hoa vàng, đậu xanh, thịt ba chỉ heo sạch",
    "price": 150000,
    "category": "Bánh Truyền Thống",
    "image": "https://example.com/image.jpg",
    "isBestSeller": true,
    "ingredients": "Gạo nếp, đậu xanh, thịt ba chỉ",
    "stockQuantity": 100
  }'
```

## Lưu ý

- Database sẽ tự động tạo bảng khi chạy lần đầu (ddl-auto=update)
- CORS đã được cấu hình cho frontend chạy tại port 3000
- Logging SQL đã được bật để debug

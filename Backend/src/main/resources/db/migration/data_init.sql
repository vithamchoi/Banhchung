-- ============================================================
-- DATA INIT - Bánh Chưng Quán Nhà Bà Ninh
-- Mỗi bảng ~5 bản ghi mẫu
-- ============================================================

-- Thứ tự insert phải đúng theo quan hệ khóa ngoại:
-- membership_cards → users → user_roles → products
-- → carts → cart_items → orders → order_items
-- → story_sections → refresh_tokens

-- ============================================================
-- 1. membership_cards
-- ============================================================
INSERT INTO membership_cards (name, description, price, discount_percentage, validity_months, benefits, color, icon, is_active, display_order, created_at, updated_at) VALUES
('Thẻ Bạc',    'Thẻ thành viên cơ bản dành cho khách hàng mới',             50000,  5,  6,  'Giảm 5% toàn bộ đơn hàng; Ưu tiên tư vấn qua hotline',                '#C0C0C0', 'silver-card',   TRUE, 1, NOW(), NOW()),
('Thẻ Vàng',   'Thẻ thành viên dành cho khách hàng thân thiết',             100000, 10, 12, 'Giảm 10% toàn bộ đơn hàng; Miễn phí giao hàng nội thành',             '#FFD700', 'gold-card',     TRUE, 2, NOW(), NOW()),
('Thẻ Bạch Kim','Thẻ cao cấp dành cho khách hàng VIP',                      200000, 15, 12, 'Giảm 15% toàn bộ đơn hàng; Miễn phí giao hàng toàn quốc; Quà tặng đặc biệt', '#E5E4E2', 'platinum-card', TRUE, 3, NOW(), NOW()),
('Thẻ Kim Cương','Thẻ đặc biệt dành cho khách hàng cực VIP',                500000, 20, 24, 'Giảm 20% toàn bộ đơn hàng; Ưu tiên đặt hàng; Giao hàng nhanh 24h',   '#B9F2FF', 'diamond-card',  TRUE, 4, NOW(), NOW()),
('Thẻ Đồng',   'Thẻ thành viên nhập môn cho khách hàng lần đầu đăng ký',   20000,  3,  3,  'Giảm 3% đơn hàng đầu tiên',                                           '#CD7F32', 'bronze-card',   TRUE, 5, NOW(), NOW());


-- ============================================================
-- 2. users
-- Mật khẩu: "password123" đã bcrypt (cost 10)
-- ============================================================
INSERT INTO users (username, email, password, full_name, phone_number, enabled, membership_card_id, created_at, updated_at) VALUES
('admin',      'admin@banhchung.vn',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lFAO', 'Quản Trị Viên',        '0901234567', TRUE, NULL, NOW(), NOW()),
('nguyen_van_a','nguyenvana@gmail.com','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lFAO', 'Nguyễn Văn A',         '0912345678', TRUE, 1,    NOW(), NOW()),
('tran_thi_b', 'tranthib@gmail.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lFAO', 'Trần Thị B',           '0923456789', TRUE, 2,    NOW(), NOW()),
('le_van_c',   'levanc@gmail.com',    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lFAO', 'Lê Văn C',             '0934567890', TRUE, 3,    NOW(), NOW()),
('pham_thi_d', 'phamthid@gmail.com',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lFAO', 'Phạm Thị D',           '0945678901', TRUE, NULL, NOW(), NOW());


-- ============================================================
-- 3. user_roles
-- ============================================================
INSERT INTO user_roles (user_id, role) VALUES
(1, 'ROLE_ADMIN'),
(1, 'ROLE_USER'),
(2, 'ROLE_USER'),
(3, 'ROLE_USER'),
(4, 'ROLE_USER'),
(5, 'ROLE_USER');


-- ============================================================
-- 4. products
-- ============================================================
INSERT INTO products (name, description, price, category, image, is_best_seller, ingredients, stock_quantity, created_at, updated_at) VALUES
('Bánh Chưng Truyền Thống',
 'Bánh chưng vuông truyền thống gói lá dong xanh, nhân thịt mỡ đậu xanh thơm ngon, nấu 12 tiếng.',
 85000, 'Bánh Chưng', '/images/banh-chung-truyen-thong.jpg', TRUE,
 'Gạo nếp cái hoa vàng, đậu xanh, thịt ba chỉ, hành tím, tiêu, lá dong, lạt tre',
 50, NOW(), NOW()),

('Bánh Chưng Gấc',
 'Bánh chưng màu đỏ cam rực rỡ từ gấc tươi, mang lại may mắn và hương vị đặc biệt.',
 95000, 'Bánh Chưng', '/images/banh-chung-gac.jpg', TRUE,
 'Gạo nếp, quả gấc, đậu xanh, thịt ba chỉ, hành, tiêu, lá dong',
 30, NOW(), NOW()),

('Bánh Tét Miền Nam',
 'Bánh tét hình trụ đặc trưng miền Nam, nhân chuối hoặc đậu xanh thịt mỡ.',
 75000, 'Bánh Tét', '/images/banh-tet-mien-nam.jpg', FALSE,
 'Gạo nếp, chuối sứ / đậu xanh, dừa nạo, lá chuối, lạt tre',
 40, NOW(), NOW()),

('Bánh Chưng Chay',
 'Bánh chưng chay không thịt dành cho người ăn chay, nhân đậu xanh và nấm hương.',
 70000, 'Bánh Chưng Chay', '/images/banh-chung-chay.jpg', FALSE,
 'Gạo nếp, đậu xanh, nấm hương, mộc nhĩ, hành tây, tiêu, lá dong',
 20, NOW(), NOW()),

('Bánh Chưng Cẩm',
 'Bánh chưng tím bắt mắt sử dụng gạo nếp cẩm, tốt cho sức khỏe.',
 90000, 'Bánh Chưng', '/images/banh-chung-cam.jpg', FALSE,
 'Gạo nếp cẩm, đậu xanh, thịt ba chỉ, hành, tiêu, lá dong',
 25, NOW(), NOW());


-- ============================================================
-- 5. carts
-- ============================================================
INSERT INTO carts (user_id, created_at, updated_at) VALUES
(2, NOW(), NOW()),
(3, NOW(), NOW()),
(4, NOW(), NOW()),
(5, NOW(), NOW()),
(1, NOW(), NOW());


-- ============================================================
-- 6. cart_items
-- ============================================================
INSERT INTO cart_items (cart_id, product_id, quantity, price, created_at, updated_at) VALUES
(1, 1, 2, 85000, NOW(), NOW()),
(1, 3, 1, 75000, NOW(), NOW()),
(2, 2, 3, 95000, NOW(), NOW()),
(3, 4, 2, 70000, NOW(), NOW()),
(4, 5, 1, 90000, NOW(), NOW());


-- ============================================================
-- 7. orders
-- ============================================================
INSERT INTO orders (user_id, order_number, status, payment_method, payment_status,
                    subtotal, shipping_fee, discount_amount, total_amount,
                    shipping_name, shipping_phone, shipping_email,
                    shipping_address, shipping_city, shipping_district, shipping_ward,
                    notes, created_at, updated_at) VALUES
(2, 'ORD-20260101-001', 'DELIVERED',  'COD',           'PAID',
 170000, 20000, 0,      190000,
 'Nguyễn Văn A', '0912345678', 'nguyenvana@gmail.com',
 '123 Đường Láng', 'Hà Nội', 'Đống Đa', 'Láng Hạ',
 NULL, '2026-01-01 10:00:00', '2026-01-01 10:00:00'),

(3, 'ORD-20260115-002', 'CONFIRMED',  'BANK_TRANSFER',  'PAID',
 285000, 0,     28500,  256500,
 'Trần Thị B', '0923456789', 'tranthib@gmail.com',
 '456 Nguyễn Trãi', 'Hà Nội', 'Thanh Xuân', 'Khương Đình',
 'Giao buổi sáng trước 10h', '2026-01-15 08:30:00', '2026-01-15 09:00:00'),

(4, 'ORD-20260120-003', 'PENDING',    'MOMO',           'UNPAID',
 140000, 30000, 0,      170000,
 'Lê Văn C', '0934567890', 'levanc@gmail.com',
 '789 Trần Hưng Đạo', 'TP.HCM', 'Quận 1', 'Phạm Ngũ Lão',
 NULL, '2026-01-20 14:00:00', '2026-01-20 14:00:00'),

(5, 'ORD-20260205-004', 'PROCESSING', 'VNPAY',          'PAID',
 90000,  20000, 0,      110000,
 'Phạm Thị D', '0945678901', 'phamthid@gmail.com',
 '321 Điện Biên Phủ', 'TP.HCM', 'Bình Thạnh', 'Phường 22',
 'Để ngoài cửa nếu không có nhà', '2026-02-05 11:00:00', '2026-02-05 11:30:00'),

(2, 'ORD-20260220-005', 'SHIPPING',   'COD',            'UNPAID',
 255000, 20000, 8500,   266500,
 'Nguyễn Văn A', '0912345678', 'nguyenvana@gmail.com',
 '123 Đường Láng', 'Hà Nội', 'Đống Đa', 'Láng Hạ',
 'Gọi điện trước khi giao', '2026-02-20 09:00:00', '2026-02-20 10:00:00');


-- ============================================================
-- 8. order_items
-- ============================================================
INSERT INTO order_items (order_id, product_id, product_name, product_image, quantity, price, subtotal) VALUES
(1, 1, 'Bánh Chưng Truyền Thống', '/images/banh-chung-truyen-thong.jpg', 2, 85000, 170000),
(2, 2, 'Bánh Chưng Gấc',          '/images/banh-chung-gac.jpg',          3, 95000, 285000),
(3, 4, 'Bánh Chưng Chay',         '/images/banh-chung-chay.jpg',         2, 70000, 140000),
(4, 5, 'Bánh Chưng Cẩm',          '/images/banh-chung-cam.jpg',          1, 90000,  90000),
(5, 1, 'Bánh Chưng Truyền Thống', '/images/banh-chung-truyen-thong.jpg', 2, 85000, 170000),
(5, 3, 'Bánh Tét Miền Nam',       '/images/banh-tet-mien-nam.jpg',       1, 75000,  75000);


-- ============================================================
-- 9. story_sections
-- ============================================================
INSERT INTO story_sections (title, content, image_url, image_alt, display_order, is_active, section_type, subtitle, highlighted_text, second_image_url, second_image_alt, created_at, updated_at) VALUES
('Câu Chuyện Bánh Chưng',
 'Từ hàng trăm năm trước, bánh chưng đã gắn liền với ngày Tết cổ truyền Việt Nam. Hình vuông tượng trưng cho Đất, lớp lá dong xanh mướt ôm trọn hương vị quê hương.',
 '/images/story-header.jpg', 'Bánh chưng truyền thống Việt Nam', 1, TRUE, 'HEADER',
 'Hương vị ngàn năm', 'Bánh chưng – hồn Tết Việt',
 NULL, NULL, NOW(), NOW()),

('Nguồn Gốc & Truyền Thuyết',
 'Tương truyền hoàng tử Lang Liêu đã dâng bánh chưng và bánh dầy lên vua Hùng, biểu trưng cho lòng hiếu thảo và sự trân trọng đất trời.',
 '/images/story-legend.jpg', 'Truyền thuyết bánh chưng Lang Liêu', 2, TRUE, 'STORY',
 'Hơn 4000 năm lịch sử', NULL,
 '/images/story-legend-2.jpg', 'Vua Hùng và hoàng tử Lang Liêu', NOW(), NOW()),

('Quy Trình Làm Bánh',
 'Mỗi chiếc bánh chưng tại Quán Nhà Bà Ninh được làm hoàn toàn thủ công: chọn gạo nếp cái hoa vàng, đậu xanh cà, thịt ba chỉ ướp gia vị rồi gói bằng lá dong tươi và nấu suốt 12 tiếng.',
 '/images/story-process.jpg', 'Quy trình làm bánh chưng thủ công', 3, TRUE, 'PROCESS',
 'Thủ công – Tâm huyết – Truyền thống', 'Nấu 12 tiếng liên tục để bánh dẻo ngon',
 '/images/story-process-2.jpg', 'Gói bánh chưng bằng lá dong', NOW(), NOW()),

('Nguyên Liệu Tuyển Chọn',
 'Chúng tôi chỉ sử dụng gạo nếp cái hoa vàng Hải Hậu, đậu xanh vàng óng từ Nghệ An, thịt ba chỉ tươi mỗi ngày và lá dong xanh mướt từ Ba Vì – tất cả đều được kiểm định kỹ lưỡng.',
 '/images/story-ingredients.jpg', 'Nguyên liệu sạch làm bánh chưng', 4, TRUE, 'STORY',
 'Sạch từ nguồn – Ngon từ tâm', NULL,
 NULL, NULL, NOW(), NOW()),

('Đặt Bánh Ngay Hôm Nay',
 'Hãy để Quán Nhà Bà Ninh mang hương vị Tết cổ truyền đến tận tay bạn và gia đình. Giao hàng toàn quốc, đảm bảo bánh tươi ngon khi đến tay khách hàng.',
 '/images/story-cta.jpg', 'Đặt bánh chưng online giao tận nhà', 5, TRUE, 'CALL_TO_ACTION',
 'Giao hàng toàn quốc – Tươi ngon đảm bảo', 'Đặt ngay – Nhận ưu đãi lên đến 20%',
 NULL, NULL, NOW(), NOW());


-- ============================================================
-- 10. refresh_tokens  (token mẫu – không dùng thực tế)
-- ============================================================
INSERT INTO refresh_tokens (token, user_id, expiry_date, created_at) VALUES
('rt_sample_token_admin_001',    1, DATE_ADD(NOW(), INTERVAL 7 DAY), NOW()),
('rt_sample_token_user_a_002',   2, DATE_ADD(NOW(), INTERVAL 7 DAY), NOW()),
('rt_sample_token_user_b_003',   3, DATE_ADD(NOW(), INTERVAL 7 DAY), NOW()),
('rt_sample_token_user_c_004',   4, DATE_ADD(NOW(), INTERVAL 7 DAY), NOW()),
('rt_sample_token_user_d_005',   5, DATE_ADD(NOW(), INTERVAL 7 DAY), NOW());


-- Migration: Thêm cột membership_card_id vào bảng users

-- Thêm cột membership_card_id (foreign key đến bảng membership_cards)
ALTER TABLE users
ADD COLUMN membership_card_id BIGINT DEFAULT NULL;

-- Thêm foreign key constraint
ALTER TABLE users
ADD CONSTRAINT fk_users_membership_card
FOREIGN KEY (membership_card_id) REFERENCES membership_cards(id)
ON DELETE SET NULL;

-- Tạo index cho performance
CREATE INDEX idx_users_membership_card_id ON users(membership_card_id);

-- Kiểm tra kết quả
SELECT COUNT(*) as total_users FROM users;
SELECT COUNT(*) as users_with_card FROM users WHERE membership_card_id IS NOT NULL;

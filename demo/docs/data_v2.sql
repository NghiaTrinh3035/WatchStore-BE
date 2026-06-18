-- Seed dữ liệu tổng hợp (base + mở rộng), chạy độc lập
-- password user all: 123456
USE projectcnpm;
SET NAMES utf8mb4;

SET FOREIGN_KEY_CHECKS = 0;
START TRANSACTION;

CREATE TABLE IF NOT EXISTS product_categories (
                                                  product_id VARCHAR(36) NOT NULL,
    category_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (product_id, category_id),
    CONSTRAINT fk_product_categories_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_product_categories_category FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
    );


-- =========================================================
-- 0) BASE BOOTSTRAP (gộp từ data.sql)
-- =========================================================
INSERT IGNORE INTO users (id, username, password, full_name, email, phone, address, gender, role, is_active, created_at) VALUES
('11111111-1111-1111-1111-111111111101', 'cus_an',   '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Nguyễn Văn An',     'an.customer@example.com',   '0901000001', 'Hà Nội',   'MALE',   'CUSTOMER', 1, '2026-03-01 08:00:00'),
('11111111-1111-1111-1111-111111111102', 'cus_binh', '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Trần Thị Bình',    'binh.customer@example.com', '0901000002', 'Đà Nẵng',  'FEMALE', 'CUSTOMER', 1, '2026-03-01 08:10:00'),
('11111111-1111-1111-1111-111111111201', 'stf_dung', '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Phạm Quốc Dũng',   'dung.staff@example.com',    '0902000001', 'Hà Nội',   'MALE',   'STAFF',    1, '2026-03-01 09:00:00'),
('11111111-1111-1111-1111-111111111202', 'stf_ha',   '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Võ Thu Hà',        'ha.staff@example.com',      '0902000002', 'Cần Thơ',  'FEMALE', 'STAFF',    1, '2026-03-01 09:10:00'),
('11111111-1111-1111-1111-111111111301', 'own_long', '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Bùi Hoàng Long',   'long.owner@example.com',    '0903000001', 'Hà Nội',   'MALE',   'OWNER',    1, '2026-03-01 10:00:00');

INSERT IGNORE INTO customers (id) VALUES
('11111111-1111-1111-1111-111111111101'),
('11111111-1111-1111-1111-111111111102');

INSERT IGNORE INTO staffs (id) VALUES
('11111111-1111-1111-1111-111111111201'),
('11111111-1111-1111-1111-111111111202');

INSERT IGNORE INTO owners (id) VALUES
('11111111-1111-1111-1111-111111111301');

INSERT IGNORE INTO categories (id, name, description) VALUES
('22222222-2222-2222-2222-222222222001', 'Đồng hồ Cơ',     'Đồng hồ cơ truyền thống'),
('22222222-2222-2222-2222-222222222002', 'Đồng hồ Quartz', 'Đồng hồ pin độ chính xác cao'),
('22222222-2222-2222-2222-222222222003', 'Đồng hồ Smart',  'Đồng hồ thông minh');

INSERT IGNORE INTO suppliers (id, name, contract_info, address) VALUES
('33333333-3333-3333-3333-333333333001', 'Nhà cung cấp A', 'Hợp đồng 2026-01', 'Hà Nội'),
('33333333-3333-3333-3333-333333333002', 'Nhà cung cấp B', 'Hợp đồng 2026-02', 'TP.HCM');

INSERT IGNORE INTO vouchers (id, code, discount_percent, usage_count, valid_from, valid_to, created_at, quantity, status) VALUES
('66666666-6666-6666-6666-666666666001', 'VC10OFF', 10, 0, '2026-03-01 00:00:00', '2026-12-31 23:59:59', '2026-03-01 00:00:00', 100, 'ACTIVE'),
('66666666-6666-6666-6666-666666666002', 'VC20OFF', 20, 3, '2026-03-01 00:00:00', '2026-06-30 23:59:59', '2026-03-01 00:00:00',  50, 'ACTIVE'),
('66666666-6666-6666-6666-666666666003', 'VC5OFF',   5, 1, '2026-01-01 00:00:00', '2026-03-31 23:59:59', '2026-01-01 00:00:00',   1, 'USED_UP');

INSERT IGNORE INTO products (
    id, brand, name, description, price, stock_quantity,
    movement_type, glass_material, water_resistance, face_size,
    wire_material, wire_color, case_color, face_color,
    status, updated_at, category_id
) VALUES
('44444444-4444-4444-4444-444444444001', 'Seiko', 'Seiko 5 Sports',
 'Đồng hồ cơ tự động, thiết kế thể thao', 5200000, 15,
 'Automatic', 'Hardlex', '100m', '42mm',
 'Thép không gỉ', 'Bạc', 'Bạc', 'Đen',
 'ACTIVE', '2026-03-05 09:00:00', '22222222-2222-2222-2222-222222222001'),
('44444444-4444-4444-4444-444444444002', 'Casio', 'Casio MTP-VT01',
 'Đồng hồ quartz cổ điển, mặt trắng', 1500000, 40,
 'Quartz', 'Mineral', '30m', '40mm',
 'Da', 'Nâu', 'Bạc', 'Trắng',
 'ACTIVE', '2026-03-05 09:10:00', '22222222-2222-2222-2222-222222222002'),
('44444444-4444-4444-4444-444444444003', 'Apple', 'Apple Watch SE',
 'Đồng hồ thông minh theo dõi sức khỏe', 6900000, 20,
 'Digital', 'Ion-X', '50m', '44mm',
 'Silicone', 'Đen', 'Đen', 'Đen',
 'ACTIVE', '2026-03-05 09:20:00', '22222222-2222-2222-2222-222222222003');

INSERT IGNORE INTO product_images (id, image_url, public_id, alt_text, is_thumbnail, product_id) VALUES
('45454545-4545-4545-4545-454545454001', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775284566/SNK385K1-600x600_fgf3c5.jpg', 'products/p1-main', 'Seiko 5 Sports', 1, '44444444-4444-4444-4444-444444444001'),
('45454545-4545-4545-4545-454545454002', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775284615/mtp-vt01l-1b_d25fa5c522354d63922-600x600_ztqb3o.jpg', 'products/p2-main', 'Casio MTP-VT01', 1, '44444444-4444-4444-4444-444444444002'),
('45454545-4545-4545-4545-454545454003', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775284659/61773_dong_ho_thong_minh_apple_watch_se_3_44mm_gps_vie_n_nhom_day_cao_su_starlight_3_ajonfq.jpg', 'products/p3-main', 'Apple Watch SE', 1, '44444444-4444-4444-4444-444444444003');

INSERT IGNORE INTO carts (id, total_amount, customer_id) VALUES
('77777777-7777-7777-7777-777777777001', 5200000, '11111111-1111-1111-1111-111111111101'),
('77777777-7777-7777-7777-777777777002', 3000000, '11111111-1111-1111-1111-111111111102');

INSERT IGNORE INTO cart_items (id, quantity, sub_total, cart_id, product_id) VALUES
('88888888-8888-8888-8888-888888888001', 1, 5200000, '77777777-7777-7777-7777-777777777001', '44444444-4444-4444-4444-444444444001'),
('88888888-8888-8888-8888-888888888002', 2, 3000000, '77777777-7777-7777-7777-777777777002', '44444444-4444-4444-4444-444444444002');

INSERT IGNORE INTO orders (id, order_date, total_amount, note, shipping_address, status, customer_id, voucher_id) VALUES
('99999999-9999-9999-9999-999999999001', '2026-03-08 10:00:00', 4680000, 'Giao giờ hành chính', 'Hà Nội',  'CONFIRMED', '11111111-1111-1111-1111-111111111101', '66666666-6666-6666-6666-666666666001'),
('99999999-9999-9999-9999-999999999002', '2026-03-09 11:00:00', 3000000, 'Gói hàng kỹ',         'Đà Nẵng', 'PENDING',   '11111111-1111-1111-1111-111111111102', NULL);

INSERT IGNORE INTO order_items (id, quantity, sub_total, order_id, product_id) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa001', 1, 5200000, '99999999-9999-9999-9999-999999999001', '44444444-4444-4444-4444-444444444001'),
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa002', 2, 3000000, '99999999-9999-9999-9999-999999999002', '44444444-4444-4444-4444-444444444002');

INSERT IGNORE INTO order_status_history (id, order_id, status, note, changed_at, changed_by) VALUES
('abababab-abab-abab-abab-ababababab01', '99999999-9999-9999-9999-999999999001', 'CONFIRMED', 'Đơn đã xác nhận', '2026-03-08 10:01:00', 'stf_dung'),
('abababab-abab-abab-abab-ababababab02', '99999999-9999-9999-9999-999999999002', 'PENDING',   'Đơn mới tạo',     '2026-03-09 11:00:00', 'cus_binh');

INSERT IGNORE INTO payments (id, payment_date, amount, method, status, is_paid, order_id) VALUES
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb001', '2026-03-08 10:05:00', 4680000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999001'),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb002', '2026-03-09 11:10:00', 3000000, 'COD',           'RECEIVED',  0, '99999999-9999-9999-9999-999999999002');

INSERT IGNORE INTO warranties (
    id, customer_phone, customer_name, issue_description,
    received_date, expected_return_date, status,
    technician_note, reject_reason, quantity, product_id
) VALUES
('dddddddd-dddd-dddd-dddd-ddddddddd001', '0901000001', 'Nguyễn Văn An', 'Đồng hồ chậm 5 phút/ngày',
 '2026-03-12 09:00:00', '2026-03-20 17:00:00', 'RECEIVED',
 'Kiểm tra bộ máy', NULL, 1, '44444444-4444-4444-4444-444444444001'),
('dddddddd-dddd-dddd-dddd-ddddddddd002', '0901000002', 'Trần Thị Bình', 'Mặt kính bị trầy nhẹ',
 '2026-03-13 09:30:00', '2026-03-22 17:00:00', 'PROCESSING',
 'Đánh bóng mặt kính', NULL, 1, '44444444-4444-4444-4444-444444444002');

INSERT IGNORE INTO import_receipts (id, import_date, note, supplier_id, owner_id) VALUES
('ffffffff-ffff-ffff-ffff-fffffffff001', '2026-03-06 08:00:00', 'Nhập lô tháng 3 - đợt 1', '33333333-3333-3333-3333-333333333001', '11111111-1111-1111-1111-111111111301'),
('ffffffff-ffff-ffff-ffff-fffffffff002', '2026-03-07 08:30:00', 'Nhập lô tháng 3 - đợt 2', '33333333-3333-3333-3333-333333333002', '11111111-1111-1111-1111-111111111301');

INSERT IGNORE INTO import_details (id, quantity, import_price, import_receipt_id, product_id) VALUES
('12121212-1212-1212-1212-121212121001', 10, 4200000, 'ffffffff-ffff-ffff-ffff-fffffffff001', '44444444-4444-4444-4444-444444444001'),
('12121212-1212-1212-1212-121212121002', 20, 1100000, 'ffffffff-ffff-ffff-ffff-fffffffff002', '44444444-4444-4444-4444-444444444002');

INSERT IGNORE INTO reviews (id, rating, comment, created_at, customer_id, product_id) VALUES
('13131313-1313-1313-1313-131313131001', 5, 'Sản phẩm đẹp, đúng như mô tả', '2026-03-11 10:00:00', '11111111-1111-1111-1111-111111111101', '44444444-4444-4444-4444-444444444001'),
('13131313-1313-1313-1313-131313131002', 4, 'Giá tốt, giao nhanh',          '2026-03-11 10:10:00', '11111111-1111-1111-1111-111111111102', '44444444-4444-4444-4444-444444444002');

INSERT IGNORE INTO chats (id, start_date, end_date, is_ai_handled, customer_id) VALUES
('14141414-1414-1414-1414-141414141001', '2026-03-12 08:00:00', '2026-03-12 08:30:00', 0, '11111111-1111-1111-1111-111111111101'),
('14141414-1414-1414-1414-141414141002', '2026-03-12 09:00:00', '2026-03-12 09:20:00', 1, '11111111-1111-1111-1111-111111111102');

INSERT IGNORE INTO review_reports (id, start_date, end_date, total_sales, score_sales, created_at, owner_id) VALUES
('15151515-1515-1515-1515-151515151001', '2026-03-01 00:00:00', '2026-03-10 23:59:59', 120000000, 8.5, '2026-03-11 09:00:00', '11111111-1111-1111-1111-111111111301');

INSERT IGNORE INTO notifications (id, title, content, direct_url, type, is_read, time_created, expiry, sender_id, receiver_id) VALUES
('16161616-1616-1616-1616-161616161001', 'Đơn hàng đã xác nhận', 'Đơn #001 đã được xác nhận', '/orders/99999999-9999-9999-9999-999999999001', 'ORDER', 0, '2026-03-08 10:06:00', '2026-04-08 10:06:00', '11111111-1111-1111-1111-111111111201', '11111111-1111-1111-1111-111111111101'),
('16161616-1616-1616-1616-161616161002', 'Khuyến mãi tháng 3',   'Mã VC20OFF đang có hiệu lực', '/vouchers', 'PROMOTION', 0, '2026-03-09 09:00:00', '2026-03-31 23:59:59', '11111111-1111-1111-1111-111111111202', '11111111-1111-1111-1111-111111111102');

INSERT IGNORE INTO mails (id, def_date, subject, body, recipient, is_sent, customer_id) VALUES
('17171717-1717-1717-1717-171717171001', '2026-03-08 10:07:00', 'Xác nhận đơn hàng #001', 'Đơn hàng của bạn đã được tiếp nhận.', 'an.customer@example.com', 1, '11111111-1111-1111-1111-111111111101'),
('17171717-1717-1717-1717-171717171002', '2026-03-09 09:05:00', 'Khuyến mãi đặc biệt',      'Sử dụng mã VC20OFF trước ngày 30/06.', 'binh.customer@example.com', 1, '11111111-1111-1111-1111-111111111102');

-- =========================================================
-- 1) USERS  (gốc: 5 -> thêm 11 -> tổng 16)
-- =========================================================
INSERT INTO users (id, username, password, full_name, email, phone, address, gender, role, is_active, created_at) VALUES
                                                                                                                      ('11111111-1111-1111-1111-111111111103', 'cus_chi',   '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Lê Thị Chi',       'chi.customer@example.com',    '0901000003', 'Huế',      'FEMALE', 'CUSTOMER', 1, '2026-03-02 08:00:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111104', 'cus_dat',   '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Nguyễn Mạnh Đạt',  'dat.customer@example.com',    '0901000004', 'TP.HCM',  'MALE',   'CUSTOMER', 1, '2026-03-02 08:30:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111105', 'cus_em',    '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Phạm Bích Em',     'em.customer@example.com',     '0901000005', 'Biên Hòa','FEMALE', 'CUSTOMER', 1, '2026-03-03 08:00:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111106', 'cus_phuc',  '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Hoàng Văn Phúc',   'phuc.customer@example.com',   '0901000006', 'Vũng Tàu','MALE',   'CUSTOMER', 1, '2026-03-03 09:00:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111107', 'cus_giang', '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Trần Kiều Giang',  'giang.customer@example.com',  '0901000007', 'Cần Thơ', 'FEMALE', 'CUSTOMER', 1, '2026-03-04 08:00:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111108', 'cus_hung',  '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Đỗ Việt Hung',     'hung.customer@example.com',   '0901000008', 'Hà Nội',  'MALE',   'CUSTOMER', 1, '2026-03-04 09:00:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111109', 'cus_khanh', '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Lê Minh Khanh',    'khanh.customer@example.com',  '0901000009', 'Nha Trắng','MALE',  'CUSTOMER', 1, '2026-03-04 10:00:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111110', 'cus_ly',    '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Nguyễn Bảo Ly',    'ly.customer@example.com',     '0901000010', 'Hải Phòng','FEMALE','CUSTOMER', 1, '2026-03-04 10:30:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111203', 'stf_kiet',  '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Nguyễn Xuan Kiet', 'kiet.staff@example.com',      '0902000003', 'TP.HCM',  'MALE',   'STAFF',    1, '2026-03-02 09:00:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111204', 'stf_lan',   '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Bùi Ngốc Lan',     'lan.staff@example.com',       '0902000004', 'Đà Nẵng', 'FEMALE', 'STAFF',    1, '2026-03-02 09:30:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111302', 'own_nam',   '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Phan Việt Nam',    'nam.owner@example.com',       '0903000002', 'TP.HCM',  'MALE',   'OWNER',    1, '2026-03-02 10:00:00');

-- sub-type
INSERT INTO customers (id) VALUES
                               ('11111111-1111-1111-1111-111111111103'),
                               ('11111111-1111-1111-1111-111111111104'),
                               ('11111111-1111-1111-1111-111111111105'),
                               ('11111111-1111-1111-1111-111111111106'),
                               ('11111111-1111-1111-1111-111111111107'),
                               ('11111111-1111-1111-1111-111111111108'),
                               ('11111111-1111-1111-1111-111111111109'),
                               ('11111111-1111-1111-1111-111111111110');

INSERT INTO staffs (id) VALUES
                            ('11111111-1111-1111-1111-111111111203'),
                            ('11111111-1111-1111-1111-111111111204');

INSERT INTO owners (id) VALUES
    ('11111111-1111-1111-1111-111111111302');

-- =========================================================
-- 1.1) THÊM 20 CUSTOMER MỚI (phân bổ cho product 012-031)
-- =========================================================
INSERT INTO users (id, username, password, full_name, email, phone, address, gender, role, is_active, created_at) VALUES
                                                                                                                      ('11111111-1111-1111-1111-111111111111', 'cus_hieu',   '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Lê Minh Hiếu',      'hieu.customer@example.com',   '0901000011', 'Hà Nội',      'MALE',   'CUSTOMER', 1, '2026-04-02 08:00:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111112', 'cus_mai',    '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Nguyễn Trúc Mai',   'mai.customer@example.com',    '0901000012', 'TP.HCM',      'FEMALE', 'CUSTOMER', 1, '2026-04-02 08:05:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111113', 'cus_khoa2',  '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Phan Đức Khoa',     'khoa2.customer@example.com',  '0901000013', 'Đà Nẵng',     'MALE',   'CUSTOMER', 1, '2026-04-02 08:10:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111114', 'cus_nhi',    '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Trần Bảo Nhi',      'nhi.customer@example.com',    '0901000014', 'Cần Thơ',     'FEMALE', 'CUSTOMER', 1, '2026-04-02 08:15:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111115', 'cus_tung',   '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Đặng Hoàng Tùng',   'tung.customer@example.com',   '0901000015', 'Hải Phòng',   'MALE',   'CUSTOMER', 1, '2026-04-02 08:20:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111116', 'cus_linh2',  '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Vũ Mỹ Linh',        'linh2.customer@example.com',  '0901000016', 'Huế',         'FEMALE', 'CUSTOMER', 1, '2026-04-02 08:25:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111117', 'cus_quan',   '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Ngô Minh Quân',     'quan.customer@example.com',   '0901000017', 'Quảng Ninh',  'MALE',   'CUSTOMER', 1, '2026-04-02 08:30:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111118', 'cus_ha2',    '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Đỗ Thu Hà',         'ha2.customer@example.com',    '0901000018', 'Nha Trang',   'FEMALE', 'CUSTOMER', 1, '2026-04-02 08:35:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111119', 'cus_viet',   '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Bùi Quốc Việt',     'viet.customer@example.com',   '0901000019', 'Bình Dương',  'MALE',   'CUSTOMER', 1, '2026-04-02 08:40:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111120', 'cus_anhthu', '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Lý Anh Thư',        'anhthu.customer@example.com', '0901000020', 'Đồng Nai',    'FEMALE', 'CUSTOMER', 1, '2026-04-02 08:45:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111121', 'cus_son',    '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Trịnh Thanh Sơn',   'son.customer@example.com',    '0901000021', 'Nam Định',    'MALE',   'CUSTOMER', 1, '2026-04-02 08:50:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111122', 'cus_yen',    '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Phạm Hải Yến',      'yen.customer@example.com',    '0901000022', 'Vĩnh Phúc',   'FEMALE', 'CUSTOMER', 1, '2026-04-02 08:55:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111123', 'cus_duc',    '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Nguyễn Minh Đức',   'duc.customer@example.com',    '0901000023', 'Thanh Hóa',   'MALE',   'CUSTOMER', 1, '2026-04-02 09:00:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111124', 'cus_thao',   '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Hoàng Ngọc Thảo',   'thao.customer@example.com',   '0901000024', 'Nghệ An',     'FEMALE', 'CUSTOMER', 1, '2026-04-02 09:05:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111125', 'cus_kiet2',  '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Lâm Đức Kiệt',      'kiet2.customer@example.com',  '0901000025', 'Bắc Ninh',    'MALE',   'CUSTOMER', 1, '2026-04-02 09:10:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111126', 'cus_phuong', '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Nguyễn Thu Phương', 'phuong.customer@example.com', '0901000026', 'Hưng Yên',    'FEMALE', 'CUSTOMER', 1, '2026-04-02 09:15:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111127', 'cus_nam2',   '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Đoàn Quốc Nam',     'nam2.customer@example.com',   '0901000027', 'Phú Thọ',     'MALE',   'CUSTOMER', 1, '2026-04-02 09:20:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111128', 'cus_huong',  '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Trần Thu Hương',    'huong.customer@example.com',  '0901000028', 'Bắc Giang',   'FEMALE', 'CUSTOMER', 1, '2026-04-02 09:25:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111129', 'cus_bao',    '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Phùng Gia Bảo',     'bao.customer@example.com',    '0901000029', 'Thái Bình',   'MALE',   'CUSTOMER', 1, '2026-04-02 09:30:00'),
                                                                                                                      ('11111111-1111-1111-1111-111111111130', 'cus_tram',   '$2a$12$JRXCc5f2OdjTBCwLotOgle1MLIzpuN7z/mlZoT5EO1LTdrmYdtDuu', 'Lê Ngọc Trâm',      'tram.customer@example.com',   '0901000030', 'Quảng Nam',   'FEMALE', 'CUSTOMER', 1, '2026-04-02 09:35:00');

INSERT INTO customers (id) VALUES
                               ('11111111-1111-1111-1111-111111111111'),
                               ('11111111-1111-1111-1111-111111111112'),
                               ('11111111-1111-1111-1111-111111111113'),
                               ('11111111-1111-1111-1111-111111111114'),
                               ('11111111-1111-1111-1111-111111111115'),
                               ('11111111-1111-1111-1111-111111111116'),
                               ('11111111-1111-1111-1111-111111111117'),
                               ('11111111-1111-1111-1111-111111111118'),
                               ('11111111-1111-1111-1111-111111111119'),
                               ('11111111-1111-1111-1111-111111111120'),
                               ('11111111-1111-1111-1111-111111111121'),
                               ('11111111-1111-1111-1111-111111111122'),
                               ('11111111-1111-1111-1111-111111111123'),
                               ('11111111-1111-1111-1111-111111111124'),
                               ('11111111-1111-1111-1111-111111111125'),
                               ('11111111-1111-1111-1111-111111111126'),
                               ('11111111-1111-1111-1111-111111111127'),
                               ('11111111-1111-1111-1111-111111111128'),
                               ('11111111-1111-1111-1111-111111111129'),
                               ('11111111-1111-1111-1111-111111111130');
-- =========================================================
-- 2) CATEGORIES (gốc: 3 → thêm 7 → tổng 10)
-- =========================================================
INSERT INTO categories (id, name, description) VALUES
                                                   ('22222222-2222-2222-2222-222222222004', 'Đồng hồ Thể thao',    'Chống nước, chống va, phù hợp vận động'),
                                                   ('22222222-2222-2222-2222-222222222005', 'Đồng hồ Nữ',          'Thiết kế nhỏ gọn, thời trang cho nữ'),
                                                   ('22222222-2222-2222-2222-222222222006', 'Đồng hồ Nam',         'Thiết kế mạnh mẽ, lịch sự cho nam'),
                                                   ('22222222-2222-2222-2222-222222222007', 'Đồng hồ Luxury',      'Đồng hồ cao cấp, đẳng cấp thượng lưu'),
                                                   ('22222222-2222-2222-2222-222222222008', 'Đồng hồ Đôi',         'Cặp đôi, phong cách tôn vinh tình yêu'),
                                                   ('22222222-2222-2222-2222-222222222009', 'Đồng hồ Trẻ Em',      'An toàn, bền, màu sắc sinh động'),
                                                   ('22222222-2222-2222-2222-222222222010', 'Đồng hồ Vintage',     'Phong cách cổ điển, hoài cổ');

-- =========================================================
-- 3) SUPPLIERS (gốc: 2 → thêm 8 → tổng 10)
-- =========================================================
INSERT INTO suppliers (id, name, contract_info, address) VALUES
                                                             ('33333333-3333-3333-3333-333333333003', 'Nhà cung cấp C', 'Hợp đồng 2026-03', 'Đà Nẵng'),
                                                             ('33333333-3333-3333-3333-333333333004', 'Nhà cung cấp D', 'Hợp đồng 2026-04', 'Huế'),
                                                             ('33333333-3333-3333-3333-333333333005', 'Nhà cung cấp E', 'Hợp đồng 2026-05', 'Cần Thơ'),
                                                             ('33333333-3333-3333-3333-333333333006', 'Seiko Distributor VN', 'Hợp đồng chính hãng 2026', 'TP.HCM'),
                                                             ('33333333-3333-3333-3333-333333333007', 'Casio Việt Nam',       'Hợp đồng chính hãng 2026', 'Hà Nội'),
                                                             ('33333333-3333-3333-3333-333333333008', 'Apple Authorized',     'Hợp đồng chính hãng 2026', 'TP.HCM'),
                                                             ('33333333-3333-3333-3333-333333333009', 'Orient VN',            'Hợp đồng 2026-09',          'Hà Nội'),
                                                             ('33333333-3333-3333-3333-333333333010', 'Citizen Distributor',  'Hợp đồng 2026-10',          'TP.HCM');

-- =========================================================
-- 4) VOUCHERS (gốc: 3 → thêm 8 → tổng 11)
-- =========================================================
INSERT INTO vouchers (id, code, discount_percent, usage_count, valid_from, valid_to, created_at, quantity, status) VALUES
                                                                                                                       ('66666666-6666-6666-6666-666666666004', 'VC15OFF',  15,  0, '2026-04-01 00:00:00', '2026-06-30 23:59:59', '2026-04-01 00:00:00',  80, 'ACTIVE'),
                                                                                                                       ('66666666-6666-6666-6666-666666666005', 'VC25OFF',  25,  0, '2026-04-01 00:00:00', '2026-05-31 23:59:59', '2026-04-01 00:00:00',  30, 'ACTIVE'),
                                                                                                                       ('66666666-6666-6666-6666-666666666006', 'SUMMER30', 30,  5, '2026-06-01 00:00:00', '2026-08-31 23:59:59', '2026-05-15 00:00:00', 200, 'ACTIVE'),
                                                                                                                       ('66666666-6666-6666-6666-666666666007', 'NEWUSER',   8,  0, '2026-01-01 00:00:00', '2026-12-31 23:59:59', '2026-01-01 00:00:00', 999, 'ACTIVE'),
                                                                                                                       ('66666666-6666-6666-6666-666666666008', 'FLASH50',  50,  2, '2026-03-15 00:00:00', '2026-03-15 23:59:59', '2026-03-14 00:00:00',  10, 'ACTIVE'),
                                                                                                                       ('66666666-6666-6666-6666-666666666009', 'VC12OFF',  12,  0, '2026-05-01 00:00:00', '2026-07-31 23:59:59', '2026-04-20 00:00:00',  60, 'ACTIVE'),
                                                                                                                       ('66666666-6666-6666-6666-666666666010', 'BIRTHDAY', 20,  1, '2026-01-01 00:00:00', '2026-12-31 23:59:59', '2026-01-01 00:00:00',  50, 'ACTIVE'),
                                                                                                                       ('66666666-6666-6666-6666-666666666011', 'ENDYEAR',  35,  0, '2026-12-01 00:00:00', '2026-12-31 23:59:59', '2026-11-01 00:00:00', 150, 'ACTIVE');

-- =========================================================
-- 5) PRODUCTS (gốc: 3 → thêm 8 → tổng 11)
-- =========================================================
INSERT INTO products (
    id, brand, name, description, price, stock_quantity,
    movement_type, glass_material, water_resistance, face_size,
    wire_material, wire_color, case_color, face_color,
    status, updated_at, category_id
) VALUES
      ('44444444-4444-4444-4444-444444444004', 'Orient', 'Orient Bambino V4',
       'Đồng hồ cổ điển phong cách Italian, mặt số trắng', 4800000, 12,
       'Automatic', 'Mineral', '30m', '40mm',
       'Da', 'Nâu', 'Vàng', 'Trắng',
       'ACTIVE', '2026-03-06 09:00:00', '22222222-2222-2222-2222-222222222001'),

      ('44444444-4444-4444-4444-444444444005', 'Citizen', 'Citizen Eco-Drive',
       'Đồng hồ quartz năng lượng mặt trời, không thay pin', 6200000, 18,
       'Quartz', 'Sapphire', '100m', '41mm',
       'Thép không gỉ', 'Bạc', 'Bạc', 'Xanh',
       'ACTIVE', '2026-03-06 09:30:00', '22222222-2222-2222-2222-222222222002'),

      ('44444444-4444-4444-4444-444444444006', 'Samsung', 'Galaxy Watch 6',
       'Đồng hồ thông minh Android, theo dõi sức khỏe toàn diện', 7500000, 25,
       'Digital', 'Sapphire', '5ATM', '44mm',
       'Silicone', 'Đen', 'Bạc', 'Đen',
       'ACTIVE', '2026-03-06 10:00:00', '22222222-2222-2222-2222-222222222003'),

      ('44444444-4444-4444-4444-444444444007', 'Casio', 'G-Shock GA-2100',
       'Đồng hồ thể thao chống va, chống nước 200m', 3200000, 30,
       'Quartz', 'Mineral', '200m', '45mm',
       'Resin', 'Đen', 'Đen', 'Đen',
       'ACTIVE', '2026-03-07 09:00:00', '22222222-2222-2222-2222-222222222004'),

      ('44444444-4444-4444-4444-444444444008', 'Seiko', 'Seiko Presage SSA399',
       'Đồng hồ cơ tự động mặt số xanh men phong cách Nhật', 12000000, 8,
       'Automatic', 'Sapphire', '100m', '40.5mm',
       'Thép không gỉ', 'Bạc', 'Vàng', 'Xanh',
       'ACTIVE', '2026-03-07 09:30:00', '22222222-2222-2222-2222-222222222007'),

      ('44444444-4444-4444-4444-444444444009', 'Fossil', 'Fossil Gen 6',
       'Đồng hồ thông minh thời trang, Wear OS', 5500000, 22,
       'Digital', 'Mineral', '3ATM', '44mm',
       'Silicone', 'Nâu', 'Vàng', 'Đen',
       'ACTIVE', '2026-03-08 09:00:00', '22222222-2222-2222-2222-222222222003'),

      ('44444444-4444-4444-4444-444444444010', 'Tissot', 'Tissot PRX',
       'Đồng hồ quartz Swiss cao cấp, mặt kính sapphire', 11000000, 10,
       'Quartz', 'Sapphire', '100m', '40mm',
       'Thép không gỉ', 'Bạc', 'Bạc', 'Xanh',
       'ACTIVE', '2026-03-08 09:30:00', '22222222-2222-2222-2222-222222222007'),

      ('44444444-4444-4444-4444-444444444011', 'Casio', 'Casio Baby-G BA-110',
       'Đồng hồ nữ thể thao, chống nước 100m, màu sắc tươi', 2400000, 35,
       'Quartz', 'Mineral', '100m', '38mm',
       'Resin', 'Hồng', 'Hồng', 'Trắng',
       'ACTIVE', '2026-03-09 09:00:00', '22222222-2222-2222-2222-222222222005');

-- =========================================================
-- 6) PRODUCT_IMAGES (gốc: 3 → thêm 8 → tổng 11)
-- =========================================================
INSERT INTO product_images (id, image_url, public_id, alt_text, is_thumbnail, product_id) VALUES
                                                                                              ('45454545-4545-4545-4545-454545454004', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775284784/4_xhhi6z.jpg', 'products/p4-main', 'Orient Bambino V4',    1, '44444444-4444-4444-4444-444444444004'),
                                                                                              ('45454545-4545-4545-4545-454545454005', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775284844/5_s25pkv.jpg', 'products/p5-main', 'Citizen Eco-Drive',    1, '44444444-4444-4444-4444-444444444005'),
                                                                                              ('45454545-4545-4545-4545-454545454006', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775284896/6_diuoua.png', 'products/p6-main', 'Galaxy Watch 6',       1, '44444444-4444-4444-4444-444444444006'),
                                                                                              ('45454545-4545-4545-4545-454545454007', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775284935/7_uxtsym.png', 'products/p7-main', 'G-Shock GA-2100',      1, '44444444-4444-4444-4444-444444444007'),
                                                                                              ('45454545-4545-4545-4545-454545454008', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775285006/8_twppiu.jpg', 'products/p8-main', 'Seiko Presage SSA399', 1, '44444444-4444-4444-4444-444444444008'),
                                                                                              ('45454545-4545-4545-4545-454545454009', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775285039/9_fyhj2t.jpg', 'products/p9-main', 'Fossil Gen 6',         1, '44444444-4444-4444-4444-444444444009'),
                                                                                              ('45454545-4545-4545-4545-454545454010', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775285084/11_qoi6zk.jpg','products/p10-main','Tissot PRX',           1, '44444444-4444-4444-4444-444444444010'),
                                                                                              ('45454545-4545-4545-4545-454545454011', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775285152/12_d5omp0.jpg','products/p11-main','Casio Baby-G BA-110',  1, '44444444-4444-4444-4444-444444444011'),
-- ảnh phụ cho sản phẩm gốc
                                                                                              ('45454545-4545-4545-4545-454545454012', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775285268/111_t7xkok.jpg', 'products/p1-side', 'Seiko 5 Sports - Side',0, '44444444-4444-4444-4444-444444444001'),
                                                                                              ('45454545-4545-4545-4545-454545454013', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775285306/123_tuglos.jpg', 'products/p2-side', 'Casio MTP-VT01 - Side',0, '44444444-4444-4444-4444-444444444002');

-- =========================================================
-- 6.1) PRODUCTS BỔ SUNG (thêm 20 mẫu, chia theo 6 hãng cao cấp)
-- =========================================================
INSERT INTO products (
    id, brand, name, description, price, stock_quantity,
    movement_type, glass_material, water_resistance, face_size,
    wire_material, wire_color, case_color, face_color,
    status, updated_at, category_id
) VALUES
      ('44444444-4444-4444-4444-444444444012', 'Rolex',     'Rolex Submariner Date',            'Biểu tượng diver sang trọng, độ hoàn thiện cao',            285000000, 6, 'Automatic', 'Sapphire', '300m', '41mm',   'Thép không gỉ', 'Bạc',  'Bạc',  'Đen',  'ACTIVE', '2026-04-02 09:00:00', '22222222-2222-2222-2222-222222222007'),
      ('44444444-4444-4444-4444-444444444013', 'Rolex',     'Rolex Datejust 36',               'Thiết kế kinh điển, phù hợp công sở cao cấp',              248000000, 7, 'Automatic', 'Sapphire', '100m', '36mm',   'Thép không gỉ', 'Bạc',  'Bạc',  'Xanh', 'ACTIVE', '2026-04-02 09:10:00', '22222222-2222-2222-2222-222222222007'),
      ('44444444-4444-4444-4444-444444444014', 'Rolex',     'Rolex GMT-Master II',             'Mẫu GMT cao cấp dành cho người thường xuyên di chuyển',    332000000, 5, 'Automatic', 'Sapphire', '100m', '40mm',   'Thép không gỉ', 'Bạc',  'Bạc',  'Đen',  'ACTIVE', '2026-04-02 09:20:00', '22222222-2222-2222-2222-222222222007'),
      ('44444444-4444-4444-4444-444444444015', 'Rolex',     'Rolex Oyster Perpetual 31',       'Thanh lịch tối giản, phù hợp cổ tay nhỏ',                  198000000, 8, 'Automatic', 'Sapphire', '100m', '31mm',   'Thép không gỉ', 'Bạc',  'Bạc',  'Trắng', 'ACTIVE', '2026-04-02 09:30:00', '22222222-2222-2222-2222-222222222005'),
      ('44444444-4444-4444-4444-444444444016', 'Cartier',   'Cartier Tank Must',               'Dáng chữ nhật biểu tượng của Cartier',                     128000000, 8, 'Quartz',    'Sapphire', '30m',  '33mm',   'Da',            'Đen',  'Bạc',  'Trắng', 'ACTIVE', '2026-04-02 09:40:00', '22222222-2222-2222-2222-222222222010'),
      ('44444444-4444-4444-4444-444444444017', 'Cartier',   'Cartier Santos de Cartier',       'Phong cách thể thao-lịch lãm đặc trưng',                   219000000, 6, 'Automatic', 'Sapphire', '100m', '39.8mm', 'Thép không gỉ', 'Bạc',  'Bạc',  'Trắng', 'ACTIVE', '2026-04-02 09:50:00', '22222222-2222-2222-2222-222222222007'),
      ('44444444-4444-4444-4444-444444444018', 'Cartier',   'Cartier Ballon Bleu 33',          'Thiết kế nữ cao cấp, mặt số thanh thoát',                  172000000, 7, 'Automatic', 'Sapphire', '30m',  '33mm',   'Thép không gỉ', 'Bạc',  'Bạc',  'Trắng', 'ACTIVE', '2026-04-02 10:00:00', '22222222-2222-2222-2222-222222222005'),
      ('44444444-4444-4444-4444-444444444019', 'Cartier',   'Cartier Pasha de Cartier',        'Mẫu cao cấp cá tính với crown cap đặc trưng',             236000000, 5, 'Automatic', 'Sapphire', '100m', '41mm',   'Da',            'Nâu',  'Bạc',  'Đen',  'ACTIVE', '2026-04-02 10:10:00', '22222222-2222-2222-2222-222222222007'),
      ('44444444-4444-4444-4444-444444444020', 'Omega',     'Omega Seamaster Diver 300M',      'Diver huyền thoại của Omega, bền bỉ và sang trọng',        162000000, 7, 'Automatic', 'Sapphire', '300m', '42mm',   'Thép không gỉ', 'Bạc',  'Bạc',  'Xanh', 'ACTIVE', '2026-04-02 10:20:00', '22222222-2222-2222-2222-222222222004'),
      ('44444444-4444-4444-4444-444444444021', 'Omega',     'Omega Speedmaster Moonwatch',     'Chronograph biểu tượng gắn với lịch sử không gian',        189000000, 6, 'Manual',    'Hesalite', '50m',  '42mm',   'Thép không gỉ', 'Bạc',  'Bạc',  'Đen',  'ACTIVE', '2026-04-02 10:30:00', '22222222-2222-2222-2222-222222222007'),
      ('44444444-4444-4444-4444-444444444022', 'Omega',     'Omega Constellation 29',          'Dòng nữ cao cấp, thiết kế tinh xảo',                       141000000, 8, 'Quartz',    'Sapphire', '50m',  '29mm',   'Thép không gỉ', 'Vàng', 'Vàng', 'Trắng', 'ACTIVE', '2026-04-02 10:40:00', '22222222-2222-2222-2222-222222222005'),
      ('44444444-4444-4444-4444-444444444023', 'Longines',  'Longines Master Collection',      'Phong cách cổ điển, hoàn thiện tinh tế',                  69000000,  10, 'Automatic', 'Sapphire', '30m',  '40mm',   'Da',            'Nâu',  'Bạc',  'Trắng', 'ACTIVE', '2026-04-02 10:50:00', '22222222-2222-2222-2222-222222222001'),
      ('44444444-4444-4444-4444-444444444024', 'Longines',  'Longines HydroConquest',          'Diver thanh lịch, cân bằng thể thao và sang trọng',        54000000,  11, 'Automatic', 'Sapphire', '300m', '41mm',   'Thép không gỉ', 'Bạc',  'Bạc',  'Đen',  'ACTIVE', '2026-04-02 11:00:00', '22222222-2222-2222-2222-222222222004'),
      ('44444444-4444-4444-4444-444444444025', 'Longines',  'Longines DolceVita',              'Mẫu dresswatch phong cách Art Deco',                       48000000,  9, 'Quartz',    'Sapphire', '30m',  '32mm',   'Da',            'Đen',  'Bạc',  'Trắng', 'ACTIVE', '2026-04-02 11:10:00', '22222222-2222-2222-2222-222222222010'),
      ('44444444-4444-4444-4444-444444444026', 'TAG Heuer', 'TAG Heuer Carrera Calibre 5',     'Thiết kế đua xe lịch lãm, dễ đeo hàng ngày',               82000000,  9, 'Automatic', 'Sapphire', '100m', '41mm',   'Thép không gỉ', 'Bạc',  'Bạc',  'Đen',  'ACTIVE', '2026-04-02 11:20:00', '22222222-2222-2222-2222-222222222007'),
      ('44444444-4444-4444-4444-444444444027', 'TAG Heuer', 'TAG Heuer Aquaracer Professional 300', 'Diver chuyên nghiệp, độ bền cao',                    76000000,  10, 'Automatic', 'Sapphire', '300m', '43mm',   'Thép không gỉ', 'Bạc',  'Bạc',  'Xanh', 'ACTIVE', '2026-04-02 11:30:00', '22222222-2222-2222-2222-222222222004'),
      ('44444444-4444-4444-4444-444444444028', 'TAG Heuer', 'TAG Heuer Monaco',                 'Thiết kế vuông biểu tượng, phong cách vintage',            99000000,  7, 'Automatic', 'Sapphire', '100m', '39mm',   'Da',            'Đen',  'Bạc',  'Xanh', 'ACTIVE', '2026-04-02 11:40:00', '22222222-2222-2222-2222-222222222010'),
      ('44444444-4444-4444-4444-444444444029', 'Rado',      'Rado Captain Cook',                'Thiết kế thể thao cao cấp với chất liệu bền nhẹ',          67000000,  11, 'Automatic', 'Sapphire', '300m', '42mm',   'Thép không gỉ', 'Bạc',  'Bạc',  'Xanh', 'ACTIVE', '2026-04-02 11:50:00', '22222222-2222-2222-2222-222222222004'),
      ('44444444-4444-4444-4444-444444444030', 'Rado',      'Rado True Square',                 'Thiết kế vuông ceramic hiện đại',                           72000000,  8, 'Automatic', 'Sapphire', '50m',  '38mm',   'Ceramic',       'Đen',  'Đen',  'Đen',  'ACTIVE', '2026-04-02 12:00:00', '22222222-2222-2222-2222-222222222010'),
      ('44444444-4444-4444-4444-444444444031', 'Rado',      'Rado Centrix Automatic',           'Mẫu dresswatch cao cấp phối ceramic và thép',              58000000,  10, 'Automatic', 'Sapphire', '50m',  '39mm',   'Ceramic',       'Đen',  'Bạc',  'Trắng', 'ACTIVE', '2026-04-02 12:10:00', '22222222-2222-2222-2222-222222222005');

-- =========================================================
-- 6.2) PRODUCT_IMAGES BỔ SUNG (1 ảnh chính / sản phẩm)
-- =========================================================
INSERT INTO product_images (id, image_url, public_id, alt_text, is_thumbnail, product_id) VALUES
                                                                                              ('45454545-4545-4545-4545-454545454014', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775406805/Rolex_Submariner_Date_126610LV-0002_2_o4ocay.jpg', 'products/p12-main', 'Rolex Submariner Date',                 1, '44444444-4444-4444-4444-444444444012'),
                                                                                              ('45454545-4545-4545-4545-454545454015', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775406844/dong-ho-rolex-datejust-36-126233-0026-oystersteel-va-vang-vang-topwatch.vn__hiarhi.webp', 'products/p13-main', 'Rolex Datejust 36',                    1, '44444444-4444-4444-4444-444444444013'),
                                                                                              ('45454545-4545-4545-4545-454545454016', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775406872/Rolex-GMT-Master-II-126713GRNR-0001_oaudnf.jpg', 'products/p14-main', 'Rolex GMT-Master II',                  1, '44444444-4444-4444-4444-444444444014'),
                                                                                              ('45454545-4545-4545-4545-454545454017', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775406928/images_o76nnd.jpg', 'products/p15-main', 'Rolex Oyster Perpetual 31',            1, '44444444-4444-4444-4444-444444444015'),
                                                                                              ('45454545-4545-4545-4545-454545454018', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775406940/CRWSTA0054_pvwmol.jpg', 'products/p16-main', 'Cartier Tank Must',                    1, '44444444-4444-4444-4444-444444444016'),
                                                                                              ('45454545-4545-4545-4545-454545454019', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775406994/images_hexfvx.jpg', 'products/p17-main', 'Cartier Santos de Cartier',            1, '44444444-4444-4444-4444-444444444017'),
                                                                                              ('45454545-4545-4545-4545-454545454020', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407029/CRW2BB0023_1_cnfc4g.webp', 'products/p18-main', 'Cartier Ballon Bleu 33',               1, '44444444-4444-4444-4444-444444444018'),
                                                                                              ('45454545-4545-4545-4545-454545454021', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407072/image_fdvl7z.png', 'products/p19-main', 'Cartier Pasha de Cartier',             1, '44444444-4444-4444-4444-444444444019'),
                                                                                              ('45454545-4545-4545-4545-454545454022', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407073/z2545704360253_7780e9664d317103a420878fed4116e2_bwwr09.jpg', 'products/p20-main', 'Omega Seamaster Diver 300M',          1, '44444444-4444-4444-4444-444444444020'),
                                                                                              ('45454545-4545-4545-4545-454545454023', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407075/images_1_wwaqu6.jpg', 'products/p21-main', 'Omega Speedmaster Moonwatch',          1, '44444444-4444-4444-4444-444444444021'),
                                                                                              ('45454545-4545-4545-4545-454545454024', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407092/images_2_vcevfd.jpg', 'products/p22-main', 'Omega Constellation 29',              1, '44444444-4444-4444-4444-444444444022'),
                                                                                              ('45454545-4545-4545-4545-454545454025', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407104/images_3_qfnwtj.jpg', 'products/p23-main', 'Longines Master Collection',          1, '44444444-4444-4444-4444-444444444023'),
                                                                                              ('45454545-4545-4545-4545-454545454026', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407113/longines-l3-782-3-56-7-1_1726213126_aqwap1.jpg', 'products/p24-main', 'Longines HydroConquest',              1, '44444444-4444-4444-4444-444444444024'),
                                                                                              ('45454545-4545-4545-4545-454545454027', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407130/L5.255.0.71.2_Desktop_1_anwd1j.jpg', 'products/p25-main', 'Longines DolceVita',                  1, '44444444-4444-4444-4444-444444444025'),
                                                                                              ('45454545-4545-4545-4545-454545454028', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407151/images_4_akml3i.jpg', 'products/p26-main', 'TAG Heuer Carrera Calibre 5',         1, '44444444-4444-4444-4444-444444444026'),
                                                                                              ('45454545-4545-4545-4545-454545454029', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407198/images_5_qvdlo5.jpg', 'products/p27-main', 'TAG Heuer Aquaracer Professional 300', 1, '44444444-4444-4444-4444-444444444027'),
                                                                                              ('45454545-4545-4545-4545-454545454030', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407200/images_6_xbkveb.jpg', 'products/p28-main', 'TAG Heuer Monaco',                    1, '44444444-4444-4444-4444-444444444028'),
                                                                                              ('45454545-4545-4545-4545-454545454031', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407202/r32138303_1706515282_zkuidx.jpg', 'products/p29-main', 'Rado Captain Cook',                   1, '44444444-4444-4444-4444-444444444029'),
                                                                                              ('45454545-4545-4545-4545-454545454032', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407229/dong-ho-rado-r27086162-1-_1664622148_kkh9wa.jpg', 'products/p30-main', 'Rado True Square',                    1, '44444444-4444-4444-4444-444444444030'),
                                                                                              ('45454545-4545-4545-4545-454545454033', 'https://res.cloudinary.com/dfz0c8xcx/image/upload/v1775407231/r30013302_1706525821_tzjsev.jpg', 'products/p31-main', 'Rado Centrix Automatic',             1, '44444444-4444-4444-4444-444444444031');

-- =========================================================
-- 7) CARTS (gốc: 2 → thêm 8 → tổng 10)
-- =========================================================
INSERT INTO carts (id, total_amount, customer_id) VALUES
                                                      ('77777777-7777-7777-7777-777777777003',  4800000, '11111111-1111-1111-1111-111111111103'),
                                                      ('77777777-7777-7777-7777-777777777004',  6200000, '11111111-1111-1111-1111-111111111104'),
                                                      ('77777777-7777-7777-7777-777777777005',  7500000, '11111111-1111-1111-1111-111111111105'),
                                                      ('77777777-7777-7777-7777-777777777006',  3200000, '11111111-1111-1111-1111-111111111106'),
                                                      ('77777777-7777-7777-7777-777777777007', 12000000, '11111111-1111-1111-1111-111111111107'),
                                                      ('77777777-7777-7777-7777-777777777008',  5500000, '11111111-1111-1111-1111-111111111108'),
                                                      ('77777777-7777-7777-7777-777777777009', 11000000, '11111111-1111-1111-1111-111111111109'),
                                                      ('77777777-7777-7777-7777-777777777010',  2400000, '11111111-1111-1111-1111-111111111110');
-- =========================================================
-- 8) CART_ITEMS (gốc: 2 → thêm 9 → tổng 11)
-- =========================================================
INSERT INTO cart_items (id, quantity, sub_total, cart_id, product_id) VALUES
                                                                          ('88888888-8888-8888-8888-888888888003', 1,  4800000, '77777777-7777-7777-7777-777777777003', '44444444-4444-4444-4444-444444444004'),
                                                                          ('88888888-8888-8888-8888-888888888004', 1,  6200000, '77777777-7777-7777-7777-777777777004', '44444444-4444-4444-4444-444444444005'),
                                                                          ('88888888-8888-8888-8888-888888888005', 1,  7500000, '77777777-7777-7777-7777-777777777005', '44444444-4444-4444-4444-444444444006'),
                                                                          ('88888888-8888-8888-8888-888888888006', 1,  3200000, '77777777-7777-7777-7777-777777777006', '44444444-4444-4444-4444-444444444007'),
                                                                          ('88888888-8888-8888-8888-888888888007', 1, 12000000, '77777777-7777-7777-7777-777777777007', '44444444-4444-4444-4444-444444444008'),
                                                                          ('88888888-8888-8888-8888-888888888008', 1,  5500000, '77777777-7777-7777-7777-777777777008', '44444444-4444-4444-4444-444444444009'),
                                                                          ('88888888-8888-8888-8888-888888888009', 1, 11000000, '77777777-7777-7777-7777-777777777009', '44444444-4444-4444-4444-444444444010'),
                                                                          ('88888888-8888-8888-8888-888888888010', 1,  2400000, '77777777-7777-7777-7777-777777777010', '44444444-4444-4444-4444-444444444011'),
                                                                          ('88888888-8888-8888-8888-888888888011', 2,  6400000, '77777777-7777-7777-7777-777777777006', '44444444-4444-4444-4444-444444444011');

-- =========================================================
-- 9) ORDERS (gốc: 2 → thêm 9 → tổng 11)
-- =========================================================
INSERT INTO orders (id, order_date, total_amount, note, shipping_address, status, customer_id, voucher_id) VALUES
                                                                                                               ('99999999-9999-9999-9999-999999999003', '2026-03-10 10:00:00',  4080000, 'Để ở cổng',          'Huế',      'DELIVERED',  '11111111-1111-1111-1111-111111111103', '66666666-6666-6666-6666-666666666001'),
                                                                                                               ('99999999-9999-9999-9999-999999999004', '2026-03-11 11:00:00',  6200000, 'Gói quà tặng',       'TP.HCM',   'CONFIRMED',  '11111111-1111-1111-1111-111111111104', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999005', '2026-03-12 09:30:00',  7500000, NULL,                 'Biên Hòa', 'PENDING',    '11111111-1111-1111-1111-111111111105', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999006', '2026-03-13 14:00:00',  2560000, 'Giao nhanh',         'Vũng Tàu', 'SHIPPING',   '11111111-1111-1111-1111-111111111106', '66666666-6666-6666-6666-666666666002'),
                                                                                                               ('99999999-9999-9999-9999-999999999007', '2026-03-14 08:00:00', 12000000, 'Hàng luxury, cẩn thận','Cần Thơ','CONFIRMED',  '11111111-1111-1111-1111-111111111107', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999008', '2026-03-15 10:30:00',  5500000, NULL,                 'Hà Nội',   'PENDING',    '11111111-1111-1111-1111-111111111108', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999009', '2026-03-16 13:00:00',  9900000, 'Mua thêm 1 chiếc',   'Hà Nội',   'DELIVERED',  '11111111-1111-1111-1111-111111111101', '66666666-6666-6666-6666-666666666004'),
                                                                                                               ('99999999-9999-9999-9999-999999999010', '2026-03-17 15:00:00',  2400000, 'Quà sinh nhật',      'Đà Nẵng',  'CANCELLED',  '11111111-1111-1111-1111-111111111102', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999011', '2026-03-18 09:00:00',  8800000, 'Đơn mua chung',      'TP.HCM',   'CONFIRMED',  '11111111-1111-1111-1111-111111111104', '66666666-6666-6666-6666-666666666004');

-- Bổ sung 20 đơn hàng mới cho 20 customer mới (mỗi customer 1 product mới)
INSERT INTO orders (id, order_date, total_amount, note, shipping_address, status, customer_id, voucher_id) VALUES
                                                                                                               ('99999999-9999-9999-9999-999999999012', '2026-04-03 09:00:00', 285000000, 'Khách VIP đặt online',       'Hà Nội',      'CONFIRMED', '11111111-1111-1111-1111-111111111111', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999013', '2026-04-03 09:10:00', 248000000, 'Yêu cầu đóng gói quà',       'TP.HCM',      'CONFIRMED', '11111111-1111-1111-1111-111111111112', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999014', '2026-04-03 09:20:00', 332000000, 'Giao giờ hành chính',         'Đà Nẵng',     'PENDING',   '11111111-1111-1111-1111-111111111113', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999015', '2026-04-03 09:30:00', 198000000, 'Liên hệ trước khi giao',      'Cần Thơ',     'DELIVERED', '11111111-1111-1111-1111-111111111114', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999016', '2026-04-03 09:40:00', 128000000, 'Giao nhanh trong ngày',       'Hải Phòng',   'CONFIRMED', '11111111-1111-1111-1111-111111111115', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999017', '2026-04-03 09:50:00', 219000000, 'Khách cần hóa đơn công ty',   'Huế',         'PENDING',   '11111111-1111-1111-1111-111111111116', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999018', '2026-04-03 10:00:00', 172000000, 'Gọi xác nhận size cổ tay',    'Quảng Ninh',  'CONFIRMED', '11111111-1111-1111-1111-111111111117', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999019', '2026-04-03 10:10:00', 236000000, 'Giao buổi chiều',             'Nha Trang',   'PENDING',   '11111111-1111-1111-1111-111111111118', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999020', '2026-04-03 10:20:00', 162000000, 'Khách đã chuyển khoản',       'Bình Dương',  'DELIVERED', '11111111-1111-1111-1111-111111111119', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999021', '2026-04-03 10:30:00', 189000000, 'Đơn ưu tiên',                 'Đồng Nai',    'CONFIRMED', '11111111-1111-1111-1111-111111111120', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999022', '2026-04-03 10:40:00', 141000000, 'Đóng hộp cao cấp',            'Nam Định',    'PENDING',   '11111111-1111-1111-1111-111111111121', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999023', '2026-04-03 10:50:00',  69000000, 'Mua tặng đối tác',            'Vĩnh Phúc',   'CONFIRMED', '11111111-1111-1111-1111-111111111122', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999024', '2026-04-03 11:00:00',  54000000, 'Giao COD',                    'Thanh Hóa',   'PENDING',   '11111111-1111-1111-1111-111111111123', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999025', '2026-04-03 11:10:00',  48000000, 'Đã xác minh số điện thoại',   'Nghệ An',     'CONFIRMED', '11111111-1111-1111-1111-111111111124', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999026', '2026-04-03 11:20:00',  82000000, 'Khách muốn bảo hiểm vận chuyển','Bắc Ninh',  'PENDING',   '11111111-1111-1111-1111-111111111125', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999027', '2026-04-03 11:30:00',  76000000, 'Giao tại văn phòng',          'Hưng Yên',    'CONFIRMED', '11111111-1111-1111-1111-111111111126', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999028', '2026-04-03 11:40:00',  99000000, 'Đơn hàng thương hiệu cao cấp', 'Phú Thọ',     'DELIVERED', '11111111-1111-1111-1111-111111111127', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999029', '2026-04-03 11:50:00',  67000000, 'Giao thứ bảy',                'Bắc Giang',   'CONFIRMED', '11111111-1111-1111-1111-111111111128', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999030', '2026-04-03 12:00:00',  72000000, 'Ưu tiên đóng gói chống sốc',   'Thái Bình',   'PENDING',   '11111111-1111-1111-1111-111111111129', NULL),
                                                                                                               ('99999999-9999-9999-9999-999999999031', '2026-04-03 12:10:00',  58000000, 'Khách đã xác nhận nhận hàng',  'Quảng Nam',   'CONFIRMED', '11111111-1111-1111-1111-111111111130', NULL);

-- =========================================================
-- 10) ORDER_ITEMS (gốc: 2 → thêm 9 → tổng 11)
-- =========================================================
INSERT INTO order_items (id, quantity, sub_total, order_id, product_id) VALUES
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa003', 1,  4800000, '99999999-9999-9999-9999-999999999003', '44444444-4444-4444-4444-444444444004'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa004', 1,  6200000, '99999999-9999-9999-9999-999999999004', '44444444-4444-4444-4444-444444444005'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa005', 1,  7500000, '99999999-9999-9999-9999-999999999005', '44444444-4444-4444-4444-444444444006'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa006', 2,  6400000, '99999999-9999-9999-9999-999999999006', '44444444-4444-4444-4444-444444444011'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa007', 1, 12000000, '99999999-9999-9999-9999-999999999007', '44444444-4444-4444-4444-444444444008'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa008', 1,  5500000, '99999999-9999-9999-9999-999999999008', '44444444-4444-4444-4444-444444444009'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa009', 1, 11000000, '99999999-9999-9999-9999-999999999009', '44444444-4444-4444-4444-444444444010'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa010', 1,  2400000, '99999999-9999-9999-9999-999999999010', '44444444-4444-4444-4444-444444444011'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa011', 1,  8800000, '99999999-9999-9999-9999-999999999011', '44444444-4444-4444-4444-444444444010');

-- Đồng bộ 20 product mới vào order_items (mỗi đơn 1 sản phẩm)
INSERT INTO order_items (id, quantity, sub_total, order_id, product_id) VALUES
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa012', 1, 285000000, '99999999-9999-9999-9999-999999999012', '44444444-4444-4444-4444-444444444012'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa013', 1, 248000000, '99999999-9999-9999-9999-999999999013', '44444444-4444-4444-4444-444444444013'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa014', 1, 332000000, '99999999-9999-9999-9999-999999999014', '44444444-4444-4444-4444-444444444014'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa015', 1, 198000000, '99999999-9999-9999-9999-999999999015', '44444444-4444-4444-4444-444444444015'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa016', 1, 128000000, '99999999-9999-9999-9999-999999999016', '44444444-4444-4444-4444-444444444016'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa017', 1, 219000000, '99999999-9999-9999-9999-999999999017', '44444444-4444-4444-4444-444444444017'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa018', 1, 172000000, '99999999-9999-9999-9999-999999999018', '44444444-4444-4444-4444-444444444018'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa019', 1, 236000000, '99999999-9999-9999-9999-999999999019', '44444444-4444-4444-4444-444444444019'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa020', 1, 162000000, '99999999-9999-9999-9999-999999999020', '44444444-4444-4444-4444-444444444020'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa021', 1, 189000000, '99999999-9999-9999-9999-999999999021', '44444444-4444-4444-4444-444444444021'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa022', 1, 141000000, '99999999-9999-9999-9999-999999999022', '44444444-4444-4444-4444-444444444022'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa023', 1,  69000000, '99999999-9999-9999-9999-999999999023', '44444444-4444-4444-4444-444444444023'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa024', 1,  54000000, '99999999-9999-9999-9999-999999999024', '44444444-4444-4444-4444-444444444024'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa025', 1,  48000000, '99999999-9999-9999-9999-999999999025', '44444444-4444-4444-4444-444444444025'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa026', 1,  82000000, '99999999-9999-9999-9999-999999999026', '44444444-4444-4444-4444-444444444026'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa027', 1,  76000000, '99999999-9999-9999-9999-999999999027', '44444444-4444-4444-4444-444444444027'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa028', 1,  99000000, '99999999-9999-9999-9999-999999999028', '44444444-4444-4444-4444-444444444028'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa029', 1,  67000000, '99999999-9999-9999-9999-999999999029', '44444444-4444-4444-4444-444444444029'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa030', 1,  72000000, '99999999-9999-9999-9999-999999999030', '44444444-4444-4444-4444-444444444030'),
                                                                            ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaa031', 1,  58000000, '99999999-9999-9999-9999-999999999031', '44444444-4444-4444-4444-444444444031');

-- =========================================================
-- 11) ORDER_STATUS_HISTORY (gốc: 2 → thêm 10 → tổng 12)
-- =========================================================
INSERT INTO order_status_history (id, order_id, status, note, changed_at, changed_by) VALUES
                                                                                          ('abababab-abab-abab-abab-ababababab03', '99999999-9999-9999-9999-999999999003', 'CONFIRMED',  'Xác nhận đơn hàng',      '2026-03-10 10:01:00', 'stf_dung'),
                                                                                          ('abababab-abab-abab-abab-ababababab04', '99999999-9999-9999-9999-999999999003', 'SHIPPING',   'Đang giao hàng',         '2026-03-11 08:00:00', 'stf_ha'),
                                                                                          ('abababab-abab-abab-abab-ababababab05', '99999999-9999-9999-9999-999999999003', 'DELIVERED',  'Giao thành công',        '2026-03-12 16:00:00', 'stf_ha'),
                                                                                          ('abababab-abab-abab-abab-ababababab06', '99999999-9999-9999-9999-999999999004', 'CONFIRMED',  'Đơn đã xác nhận',        '2026-03-11 11:01:00', 'stf_kiet'),
                                                                                          ('abababab-abab-abab-abab-ababababab07', '99999999-9999-9999-9999-999999999005', 'PENDING',    'Đơn mới tạo',            '2026-03-12 09:30:00', 'cus_em'),
                                                                                          ('abababab-abab-abab-abab-ababababab08', '99999999-9999-9999-9999-999999999006', 'CONFIRMED',  'Xác nhận',               '2026-03-13 14:01:00', 'stf_lan'),
                                                                                          ('abababab-abab-abab-abab-ababababab09', '99999999-9999-9999-9999-999999999006', 'SHIPPING',   'Đang vận chuyển',        '2026-03-14 09:00:00', 'stf_lan'),
                                                                                          ('abababab-abab-abab-abab-ababababab10', '99999999-9999-9999-9999-999999999007', 'CONFIRMED',  'Đơn hàng cao cấp OK',    '2026-03-14 08:01:00', 'stf_dung'),
                                                                                          ('abababab-abab-abab-abab-ababababab11', '99999999-9999-9999-9999-999999999010', 'PENDING',    'Đơn mới',                '2026-03-17 15:00:00', 'cus_binh'),
                                                                                          ('abababab-abab-abab-abab-ababababab12', '99999999-9999-9999-9999-999999999010', 'CANCELLED',  'Khách hủy đơn',          '2026-03-17 16:00:00', 'cus_binh');

-- =========================================================
-- 12) PAYMENTS (gốc: 2 → thêm 9 → tổng 11)
-- =========================================================
INSERT INTO payments (id, payment_date, amount, method, status, is_paid, order_id) VALUES
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb003', '2026-03-10 10:10:00',  4080000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999003'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb004', '2026-03-11 11:20:00',  6200000, 'COD',           'RECEIVED',  0, '99999999-9999-9999-9999-999999999004'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb005', '2026-03-12 09:35:00',  7500000, 'VNPAY', 'PROCESSING',0, '99999999-9999-9999-9999-999999999005'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb006', '2026-03-13 14:05:00',  2560000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999006'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb007', '2026-03-14 08:05:00', 12000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999007'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb008', '2026-03-15 10:35:00',  5500000, 'COD',           'RECEIVED',  0, '99999999-9999-9999-9999-999999999008'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb009', '2026-03-16 13:10:00',  9900000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999009'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb010', '2026-03-17 15:05:00',  2400000, 'COD',           'REJECTED',  0, '99999999-9999-9999-9999-999999999010'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb011', '2026-03-18 09:10:00',  8800000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999011'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb012', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999012'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb013', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999013'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb014', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999014'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb015', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999015'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb016', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999016'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb017', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999017'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb018', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999018'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb019', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999019'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb020', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999020'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb021', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999021'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb022', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999022'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb023', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999023'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb024', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999024'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb025', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999025'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb026', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999026'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb027', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999027'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb028', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999028'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb029', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999029'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb030', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999030'),
                                                                                       ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbb031', '2026-04-03 10:00:00', 5000000, 'VNPAY', 'COMPLETED', 1, '99999999-9999-9999-9999-999999999031');


-- =========================================================
-- 14) WARRANTIES (gốc: 2 → thêm 8 → tổng 10)
-- =========================================================
INSERT INTO warranties (
    id, customer_phone, customer_name, issue_description,
    received_date, expected_return_date, status,
    technician_note, reject_reason, quantity, product_id
) VALUES
      ('dddddddd-dddd-dddd-dddd-ddddddddd003', '0901000003', 'Lê Thị Chi',      'Kim giây không chạy',
       '2026-03-14 09:00:00', '2026-03-22 17:00:00', 'PROCESSING', 'Thay pin', NULL, 1, '44444444-4444-4444-4444-444444444002'),

      ('dddddddd-dddd-dddd-dddd-ddddddddd004', '0901000004', 'Nguyễn Mạnh Đạt', 'Nước vào đồng hồ',
       '2026-03-15 09:00:00', '2026-03-25 17:00:00', 'RECEIVED',   'Kiểm tra gioăng cao su', NULL, 1, '44444444-4444-4444-4444-444444444003'),

      ('dddddddd-dddd-dddd-dddd-ddddddddd005', '0901000005', 'Phạm Bích Em',    'Màn hình bị liệt',
       '2026-03-16 10:00:00', '2026-03-26 17:00:00', 'PROCESSING', 'Thay màn hình', NULL, 1, '44444444-4444-4444-4444-444444444006'),

      ('dddddddd-dddd-dddd-dddd-ddddddddd006', '0901000006', 'Hoàng Văn Phúc',  'Đồng hồ cơ chạy quá nhanh',
       '2026-03-17 09:00:00', '2026-03-28 17:00:00', 'RECEIVED',   'Căn chỉnh bộ máy', NULL, 1, '44444444-4444-4444-4444-444444444004'),

      ('dddddddd-dddd-dddd-dddd-ddddddddd007', '0901000007', 'Trần Kiều Giang', 'Mặt kính nứt nhỏ',
       '2026-03-18 09:30:00', '2026-03-29 17:00:00', 'COMPLETED',  'Đã thay mặt kính', NULL, 1, '44444444-4444-4444-4444-444444444005'),

      ('dddddddd-dddd-dddd-dddd-ddddddddd008', '0901000008', 'Đỗ Việt Hùng',    'Chức năng GPS lỗi',
       '2026-03-19 10:00:00', '2026-03-30 17:00:00', 'PROCESSING', 'Cập nhật firmware', NULL, 1, '44444444-4444-4444-4444-444444444009'),

      ('dddddddd-dddd-dddd-dddd-ddddddddd009', '0902000001', 'Phạm Quốc Dũng',  'Dây cao su bị bong',
       '2026-03-20 09:00:00', '2026-03-31 17:00:00', 'RECEIVED',   'Thay dây mới', NULL, 1, '44444444-4444-4444-4444-444444444007'),

      ('dddddddd-dddd-dddd-dddd-ddddddddd010', '0902000002', 'Võ Thu Hà',       'Nuống có bị rỉ sét',
       '2026-03-21 09:00:00', '2026-04-01 17:00:00', 'REJECTED',   NULL, 'Không trong thời hạn bảo hành', 1, '44444444-4444-4444-4444-444444444008');


-- =========================================================
-- 16) IMPORT_RECEIPTS (gốc: 2 → thêm 8 → tổng 10)
-- =========================================================
INSERT INTO import_receipts (id, import_date, note, supplier_id, owner_id) VALUES
                                                                               ('ffffffff-ffff-ffff-ffff-fffffffff003', '2026-03-10 08:00:00', 'Nhập lô Orient',         '33333333-3333-3333-3333-333333333006', '11111111-1111-1111-1111-111111111302'),
                                                                               ('ffffffff-ffff-ffff-ffff-fffffffff004', '2026-03-11 08:30:00', 'Nhập lô Citizen',        '33333333-3333-3333-3333-333333333010', '11111111-1111-1111-1111-111111111302'),
                                                                               ('ffffffff-ffff-ffff-ffff-fffffffff005', '2026-03-12 09:00:00', 'Nhập lô Samsung',        '33333333-3333-3333-3333-333333333003', '11111111-1111-1111-1111-111111111302'),
                                                                               ('ffffffff-ffff-ffff-ffff-fffffffff006', '2026-03-13 08:00:00', 'Nhập lô G-Shock',        '33333333-3333-3333-3333-333333333007', '11111111-1111-1111-1111-111111111302'),
                                                                               ('ffffffff-ffff-ffff-ffff-fffffffff007', '2026-03-14 08:00:00', 'Nhập lô Seiko Presage',  '33333333-3333-3333-3333-333333333006', '11111111-1111-1111-1111-111111111302'),
                                                                               ('ffffffff-ffff-ffff-ffff-fffffffff008', '2026-03-15 08:30:00', 'Nhập lô Fossil',         '33333333-3333-3333-3333-333333333004', '11111111-1111-1111-1111-111111111302'),
                                                                               ('ffffffff-ffff-ffff-ffff-fffffffff009', '2026-03-16 08:00:00', 'Nhập lô Tissot',         '33333333-3333-3333-3333-333333333005', '11111111-1111-1111-1111-111111111302'),
                                                                               ('ffffffff-ffff-ffff-ffff-fffffffff010', '2026-03-17 09:00:00', 'Nhập lô Baby-G',         '33333333-3333-3333-3333-333333333007', '11111111-1111-1111-1111-111111111302');

-- =========================================================
-- 17) IMPORT_DETAILS (gốc: 2 → thêm 9 → tổng 11)
-- =========================================================
INSERT INTO import_details (id, quantity, import_price, import_receipt_id, product_id) VALUES
                                                                                           ('12121212-1212-1212-1212-121212121003', 12, 3800000, 'ffffffff-ffff-ffff-ffff-fffffffff003', '44444444-4444-4444-4444-444444444004'),
                                                                                           ('12121212-1212-1212-1212-121212121004', 15, 5000000, 'ffffffff-ffff-ffff-ffff-fffffffff004', '44444444-4444-4444-4444-444444444005'),
                                                                                           ('12121212-1212-1212-1212-121212121005', 20, 6000000, 'ffffffff-ffff-ffff-ffff-fffffffff005', '44444444-4444-4444-4444-444444444006'),
                                                                                           ('12121212-1212-1212-1212-121212121006', 25, 2500000, 'ffffffff-ffff-ffff-ffff-fffffffff006', '44444444-4444-4444-4444-444444444007'),
                                                                                           ('12121212-1212-1212-1212-121212121007',  8, 9500000, 'ffffffff-ffff-ffff-ffff-fffffffff007', '44444444-4444-4444-4444-444444444008'),
                                                                                           ('12121212-1212-1212-1212-121212121008', 18, 4300000, 'ffffffff-ffff-ffff-ffff-fffffffff008', '44444444-4444-4444-4444-444444444009'),
                                                                                           ('12121212-1212-1212-1212-121212121009', 10, 8800000, 'ffffffff-ffff-ffff-ffff-fffffffff009', '44444444-4444-4444-4444-444444444010'),
                                                                                           ('12121212-1212-1212-1212-121212121010', 30, 1900000, 'ffffffff-ffff-ffff-ffff-fffffffff010', '44444444-4444-4444-4444-444444444011'),
                                                                                           ('12121212-1212-1212-1212-121212121011',  5, 4200000, 'ffffffff-ffff-ffff-ffff-fffffffff003', '44444444-4444-4444-4444-444444444001');

-- Đồng bộ import_details cho 20 product mới (012-031)
INSERT INTO import_details (id, quantity, import_price, import_receipt_id, product_id) VALUES
                                                                                           ('12121212-1212-1212-1212-121212121012',  6, 228000000, 'ffffffff-ffff-ffff-ffff-fffffffff003', '44444444-4444-4444-4444-444444444012'),
                                                                                           ('12121212-1212-1212-1212-121212121013',  7, 198000000, 'ffffffff-ffff-ffff-ffff-fffffffff004', '44444444-4444-4444-4444-444444444013'),
                                                                                           ('12121212-1212-1212-1212-121212121014',  5, 266000000, 'ffffffff-ffff-ffff-ffff-fffffffff005', '44444444-4444-4444-4444-444444444014'),
                                                                                           ('12121212-1212-1212-1212-121212121015',  8, 158000000, 'ffffffff-ffff-ffff-ffff-fffffffff006', '44444444-4444-4444-4444-444444444015'),
                                                                                           ('12121212-1212-1212-1212-121212121016',  9, 102000000, 'ffffffff-ffff-ffff-ffff-fffffffff007', '44444444-4444-4444-4444-444444444016'),
                                                                                           ('12121212-1212-1212-1212-121212121017',  7, 175000000, 'ffffffff-ffff-ffff-ffff-fffffffff008', '44444444-4444-4444-4444-444444444017'),
                                                                                           ('12121212-1212-1212-1212-121212121018',  7, 137000000, 'ffffffff-ffff-ffff-ffff-fffffffff009', '44444444-4444-4444-4444-444444444018'),
                                                                                           ('12121212-1212-1212-1212-121212121019',  6, 189000000, 'ffffffff-ffff-ffff-ffff-fffffffff010', '44444444-4444-4444-4444-444444444019'),
                                                                                           ('12121212-1212-1212-1212-121212121020',  8, 129000000, 'ffffffff-ffff-ffff-ffff-fffffffff003', '44444444-4444-4444-4444-444444444020'),
                                                                                           ('12121212-1212-1212-1212-121212121021',  7, 151000000, 'ffffffff-ffff-ffff-ffff-fffffffff004', '44444444-4444-4444-4444-444444444021'),
                                                                                           ('12121212-1212-1212-1212-121212121022',  8, 113000000, 'ffffffff-ffff-ffff-ffff-fffffffff005', '44444444-4444-4444-4444-444444444022'),
                                                                                           ('12121212-1212-1212-1212-121212121023', 10,  52000000, 'ffffffff-ffff-ffff-ffff-fffffffff006', '44444444-4444-4444-4444-444444444023'),
                                                                                           ('12121212-1212-1212-1212-121212121024', 11,  41000000, 'ffffffff-ffff-ffff-ffff-fffffffff007', '44444444-4444-4444-4444-444444444024'),
                                                                                           ('12121212-1212-1212-1212-121212121025',  9,  36000000, 'ffffffff-ffff-ffff-ffff-fffffffff008', '44444444-4444-4444-4444-444444444025'),
                                                                                           ('12121212-1212-1212-1212-121212121026',  9,  62000000, 'ffffffff-ffff-ffff-ffff-fffffffff009', '44444444-4444-4444-4444-444444444026'),
                                                                                           ('12121212-1212-1212-1212-121212121027', 10,  58000000, 'ffffffff-ffff-ffff-ffff-fffffffff010', '44444444-4444-4444-4444-444444444027'),
                                                                                           ('12121212-1212-1212-1212-121212121028',  7,  76000000, 'ffffffff-ffff-ffff-ffff-fffffffff003', '44444444-4444-4444-4444-444444444028'),
                                                                                           ('12121212-1212-1212-1212-121212121029', 11,  51000000, 'ffffffff-ffff-ffff-ffff-fffffffff004', '44444444-4444-4444-4444-444444444029'),
                                                                                           ('12121212-1212-1212-1212-121212121030',  8,  55000000, 'ffffffff-ffff-ffff-ffff-fffffffff005', '44444444-4444-4444-4444-444444444030'),
                                                                                           ('12121212-1212-1212-1212-121212121031', 10,  44000000, 'ffffffff-ffff-ffff-ffff-fffffffff006', '44444444-4444-4444-4444-444444444031');

-- =========================================================
-- 18) REVIEWS (bổ sung thêm review cho product mới)
-- =========================================================
INSERT INTO reviews (id, rating, comment, created_at, customer_id, product_id) VALUES
                                                                                   ('13131313-1313-1313-1313-131313131003', 5, 'Hàng chính hãng, đẹp xuất sắc',     '2026-03-15 10:00:00', '11111111-1111-1111-1111-111111111103', '44444444-4444-4444-4444-444444444004'),
                                                                                   ('13131313-1313-1313-1313-131313131004', 4, 'Pin tiết kiệm, đeo thoải mái',      '2026-03-16 11:00:00', '11111111-1111-1111-1111-111111111104', '44444444-4444-4444-4444-444444444005'),
                                                                                   ('13131313-1313-1313-1313-131313131005', 4, 'Nhiều tính năng hay, pin hơi yếu',  '2026-03-17 09:00:00', '11111111-1111-1111-1111-111111111105', '44444444-4444-4444-4444-444444444006'),
                                                                                   ('13131313-1313-1313-1313-131313131006', 5, 'Bền, chống va tốt, đúng ý mô tả',   '2026-03-18 10:00:00', '11111111-1111-1111-1111-111111111106', '44444444-4444-4444-4444-444444444007'),
                                                                                   ('13131313-1313-1313-1313-131313131007', 5, 'Đồng hồ đẹp lắm, sang trọng',       '2026-03-19 11:00:00', '11111111-1111-1111-1111-111111111107', '44444444-4444-4444-4444-444444444008'),
                                                                                   ('13131313-1313-1313-1313-131313131008', 3, 'Tạm ổn, có vài lỗi nhỏ',            '2026-03-20 10:00:00', '11111111-1111-1111-1111-111111111108', '44444444-4444-4444-4444-444444444009'),
                                                                                   ('13131313-1313-1313-1313-131313131009', 5, 'Swiss made, giá hợp lý',            '2026-03-21 10:00:00', '11111111-1111-1111-1111-111111111101', '44444444-4444-4444-4444-444444444010'),
                                                                                   ('13131313-1313-1313-1313-131313131010', 4, 'Màu hồng cute, thích hợp đi học',   '2026-03-22 10:00:00', '11111111-1111-1111-1111-111111111102', '44444444-4444-4444-4444-444444444011'),
                                                                                   ('13131313-1313-1313-1313-131313131011', 2, 'Giao chậm hơn dự kiến',             '2026-03-23 10:00:00', '11111111-1111-1111-1111-111111111103', '44444444-4444-4444-4444-444444444003'),
                                                                                   ('13131313-1313-1313-1313-131313131012', 5, 'Rolex Submariner hoàn thiện rất tốt, đeo chắc tay.', '2026-04-02 13:00:00', '11111111-1111-1111-1111-111111111101', '44444444-4444-4444-4444-444444444012'),
                                                                                   ('13131313-1313-1313-1313-131313131013', 5, 'Datejust đeo rất sang, mặt xanh đẹp hơn ảnh.',       '2026-04-02 13:05:00', '11111111-1111-1111-1111-111111111102', '44444444-4444-4444-4444-444444444013'),
                                                                                   ('13131313-1313-1313-1313-131313131014', 4, 'GMT-Master II chạy ổn định, khóa dây chắc chắn.',    '2026-04-02 13:10:00', '11111111-1111-1111-1111-111111111103', '44444444-4444-4444-4444-444444444014'),
                                                                                   ('13131313-1313-1313-1313-131313131015', 5, 'Oyster Perpetual nhỏ gọn, rất hợp cổ tay nữ.',       '2026-04-02 13:15:00', '11111111-1111-1111-1111-111111111104', '44444444-4444-4444-4444-444444444015'),
                                                                                   ('13131313-1313-1313-1313-131313131016', 4, 'Tank Must đúng kiểu cổ điển, mặt số rõ nét.',        '2026-04-02 13:20:00', '11111111-1111-1111-1111-111111111105', '44444444-4444-4444-4444-444444444016'),
                                                                                   ('13131313-1313-1313-1313-131313131017', 5, 'Santos lên tay rất đẹp, đeo đi làm cực hợp.',        '2026-04-02 13:25:00', '11111111-1111-1111-1111-111111111106', '44444444-4444-4444-4444-444444444017'),
                                                                                   ('13131313-1313-1313-1313-131313131018', 5, 'Ballon Bleu tinh tế, đóng gói rất cẩn thận.',         '2026-04-02 13:30:00', '11111111-1111-1111-1111-111111111107', '44444444-4444-4444-4444-444444444018'),
                                                                                   ('13131313-1313-1313-1313-131313131019', 4, 'Pasha thiết kế lạ mắt, dây da mềm và ôm tay.',       '2026-04-02 13:35:00', '11111111-1111-1111-1111-111111111108', '44444444-4444-4444-4444-444444444019'),
                                                                                   ('13131313-1313-1313-1313-131313131020', 5, 'Seamaster đúng chuẩn diver cao cấp, rất hài lòng.',  '2026-04-02 13:40:00', '11111111-1111-1111-1111-111111111109', '44444444-4444-4444-4444-444444444020'),
                                                                                   ('13131313-1313-1313-1313-131313131021', 5, 'Moonwatch lên tay cực kỳ nam tính, hoàn thiện tốt.', '2026-04-02 13:45:00', '11111111-1111-1111-1111-111111111110', '44444444-4444-4444-4444-444444444021'),
                                                                                   ('13131313-1313-1313-1313-131313131022', 4, 'Constellation nhỏ gọn, mặt trắng rất thanh lịch.',   '2026-04-02 13:50:00', '11111111-1111-1111-1111-111111111101', '44444444-4444-4444-4444-444444444022'),
                                                                                   ('13131313-1313-1313-1313-131313131023', 5, 'Master Collection chạy êm, kính sapphire trong.',    '2026-04-02 13:55:00', '11111111-1111-1111-1111-111111111102', '44444444-4444-4444-4444-444444444023'),
                                                                                   ('13131313-1313-1313-1313-131313131024', 4, 'HydroConquest thể thao nhưng vẫn sang trọng.',       '2026-04-02 14:00:00', '11111111-1111-1111-1111-111111111103', '44444444-4444-4444-4444-444444444024'),
                                                                                   ('13131313-1313-1313-1313-131313131025', 5, 'DolceVita rất đẹp, phù hợp làm quà tặng.',           '2026-04-02 14:05:00', '11111111-1111-1111-1111-111111111104', '44444444-4444-4444-4444-444444444025'),
                                                                                   ('13131313-1313-1313-1313-131313131026', 4, 'Carrera thiết kế đẹp, kim và cọc số hoàn thiện tốt.', '2026-04-02 14:10:00', '11111111-1111-1111-1111-111111111105', '44444444-4444-4444-4444-444444444026'),
                                                                                   ('13131313-1313-1313-1313-131313131027', 5, 'Aquaracer đeo chắc tay, chống nước đúng kỳ vọng.',   '2026-04-02 14:15:00', '11111111-1111-1111-1111-111111111106', '44444444-4444-4444-4444-444444444027'),
                                                                                   ('13131313-1313-1313-1313-131313131028', 5, 'Monaco form vuông nổi bật, rất khác biệt.',          '2026-04-02 14:20:00', '11111111-1111-1111-1111-111111111107', '44444444-4444-4444-4444-444444444028'),
                                                                                   ('13131313-1313-1313-1313-131313131029', 4, 'Captain Cook đẹp, dây thép đeo thoải mái.',         '2026-04-02 14:25:00', '11111111-1111-1111-1111-111111111108', '44444444-4444-4444-4444-444444444029'),
                                                                                   ('13131313-1313-1313-1313-131313131030', 4, 'True Square nhìn hiện đại, chất liệu hoàn thiện tốt.', '2026-04-02 14:30:00', '11111111-1111-1111-1111-111111111109', '44444444-4444-4444-4444-444444444030'),
                                                                                   ('13131313-1313-1313-1313-131313131031', 5, 'Centrix Automatic sang và đeo nhẹ tay.',             '2026-04-02 14:35:00', '11111111-1111-1111-1111-111111111110', '44444444-4444-4444-4444-444444444031');

-- =========================================================
-- 19) CHATS (gốc: 2 → thêm 8 → tổng 10)
-- =========================================================
INSERT INTO chats (id, start_date, end_date, is_ai_handled, customer_id) VALUES
                                                                             ('14141414-1414-1414-1414-141414141003', '2026-03-13 08:00:00', '2026-03-13 08:20:00', 0, '11111111-1111-1111-1111-111111111103'),
                                                                             ('14141414-1414-1414-1414-141414141004', '2026-03-14 09:00:00', '2026-03-14 09:30:00', 1, '11111111-1111-1111-1111-111111111104'),
                                                                             ('14141414-1414-1414-1414-141414141005', '2026-03-15 10:00:00', '2026-03-15 10:25:00', 1, '11111111-1111-1111-1111-111111111105'),
                                                                             ('14141414-1414-1414-1414-141414141006', '2026-03-16 08:30:00', '2026-03-16 09:00:00', 0, '11111111-1111-1111-1111-111111111106'),
                                                                             ('14141414-1414-1414-1414-141414141007', '2026-03-17 09:00:00', '2026-03-17 09:40:00', 1, '11111111-1111-1111-1111-111111111107'),
                                                                             ('14141414-1414-1414-1414-141414141008', '2026-03-18 10:00:00', '2026-03-18 10:30:00', 0, '11111111-1111-1111-1111-111111111108'),
                                                                             ('14141414-1414-1414-1414-141414141009', '2026-03-19 11:00:00', '2026-03-19 11:20:00', 1, '11111111-1111-1111-1111-111111111101'),
                                                                             ('14141414-1414-1414-1414-141414141010', '2026-03-20 08:00:00', '2026-03-20 08:30:00', 1, '11111111-1111-1111-1111-111111111102');

-- =========================================================
-- 20) REVIEW_REPORTS (gốc: 1 → thêm 9 → tổng 10)
-- =========================================================
INSERT INTO review_reports (id, start_date, end_date, total_sales, score_sales, created_at, owner_id) VALUES
                                                                                                          ('15151515-1515-1515-1515-151515151002', '2026-03-11 00:00:00', '2026-03-20 23:59:59', 230000000, 8.7, '2026-03-21 09:00:00', '11111111-1111-1111-1111-111111111302'),
                                                                                                          ('15151515-1515-1515-1515-151515151003', '2026-02-01 00:00:00', '2026-02-28 23:59:59',  98000000, 7.9, '2026-03-01 09:00:00', '11111111-1111-1111-1111-111111111302'),
                                                                                                          ('15151515-1515-1515-1515-151515151004', '2026-01-01 00:00:00', '2026-01-31 23:59:59',  85000000, 7.5, '2026-02-01 09:00:00', '11111111-1111-1111-1111-111111111302'),
                                                                                                          ('15151515-1515-1515-1515-151515151005', '2025-12-01 00:00:00', '2025-12-31 23:59:59', 145000000, 9.0, '2026-01-02 09:00:00', '11111111-1111-1111-1111-111111111302'),
                                                                                                          ('15151515-1515-1515-1515-151515151006', '2025-11-01 00:00:00', '2025-11-30 23:59:59',  90000000, 8.1, '2025-12-01 09:00:00', '11111111-1111-1111-1111-111111111302'),
                                                                                                          ('15151515-1515-1515-1515-151515151007', '2025-10-01 00:00:00', '2025-10-31 23:59:59',  78000000, 7.8, '2025-11-01 09:00:00', '11111111-1111-1111-1111-111111111302'),
                                                                                                          ('15151515-1515-1515-1515-151515151008', '2025-09-01 00:00:00', '2025-09-30 23:59:59',  65000000, 7.2, '2025-10-01 09:00:00', '11111111-1111-1111-1111-111111111302'),
                                                                                                          ('15151515-1515-1515-1515-151515151009', '2025-08-01 00:00:00', '2025-08-31 23:59:59',  72000000, 7.6, '2025-09-01 09:00:00', '11111111-1111-1111-1111-111111111302'),
                                                                                                          ('15151515-1515-1515-1515-151515151010', '2026-03-21 00:00:00', '2026-03-31 23:59:59',  55000000, 8.3, '2026-04-01 09:00:00', '11111111-1111-1111-1111-111111111302');

-- =========================================================
-- 21) NOTIFICATIONS (gốc: 2 → thêm 9 → tổng 11)
-- =========================================================
INSERT INTO notifications (id, title, content, direct_url, type, is_read, time_created, expiry, sender_id, receiver_id) VALUES
                                                                                                                            ('16161616-1616-1616-1616-161616161003', 'Đơn hàng đã giao',     'Đơn #003 giao thành công',        '/orders/99999999-9999-9999-9999-999999999003', 'ORDER', 1, '2026-03-12 16:01:00', '2026-04-12 16:01:00', '11111111-1111-1111-1111-111111111201', '11111111-1111-1111-1111-111111111103'),
                                                                                                                            ('16161616-1616-1616-1616-161616161004', 'Xác nhận đơn #004',    'Đơn hàng của bạn đã xác nhận',    '/orders/99999999-9999-9999-9999-999999999004', 'ORDER', 0, '2026-03-11 11:01:00', '2026-04-11 11:01:00', '11111111-1111-1111-1111-111111111203', '11111111-1111-1111-1111-111111111104'),
                                                                                                                            ('16161616-1616-1616-1616-161616161005', 'Giao hàng đang đến',   'Đơn #006 đang trên đường',        '/orders/99999999-9999-9999-9999-999999999006', 'ORDER', 0, '2026-03-14 09:01:00', '2026-04-14 09:01:00', '11111111-1111-1111-1111-111111111204', '11111111-1111-1111-1111-111111111106'),
                                                                                                                            ('16161616-1616-1616-1616-161616161006', 'Đơn hàng bị hủy',      'Đơn #010 đã bị hủy theo yêu cầu', '/orders/99999999-9999-9999-9999-999999999010', 'ORDER', 1, '2026-03-17 16:01:00', '2026-04-17 16:01:00', '11111111-1111-1111-1111-111111111201', '11111111-1111-1111-1111-111111111102'),
                                                                                                                            ('16161616-1616-1616-1616-161616161007', 'Bảo hành hoàn tất',    'Bảo hành #007 đã hoàn thành',     '/warranties', 'WARRANTY', 1, '2026-03-29 17:01:00', '2026-04-29 17:01:00', '11111111-1111-1111-1111-111111111203', '11111111-1111-1111-1111-111111111107'),
                                                                                                                            ('16161616-1616-1616-1616-161616161008', 'Voucher sắp hết hạn',  'Mã VC5OFF hết hạn trong 3 ngày',  '/vouchers', 'PROMOTION', 0, '2026-03-28 09:00:00', '2026-03-31 23:59:59', '11111111-1111-1111-1111-111111111204', '11111111-1111-1111-1111-111111111101'),
                                                                                                                            ('16161616-1616-1616-1616-161616161009', 'Khuyến mãi tháng 4',   'SUMMER30 áp dụng từ 01/06',       '/vouchers', 'PROMOTION', 0, '2026-03-25 09:00:00', '2026-06-30 23:59:59', '11111111-1111-1111-1111-111111111201', '11111111-1111-1111-1111-111111111105'),
                                                                                                                            ('16161616-1616-1616-1616-161616161010', 'Sản phẩm mới về',      'Tissot PRX đã có hàng',           '/products/44444444-4444-4444-4444-444444444010', 'NEWS', 0, '2026-03-08 10:00:00', '2026-04-08 10:00:00', '11111111-1111-1111-1111-111111111203', '11111111-1111-1111-1111-111111111108'),
                                                                                                                            ('16161616-1616-1616-1616-161616161011', 'Đánh giá sản phẩm',    'Hãy đánh giá đơn hàng gần đây',   '/reviews', 'SYSTEM', 0, '2026-03-22 09:00:00', '2026-04-22 09:00:00', '11111111-1111-1111-1111-111111111204', '11111111-1111-1111-1111-111111111107');

-- =========================================================
-- 22) MAILS (gốc: 2 → thêm 9 → tổng 11)
-- =========================================================
INSERT INTO mails (id, def_date, subject, body, recipient, is_sent, customer_id) VALUES
                                                                                     ('17171717-1717-1717-1717-171717171003', '2026-03-10 10:07:00', 'Xác nhận đơn hàng #003', 'Đơn hàng của bạn đã được tiếp nhận và xử lý.',       'chi.customer@example.com',   1, '11111111-1111-1111-1111-111111111103'),
                                                                                     ('17171717-1717-1717-1717-171717171004', '2026-03-11 11:07:00', 'Xác nhận đơn hàng #004', 'Đơn hàng của bạn đã được xác nhận.',                  'dat.customer@example.com',   1, '11111111-1111-1111-1111-111111111104'),
                                                                                     ('17171717-1717-1717-1717-171717171005', '2026-03-12 09:35:00', 'Chúng tôi đã nhận đơn',  'Đơn hàng #005 đang chờ xử lý.',                      'em.customer@example.com',    1, '11111111-1111-1111-1111-111111111105'),
                                                                                     ('17171717-1717-1717-1717-171717171006', '2026-03-13 14:05:00', 'Đơn hàng đang vận chuyển','Đơn #006 đang trên đường giao đến bạn.',              'phuc.customer@example.com',  1, '11111111-1111-1111-1111-111111111106'),
                                                                                     ('17171717-1717-1717-1717-171717171007', '2026-03-29 17:05:00', 'Bảo hành hoàn tất',      'Đồng hồ của bạn đã được xử lý và sẵn sàng trả.',     'giang.customer@example.com', 1, '11111111-1111-1111-1111-111111111107'),
                                                                                     ('17171717-1717-1717-1717-171717171008', '2026-03-15 10:35:00', 'Đơn hàng của bạn',       'Chúng tôi đã nhận đơn hàng #008.',                    'hung.customer@example.com',  1, '11111111-1111-1111-1111-111111111108'),
                                                                                     ('17171717-1717-1717-1717-171717171009', '2026-03-16 13:10:00', 'Giao hàng thành công',   'Đơn hàng #009 đã giao đến bạn thành công.',           'an.customer@example.com',    1, '11111111-1111-1111-1111-111111111101'),
                                                                                     ('17171717-1717-1717-1717-171717171010', '2026-03-17 16:05:00', 'Đơn hàng đã bị hủy',     'Đơn hàng #010 đã được hủy theo yêu cầu của bạn.',    'binh.customer@example.com',  1, '11111111-1111-1111-1111-111111111102'),
                                                                                     ('17171717-1717-1717-1717-171717171011', '2026-04-01 09:05:00', 'Khuyến mãi tháng 6',     'Dùng mã SUMMER30 giảm 30% cho mỗi đơn từ 01/06.',    'chi.customer@example.com',   0, '11111111-1111-1111-1111-111111111103');

-- =========================================================
-- 23) PRODUCT_CATEGORIES (bổ sung liên kết nhiều-nhiều)
-- =========================================================
INSERT INTO product_categories (product_id, category_id)
SELECT p.id, p.category_id
FROM products p
WHERE p.category_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM product_categories pc WHERE pc.product_id = p.id AND pc.category_id = p.category_id
);

-- Thêm liên kết cross-category
INSERT INTO product_categories (product_id, category_id) VALUES
                                                             ('44444444-4444-4444-4444-444444444004', '22222222-2222-2222-2222-222222222006'),  -- Orient → Nam
                                                             ('44444444-4444-4444-4444-444444444005', '22222222-2222-2222-2222-222222222006'),  -- Citizen → Nam
                                                             ('44444444-4444-4444-4444-444444444007', '22222222-2222-2222-2222-222222222004'),  -- G-Shock → Thể thao
                                                             ('44444444-4444-4444-4444-444444444007', '22222222-2222-2222-2222-222222222006'),  -- G-Shock → Nam
                                                             ('44444444-4444-4444-4444-444444444008', '22222222-2222-2222-2222-222222222006'),  -- Seiko Presage → Nam
                                                             ('44444444-4444-4444-4444-444444444008', '22222222-2222-2222-2222-222222222007'),  -- Seiko Presage → Luxury
                                                             ('44444444-4444-4444-4444-444444444010', '22222222-2222-2222-2222-222222222006'),  -- Tissot PRX → Nam
                                                             ('44444444-4444-4444-4444-444444444010', '22222222-2222-2222-2222-222222222007'),  -- Tissot PRX → Luxury
                                                             ('44444444-4444-4444-4444-444444444011', '22222222-2222-2222-2222-222222222005'),  -- Baby-G → Nữ
                                                             ('44444444-4444-4444-4444-444444444011', '22222222-2222-2222-2222-222222222004'),  -- Baby-G → Thể thao
                                                             ('44444444-4444-4444-4444-444444444003', '22222222-2222-2222-2222-222222222004'),  -- Apple Watch → Thể thao
                                                             ('44444444-4444-4444-4444-444444444006', '22222222-2222-2222-2222-222222222004'),  -- Galaxy Watch → Thể thao
                                                             ('44444444-4444-4444-4444-444444444012', '22222222-2222-2222-2222-222222222004'),  -- Submariner → Thể thao
                                                             ('44444444-4444-4444-4444-444444444013', '22222222-2222-2222-2222-222222222006'),  -- Datejust → Nam
                                                             ('44444444-4444-4444-4444-444444444014', '22222222-2222-2222-2222-222222222004'),  -- GMT-Master II → Thể thao
                                                             ('44444444-4444-4444-4444-444444444015', '22222222-2222-2222-2222-222222222007'),  -- Oyster Perpetual 31 → Luxury
                                                             ('44444444-4444-4444-4444-444444444016', '22222222-2222-2222-2222-222222222007'),  -- Tank Must → Luxury
                                                             ('44444444-4444-4444-4444-444444444017', '22222222-2222-2222-2222-222222222006'),  -- Santos → Nam
                                                             ('44444444-4444-4444-4444-444444444018', '22222222-2222-2222-2222-222222222007'),  -- Ballon Bleu → Luxury
                                                             ('44444444-4444-4444-4444-444444444019', '22222222-2222-2222-2222-222222222006'),  -- Pasha → Nam
                                                             ('44444444-4444-4444-4444-444444444020', '22222222-2222-2222-2222-222222222007'),  -- Seamaster → Luxury
                                                             ('44444444-4444-4444-4444-444444444021', '22222222-2222-2222-2222-222222222006'),  -- Speedmaster → Nam
                                                             ('44444444-4444-4444-4444-444444444022', '22222222-2222-2222-2222-222222222007'),  -- Constellation → Luxury
                                                             ('44444444-4444-4444-4444-444444444023', '22222222-2222-2222-2222-222222222006'),  -- Master Collection → Nam
                                                             ('44444444-4444-4444-4444-444444444024', '22222222-2222-2222-2222-222222222007'),  -- HydroConquest → Luxury
                                                             ('44444444-4444-4444-4444-444444444025', '22222222-2222-2222-2222-222222222005'),  -- DolceVita → Nữ
                                                             ('44444444-4444-4444-4444-444444444026', '22222222-2222-2222-2222-222222222006'),  -- Carrera → Nam
                                                             ('44444444-4444-4444-4444-444444444027', '22222222-2222-2222-2222-222222222007'),  -- Aquaracer → Luxury
                                                             ('44444444-4444-4444-4444-444444444028', '22222222-2222-2222-2222-222222222007'),  -- Monaco → Luxury
                                                             ('44444444-4444-4444-4444-444444444029', '22222222-2222-2222-2222-222222222007'),  -- Captain Cook → Luxury
                                                             ('44444444-4444-4444-4444-444444444030', '22222222-2222-2222-2222-222222222006'),  -- True Square → Nam
                                                             ('44444444-4444-4444-4444-444444444031', '22222222-2222-2222-2222-222222222007')   -- Centrix Automatic → Luxury
    ON DUPLICATE KEY UPDATE product_id = VALUES(product_id);

COMMIT;
SET FOREIGN_KEY_CHECKS = 1;


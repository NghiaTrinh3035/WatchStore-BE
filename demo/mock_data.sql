-- Mock Data for WatchStore (Excluding Orders/Cart)
-- Password for all users is: '123456'

-- 1. Users & Accounts
INSERT INTO users (id, username, password, full_name, email, phone, address, gender, role, is_active, created_at)
VALUES 
('11111111-1111-1111-1111-111111111111', 'admin', '$2a$10$wE/.76M.E4h4T6zL0PZ/.ebl0f1g.jKj7/g9N5b/zL8D5/T/yJ', 'Admin Owner', 'admin@example.com', '0123456789', 'HCM City', 'MALE', 'OWNER', 1, NOW()),
('22222222-2222-2222-2222-222222222222', 'customer1', '$2a$10$wE/.76M.E4h4T6zL0PZ/.ebl0f1g.jKj7/g9N5b/zL8D5/T/yJ', 'Test Customer', 'customer@example.com', '0987654321', 'HCM City', 'MALE', 'CUSTOMER', 1, NOW());

INSERT INTO owners (id) VALUES ('11111111-1111-1111-1111-111111111111');
INSERT INTO customers (id) VALUES ('22222222-2222-2222-2222-222222222222');

-- 2. Categories
INSERT INTO categories (id, name, description)
VALUES 
('33333333-3333-3333-3333-333333333333', 'Men Watches', 'Watches for men'),
('33333333-3333-3333-3333-333333333334', 'Luxury Watches', 'High end watches');

-- 3. Products
INSERT INTO products (id, brand, name, description, price, stock_quantity, movement_type, glass_material, water_resistance, face_size, wire_material, wire_color, case_color, face_color, status, updated_at, category_id)
VALUES 
('44444444-4444-4444-4444-444444444444', 'Rolex', 'Submariner Date', 'Iconic diver watch', 200000000, 10, 'Automatic', 'Sapphire', '300m', '41mm', 'Steel', 'Silver', 'Silver', 'Black', 'ACTIVE', NOW(), '33333333-3333-3333-3333-333333333334'),
('44444444-4444-4444-4444-444444444445', 'Casio', 'G-Shock Classic', 'Tough digital watch', 3000000, 50, 'Quartz', 'Mineral', '200m', '45mm', 'Resin', 'Black', 'Black', 'Black', 'ACTIVE', NOW(), '33333333-3333-3333-3333-333333333333');

-- 4. Product Categories Join Table
INSERT INTO product_categories (product_id, category_id)
VALUES 
('44444444-4444-4444-4444-444444444444', '33333333-3333-3333-3333-333333333334'),
('44444444-4444-4444-4444-444444444445', '33333333-3333-3333-3333-333333333333');

-- 5. Product Images
INSERT INTO product_images (id, image_url, public_id, alt_text, is_thumbnail, product_id)
VALUES 
('55555555-5555-5555-5555-555555555555', 'https://example.com/rolex.jpg', 'rolex_1', 'Rolex Submariner', 1, '44444444-4444-4444-4444-444444444444');

-- 6. Vouchers
INSERT INTO vouchers (id, code, discount_percent, usage_count, valid_from, valid_to, created_at, quantity, status)
VALUES 
('66666666-6666-6666-6666-666666666666', 'SALE10', 10, 0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), NOW(), 100, 'ACTIVE');

-- 7. Suppliers
INSERT INTO suppliers (id, name, contract_info, address)
VALUES 
('77777777-7777-7777-7777-777777777777', 'Watch Distributor Inc', 'contract_2026', '123 Dist Street');

-- 8. Import Receipts
INSERT INTO import_receipts (id, import_date, note, supplier_id, owner_id)
VALUES 
('88888888-8888-8888-8888-888888888888', NOW(), 'Initial stock', '77777777-7777-7777-7777-777777777777', '11111111-1111-1111-1111-111111111111');

-- 9. Import Details
INSERT INTO import_details (id, quantity, import_price, import_receipt_id, product_id)
VALUES 
('99999999-9999-9999-9999-999999999999', 10, 150000000, '88888888-8888-8888-8888-888888888888', '44444444-4444-4444-4444-444444444444'),
('99999999-9999-9999-9999-999999999998', 50, 2000000, '88888888-8888-8888-8888-888888888888', '44444444-4444-4444-4444-444444444445');

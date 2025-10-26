/* ========= CATEGORIES ========= */
IF NOT EXISTS (SELECT 1 FROM categories WHERE slug = N'banh-kem')
INSERT INTO categories (name, slug, parent_id, is_active, sort_order)
VALUES (N'Bánh Kem', N'banh-kem', NULL, 1, 1);

IF NOT EXISTS (SELECT 1 FROM categories WHERE slug = N'banh-mi')
INSERT INTO categories (name, slug, parent_id, is_active, sort_order)
VALUES (N'Bánh Mì', N'banh-mi', NULL, 1, 2);

IF NOT EXISTS (SELECT 1 FROM categories WHERE slug = N'banh-quy')
INSERT INTO categories (name, slug, parent_id, is_active, sort_order)
VALUES (N'Bánh Quy', N'banh-quy', NULL, 1, 3);

/* ========= PRODUCTS ========= */
IF NOT EXISTS (SELECT 1 FROM products WHERE sku = N'CK-001')
INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
VALUES (N'CK-001', N'Bánh kem dâu tây', N'banh-kem-dau-tay', N'Bánh kem dâu, mềm mịn',
        N'<p>Bánh kem dâu tây tươi, cốt bông lan mềm, kem béo nhẹ.</p>',
        (SELECT TOP(1) id FROM categories WHERE slug = N'banh-kem'), 180000, 220000, 1);

IF NOT EXISTS (SELECT 1 FROM products WHERE sku = N'CK-002')
INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
VALUES (N'CK-002', N'Bánh kem socola', N'banh-kem-socola', N'Đậm vị cacao',
        N'<p>Sô cô la Bỉ, ít ngọt, phù hợp tiệc sinh nhật.</p>',
        (SELECT TOP(1) id FROM categories WHERE slug = N'banh-kem'), 200000, 250000, 1);

IF NOT EXISTS (SELECT 1 FROM products WHERE sku = N'CK-003')
INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
VALUES (N'CK-003', N'Bánh kem matcha', N'banh-kem-matcha', N'Hương trà xanh',
        N'<p>Matcha Nhật Bản, mùi thơm dịu, ít ngọt.</p>',
        (SELECT TOP(1) id FROM categories WHERE slug = N'banh-kem'), 195000, NULL, 1);

IF NOT EXISTS (SELECT 1 FROM products WHERE sku = N'CK-004')
INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
VALUES (N'CK-004', N'Bánh kem tiramisu', N'banh-kem-tiramisu', N'Cà phê Ý',
        N'<p>Tiramisu mềm mịn, vị cà phê nhẹ.</p>',
        (SELECT TOP(1) id FROM categories WHERE slug = N'banh-kem'), 210000, 260000, 1);

IF NOT EXISTS (SELECT 1 FROM products WHERE sku = N'CK-005')
INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
VALUES (N'CK-005', N'Bánh kem phô mai', N'banh-kem-pho-mai', N'New York cheesecake',
        N'<p>Béo vừa, chua nhẹ, thơm phô mai.</p>',
        (SELECT TOP(1) id FROM categories WHERE slug = N'banh-kem'), 230000, NULL, 1);

IF NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BM-001')
INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
VALUES (N'BM-001', N'Bánh mì bơ tỏi', N'banh-mi-bo-toi', N'Giòn thơm',
        N'<p>Bơ tỏi nướng vàng, thơm lừng.</p>',
        (SELECT TOP(1) id FROM categories WHERE slug = N'banh-mi'), 25000, NULL, 1);

IF NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BM-002')
INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
VALUES (N'BM-002', N'Bánh mì phô mai', N'banh-mi-pho-mai', N'Béo ngậy',
        N'<p>Phô mai mozzarella, vỏ mềm.</p>',
        (SELECT TOP(1) id FROM categories WHERE slug = N'banh-mi'), 30000, 35000, 1);

IF NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BM-003')
INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
VALUES (N'BM-003', N'Bánh mì xúc xích', N'banh-mi-xuc-xich', N'No lâu',
        N'<p>Xúc xích Đức, sốt đặc trưng.</p>',
        (SELECT TOP(1) id FROM categories WHERE slug = N'banh-mi'), 32000, NULL, 1);

IF NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BQ-001')
INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
VALUES (N'BQ-001', N'Cookie bơ', N'cookie-bo', N'Giòn rụm',
        N'<p>Cookie bơ thơm, vị vanilla.</p>',
        (SELECT TOP(1) id FROM categories WHERE slug = N'banh-quy'), 45000, NULL, 1);

IF NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BQ-002')
INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
VALUES (N'BQ-002', N'Cookie hạnh nhân', N'cookie-hanh-nhan', N'Bùi béo',
        N'<p>Hạnh nhân rang, giòn.</p>',
        (SELECT TOP(1) id FROM categories WHERE slug = N'banh-quy'), 55000, 65000, 1);

IF NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BQ-003')
INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
VALUES (N'BQ-003', N'Cookie chocolate chip', N'cookie-chip', N'Nhiều chip',
        N'<p>Chocolate chip đậm đà.</p>',
        (SELECT TOP(1) id FROM categories WHERE slug = N'banh-quy'), 52000, NULL, 1);

IF NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BQ-004')
INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
VALUES (N'BQ-004', N'Cookie trà xanh', N'cookie-matcha', N'Thơm trà',
        N'<p>Vị matcha nhẹ, ít ngọt.</p>',
        (SELECT TOP(1) id FROM categories WHERE slug = N'banh-quy'), 50000, NULL, 1);

/* ========= PRODUCT IMAGES ========= */
INSERT INTO product_images (product_id, url, alt, sort_order)
SELECT p.id,
       CONCAT('https://picsum.photos/seed/', p.sku, '/800/600'),
       p.name,
       0
FROM products p
WHERE NOT EXISTS (SELECT 1 FROM product_images i WHERE i.product_id = p.id);

/* ========= PRODUCT STATS ========= */
/* đảm bảo có cột favorite_count (single line, không BEGIN/END) */
IF COL_LENGTH('product_stats','favorite_count') IS NULL
  ALTER TABLE product_stats
    ADD favorite_count INT NOT NULL
        CONSTRAINT DF_product_stats_favorite_count DEFAULT(0) WITH VALUES;

/* seed stats nếu chưa có */
INSERT INTO product_stats (product_id, sold_count, view_count, rating_avg, rating_count, favorite_count)
SELECT p.id,
       CASE WHEN p.sku LIKE 'CK-%' THEN 50
            WHEN p.sku LIKE 'BM-%' THEN 25
            ELSE 15 END,
       100, 4.50, 10, 5
FROM products p
WHERE NOT EXISTS (SELECT 1 FROM product_stats s WHERE s.product_id = p.id);

/* ========= BANNERS ========= */
IF NOT EXISTS (SELECT 1 FROM banners WHERE title = N'Ưu đãi bánh kem')
INSERT INTO banners (title, image_url, link_to, sort_order, is_active)
VALUES (N'Ưu đãi bánh kem', 'https://picsum.photos/seed/banner1/1200/350', '/category/banh-kem', 1, 1);

IF NOT EXISTS (SELECT 1 FROM banners WHERE title = N'Cookie giảm giá')
INSERT INTO banners (title, image_url, link_to, sort_order, is_active)
VALUES (N'Cookie giảm giá', 'https://picsum.photos/seed/banner2/1200/350', '/category/banh-quy', 2, 1);

/* ========= COUPONS ========= */
IF NOT EXISTS (SELECT 1 FROM coupons WHERE code = 'SALE10')
INSERT INTO coupons (code, type, value, min_subtotal, max_discount, quantity, starts_at, ends_at, branch_id, is_active)
VALUES ('SALE10', N'PERCENT', 10, 100000, 50000, 9999, SYSDATETIME(), DATEADD(day, 30, SYSDATETIME()), NULL, 1);

IF NOT EXISTS (SELECT 1 FROM coupons WHERE code = 'OFF20K')
INSERT INTO coupons (code, type, value, min_subtotal, max_discount, quantity, starts_at, ends_at, branch_id, is_active)
VALUES ('OFF20K', N'AMOUNT', 20000, 100000, NULL, 9999, SYSDATETIME(), DATEADD(day, 30, SYSDATETIME()), NULL, 1);

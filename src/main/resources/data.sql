/* =========================================================
   SAFE SEED (no BEGIN/END blocks, mỗi IF chỉ 1 câu lệnh)
   ========================================================= */

/* =============== CATEGORIES =============== */
IF OBJECT_ID(N'dbo.categories','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = N'banh-kem')
    INSERT INTO categories (name, slug, parent_id, is_active, sort_order)
    VALUES (N'Bánh Kem', N'banh-kem', NULL, 1, 1);

IF OBJECT_ID(N'dbo.categories','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = N'banh-mi')
    INSERT INTO categories (name, slug, parent_id, is_active, sort_order)
    VALUES (N'Bánh Mì', N'banh-mi', NULL, 1, 2);

IF OBJECT_ID(N'dbo.categories','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM categories WHERE slug = N'banh-quy')
    INSERT INTO categories (name, slug, parent_id, is_active, sort_order)
    VALUES (N'Bánh Quy', N'banh-quy', NULL, 1, 3);

/* =============== PRODUCTS =============== */
IF OBJECT_ID(N'dbo.products','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE sku = N'CK-001')
    INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
    VALUES (N'CK-001', N'Bánh kem dâu tây', N'banh-kem-dau-tay', N'Bánh kem dâu, mềm mịn',
            N'<p>Bánh kem dâu tây tươi, cốt bông lan mềm, kem béo nhẹ.</p>',
            (SELECT TOP(1) id FROM categories WHERE slug = N'banh-kem'), 180000, 220000, 1);

IF OBJECT_ID(N'dbo.products','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE sku = N'CK-002')
    INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
    VALUES (N'CK-002', N'Bánh kem socola', N'banh-kem-socola', N'Đậm vị cacao',
            N'<p>Sô cô la Bỉ, ít ngọt, phù hợp tiệc sinh nhật.</p>',
            (SELECT TOP(1) id FROM categories WHERE slug = N'banh-kem'), 200000, 250000, 1);

IF OBJECT_ID(N'dbo.products','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE sku = N'CK-003')
    INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
    VALUES (N'CK-003', N'Bánh kem matcha', N'banh-kem-matcha', N'Hương trà xanh',
            N'<p>Matcha Nhật Bản, mùi thơm dịu, ít ngọt.</p>',
            (SELECT TOP(1) id FROM categories WHERE slug = N'banh-kem'), 195000, NULL, 1);

IF OBJECT_ID(N'dbo.products','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE sku = N'CK-004')
    INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
    VALUES (N'CK-004', N'Bánh kem tiramisu', N'banh-kem-tiramisu', N'Cà phê Ý',
            N'<p>Tiramisu mềm mịn, vị cà phê nhẹ.</p>',
            (SELECT TOP(1) id FROM categories WHERE slug = N'banh-kem'), 210000, 260000, 1);

IF OBJECT_ID(N'dbo.products','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE sku = N'CK-005')
    INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
    VALUES (N'CK-005', N'Bánh kem phô mai', N'banh-kem-pho-mai', N'New York cheesecake',
            N'<p>Béo vừa, chua nhẹ, thơm phô mai.</p>',
            (SELECT TOP(1) id FROM categories WHERE slug = N'banh-kem'), 230000, NULL, 1);

IF OBJECT_ID(N'dbo.products','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BM-001')
    INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
    VALUES (N'BM-001', N'Bánh mì bơ tỏi', N'banh-mi-bo-toi', N'Giòn thơm',
            N'<p>Bơ tỏi nướng vàng, thơm lừng.</p>',
            (SELECT TOP(1) id FROM categories WHERE slug = N'banh-mi'), 25000, NULL, 1);

IF OBJECT_ID(N'dbo.products','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BM-002')
    INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
    VALUES (N'BM-002', N'Bánh mì phô mai', N'banh-mi-pho-mai', N'Béo ngậy',
            N'<p>Phô mai mozzarella, vỏ mềm.</p>',
            (SELECT TOP(1) id FROM categories WHERE slug = N'banh-mi'), 30000, 35000, 1);

IF OBJECT_ID(N'dbo.products','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BM-003')
    INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
    VALUES (N'BM-003', N'Bánh mì xúc xích', N'banh-mi-xuc-xich', N'No lâu',
            N'<p>Xúc xích Đức, sốt đặc trưng.</p>',
            (SELECT TOP(1) id FROM categories WHERE slug = N'banh-mi'), 32000, NULL, 1);

IF OBJECT_ID(N'dbo.products','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BQ-001')
    INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
    VALUES (N'BQ-001', N'Cookie bơ', N'cookie-bo', N'Giòn rụm',
            N'<p>Cookie bơ thơm, vị vanilla.</p>',
            (SELECT TOP(1) id FROM categories WHERE slug = N'banh-quy'), 45000, NULL, 1);

IF OBJECT_ID(N'dbo.products','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BQ-002')
    INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
    VALUES (N'BQ-002', N'Cookie hạnh nhân', N'cookie-hanh-nhan', N'Bùi béo',
            N'<p>Hạnh nhân rang, giòn.</p>',
            (SELECT TOP(1) id FROM categories WHERE slug = N'banh-quy'), 55000, 65000, 1);

IF OBJECT_ID(N'dbo.products','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BQ-003')
    INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
    VALUES (N'BQ-003', N'Cookie chocolate chip', N'cookie-chip', N'Nhiều chip',
            N'<p>Chocolate chip đậm đà.</p>',
            (SELECT TOP(1) id FROM categories WHERE slug = N'banh-quy'), 52000, NULL, 1);

IF OBJECT_ID(N'dbo.products','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM products WHERE sku = N'BQ-004')
    INSERT INTO products (sku, name, slug, short_desc, description, category_id, price, compare_at_price, status)
    VALUES (N'BQ-004', N'Cookie trà xanh', N'cookie-matcha', N'Thơm trà',
            N'<p>Vị matcha nhẹ, ít ngọt.</p>',
            (SELECT TOP(1) id FROM categories WHERE slug = N'banh-quy'), 50000, NULL, 1);

/* =============== PRODUCT IMAGES =============== */
/* product_images đã tồn tại theo log Hibernate */
IF OBJECT_ID(N'dbo.product_images','U') IS NOT NULL
    INSERT INTO product_images (product_id, url, alt, sort_order)
    SELECT p.id,
           CONCAT('https://picsum.photos/seed/', p.sku, '/800/600'),
           p.name,
           0
    FROM products p
    WHERE NOT EXISTS (SELECT 1 FROM product_images i WHERE i.product_id = p.id);

/* =============== PRODUCT STATS (chỉ các cột có sẵn) =============== */
IF OBJECT_ID(N'dbo.product_stats','U') IS NOT NULL
    INSERT INTO product_stats (product_id, sold_count, view_count, rating_avg, rating_count)
    SELECT p.id,
           CASE WHEN p.sku LIKE 'CK-%' THEN 50
                WHEN p.sku LIKE 'BM-%' THEN 25
                ELSE 15 END,
           100, 4.50, 10
    FROM products p
    WHERE NOT EXISTS (SELECT 1 FROM product_stats s WHERE s.product_id = p.id);

/* =============== BANNERS =============== */
IF OBJECT_ID(N'dbo.banners','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM banners WHERE title = N'Ưu đãi bánh kem')
    INSERT INTO banners (title, image_url, link_to, sort_order, is_active)
    VALUES (N'Ưu đãi bánh kem', 'https://picsum.photos/seed/banner1/1200/350', '/category/banh-kem', 1, 1);

IF OBJECT_ID(N'dbo.banners','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM banners WHERE title = N'Cookie giảm giá')
    INSERT INTO banners (title, image_url, link_to, sort_order, is_active)
    VALUES (N'Cookie giảm giá', 'https://picsum.photos/seed/banner2/1200/350', '/category/banh-quy', 2, 1);

/* =============== BRANCHES =============== */
IF OBJECT_ID(N'dbo.branches','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM branches WHERE code = N'HN-1')
    INSERT INTO branches (name, code, city, is_active)
    VALUES (N'CakeStore Hà Nội - Cầu Giấy', N'HN-1', N'Hà Nội', 1);

IF OBJECT_ID(N'dbo.branches','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM branches WHERE code = N'HC-1')
    INSERT INTO branches (name, code, city, is_active)
    VALUES (N'CakeStore HCM - Quận 1', N'HC-1', N'Hồ Chí Minh', 1);

IF OBJECT_ID(N'dbo.branches','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM branches WHERE code = N'HN-2')
    INSERT INTO branches (name, code, city, is_active)
    VALUES (N'CakeStore Hà Nội - Ba Đình', N'HN-2', N'Hà Nội', 1);

IF OBJECT_ID(N'dbo.branches','U') IS NOT NULL AND NOT EXISTS (SELECT 1 FROM branches WHERE code = N'DN-1')
    INSERT INTO branches (name, code, city, is_active)
    VALUES (N'CakeStore Đà Nẵng - Hải Châu', N'DN-1', N'Đà Nẵng', 1);

/* =============== BRANCH INVENTORY (chèn thêm reserved = 0) =============== */
IF OBJECT_ID(N'dbo.branch_inventory','U') IS NOT NULL
INSERT INTO branch_inventory (branch_id, product_id, quantity, reserved)
SELECT b.id, p.id, 50, 0
FROM branches b
JOIN products p ON p.sku IN (N'CK-001', N'CK-002', N'CK-003', N'BM-001')
WHERE b.code = N'HN-1'
  AND NOT EXISTS (
      SELECT 1 FROM branch_inventory bi
      WHERE bi.branch_id = b.id AND bi.product_id = p.id
  );

IF OBJECT_ID(N'dbo.branch_inventory','U') IS NOT NULL
INSERT INTO branch_inventory (branch_id, product_id, quantity, reserved)
SELECT b.id, p.id, 30, 0
FROM branches b
JOIN products p ON p.sku IN (N'CK-004', N'CK-005', N'BM-002', N'BQ-001')
WHERE b.code = N'HC-1'
  AND NOT EXISTS (
      SELECT 1 FROM branch_inventory bi
      WHERE bi.branch_id = b.id AND bi.product_id = p.id
  );

IF OBJECT_ID(N'dbo.branch_inventory','U') IS NOT NULL
INSERT INTO branch_inventory (branch_id, product_id, quantity, reserved)
SELECT b.id, p.id, 40, 0
FROM branches b
JOIN products p ON p.sku IN (N'CK-001', N'BQ-2', N'BQ-3') -- đúng mã nào bạn đang seed thì giữ
WHERE b.code = N'HN-2'
  AND NOT EXISTS (
      SELECT 1 FROM branch_inventory bi
      WHERE bi.branch_id = b.id AND bi.product_id = p.id
  );

IF OBJECT_ID(N'dbo.branch_inventory','U') IS NOT NULL
INSERT INTO branch_inventory (branch_id, product_id, quantity, reserved)
SELECT b.id, p.id, 25, 0
FROM branches b
JOIN products p ON p.sku IN (N'CK-003', N'BM-003', N'BQ-004')
WHERE b.code = N'DN-1'
  AND NOT EXISTS (
      SELECT 1 FROM branch_inventory bi
      WHERE bi.branch_id = b.id AND bi.product_id = p.id
  );

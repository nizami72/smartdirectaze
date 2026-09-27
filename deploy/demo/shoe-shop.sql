-- Demo catalog: a shoe shop in Baku. Loaded by deploy/seed-demo-shop.sh into the shop given as :shop_id.
-- Re-running adds nothing twice: products are matched by SKU (unique per shop).
\set ON_ERROR_STOP on
BEGIN;

CREATE TEMP TABLE demo (sku text, title text, description text, price numeric, stock int, available boolean, size text, unit text) ON COMMIT DROP;
INSERT INTO demo VALUES
 ('SH-001', 'Туфли-лодочки женские чёрные', 'Натуральная кожа, каблук 8 см, мягкая стелька. Размеры 36–40. Подходят под офисный стиль.', 119, 12, true, '36-40', 'пара'),
 ('SH-002', 'Туфли-лодочки женские бежевые', 'Натуральная кожа, каблук 8 см. Размеры 36–40. Универсальный цвет под любой наряд.', 119, 6, true, '36-40', 'пара'),
 ('SH-003', 'Туфли на шпильке красные', 'Лакированная экокожа, шпилька 10 см. Размеры 36–39. Для вечера и праздников.', 135, 2, true, '36-39', 'пара'),
 ('SH-004', 'Оксфорды мужские коричневые', 'Натуральная кожа, кожаная подкладка, резиновая подошва. Размеры 40–45.', 149, 8, true, '40-45', 'пара'),
 ('SH-005', 'Туфли мужские классические чёрные', 'Натуральная кожа, шнуровка, для костюма. Размеры 40–46.', 139, 10, true, '40-46', 'пара'),
 ('SN-001', 'Кроссовки женские белые', 'Экокожа, лёгкая подошва, на каждый день. Размеры 36–41.', 89, 15, true, '36-41', 'пара'),
 ('SN-002', 'Кроссовки мужские беговые серые', 'Дышащая сетка, амортизирующая подошва. Размеры 40–46.', 109, 9, true, '40-46', 'пара'),
 ('SN-003', 'Кроссовки детские с подсветкой розовые', 'Светящаяся подошва, липучки, легко надевать. Размеры 28–35.', 55, 7, true, '28-35', 'пара'),
 ('SN-004', 'Кеды унисекс чёрные', 'Хлопковый канвас, резиновая подошва. Размеры 36–45. Сейчас нет в наличии, ожидаем поставку.', 59, 0, false, '36-45', 'пара'),
 ('BT-001', 'Ботинки мужские зимние на меху', 'Натуральная кожа, натуральный мех внутри, нескользящая подошва. Размеры 40–45.', 179, 4, true, '40-45', 'пара'),
 ('BT-002', 'Сапоги женские замшевые коричневые', 'Натуральная замша, высота голенища 40 см, каблук 5 см. Размеры 36–40. Сейчас нет в наличии.', 229, 0, false, '36-40', 'пара'),
 ('BT-003', 'Ботильоны женские на каблуке чёрные', 'Натуральная кожа, каблук 6 см, молния сбоку. Размеры 36–40.', 159, 5, true, '36-40', 'пара'),
 ('SD-001', 'Сандалии женские кожаные бежевые', 'Натуральная кожа, плоская подошва, регулируемый ремешок. Размеры 36–41.', 65, 11, true, '36-41', 'пара'),
 ('SD-002', 'Шлёпанцы мужские резиновые', 'Лёгкие, для пляжа и бассейна. Размеры 40–46.', 19, 25, true, '40-46', 'пара'),
 ('LF-001', 'Лоферы женские замшевые бордовые', 'Натуральная замша, мягкий задник. Размеры 36–40.', 99, 3, true, '36-40', 'пара'),
 ('LF-002', 'Мокасины мужские кожаные синие', 'Натуральная кожа, ручная прострочка. Размеры 40–45.', 115, 6, true, '40-45', 'пара'),
 ('HM-001', 'Тапочки домашние женские серые', 'Мягкий флис, нескользящая подошва. Размеры 36–41.', 25, 20, true, '36-41', 'пара'),
 ('AC-001', 'Стельки ортопедические', 'Поддержка свода стопы, обрезаются под размер 35–46.', 22, 30, true, '35-46', 'пара'),
 ('AC-002', 'Набор для ухода за обувью', 'Крем для гладкой кожи, щётка и губка. Бесцветный крем подходит к любому цвету.', 15, 40, true, NULL, 'шт'),
 ('AC-003', 'Ремень мужской кожаный чёрный', 'Натуральная кожа, металлическая пряжка, длина 110–125 см.', 45, 8, true, '110-125', 'шт');

INSERT INTO products (shop_id, sku, sale_price, currency, stock_quantity, is_available, size, track_quantity)
SELECT :shop_id, sku, price, 'AZN', stock, available, size, true FROM demo
ON CONFLICT (shop_id, sku) DO NOTHING;

INSERT INTO product_titles (product_id, lang_code, title_text)
SELECT p.id, 'ru', d.title FROM products p JOIN demo d ON d.sku = p.sku AND p.shop_id = :shop_id
WHERE NOT EXISTS (SELECT 1 FROM product_titles t WHERE t.product_id = p.id AND t.lang_code = 'ru');

INSERT INTO product_descriptions (product_id, lang_code, description_text)
SELECT p.id, 'ru', d.description FROM products p JOIN demo d ON d.sku = p.sku AND p.shop_id = :shop_id
WHERE NOT EXISTS (SELECT 1 FROM product_descriptions t WHERE t.product_id = p.id AND t.lang_code = 'ru');

INSERT INTO product_unit_of_measure (product_id, lang_code, unit_of_measure)
SELECT p.id, 'ru', d.unit FROM products p JOIN demo d ON d.sku = p.sku AND p.shop_id = :shop_id
WHERE NOT EXISTS (SELECT 1 FROM product_unit_of_measure t WHERE t.product_id = p.id AND t.lang_code = 'ru');

COMMIT;

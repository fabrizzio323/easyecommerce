-- 1. USERS
INSERT INTO users (name, email, password, role, street, city, zip_code, created_at) VALUES ('testAdministrator', 'admin@test.com', '$2a$10$oGAqFbBNI0CkpESL1Olz8OEwLPaljxlgSpxgS.GKPITYtSgCoJKr2', 'ADMINISTRATOR', 'Av. Fascio 123', 'San Salvador de Jujuy', '4600', CURRENT_TIMESTAMP);

INSERT INTO users (name, email, password, role, street, city, zip_code, created_at) VALUES ('testUser', 'user@test.com', '$2a$10$jo/wEzbfoxeGTVR/ceTsfuAqxdQhEpCbxHWdsEl6XsVn57TaLe2di', 'USER', 'Belgrano 450', 'San Salvador de Jujuy', '4600', CURRENT_TIMESTAMP);

-- 2. CATEGORIES
INSERT INTO categories (name, description) VALUES ('Electronics', 'Mobile devices, computers, and technology accessories');
INSERT INTO categories (name, description) VALUES ('Apparel', 'Clothing for adults and children, plus fashion accessories');
INSERT INTO categories (name, description) VALUES ('Home and Furniture', 'Home decor items, office furniture, and kitchen essentials');
INSERT INTO categories (name, description) VALUES ('Sports', 'Sports equipment, supplements, and training apparel');
INSERT INTO categories (name, description) VALUES ('Health and Beauty', 'Personal care, makeup, and pharmacy products');
INSERT INTO categories (name, description) VALUES ('Books and Stationery', 'Novels, school books, and office supplies');

-- 3. PRODUCT
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Smartphone X10', '6.5-inch display, 128GB storage', 450000.00, 15, true, 1, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Notebook Pro 14', 'i7 processor, 16GB RAM, 512GB SSD', 1200000.00, 8, true, 1, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Sony Headphones', 'Active noise cancellation, 30-hour battery life', 95000.00, 25, true, 1, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Cotton T-Shirt', 'Basic 100% cotton t-shirt, black color', 12500.00, 100, true, 2, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Jeans Slim Fit', 'Slim-fit denim jeans', 35000.00, 45, true, 2, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Running Sneakers', 'Running shoes with gel cushioning', 78000.00, 30, true, 2, CURRENT_TIMESTAMP);

-- 4. CART
INSERT INTO carts (user_id, created_at) VALUES (1, NOW());
INSERT INTO carts (user_id, created_at) VALUES (2, NOW());

-- 5. CART ITEMS
INSERT INTO cart_items (cart_id, product_id, quantity, price_at_purchase) VALUES (2, 1, 1, 450000.00);
INSERT INTO cart_items (cart_id, product_id, quantity, price_at_purchase) VALUES (2, 3, 2, 95000.00);

-- 6. ORDERS
INSERT INTO orders (user_id, total_amount, status, shipping_address, city, zip_code, created_at) VALUES (2, 640000.00, 'PAID', 'Belgrano 450', 'San Salvador de Jujuy', '4600', CURRENT_TIMESTAMP);

-- 7. ORDER ITEMS
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (1, 1, 1, 450000.00);
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (1, 3, 2, 95000.00);
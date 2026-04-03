-- 1. USUARIOS (Agregamos Street, City y Zip_Code)
INSERT INTO users (name, email, password, role, street, city, zip_code, created_at) VALUES ('testAdministrator', 'admin@test.com', '$2a$10$oGAqFbBNI0CkpESL1Olz8OEwLPaljxlgSpxgS.GKPITYtSgCoJKr2', 'ADMINISTRATOR', 'Av. Fascio 123', 'San Salvador de Jujuy', '4600', CURRENT_TIMESTAMP);

INSERT INTO users (name, email, password, role, street, city, zip_code, created_at) VALUES ('testUser', 'user@test.com', '$2a$10$jo/wEzbfoxeGTVR/ceTsfuAqxdQhEpCbxHWdsEl6XsVn57TaLe2di', 'USER', 'Belgrano 450', 'San Salvador de Jujuy', '4600', CURRENT_TIMESTAMP);

-- 2. CATEGORÍAS (Sin cambios)
INSERT INTO categories (name, description) VALUES ('Electrónica', 'Dispositivos móviles, computadoras y accesorios tecnológicos');
INSERT INTO categories (name, description) VALUES ('Indumentaria', 'Ropa para adultos, niños y accesorios de moda');
INSERT INTO categories (name, description) VALUES ('Hogar y Muebles', 'Artículos para decoración, mobiliario de oficina y cocina');
INSERT INTO categories (name, description) VALUES ('Deportes', 'Equipamiento deportivo, suplementos y ropa de entrenamiento');
INSERT INTO categories (name, description) VALUES ('Salud y Belleza', 'Cuidado personal, maquillaje y artículos de farmacia');
INSERT INTO categories (name, description) VALUES ('Libros y Papelería', 'Novelas, textos escolares y artículos de oficina');

-- 3. PRODUCTOS (Sin cambios)
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Smartphone X10', 'Pantalla 6.5 pulgadas, 128GB almacenamiento', 450000.00, 15, true, 1, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Notebook Pro 14', 'Procesador i7, 16GB RAM, SSD 512GB', 1200000.00, 8, true, 1, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Auriculares Sony', 'Cancelación de ruido activa, 30h batería', 95000.00, 25, true, 1, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Camiseta Algodón', 'Camiseta básica 100% algodón, color negro', 12500.00, 100, true, 2, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Jeans Slim Fit', 'Pantalón vaquero corte ajustado', 35000.00, 45, true, 2, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Zapatillas Running', 'Calzado deportivo con amortiguación de gel', 78000.00, 30, true, 2, CURRENT_TIMESTAMP);

-- 4. CARRIOS
INSERT INTO carts (user_id, created_at) VALUES (1, NOW());
INSERT INTO carts (user_id, created_at) VALUES (2, NOW());

-- 5. ITEMS DEL CARRITO (Usuario 2 tiene cosas en el carrito)
INSERT INTO cart_items (cart_id, product_id, quantity, price_at_purchase) VALUES (2, 1, 1, 450000.00);
INSERT INTO cart_items (cart_id, product_id, quantity, price_at_purchase) VALUES (2, 3, 2, 95000.00);

-- 6. ÓRDENES (Asegurate que los nombres de columnas coincidan con tus @Column de la entidad)
INSERT INTO orders (user_id, total_amount, status, shipping_address, city, zip_code, created_at) VALUES (2, 640000.00, 'PAID', 'Belgrano 450', 'San Salvador de Jujuy', '4600', CURRENT_TIMESTAMP);

-- 7. ITEMS DE LA ORDEN (Solo funcionarán si el de arriba NO falla)
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (1, 1, 1, 450000.00);
INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (1, 3, 2, 95000.00);
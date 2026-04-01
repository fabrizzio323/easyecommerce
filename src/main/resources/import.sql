-- 1. USUARIOS
INSERT INTO users (name, email, password, role, created_at) VALUES ('testAdministrator', 'admin@test.com', '$2a$10$oGAqFbBNI0CkpESL1Olz8OEwLPaljxlgSpxgS.GKPITYtSgCoJKr2', 'ADMINISTRATOR', CURRENT_TIMESTAMP);
INSERT INTO users (name, email, password, role, created_at) VALUES ('testUser', 'user@test.com', '$2a$10$jo/wEzbfoxeGTVR/ceTsfuAqxdQhEpCbxHWdsEl6XsVn57TaLe2di', 'USER', CURRENT_TIMESTAMP);

-- 2. CATEGORÍAS
INSERT INTO categories (name, description) VALUES ('Electrónica', 'Dispositivos móviles, computadoras y accesorios tecnológicos');
INSERT INTO categories (name, description) VALUES ('Indumentaria', 'Ropa para adultos, niños y accesorios de moda');
INSERT INTO categories (name, description) VALUES ('Hogar y Muebles', 'Artículos para decoración, mobiliario de oficina y cocina');
INSERT INTO categories (name, description) VALUES ('Deportes', 'Equipamiento deportivo, suplementos y ropa de entrenamiento');
INSERT INTO categories (name, description) VALUES ('Salud y Belleza', 'Cuidado personal, maquillaje y artículos de farmacia');
INSERT INTO categories (name, description) VALUES ('Libros y Papelería', 'Novelas, textos escolares y artículos de oficina');

-- 3. PRODUCTOS
-- Electrónica (ID 1)
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Smartphone X10', 'Pantalla 6.5 pulgadas, 128GB almacenamiento', 450000.00, 15, true, 1, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Notebook Pro 14', 'Procesador i7, 16GB RAM, SSD 512GB', 1200000.00, 8, true, 1, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Auriculares Sony', 'Cancelación de ruido activa, 30h batería', 95000.00, 25, true, 1, CURRENT_TIMESTAMP);

-- Indumentaria (ID 2)
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Camiseta Algodón', 'Camiseta básica 100% algodón, color negro', 12500.00, 100, true, 2, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Jeans Slim Fit', 'Pantalón vaquero corte ajustado', 35000.00, 45, true, 2, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Zapatillas Running', 'Calzado deportivo con amortiguación de gel', 78000.00, 30, true, 2, CURRENT_TIMESTAMP);

-- Hogar y Muebles (ID 3)
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Silla Ergonómica', 'Silla de oficina ajustable con soporte lumbar', 85000.00, 20, true, 3, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Lámpara LED', 'Lámpara de escritorio con 3 niveles de brillo', 18000.00, 30, true, 3, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Mesa de Centro', 'Madera de roble con acabado minimalista', 55000.00, 10, true, 3, CURRENT_TIMESTAMP);

-- Deportes (ID 4)
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Mancuernas 5kg', 'Par de mancuernas recubiertas en neopreno', 22000.00, 12, true, 4, CURRENT_TIMESTAMP);
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Mat de Yoga', 'Colchoneta antideslizante 6mm de espesor', 15000.00, 25, true, 4, CURRENT_TIMESTAMP);

-- Salud y Belleza (ID 5)
INSERT INTO products (name, description, price, stock, active, category_id, created_at) VALUES ('Protector Solar FPS 50', 'Crema protectora de amplio espectro 200ml', 9500.00, 60, true, 5, CURRENT_TIMESTAMP);
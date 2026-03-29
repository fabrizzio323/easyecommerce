INSERT INTO users (name, email, password, role, created_at) VALUES ('testAdministrator', 'admin@test.com', '$2a$10$oGAqFbBNI0CkpESL1Olz8OEwLPaljxlgSpxgS.GKPITYtSgCoJKr2', 'ADMINISTRATOR', NOW());
INSERT INTO users (name, email, password, role, created_at) VALUES ('testUser','user@test.com','$2a$10$jo/wEzbfoxeGTVR/ceTsfuAqxdQhEpCbxHWdsEl6XsVn57TaLe2di','USER',NOW());

INSERT INTO categories (name, description) VALUES ('Electrónica', 'Dispositivos móviles, computadoras y accesorios tecnológicos');
INSERT INTO categories (name, description) VALUES ('Indumentaria', 'Ropa para adultos, niños y accesorios de moda');
INSERT INTO categories (name, description) VALUES ('Hogar y Muebles', 'Artículos para decoración, mobiliario de oficina y cocina');
INSERT INTO categories (name, description) VALUES ('Deportes', 'Equipamiento deportivo, suplementos y ropa de entrenamiento');
INSERT INTO categories (name, description) VALUES ('Salud y Belleza', 'Cuidado personal, maquillaje y artículos de farmacia');
INSERT INTO categories (name, description) VALUES ('Libros y Papelería', 'Novelas, textos escolares y artículos de oficina');
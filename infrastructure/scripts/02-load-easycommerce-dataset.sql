-- EasyCommerce: generador incremental de dataset de 6.000.000 de registros
-- Base esperada: easyecommerce (PostgreSQL 15)
-- Ejecutar manualmente con psql. No elimina ni trunca datos existentes.
-- Contraseña de las cuentas sintéticas: password
-- IMPORTANTE: usar únicamente en un entorno de desarrollo/pruebas.

\set ON_ERROR_STOP on

BEGIN;

SET LOCAL synchronous_commit = OFF;
SET LOCAL statement_timeout = 0;
SET LOCAL lock_timeout = 0;

-- Objetivos TOTALES, incluyendo los registros que ya existen.
-- users=300000, categories=100, products=1000000, carts=250000,
-- cart_items=1000000, orders=1000000, order_items=2449900.
-- La suma exacta es 6.000.000.


-- ============================================================
-- CUENTAS LOCALES DE PRUEBA
-- Solo para desarrollo en esta PC.
-- No publicar estas credenciales en el repositorio.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO public.users (
    city,
    created_at,
    email,
    name,
    password,
    role,
    street,
    zip_code
)
VALUES
    (
        'San Salvador de Jujuy',
        NOW(),
        'user@test.com',
        'Usuario de Prueba',
        crypt('user123', gen_salt('bf', 10)),
        'USER',
        'Calle de Prueba 100',
        '4600'
    ),
    (
        'San Salvador de Jujuy',
        NOW(),
        'admin@test.com',
        'Administrador de Prueba',
        crypt('admin123', gen_salt('bf', 10)),
        'ADMINISTRATOR',
        'Calle de Prueba 200',
        '4600'
    )
    ON CONFLICT (email) DO NOTHING;


DO $$
BEGIN
    IF (SELECT COUNT(*) FROM public.users) > 300000
       OR (SELECT COUNT(*) FROM public.categories) > 100
       OR (SELECT COUNT(*) FROM public.products) > 1000000
       OR (SELECT COUNT(*) FROM public.carts) > 250000
       OR (SELECT COUNT(*) FROM public.cart_items) > 1000000
       OR (SELECT COUNT(*) FROM public.orders) > 1000000
       OR (SELECT COUNT(*) FROM public.order_items) > 2449900 THEN
        RAISE EXCEPTION
          'Una o más tablas ya superan el objetivo. No se insertó ningún registro.';
    END IF;
END $$;
-- 1. Categorías: completar hasta 100.
CREATE TEMP TABLE tmp_category_ids (id bigint) ON COMMIT DROP;
INSERT INTO tmp_category_ids SELECT id FROM public.categories;

WITH new_rows AS (
    INSERT INTO public.categories (description, name)
    SELECT
        'Categoría sintética para pruebas de carga',
        'Dataset Category ' || lpad(g::text, 3, '0')
    FROM generate_series(
        1,
        GREATEST(0, 100 - (SELECT COUNT(*) FROM public.categories))
    ) AS g
    RETURNING id
)
INSERT INTO tmp_category_ids SELECT id FROM new_rows;

CREATE TEMP TABLE tmp_category_index ON COMMIT DROP AS
SELECT id, row_number() OVER (ORDER BY id)::bigint AS rn
FROM tmp_category_ids;

-- 2. Usuarios: completar hasta 300.000.
CREATE TEMP TABLE tmp_user_ids (id bigint) ON COMMIT DROP;
INSERT INTO tmp_user_ids SELECT id FROM public.users;

WITH new_rows AS (
    INSERT INTO public.users
        (city, created_at, email, name, password, role, street, zip_code)
    SELECT
        CASE (g % 8)
            WHEN 0 THEN 'San Salvador de Jujuy'
            WHEN 1 THEN 'Palpala'
            WHEN 2 THEN 'Cordoba'
            WHEN 3 THEN 'Buenos Aires'
            WHEN 4 THEN 'Salta'
            WHEN 5 THEN 'Mendoza'
            WHEN 6 THEN 'Tucuman'
            ELSE 'Rosario'
        END,
        timestamp '2024-01-01 00:00:00' + (g % 900)::int * interval '1 day',
        'dataset.user.' || lpad(g::text, 8, '0') || '@easycommerce.test',
        'Usuario Dataset ' || g,
        '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
        'USER',
        'Calle de prueba ' || (g % 10000),
        lpad((1000 + (g % 8999))::text, 4, '0')
    FROM generate_series(
        1,
        GREATEST(0, 300000 - (SELECT COUNT(*) FROM public.users))
    ) AS g
    RETURNING id
)
INSERT INTO tmp_user_ids SELECT id FROM new_rows;

CREATE TEMP TABLE tmp_user_index ON COMMIT DROP AS
SELECT id, row_number() OVER (ORDER BY id)::bigint AS rn
FROM tmp_user_ids;

-- 3. Carritos: completar hasta 250.000.
-- La restricción UNIQUE(user_id) obliga a usar usuarios que todavía no tengan carrito.
CREATE TEMP TABLE tmp_cart_ids (id bigint) ON COMMIT DROP;
INSERT INTO tmp_cart_ids SELECT id FROM public.carts;

WITH eligible_users AS (
    SELECT u.id
    FROM tmp_user_ids u
    WHERE NOT EXISTS (
        SELECT 1 FROM public.carts c WHERE c.user_id = u.id
    )
    ORDER BY u.id
    LIMIT GREATEST(0, 250000 - (SELECT COUNT(*) FROM public.carts))
),
new_rows AS (
    INSERT INTO public.carts (created_at, user_id)
    SELECT
        timestamp '2025-01-01 00:00:00' + row_number() OVER (ORDER BY id) * interval '1 minute',
        id
    FROM eligible_users
    RETURNING id
)
INSERT INTO tmp_cart_ids SELECT id FROM new_rows;

CREATE TEMP TABLE tmp_cart_index ON COMMIT DROP AS
SELECT id, row_number() OVER (ORDER BY id)::bigint AS rn
FROM tmp_cart_ids;

-- 4. Productos: completar hasta 1.000.000.
CREATE TEMP TABLE tmp_product_ids (id bigint) ON COMMIT DROP;
INSERT INTO tmp_product_ids SELECT id FROM public.products;

WITH new_rows AS (
    INSERT INTO public.products
        (active, created_at, description, name, price, stock, category_id)
    SELECT
        (g % 10 <> 0),
        timestamp '2024-06-01 00:00:00' + (g % 700)::int * interval '1 hour',
        'Producto sintético generado para pruebas de rendimiento.',
        'Dataset Product ' || lpad(g::text, 8, '0'),
        (10 + (g % 250000))::numeric(38,2),
        1 + (g % 500)::int,
        c.id
    FROM generate_series(
        1,
        GREATEST(0, 1000000 - (SELECT COUNT(*) FROM public.products))
    ) AS g
    CROSS JOIN LATERAL (
        SELECT ci.id
        FROM tmp_category_index ci
        WHERE ci.rn = ((g - 1) % (SELECT COUNT(*) FROM tmp_category_index)) + 1
    ) c
    RETURNING id
)
INSERT INTO tmp_product_ids SELECT id FROM new_rows;

CREATE TEMP TABLE tmp_product_index ON COMMIT DROP AS
SELECT
    p.id,
    p.price,
    row_number() OVER (ORDER BY p.id)::bigint AS rn
FROM public.products p;

-- 5. Carrito: completar hasta 1.000.000 de artículos.
-- Se distribuyen los artículos entre los carritos y productos disponibles.
WITH limits AS (
    SELECT
        GREATEST(0, 1000000 - (SELECT COUNT(*) FROM public.cart_items))::bigint AS missing,
        (SELECT COUNT(*) FROM tmp_cart_index)::bigint AS cart_count,
        (SELECT COUNT(*) FROM tmp_product_index)::bigint AS product_count
)
INSERT INTO public.cart_items (price_at_purchase, quantity, cart_id, product_id)
SELECT
    p.price,
    1 + (g.n % 5)::int,
    c.id,
    p.id
FROM limits l
JOIN LATERAL generate_series(1, l.missing) AS g(n) ON l.missing > 0
JOIN tmp_cart_index c ON c.rn = ((g.n - 1) % l.cart_count) + 1
JOIN tmp_product_index p ON p.rn = ((g.n - 1) % l.product_count) + 1;

-- 6. Pedidos: completar hasta 1.000.000.
CREATE TEMP TABLE tmp_new_order_ids (id bigint) ON COMMIT DROP;

WITH limits AS (
    SELECT GREATEST(0, 1000000 - (SELECT COUNT(*) FROM public.orders))::bigint AS missing,
           (SELECT COUNT(*) FROM tmp_user_index)::bigint AS user_count
),
new_rows AS (
    INSERT INTO public.orders
        (city, created_at, shipping_address, status, total_amount, zip_code, user_id)
    SELECT
        CASE (g.n % 6)
            WHEN 0 THEN 'San Salvador de Jujuy'
            WHEN 1 THEN 'Cordoba'
            WHEN 2 THEN 'Buenos Aires'
            WHEN 3 THEN 'Salta'
            WHEN 4 THEN 'Mendoza'
            ELSE 'Tucuman'
        END,
        timestamp '2025-01-01 00:00:00' + (g.n % 500000)::int * interval '1 minute',
        'Dirección sintética ' || g.n,
        CASE (g.n % 4)
            WHEN 0 THEN 'CREATED'
            WHEN 1 THEN 'PAID'
            WHEN 2 THEN 'SHIPPED'
            ELSE 'CANCELED'
        END,
        0.00,
        lpad((1000 + (g.n % 8999))::text, 4, '0'),
        u.id
    FROM limits l
    JOIN LATERAL generate_series(1, l.missing) AS g(n) ON l.missing > 0
    JOIN tmp_user_index u ON u.rn = ((g.n - 1) % l.user_count) + 1
    RETURNING id
)
INSERT INTO tmp_new_order_ids SELECT id FROM new_rows;

CREATE TEMP TABLE tmp_new_order_index ON COMMIT DROP AS
SELECT id, row_number() OVER (ORDER BY id)::bigint AS rn
FROM tmp_new_order_ids;

-- 7. Detalles de pedido: completar hasta 2.449.900.
-- Cada pedido nuevo recibe 2 artículos y una parte recibe un tercero.
-- Con los conteos iniciales compartidos, esto produce exactamente 2.449.897 filas nuevas.
WITH limits AS (
    SELECT
        GREATEST(0, 2449900 - (SELECT COUNT(*) FROM public.order_items))::bigint AS missing,
        (SELECT COUNT(*) FROM tmp_new_order_index)::bigint AS order_count,
        (SELECT COUNT(*) FROM tmp_product_index)::bigint AS product_count
),
order_rows AS (
    SELECT
        o.id AS order_id,
        o.rn,
        2 + CASE
              WHEN o.rn <= (l.missing - (2 * l.order_count)) THEN 1
              ELSE 0
            END AS item_count,
        l.product_count
    FROM tmp_new_order_index o
    CROSS JOIN limits l
),
item_rows AS (
    SELECT
        o.order_id,
        row_number() OVER (ORDER BY o.rn, x.item_no)::bigint AS item_seq
    FROM order_rows o
    CROSS JOIN LATERAL generate_series(1, o.item_count) AS x(item_no)
)
INSERT INTO public.order_items (price, quantity, order_id, product_id)
SELECT
    p.price,
    1 + (i.item_seq % 4)::int,
    i.order_id,
    p.id
FROM item_rows i
CROSS JOIN limits l
JOIN tmp_product_index p
  ON p.rn = ((i.item_seq - 1) % l.product_count) + 1;

-- Actualizar únicamente el total de los pedidos sintéticos recién creados.
UPDATE public.orders o
SET total_amount = totals.amount
FROM (
    SELECT oi.order_id, SUM(oi.price * oi.quantity)::numeric(38,2) AS amount
    FROM public.order_items oi
    JOIN tmp_new_order_ids n ON n.id = oi.order_id
    GROUP BY oi.order_id
) totals
WHERE o.id = totals.order_id;

-- Verificación estricta: si no se alcanzan exactamente los objetivos, rollback.
DO $$
DECLARE
    n_users bigint;
    n_categories bigint;
    n_products bigint;
    n_carts bigint;
    n_cart_items bigint;
    n_orders bigint;
    n_order_items bigint;
BEGIN
    SELECT COUNT(*) INTO n_users FROM public.users;
    SELECT COUNT(*) INTO n_categories FROM public.categories;
    SELECT COUNT(*) INTO n_products FROM public.products;
    SELECT COUNT(*) INTO n_carts FROM public.carts;
    SELECT COUNT(*) INTO n_cart_items FROM public.cart_items;
    SELECT COUNT(*) INTO n_orders FROM public.orders;
    SELECT COUNT(*) INTO n_order_items FROM public.order_items;

    IF n_users <> 300000 OR n_categories <> 100 OR n_products <> 1000000
       OR n_carts <> 250000 OR n_cart_items <> 1000000
       OR n_orders <> 1000000 OR n_order_items <> 2449900 THEN
        RAISE EXCEPTION
          'Conteos finales inesperados: users %, categories %, products %, carts %, cart_items %, orders %, order_items %',
          n_users, n_categories, n_products, n_carts, n_cart_items, n_orders, n_order_items;
    END IF;
END $$;

COMMIT;

-- Resumen final.
SELECT
    (SELECT COUNT(*) FROM public.users) AS users,
    (SELECT COUNT(*) FROM public.categories) AS categories,
    (SELECT COUNT(*) FROM public.products) AS products,
    (SELECT COUNT(*) FROM public.carts) AS carts,
    (SELECT COUNT(*) FROM public.cart_items) AS cart_items,
    (SELECT COUNT(*) FROM public.orders) AS orders,
    (SELECT COUNT(*) FROM public.order_items) AS order_items,
    (
        (SELECT COUNT(*) FROM public.users) +
        (SELECT COUNT(*) FROM public.categories) +
        (SELECT COUNT(*) FROM public.products) +
        (SELECT COUNT(*) FROM public.carts) +
        (SELECT COUNT(*) FROM public.cart_items) +
        (SELECT COUNT(*) FROM public.orders) +
        (SELECT COUNT(*) FROM public.order_items)
    ) AS total_records;

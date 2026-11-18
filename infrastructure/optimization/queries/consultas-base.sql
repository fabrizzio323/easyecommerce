-- ============================================================
-- EASYCOMMERCE - CONSULTAS BASE PARA BENCHMARKING
-- Estado: ANTES DE LA OPTIMIZACION
-- Motor: PostgreSQL 15
-- ============================================================

-- 1. Productos activos ordenados por fecha de creación
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, name, price, stock, created_at
FROM products
WHERE active = TRUE
ORDER BY created_at DESC
LIMIT 20;


-- 2. Productos de una categoría ordenados por precio
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, name, price, stock
FROM products
WHERE category_id = (SELECT MIN(id) FROM categories)
ORDER BY price ASC
LIMIT 20;


-- 3. Historial de pedidos de un usuario
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, status, total_amount, created_at
FROM orders
WHERE user_id = (
    SELECT id
    FROM users
    WHERE email = 'user@test.com'
)
ORDER BY created_at DESC
LIMIT 10;


-- 4. Búsqueda de pedidos por estado
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, user_id, total_amount, created_at
FROM orders
WHERE status = (
    SELECT status
    FROM orders
    LIMIT 1
)
ORDER BY created_at DESC
LIMIT 20;


-- 5. Elementos de un carrito
EXPLAIN (ANALYZE, BUFFERS)
SELECT id, product_id, quantity, price_at_purchase
FROM cart_items
WHERE cart_id = (
    SELECT MIN(id)
    FROM carts
)
;


-- 6. Detalle de pedidos con información de productos
EXPLAIN (ANALYZE, BUFFERS)
SELECT
    oi.order_id,
    oi.product_id,
    p.name,
    oi.quantity,
    oi.price
FROM order_items oi
JOIN products p ON p.id = oi.product_id
WHERE oi.order_id = (
    SELECT MIN(id)
    FROM orders
);

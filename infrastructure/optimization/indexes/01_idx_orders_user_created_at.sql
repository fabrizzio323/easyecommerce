-- ==========================================================
-- OPTIMIZACION 01: HISTORIAL DE PEDIDOS POR USUARIO
-- Tabla: orders
-- Objetivo: acelerar la búsqueda y ordenamiento de pedidos.
-- Columnas: user_id, created_at DESC
-- ==========================================================

CREATE INDEX CONCURRENTLY IF NOT EXISTS
    idx_orders_user_created_at
ON public.orders (user_id, created_at DESC);

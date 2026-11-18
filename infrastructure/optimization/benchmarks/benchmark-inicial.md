# Benchmark inicial - EasyCommerce

## Entorno
- PostgreSQL 15
- Docker Compose
- Dataset: 6.000.000 de registros
- Índices existentes: 10

## Resultados originales

| Consulta | Tiempo (ms) |
|---|---:|
| Productos activos por fecha | 562.012 |
| Productos por categoría y precio | 70.033 |
| Pedidos de un usuario | 639.628 |
| Pedidos por estado | 138.916 |
| Elementos de un carrito | 199.422 |
| Detalle de pedidos | 442.795 |

## Problemas detectados

- Escaneos secuenciales paralelos sobre products.
- Escaneos secuenciales paralelos sobre orders.
- Escaneos secuenciales paralelos sobre cart_items.
- Escaneos secuenciales paralelos sobre order_items.
- Ordenamientos posteriores a los escaneos.

## Prioridades de optimización

1. Consulta de pedidos por usuario.
2. Productos activos ordenados por fecha.
3. Detalle de pedidos.
4. Elementos del carrito.
5. Pedidos por estado.
6. Productos por categoría y precio.

## Observaciones

Los tiempos corresponden a una ejecución inicial de cada consulta.
Se deberán repetir las mediciones después de cada optimización.
No se han creado ni eliminado índices.

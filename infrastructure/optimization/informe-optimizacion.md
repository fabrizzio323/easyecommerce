# EASYCOMMERCE - INFORME DE OPTIMIZACION DE CONSULTAS SQL

## 1. Objetivo

El objetivo de este trabajo es analizar y optimizar consultas SQL
del backend EasyCommerce, utilizando PostgreSQL 15 como motor
de base de datos.

Se busca reducir los tiempos de ejecucion mediante el analisis
de planes de ejecucion y la creacion de indices adecuados.

Las optimizaciones se validan mediante EXPLAIN (ANALYZE, BUFFERS).

## 2. Entorno de pruebas

- Proyecto: EasyCommerce.
- Motor de base de datos: PostgreSQL 15.
- Entorno: Docker Desktop.
- Base de datos: easyecommerce.
- Dataset: aproximadamente 6 millones de registros.
- Herramienta de analisis: EXPLAIN (ANALYZE, BUFFERS).

### Volumen aproximado del dataset

| Tabla | Registros |
|---|---:|
| users | 300.000 |
| categories | 106 |
| products | 1.000.025 |
| carts | 250.002 |
| cart_items | 1.000.008 |
| orders | 1.000.006 |
| order_items | 2.449.910 |

## 3. Metodologia

Para cada consulta se siguieron estos pasos:

1. Ejecutar la consulta original con EXPLAIN (ANALYZE, BUFFERS).
2. Identificar operaciones costosas en el plan de ejecucion.
3. Analizar posibles indices que permitan mejorar el acceso a los datos.
4. Crear el indice correspondiente.
5. Ejecutar nuevamente la consulta.
6. Comparar tiempos, buffers y operaciones utilizadas.
7. Documentar los resultados.

Las mediciones corresponden a ejecuciones puntuales y pueden
variar segun el estado de la cache y las condiciones del entorno.

## 4. Benchmark inicial

Los siguientes valores corresponden a las mediciones iniciales
realizadas antes de comenzar las optimizaciones.

| N.º | Consulta | Tiempo inicial |
|---|---|---:|
| 1 | Productos activos ordenados por fecha | 562.012 ms |
| 2 | Productos por categoria y precio | 70.033 ms |
| 3 | Pedidos de un usuario | 639.628 ms |
| 4 | Pedidos por estado | 138.916 ms |
| 5 | Elementos de un carrito | 199.422 ms |
| 6 | Detalle de pedidos | 442.795 ms |

Nota: los tiempos del benchmark inicial son las mediciones
originales. Algunas consultas tuvieron mediciones adicionales
durante el proceso de optimizacion. Esos valores se documentan
por separado para mantener la trazabilidad.

## 5. Optimizacion 01: Historial de pedidos por usuario

### Problema identificado

La consulta de historial de pedidos de un usuario presentaba
un tiempo inicial elevado y requeria mejorar el acceso a los
pedidos asociados a cada usuario.

Tiempo inicial del benchmark: 639.628 ms.

### Indice creado

CREATE INDEX CONCURRENTLY IF NOT EXISTS
idx_orders_user_created_at
ON public.orders (user_id, created_at DESC);

### Resultado posterior

Se verifico que PostgreSQL utilizaba el indice
idx_orders_user_created_at.

Mediciones posteriores registradas:

- Primera medicion: 0.530 ms.
- Segunda medicion: 4.150 ms.
- En la segunda medicion se observaron 9 bloques en cache.

### Analisis tecnico

El indice compuesto permite localizar los pedidos de un usuario
y recorrerlos en orden descendente de fecha de creacion.

Esto reduce la necesidad de recorrer una gran parte de la tabla
y facilita la recuperacion del historial solicitado.

La diferencia entre mediciones demuestra que los tiempos pueden
variar segun las condiciones de ejecucion y la cache.

## 6. Optimizacion 02: Productos activos ordenados por fecha

### Consulta analizada

EXPLAIN (ANALYZE, BUFFERS)
SELECT id, name, price, stock, created_at
FROM products
WHERE active = TRUE
ORDER BY created_at DESC
LIMIT 20;

### Problema identificado

El plan inicial utilizaba Parallel Seq Scan sobre products,
seguido de una operacion de ordenacion top-N heapsort.

Medicion previa a la creacion del indice:

- Execution Time: 294.524 ms.
- Buffers: shared hit=3374 read=15930.
- Rows Removed by Filter: aproximadamente 33.334 por worker.

El benchmark original habia registrado 562.012 ms para esta
consulta. Se conserva ese valor como referencia inicial.

### Indice creado

CREATE INDEX CONCURRENTLY IF NOT EXISTS
idx_products_active_created_at
ON public.products (created_at DESC)
WHERE active = TRUE;

### Resultado posterior

- Plan: Index Scan using idx_products_active_created_at.
- Buffers: shared hit=5.
- Planning Time: 1.959 ms.
- Execution Time: 0.228 ms.

### Comparacion

Tomando como referencia la medicion inmediatamente anterior
al indice:

- Tiempo anterior: 294.524 ms.
- Tiempo posterior: 0.228 ms.
- Reduccion aproximada: 99.92%.
- Mejora aproximada: 1.292 veces mas rapido.

### Analisis tecnico

Se creo un indice parcial sobre created_at para los productos
activos.

El indice permite recuperar directamente los registros en
orden descendente de fecha, evitando el escaneo secuencial
paralelo y la ordenacion posterior.

Al ser parcial, contiene unicamente las filas que cumplen
active = TRUE.

## 7. Optimizacion 03: Productos por categoria ordenados por precio

### Consulta analizada

EXPLAIN (ANALYZE, BUFFERS)
SELECT id, name, price, stock
FROM products
WHERE category_id = (
    SELECT MIN(id) FROM categories
)
ORDER BY price ASC
LIMIT 20;

### Problema identificado

El plan inicial utilizaba Parallel Seq Scan sobre products,
seguido de una ordenacion top-N heapsort.

Medicion previa a la creacion del indice:

- Execution Time: 88.989 ms.
- Buffers: shared hit=3664 read=15642.
- Rows Removed by Filter: aproximadamente 330.004 por worker.

### Indice creado

CREATE INDEX CONCURRENTLY IF NOT EXISTS
idx_products_category_price
ON public.products (category_id, price ASC);

### Resultado posterior

- Plan: Index Scan using idx_products_category_price.
- Index Cond: (category_id = $1).
- La operacion Sort desaparecio.
- Buffers: shared hit=12 read=13.
- Planning Time: 2.323 ms.
- Execution Time: 6.183 ms.

### Comparacion

- Tiempo anterior: 88.989 ms.
- Tiempo posterior: 6.183 ms.
- Reduccion aproximada: 93.05%.
- Mejora aproximada: 14.4 veces mas rapido.

### Analisis tecnico

El indice compuesto permite localizar los productos de una
categoria y recorrerlos directamente en orden ascendente
de precio.

Esto evita el escaneo secuencial de la tabla y la operacion
de ordenacion posterior de los resultados.

## 8. Optimizacion 04: Busqueda de pedidos por estado

### Consulta analizada

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

### Problema identificado

El plan previo utilizaba Parallel Seq Scan sobre orders
para filtrar los pedidos por estado.

Posteriormente, PostgreSQL realizaba un Gather Merge
y una ordenacion top-N heapsort para obtener los 20
registros mas recientes.

Medicion previa a la creacion del indice:

- Execution Time: 161.832 ms.
- Planning Time: 0.864 ms.
- Buffers: shared hit=174 read=28253.
- Filas descartadas: aproximadamente 250.001 por worker.

La primera ejecucion habia registrado aproximadamente
2.320 ms. Se conserva la medicion de 161.832 ms como
referencia inmediata anterior al indice.

### Indice creado

CREATE INDEX CONCURRENTLY IF NOT EXISTS
idx_orders_status_created_at
ON public.orders (status, created_at DESC);

### Resultado posterior

- Plan: Index Scan using idx_orders_status_created_at.
- Index Cond: (status = $0).
- La operacion Gather Merge desaparecio.
- No fue necesario realizar una ordenacion adicional.
- Buffers: shared hit=20 read=4.
- Planning Time: 0.707 ms.
- Execution Time: 0.751 ms.

### Comparacion

- Tiempo anterior: 161.832 ms.
- Tiempo posterior: 0.751 ms.
- Reduccion aproximada: 99.54%.
- Mejora aproximada: 215.5 veces mas rapido.

### Analisis tecnico

El indice compuesto sobre status y created_at permite
filtrar los pedidos por estado y recuperarlos directamente
en orden descendente de fecha.

PostgreSQL reemplazo el escaneo secuencial paralelo por
un acceso mediante Index Scan, eliminando tambien
la operacion de ordenamiento posterior.

La cantidad de bloques leidos se redujo considerablemente,
pasando de 28.253 bloques leidos a solamente 4.

### Consideracion sobre la consulta

La subconsulta utiliza LIMIT 1 sin ORDER BY:

SELECT status FROM orders LIMIT 1;

Por lo tanto, no garantiza que se seleccione un estado
determinado de manera estable.

La creacion del indice puede modificar el registro
encontrado primero y, por consiguiente, el estado utilizado
por la consulta.

Esta particularidad debe considerarse al interpretar
los resultados. La optimizacion tecnica fue comprobada,
pero la consulta deberia revisarse para garantizar
un comportamiento determinista en un escenario real.

## 9. Optimizacion 05: Elementos de un carrito

### Consulta analizada

EXPLAIN (ANALYZE, BUFFERS)
SELECT id, product_id, quantity, price_at_purchase
FROM cart_items
WHERE cart_id = (
    SELECT MIN(id)
    FROM carts
);

### Problema identificado

El plan previo utilizaba Parallel Seq Scan sobre cart_items,
recorriendo secuencialmente la tabla para localizar los
elementos correspondientes al carrito.

Medicion previa a la creacion del indice:

- Execution Time: 763.381 ms.
- Planning Time: 5.025 ms.
- Buffers: shared hit=2 read=8336.
- Filas devueltas: 4.
- Workers utilizados: 2.

La subconsulta MIN(id) ya utilizaba correctamente
un Index Only Scan sobre carts_pkey.

El benchmark inicial habia registrado 199.422 ms.
Se conserva ese valor como referencia original.

### Indice creado

CREATE INDEX CONCURRENTLY IF NOT EXISTS
idx_cart_items_cart_id
ON public.cart_items (cart_id);

### Resultado posterior

- Plan: Index Scan using idx_cart_items_cart_id.
- Index Cond: (cart_id = $1).
- Filas devueltas: 4.
- Buffers: shared hit=7 read=4.
- Planning Time: 0.872 ms.
- Execution Time: 1.204 ms.

La subconsulta continuo utilizando carts_pkey
mediante Index Only Scan.

### Comparacion

Tomando como referencia la medicion inmediatamente anterior
al indice:

- Tiempo anterior: 763.381 ms.
- Tiempo posterior: 1.204 ms.
- Reduccion aproximada: 99.84%.
- Mejora aproximada: 634 veces mas rapido.

### Analisis tecnico

El indice sobre cart_id permitio reemplazar el escaneo
secuencial paralelo de cart_items por un acceso directo
mediante Index Scan.

Esto redujo considerablemente la cantidad de bloques
leidos y el tiempo necesario para recuperar los elementos
del carrito.

La subconsulta encargada de obtener el identificador
minimo de carts ya utilizaba correctamente su clave
primaria, por lo que no requirio modificaciones.
## 10. Optimizacion 06: Detalle de pedidos

### Consulta analizada

EXPLAIN (ANALYZE, BUFFERS)
SELECT oi.id, oi.order_id, oi.product_id, oi.quantity, oi.price
FROM order_items oi
WHERE oi.order_id = (
    SELECT MIN(id)
    FROM orders
);

### Problema identificado

El plan previo utilizaba Parallel Seq Scan sobre order_items,
recorriendo secuencialmente la tabla para localizar los
elementos correspondientes al pedido.

Medicion previa a la creacion del indice:

- Execution Time: 123.118 ms.
- Planning Time: 0.647 ms.
- Buffers: shared hit=8077 read=12343.
- Filas devueltas: 11.
- Workers utilizados: 2.

La subconsulta MIN(id) ya utilizaba correctamente
un Index Only Scan sobre orders_pkey.

El benchmark inicial habia registrado 442.795 ms.
Se conserva ese valor como referencia original.

### Indice creado

CREATE INDEX CONCURRENTLY IF NOT EXISTS
idx_order_items_order_id
ON public.order_items (order_id);

### Resultado posterior

- Plan: Index Scan using idx_order_items_order_id.
- Index Cond: (order_id = $1).
- Filas devueltas: 11.
- Buffers: shared hit=8 read=1.
- Planning Time: 1.180 ms.
- Execution Time: 0.207 ms.

La subconsulta continuo utilizando orders_pkey
mediante Index Only Scan.

### Comparacion

Tomando como referencia la medicion inmediatamente anterior
al indice:

- Tiempo anterior: 123.118 ms.
- Tiempo posterior: 0.207 ms.
- Reduccion aproximada: 99.83%.
- Mejora aproximada: 595 veces mas rapido.

### Analisis tecnico

El indice sobre order_id permitio reemplazar el escaneo
secuencial paralelo de order_items por un acceso directo
mediante Index Scan.

La consulta pudo recuperar los 11 elementos del pedido
sin recorrer secuencialmente la tabla completa.

La cantidad de bloques leidos se redujo considerablemente,
pasando de 12.343 bloques leidos a solamente 1.

La subconsulta encargada de obtener el identificador
minimo de orders ya utilizaba correctamente su clave
primaria, por lo que no requirio modificaciones.
## 11. Resumen de optimizaciones realizadas

| N.º | Consulta | Indice creado | Resultado |
|---|---|---|---|
| 01 | Historial de pedidos por usuario | idx_orders_user_created_at | Indice utilizado |
| 02 | Productos activos por fecha | idx_products_active_created_at | Index Scan, sin escaneo secuencial |
| 03 | Productos por categoria y precio | idx_products_category_price | Index Scan, sin Sort |
| 04 | Pedidos por estado | idx_orders_status_created_at | Index Scan, sin Sort |
| 05 | Elementos de un carrito | idx_cart_items_cart_id | Index Scan |
| 06 | Detalle de pedidos | idx_order_items_order_id | Index Scan |

## 12. Consideraciones tecnicas

- Los indices mejoran determinadas consultas de lectura,
  pero ocupan espacio adicional en disco.
- Cada indice implica trabajo adicional durante operaciones
  de INSERT, UPDATE y DELETE.
- No todas las consultas necesitan indices adicionales.
- La eleccion de indices debe basarse en los filtros,
  ordenamientos y planes de ejecucion reales.
- Las mediciones individuales no constituyen por si solas
  un benchmark estadistico.
- Para obtener resultados mas representativos conviene
  repetir las mediciones bajo condiciones comparables.
- Las consultas utilizadas en los benchmarks deben tener
  criterios deterministas cuando se espera comparar
  exactamente los mismos resultados.

## 13. Conclusiones

Las optimizaciones realizadas demuestran la importancia
de analizar los planes de ejecucion antes de modificar
la estructura de una base de datos.

En las consultas de productos se consiguio reemplazar
escaneos secuenciales y operaciones de ordenacion por
accesos mediante indices.

La optimizacion de elementos de carrito permitio reemplazar un escaneo secuencial paralelo por un Index Scan, reduciendo el tiempo medido de 763.381 ms a 1.204 ms.

Las optimizaciones sobre orders permitieron mejorar
el acceso al historial de pedidos por usuario y la busqueda
de pedidos por estado mediante indices compuestos.

La optimizacion de pedidos por estado redujo el tiempo
de ejecucion medido de 161.832 ms a 0.751 ms,
con una mejora aproximada del 99.54%.

En total se completaron seis optimizaciones respaldadas
por sus respectivos planes de ejecucion y archivos
de resultados individuales.

La optimizacion del detalle de pedidos redujo el tiempo medido de 123.118 ms a 0.207 ms, con una mejora aproximada del 99.83%.

El trabajo evidencia como una seleccion adecuada de indices
puede reducir considerablemente los tiempos de respuesta
en una base de datos con un volumen elevado de registros.

Las siguientes etapas consistiran en evaluar el impacto
global de las optimizaciones y realizar pruebas de
rendimiento integrales bajo condiciones controladas.
========================================
EASYCOMMERCE - FIN DEL INFORME





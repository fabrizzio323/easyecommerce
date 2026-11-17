import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getProducts, createProduct, updateProduct, deleteProduct } from "../api/productService";
import { getCategories, createCategory, updateCategory, deleteCategory } from "../api/categoryService";
import { getAllOrdersAdmin, updateOrderStatus } from "../api/orderService";
import { normalizeOrder } from "../utils/normalize";
import { money, dateTime } from "../utils/format";
import FormField from "../components/FormField";

export default function AdminDashboard() {
    const [tab, setTab] = useState("products"); // 'products' | 'categories' | 'orders'

    // --- State: Products ---
    const [products, setProducts] = useState([]);
    const [productPage, setProductPage] = useState(0);
    const [productTotalElements, setProductTotalElements] = useState(0);
    const [productTotalPages, setProductTotalPages] = useState(1);
    const [categories, setCategories] = useState([]);
    const [loadingProducts, setLoadingProducts] = useState(false);
    const [productModal, setProductModal] = useState(false);
    const [editingProduct, setEditingProduct] = useState(null);
    const [productForm, setProductForm] = useState({
        name: "",
        description: "",
        price: "",
        stock: "",
        categoryId: "",
        imageUrl: "",
    });

    // --- State: Categories ---
    const [loadingCategories, setLoadingCategories] = useState(false);
    const [categoryModal, setCategoryModal] = useState(false);
    const [editingCategory, setEditingCategory] = useState(null);
    const [categoryForm, setCategoryForm] = useState({ name: "", description: "" });

    // --- State: Orders ---
    const [orders, setOrders] = useState([]);
    const [orderPage, setOrderPage] = useState(0);
    const [orderTotalElements, setOrderTotalElements] = useState(null);
    const [orderTotalPages, setOrderTotalPages] = useState(1);
    const [ordersError, setOrdersError] = useState("");
    const [loadingOrders, setLoadingOrders] = useState(false);
    const [updatingOrderId, setUpdatingOrderId] = useState(null);

    // --- Global Notifications / Errors ---
    const [msg, setMsg] = useState("");
    const [error, setError] = useState("");

    const showSuccess = (text) => {
        setMsg(text);
        setError("");
        setTimeout(() => setMsg(""), 4000);
    };

    const showError = (text) => {
        setError(text);
        setTimeout(() => setError(""), 5000);
    };

    // --- Load Data ---
    const loadProducts = async () => {
        setLoadingProducts(true);
        try {
            const { data } = await getProducts(productPage, 100);
            const list = Array.isArray(data) ? data : (data?.content ?? []);
            setProducts(list);
            setProductTotalElements(Array.isArray(data) ? list.length : (data?.totalElements ?? list.length));
            setProductTotalPages(Array.isArray(data) ? 1 : (data?.totalPages ?? 1));
        } catch {
            showError("No se pudieron cargar los productos.");
        } finally {
            setLoadingProducts(false);
        }
    };

    const loadCategories = async () => {
        setLoadingCategories(true);
        try {
            const { data } = await getCategories();
            const list = Array.isArray(data) ? data : (data?.content || []);
            setCategories(list);
        } catch {
            showError("No se pudieron cargar las categorías.");
        } finally {
            setLoadingCategories(false);
        }
    };

    const loadOrders = async () => {
        setLoadingOrders(true);
        setOrdersError("");
        try {
            const { data } = await getAllOrdersAdmin(orderPage, 20);
            if (!Array.isArray(data?.content)) {
                throw new Error("La API administrativa no devolvió una página de pedidos");
            }
            setOrders(data.content.map(normalizeOrder));
            setOrderTotalElements(data.totalElements ?? data.content.length);
            setOrderTotalPages(Math.max(1, data.totalPages ?? 1));
        } catch (err) {
            console.error("Error al cargar pedidos administrativos:", err);
            setOrdersError("No se pudieron cargar los pedidos. Verificá la API administrativa.");
            setOrders([]);
        } finally {
            setLoadingOrders(false);
        }
    };

    useEffect(() => {
        loadCategories();
    }, []);

    useEffect(() => {
        loadProducts();
    }, [productPage]);

    // Cargar los pedidos solo al visitar su pestaña o cambiar de página.
    useEffect(() => {
        if (tab === "orders") {
            loadOrders();
        }
    }, [tab, orderPage]);

    // --- Handlers: Products ---
    const openNewProduct = () => {
        setEditingProduct(null);
        setProductForm({
            name: "",
            description: "",
            price: "",
            stock: "",
            categoryId: categories[0]?.id || "",
            imageUrl: "",
        });
        setProductModal(true);
    };

    const openEditProduct = (p) => {
        setEditingProduct(p);
        setProductForm({
            name: p.name,
            description: p.description || "",
            price: p.price,
            stock: p.stock,
            categoryId: p.category?.id || categories[0]?.id || "",
            imageUrl: p.imageUrl || "",
        });
        setProductModal(true);
    };

    const handleSaveProduct = async (e) => {
        e.preventDefault();
        try {
            const payload = {
                name: productForm.name,
                description: productForm.description,
                price: parseFloat(productForm.price),
                stock: parseInt(productForm.stock, 10),
                idCategory: parseInt(productForm.categoryId, 10),
                imageUrl: productForm.imageUrl || null,
            };

            if (editingProduct) {
                await updateProduct(editingProduct.id, payload);
                showSuccess(`Producto "${payload.name}" actualizado correctamente.`);
            } else {
                await createProduct(payload);
                showSuccess(`Producto "${payload.name}" creado con éxito.`);
            }
            setProductModal(false);
            loadProducts();
        } catch (err) {
            showError(err.response?.data?.message || "Error al guardar el producto.");
        }
    };

    const handleDeleteProduct = async (p) => {
        if (!window.confirm(`¿Seguro que deseas dar de baja el producto "${p.name}"?`)) return;
        try {
            await deleteProduct(p.id);
            showSuccess(`Producto "${p.name}" dado de baja correctamente.`);
            loadProducts();
        } catch (err) {
            showError(err.response?.data?.message || "No se pudo eliminar el producto.");
        }
    };

    // --- Handlers: Categories ---
    const openNewCategory = () => {
        setEditingCategory(null);
        setCategoryForm({ name: "", description: "" });
        setCategoryModal(true);
    };

    const openEditCategory = (c) => {
        setEditingCategory(c);
        setCategoryForm({ name: c.name, description: c.description || "" });
        setCategoryModal(true);
    };

    const handleSaveCategory = async (e) => {
        e.preventDefault();
        try {
            if (editingCategory) {
                await updateCategory(editingCategory.id, categoryForm);
                showSuccess(`Categoría "${categoryForm.name}" actualizada.`);
            } else {
                await createCategory(categoryForm);
                showSuccess(`Categoría "${categoryForm.name}" creada.`);
            }
            setCategoryModal(false);
            loadCategories();
        } catch (err) {
            showError(err.response?.data?.message || "Error al guardar la categoría.");
        }
    };

    const handleDeleteCategory = async (c) => {
        if (!window.confirm(`¿Eliminar la categoría "${c.name}"?`)) return;
        try {
            await deleteCategory(c.id);
            showSuccess(`Categoría "${c.name}" eliminada.`);
            loadCategories();
        } catch (err) {
            showError(err.response?.data?.message || "No se pudo eliminar la categoría.");
        }
    };

    // --- Handlers: Orders Status ---
    const handleStatusChange = async (orderId, newStatus) => {
        setUpdatingOrderId(orderId);
        try {
            await updateOrderStatus(orderId, newStatus);
            showSuccess(`Estado del pedido #${orderId} actualizado a ${newStatus}.`);
            loadOrders();
        } catch (err) {
            showError(err.response?.data?.message || "No se pudo actualizar el estado del pedido.");
        } finally {
            setUpdatingOrderId(null);
        }
    };

    return (
        <div>
            {/* Header del Panel */}
            <div className="page-header" style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                    <h1 style={{ margin: "0 0 4px" }}>Panel de Administración</h1>
                    <p style={{ margin: 0, color: "var(--muted)", fontSize: 13 }}>
                        Gestión global del catálogo, categorías y logística de órdenes
                    </p>
                </div>
                <Link to="/products" className="btn secondary" style={{ fontSize: 13 }}>
                    ← Volver a la Tienda
                </Link>
            </div>

            {/* Mensajes de Notificación */}
            {msg && (
                <div className="notice" style={{ marginBottom: 16, background: "#f0fdf4", borderColor: "#bbf7d0", color: "#166534" }}>
                    {msg}
                </div>
            )}
            {error && (
                <div className="notice" style={{ marginBottom: 16, background: "#fef2f2", borderColor: "#fecaca", color: "#991b1b" }}>
                    {error}
                </div>
            )}

            {/* Pestañas de Navegación */}
            <div style={{ display: "flex", gap: 8, borderBottom: "1px solid var(--border)", marginBottom: 20 }}>
                <button
                    className={`btn ${tab === "products" ? "" : "secondary"}`}
                    style={{ borderRadius: "8px 8px 0 0", borderBottom: "none" }}
                    onClick={() => setTab("products")}
                >
                    📦 Productos ({productTotalElements.toLocaleString("es-AR")})
                </button>
                <button
                    className={`btn ${tab === "categories" ? "" : "secondary"}`}
                    style={{ borderRadius: "8px 8px 0 0", borderBottom: "none" }}
                    onClick={() => setTab("categories")}
                >
                    🏷️ Categorías ({categories.length})
                </button>
                <button
                    className={`btn ${tab === "orders" ? "" : "secondary"}`}
                    style={{ borderRadius: "8px 8px 0 0", borderBottom: "none" }}
                    onClick={() => setTab("orders")}
                >
                    🚚 Pedidos de Clientes{orderTotalElements === null ? "" : ` (${orderTotalElements.toLocaleString("es-AR")})`}
                </button>
            </div>

            {/* TAB 1: PRODUCTOS */}
            {tab === "products" && (
                <div>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
                        <p style={{ margin: 0, fontSize: 14, color: "var(--muted)" }}>
                            Control del catálogo general de la tienda
                        </p>
                        <button className="btn" onClick={openNewProduct}>
                            + Nuevo Producto
                        </button>
                    </div>

                    {loadingProducts ? (
                        <p style={{ color: "var(--muted)" }}>Cargando catálogo...</p>
                    ) : (
                        <div className="table-wrap">
                            <table className="table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Producto</th>
                                        <th>Categoría</th>
                                        <th style={{ textAlign: "right" }}>Precio</th>
                                        <th style={{ textAlign: "center" }}>Stock</th>
                                        <th style={{ textAlign: "right" }}>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {products.map((p) => (
                                        <tr key={p.id}>
                                            <td>#{p.id}</td>
                                            <td>
                                                <strong>{p.name}</strong>
                                                {p.description && (
                                                    <p style={{ margin: "2px 0 0", fontSize: 12, color: "var(--muted)" }}>
                                                        {p.description.length > 50 ? `${p.description.substring(0, 50)}...` : p.description}
                                                    </p>
                                                )}
                                            </td>
                                            <td>
                                                <span className="tag">{p.categoryName || "Sin categoría"}</span>
                                            </td>
                                            <td style={{ textAlign: "right" }}>{money(p.price)}</td>
                                            <td style={{ textAlign: "center" }}>
                                                <span style={{
                                                    color: p.stock > 5 ? "var(--text)" : "var(--danger)",
                                                    fontWeight: 600
                                                }}>
                                                    {p.stock}
                                                </span>
                                            </td>
                                            <td style={{ textAlign: "right" }}>
                                                <button
                                                    className="icon-btn"
                                                    style={{ marginRight: 8, fontSize: 13 }}
                                                    onClick={() => openEditProduct(p)}
                                                >
                                                    Editar
                                                </button>
                                                <button
                                                    className="icon-btn danger"
                                                    style={{ fontSize: 13 }}
                                                    onClick={() => handleDeleteProduct(p)}
                                                >
                                                    Dar de baja
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                    {productTotalPages > 1 && (
                        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 16, margin: "20px 0" }}>
                            <button
                                className="btn secondary"
                                disabled={loadingProducts || productPage === 0}
                                onClick={() => setProductPage((prev) => Math.max(0, prev - 1))}
                            >
                                ← Anterior
                            </button>
                            <span>Página {productPage + 1} de {productTotalPages}</span>
                            <button
                                className="btn secondary"
                                disabled={loadingProducts || productPage >= productTotalPages - 1}
                                onClick={() => setProductPage((prev) => Math.min(productTotalPages - 1, prev + 1))}
                            >
                                Siguiente →
                            </button>
                        </div>
                    )}
                </div>
            )}

            {/* TAB 2: CATEGORÍAS */}
            {tab === "categories" && (
                <div>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 16 }}>
                        <p style={{ margin: 0, fontSize: 14, color: "var(--muted)" }}>
                            Clasificación de productos
                        </p>
                        <button className="btn" onClick={openNewCategory}>
                            + Nueva Categoría
                        </button>
                    </div>

                    {loadingCategories ? (
                        <p style={{ color: "var(--muted)" }}>Cargando categorías...</p>
                    ) : (
                        <div className="table-wrap">
                            <table className="table">
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>Nombre</th>
                                        <th>Descripción</th>
                                        <th style={{ textAlign: "right" }}>Acciones</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {categories.map((c) => (
                                        <tr key={c.id}>
                                            <td>#{c.id}</td>
                                            <td><strong>{c.name}</strong></td>
                                            <td style={{ color: "var(--muted)", fontSize: 13 }}>{c.description || "—"}</td>
                                            <td style={{ textAlign: "right" }}>
                                                <button
                                                    className="icon-btn"
                                                    style={{ marginRight: 8, fontSize: 13 }}
                                                    onClick={() => openEditCategory(c)}
                                                >
                                                    Editar
                                                </button>
                                                <button
                                                    className="icon-btn danger"
                                                    style={{ fontSize: 13 }}
                                                    onClick={() => handleDeleteCategory(c)}
                                                >
                                                    Eliminar
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            )}

            {/* TAB 3: PEDIDOS DE CLIENTES (LOGÍSTICA) */}
            {tab === "orders" && (
                <div>
                    <div style={{ marginBottom: 16 }}>
                        <p style={{ margin: 0, fontSize: 14, color: "var(--muted)" }}>
                            Listado paginado de órdenes y despacho logístico
                            {orderTotalElements !== null && ` · Total: ${orderTotalElements.toLocaleString("es-AR")}`}
                        </p>
                    </div>

                    {loadingOrders ? (
                        <p style={{ color: "var(--muted)" }}>Cargando pedidos...</p>
                    ) : ordersError ? (
                        <div className="empty">
                            <h3>{ordersError}</h3>
                            <button className="btn secondary" onClick={loadOrders}>Reintentar</button>
                        </div>
                    ) : orders.length === 0 ? (
                        <div className="empty">
                            <h3>No hay pedidos registrados aún</h3>
                        </div>
                    ) : (
                        <div className="table-wrap">
                            <table className="table">
                                <thead>
                                    <tr>
                                        <th>Orden</th>
                                        <th>Cliente</th>
                                        <th>Dirección de Envío</th>
                                        <th>Fecha</th>
                                        <th style={{ textAlign: "right" }}>Total</th>
                                        <th>Estado Actual</th>
                                        <th style={{ textAlign: "center" }}>Cambiar Estado</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {orders.map((o) => (
                                        <tr key={o.id}>
                                            <td><strong>#{o.id}</strong></td>
                                            <td>
                                                <span style={{ fontSize: 13 }}>{o.userEmail}</span>
                                            </td>
                                            <td>
                                                <p style={{ margin: 0, fontSize: 13 }}>{o.shippingAddress}</p>
                                                <p style={{ margin: 0, fontSize: 11, color: "var(--muted)" }}>
                                                    {o.city} (CP: {o.zipCode})
                                                </p>
                                            </td>
                                            <td style={{ fontSize: 12, color: "var(--muted)" }}>
                                                {dateTime(o.createdAt)}
                                            </td>
                                            <td style={{ textAlign: "right", fontWeight: 600 }}>
                                                {money(o.total)}
                                            </td>
                                            <td>
                                                <span className={`status ${String(o.status).toLowerCase()}`}>
                                                    {o.status}
                                                </span>
                                            </td>
                                            <td style={{ textAlign: "center" }}>
                                                <select
                                                    value={o.status}
                                                    disabled={updatingOrderId === o.id}
                                                    onChange={(e) => handleStatusChange(o.id, e.target.value)}
                                                    style={{
                                                        padding: "4px 8px",
                                                        borderRadius: 6,
                                                        border: "1px solid var(--border)",
                                                        fontSize: 12,
                                                        background: "#fff"
                                                    }}
                                                >
                                                    <option value="CREATED">CREATED</option>
                                                    <option value="PAID">PAID</option>
                                                    <option value="SHIPPED">SHIPPED</option>
                                                    <option value="DELIVERED">DELIVERED</option>
                                                    <option value="CANCELLED">CANCELLED</option>
                                                </select>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                    {!loadingOrders && !ordersError && orderTotalPages > 1 && (
                        <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 16, margin: "20px 0" }}>
                            <button
                                className="btn secondary"
                                disabled={orderPage === 0 || updatingOrderId !== null}
                                onClick={() => setOrderPage((prev) => Math.max(0, prev - 1))}
                            >
                                ← Anterior
                            </button>
                            <span>Página {orderPage + 1} de {orderTotalPages.toLocaleString("es-AR")}</span>
                            <button
                                className="btn secondary"
                                disabled={orderPage >= orderTotalPages - 1 || updatingOrderId !== null}
                                onClick={() => setOrderPage((prev) => Math.min(orderTotalPages - 1, prev + 1))}
                            >
                                Siguiente →
                            </button>
                        </div>
                    )}
                </div>
            )}

            {/* MODAL: PRODUCTO */}
            {productModal && (
                <div className="overlay" style={{ background: "rgba(0,0,0,0.5)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 100 }}>
                    <div className="card" style={{ maxWidth: 480, width: "100%", maxHeight: "90vh", overflowY: "auto" }}>
                        <h3 style={{ margin: "0 0 16px" }}>
                            {editingProduct ? "Editar Producto" : "Nuevo Producto"}
                        </h3>
                        <form onSubmit={handleSaveProduct}>
                            <FormField
                                label="Nombre del producto"
                                name="name"
                                value={productForm.name}
                                onChange={(e) => setProductForm({ ...productForm, name: e.target.value })}
                                required
                                autoFocus
                            />
                            <div className="field">
                                <label>Categoría</label>
                                <select
                                    className="input"
                                    value={productForm.categoryId}
                                    onChange={(e) => setProductForm({ ...productForm, categoryId: e.target.value })}
                                    required
                                >
                                    <option value="" disabled>Selecciona una categoría</option>
                                    {categories.map((c) => (
                                        <option key={c.id} value={c.id}>{c.name}</option>
                                    ))}
                                </select>
                            </div>
                            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
                                <FormField
                                    label="Precio ($)"
                                    name="price"
                                    type="number"
                                    step="0.01"
                                    min="0"
                                    value={productForm.price}
                                    onChange={(e) => setProductForm({ ...productForm, price: e.target.value })}
                                    required
                                />
                                <FormField
                                    label="Stock disponible"
                                    name="stock"
                                    type="number"
                                    min="0"
                                    value={productForm.stock}
                                    onChange={(e) => setProductForm({ ...productForm, stock: e.target.value })}
                                    required
                                />
                            </div>
                            <div className="field">
                                <label>Descripción</label>
                                <textarea
                                    className="input"
                                    rows="3"
                                    value={productForm.description}
                                    onChange={(e) => setProductForm({ ...productForm, description: e.target.value })}
                                />
                            </div>
                            <FormField
                                label="URL de Imagen (Opcional)"
                                name="imageUrl"
                                placeholder="https://..."
                                value={productForm.imageUrl}
                                onChange={(e) => setProductForm({ ...productForm, imageUrl: e.target.value })}
                            />
                            <div style={{ display: "flex", gap: 8, marginTop: 18 }}>
                                <button type="button" className="btn secondary" style={{ flex: 1 }} onClick={() => setProductModal(false)}>
                                    Cancelar
                                </button>
                                <button type="submit" className="btn" style={{ flex: 1 }}>
                                    Guardar
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            {/* MODAL: CATEGORÍA */}
            {categoryModal && (
                <div className="overlay" style={{ background: "rgba(0,0,0,0.5)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 100 }}>
                    <div className="card" style={{ maxWidth: 400, width: "100%" }}>
                        <h3 style={{ margin: "0 0 16px" }}>
                            {editingCategory ? "Editar Categoría" : "Nueva Categoría"}
                        </h3>
                        <form onSubmit={handleSaveCategory}>
                            <FormField
                                label="Nombre de la categoría"
                                name="name"
                                value={categoryForm.name}
                                onChange={(e) => setCategoryForm({ ...categoryForm, name: e.target.value })}
                                required
                                autoFocus
                            />
                            <div className="field">
                                <label>Descripción</label>
                                <textarea
                                    className="input"
                                    rows="2"
                                    value={categoryForm.description}
                                    onChange={(e) => setCategoryForm({ ...categoryForm, description: e.target.value })}
                                />
                            </div>
                            <div style={{ display: "flex", gap: 8, marginTop: 18 }}>
                                <button type="button" className="btn secondary" style={{ flex: 1 }} onClick={() => setCategoryModal(false)}>
                                    Cancelar
                                </button>
                                <button type="submit" className="btn" style={{ flex: 1 }}>
                                    Guardar
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
}

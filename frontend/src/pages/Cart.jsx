import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";
import { checkout } from "../api/orderService";
import { money } from "../utils/format";

export default function Cart() {
    const { cart, loading, updateItem, removeItem, clear, refresh } = useCart();
    const { user } = useAuth();
    const navigate = useNavigate();
    const [busyId, setBusyId] = useState(null);
    const [paying, setPaying] = useState(false);
    const [clearing, setClearing] = useState(false);
    const [error, setError] = useState("");

    const run = async (id, fn) => {
        setBusyId(id);
        setError("");
        try {
            await fn();
        } catch (err) {
            setError(err.response?.data?.message || err.message || "No se pudo actualizar el carrito.");
        } finally {
            setBusyId(null);
        }
    };

    const handleClearCart = async () => {
        if (!window.confirm("¿Estás seguro de que deseas vaciar tu carrito?")) return;
        setClearing(true);
        setError("");
        try {
            await clear();
        } catch (err) {
            setError(err.response?.data?.message || "No se pudo vaciar el carrito.");
        } finally {
            setClearing(false);
        }
    };

    const handleCheckout = async () => {
        setPaying(true);
        setError("");
        try {
            const { data } = await checkout();
            await refresh();
            navigate(data?.id ? `/orders/${data.id}` : "/orders", { state: { justOrdered: true } });
        } catch (err) {
            setError(err.response?.data?.message || err.message || "No se pudo completar la compra.");
        } finally {
            setPaying(false);
        }
    };

    if (loading && cart.items.length === 0) {
        return (
            <div className="container" style={{ padding: "40px 0", textAlign: "center" }}>
                <p style={{ color: "var(--muted)" }}>Cargando carrito...</p>
            </div>
        );
    }

    if (cart.items.length === 0) {
        return (
            <div className="empty">
                <h3>Tu carrito está vacío</h3>
                <p>Agrega productos desde el catálogo para empezar a comprar.</p>
                <Link to="/products" className="btn" style={{ marginTop: 12 }}>Ver productos</Link>
            </div>
        );
    }

    return (
        <div>
            <div className="page-header" style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <div>
                    <h1>Carrito de compras</h1>
                    <p>{cart.items.length} {cart.items.length === 1 ? "producto diferente" : "productos diferentes"}</p>
                </div>
                <button
                    type="button"
                    className="btn secondary"
                    style={{ fontSize: 13, color: "var(--danger)" }}
                    disabled={clearing || paying}
                    onClick={handleClearCart}
                >
                    {clearing ? "Vaciando..." : "🗑️ Vaciar carrito"}
                </button>
            </div>

            <div className="two-col">
                <div className="card" style={{ paddingTop: 4, paddingBottom: 4 }}>
                    {cart.items.map((item) => (
                        <div className="cart-row" key={item.id}>
                            <div className="thumb">
                                {item.imageUrl ? <img src={item.imageUrl} alt={item.name} /> : "📦"}
                            </div>

                            <div>
                                <p style={{ margin: 0, fontWeight: 500, fontSize: 15 }}>{item.name}</p>
                                <p style={{ margin: "2px 0 8px", fontSize: 13, color: "var(--muted)" }}>
                                    {money(item.price)} c/u
                                </p>
                                <button
                                    className="icon-btn danger"
                                    style={{ fontSize: 13, padding: 0 }}
                                    disabled={busyId === item.id}
                                    onClick={() => run(item.id, () => removeItem(item.id))}
                                >
                                    Quitar
                                </button>
                            </div>

                            <div className="qty">
                                <button
                                    aria-label="Restar"
                                    disabled={busyId === item.id || item.quantity <= 1}
                                    onClick={() => run(item.id, () => updateItem(item.id, item.quantity - 1))}
                                >−</button>
                                <span>{item.quantity}</span>
                                <button
                                    aria-label="Sumar"
                                    disabled={busyId === item.id}
                                    onClick={() => run(item.id, () => updateItem(item.id, item.quantity + 1))}
                                >+</button>
                            </div>

                            <strong style={{ fontSize: 15, minWidth: 90, textAlign: "right" }}>
                                {money(item.subtotal)}
                            </strong>
                        </div>
                    ))}
                </div>

                <aside>
                    {/* Domicilio de envío */}
                    {user && (
                        <div className="card" style={{ marginBottom: 16 }}>
                            <h4 style={{ margin: "0 0 8px", fontSize: 14, fontWeight: 600 }}>📍 Domicilio de entrega</h4>
                            <p style={{ margin: "0 0 4px", fontSize: 13, color: "var(--text)" }}>
                                {user.street || "Dirección no especificada"}
                            </p>
                            <p style={{ margin: 0, fontSize: 12, color: "var(--muted)" }}>
                                {user.city ? `${user.city} (CP: ${user.zipCode})` : ""}
                            </p>
                        </div>
                    )}

                    {/* Resumen de costos */}
                    <div className="card">
                        <h3 style={{ margin: "0 0 16px", fontSize: 16, fontWeight: 600 }}>Resumen del pedido</h3>
                        <div className="summary-line">
                            <span style={{ color: "var(--muted)" }}>Total de unidades</span>
                            <span>{cart.items.reduce((s, i) => s + i.quantity, 0)}</span>
                        </div>
                        <div className="summary-line">
                            <span style={{ color: "var(--muted)" }}>Envío</span>
                            <span style={{ color: "#166534", fontWeight: 500 }}>Gratis</span>
                        </div>
                        <div className="summary-total">
                            <span>Total</span>
                            <span>{money(cart.total)}</span>
                        </div>

                        {error && (
                            <p style={{ color: "var(--danger)", fontSize: 13, margin: "14px 0 0" }}>
                                {error}
                            </p>
                        )}

                        <button
                            className="btn"
                            style={{ width: "100%", marginTop: 18, padding: "12px 16px" }}
                            onClick={handleCheckout}
                            disabled={paying || cart.items.length === 0}
                        >
                            {paying ? "Procesando compra..." : "Confirmar compra (Checkout)"}
                        </button>
                        <Link to="/products" className="btn secondary" style={{ display: "block", textAlign: "center", marginTop: 8 }}>
                            Seguir comprando
                        </Link>
                    </div>
                </aside>
            </div>
        </div>
    );
}

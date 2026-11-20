import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { getMyOrders } from "../api/orderService";
import { normalizeOrder } from "../utils/normalize";
import { money, dateTime } from "../utils/format";

export default function OrderDetail() {
    const { id } = useParams();
    const location = useLocation();
    const [order, setOrder] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        getMyOrders()
            .then(({ data }) => {
                const list = Array.isArray(data) ? data : data?.content ?? [];
                const found = list.find((o) => String(o.id) === String(id));
                if (found) {
                    setOrder(normalizeOrder(found));
                } else {
                    setError(`No se encontró el pedido #${id}.`);
                }
            })
            .catch(() => setError("No se pudieron cargar los detalles del pedido."))
            .finally(() => setLoading(false));
    }, [id]);

    if (loading) {
        return (
            <div style={{ padding: "40px 0", textAlign: "center" }}>
                <p style={{ color: "var(--muted)" }}>Cargando pedido...</p>
            </div>
        );
    }

    if (error || !order) {
        return (
            <div className="empty">
                <h3>{error || "Pedido no encontrado"}</h3>
                <Link to="/orders" className="btn secondary" style={{ marginTop: 12 }}>← Volver a mis pedidos</Link>
            </div>
        );
    }

    return (
        <div>
            <Link to="/orders" style={{ fontSize: 13, color: "var(--muted)", textDecoration: "none" }}>
                ← Volver a mis pedidos
            </Link>

            <div className="page-header" style={{ marginTop: 14, display: "flex", justifyContent: "space-between", alignItems: "flex-start" }}>
                <div>
                    <h1 style={{ margin: "0 0 4px" }}>Pedido #{order.id}</h1>
                    <p style={{ margin: 0, color: "var(--muted)", fontSize: 13 }}>
                        Realizado el {dateTime(order.createdAt)}
                    </p>
                </div>
                <span className={`status ${String(order.status).toLowerCase()}`} style={{ fontSize: 13, padding: "6px 12px" }}>
                    {order.status}
                </span>
            </div>

            {location.state?.justOrdered && (
                <div className="notice" style={{ marginBottom: 18, background: "#f0fdf4", borderColor: "#bbf7d0", color: "#166534" }}>
                    🎉 ¡Tu compra ha sido procesada con éxito! Hemos registrado tu pedido.
                </div>
            )}

            <div className="two-col" style={{ alignItems: "flex-start" }}>
                {/* Listado de ítems de la orden */}
                <div className="card" style={{ padding: "16px 20px" }}>
                    <h3 style={{ margin: "0 0 16px", fontSize: 16, fontWeight: 600 }}>Ítems adquiridos</h3>
                    {order.items.map((item, i) => (
                        <div
                            key={item.id ?? i}
                            style={{
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                                padding: "12px 0",
                                borderBottom: i < order.items.length - 1 ? "1px solid var(--border)" : "none"
                            }}
                        >
                            <div>
                                <p style={{ margin: "0 0 4px", fontWeight: 500, fontSize: 14 }}>{item.name}</p>
                                <p style={{ margin: 0, fontSize: 12, color: "var(--muted)" }}>
                                    {item.quantity} × {money(item.price)}
                                </p>
                            </div>
                            <strong style={{ fontSize: 14 }}>{money(item.subtotal)}</strong>
                        </div>
                    ))}
                </div>

                {/* Resumen y datos de envío */}
                <aside>
                    <div className="card" style={{ marginBottom: 16 }}>
                        <h4 style={{ margin: "0 0 10px", fontSize: 14, fontWeight: 600 }}>📍 Destino de envío</h4>
                        <p style={{ margin: "0 0 4px", fontSize: 13, color: "var(--text)" }}>
                            {order.shippingAddress || "Dirección no especificada"}
                        </p>
                        <p style={{ margin: 0, fontSize: 12, color: "var(--muted)" }}>
                            {order.city ? `${order.city} (CP: ${order.zipCode})` : ""}
                        </p>
                    </div>

                    <div className="card">
                        <h4 style={{ margin: "0 0 12px", fontSize: 14, fontWeight: 600 }}>Total de la compra</h4>
                        <div className="summary-line">
                            <span style={{ color: "var(--muted)" }}>Cantidad de productos</span>
                            <span>{order.items.reduce((s, i) => s + i.quantity, 0)}</span>
                        </div>
                        <div className="summary-line">
                            <span style={{ color: "var(--muted)" }}>Costo de envío</span>
                            <span style={{ color: "#166534", fontWeight: 500 }}>Gratis</span>
                        </div>
                        <div className="summary-total">
                            <span>Total abonado</span>
                            <span style={{ fontSize: 18, fontWeight: 700 }}>{money(order.total)}</span>
                        </div>
                    </div>
                </aside>
            </div>
        </div>
    );
}

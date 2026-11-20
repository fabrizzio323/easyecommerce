import { useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { getOrders } from "../api/orderService";
import { normalizeOrder } from "../utils/normalize";
import { money, dateTime } from "../utils/format";

export default function Orders() {
    const location = useLocation();
    const [orders, setOrders] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        getOrders()
            .then(({ data }) => {
                const list = Array.isArray(data) ? data : data?.content ?? [];
                setOrders(list.map(normalizeOrder).sort((a, b) => b.id - a.id));
            })
            .catch(() => setError("No se pudieron cargar tus pedidos."))
            .finally(() => setLoading(false));
    }, []);

    return (
        <div>
            <div className="page-header">
                <div>
                    <h1>Mis pedidos</h1>
                    {!loading && !error && <p>{orders.length} {orders.length === 1 ? "pedido" : "pedidos"}</p>}
                </div>
            </div>

            {location.state?.justOrdered && <div className="notice">Compra realizada correctamente.</div>}
            {loading && <p style={{ color: "var(--muted)" }}>Cargando...</p>}
            {error && <div className="empty"><h3>{error}</h3></div>}

            {!loading && !error && orders.length === 0 && (
                <div className="empty">
                    <h3>Todavía no tienes pedidos</h3>
                    <p>Cuando compres, aparecerán aquí.</p>
                    <Link to="/products" className="btn">Ver productos</Link>
                </div>
            )}

            {orders.length > 0 && (
                <div className="table-wrap">
                    <table className="table">
                        <thead>
                            <tr>
                                <th>Pedido</th><th>Fecha</th><th>Estado</th>
                                <th style={{ textAlign: "right" }}>Total</th><th></th>
                            </tr>
                        </thead>
                        <tbody>
                            {orders.map((o) => (
                                <tr key={o.id}>
                                    <td>#{o.id}</td>
                                    <td>{dateTime(o.createdAt)}</td>
                                    <td><span className={`status ${String(o.status).toLowerCase()}`}>{o.status}</span></td>
                                    <td style={{ textAlign: "right" }}>{money(o.total)}</td>
                                    <td style={{ textAlign: "right" }}>
                                        <Link to={`/orders/${o.id}`} style={{ textDecoration: "underline", fontSize: 13 }}>Ver</Link>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            )}
        </div>
    );
}
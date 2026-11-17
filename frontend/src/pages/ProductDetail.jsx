import { useEffect, useState } from "react";
import { Link, useParams, useNavigate } from "react-router-dom";
import { getProduct } from "../api/productService";
import { useCart } from "../context/CartContext";
import { useAuth } from "../context/AuthContext";
import { money } from "../utils/format";

export default function ProductDetail() {
    const { id } = useParams();
    const navigate = useNavigate();
    const { addItem } = useCart();
    const { user } = useAuth();

    const [product, setProduct] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [quantity, setQuantity] = useState(1);
    const [adding, setAdding] = useState(false);
    const [successMsg, setSuccessMsg] = useState("");

    useEffect(() => {
        setLoading(true);
        getProduct(id)
            .then(({ data }) => setProduct(data))
            .catch(() => setError("No se pudo cargar la información del producto."))
            .finally(() => setLoading(false));
    }, [id]);

    const handleAddToCart = async () => {
        if (!user) {
            navigate("/login", { state: { from: { pathname: `/products/${id}` } } });
            return;
        }

        setAdding(true);
        setSuccessMsg("");
        setError("");
        try {
            await addItem(product.id, quantity);
            setSuccessMsg(`¡Se agregaron ${quantity} unidad(es) de ${product.name} al carrito!`);
            setTimeout(() => setSuccessMsg(""), 4000);
        } catch (err) {
            setError(err.response?.data?.message || err.message || "Error al agregar al carrito.");
        } finally {
            setAdding(false);
        }
    };

    if (loading) {
        return (
            <div className="container" style={{ padding: "40px 0", textAlign: "center" }}>
                <p style={{ color: "var(--muted)" }}>Cargando producto...</p>
            </div>
        );
    }

    if (error && !product) {
        return (
            <div className="container" style={{ padding: "40px 0", textAlign: "center" }}>
                <div className="card" style={{ maxWidth: 450, margin: "0 auto" }}>
                    <h3>Producto no encontrado</h3>
                    <p style={{ color: "var(--muted)", margin: "8px 0 16px" }}>{error}</p>
                    <Link to="/products" className="btn secondary">← Volver al catálogo</Link>
                </div>
            </div>
        );
    }

    const isAvailable = product.stock > 0;

    return (
        <div className="container" style={{ maxWidth: 900, padding: "24px 16px" }}>
            <Link to="/products" style={{ fontSize: 13, color: "var(--muted)", display: "inline-block", marginBottom: 16 }}>
                ← Volver al catálogo de productos
            </Link>

            <div className="card" style={{ display: "grid", gridTemplateColumns: "1fr 1.2fr", gap: 32, padding: 32 }}>
                {/* Visual Placeholder o Imagen */}
                <div style={{
                    background: "var(--surface)",
                    borderRadius: 12,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    minHeight: 260,
                    border: "1px solid var(--border)",
                    color: "var(--muted)",
                    fontSize: 48
                }}>
                    {product.imageUrl ? (
                        <img src={product.imageUrl} alt={product.name} style={{ maxWidth: "100%", maxHeight: "100%", borderRadius: 12, objectFit: "cover" }} />
                    ) : (
                        "📦"
                    )}
                </div>

                {/* Info del Producto */}
                <div>
                    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", gap: 12 }}>
                        <h1 style={{ margin: "0 0 8px", fontSize: 24, fontWeight: 600 }}>{product.name}</h1>
                        {product.category?.name && (
                            <span className="tag" style={{ fontSize: 12, padding: "4px 10px" }}>
                                {product.category.name}
                            </span>
                        )}
                    </div>

                    <p style={{ fontSize: 26, fontWeight: 700, margin: "12px 0 16px", color: "var(--text)" }}>
                        {money(product.price)}
                    </p>

                    <p style={{ color: "var(--muted)", fontSize: 14, lineHeight: 1.6, margin: "0 0 24px" }}>
                        {product.description || "Sin descripción disponible para este producto."}
                    </p>

                    <div style={{ padding: "14px 0", borderTop: "1px solid var(--border)", borderBottom: "1px solid var(--border)", marginBottom: 20 }}>
                        <div style={{ display: "flex", justifyContent: "space-between", fontSize: 13 }}>
                            <span style={{ color: "var(--muted)" }}>Disponibilidad:</span>
                            <strong style={{ color: isAvailable ? "var(--text)" : "var(--danger)" }}>
                                {isAvailable ? `${product.stock} unidades en stock` : "Agotado"}
                            </strong>
                        </div>
                    </div>

                    {successMsg && (
                        <div className="notice" style={{ marginBottom: 16, background: "#f0fdf4", borderColor: "#bbf7d0", color: "#166534" }}>
                            {successMsg}
                        </div>
                    )}

                    {error && (
                        <p style={{ color: "var(--danger)", fontSize: 13, margin: "0 0 14px" }}>
                            {error}
                        </p>
                    )}

                    {isAvailable ? (
                        <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
                            <div style={{ display: "flex", alignItems: "center", border: "1px solid var(--border)", borderRadius: 8 }}>
                                <button
                                    type="button"
                                    className="icon-btn"
                                    style={{ padding: "8px 12px" }}
                                    disabled={quantity <= 1 || adding}
                                    onClick={() => setQuantity(q => Math.max(1, q - 1))}
                                >
                                    -
                                </button>
                                <span style={{ padding: "0 10px", fontSize: 14, fontWeight: 600 }}>{quantity}</span>
                                <button
                                    type="button"
                                    className="icon-btn"
                                    style={{ padding: "8px 12px" }}
                                    disabled={quantity >= product.stock || adding}
                                    onClick={() => setQuantity(q => Math.min(product.stock, q + 1))}
                                >
                                    +
                                </button>
                            </div>

                            <button
                                className="btn"
                                style={{ flex: 1, padding: "12px 20px" }}
                                disabled={adding}
                                onClick={handleAddToCart}
                            >
                                {adding ? "Agregando..." : user ? "Agregar al carrito" : "Inicia sesión para comprar"}
                            </button>
                        </div>
                    ) : (
                        <button className="btn secondary" style={{ width: "100%" }} disabled>
                            Producto no disponible
                        </button>
                    )}
                </div>
            </div>
        </div>
    );
}

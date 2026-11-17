import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useCart } from "../context/CartContext";
import { money } from "../utils/format";

export default function ProductCard({ product }) {
    const { user } = useAuth();
    const { addItem } = useCart();
    const navigate = useNavigate();
    const [adding, setAdding] = useState(false);
    const [added, setAdded] = useState(false);

    const handleAdd = async () => {
        if (!user) return navigate("/login", { state: { from: { pathname: "/products" } } });
        setAdding(true);
        try {
            await addItem(product.id, 1);
            setAdded(true);
            setTimeout(() => setAdded(false), 1500);
        } catch {
            alert("No se pudo agregar al carrito.");
        } finally {
            setAdding(false);
        }
    };

    return (
        <div className="product-card">
            <div className="image">
                {product.imageUrl ? (
                    <img src={product.imageUrl} alt={product.name} style={{ width: "100%", height: "100%", objectFit: "cover" }} />
                ) : "Sin imagen"}
            </div>
            <div className="body">
                {product.category && <span className="tag">{product.category.name}</span>}
                <h4 style={{ margin: "8px 0 2px", fontSize: 15, fontWeight: 500 }}>{product.name}</h4>
                <p style={{ margin: "0 0 12px", fontSize: 14, color: "var(--muted)" }}>{money(product.price)}</p>
                <div style={{ display: "flex", gap: 8 }}>
                    <Link to={`/products/${product.id}`} className="btn secondary">Detalle</Link>
                    <button className="btn" onClick={handleAdd} disabled={adding}>
                        {added ? "Agregado ✓" : adding ? "..." : "Agregar"}
                    </button>
                </div>
            </div>
        </div>
    );
}
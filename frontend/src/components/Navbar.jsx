import { Link } from "react-router-dom";
import { useCart } from "../context/CartContext";

export default function Navbar({ user, isAdmin, onLogout }) {
    const { count } = useCart();

    return (
        <header className="navbar">
            <nav>
                <Link to="/" className="brand" style={{ display: "flex", alignItems: "center", gap: 6 }}>
                    🛍️ <span>EasyCommerce</span>
                </Link>
                <Link to="/products">Catálogo</Link>
                {user && <Link to="/orders">Mis pedidos</Link>}
                {isAdmin && (
                    <Link to="/admin" style={{ fontWeight: 600, color: "#166534", display: "flex", alignItems: "center", gap: 4 }}>
                        <span>🛡️</span> <span>Admin</span>
                    </Link>
                )}
            </nav>
            <div style={{ display: "flex", gap: 12, alignItems: "center" }}>
                {user ? (
                    <>
                        <Link to="/cart" className="cart-link" aria-label="Carrito" style={{ position: "relative", display: "flex", alignItems: "center", gap: 6 }}>
                            <span>🛒 Carrito</span>
                            {count > 0 && (
                                <span className="cart-badge" style={{
                                    background: "var(--accent)",
                                    color: "#fff",
                                    borderRadius: 999,
                                    fontSize: 11,
                                    fontWeight: 700,
                                    padding: "2px 6px",
                                    lineHeight: 1
                                }}>
                                    {count}
                                </span>
                            )}
                        </Link>
                        <span style={{ fontSize: 13, color: "var(--muted)", maxWidth: 160, overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }} title={user.email}>
                            {user.name || user.email}
                        </span>
                        <button className="btn secondary" style={{ fontSize: 12, padding: "6px 12px" }} onClick={onLogout}>
                            Salir
                        </button>
                    </>
                ) : (
                    <>
                        <Link to="/login" className="btn secondary" style={{ fontSize: 13, padding: "7px 14px" }}>
                            Ingresar
                        </Link>
                        <Link to="/register" className="btn" style={{ fontSize: 13, padding: "7px 14px" }}>
                            Registrarse
                        </Link>
                    </>
                )}
            </div>
        </header>
    );
}

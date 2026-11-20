import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import FormField from "../components/FormField";

export default function Login() {
    const { login } = useAuth();
    const navigate = useNavigate();
    const location = useLocation();
    const redirectTo = location.state?.from?.pathname || "/";

    const [form, setForm] = useState({ email: "", password: "" });
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const handleChange = (e) =>
        setForm({ ...form, [e.target.name]: e.target.value });

    const setDemoCredentials = (email, password) => {
        setForm({ email, password });
        setError("");
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSubmitting(true);
        try {
            await login(form);
            navigate(redirectTo, { replace: true });
        } catch (err) {
            const status = err.response?.status;
            if (status === 401 || status === 403) {
                setError("Email o contraseña incorrectos.");
            } else if (!err.response) {
                setError("No se pudo conectar con el servidor.");
            } else {
                setError(err.response?.data?.message || "Ocurrió un error. Intenta de nuevo.");
            }
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="card" style={{ maxWidth: 400, margin: "60px auto" }}>
            <h2 style={{ margin: "0 0 4px", fontSize: 20 }}>Iniciar sesión</h2>
            <p style={{ margin: "0 0 20px", fontSize: 14, color: "var(--muted)" }}>
                Accede a tu cuenta de EasyCommerce
            </p>

            <form onSubmit={handleSubmit}>
                <FormField
                    label="Email"
                    name="email"
                    type="email"
                    placeholder="nombre@correo.com"
                    value={form.email}
                    onChange={handleChange}
                    required
                    autoFocus
                />
                <FormField
                    label="Contraseña"
                    name="password"
                    type="password"
                    placeholder="••••••••"
                    value={form.password}
                    onChange={handleChange}
                    required
                />

                {error && (
                    <p style={{ color: "var(--danger)", fontSize: 13, margin: "0 0 14px" }}>
                        {error}
                    </p>
                )}

                <button className="btn" style={{ width: "100%" }} disabled={submitting}>
                    {submitting ? "Iniciando sesión..." : "Entrar"}
                </button>
            </form>

            <div style={{ marginTop: 20, paddingTop: 16, borderTop: "1px solid var(--border)" }}>
                <p style={{ margin: "0 0 8px", fontSize: 12, color: "var(--muted)", textAlign: "center" }}>
                    Credenciales de prueba preconfiguradas:
                </p>
                <div style={{ display: "flex", gap: 8 }}>
                    <button
                        type="button"
                        className="btn secondary"
                        style={{ flex: 1, fontSize: 12, padding: "6px 8px" }}
                        onClick={() => setDemoCredentials("user@test.com", "user123")}
                    >
                        👤 Usuario
                    </button>
                    <button
                        type="button"
                        className="btn secondary"
                        style={{ flex: 1, fontSize: 12, padding: "6px 8px" }}
                        onClick={() => setDemoCredentials("admin@test.com", "admin123")}
                    >
                        🛡️ Administrador
                    </button>
                </div>
            </div>

            <p style={{ margin: "18px 0 0", fontSize: 13, color: "var(--muted)", textAlign: "center" }}>
                ¿No tienes cuenta? <Link to="/register" style={{ textDecoration: "underline", color: "var(--text)" }}>Regístrate aquí</Link>
            </p>
        </div>
    );
}

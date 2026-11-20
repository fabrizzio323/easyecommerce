import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { register } from "../api/authService";
import { useAuth } from "../context/AuthContext";
import FormField from "../components/FormField";

export default function Register() {
    const { login } = useAuth();
    const navigate = useNavigate();

    const [form, setForm] = useState({
        name: "",
        email: "",
        password: "",
        street: "",
        city: "",
        zipCode: "",
    });
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const handleChange = (e) =>
        setForm({ ...form, [e.target.name]: e.target.value });

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSubmitting(true);
        try {
            await register(form);
            // Iniciar sesión automáticamente tras el registro
            await login({ email: form.email, password: form.password });
            navigate("/products", { replace: true });
        } catch (err) {
            const status = err.response?.status;
            if (status === 400) {
                setError(err.response?.data?.message || "Datos inválidos. Verifica todos los campos.");
            } else if (status === 409) {
                setError("El email ya se encuentra registrado.");
            } else if (!err.response) {
                setError("No se pudo conectar con el servidor.");
            } else {
                setError(err.response?.data?.message || "Ocurrió un error al registrarte.");
            }
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div className="card" style={{ maxWidth: 440, margin: "40px auto" }}>
            <h2 style={{ margin: "0 0 4px", fontSize: 20 }}>Crear cuenta</h2>
            <p style={{ margin: "0 0 20px", fontSize: 14, color: "var(--muted)" }}>
                Completa tus datos y dirección de envío para comenzar a comprar
            </p>

            <form onSubmit={handleSubmit}>
                <FormField
                    label="Nombre completo"
                    name="name"
                    type="text"
                    placeholder="Juan Pérez"
                    value={form.name}
                    onChange={handleChange}
                    required
                    autoFocus
                />
                <FormField
                    label="Email"
                    name="email"
                    type="email"
                    placeholder="juan@correo.com"
                    value={form.email}
                    onChange={handleChange}
                    required
                />
                <FormField
                    label="Contraseña (mínimo 6 caracteres)"
                    name="password"
                    type="password"
                    placeholder="••••••••"
                    value={form.password}
                    onChange={handleChange}
                    required
                />

                <div style={{ marginTop: 18, marginBottom: 8, borderTop: "1px solid var(--border)", paddingTop: 14 }}>
                    <p style={{ margin: "0 0 10px", fontSize: 13, fontWeight: 600, color: "var(--text)" }}>
                        📍 Domicilio de entrega
                    </p>
                    <FormField
                        label="Calle y número"
                        name="street"
                        type="text"
                        placeholder="Av. Belgrano 450"
                        value={form.street}
                        onChange={handleChange}
                        required
                    />
                    <div style={{ display: "grid", gridTemplateColumns: "2fr 1fr", gap: 12 }}>
                        <FormField
                            label="Ciudad / Localidad"
                            name="city"
                            type="text"
                            placeholder="San Salvador de Jujuy"
                            value={form.city}
                            onChange={handleChange}
                            required
                        />
                        <FormField
                            label="Cód. Postal"
                            name="zipCode"
                            type="text"
                            placeholder="4600"
                            value={form.zipCode}
                            onChange={handleChange}
                            required
                        />
                    </div>
                </div>

                {error && (
                    <p style={{ color: "var(--danger)", fontSize: 13, margin: "10px 0 14px" }}>
                        {error}
                    </p>
                )}

                <button className="btn" style={{ width: "100%", marginTop: 8 }} disabled={submitting}>
                    {submitting ? "Creando cuenta..." : "Registrarme"}
                </button>
            </form>

            <p style={{ margin: "16px 0 0", fontSize: 13, color: "var(--muted)", textAlign: "center" }}>
                ¿Ya tienes una cuenta? <Link to="/login" style={{ textDecoration: "underline", color: "var(--text)" }}>Inicia sesión</Link>
            </p>
        </div>
    );
}

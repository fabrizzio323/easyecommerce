import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function ProtectedRoute({ role }) {
    const { user, loading, isAdmin } = useAuth();
    const location = useLocation();

    if (loading) return null;
    if (!user) return <Navigate to="/login" state={{ from: location }} replace />;
    if (role === "ADMIN" && !isAdmin) return <Navigate to="/" replace />;

    return <Outlet />;
}
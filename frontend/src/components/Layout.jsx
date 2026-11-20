import { Outlet } from "react-router-dom";
import Navbar from "./Navbar";
import { useAuth } from "../context/AuthContext";

export default function Layout() {
    const { user, isAdmin, logout } = useAuth();
    return (
        <>
            <Navbar user={user} isAdmin={isAdmin} onLogout={logout} />
            <main className="container">
                <Outlet />
            </main>
        </>
    );
}
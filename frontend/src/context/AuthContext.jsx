import { createContext, useContext, useEffect, useState, useCallback } from "react";
import * as authService from "../api/authService";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const logout = useCallback(() => {
        localStorage.removeItem("token");
        setUser(null);
    }, []);

    const loadUser = useCallback(async () => {
        if (!localStorage.getItem("token")) {
            setLoading(false);
            return;
        }
        try {
            const { data } = await authService.getMe();
            setUser(data);
        } catch {
            logout();
        } finally {
            setLoading(false);
        }
    }, [logout]);

    useEffect(() => {
        loadUser();
        window.addEventListener("auth:logout", logout);
        return () => window.removeEventListener("auth:logout", logout);
    }, [loadUser, logout]);

    const login = async (credentials) => {
        const { data } = await authService.login(credentials);
        localStorage.setItem("token", data.jwt || data.token);
        await loadUser();
    };

    const isAdmin = user?.role === "ADMIN" || user?.role === "ADMINISTRATOR";

    return (
        <AuthContext.Provider value={{ user, loading, login, logout, isAdmin }}>
            {children}
        </AuthContext.Provider>
    );
}

export const useAuth = () => useContext(AuthContext);

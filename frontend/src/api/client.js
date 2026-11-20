import axios from "axios";

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
});

// Adjunta el token a cada petición
api.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");
    if (token) config.headers.Authorization = `Bearer ${token}`;
    return config;
});

// Si el token expiró o es inválido, cerramos sesión
api.interceptors.response.use(
    (res) => res,
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem("token");
            window.dispatchEvent(new Event("auth:logout"));
        }
        return Promise.reject(error);
    }
);

export default api;
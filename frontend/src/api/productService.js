import api from "./client";

export const getProducts = (page = 0, size = 20, categoryId = null, search = "") => {
    const params = { page, size, sort: "id,asc" };
    if (categoryId != null) params.categoryId = categoryId;
    if (search.trim()) params.search = search.trim();
    return api.get("/products", { params });
};
export const getProduct = (id) => api.get(`/products/${id}`);
export const createProduct = (data) => api.post("/products", data);
export const updateProduct = (id, data) => api.put(`/products/${id}`, data);
export const deleteProduct = (id) => api.delete(`/products/${id}`);

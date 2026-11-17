import api from "./client";

export const getCart = () => api.get("/cart");
export const addCartItem = (userId, productId, quantity = 1) =>
    api.post(`/cart/add/${userId}`, { productId, quantity });
export const updateCartItem = (cartItemId, quantity) =>
    api.put(`/cart/item/${cartItemId}?quantity=${quantity}`);
export const removeCartItem = (cartItemId) =>
    api.delete(`/cart/item/${cartItemId}`);
export const clearCart = () =>
    api.delete("/cart/clear");

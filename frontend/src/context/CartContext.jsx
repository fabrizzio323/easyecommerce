import { createContext, useContext, useEffect, useState, useCallback } from "react";
import * as cartService from "../api/cartService";
import { normalizeCart } from "../utils/normalize";
import { useAuth } from "./AuthContext";

const CartContext = createContext(null);
const EMPTY = { items: [], total: 0 };

export function CartProvider({ children }) {
    const { user } = useAuth();
    const [cart, setCart] = useState(EMPTY);
    const [loading, setLoading] = useState(false);

    const refresh = useCallback(async () => {
        if (!user) {
            setCart(EMPTY);
            return;
        }
        setLoading(true);
        try {
            const { data } = await cartService.getCart();
            setCart(normalizeCart(data));
        } catch {
            setCart(EMPTY);
        } finally {
            setLoading(false);
        }
    }, [user]);

    useEffect(() => {
        refresh();
    }, [refresh]);

    const addItem = async (productId, quantity = 1) => {
        if (!user?.id) {
            throw new Error("Debes iniciar sesión para agregar productos al carrito.");
        }
        await cartService.addCartItem(user.id, productId, quantity);
        await refresh();
    };

    const updateItem = async (itemId, quantity) => {
        await cartService.updateCartItem(itemId, quantity);
        await refresh();
    };

    const removeItem = async (itemId) => {
        await cartService.removeCartItem(itemId);
        await refresh();
    };

    const clear = async () => {
        await cartService.clearCart();
        await refresh();
    };

    const count = cart.items.reduce((sum, i) => sum + i.quantity, 0);

    return (
        <CartContext.Provider
            value={{ cart, count, loading, refresh, addItem, updateItem, removeItem, clear }}
        >
            {children}
        </CartContext.Provider>
    );
}

export const useCart = () => useContext(CartContext);

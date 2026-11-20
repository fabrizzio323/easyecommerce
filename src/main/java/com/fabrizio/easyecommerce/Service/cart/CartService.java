package com.fabrizio.easyecommerce.Service.cart;

import com.fabrizio.easyecommerce.dto.cart.CartItemRequestDTO;
import com.fabrizio.easyecommerce.dto.cart.CartResponseDTO;

public interface CartService {
        CartResponseDTO getMyCart();
        void createCartForUser(Long userId);
        void addItemToCart(Long userId, CartItemRequestDTO request);
        void updateCartItemQuantity(Long cartItemId, int quantity);
        void removeCartItem(Long cartItemId);
        void clearCart(Long userId);
        void clearCartByEmail(String email);
    }



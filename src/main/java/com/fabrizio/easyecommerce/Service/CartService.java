package com.fabrizio.easyecommerce.Service;

import com.fabrizio.easyecommerce.dto.CartItemRequestDTO;
import com.fabrizio.easyecommerce.dto.CartRequestDTO;
import com.fabrizio.easyecommerce.dto.CartResponseDTO;

public interface CartService {
        CartResponseDTO getMyCart();
        void createCartForUser(Long userId);
        void addItemToCart(Long userId, CartItemRequestDTO request);
        void updateCartItemQuantity(Long cartItemId, int quantity);
        void removeCartItem(Long cartItemId);
        void clearCart(Long userId);
    }



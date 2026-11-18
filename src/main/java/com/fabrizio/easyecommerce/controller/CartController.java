
package com.fabrizio.easyecommerce.controller;

import com.fabrizio.easyecommerce.Service.cart.CartService;
import com.fabrizio.easyecommerce.dto.cart.CartItemRequestDTO;
import com.fabrizio.easyecommerce.dto.cart.CartResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponseDTO> getMyCart() {
        return ResponseEntity.ok(cartService.getMyCart());
    }

    @PostMapping("/create/{userId}")
    public ResponseEntity<Void> createCart(@PathVariable Long userId) {
        cartService.createCartForUser(userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/add/{userId}")
    public ResponseEntity<Void> addItemToCart(
            @PathVariable Long userId,
            @RequestBody CartItemRequestDTO request) {

        cartService.addItemToCart(userId, request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<Void> updateCartItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam int quantity) {

        cartService.updateCartItemQuantity(cartItemId, quantity);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(
            @PathVariable Long cartItemId) {

        cartService.removeCartItem(cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart() {
        String currentEmail = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        cartService.clearCartByEmail(currentEmail);

        return ResponseEntity.noContent().build();
    }
}


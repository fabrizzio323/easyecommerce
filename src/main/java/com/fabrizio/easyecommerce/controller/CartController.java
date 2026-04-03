package com.fabrizio.easyecommerce.controller;

import com.fabrizio.easyecommerce.Service.CartService;
import com.fabrizio.easyecommerce.dto.CartItemRequestDTO;
import com.fabrizio.easyecommerce.dto.CartRequestDTO;
import com.fabrizio.easyecommerce.dto.CartResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@Tag(
        name = "Cart Resource",
        description = """
    Endpoints for managing shopping carts in the e-commerce system.

    🔐 Authentication required:
    Valid JWT token is mandatory for all operations.

    🛒 Cart Logic:
    - Each user has one unique cart.
    - Prices are captured at the moment an item is added.
    - Automatic cart creation is supported during retrieval.

    ⚠️ Permissions:
    - READ_ONE_CART → view personal cart
    - SAVE_ONE_CART → initialize a cart for a specific user
    - ADD_ITEM_CART → add products to cart
    - UPDATE_QUANTITY_ITEM_CART → modify item quantities
    - DELETE_ITEM_CART → remove specific items
    - CLEAR_CART → empty the entire cart
    """
)
public class CartController {

    @Autowired
    private CartService cartService;

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Operation(
            summary = "Get current user's cart",
            description = "Retrieves the cart associated with the authenticated user from the JWT context.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Missing READ_ONE_CART permission"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasAuthority('READ_ONE_CART')")
    @GetMapping
    public ResponseEntity<CartResponseDTO> getMyCart() {
        logger.info("REST request to get current user's cart");
        return ResponseEntity.status(HttpStatus.OK).body(cartService.getMyCart());
    }

    @Operation(
            summary = "Create cart for specific user",
            description = "Manually initializes a cart for a user ID. Usually used during registration.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cart created successfully"),
            @ApiResponse(responseCode = "400", description = "User already has a cart"),
            @ApiResponse(responseCode = "403", description = "Missing SAVE_ONE_CART permission")
    })
    @PreAuthorize("hasAuthority('SAVE_ONE_CART')")
    @PostMapping("/create/{userId}")
    public ResponseEntity<String> createCartForUser(@PathVariable Long userId, @Valid @RequestBody CartRequestDTO request) {
        logger.info("REST request to create cart for user ID: {}", userId);
        cartService.createCartForUser(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cart created successfully for user with id " + userId);
    }

    @Operation(
            summary = "Add item to cart",
            description = "Adds a product to a user's cart. If the product exists, it increments the quantity.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item added successfully"),
            @ApiResponse(responseCode = "404", description = "Cart or Product not found"),
            @ApiResponse(responseCode = "403", description = "Missing ADD_ITEM_CART permission")
    })
    @PreAuthorize("hasAuthority('ADD_ITEM_CART')")
    @PostMapping("/add/{userId}")
    public ResponseEntity<String> addItemToCart(@PathVariable Long userId, @Valid @RequestBody CartItemRequestDTO request) {
        logger.info("REST request to add product ID: {} to user ID: {}", request.getProductId(), userId);
        cartService.addItemToCart(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body("Item added to cart successfully");
    }

    @Operation(
            summary = "Update item quantity",
            description = "Updates the quantity of a specific CartItem by its ID. Quantity must be > 0.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Quantity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity value"),
            @ApiResponse(responseCode = "404", description = "CartItem not found")
    })
    @PreAuthorize("hasAuthority('UPDATE_QUANTITY_ITEM_CART')")
    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<String> updateQuantity(@PathVariable Long cartItemId, @RequestParam int quantity) {
        logger.info("REST request to update quantity for CartItem ID: {} to {}", cartItemId, quantity);
        cartService.updateCartItemQuantity(cartItemId, quantity);
        return ResponseEntity.status(HttpStatus.OK).body("Cart item quantity updated successfully");
    }

    @Operation(
            summary = "Remove item from cart",
            description = "Deletes a specific item from the cart using the CartItem ID.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item removed successfully"),
            @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @PreAuthorize("hasAuthority('DELETE_ITEM_CART')")
    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long cartItemId) {
        logger.info("REST request to remove CartItem ID: {}", cartItemId);
        cartService.removeCartItem(cartItemId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Clear entire cart",
            description = "Removes all items from a specific user's cart.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cart cleared successfully"),
            @ApiResponse(responseCode = "404", description = "Cart not found")
    })
    @PreAuthorize("hasAuthority('CLEAR_CART')")
    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<String> clearCart(@PathVariable Long userId) {
        logger.info("REST request to clear cart for user ID: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.status(HttpStatus.OK).body("Cart cleared successfully");
    }
}
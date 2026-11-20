package com.fabrizio.easyecommerce.controller;

import lombok.RequiredArgsConstructor;

import com.fabrizio.easyecommerce.Service.order.OrderService;
import com.fabrizio.easyecommerce.dto.order.OrderResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Resource", description = """
                Endpoints for processing checkouts and managing customer orders.

                🚀 **Workflow**:
                - **Checkout**: Converts the current Cart into a finalized Order and **captures the user's shipping address**.
                - **Stock**: Automatically deducted during checkout.
                - **Shipping Details**: Every order stores a snapshot of the address (Street, City, ZipCode) to ensure delivery consistency.
                """)
@RequiredArgsConstructor
public class OrderController {

        private final OrderService orderService;

        private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

        @Operation(summary = "Perform Checkout for authenticated user", description = "Converts the authenticated user's cart into an order, captures shipping snapshot and clears cart.", security = @SecurityRequirement(name = "bearerAuth"))
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Order created successfully"),
                        @ApiResponse(responseCode = "400", description = "Insufficient stock or empty cart"),
                        @ApiResponse(responseCode = "404", description = "Cart or User not found")
        })
        @PreAuthorize("hasAuthority('PERFORM_CHECKOUT')")
        @PostMapping("/checkout")
        public ResponseEntity<OrderResponseDTO> checkout(Authentication authentication) {
                String email = authentication.getName();
                logger.info("REST request to perform checkout for authenticated user: {}", email);
                OrderResponseDTO response = orderService.performCheckoutByEmail(email);
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        @Operation(summary = "Get my orders with shipping info", description = "Retrieves the order history for the authenticated user, including where each order was shipped.", security = @SecurityRequirement(name = "bearerAuth"))
        @PreAuthorize("hasAuthority('READ_MY_ORDERS')")
        @GetMapping("/my-orders")
        public ResponseEntity<List<OrderResponseDTO>> getMyOrders(Authentication authentication) {
                String email = authentication.getName();
                logger.info("REST request to get order history for user: {}", email);
                return ResponseEntity.ok(orderService.getMyOrders(email));
        }

        @Operation(summary = "Update order status", description = "Updates the status of an order (e.g., PAID, SHIPPED, DELIVERED). ADMIN only.", security = @SecurityRequirement(name = "bearerAuth"))
        @PreAuthorize("hasAuthority('UPDATE_ORDER_STATUS')")
        @PutMapping("/{orderId}/status")
        public ResponseEntity<Void> updateStatus(@PathVariable Long orderId, @RequestParam String status) {
                logger.info("REST request to update status for order {} to {}", orderId, status);
                orderService.updateOrderStatus(orderId, status);
                return ResponseEntity.ok().build();
        }

        @Operation(summary = "Get all orders with customer addresses (Admin)", description = "Retrieves all orders in the system. Essential for logistics and store management to know delivery destinations.", security = @SecurityRequirement(name = "bearerAuth"))

        @PreAuthorize("hasAuthority('READ_ALL_ORDERS')")
        @GetMapping("/admin/all")
        public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(
                        @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

                logger.info(
                                "REST request to get orders - page: {}, size: {}",
                                pageable.getPageNumber(),
                                pageable.getPageSize());

                return ResponseEntity.ok(
                                orderService.getAllOrders(pageable));
        }

}

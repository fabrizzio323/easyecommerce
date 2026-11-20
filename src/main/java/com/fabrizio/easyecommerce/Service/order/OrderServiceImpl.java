
package com.fabrizio.easyecommerce.Service.order;

import com.fabrizio.easyecommerce.Service.cart.CartService;
import com.fabrizio.easyecommerce.dto.order.OrderResponseDTO;
import com.fabrizio.easyecommerce.entity.Cart;
import com.fabrizio.easyecommerce.entity.Order;
import com.fabrizio.easyecommerce.entity.OrderItem;
import com.fabrizio.easyecommerce.entity.Product;
import com.fabrizio.easyecommerce.enums.OrderStatus;
import com.fabrizio.easyecommerce.exception.Cart.CartNotFoundException;
import com.fabrizio.easyecommerce.exception.Order.OrderNotFoundException;
import com.fabrizio.easyecommerce.exception.Products.InsufficientStockException;
import com.fabrizio.easyecommerce.mapper.Order.OrderMapper;
import com.fabrizio.easyecommerce.repository.Cart.CartRepository;
import com.fabrizio.easyecommerce.repository.order.OrderRepository;
import com.fabrizio.easyecommerce.repository.product.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

        private final OrderRepository orderRepository;
        private final CartRepository cartRepository;
        private final ProductRepository productRepository;
        private final OrderMapper orderMapper;
        private final CartService cartService;

        @Override
        @Transactional
        public OrderResponseDTO performCheckout(Long userId) {
                log.info("Performing checkout for user with id: {}", userId);

                Cart cart = cartRepository.findByUserId(userId)
                                .orElseThrow(() -> new CartNotFoundException(
                                                "Cart not found for user with id: " + userId));

                if (cart.getItems().isEmpty()) {
                        log.warn("Cart is empty for user with id: {}", userId);
                        throw new CartNotFoundException(
                                        "Cart is empty for user with id: " + userId);
                }

                Order order = new Order();
                order.setUser(cart.getUser());
                order.setCity(cart.getUser().getCity());
                order.setShippingAddress(cart.getUser().getStreet());
                order.setZipCode(cart.getUser().getZipCode());
                order.setStatus(OrderStatus.CREATED);

                List<OrderItem> orderItems = cart.getItems().stream()
                                .map(cartItem -> {
                                        Product product = cartItem.getProduct();

                                        if (product.getStock() < cartItem.getQuantity()) {
                                                throw new InsufficientStockException(
                                                                "Sin stock para: " + product.getName());
                                        }

                                        product.setStock(
                                                        product.getStock() - cartItem.getQuantity());

                                        productRepository.save(product);

                                        OrderItem oi = new OrderItem();
                                        oi.setOrder(order);
                                        oi.setProduct(product);
                                        oi.setQuantity(cartItem.getQuantity());
                                        oi.setPrice(product.getPrice());

                                        return oi;
                                })
                                .collect(Collectors.toList());

                BigDecimal totalOrder = orderItems.stream()
                                .map(item -> item.getPrice().multiply(
                                                new BigDecimal(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                order.setItems(orderItems);
                order.setTotalAmount(totalOrder);

                Order savedOrder = orderRepository.save(order);

                cartService.clearCart(userId);

                log.info(
                                "Checkout completed for user with id: {}. Order id: {}",
                                userId,
                                savedOrder.getId());

                return orderMapper.toOrderResponseDTO(savedOrder);
        }

        @Override
        @Transactional
        public OrderResponseDTO performCheckoutByEmail(String email) {
                log.info(
                                "Performing checkout for authenticated user with email: {}",
                                email);

                Cart cart = cartRepository.findByUserEmail(email)
                                .orElseThrow(() -> new CartNotFoundException(
                                                "Cart not found for user: " + email));

                if (cart.getItems().isEmpty()) {
                        log.warn("Cart is empty for user: {}", email);
                        throw new CartNotFoundException(
                                        "Cart is empty for user: " + email);
                }

                Order order = new Order();
                order.setUser(cart.getUser());
                order.setCity(cart.getUser().getCity());
                order.setShippingAddress(cart.getUser().getStreet());
                order.setZipCode(cart.getUser().getZipCode());
                order.setStatus(OrderStatus.CREATED);

                List<OrderItem> orderItems = cart.getItems().stream()
                                .map(cartItem -> {
                                        Product product = cartItem.getProduct();

                                        if (product.getStock() < cartItem.getQuantity()) {
                                                throw new InsufficientStockException(
                                                                "Sin stock para: " + product.getName());
                                        }

                                        product.setStock(
                                                        product.getStock() - cartItem.getQuantity());

                                        productRepository.save(product);

                                        OrderItem oi = new OrderItem();
                                        oi.setOrder(order);
                                        oi.setProduct(product);
                                        oi.setQuantity(cartItem.getQuantity());
                                        oi.setPrice(product.getPrice());

                                        return oi;
                                })
                                .collect(Collectors.toList());

                BigDecimal totalOrder = orderItems.stream()
                                .map(item -> item.getPrice().multiply(
                                                new BigDecimal(item.getQuantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                order.setItems(orderItems);
                order.setTotalAmount(totalOrder);

                Order savedOrder = orderRepository.save(order);

                cartService.clearCartByEmail(email);

                log.info(
                                "Checkout completed for user: {}. Order id: {}",
                                email,
                                savedOrder.getId());

                return orderMapper.toOrderResponseDTO(savedOrder);
        }

        @Override
        @Transactional(readOnly = true)
        public List<OrderResponseDTO> getMyOrders(String email) {
                log.info("Fetching orders for user with email: {}", email);

                return orderRepository.findByUserEmail(email).stream()
                                .map(orderMapper::toOrderResponseDTO)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public OrderResponseDTO getOrderById(Long orderId) {
                log.info("Fetching order with id: {}", orderId);

                return orderRepository.findById(orderId)
                                .map(orderMapper::toOrderResponseDTO)
                                .orElseThrow(() -> new OrderNotFoundException(
                                                "Order not found with id: " + orderId));
        }

        // ADMINISTRADOR: OBTENER PEDIDOS PAGINADOS

        @Override
        @Transactional(readOnly = true)
        public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
                log.info(
                                "Fetching orders as ADMINISTRATOR, page: {}, size: {}",
                                pageable.getPageNumber(),
                                pageable.getPageSize());

                return orderRepository.findAll(pageable)
                                .map(orderMapper::toOrderResponseDTO);
        }

        @Override
        @Transactional
        public void updateOrderStatus(Long orderId, String status) {
                log.info(
                                "Updating order status for order with id: {} to status: {}",
                                orderId,
                                status);

                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new OrderNotFoundException(
                                                "Order not found with id: " + orderId));

                try {
                        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());

                        order.setStatus(newStatus);
                        orderRepository.save(order);

                        log.info(
                                        "Order status updated successfully for order with id: {}",
                                        orderId);

                } catch (IllegalArgumentException e) {
                        log.error("Invalid order status provided: {}", status);
                        throw new IllegalArgumentException(
                                        "Invalid order status: " + status);
                }
        }
}

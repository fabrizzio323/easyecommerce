package com.fabrizio.easyecommerce.Service;


import com.fabrizio.easyecommerce.dto.OrderResponseDTO;
import com.fabrizio.easyecommerce.entity.Cart;
import com.fabrizio.easyecommerce.entity.Order;
import com.fabrizio.easyecommerce.entity.OrderItem;
import com.fabrizio.easyecommerce.entity.Product;
import com.fabrizio.easyecommerce.enums.OrderStatus;
import com.fabrizio.easyecommerce.exception.Cart.CartNotFoundException;
import com.fabrizio.easyecommerce.exception.Order.OrderNotFoundException;
import com.fabrizio.easyecommerce.exception.Products.InsufficientStockException;
import com.fabrizio.easyecommerce.mapper.OrderMapper;
import com.fabrizio.easyecommerce.repository.CartRepository;
import com.fabrizio.easyecommerce.repository.OrderRepository;
import com.fabrizio.easyecommerce.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private CartService cartService;

    @Override
    @Transactional
    public OrderResponseDTO performCheckout(Long userId) {
        log.info("Performing checkout for user with id: {}", userId);
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new CartNotFoundException("Cart not found for user with id: " + userId));
        if(cart.getItems().isEmpty()){
            log.warn("Cart is empty for user with id: {}", userId);
            throw new CartNotFoundException("Cart is empty for user with id: " + userId);
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
                        throw new InsufficientStockException("Sin stock para: " + product.getName());
                    }

                    product.setStock(product.getStock() - cartItem.getQuantity());
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
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setItems(orderItems);
        order.setTotalAmount(totalOrder);

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart(userId);

        log.info("Checkout completed for user with id: {}. Order id: {}", userId, savedOrder.getId());

        return orderMapper.toOrderResponseDTO(order);
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
    @Transactional
    public OrderResponseDTO getOrderById(Long orderId) {
        log.info("Fetching order with id: {}", orderId);

        return orderRepository.findById(orderId).map(orderMapper::toOrderResponseDTO)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
    }

    @Override
    @Transactional(readOnly = true)

    public List<OrderResponseDTO> getAllOrders() {
        log.info("Fetching all orders as ADMINISTRATOR");

        return orderRepository.findAll().stream()
                .map(orderMapper::toOrderResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
       log.info("Updating order status for order with id: {} to status: {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
            orderRepository.save(order);
            log.info("Order status updated successfully for order with id: {}", orderId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid order status provided: {}", status);
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }

}

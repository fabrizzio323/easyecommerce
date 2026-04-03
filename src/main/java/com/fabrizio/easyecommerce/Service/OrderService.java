package com.fabrizio.easyecommerce.Service;

import com.fabrizio.easyecommerce.dto.OrderResponseDTO;

import java.util.List;

public interface OrderService {

    OrderResponseDTO performCheckout(Long userId);

    List<OrderResponseDTO> getMyOrders(String email);

    OrderResponseDTO getOrderById(Long orderId);

    List<OrderResponseDTO> getAllOrders();

    void updateOrderStatus(Long orderId, String status);
}

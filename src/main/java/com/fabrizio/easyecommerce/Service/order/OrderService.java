package com.fabrizio.easyecommerce.Service.order;

import com.fabrizio.easyecommerce.dto.order.OrderResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface OrderService {

    OrderResponseDTO performCheckout(Long userId);

    Page<OrderResponseDTO> getAllOrders(Pageable pageable);

    OrderResponseDTO performCheckoutByEmail(String email);

    List<OrderResponseDTO> getMyOrders(String email);

    OrderResponseDTO getOrderById(Long orderId);

    void updateOrderStatus(Long orderId, String status);
}

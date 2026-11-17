
package com.fabrizio.easyecommerce.controller;

import com.fabrizio.easyecommerce.Service.order.OrderService;
import com.fabrizio.easyecommerce.dto.order.OrderResponseDTO;
import com.fabrizio.easyecommerce.enums.OrderStatus;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    private OrderResponseDTO mockOrderResponse;

    @BeforeEach
    void setUp() {
        mockOrderResponse = new OrderResponseDTO();
        mockOrderResponse.setId(1L);
        mockOrderResponse.setTotalAmount(BigDecimal.valueOf(1500.0));
        mockOrderResponse.setStatus(OrderStatus.CREATED);
        mockOrderResponse.setShippingAddress("Belgrano 450");
        mockOrderResponse.setCity("San Salvador de Jujuy");
        mockOrderResponse.setZipCode("4600");
        mockOrderResponse.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(authorities = "PERFORM_CHECKOUT")
    void shouldPerformCheckoutSuccessfully() throws Exception {

        Mockito.when(
                orderService.performCheckoutByEmail(Mockito.anyString())).thenReturn(mockOrderResponse);

        mockMvc.perform(post("/api/orders/checkout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingAddress")
                        .value("Belgrano 450"))
                .andExpect(jsonPath("$.city")
                        .value("San Salvador de Jujuy"))
                .andExpect(jsonPath("$.status")
                        .value("CREATED"));
    }

    @Test
    @WithMockUser(authorities = "READ_MY_ORDERS")
    void shouldGetMyOrders() throws Exception {

        Mockito.when(
                orderService.getMyOrders(Mockito.anyString())).thenReturn(List.of(mockOrderResponse));

        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shippingAddress")
                        .value("Belgrano 450"))
                .andExpect(jsonPath("$[0].zipCode")
                        .value("4600"));
    }

    @Test
    @WithMockUser(authorities = "READ_ALL_ORDERS")
    void shouldGetAllOrdersForAdmin() throws Exception {

        // Simular una página con una orden
        Page<OrderResponseDTO> mockPage = new PageImpl<>(
                List.of(mockOrderResponse),
                PageRequest.of(0, 10),
                1);

        // Mock del servicio con Pageable
        Mockito.when(
                orderService.getAllOrders(Mockito.any(Pageable.class))).thenReturn(mockPage);

        // Verificar el endpoint paginado
        mockMvc.perform(get("/api/orders/admin/all")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id")
                        .value(1))
                .andExpect(jsonPath("$.content[0].shippingAddress")
                        .value("Belgrano 450"))
                .andExpect(jsonPath("$.totalElements")
                        .value(1))
                .andExpect(jsonPath("$.totalPages")
                        .value(1))
                .andExpect(jsonPath("$.number")
                        .value(0));
    }
}

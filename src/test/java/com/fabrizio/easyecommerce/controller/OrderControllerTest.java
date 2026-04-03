package com.fabrizio.easyecommerce.controller;

import com.fabrizio.easyecommerce.Service.OrderService;
import com.fabrizio.easyecommerce.dto.OrderResponseDTO;
import com.fabrizio.easyecommerce.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;

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
        // Configuramos una respuesta de orden con los nuevos campos de dirección
        mockOrderResponse = new OrderResponseDTO();
        mockOrderResponse.setId(1L);
        mockOrderResponse.setTotalAmount(BigDecimal.valueOf(1500.0));
        mockOrderResponse.setStatus(OrderStatus.valueOf("CREATED"));
        mockOrderResponse.setShippingAddress("Belgrano 450");
        mockOrderResponse.setCity("San Salvador de Jujuy");
        mockOrderResponse.setZipCode("4600");
        mockOrderResponse.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(authorities = "PERFORM_CHECKOUT")
    void shouldPerformCheckoutSuccessfully() throws Exception {
        // Simulamos que el servicio devuelve nuestra orden mockeada
        Mockito.when(orderService.performCheckout(anyLong())).thenReturn(mockOrderResponse);

        mockMvc.perform(post("/api/orders/checkout/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingAddress").value("Belgrano 450"))
                .andExpect(jsonPath("$.city").value("San Salvador de Jujuy"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @WithMockUser(authorities = "READ_MY_ORDERS")
    void shouldGetMyOrders() throws Exception {
        Mockito.when(orderService.getMyOrders(Mockito.anyString())).thenReturn(List.of(mockOrderResponse));

        mockMvc.perform(get("/api/orders/my-orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].shippingAddress").value("Belgrano 450"))
                .andExpect(jsonPath("$[0].zipCode").value("4600"));
    }

    @Test
    @WithMockUser(authorities = "READ_ALL_ORDERS") // Rol de ADMIN
    void shouldGetAllOrdersForAdmin() throws Exception {
        Mockito.when(orderService.getAllOrders()).thenReturn(List.of(mockOrderResponse));

        mockMvc.perform(get("/api/orders/admin/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists());
    }
}
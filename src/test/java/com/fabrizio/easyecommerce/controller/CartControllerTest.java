package com.fabrizio.easyecommerce.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 🔐 LOGIN reutilizable
    private String getAdminToken() throws Exception {
        String login = """
                {
                  "email": "admin@test.com",
                  "password": "admin123"
                }
                """;
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("jwt").asText();
    }

    private String getUserToken() throws Exception {
        String login = """
                {
                  "email": "user@test.com",
                  "password": "user123"
                }
                """;
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("jwt").asText();
    }

    // 🟢 GET MY CART
    @Test
    void shouldGetMyCart() throws Exception {
        String token = getUserToken();

        mockMvc.perform(get("/api/cart")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user@test.com"))
                .andExpect(jsonPath("$.items").isArray());
    }

    // 🟢 ADD ITEM TO CART (User con ID 2 según tu import.sql)
    @Test
    void shouldAddItemToCart() throws Exception {
        String token = getUserToken();
        String request = """
                {
                  "productId": 1,
                  "quantity": 2
                }
                """;

        mockMvc.perform(post("/api/cart/add/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string("Item added to cart successfully"));
    }

    // 🟢 UPDATE ITEM QUANTITY (Usa el cart_item ID 1 del import.sql)
    @Test
    void shouldUpdateItemQuantity() throws Exception {
        String token = getUserToken();

        mockMvc.perform(put("/api/cart/item/1")
                        .header("Authorization", "Bearer " + token)
                        .param("quantity", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cart item quantity updated successfully"));
    }

    // 🟢 REMOVE ITEM FROM CART
    @Test
    void shouldRemoveItemFromCart() throws Exception {
        String token = getUserToken();

        mockMvc.perform(delete("/api/cart/item/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // 🟢 CLEAR CART
    @Test
    void shouldClearCart() throws Exception {
        String token = getUserToken();

        mockMvc.perform(delete("/api/cart/clear/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string("Cart cleared successfully"));
    }

    // 🔴 VALIDATION ERRORS

    @Test
    void shouldFailWhenAddQuantityIsZero() throws Exception {
        String token = getUserToken();
        String request = """
                {
                  "productId": 1,
                  "quantity": 0
                }
                """;

        mockMvc.perform(post("/api/cart/add/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailWhenProductNotFound() throws Exception {
        String token = getUserToken();
        String request = """
                {
                  "productId": 999,
                  "quantity": 1
                }
                """;

        mockMvc.perform(post("/api/cart/add/2")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    // 🔴 PERMISSION ERRORS

    @Test
    void shouldFailWhenUserLacksPermissionToAdd() throws Exception {
        // Suponiendo que creamos un token de un usuario sin permisos específicos
        // Para este ejemplo, simplemente no enviamos token o usamos uno inválido
        mockMvc.perform(post("/api/cart/add/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    // 🔴 BUSINESS LOGIC ERRORS

    @Test
    void shouldFailWhenUpdatingNonExistentCartItem() throws Exception {
        String token = getUserToken();

        mockMvc.perform(put("/api/cart/item/9999")
                        .header("Authorization", "Bearer " + token)
                        .param("quantity", "5"))
                .andExpect(status().isNotFound());
    }
}
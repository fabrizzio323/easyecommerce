package com.fabrizio.easyecommerce.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 🔐 LOGIN reutilizable (usa admin del import.sql)
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
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("jwt").asText();
    }

    // 🔐 LOGIN usuario regular
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
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("jwt").asText();
    }

    // 🟢 GET ALL PRODUCTS (usa import.sql → siempre hay 12 productos)
    @Test
    void shouldGetAllProducts() throws Exception {

        String token = getUserToken();

        mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12));
    }

    // 🟢 GET PRODUCT BY ID (usa producto existente del import.sql - ID 2 para evitar conflictos)
    @Test
    void shouldGetProductById() throws Exception {

        String token = getUserToken();

        mockMvc.perform(get("/api/products/2")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.name").value("Notebook Pro 14"));
    }

    // 🟢 CREATE PRODUCT (ADMIN only)
    @Test
    void shouldCreateProduct() throws Exception {

        String token = getAdminToken();

        String request = """
                {
                  "name": "Test Product",
                  "description": "Test product description",
                  "price": 999.99,
                  "stock": 10,
                  "idCategory": 1
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(content().string("Product created successfully"));
    }

    // 🟢 UPDATE PRODUCT (ADMIN only - usa producto 3 para evitar conflictos)
    @Test
    void shouldUpdateProduct() throws Exception {

        String token = getAdminToken();

        String request = """
                {
                  "name": "Auriculares Sony Updated",
                  "description": "Updated description",
                  "price": 100000.00,
                  "stock": 30,
                  "idCategory": 1
                }
                """;

        mockMvc.perform(put("/api/products/3")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(content().string("Product updated successfully"));
    }

    // 🟢 DELETE PRODUCT (ADMIN only - soft delete)
    @Test
    void shouldDeleteProduct() throws Exception {

        String token = getAdminToken();

        mockMvc.perform(delete("/api/products/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    // 🔴 VALIDATION ERRORS

    // Nombre vacío
    @Test
    void shouldFailWhenNameIsBlank() throws Exception {

        String token = getAdminToken();

        String request = """
                {
                  "name": "",
                  "description": "Test",
                  "price": 100.00,
                  "stock": 5,
                  "idCategory": 1
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    // Precio negativo
    @Test
    void shouldFailWhenPriceIsNegative() throws Exception {

        String token = getAdminToken();

        String request = """
                {
                  "name": "Test Product",
                  "description": "Test",
                  "price": -100.00,
                  "stock": 5,
                  "idCategory": 1
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    // Stock negativo
    @Test
    void shouldFailWhenStockIsNegative() throws Exception {

        String token = getAdminToken();

        String request = """
                {
                  "name": "Test Product",
                  "description": "Test",
                  "price": 100.00,
                  "stock": -1,
                  "idCategory": 1
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }

    // Categoría inexistente
    @Test
    void shouldFailWhenCategoryNotExists() throws Exception {

        String token = getAdminToken();

        String request = """
                {
                  "name": "Test Product",
                  "description": "Test",
                  "price": 100.00,
                  "stock": 5,
                  "idCategory": 999
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    // 🔴 PERMISSION ERRORS

    // Usuario regular intenta crear producto
    @Test
    void shouldFailWhenUserTriesToCreateProduct() throws Exception {

        String token = getUserToken();

        String request = """
                {
                  "name": "Test Product",
                  "description": "Test",
                  "price": 100.00,
                  "stock": 5,
                  "idCategory": 1
                }
                """;

        mockMvc.perform(post("/api/products")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }

    // Usuario regular intenta actualizar producto
    @Test
    void shouldFailWhenUserTriesToUpdateProduct() throws Exception {

        String token = getUserToken();

        String request = """
                {
                  "name": "Updated Product",
                  "description": "Updated",
                  "price": 200.00,
                  "stock": 10,
                  "idCategory": 1
                }
                """;

        mockMvc.perform(put("/api/products/1")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }

    // Usuario regular intenta eliminar producto
    @Test
    void shouldFailWhenUserTriesToDeleteProduct() throws Exception {

        String token = getUserToken();

        mockMvc.perform(delete("/api/products/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // 🔴 AUTHENTICATION ERRORS

    // Sin token
    @Test
    void shouldFailWithoutToken() throws Exception {

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isForbidden());
    }

    // Producto inexistente
    @Test
    void shouldFailWhenProductNotFound() throws Exception {

        String token = getUserToken();

        mockMvc.perform(get("/api/products/999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}

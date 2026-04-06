package com.fabrizio.easyecommerce.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // 🔐 LOGIN reutilizable
    private String getToken() throws Exception {

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

    // 🟢 GET (usa import.sql → siempre hay 6)
    @Test
    void shouldGetAllCategories() throws Exception {

        String token = getToken();

        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    // 🟢 CREATE (nombre único para no romper UNIQUE)
    @Test
    void shouldCreateCategory() throws Exception {

        String token = getToken();

        String uniqueName = "Cat_" + System.currentTimeMillis();

        String request = """
                {
                  "name": "%s",
                  "description": "Test category"
                }
                """.formatted(uniqueName);

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated());
    }

    // 🟢 UPDATE (usa categoría existente del import.sql)
    @Test
    void shouldUpdateCategory() throws Exception {

        String token = getToken();

        String request = """
                {
                  "name": "Electronics",
                  "description": "Actualizada"
                }
                """;

        mockMvc.perform(put("/api/categories/Electronics")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());
    }

    // 🔴 VALIDACIÓN (esto te suma puntos en entrevistas)
    @Test
    void shouldFailWhenNameIsBlank() throws Exception {

        String token = getToken();

        String request = """
                {
                  "name": "",
                  "description": "Test"
                }
                """;

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    // 🔴 SIN TOKEN → 403
    @Test
    void shouldFailWithoutToken() throws Exception {

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isForbidden());
    }
}
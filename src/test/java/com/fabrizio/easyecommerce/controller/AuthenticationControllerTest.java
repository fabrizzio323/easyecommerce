package com.fabrizio.easyecommerce.controller;

import com.fabrizio.easyecommerce.dto.AuthenticationRequest;
import com.fabrizio.easyecommerce.dto.RegisterRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String generateEmail() {
        return "fabri" + System.currentTimeMillis() + "@test.com";
    }

    @Test
    void shouldRegisterUser() throws Exception {

        String email = generateEmail();

        RegisterRequest register = new RegisterRequest();
        register.setName("Fabri");
        register.setEmail(email);
        register.setPassword("123456");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldLoginAndReturnToken() throws Exception {

        String email = generateEmail();

        // primero registrar
        RegisterRequest register = new RegisterRequest();
        register.setName("Fabri");
        register.setEmail(email);
        register.setPassword("123456");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // después login
        AuthenticationRequest login = new AuthenticationRequest();
        login.setEmail(email);
        login.setPassword("123456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").exists());
    }

    @Test
    void shouldAccessAuthMeWithValidToken() throws Exception {

        String email = generateEmail();

        // 1. REGISTER
        RegisterRequest register = new RegisterRequest();
        register.setName("Fabri");
        register.setEmail(email);
        register.setPassword("123456");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());

        // 2. LOGIN
        AuthenticationRequest login = new AuthenticationRequest();
        login.setEmail(email);
        login.setPassword("123456");

        String response = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        String token = jsonNode.get("jwt").asText();

        // 3. AUTH-ME
        mockMvc.perform(get("/auth/auth-me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }
}
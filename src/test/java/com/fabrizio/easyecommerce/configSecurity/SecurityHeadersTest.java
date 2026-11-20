package com.fabrizio.easyecommerce.configSecurity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldIncludeDefensiveSecurityHeadersInResponses() throws Exception {
        mockMvc.perform(get("/actuator/health").secure(true))
                .andExpect(status().isOk())
                .andExpect(header().exists("Strict-Transport-Security"))
                .andExpect(header().string("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none'; object-src 'none';"))
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("Permissions-Policy", "geolocation=(), microphone=(), camera=()"));
    }
}

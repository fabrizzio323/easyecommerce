package com.fabrizio.easyecommerce.configSecurity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ActuatorSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointShouldBePubliclyAccessible() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void actuatorRootShouldBeDeniedForAnonymousUser() throws Exception {
        mockMvc.perform(get("/actuator"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void actuatorRootShouldBeDeniedForStandardUser() throws Exception {
        mockMvc.perform(get("/actuator"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void actuatorRootShouldBeAccessibleForAdmin() throws Exception {
        mockMvc.perform(get("/actuator"))
                .andExpect(status().isOk());
    }
}

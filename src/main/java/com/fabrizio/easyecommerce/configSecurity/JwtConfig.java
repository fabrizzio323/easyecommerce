package com.fabrizio.easyecommerce.configSecurity;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Key;
import java.nio.charset.StandardCharsets;

@Configuration
public class JwtConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);

    @Value("${security.jwt.secret:}")
    private String jwtSecret;

    @Bean
    public Key jwtSigningKey() {
        if (jwtSecret != null && !jwtSecret.isBlank()) {
            byte[] secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
            if (secretBytes.length < 32) {
                throw new IllegalStateException("JWT secret must be at least 32 bytes long for HS256");
            }

            logger.info("JWT signing key loaded from configuration");
            return Keys.hmacShaKeyFor(secretBytes);
        }

        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        logger.info("JWT signing key generated successfully");
        return key;
    }
}


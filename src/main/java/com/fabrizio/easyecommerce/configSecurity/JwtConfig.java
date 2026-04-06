package com.fabrizio.easyecommerce.configSecurity;

import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.Key;
import java.util.Base64;

@Configuration
public class JwtConfig {

    private static final Logger logger = LoggerFactory.getLogger(JwtConfig.class);

    @Bean
    public Key jwtSigningKey() {
        Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        logger.info("JWT signing key generated successfully");
        logger.debug("Generated JWT key (encoded): {}", encodedKey.substring(0, Math.min(20, encodedKey.length())) + "...");
        return key;
    }
}


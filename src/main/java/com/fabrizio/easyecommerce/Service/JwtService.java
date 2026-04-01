package com.fabrizio.easyecommerce.Service;

import com.fabrizio.easyecommerce.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.Key;
import java.util.Date;
import java.util.Map;


@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    @Value("${security.jwt.expiration-minutes}")
    private Long EXPIRATION_MINUTES;

    @Value("${security.jwt.secret-key}")
    private String SECRET_KEY;


    public String generateToken(String email, Map<String, Object> extraClaims){
        logger.debug("Generating JWT token for user: {}", email);
        Date issuedAt = new Date(System.currentTimeMillis());
        Date expiration = new Date(issuedAt.getTime() + (EXPIRATION_MINUTES * 60 * 1000));
        String token = Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(email)
                .setIssuedAt(issuedAt)
                .setExpiration(expiration)
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .signWith(generateKey(), SignatureAlgorithm.HS256)
                .compact();
        logger.info("JWT token generated successfully for user: {}", email);
        return token;
    }



    private Key generateKey(){
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }
    public String extractEmail(String jwt){
        String email = extractAllClaims(jwt).getSubject();
        logger.debug("Extracted email from JWT: {}", email);
        return email;
    }

    public boolean isTokenValid(String jwt, User user){
        try{
            String email = extractEmail(jwt);
            boolean isValid = (email.equals(user.getEmail()) && !isTokenExpired(jwt));
            logger.debug("Token validation for user {}: {}", user.getEmail(), isValid ? "VALID" : "INVALID");
            return isValid;
        }catch (Exception e){
            logger.warn("Token validation failed for user {}: {}", user.getEmail(), e.getMessage());
            return false;
        }
    }

    public boolean isTokenExpired(String jwt){
        return extractExpiration(jwt).before(new Date());
    }
    public Date extractExpiration(String jwt){
        return extractAllClaims(jwt).getExpiration();
    }

    private Claims extractAllClaims(String jwt){
        return Jwts.parserBuilder().setSigningKey(generateKey()).build()
                .parseClaimsJws(jwt).getBody();
    }
}

package com.cognizant.agriserve.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    // Best Practice: Pull the secret from application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Updated for JJWT 0.13.0 Syntax
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey()) // Replaces setSigningKey()
                    .build()
                    .parseSignedClaims(token); // Replaces parseClaimsJws()
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // parseSignedClaims automatically throws an exception if the token is expired or altered!
            return false;
        }
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // CRITICAL: We keep this from the old util so the Gateway can pass the ID to microservices
    public Long extractUserId(String token) {
        Number userId = getClaims(token).get("userId", Number.class);
        if (userId != null) {
            return userId.longValue();
        }
        return null;
    }

    // Updated for JJWT 0.13.0 Syntax
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload(); // Replaces getBody()
    }
}
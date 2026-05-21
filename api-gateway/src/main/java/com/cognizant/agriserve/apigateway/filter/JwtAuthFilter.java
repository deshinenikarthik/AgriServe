package com.cognizant.agriserve.apigateway.filter;

import com.cognizant.agriserve.apigateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 1. Skip JWT validation for auth endpoints AND internal errors
        if (path.startsWith("/auth") || path.startsWith("/api/auth") || path.equals("/error")) {
            log.info("Endpoint bypassed by JWT Filter: {}", path);
            return chain.filter(exchange);
        }

        // 2. Check for Authorization Header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing Authorization Header for path: {}", path);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        // Extract token
        String token = authHeader.substring(7);

        // 3. Validate Token & Extract Data
        try {
            if (!jwtUtil.isTokenValid(token)) {
                log.warn("Invalid Token for path: {}", path);
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
            }

            // Extract Info
            String username = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractRole(token);
            Long userId = jwtUtil.extractUserId(token);

            log.info("Token validated for user: {} with role: {}", username, role);

            // 4. Securely forward via headers to downstream microservices
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(builder -> {
                        // CRITICAL SECURITY FIX: Strip any existing headers sent by a malicious client
                        builder.headers(headers -> {
                            headers.remove("X-Logged-In-User-Id");
                            headers.remove("X-User-Name");
                            headers.remove("X-User-Role");
                        });

                        // Inject the trusted, verified values from the JWT
                        builder.header("X-Logged-In-User-Id", String.valueOf(userId))
                                .header("X-User-Name", username)
                                .header("X-User-Role", role);
                    })
                    .build();

            log.info("GATEWAY DEBUG: Forwarding request with User ID: {}", userId);

            return chain.filter(mutatedExchange);

        } catch (Exception e) {
            log.error("Error validating token for path: {}, error: {}", path, e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token validation failed: " + e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
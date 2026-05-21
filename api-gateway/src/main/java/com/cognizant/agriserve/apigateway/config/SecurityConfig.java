package com.cognizant.agriserve.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                // 0. CRITICAL FIX: Enable CORS integration for Spring Security WebFlux
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 1. Disable CSRF (Not needed for stateless JWT APIs)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                // 2. Tell Spring Security NOT to create sessions.
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

                // 3. Let all requests pass through Spring Security.
                // Your custom JwtAuthFilter will catch and validate them!
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/auth/**", "/api/auth/**").permitAll()
                        // Ensure OPTIONS preflight requests are explicitly allowed
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .anyExchange().permitAll()
                )

                // 4. Disable default browser pop-up login screens
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)

                .build();
    }

    // 5. The WebFlux CORS Configuration Source
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow your Angular frontend
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));

        // Allow the preflight OPTIONS method, plus standard methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow headers that Angular will send (like Authorization and custom headers)
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Logged-In-User-Id", "X-User-Role"));

        // Required if you ever use cookies or credentials
        configuration.setAllowCredentials(true);

        // Cache the preflight response for 1 hour so the browser doesn't ask on every single click
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this CORS policy to all routes going through the gateway
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
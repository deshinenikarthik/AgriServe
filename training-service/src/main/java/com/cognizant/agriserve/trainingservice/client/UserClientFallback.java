package com.cognizant.agriserve.trainingservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Fallback implementation for UserClient.
 *
 * Activated when the User Service is unreachable (not registered in Eureka,
 * or the User Service is down). Returns safe defaults so the Training Service
 * can continue working for development and testing without the User Service running.
 *
 * NOTE: This fallback requires spring.cloud.openfeign.circuitbreaker.enabled=true
 *       in application.properties.
 */
@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public boolean checkUserExists(Long id) {
        log.warn("⚠️  User Service is unavailable. Defaulting user ID {} as EXISTING for development purposes.", id);
        // Return true so the Training Service doesn't block operations when
        // the User Service isn't running (e.g., during isolated testing)
        return true;
    }
}

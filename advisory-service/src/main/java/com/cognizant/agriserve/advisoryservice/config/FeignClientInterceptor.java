package com.cognizant.agriserve.advisoryservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Value("${internal.service.key}")
    private String keyForInternalService;

    @Override
    public void apply(RequestTemplate requestTemplate) {

        // 1. Add your Internal Service Key (Machine-to-Machine Auth)
        requestTemplate.header("X-Internal-service-key", keyForInternalService);

        // 2. Safely extract the original request
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 3. Forward the User's Role (Fixes the 400 Missing Header error)
            String userRole = request.getHeader("X-User-Role");
            if (userRole != null) {
                requestTemplate.header("X-User-Role", userRole);
            }

            // 4. Forward the User's Name
            String userName = request.getHeader("X-User-Name");
            if (userName != null) {
                requestTemplate.header("X-User-Name", userName);
            }

            // 5. (Optional) Forward Authorization if you also use JWTs alongside Gateway headers
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null) {
                requestTemplate.header("Authorization", authHeader);
            }
        }
    }
}
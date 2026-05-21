package com.cognizant.agriserve.authservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FeignClientInterceptor implements RequestInterceptor {

    @Value("${internal.service.key}")
    private String keyForInternalService;

    @Override
    public void apply(RequestTemplate requestTemplate) {
        requestTemplate.header("X-Internal-service-key", keyForInternalService);
    }
}
package com.cognizant.agriserve.apigateway.exception;

import com.cognizant.agriserve.apigateway.dto.ErrorResponseDTO;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Order(-2) // High priority to ensure it catches filter exceptions
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    // 1. You must have a constructor that calls super()
    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  WebProperties webProperties,
                                  ApplicationContext applicationContext,
                                  ServerCodecConfigurer codecConfigurer) {
        super(errorAttributes, webProperties.getResources(), applicationContext);
        this.setMessageWriters(codecConfigurer.getWriters());
        this.setMessageReaders(codecConfigurer.getReaders());
    }

    // 2. Define the routing logic for errors
    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    // 3. Format the actual JSON response
    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());

        int status = (int) errorPropertiesMap.getOrDefault("status", 500);
        String message = (String) errorPropertiesMap.getOrDefault("message", "No message available");

        // Custom logic for Auth failures (passed dynamically from your JwtAuthFilter)
        if (status == 401) {
            // Only override if the message isn't already a specific custom message
            if (message.equals("No message available") || message.contains("Unauthorized")) {
                message = "Unauthorized: Access token is missing or invalid.";
            }
        }

        // Swapped out GatewayErrorResponse for ErrorResponseDTO
        ErrorResponseDTO response = new ErrorResponseDTO(
                LocalDateTime.now(),
                status,
                (String) errorPropertiesMap.getOrDefault("error", "Internal Error"),
                message,
                request.path()
        );

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(response));
    }
}
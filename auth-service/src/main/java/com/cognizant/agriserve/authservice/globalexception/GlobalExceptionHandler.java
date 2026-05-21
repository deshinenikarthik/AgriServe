package com.cognizant.agriserve.authservice.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

// NOTE: Ensure this import matches the exact location of your ErrorResponseDTO in the Auth Service
import com.cognizant.agriserve.authservice.dto.ErrorResponseDTO;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 1. Catch 401: Wrong Password or Username during Login
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request) {

        log.warn("Login failed: Bad credentials for request at {}", request.getRequestURI());

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                "Invalid email or password. Please try again.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    // 2. Catch 404: User not found in the database during Auth checks
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUsernameNotFound(
            UsernameNotFoundException ex,
            HttpServletRequest request) {

        log.warn("Auth lookup failed: {}", ex.getMessage());

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // 3. Catch OpenFeign Errors (e.g., when calling User Service to register)
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponseDTO> handleFeignException(
            FeignException ex,
            HttpServletRequest request) {

        log.error("Downstream microservice error: Status {}, Message: {}", ex.status(), ex.getMessage());

        int status = ex.status() != -1 ? ex.status() : HttpStatus.INTERNAL_SERVER_ERROR.value();
        String errorType = HttpStatus.valueOf(status).getReasonPhrase();
        String errorMessage = "An error occurred while communicating with an internal service.";

        // Custom translation for common microservice interactions
        if (status == 409) {
            errorMessage = "Registration Conflict: The email provided is already in use.";
        } else if (status == 404) {
            errorMessage = "Requested resource was not found in the internal system.";
        } else if (status == 400) {
            errorMessage = "Invalid data sent to internal service.";
        }

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                status,
                errorType,
                errorMessage,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }

    // 4. Catch 403: Spring Security Access Denied
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request) {

        log.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "You do not have permission to access this resource.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    // 5. Catch 400: Validation Failures (@Valid in RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                errorMessage,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 6. Catch 400: Validation Failures in URL Params
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        String message = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(", "));

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                message,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // 7. Catch 500: The Ultimate Safety Net
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {

        log.error("CRITICAL SERVER ERROR at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected system error occurred. Please try again later.",
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Catch Internal Authentication Errors (Thrown when Feign fails inside Spring Security's UserDetailsService)
    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ErrorResponseDTO> handleInternalAuthenticationServiceException(
            InternalAuthenticationServiceException ex,
            HttpServletRequest request) {

        log.error("Internal Auth Error at {}: {}", request.getRequestURI(), ex.getMessage());

        int status = HttpStatus.SERVICE_UNAVAILABLE.value();
        String errorType = "Service Unavailable";
        String errorMessage = "The authentication service is temporarily unavailable. Please try again later.";

        // Unwrap the exception to see if OpenFeign caused it
        if (ex.getCause() instanceof FeignException) {
            FeignException feignEx = (FeignException) ex.getCause();

            // If the User Service returned 404 (User doesn't exist), treat it as a standard login failure
            if (feignEx.status() == 404) {
                status = HttpStatus.UNAUTHORIZED.value();
                errorType = "Unauthorized";
                errorMessage = "Invalid email or password.";
            }
            // If the User Service threw a 400 or 500 (like it did with "NONE_PROVIDED")
            else {
                errorMessage = "Failed to verify credentials with the User System.";
            }
        }

        ErrorResponseDTO errorResponse = new ErrorResponseDTO(
                LocalDateTime.now(),
                status,
                errorType,
                errorMessage,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(errorResponse);
    }
}
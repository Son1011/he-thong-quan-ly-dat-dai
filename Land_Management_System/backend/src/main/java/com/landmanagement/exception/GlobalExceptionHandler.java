package com.landmanagement.exception;

import com.landmanagement.dto.response.ApiErrorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for consistent error responses across the
 * application.
 * Handles validation errors, authentication/authorization errors, and business
 * logic exceptions.
 */
@RestControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class GlobalExceptionHandler {

        /**
         * Handle validation errors from @Valid annotations
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
                        HttpServletRequest request) {
                Map<String, String> errors = new HashMap<>();
                ex.getBindingResult().getAllErrors().forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.put(fieldName, errorMessage);
                });

                ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                                .code("VALIDATION_ERROR")
                                .message("Validation failed")
                                .status(HttpStatus.BAD_REQUEST.value())
                                .details(errors)
                                .path(request.getRequestURI())
                                .build();

                log.warn("Validation error: {}", errors);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        /**
         * Handle authentication exceptions (login failures, missing tokens)
         */
        @ExceptionHandler({ AuthenticationException.class, BadCredentialsException.class })
        public ResponseEntity<ApiErrorResponse> handleAuthenticationException(Exception ex,
                        HttpServletRequest request) {
                ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                                .code("AUTHENTICATION_ERROR")
                                .message("Authentication failed: " + ex.getMessage())
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .path(request.getRequestURI())
                                .build();

                log.warn("Authentication error: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        /**
         * Handle authorization exceptions (insufficient permissions)
         */
        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(AccessDeniedException ex,
                        HttpServletRequest request) {
                ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                                .code("ACCESS_DENIED")
                                .message("You do not have permission to access this resource")
                                .status(HttpStatus.FORBIDDEN.value())
                                .path(request.getRequestURI())
                                .build();

                log.warn("Access denied: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        /**
         * Handle resource not found
         */
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex,
                        HttpServletRequest request) {
                ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                                .code("RESOURCE_NOT_FOUND")
                                .message(ex.getMessage())
                                .status(HttpStatus.NOT_FOUND.value())
                                .path(request.getRequestURI())
                                .build();

                log.warn("Resource not found: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        /**
         * Handle illegal argument exceptions (business logic violations)
         */
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
                        HttpServletRequest request) {
                ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                                .code("INVALID_REQUEST")
                                .message(ex.getMessage())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .path(request.getRequestURI())
                                .build();

                log.warn("Invalid argument: {}", ex.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        /**
         * Handle business logic exceptions
         */
        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ApiErrorResponse> handleBusinessException(BusinessException ex,
                        HttpServletRequest request) {
                ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                                .code(ex.getCode())
                                .message(ex.getMessage())
                                .status(ex.getStatusCode())
                                .path(request.getRequestURI())
                                .build();

                log.warn("Business exception [{}]: {}", ex.getCode(), ex.getMessage());
                return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
        }

        /**
         * Handle 404 - endpoint not found
         */
        @ExceptionHandler(NoHandlerFoundException.class)
        public ResponseEntity<ApiErrorResponse> handleNoHandlerFoundException(NoHandlerFoundException ex,
                        HttpServletRequest request) {
                ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                                .code("ENDPOINT_NOT_FOUND")
                                .message("The requested endpoint does not exist")
                                .status(HttpStatus.NOT_FOUND.value())
                                .path(ex.getRequestURL())
                                .build();

                log.warn("Endpoint not found: {} {}", ex.getHttpMethod(), ex.getRequestURL());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        /**
         * Handle all other exceptions (catch-all)
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiErrorResponse> handleGenericException(Exception ex,
                        HttpServletRequest request) {
                ApiErrorResponse errorResponse = ApiErrorResponse.builder()
                                .code("INTERNAL_SERVER_ERROR")
                                .message("An unexpected error occurred")
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .path(request.getRequestURI())
                                .build();

                log.error("Unexpected error", ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
}

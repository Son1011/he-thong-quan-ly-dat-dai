package com.landmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response format for all API errors.
 * Ensures consistent error handling across the application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    private String code; // Machine-readable error code
    private String message; // User-friendly error message
    private Integer status; // HTTP status code
    private LocalDateTime timestamp;
    private String path; // Request path
    private Map<String, String> details; // Additional error details (for validation errors)
    private String traceId; // Request trace ID for debugging

    /**
     * Create a simple error response
     */
    public static ApiErrorResponse of(String code, String message, Integer status) {
        return ApiErrorResponse.builder()
                .code(code)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Create error response with details
     */
    public static ApiErrorResponse of(String code, String message, Integer status, Map<String, String> details) {
        return ApiErrorResponse.builder()
                .code(code)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
    }
}

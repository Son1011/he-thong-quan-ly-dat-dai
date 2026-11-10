package com.landmanagement.exception;

/**
 * Custom exception for business logic violations.
 * Provides machine-readable error codes and HTTP status codes.
 */
public class BusinessException extends RuntimeException {
    private final String code;
    private final int statusCode;

    public BusinessException(String code, String message) {
        this(code, message, 400);
    }

    public BusinessException(String code, String message, int statusCode) {
        super(message);
        this.code = code;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public static BusinessException withCode(String code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException withCode(String code, String message, int statusCode) {
        return new BusinessException(code, message, statusCode);
    }
}

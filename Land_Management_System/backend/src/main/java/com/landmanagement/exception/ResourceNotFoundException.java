package com.landmanagement.exception;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static ResourceNotFoundException forResource(String resourceType, Long id) {
        return new ResourceNotFoundException(String.format("%s with id %d not found", resourceType, id));
    }

    public static ResourceNotFoundException forResource(String resourceType, String identifier) {
        return new ResourceNotFoundException(
                String.format("%s with identifier '%s' not found", resourceType, identifier));
    }
}

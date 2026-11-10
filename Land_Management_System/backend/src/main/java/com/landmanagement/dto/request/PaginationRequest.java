package com.landmanagement.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for pagination parameters.
 * Provides standard pagination fields for list APIs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginationRequest {
    @Min(value = 0, message = "Page must be >= 0")
    private int page;

    @Min(value = 1, message = "Size must be >= 1")
    private int size;

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    /**
     * Get page with default value if null or invalid
     */
    public int getPageOrDefault() {
        return page < 0 ? DEFAULT_PAGE : page;
    }

    /**
     * Get size with default value if invalid, capped at MAX_SIZE
     */
    public int getSizeOrDefault() {
        if (size <= 0) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    /**
     * Get offset for database queries
     */
    public int getOffset() {
        return getPageOrDefault() * getSizeOrDefault();
    }

    /**
     * Create pagination request with defaults
     */
    public static PaginationRequest withDefaults(int page, int size) {
        return PaginationRequest.builder()
                .page(page)
                .size(size)
                .build();
    }
}

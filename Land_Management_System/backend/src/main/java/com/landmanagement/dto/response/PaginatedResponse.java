package com.landmanagement.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response wrapper for list APIs.
 * Provides consistent pagination structure with metadata.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaginatedResponse<T> {
    private List<T> data;
    private long total;
    private int page;
    private int size;
    @JsonProperty("total_pages")
    private int totalPages;
    @JsonProperty("has_next")
    private boolean hasNext;
    @JsonProperty("has_previous")
    private boolean hasPrevious;

    /**
     * Create paginated response from data
     */
    public static <T> PaginatedResponse<T> of(List<T> data, long total, int page, int size) {
        int totalPages = (int) Math.ceil((double) total / size);
        return PaginatedResponse.<T>builder()
                .data(data)
                .total(total)
                .page(page)
                .size(size)
                .totalPages(totalPages)
                .hasNext(page < totalPages - 1)
                .hasPrevious(page > 0)
                .build();
    }
}

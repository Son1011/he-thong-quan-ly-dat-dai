package com.landmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachmentResponse {
    private Long id;
    private Long dossierId;
    private String originalFilename;
    private String contentType;
    private Long uploadedByUserId;
    private LocalDateTime uploadedAt;
}

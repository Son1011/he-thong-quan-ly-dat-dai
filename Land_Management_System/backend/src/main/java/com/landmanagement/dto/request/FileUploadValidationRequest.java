package com.landmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Validation constraints for file uploads.
 * Ensures file size, type, and name are valid before processing.
 */
@Data
public class FileUploadValidationRequest {
    @NotBlank(message = "Filename is required")
    private String filename;

    @NotNull(message = "File size is required")
    @Positive(message = "File size must be positive")
    private Long fileSize;

    @NotBlank(message = "MIME type is required")
    private String mimeType;

    // Validation constants
    public static class ValidationConstraints {
        public static final long MAX_FILE_SIZE = 50 * 1024 * 1024;  // 50 MB
        public static final long MIN_FILE_SIZE = 1;                  // 1 byte

        public static final String[] ALLOWED_MIME_TYPES = {
                "application/pdf",
                "image/jpeg",
                "image/png",
                "image/tiff",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/plain",
                "application/zip"
        };

        public static final String[] BLOCKED_EXTENSIONS = {
                ".exe", ".bat", ".cmd", ".com", ".pif", ".scr",
                ".vbs", ".js", ".jar", ".zip", ".rar"
        };

        public static final int MAX_FILENAME_LENGTH = 255;
    }

    /**
     * Validate file size
     */
    public boolean isFileSizeValid() {
        return fileSize >= ValidationConstraints.MIN_FILE_SIZE &&
                fileSize <= ValidationConstraints.MAX_FILE_SIZE;
    }

    /**
     * Validate MIME type
     */
    public boolean isMimeTypeAllowed() {
        for (String allowed : ValidationConstraints.ALLOWED_MIME_TYPES) {
            if (mimeType.equalsIgnoreCase(allowed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if file extension is blocked
     */
    public boolean isExtensionBlocked() {
        String lowerFilename = filename.toLowerCase();
        for (String blocked : ValidationConstraints.BLOCKED_EXTENSIONS) {
            if (lowerFilename.endsWith(blocked)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validate filename length
     */
    public boolean isFilenameLengthValid() {
        return filename.length() <= ValidationConstraints.MAX_FILENAME_LENGTH;
    }

    /**
     * Comprehensive validation
     */
    public boolean isValid() {
        return isFilenameLengthValid() &&
                isFileSizeValid() &&
                isMimeTypeAllowed() &&
                !isExtensionBlocked();
    }
}

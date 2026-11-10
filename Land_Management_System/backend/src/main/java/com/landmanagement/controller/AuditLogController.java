package com.landmanagement.controller;

import com.landmanagement.dto.response.PaginatedResponse;
import com.landmanagement.entity.AuditLog;
import com.landmanagement.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for audit log management.
 * Provides endpoints to view audit logs for compliance and debugging purposes.
 * Only accessible to ADMIN users.
 */
@RestController
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "Audit log management endpoints (Admin only)")
@SecurityRequirement(name = "bearer-jwt")
public class AuditLogController {

    private final AuditLogService auditLogService;

    /**
     * Get audit logs for a specific dossier
     */
    @GetMapping("/dossier/{dossierId}")
    @Operation(summary = "Get audit logs for a dossier")
    public ResponseEntity<PaginatedResponse<AuditLog>> getDossierAuditLogs(
            @PathVariable Long dossierId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute("userId") Long userId) {
        Page<AuditLog> result = auditLogService.getDossierAuditLogs(dossierId, page, size);
        PaginatedResponse<AuditLog> response = PaginatedResponse.of(
                result.getContent(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get audit logs for a specific user
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get audit logs for a user")
    public ResponseEntity<PaginatedResponse<AuditLog>> getUserAuditLogs(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute("userId") Long requesterId) {
        Page<AuditLog> result = auditLogService.getUserAuditLogs(userId, page, size);
        PaginatedResponse<AuditLog> response = PaginatedResponse.of(
                result.getContent(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get audit logs by action type
     */
    @GetMapping("/by-action/{actionType}")
    @Operation(summary = "Get audit logs by action type")
    public ResponseEntity<PaginatedResponse<AuditLog>> getAuditLogsByActionType(
            @PathVariable String actionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestAttribute("userId") Long userId) {
        Page<AuditLog> result = auditLogService.getAuditLogsByActionType(actionType, page, size);
        PaginatedResponse<AuditLog> response = PaginatedResponse.of(
                result.getContent(),
                result.getTotalElements(),
                result.getNumber(),
                result.getSize()
        );
        return ResponseEntity.ok(response);
    }
}

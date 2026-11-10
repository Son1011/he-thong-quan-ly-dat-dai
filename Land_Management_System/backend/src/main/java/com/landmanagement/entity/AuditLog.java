package com.landmanagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Audit log entity for tracking sensitive operations.
 * Records user actions, timestamps, and changes for compliance and debugging.
 */
@Entity
@Table(name = "audit_log", indexes = {
        @Index(name = "idx_audit_user_id", columnList = "user_id"),
        @Index(name = "idx_audit_dossier_id", columnList = "dossier_id"),
        @Index(name = "idx_audit_timestamp", columnList = "created_at"),
        @Index(name = "idx_audit_action", columnList = "action_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "action_type", nullable = false)
    private String actionType;  // CREATE, UPDATE, DELETE, APPROVE, REJECT, etc.

    @Column(name = "entity_type", nullable = false)
    private String entityType;  // DOSSIER, USER, UNIT, ATTACHMENT, etc.

    @Column(name = "entity_id")
    private Long entityId;      // ID of the affected entity

    @Column(name = "dossier_id")
    private Long dossierId;     // For tracking dossier-related actions

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;    // Previous value (JSON format)

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;    // New value (JSON format)

    @Column(name = "ip_address", length = 45)
    private String ipAddress;   // IPv4 or IPv6

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "status", nullable = false)
    private String status;      // SUCCESS, FAILED

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

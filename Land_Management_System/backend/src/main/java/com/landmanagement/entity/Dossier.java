package com.landmanagement.entity;

import com.landmanagement.enums.DossierStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "dossier", uniqueConstraints = {
        @UniqueConstraint(columnNames = "dossier_code")
}, indexes = {
        @Index(name = "idx_dossier_status", columnList = "status_code"),
        @Index(name = "idx_dossier_origin_unit", columnList = "origin_unit_id"),
        @Index(name = "idx_dossier_assigned_unit", columnList = "assigned_to_unit_id"),
        @Index(name = "idx_dossier_created_by", columnList = "created_by_user_id"),
        @Index(name = "idx_dossier_sent_to_central", columnList = "sent_to_central"),
        @Index(name = "idx_dossier_created_at", columnList = "created_at"),
        @Index(name = "idx_dossier_status_origin", columnList = "status_code,origin_unit_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dossier_code", nullable = false, unique = true)
    private String dossierCode;

    @Column(nullable = false)
    private String title;

    @Column(name = "citizen_name", nullable = false)
    private String citizenName;

    @Column(name = "citizen_phone")
    private String citizenPhone;

    @Column(name = "citizen_identity_number")
    private String citizenIdentityNumber;

    @Column(name = "citizen_address")
    private String citizenAddress;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "dossier_type_id", nullable = false)
    private DossierType dossierType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_code", nullable = false)
    private DossierStatus status;

    @Column(name = "sent_to_central", nullable = false)
    @Builder.Default
    private Boolean sentToCentral = false;

    @Column(name = "origin_unit_id", nullable = false)
    private Long originUnitId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "origin_unit_id", insertable = false, updatable = false)
    private AdministrativeUnit originUnit;

    @Column(name = "assigned_to_unit_id", nullable = false)
    private Long assignedToUnitId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_to_unit_id", insertable = false, updatable = false)
    private AdministrativeUnit assignedToUnit;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_user_id", insertable = false, updatable = false)
    private UserAccount createdByUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Priority priority = Priority.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_step", nullable = false)
    @Builder.Default
    private CurrentStep currentStep = CurrentStep.RECEIVE;

    @Column(name = "received_at")
    private LocalDateTime receivedAt;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum Priority {
        NORMAL,
        URGENT,
        EMERGENCY
    }

    public enum CurrentStep {
        RECEIVE,
        VERIFY,
        APPROVE,
        COMPLETE
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

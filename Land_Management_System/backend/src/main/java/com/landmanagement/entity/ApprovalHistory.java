package com.landmanagement.entity;

import com.landmanagement.enums.ApprovalAction;
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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "approval_history", indexes = {
        @Index(name = "idx_approval_dossier", columnList = "dossier_id"),
        @Index(name = "idx_approval_actor", columnList = "actor_user_id"),
        @Index(name = "idx_approval_actor_unit", columnList = "actor_unit_id"),
        @Index(name = "idx_approval_action", columnList = "action_code"),
        @Index(name = "idx_approval_created", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dossier_id", nullable = false)
    private Long dossierId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", insertable = false, updatable = false)
    private Dossier dossier;

    @Column(name = "actor_user_id", nullable = false)
    private Long actorUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", insertable = false, updatable = false)
    private UserAccount actorUser;

    @Column(name = "actor_unit_id", nullable = false)
    private Long actorUnitId;

    @Column(name = "from_unit_id")
    private Long fromUnitId;

    @Column(name = "to_unit_id")
    private Long toUnitId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_code", nullable = false)
    private ApprovalAction action;

    @Column
    private String note;

    @Column(name = "signature_base64_png")
    private String signatureBase64Png;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

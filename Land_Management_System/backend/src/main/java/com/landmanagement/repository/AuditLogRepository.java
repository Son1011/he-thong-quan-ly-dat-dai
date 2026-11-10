package com.landmanagement.repository;

import com.landmanagement.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs for a specific dossier
     */
    Page<AuditLog> findByDossierId(Long dossierId, Pageable pageable);

    /**
     * Find audit logs by user
     */
    Page<AuditLog> findByUserId(Long userId, Pageable pageable);

    /**
     * Find audit logs by action type
     */
    Page<AuditLog> findByActionType(String actionType, Pageable pageable);

    /**
     * Find audit logs by entity type
     */
    @Query("SELECT a FROM AuditLog a WHERE a.entityType = :entityType ORDER BY a.createdAt DESC")
    Page<AuditLog> findByEntityType(@Param("entityType") String entityType, Pageable pageable);

    /**
     * Find audit logs within a date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit logs for a specific dossier and action type
     */
    @Query("SELECT a FROM AuditLog a WHERE a.dossierId = :dossierId AND a.actionType = :actionType ORDER BY a.createdAt DESC")
    List<AuditLog> findByDossierIdAndActionType(@Param("dossierId") Long dossierId,
            @Param("actionType") String actionType);
}

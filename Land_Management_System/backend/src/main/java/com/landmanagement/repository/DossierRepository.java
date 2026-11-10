package com.landmanagement.repository;

import com.landmanagement.entity.Dossier;
import com.landmanagement.enums.ApprovalAction;
import com.landmanagement.enums.DossierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DossierRepository extends JpaRepository<Dossier, Long> {

        List<Dossier> findByOriginUnitIdOrderByUpdatedAtDesc(Long originUnitId);

        List<Dossier> findByAssignedToUnitIdOrderByUpdatedAtDesc(Long assignedToUnitId);

        @Query("SELECT d FROM Dossier d WHERE d.originUnitId IN :unitIds ORDER BY d.updatedAt DESC")
        List<Dossier> findByOriginUnitIdInOrderByUpdatedAtDesc(@Param("unitIds") List<Long> unitIds);

        @Query("""
                        SELECT d FROM Dossier d
                        WHERE d.originUnitId IN :unitIds
                          AND (:status IS NULL OR d.status = :status)
                          AND (:sentToCentral IS NULL OR d.sentToCentral = :sentToCentral)
                        ORDER BY d.updatedAt DESC
                        """)
        List<Dossier> searchByOriginUnitIds(@Param("unitIds") List<Long> unitIds,
                        @Param("status") DossierStatus status,
                        @Param("sentToCentral") Boolean sentToCentral);

        @Query("""
                        SELECT d FROM Dossier d
                        WHERE d.assignedToUnitId = :assignedToUnitId
                          AND (:status IS NULL OR d.status = :status)
                          AND (:sentToCentral IS NULL OR d.sentToCentral = :sentToCentral)
                        ORDER BY d.updatedAt DESC
                        """)
        List<Dossier> searchByAssignedToUnitId(@Param("assignedToUnitId") Long assignedToUnitId,
                        @Param("status") DossierStatus status,
                        @Param("sentToCentral") Boolean sentToCentral);

        @Query("""
                        SELECT d FROM Dossier d
                        WHERE (:status IS NULL OR d.status = :status)
                          AND (:sentToCentral IS NULL OR d.sentToCentral = :sentToCentral)
                        ORDER BY d.updatedAt DESC
                        """)
        List<Dossier> searchAll(@Param("status") DossierStatus status,
                        @Param("sentToCentral") Boolean sentToCentral);

        @Query("""
                        SELECT d FROM Dossier d
                        WHERE EXISTS (
                            SELECT h FROM ApprovalHistory h
                            WHERE h.dossier.id = d.id
                              AND h.id = (
                                  SELECT MAX(h2.id) FROM ApprovalHistory h2
                                  WHERE h2.dossier.id = d.id
                              )
                              AND h.action IN :actionTypes
                        )
                        ORDER BY d.updatedAt DESC
                        """)
        List<Dossier> findByLatestApprovalActionInOrderByUpdatedAtDesc(
                        @Param("actionTypes") List<ApprovalAction> actionTypes);

        // Pagination queries

        /**
         * Find dossiers by origin units with pagination
         */
        @Query("""
                        SELECT d FROM Dossier d
                        WHERE d.originUnitId IN :unitIds
                          AND (:status IS NULL OR d.status = :status)
                          AND (:sentToCentral IS NULL OR d.sentToCentral = :sentToCentral)
                        ORDER BY d.updatedAt DESC
                        """)
        Page<Dossier> searchByOriginUnitIdsPageable(@Param("unitIds") List<Long> unitIds,
                        @Param("status") DossierStatus status,
                        @Param("sentToCentral") Boolean sentToCentral,
                        Pageable pageable);

        /**
         * Find dossiers by assigned to unit with pagination
         */
        @Query("""
                        SELECT d FROM Dossier d
                        WHERE d.assignedToUnitId = :assignedToUnitId
                          AND (:status IS NULL OR d.status = :status)
                          AND (:sentToCentral IS NULL OR d.sentToCentral = :sentToCentral)
                        ORDER BY d.updatedAt DESC
                        """)
        Page<Dossier> searchByAssignedToUnitIdPageable(@Param("assignedToUnitId") Long assignedToUnitId,
                        @Param("status") DossierStatus status,
                        @Param("sentToCentral") Boolean sentToCentral,
                        Pageable pageable);

        /**
         * Find all dossiers with pagination
         */
        @Query("""
                        SELECT d FROM Dossier d
                        WHERE (:status IS NULL OR d.status = :status)
                          AND (:sentToCentral IS NULL OR d.sentToCentral = :sentToCentral)
                        ORDER BY d.updatedAt DESC
                        """)
        Page<Dossier> searchAllPageable(@Param("status") DossierStatus status,
                        @Param("sentToCentral") Boolean sentToCentral,
                        Pageable pageable);

        /**
         * Find inbox dossiers with pagination
         */
        @Query("""
                        SELECT d FROM Dossier d
                        WHERE d.assignedToUnitId = :unitId
                          AND d.status NOT IN ('COMPLETED', 'REJECTED')
                        ORDER BY d.updatedAt DESC
                        """)
        Page<Dossier> findInboxPageable(@Param("unitId") Long unitId, Pageable pageable);

        /**
         * Find central decisions with pagination
         */
        @Query("""
                        SELECT d FROM Dossier d
                        WHERE d.sentToCentral = true
                          AND d.status NOT IN ('COMPLETED', 'REJECTED')
                        ORDER BY d.updatedAt DESC
                        """)
        Page<Dossier> findCentralDecisionsPageable(Pageable pageable);
}

package com.landmanagement.repository;

import com.landmanagement.entity.ApprovalHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalHistoryRepository extends JpaRepository<ApprovalHistory, Long> {
    List<ApprovalHistory> findByDossierIdOrderByCreatedAtAsc(Long dossierId);

    List<ApprovalHistory> findByDossier_IdOrderByCreatedAtAsc(Long dossierId);
}

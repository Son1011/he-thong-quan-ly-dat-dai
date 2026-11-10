package com.landmanagement.repository;

import com.landmanagement.entity.DossierAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DossierAttachmentRepository extends JpaRepository<DossierAttachment, Long> {
    List<DossierAttachment> findByDossierIdOrderByUploadedAtDesc(Long dossierId);

    List<DossierAttachment> findByDossier_IdOrderByUploadedAtDesc(Long dossierId);

    long countByDossierIdIn(List<Long> dossierIds);

    long countByDossier_IdIn(List<Long> dossierIds);

    Optional<DossierAttachment> findByIdAndDossierId(Long id, Long dossierId);

    Optional<DossierAttachment> findByIdAndDossier_Id(Long id, Long dossierId);
}

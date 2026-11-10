package com.landmanagement.repository;

import com.landmanagement.entity.DossierTransition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DossierTransitionRepository extends JpaRepository<DossierTransition, Long> {

    @Query("SELECT dt FROM DossierTransition dt WHERE dt.dossier.id = :dossierId ORDER BY dt.actionTime DESC")
    List<DossierTransition> findByDossierIdOrderByActionTimeDesc(@Param("dossierId") Long dossierId);
}
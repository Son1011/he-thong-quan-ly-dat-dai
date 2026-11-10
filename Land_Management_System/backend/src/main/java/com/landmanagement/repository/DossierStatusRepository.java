package com.landmanagement.repository;

import com.landmanagement.entity.DossierStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DossierStatusRepository extends JpaRepository<DossierStatus, Long> {
    Optional<DossierStatus> findByCode(String code);
}
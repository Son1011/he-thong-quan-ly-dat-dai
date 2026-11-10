package com.landmanagement.repository;

import com.landmanagement.entity.DossierType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DossierTypeRepository extends JpaRepository<DossierType, Long> {
    Optional<DossierType> findByCode(String code);
}
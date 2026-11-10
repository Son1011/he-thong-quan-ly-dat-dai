package com.landmanagement.repository;

import com.landmanagement.entity.AdministrativeUnit;
import com.landmanagement.enums.UnitLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitRepository extends JpaRepository<AdministrativeUnit, Long> {
        // Using UnitLevel field name (from entity)
        Optional<AdministrativeUnit> findByUnitLevelAndIsActive(UnitLevel unitLevel, Boolean isActive);

        List<AdministrativeUnit> findByUnitLevelAndIsActiveOrderByName(UnitLevel unitLevel, Boolean isActive);

        List<AdministrativeUnit> findByParentIdAndUnitLevelAndIsActiveOrderByName(Long parentId, UnitLevel unitLevel,
                        Boolean isActive);

        List<AdministrativeUnit> findByParent_IdAndUnitLevelAndIsActiveOrderByName(Long parentId, UnitLevel unitLevel,
                        Boolean isActive);

        List<AdministrativeUnit> findByNameContainingIgnoreCaseAndUnitLevelAndIsActive(String name, UnitLevel unitLevel,
                        Boolean isActive);

        // Aliases for convenience (shorter method names without "Unit" prefix)
        default Optional<AdministrativeUnit> findByLevelAndIsActive(UnitLevel level, Boolean isActive) {
                return findByUnitLevelAndIsActive(level, isActive);
        }

        default List<AdministrativeUnit> findByLevelAndIsActiveOrderByName(UnitLevel level, Boolean isActive) {
                return findByUnitLevelAndIsActiveOrderByName(level, isActive);
        }

        default List<AdministrativeUnit> findByParentIdAndLevelAndIsActiveOrderByName(Long parentId, UnitLevel level,
                        Boolean isActive) {
                return findByParentIdAndUnitLevelAndIsActiveOrderByName(parentId, level, isActive);
        }

        default List<AdministrativeUnit> findByNameContainingIgnoreCaseAndLevelAndIsActive(String name, UnitLevel level,
                        Boolean isActive) {
                return findByNameContainingIgnoreCaseAndUnitLevelAndIsActive(name, level, isActive);
        }
}

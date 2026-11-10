package com.landmanagement.repository;

import com.landmanagement.entity.UserAccount;
import com.landmanagement.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByUsername(String username);

  Optional<UserAccount> findByEmail(String email);

  @Query("""
      SELECT u FROM UserAccount u
      WHERE (:q IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
             OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')))
        AND (:role IS NULL OR u.role = :role)
        AND (:unitId IS NULL OR u.unitId = :unitId)
        
      ORDER BY u.username
      """)
  Page<UserAccount> searchUsers(@Param("q") String q,
      @Param("role") UserRole role,
      @Param("unitId") Long unitId,
      Pageable pageable);
}

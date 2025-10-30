// src/main/java/com/cakestore/cakestore/repository/UserRepository.java
package com.cakestore.cakestore.repository.admin;

import com.cakestore.cakestore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List; // Thêm import
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"branch", "addresses"})
	Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Override
    @EntityGraph(attributePaths = {"addresses", "branch"})
    Optional<User> findById(Long id);


    // CẬP NHẬT: Thêm @EntityGraph và SỬA LỖI (xóa comment)
    @EntityGraph(attributePaths = {"branch"}) 
    @Query("""
        SELECT u FROM User u
        WHERE (:status IS NULL OR u.isActive = :status)
          AND (:branchId IS NULL OR u.branch.id = :branchId) 
          AND (
            :kw = '' OR
            LOWER(u.email)    LIKE LOWER(CONCAT('%', :kw, '%')) OR
            LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) OR
            u.phone           LIKE CONCAT('%', :kw, '%')
          )
        """)
    Page<User> searchAllWithStatus(@Param("kw") String keyword,
                                   @Param("status") Boolean status,
                                   @Param("branchId") Long branchId,
                                   Pageable pageable);

    // CẬP NHẬT: Thêm @EntityGraph và SỬA LỖI (xóa comment)
    @EntityGraph(attributePaths = {"branch"}) 
    @Query("""
    	    SELECT u FROM User u
    	    WHERE u.role = :role
    	      AND (:status IS NULL OR u.isActive = :status)
              AND (:branchId IS NULL OR u.branch.id = :branchId)
    	      AND (
    	        :kw = '' OR
    	        LOWER(u.email)    LIKE LOWER(CONCAT('%', :kw, '%')) OR
    	        LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) OR
    	        u.phone           LIKE CONCAT('%', :kw, '%')
    	      )
    	    """)
    	Page<User> searchByRoleWithStatus(@Param("role") String role,
    	                                  @Param("kw") String keyword,
    	                                  @Param("status") Boolean status,
                                          @Param("branchId") Long branchId,
    	                                  Pageable pageable);
    
    // THÊM MỚI: Lấy danh sách staff/admin active cho dropdown
    List<User> findByRoleInAndIsActiveTrue(List<String> roles);

}
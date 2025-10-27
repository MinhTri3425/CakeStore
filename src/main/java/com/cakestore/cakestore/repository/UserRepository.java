package com.cakestore.cakestore.repository;

import com.cakestore.cakestore.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Tìm kiếm + lọc trạng thái (status=null => không lọc)
    @Query("""
        SELECT u FROM User u
        WHERE (:status IS NULL OR u.isActive = :status)
          AND (
            :kw = '' OR
            LOWER(u.email)    LIKE LOWER(CONCAT('%', :kw, '%')) OR
            LOWER(u.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) OR
            u.phone           LIKE CONCAT('%', :kw, '%')
          )
        """)
    Page<User> searchAllWithStatus(@Param("kw") String keyword,
                                   @Param("status") Boolean status,
                                   Pageable pageable);

    // Tìm kiếm + lọc role + lọc trạng thái
    @Query("""
        SELECT u FROM User u
        WHERE u.role = :role
          AND (:status IS NULL OR u.isActive = :status)
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
                                      Pageable pageable);
}
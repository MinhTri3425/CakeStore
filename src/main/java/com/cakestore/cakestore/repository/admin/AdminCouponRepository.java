package com.cakestore.cakestore.repository.admin;

import com.cakestore.cakestore.entity.Coupon;
import com.cakestore.cakestore.entity.Coupon.Type;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminCouponRepository extends JpaRepository<Coupon, Long> {
    boolean existsByCode(String code);

    @EntityGraph(attributePaths = { "branch" }) // để hiển thị branch.code không bị lazy nổ
    @Query("""
            select c from Coupon c
            where (:q is null or lower(c.code) like lower(concat('%', :q, '%')))
              and (:type is null or c.type = :type)
              and (:active is null or c.isActive = :active)
              and (:branchId is null or (c.branch is not null and c.branch.id = :branchId))
            """)
    Page<Coupon> search(
            @Param("q") String q,
            @Param("type") Type type,
            @Param("active") Boolean active,
            @Param("branchId") Long branchId,
            Pageable pageable);

    boolean existsByBranch_Id(Long branchId);
}
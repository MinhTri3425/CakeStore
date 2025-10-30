package com.cakestore.cakestore.repository.admin;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.Order.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface AdminOrderRepository extends JpaRepository<Order, Long> {

        // --- Phân trang có EntityGraph ---
        @EntityGraph(attributePaths = { "user", "branch" })
        Page<Order> findAll(Pageable pageable);

        @EntityGraph(attributePaths = { "user", "branch" })
        Page<Order> findByStatus(OrderStatus status, Pageable pageable);

        @EntityGraph(attributePaths = { "user", "branch" })
        Page<Order> findByBranchId(Long branchId, Pageable pageable);

        @EntityGraph(attributePaths = { "user", "branch" })
        Page<Order> findByStatusAndBranchId(OrderStatus status, Long branchId, Pageable pageable);

        // --- Không phân trang, join fetch ---
        @Query("""
                            SELECT DISTINCT o FROM Order o
                            LEFT JOIN FETCH o.user
                            LEFT JOIN FETCH o.branch
                            WHERE (:status IS NULL OR o.status = :status)
                            ORDER BY o.createdAt DESC
                        """)
        List<Order> findOrdersWithUserAndBranch(@Param("status") OrderStatus status);

        @Query("""
                            SELECT o FROM Order o
                            LEFT JOIN FETCH o.items i
                            LEFT JOIN FETCH i.product
                            WHERE o.id = :id
                        """)
        Optional<Order> findByIdWithItems(@Param("id") Long id);

        // --- Thống kê giữ nguyên ---
        @Query("""
                            SELECT SUM(o.total) FROM Order o
                            WHERE o.status IN :statuses
                            AND o.createdAt BETWEEN :start AND :end
                        """)
        Optional<BigDecimal> sumTotalByStatusInAndCreatedAtBetween(
                        @Param("statuses") Collection<OrderStatus> statuses,
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        long countByStatusInAndCreatedAtBetween(
                        Collection<OrderStatus> statuses,
                        LocalDateTime start,
                        LocalDateTime end);

        @Query("""
                            SELECT FUNCTION('CONVERT', VARCHAR(10), o.createdAt, 111) as orderDate, SUM(o.total) as dailyTotal
                            FROM Order o
                            WHERE o.status IN :statuses AND o.createdAt >= :startDate
                            GROUP BY FUNCTION('CONVERT', VARCHAR(10), o.createdAt, 111)
                            ORDER BY orderDate ASC
                        """)
        List<Map<String, Object>> findDailyRevenueSince(
                        @Param("statuses") Collection<OrderStatus> statuses,
                        @Param("startDate") LocalDateTime startDate);

        @Query("""
                            SELECT o.status as status, COUNT(o.id) as count
                            FROM Order o
                            GROUP BY o.status
                            ORDER BY count DESC
                        """)
        List<Map<String, Object>> countOrdersByStatus();
}

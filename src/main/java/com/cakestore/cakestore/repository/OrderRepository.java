package com.cakestore.cakestore.repository;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.Order.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Phương thức để lấy các đơn hàng theo trạng thái cụ thể
	Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    
    @Query("""
    	    SELECT o FROM Order o
    	    LEFT JOIN FETCH o.user
    	    LEFT JOIN FETCH o.branch
    	    WHERE (:status IS NULL OR o.status = :status)
    	""")
    	List<Order> findOrdersWithUserAndBranch(@Param("status") String status, Pageable pageable);
    @Query("""
            SELECT o FROM Order o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.product
            WHERE o.id = :id
        """)
        Optional<Order> findByIdWithItems(@Param("id") Long id);
}
package com.cakestore.cakestore.repository;

import com.cakestore.cakestore.entity.Order;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Phương thức để lấy các đơn hàng theo trạng thái cụ thể
    List<Order> findByStatus(Order.OrderStatus status);
    
    @Query("""
    	    SELECT o FROM Order o
    	    LEFT JOIN FETCH o.user
    	    LEFT JOIN FETCH o.branch
    	    WHERE (:status IS NULL OR o.status = :status)
    	""")
    	List<Order> findOrdersWithUserAndBranch(@Param("status") String status, Pageable pageable);

}
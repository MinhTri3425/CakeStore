// OrderItemRepository.java
package com.cakestore.cakestore.repository.user;

import com.cakestore.cakestore.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
}

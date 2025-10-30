package com.cakestore.cakestore.repository.user;

import com.cakestore.cakestore.entity.Branch;
import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // ---------- CÁC HÀM GỐC (vẫn giữ) ----------
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    Optional<Order> findByIdAndUser(Long id, User user);

    List<Order> findByBranchOrderByCreatedAtDesc(Branch branch);

    // ---------- HÀM MỚI: DÙNG CHO /orders (danh sách lịch sử đơn) ----------
    // ép load luôn branch để Thymeleaf đọc được o.branch.name mà không bị lazy
    @Query("""
                select o
                from Order o
                left join fetch o.branch b
                where o.user.id = :userId
                order by o.createdAt desc
            """)
    List<Order> findAllByUserIdFetchBranch(@Param("userId") Long userId);

    // ---------- HÀM MỚI: DÙNG CHO /orders/{id} (chi tiết đơn) ----------
    // ép load:
    // - branch (chi nhánh xử lý)
    // - address (địa chỉ giao hàng snapshot)
    // - items (các sản phẩm trong đơn)
    // - product và variant để show tên sp, v.v.
    @Query("""
                select distinct o
                from Order o
                left join fetch o.branch b
                left join fetch o.address a
                left join fetch o.items it
                left join fetch it.product p
                left join fetch it.variant v
                where o.id = :orderId
                  and o.user.id = :userId
            """)
    Optional<Order> findByIdAndUserIdFetchAll(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId);
}

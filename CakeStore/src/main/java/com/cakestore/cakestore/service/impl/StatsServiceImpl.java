// CakeStore/src/main/java/com/cakestore/cakestore/service/impl/StatsServiceImpl.java
package com.cakestore.cakestore.service.impl;

import com.cakestore.cakestore.repository.OrderRepository;
import com.cakestore.cakestore.repository.ProductRepository;
import com.cakestore.cakestore.repository.UserRepository;
import com.cakestore.cakestore.service.StatsService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class StatsServiceImpl implements StatsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // Các Repository được inject
    public StatsServiceImpl(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Map<String, Object> getDashboardStats() {
        // *Chú ý: Dữ liệu mock/placeholder - Cần viết query tối ưu cho production*

        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();

        // Giả lập doanh thu và đơn hàng mới
        BigDecimal todayRevenue = BigDecimal.valueOf(new Random().nextInt(40000000) + 10000000); // 10tr - 50tr VND
        int newOrders = new Random().nextInt(20) + 5; // 5 - 25 đơn mới

        return Map.of(
                "totalProducts", totalProducts,
                "totalUsers", totalUsers,
                "todayRevenue", todayRevenue,
                "newOrders", newOrders
        );
    }

    @Override
    public Map<String, Object> getDetailedStats() {
        // *Chú ý: MOCK DATA cho biểu đồ doanh thu 7 ngày qua*
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("revenueLabels", new String[]{"T2", "T3", "T4", "T5", "T6", "T7", "CN"});
        stats.put("revenueData", new int[]{150, 200, 180, 250, 300, 220, 350}); // Triệu VND

        return stats;
    }
}
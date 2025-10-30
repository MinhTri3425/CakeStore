package com.cakestore.cakestore.service.admin.impl;

import com.cakestore.cakestore.entity.Order;
import com.cakestore.cakestore.entity.Order.OrderStatus;
import com.cakestore.cakestore.repository.admin.AdminOrderItemRepository;
import com.cakestore.cakestore.repository.admin.AdminOrderRepository;
import com.cakestore.cakestore.repository.admin.AdminProductRepository;
import com.cakestore.cakestore.repository.admin.AdminUserRepository;
import com.cakestore.cakestore.service.admin.StatsService;

import org.springframework.data.domain.PageRequest; // Import PageRequest
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*; // Import Arrays, Collections
import java.util.stream.Collectors;

@Service
public class StatsServiceImpl implements StatsService {

        private final AdminOrderRepository orderRepository;
        private final AdminProductRepository productRepository;
        private final AdminUserRepository userRepository;
        private final AdminOrderItemRepository orderItemRepository;

        public StatsServiceImpl(AdminOrderRepository orderRepository, AdminProductRepository productRepository,
                        AdminUserRepository userRepository, AdminOrderItemRepository orderItemRepository) { // Cập nhật
                                                                                                            // constructor
                this.orderRepository = orderRepository;
                this.productRepository = productRepository;
                this.userRepository = userRepository;
                this.orderItemRepository = orderItemRepository; // Gán repository
        }

        @Override
        public Map<String, Object> getDashboardStats() {
                // Lấy tổng số sản phẩm (đã đúng)
                long totalProducts = productRepository.count();
                // Lấy tổng số user (ví dụ, có thể không cần hiển thị)
                // long totalUsers = userRepository.count();

                // Tính toán thời gian bắt đầu và kết thúc của ngày hôm nay
                LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
                LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

                // Tính tổng doanh thu hôm nay (chỉ tính đơn hàng đã giao hoặc đang giao?)
                // Thay đổi logic status tùy theo yêu cầu (ví dụ: chỉ tính DELIVERED)
                List<OrderStatus> revenueStatuses = List.of(OrderStatus.DELIVERED, OrderStatus.SHIPPING,
                                OrderStatus.CONFIRMED);
                BigDecimal todayRevenue = orderRepository.sumTotalByStatusInAndCreatedAtBetween(
                                revenueStatuses, startOfDay, endOfDay)
                                .orElse(BigDecimal.ZERO); // Trả về 0 nếu không có đơn hàng nào

                // Đếm đơn hàng mới hôm nay (ví dụ: trạng thái NEW hoặc CONFIRMED)
                List<OrderStatus> newOrderStatuses = List.of(OrderStatus.NEW, OrderStatus.CONFIRMED);
                long newOrders = orderRepository.countByStatusInAndCreatedAtBetween(
                                newOrderStatuses, startOfDay, endOfDay);

                // Trả về Map chứa dữ liệu thật
                return Map.of(
                                "totalProducts", totalProducts,
                                // "totalUsers", totalUsers, // Bỏ đi nếu không cần
                                "todayRevenue", todayRevenue,
                                "newOrders", newOrders);
        }

        @Override
        public Map<String, Object> getDetailedStats() {
                Map<String, Object> stats = new HashMap<>();

                // --- Doanh thu 7 ngày qua --- (Giữ nguyên logic đã làm)
                LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();
                List<OrderStatus> revenueStatuses = List.of(OrderStatus.DELIVERED, OrderStatus.SHIPPING,
                                OrderStatus.CONFIRMED);
                List<Map<String, Object>> dailyRevenueRaw = orderRepository.findDailyRevenueSince(revenueStatuses,
                                sevenDaysAgo);
                Map<LocalDate, BigDecimal> revenueByDate = dailyRevenueRaw.stream()
                                .collect(Collectors.toMap(
                                                row -> LocalDate.parse((String) row.get("orderDate"),
                                                                DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                                                row -> (BigDecimal) row.get("dailyTotal"),
                                                BigDecimal::add));
                List<String> revenueLabels = new ArrayList<>();
                List<BigDecimal> revenueData = new ArrayList<>();
                LocalDate date = LocalDate.now().minusDays(6);
                for (int i = 0; i < 7; i++) {
                        revenueLabels.add(formatDayOfWeekVietnamese(date.getDayOfWeek()));
                        BigDecimal dailyTotal = revenueByDate.getOrDefault(date, BigDecimal.ZERO);
                        revenueData.add(dailyTotal.divide(BigDecimal.valueOf(1000000), 2, RoundingMode.HALF_UP));
                        date = date.plusDays(1);
                }
                stats.put("revenueLabels", revenueLabels);
                stats.put("revenueData", revenueData);

                // --- Top 5 Sản phẩm bán chạy ---
                Pageable topFive = PageRequest.of(0, 5); // Lấy 5 sản phẩm đầu
                List<Map<String, Object>> topProductsRaw = orderItemRepository.findTopSellingProducts(topFive);

                List<String> topProductLabels = topProductsRaw.stream()
                                .map(row -> (String) row.get("productName"))
                                .collect(Collectors.toList());
                List<Long> topProductData = topProductsRaw.stream()
                                .map(row -> (Long) row.get("totalQuantity"))
                                .collect(Collectors.toList());

                stats.put("topProductLabels", topProductLabels);
                stats.put("topProductData", topProductData);

                // --- Tỉ lệ Đơn hàng theo Trạng thái ---
                List<Map<String, Object>> orderStatusCountsRaw = orderRepository.countOrdersByStatus();
                long totalOrders = orderStatusCountsRaw.stream().mapToLong(row -> (Long) row.get("count")).sum(); // Tính
                                                                                                                  // tổng
                                                                                                                  // số
                                                                                                                  // đơn

                List<String> orderStatusLabels = new ArrayList<>();
                List<Double> orderStatusData = new ArrayList<>(); // Dùng Double cho tỷ lệ %

                // Sắp xếp trạng thái theo thứ tự mong muốn (ví dụ)
                List<OrderStatus> orderedStatuses = Arrays.asList(
                                OrderStatus.NEW, OrderStatus.CONFIRMED, OrderStatus.SHIPPING,
                                OrderStatus.DELIVERED, OrderStatus.CANCELED, OrderStatus.RETURNED);

                Map<OrderStatus, Long> countsByStatus = orderStatusCountsRaw.stream()
                                .collect(Collectors.toMap(
                                                row -> (OrderStatus) row.get("status"),
                                                row -> (Long) row.get("count")));

                for (OrderStatus status : orderedStatuses) {
                        long count = countsByStatus.getOrDefault(status, 0L);
                        if (count > 0 || totalOrders > 0) { // Chỉ thêm nếu có hoặc để tính % 0
                                orderStatusLabels.add(status.name()); // Hoặc getDb() nếu muốn hiển thị khác
                                double percentage = (totalOrders == 0) ? 0.0 : ((double) count / totalOrders) * 100.0;
                                orderStatusData.add(Math.round(percentage * 10.0) / 10.0); // Làm tròn 1 chữ số thập
                                                                                           // phân
                        }
                }

                stats.put("orderStatusLabels", orderStatusLabels);
                stats.put("orderStatusData", orderStatusData); // Dữ liệu là tỷ lệ %

                return stats;
        }

        private String formatDayOfWeekVietnamese(DayOfWeek dayOfWeek) {
                return switch (dayOfWeek) {
                        case MONDAY -> "T2";
                        case TUESDAY -> "T3";
                        case WEDNESDAY -> "T4";
                        case THURSDAY -> "T5";
                        case FRIDAY -> "T6";
                        case SATURDAY -> "T7";
                        case SUNDAY -> "CN";
                };
        }
}
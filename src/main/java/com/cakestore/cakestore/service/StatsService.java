
package com.cakestore.cakestore.service;

import java.math.BigDecimal;
import java.util.Map;

public interface StatsService {
    
    // Lấy tổng quan: Doanh thu, đơn hàng mới, tổng sản phẩm
    Map<String, Object> getDashboardStats();

    // Lấy dữ liệu thống kê chi tiết cho trang /admin/stats (ví dụ: Doanh thu theo tháng)
    Map<String, Object> getDetailedStats();
}
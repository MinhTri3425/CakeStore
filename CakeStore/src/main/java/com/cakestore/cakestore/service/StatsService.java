// src/main/java/com/cakestore/cakestore/service/StatsService.java
package com.cakestore.cakestore.service;

import java.util.Map;

public interface StatsService {
    // Lấy tổng quan: Doanh thu, đơn hàng mới, tổng sản phẩm
    Map<String, Object> getDashboardStats();

    // Lấy dữ liệu thống kê chi tiết cho trang /admin/stats
    Map<String, Object> getDetailedStats();
}
package com.cakestore.cakestore.dto;

import java.math.BigDecimal;

public record ProductCardDto(
        Long id,
        String name,
        String slug,
        BigDecimal price,
        String thumbnailUrl, // ảnh đầu tiên (nullable)
        Integer soldCount, // từ ProductStats (nullable)
        java.math.BigDecimal ratingAvg,
        Integer ratingCount) {
}

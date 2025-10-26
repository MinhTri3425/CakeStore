package com.cakestore.cakestore.dto.user;

import java.math.BigDecimal;
import java.util.List;

public record ProductDetailDto(
        Long id,
        String name,
        String slug,
        BigDecimal price,
        String shortDesc,
        String description,
        List<String> images,
        List<VariantDto> variants,
        BigDecimal ratingAvg,
        Integer ratingCount) {
    public record VariantDto(Long id, String attrName, String attrValue, BigDecimal priceAdj) {
    }
}

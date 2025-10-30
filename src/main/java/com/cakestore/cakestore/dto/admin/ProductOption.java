package com.cakestore.cakestore.dto.admin;
import java.math.BigDecimal;

public record ProductOption(Long id, String name, String sku, BigDecimal price) {}
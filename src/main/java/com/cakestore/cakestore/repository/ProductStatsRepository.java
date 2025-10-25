package com.cakestore.cakestore.repository;

import com.cakestore.cakestore.entity.ProductStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductStatsRepository extends JpaRepository<ProductStats, Long> {
    // Id = ProductId (PK chia sẻ)
}

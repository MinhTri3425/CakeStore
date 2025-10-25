package com.cakestore.cakestore.repository;

import com.cakestore.cakestore.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;
import java.util.List;

public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByIsActiveTrueOrderBySortOrderAscIdAsc();
}
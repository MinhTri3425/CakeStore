package com.cakestore.cakestore.service;

import com.cakestore.cakestore.dto.ProductCardDto;
import com.cakestore.cakestore.dto.ProductDetailDto;
import com.cakestore.cakestore.entity.Product;
import com.cakestore.cakestore.entity.ProductStats;
import com.cakestore.cakestore.mapper.ProductMapper;
import com.cakestore.cakestore.repository.ProductRepository;
import com.cakestore.cakestore.repository.ProductStatsRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductQueryService {

    private final ProductRepository productRepo;
    private final ProductStatsRepository statsRepo;

    public ProductQueryService(ProductRepository p, ProductStatsRepository s) {
        this.productRepo = p;
        this.statsRepo = s;
    }

    /** Home: top bán chạy (>10) lấy 12 sp */
    public List<ProductCardDto> homeTopSelling() {
        List<Product> lst = productRepo.topSellingOver10(PageRequest.of(0, 12));
        return lst.stream()
                .map(p -> ProductMapper.toCard(p, statsRepo.findById(p.getId()).orElse(null)))
                .toList();
    }

    /** Trang category/search: trả Page<ProductCardDto> */
    public Page<ProductCardDto> search(Long categoryId, String q, int page, int size) {
        Page<Product> data = productRepo.search(categoryId, q, PageRequest.of(page, size));
        return data.map(p -> ProductMapper.toCard(p, statsRepo.findById(p.getId()).orElse(null)));
    }

    /** Chi tiết sản phẩm */
    public ProductDetailDto detail(Long id) {
        Product p = productRepo.findById(id).orElseThrow();
        ProductStats s = statsRepo.findById(id).orElse(null);
        return ProductMapper.toDetail(p, s);
    }

    public Page<ProductCardDto> search(Long categoryId, String q, int page, int size, String sort) {
        Sort s = switch (sort == null ? "newest" : sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "sold_desc" -> Sort.by(Sort.Order.desc("productStats.soldCount"), Sort.Order.desc("id"));
            case "rating_desc" -> Sort.by(Sort.Order.desc("productStats.ratingAvg"), Sort.Order.desc("id"));
            default -> Sort.by("id").descending(); // newest
        };
        Page<Product> data = productRepo.searchAll(categoryId, q, PageRequest.of(page, size, s));
        return data.map(p -> ProductMapper.toCard(p, statsRepo.findById(p.getId()).orElse(null)));
    }
}

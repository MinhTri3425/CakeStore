package com.cakestore.cakestore.service.user;

import com.cakestore.cakestore.dto.user.ProductCardDto;
import com.cakestore.cakestore.dto.user.ProductDetailDto;
import com.cakestore.cakestore.entity.Product;
import com.cakestore.cakestore.entity.ProductStats;
import com.cakestore.cakestore.mapper.ProductMapper;
import com.cakestore.cakestore.repository.user.ProductRepository;
import com.cakestore.cakestore.repository.user.ProductStatsRepository;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductQueryService {

    private final ProductRepository productRepo;
    private final ProductStatsRepository statsRepo;

    public ProductQueryService(ProductRepository p, ProductStatsRepository s) {
        this.productRepo = p;
        this.statsRepo = s;
    }

    /** Home: top bán chạy (>10) lấy 12 sp */
    @Transactional
    public List<ProductCardDto> homeTopSelling() {
        List<Product> lst = productRepo.topSellingOver10(PageRequest.of(0, 12));
        Map<Long, ProductStats> statsMap = loadStatsMap(lst);

        return lst.stream()
                .map(p -> ProductMapper.toCard(p, statsMap.get(p.getId())))
                .toList();
    }

    /** Trang category/search: trả Page<ProductCardDto> */
    @Transactional
    public Page<ProductCardDto> search(Long categoryId, String q, int page, int size) {
        Page<Product> data = productRepo.search(categoryId, q, PageRequest.of(page, size));
        Map<Long, ProductStats> statsMap = loadStatsMap(data.getContent());

        return data.map(p -> ProductMapper.toCard(p, statsMap.get(p.getId())));
    }

    /** Chi tiết sản phẩm */
    @Transactional
    public ProductDetailDto detail(Long id) {
        Product p = productRepo.findByIdWithImages(id).orElseThrow();
        ProductStats s = statsRepo.findById(id).orElse(null);
        return ProductMapper.toDetail(p, s);
    }

    @Transactional
    public Page<ProductCardDto> search(Long categoryId, String q, int page, int size, String sort) {
        Sort s = switch (sort == null ? "newest" : sort) {
            case "price_asc" -> Sort.by("price").ascending();
            case "price_desc" -> Sort.by("price").descending();
            case "sold_desc" -> Sort.by(Sort.Order.desc("stats.soldCount"), Sort.Order.desc("id"));
            case "rating_desc" -> Sort.by(Sort.Order.desc("stats.ratingAvg"), Sort.Order.desc("id"));
            default -> Sort.by("id").descending(); // newest
        };
        Page<Product> data = productRepo.searchAll(categoryId, q, PageRequest.of(page, size, s));
        Map<Long, ProductStats> statsMap = loadStatsMap(data.getContent());

        return data.map(p -> ProductMapper.toCard(p, statsMap.get(p.getId())));
    }

    private Map<Long, ProductStats> loadStatsMap(List<Product> products) {
        if (products.isEmpty())
            return Collections.emptyMap();
        List<Long> ids = products.stream().map(Product::getId).toList();
        return statsRepo.findAllById(ids).stream()
                // giả định id của ProductStats == product_id
                .collect(Collectors.toMap(ProductStats::getProductId, Function.identity()));
    }
}

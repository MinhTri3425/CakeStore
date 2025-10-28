package com.cakestore.cakestore.service.user;

import com.cakestore.cakestore.dto.user.ProductCardDto;
import com.cakestore.cakestore.dto.user.ProductDetailDto;
import com.cakestore.cakestore.entity.Product;
import com.cakestore.cakestore.entity.ProductStats;
import com.cakestore.cakestore.mapper.ProductMapper;
import com.cakestore.cakestore.repository.user.BranchInventoryRepository;
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
    private final BranchInventoryRepository branchInvRepo;

    public ProductQueryService(
            ProductRepository p,
            ProductStatsRepository s,
            BranchInventoryRepository branchInvRepo) {
        this.productRepo = p;
        this.statsRepo = s;
        this.branchInvRepo = branchInvRepo;
    }

    // =====================
    // TOP BÁN CHẠY (12 sp)
    // =====================
    @Transactional
    public List<ProductCardDto> homeTopSelling() {
        List<Product> lst = productRepo.topSellingOver10(PageRequest.of(0, 12));
        Map<Long, ProductStats> statsMap = loadStatsMap(lst);

        return lst.stream()
                .map(p -> ProductMapper.toCard(p, statsMap.get(p.getId())))
                .toList();
    }

    // =====================
    // TOP BÁN CHẠY (24 sp)
    // =====================
    @Transactional
    public List<ProductCardDto> homeTopSelling20() {
        List<Product> lst = productRepo.topSellingOver10(PageRequest.of(0, 24));
        Map<Long, ProductStats> statsMap = loadStatsMap(lst);

        return lst.stream()
                .map(p -> ProductMapper.toCard(p, statsMap.get(p.getId())))
                .toList();
    }

    // =========================================================
    // TOP BÁN CHẠY THEO CHI NHÁNH (lọc chỉ còn hàng ở branch)
    // limit24 = false => 12 sp
    // limit24 = true => 24 sp
    // =========================================================
    @Transactional
    public List<ProductCardDto> homeTopSellingInBranch(Long branchId, boolean limit24) {

        // lấy list gốc
        List<ProductCardDto> base = limit24
                ? homeTopSelling20()
                : homeTopSelling();

        // nếu chưa có chi nhánh (khách chưa chọn) -> trả nguyên danh sách
        if (branchId == null) {
            return base;
        }

        // hỏi kho: product nào còn hàng (>0 available) tại branch này
        List<Long> inStockIds = branchInvRepo.findInStockProductIdsByBranch(branchId);
        if (inStockIds == null || inStockIds.isEmpty()) {
            // chi nhánh này hiện không còn món nào trong list top
            return List.of();
        }
        Set<Long> allow = new HashSet<>(inStockIds);

        // lọc: chỉ giữ những dto có id nằm trong allow
        return base.stream()
                .filter(dto -> dto.id() != null && allow.contains(dto.id()))
                .toList();
    }

    // =====================================================
    // SEARCH / CATEGORY (Page<ProductCardDto>) không sort
    // =====================================================
    @Transactional
    public Page<ProductCardDto> search(Long categoryId, String q, int page, int size) {
        Page<Product> data = productRepo.search(categoryId, q, PageRequest.of(page, size));
        Map<Long, ProductStats> statsMap = loadStatsMap(data.getContent());

        return data.map(p -> ProductMapper.toCard(p, statsMap.get(p.getId())));
    }

    // =====================================================
    // SEARCH / CATEGORY có sort
    // =====================================================
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

    // =====================================================
    // SEARCH / CATEGORY THEO CHI NHÁNH
    // sau khi có page gốc -> filter theo hàng còn trong branch
    // =====================================================
    @Transactional
    public Page<ProductCardDto> searchInBranch(
            Long branchId,
            Long categoryId,
            String q,
            int page,
            int size,
            String sort) {
        // lấy page gốc (đã sort)
        Page<ProductCardDto> basePage = search(categoryId, q, page, size, sort);

        // nếu chưa chọn branch -> trả nguyên page
        if (branchId == null) {
            return basePage;
        }

        // lấy danh sách ID sản phẩm còn hàng tại branch
        List<Long> inStockIds = branchInvRepo.findInStockProductIdsByBranch(branchId);
        if (inStockIds == null || inStockIds.isEmpty()) {
            // branch không còn gì -> trả Page rỗng
            return Page.empty(basePage.getPageable());
        }
        Set<Long> allow = new HashSet<>(inStockIds);

        // lọc content theo tồn kho
        List<ProductCardDto> filteredContent = basePage.getContent().stream()
                .filter(dto -> dto.id() != null && allow.contains(dto.id()))
                .toList();

        // trả PageImpl mới với content đã lọc
        return new PageImpl<>(
                filteredContent,
                basePage.getPageable(),
                filteredContent.size());
    }

    // =====================
    // CHI TIẾT SẢN PHẨM
    // =====================
    @Transactional
    public ProductDetailDto detail(Long id) {
        Product p = productRepo.findByIdWithImages(id).orElseThrow();
        ProductStats s = statsRepo.findById(id).orElse(null);
        return ProductMapper.toDetail(p, s);
    }

    // =====================
    // HELPERS
    // =====================
    private Map<Long, ProductStats> loadStatsMap(List<Product> products) {
        if (products.isEmpty())
            return Collections.emptyMap();

        List<Long> ids = products.stream()
                .map(Product::getId)
                .toList();

        return statsRepo.findAllById(ids).stream()
                // giả định ProductStats.productId == Product.id
                .collect(Collectors.toMap(
                        ProductStats::getProductId,
                        Function.identity()));
    }
}

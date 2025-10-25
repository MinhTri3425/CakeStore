package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(name = "ProductStats")
public class ProductStats {

    @Id
    @Column(name = "ProductId")
    private Long productId; // PK đồng thời là FK tới Products

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId // dùng cùng khóa với product
    @JoinColumn(name = "ProductId")
    private Product product;

    @Column(name = "SoldCount", nullable = false)
    private Integer soldCount = 0;

    @Column(name = "ViewCount", nullable = false)
    private Integer viewCount = 0;

    // DECIMAL(3,2) — giới hạn 0..5
    @Column(name = "RatingAvg", nullable = false, precision = 3, scale = 2)
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "RatingCount", nullable = false)
    private Integer ratingCount = 0;

    public ProductStats() {
    }

    public ProductStats(Product product) {
        this.product = product;
        this.productId = (product != null ? product.getId() : null);
    }

    // ===== Getters/Setters =====
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
        this.productId = (product != null ? product.getId() : null);
    }

    public Integer getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(Integer soldCount) {
        this.soldCount = (soldCount == null ? 0 : soldCount);
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = (viewCount == null ? 0 : viewCount);
    }

    public BigDecimal getRatingAvg() {
        return ratingAvg;
    }

    public void setRatingAvg(BigDecimal ratingAvg) {
        this.ratingAvg = ratingAvg == null ? BigDecimal.ZERO : ratingAvg;
    }

    public Integer getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = (ratingCount == null ? 0 : ratingCount);
    }

    // ===== Helpers nghiệp vụ gọn =====

    /** +1 view */
    public void incView() {
        this.viewCount = (this.viewCount == null ? 1 : this.viewCount + 1);
    }

    /** +qty đơn vị bán (>=1) */
    public void addSold(int qty) {
        if (qty <= 0)
            return;
        this.soldCount = (this.soldCount == null ? qty : this.soldCount + qty);
    }

    /** Thêm 1 đánh giá (1..5), cập nhật trung bình */
    public void addRating(int stars) {
        if (stars < 1 || stars > 5)
            return;
        int n = (this.ratingCount == null ? 0 : this.ratingCount);
        BigDecimal avg = this.ratingAvg == null ? BigDecimal.ZERO : this.ratingAvg;

        BigDecimal total = avg.multiply(BigDecimal.valueOf(n)).add(BigDecimal.valueOf(stars));
        int newCount = n + 1;
        BigDecimal newAvg = total.divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

        // clamp 0..5 để khớp CHECK trong DB
        if (newAvg.compareTo(BigDecimal.ZERO) < 0)
            newAvg = BigDecimal.ZERO;
        if (newAvg.compareTo(BigDecimal.valueOf(5)) > 0)
            newAvg = BigDecimal.valueOf(5);

        this.ratingAvg = newAvg;
        this.ratingCount = newCount;
    }

    // ===== equals/hashCode theo PK =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProductStats that))
            return false;
        return productId != null && productId.equals(that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(productId);
    }
}

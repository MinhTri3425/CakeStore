package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "PromotionItems", indexes = {
        @Index(name = "IX_PromotionItems_Promotion", columnList = "PromotionId"),
        @Index(name = "IX_PromotionItems_Product", columnList = "ProductId"),
        @Index(name = "IX_PromotionItems_Category", columnList = "CategoryId")
})
public class PromotionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    // bắt buộc
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PromotionId", nullable = false)
    private Promotion promotion;

    // tùy chọn: áp cho 1 sản phẩm cụ thể
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ProductId")
    private Product product;

    // tùy chọn: áp cho cả danh mục
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CategoryId")
    private Category category;

    public PromotionItem() {
    }

    public PromotionItem(Promotion promotion) {
        this.promotion = promotion;
    }

    public PromotionItem(Promotion promotion, Product product) {
        this.promotion = promotion;
        this.product = product;
    }

    public PromotionItem(Promotion promotion, Category category) {
        this.promotion = promotion;
        this.category = category;
    }

    // ===== Getters/Setters =====
    public Long getId() {
        return id;
    }

    public Promotion getPromotion() {
        return promotion;
    }

    public void setPromotion(Promotion promotion) {
        this.promotion = promotion;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    // ===== equals/hashCode theo Id =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PromotionItem that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

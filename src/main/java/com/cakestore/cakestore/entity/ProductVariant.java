package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "ProductVariants", indexes = {
        @Index(name = "IX_ProductVariants_Product", columnList = "ProductId")
})
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    @Column(name = "AttrName", nullable = false, length = 50)
    private String attrName; // ví dụ: Size, Flavor

    @Column(name = "AttrValue", nullable = false, length = 50)
    private String attrValue; // ví dụ: 6 inch, Chocolate

    @Column(name = "PriceAdj", precision = 12, scale = 2)
    private BigDecimal priceAdj; // có thể âm/dương hoặc null

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // ===== Constructors =====
    public ProductVariant() {
    }

    public ProductVariant(Product product, String attrName, String attrValue, BigDecimal priceAdj) {
        this.product = product;
        this.attrName = attrName;
        this.attrValue = attrValue;
        this.priceAdj = priceAdj;
    }

    // ===== Getters/Setters =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public String getAttrValue() {
        return attrValue;
    }

    public void setAttrValue(String attrValue) {
        this.attrValue = attrValue;
    }

    public BigDecimal getPriceAdj() {
        return priceAdj;
    }

    public void setPriceAdj(BigDecimal priceAdj) {
        this.priceAdj = priceAdj;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ===== equals/hashCode theo Id =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ProductVariant other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

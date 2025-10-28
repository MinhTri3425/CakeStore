package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Products", indexes = {
        @Index(name = "IX_Products_Category", columnList = "CategoryId")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UQ_Products_Sku", columnNames = "Sku"),
        @UniqueConstraint(name = "UQ_Products_Slug", columnNames = "Slug")
})
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Sku", nullable = false, length = 64, columnDefinition = "NVARCHAR(64)")
    private String sku;

    @Column(name = "Name", nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String name;

    @Column(name = "Slug", nullable = false, length = 150, unique = true, columnDefinition = "NVARCHAR(150)")
    private String slug;

    @Column(name = "ShortDesc", length = 500, columnDefinition = "NVARCHAR(500)")
    private String shortDesc;

    @Lob
    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    // many products -> one category
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CategoryId", nullable = false)
    private Category category;

    @Column(name = "Price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "CompareAtPrice", precision = 12, scale = 2)
    private BigDecimal compareAtPrice;

    /** 1 = active, 0 = hidden (global) */
    @Column(name = "Status", nullable = false)
    private Integer status = 1;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /* ================= Relations (lazy) ================ */

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductImage> images = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductVariant> variants = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BranchProduct> branchProducts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BranchInventory> branchInventory = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<Review> reviews = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<Favorite> favorites = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<RecentlyViewed> recentlyViewed = new LinkedHashSet<>();

    @OneToOne(mappedBy = "product", fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = true)
    private ProductStats productStats;

    /* ================= Constructors ==================== */

    public Product() {
    }

    public Product(String sku, String name, String slug, Category category, BigDecimal price) {
        this.sku = sku;
        this.name = name;
        this.slug = slug;
        this.category = category;
        this.price = price;
    }

    /* ================= Getters/Setters ================= */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc(String shortDesc) {
        this.shortDesc = shortDesc;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCompareAtPrice() {
        return compareAtPrice;
    }

    public void setCompareAtPrice(BigDecimal compareAtPrice) {
        this.compareAtPrice = compareAtPrice;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Set<ProductImage> getImages() {
        return images;
    }

    public void setImages(Set<ProductImage> images) {
        this.images = images;
    }

    public Set<ProductVariant> getVariants() {
        return variants;
    }

    public void setVariants(Set<ProductVariant> variants) {
        this.variants = variants;
    }

    public Set<BranchProduct> getBranchProducts() {
        return branchProducts;
    }

    public void setBranchProducts(Set<BranchProduct> branchProducts) {
        this.branchProducts = branchProducts;
    }

    public Set<BranchInventory> getBranchInventory() {
        return branchInventory;
    }

    public void setBranchInventory(Set<BranchInventory> branchInventory) {
        this.branchInventory = branchInventory;
    }

    public Set<Review> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Review> reviews) {
        this.reviews = reviews;
    }

    public Set<Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(Set<Favorite> favorites) {
        this.favorites = favorites;
    }

    public Set<RecentlyViewed> getRecentlyViewed() {
        return recentlyViewed;
    }

    public void setRecentlyViewed(Set<RecentlyViewed> recentlyViewed) {
        this.recentlyViewed = recentlyViewed;
    }

    public ProductStats getProductStats() {
        return productStats;
    }

    public void setProductStats(ProductStats productStats) {
        this.productStats = productStats;
    }

    /* ================= Helpers ========================= */

    public void addImage(ProductImage img) {
        if (img == null)
            return;
        img.setProduct(this);
        this.images.add(img);
    }

    public void addVariant(ProductVariant v) {
        if (v == null)
            return;
        v.setProduct(this);
        this.variants.add(v);
    }

    /* equals/hashCode theo Id để dùng trong Set */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Product other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

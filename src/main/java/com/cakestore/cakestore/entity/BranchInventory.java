package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "BranchInventory", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_BranchInventory", columnNames = { "BranchId", "ProductId" })
}, indexes = {
        @Index(name = "IX_BranchInventory_Branch", columnList = "BranchId"),
        @Index(name = "IX_BranchInventory_Product", columnList = "ProductId")
})
public class BranchInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BranchId", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity = 0; // tổng tồn

    @Column(name = "Reserved", nullable = false)
    private Integer reserved = 0; // đã giữ chỗ (đơn chưa giao)

    @Column(name = "UpdatedAt", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public BranchInventory() {
    }

    public BranchInventory(Branch branch, Product product, Integer quantity) {
        this.branch = branch;
        this.product = product;
        if (quantity != null)
            this.quantity = quantity;
    }

    // Getters/Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getReserved() {
        return reserved;
    }

    public void setReserved(Integer reserved) {
        this.reserved = reserved;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Helper: tồn khả dụng
    @Transient
    public int getAvailable() {
        return Math.max(0, (quantity == null ? 0 : quantity) - (reserved == null ? 0 : reserved));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BranchInventory other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

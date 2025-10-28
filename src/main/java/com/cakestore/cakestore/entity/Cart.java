package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Carts", indexes = {
        @Index(name = "IX_Carts_User", columnList = "UserId"),
        @Index(name = "IX_Carts_Branch", columnList = "BranchId")
})
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "BranchId", nullable = false)
    private Branch branch;

    @Convert(converter = CartStatus.Converter.class)
    @Column(name = "Status", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private CartStatus status = CartStatus.ACTIVE;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "cart", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> items = new LinkedHashSet<>();

    public Cart() {
    }

    public Cart(User user, Branch branch) {
        this.user = user;
        this.branch = branch;
    }

    // ===== Getters/Setters =====
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public CartStatus getStatus() {
        return status;
    }

    public void setStatus(CartStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Set<CartItem> getItems() {
        return items;
    }

    public void setItems(Set<CartItem> items) {
        this.items = items;
    }

    // Helpers
    public void addItem(CartItem item) {
        if (item == null)
            return;
        item.setCart(this);
        this.items.add(item);
    }

    public void removeItem(CartItem item) {
        if (item == null)
            return;
        item.setCart(null);
        this.items.remove(item);
    }

    // equals/hashCode theo Id
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Cart other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // ===== Enum + Converter để lưu đúng chuỗi trong DB =====
    public enum CartStatus {
        ACTIVE("active"),
        CONVERTED("converted"),
        ABANDONED("abandoned");

        private final String dbValue;

        CartStatus(String v) {
            this.dbValue = v;
        }

        public String getDbValue() {
            return dbValue;
        }

        public static CartStatus fromDb(String v) {
            if (v == null)
                return null;
            for (CartStatus s : values())
                if (s.dbValue.equalsIgnoreCase(v))
                    return s;
            throw new IllegalArgumentException("Unknown CartStatus: " + v);
        }

        @jakarta.persistence.Converter(autoApply = false)
        public static class Converter implements AttributeConverter<CartStatus, String> {
            @Override
            public String convertToDatabaseColumn(CartStatus attribute) {
                return attribute == null ? null : attribute.getDbValue();
            }

            @Override
            public CartStatus convertToEntityAttribute(String dbData) {
                return CartStatus.fromDb(dbData);
            }
        }
    }
}

package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Promotions", indexes = {
        @Index(name = "IX_Promotions_Branch", columnList = "BranchId")
})
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Name", nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String name;

    @Convert(converter = Scope.Converter.class)
    @Column(name = "Scope", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private Scope scope; // APP / PRODUCT

    @Convert(converter = Type.Converter.class)
    @Column(name = "Type", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private Type type; // PERCENT / AMOUNT / SHIPPING_OFF

    @Column(name = "Value", nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Column(name = "StartsAt", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "EndsAt", nullable = false)
    private LocalDateTime endsAt;

    // NULL = áp dụng toàn hệ thống
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BranchId")
    private Branch branch;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive = true;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    /*
     * Quan hệ con: PromotionItems (liệt kê sản phẩm/danh mục áp dụng khi
     * Scope=PRODUCT)
     */
    @OneToMany(mappedBy = "promotion", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PromotionItem> promotionItems = new LinkedHashSet<>();

    public Promotion() {
    }

    public Promotion(String name, Scope scope, Type type, BigDecimal value, LocalDateTime startsAt,
            LocalDateTime endsAt) {
        this.name = name;
        this.scope = scope;
        this.type = type;
        this.value = value;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    // ===== Getters/Setters =====
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public LocalDateTime getStartsAt() {
        return startsAt;
    }

    public void setStartsAt(LocalDateTime startsAt) {
        this.startsAt = startsAt;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Set<PromotionItem> getPromotionItems() {
        return promotionItems;
    }

    public void setPromotionItems(Set<PromotionItem> promotionItems) {
        this.promotionItems = promotionItems;
    }

    // ===== Helper =====
    @Transient
    public boolean isActiveNow(LocalDateTime now) {
        if (!isActive)
            return false;
        if (startsAt != null && now.isBefore(startsAt))
            return false;
        if (endsAt != null && now.isAfter(endsAt))
            return false;
        return true;
    }

    public void addItem(PromotionItem item) {
        if (item == null)
            return;
        item.setPromotion(this);
        this.promotionItems.add(item);
    }

    // ===== equals/hashCode theo Id =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Promotion that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // ===== Enums + Converters (khớp CHECK constraint) =====
    public enum Scope {
        APP("APP"), PRODUCT("PRODUCT");

        private final String db;

        Scope(String db) {
            this.db = db;
        }

        public String getDb() {
            return db;
        }

        public static Scope of(String v) {
            for (var s : values())
                if (s.db.equalsIgnoreCase(v))
                    return s;
            throw new IllegalArgumentException("Unknown Promotion Scope: " + v);
        }

        @jakarta.persistence.Converter(autoApply = false)
        public static class Converter implements AttributeConverter<Scope, String> {
            @Override
            public String convertToDatabaseColumn(Scope a) {
                return a == null ? null : a.getDb();
            }

            @Override
            public Scope convertToEntityAttribute(String v) {
                return v == null ? null : Scope.of(v);
            }
        }
    }

    public enum Type {
        PERCENT("PERCENT"), AMOUNT("AMOUNT"), SHIPPING_OFF("SHIPPING_OFF");

        private final String db;

        Type(String db) {
            this.db = db;
        }

        public String getDb() {
            return db;
        }

        public static Type of(String v) {
            for (var t : values())
                if (t.db.equalsIgnoreCase(v))
                    return t;
            throw new IllegalArgumentException("Unknown Promotion Type: " + v);
        }

        @jakarta.persistence.Converter(autoApply = false)
        public static class Converter implements AttributeConverter<Type, String> {
            @Override
            public String convertToDatabaseColumn(Type a) {
                return a == null ? null : a.getDb();
            }

            @Override
            public Type convertToEntityAttribute(String v) {
                return v == null ? null : Type.of(v);
            }
        }
    }
}

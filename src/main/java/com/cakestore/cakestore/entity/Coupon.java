package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Coupons", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_Coupons_Code", columnNames = "Code")
}, indexes = {
        @Index(name = "IX_Coupons_Branch", columnList = "BranchId")
})
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Code", nullable = false, length = 50, unique = true, columnDefinition = "NVARCHAR(50)")
    private String code;

    @Convert(converter = Type.Converter.class)
    @Column(name = "Type", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private Type type; // PERCENT / AMOUNT / SHIPPING_OFF

    @Column(name = "Value", nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Column(name = "MinSubtotal", precision = 12, scale = 2)
    private BigDecimal minSubtotal;

    @Column(name = "MaxDiscount", precision = 12, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "Quantity", nullable = false)
    private Integer quantity = 0; // số lượt phát hành còn lại

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

    /* ===== Quan hệ ngược ===== */
    @OneToMany(mappedBy = "coupon", fetch = FetchType.LAZY)
    private Set<CouponUsage> couponUsages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "coupon", fetch = FetchType.LAZY)
    private Set<OrderCoupon> orderCoupons = new LinkedHashSet<>();

    /* ===== Constructors ===== */
    public Coupon() {
    }

    public Coupon(String code, Type type, BigDecimal value, LocalDateTime startsAt, LocalDateTime endsAt) {
        this.code = code;
        this.type = type;
        this.value = value;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    /* ===== Getters/Setters ===== */
    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public BigDecimal getMinSubtotal() {
        return minSubtotal;
    }

    public void setMinSubtotal(BigDecimal minSubtotal) {
        this.minSubtotal = minSubtotal;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    public void setMaxDiscount(BigDecimal maxDiscount) {
        this.maxDiscount = maxDiscount;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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

    public Set<CouponUsage> getCouponUsages() {
        return couponUsages;
    }

    public Set<OrderCoupon> getOrderCoupons() {
        return orderCoupons;
    }

    /* ===== Helpers ===== */
    @Transient
    public boolean isActiveNow(LocalDateTime now) {
        if (!isActive)
            return false;
        if (startsAt != null && now.isBefore(startsAt))
            return false;
        if (endsAt != null && now.isAfter(endsAt))
            return false;
        return quantity == null || quantity > 0;
    }

    /** giảm quantity an toàn (>=0) */
    public void decQuantity(int n) {
        if (n <= 0)
            return;
        if (this.quantity == null)
            return;
        this.quantity = Math.max(0, this.quantity - n);
    }

    /* ===== equals/hashCode theo Id ===== */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Coupon that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /* ===== Enum + Converter (khớp CHECK constraint) ===== */
    public enum Type {
        PERCENT("PERCENT"),
        AMOUNT("AMOUNT"),
        SHIPPING_OFF("SHIPPING_OFF");

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
            throw new IllegalArgumentException("Unknown Coupon type: " + v);
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

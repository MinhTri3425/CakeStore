package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "OrderCoupons")
public class OrderCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    // nhiều coupon snapshot -> 1 order
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "OrderId", nullable = false)
    private Order order;

    // tham chiếu coupon gốc có thể NULL (DB: ON DELETE SET NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CouponId")
    private Coupon coupon;

    @Column(name = "CodeSnap", nullable = false, length = 50, columnDefinition = "NVARCHAR(50)")
    private String codeSnap;

    @Column(name = "Value", nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Convert(converter = Type.Converter.class)
    @Column(name = "Type", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private Type type; // PERCENT / AMOUNT / SHIPPING_OFF

    public OrderCoupon() {
    }

    public OrderCoupon(Order order, String codeSnap, BigDecimal value, Type type) {
        this.order = order;
        this.codeSnap = codeSnap;
        this.value = value;
        this.type = type;
    }

    // ===== Getters/Setters =====
    public Long getId() {
        return id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public String getCodeSnap() {
        return codeSnap;
    }

    public void setCodeSnap(String codeSnap) {
        this.codeSnap = codeSnap;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    // ===== equals/hashCode theo Id =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OrderCoupon that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // ===== Enum + Converter (khớp CHECK constraint) =====
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
            throw new IllegalArgumentException("Unknown OrderCoupon type: " + v);
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

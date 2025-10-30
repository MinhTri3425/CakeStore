package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "Orders", indexes = {
        @Index(name = "IX_Orders_User", columnList = "UserId"),
        @Index(name = "IX_Orders_Branch", columnList = "BranchId"),
        @Index(name = "IX_Orders_Address", columnList = "AddressId")
})
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    // ===== FK =====
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "BranchId", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "AddressId")
    private Address address;

    // ===== Money =====
    @Column(name = "Subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "Discount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "ShippingFee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @Column(name = "Total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    // ===== Status/Method =====
    @Convert(converter = PaymentMethod.Converter.class)
    @Column(name = "PaymentMethod", length = 20)
    private PaymentMethod paymentMethod; // COD/VNPAY/MOMO (nullable)

    @Convert(converter = PaymentStatus.Converter.class)
    @Column(name = "PaymentStatus", nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Convert(converter = OrderStatus.Converter.class)
    @Column(name = "Status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.NEW;

    @Column(name = "Note", length = 500)
    private String note;

    // DB tự set
    @CreationTimestamp
    @Column(name = "CreatedAt", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    // ===== Relations (children) =====
    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("Id ASC")
    private Set<OrderItem> items = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PaymentTransaction> payments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Fulfillment> fulfillments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderCoupon> coupons = new LinkedHashSet<>();

    // ===== Constructors =====
    public Order() {
    }

    public Order(User user, Branch branch) {
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

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = nvl(subtotal);
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = nvl(discount);
    }

    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = nvl(shippingFee);
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = nvl(total);
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Set<OrderItem> getItems() {
        return items;
    }

    public void setItems(Set<OrderItem> items) {
        this.items = items;
    }

    public Set<PaymentTransaction> getPayments() {
        return payments;
    }

    public void setPayments(Set<PaymentTransaction> payments) {
        this.payments = payments;
    }

    public Set<Fulfillment> getFulfillments() {
        return fulfillments;
    }

    public void setFulfillments(Set<Fulfillment> fulfillments) {
        this.fulfillments = fulfillments;
    }

    public Set<OrderCoupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(Set<OrderCoupon> coupons) {
        this.coupons = coupons;
    }

    // ===== Helpers =====
    public void addItem(OrderItem item) {
        if (item == null)
            return;
        item.setOrder(this);
        this.items.add(item);
    }

    @Transient
    public BigDecimal calcTotalFromItems() {
        return items.stream()
                .map(i -> i.getLineTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal nvl(BigDecimal x) {
        return x == null ? BigDecimal.ZERO : x;
    }

    // ===== equals/hashCode theo Id =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Order other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // ================== ENUMS + CONVERTERS ==================
    public enum PaymentMethod {
        COD("COD"), VNPAY("VNPAY"), MOMO("MOMO");

        private final String db;

        PaymentMethod(String db) {
            this.db = db;
        }

        public String getDb() {
            return db;
        }

        public static PaymentMethod of(String v) {
            if (v == null)
                return null;
            for (var e : values())
                if (e.db.equalsIgnoreCase(v))
                    return e;
            throw new IllegalArgumentException("Unknown PaymentMethod: " + v);
        }

        @jakarta.persistence.Converter(autoApply = false)
        public static class Converter implements AttributeConverter<PaymentMethod, String> {
            public String convertToDatabaseColumn(PaymentMethod a) {
                return a == null ? null : a.getDb();
            }

            public PaymentMethod convertToEntityAttribute(String v) {
                return PaymentMethod.of(v);
            }
        }
    }

    public enum PaymentStatus {
        UNPAID("unpaid"), PAID("paid"), REFUNDED("refunded"), FAILED("failed");

        private final String db;

        PaymentStatus(String db) {
            this.db = db;
        }

        public String getDb() {
            return db;
        }

        public static PaymentStatus of(String v) {
            for (var e : values())
                if (e.db.equalsIgnoreCase(v))
                    return e;
            throw new IllegalArgumentException("Unknown PaymentStatus: " + v);
        }

        @jakarta.persistence.Converter(autoApply = false)
        public static class Converter implements AttributeConverter<PaymentStatus, String> {
            public String convertToDatabaseColumn(PaymentStatus a) {
                return a == null ? null : a.getDb();
            }

            public PaymentStatus convertToEntityAttribute(String v) {
                return PaymentStatus.of(v);
            }
        }
    }

    public enum OrderStatus {
        NEW("NEW"), CONFIRMED("CONFIRMED"), SHIPPING("SHIPPING"),
        DELIVERED("DELIVERED"), CANCELED("CANCELED"), RETURNED("RETURNED");

        private final String db;

        OrderStatus(String db) {
            this.db = db;
        }

        public String getDb() {
            return db;
        }

        public static OrderStatus of(String v) {
            for (var e : values())
                if (e.db.equalsIgnoreCase(v))
                    return e;
            throw new IllegalArgumentException("Unknown OrderStatus: " + v);
        }

        @jakarta.persistence.Converter(autoApply = false)
        public static class Converter implements AttributeConverter<OrderStatus, String> {
            public String convertToDatabaseColumn(OrderStatus a) {
                return a == null ? null : a.getDb();
            }

            public OrderStatus convertToEntityAttribute(String v) {
                return OrderStatus.of(v);
            }
        }
    }
}
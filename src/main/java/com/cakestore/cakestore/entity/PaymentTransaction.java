package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "PaymentTransactions", indexes = {
        @Index(name = "IX_Payments_Order", columnList = "OrderId")
})
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "OrderId", nullable = false)
    private Order order;

    @Convert(converter = Provider.Converter.class)
    @Column(name = "Provider", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private Provider provider; // COD / VNPAY / MOMO

    @Column(name = "ProviderTxnId", length = 128, columnDefinition = "NVARCHAR(128)")
    private String providerTxnId;

    @Column(name = "Amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Convert(converter = Status.Converter.class)
    @Column(name = "Status", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private Status status; // created / success / failed / refunded

    @Lob
    @Column(name = "RawPayload", columnDefinition = "NVARCHAR(MAX)")
    private String rawPayload;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public PaymentTransaction() {
    }

    public PaymentTransaction(Order order, Provider provider, BigDecimal amount, Status status) {
        this.order = order;
        this.provider = provider;
        this.amount = amount;
        this.status = status;
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

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public String getProviderTxnId() {
        return providerTxnId;
    }

    public void setProviderTxnId(String providerTxnId) {
        this.providerTxnId = providerTxnId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ===== equals/hashCode theo Id =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PaymentTransaction that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // ================== ENUMS + CONVERTERS ==================
    public enum Provider {
        COD("COD"), VNPAY("VNPAY"), MOMO("MOMO");

        private final String db;

        Provider(String db) {
            this.db = db;
        }

        public String getDb() {
            return db;
        }

        public static Provider of(String v) {
            for (var e : values())
                if (e.db.equalsIgnoreCase(v))
                    return e;
            throw new IllegalArgumentException("Unknown Provider: " + v);
        }

        @jakarta.persistence.Converter(autoApply = false)
        public static class Converter implements AttributeConverter<Provider, String> {
            @Override
            public String convertToDatabaseColumn(Provider attr) {
                return attr == null ? null : attr.getDb();
            }

            @Override
            public Provider convertToEntityAttribute(String db) {
                return db == null ? null : Provider.of(db);
            }
        }
    }

    public enum Status {
        CREATED("created"), SUCCESS("success"), FAILED("failed"), REFUNDED("refunded");

        private final String db;

        Status(String db) {
            this.db = db;
        }

        public String getDb() {
            return db;
        }

        public static Status of(String v) {
            for (var e : values())
                if (e.db.equalsIgnoreCase(v))
                    return e;
            throw new IllegalArgumentException("Unknown Payment Status: " + v);
        }

        @jakarta.persistence.Converter(autoApply = false)
        public static class Converter implements AttributeConverter<Status, String> {
            @Override
            public String convertToDatabaseColumn(Status attr) {
                return attr == null ? null : attr.getDb();
            }

            @Override
            public Status convertToEntityAttribute(String db) {
                return db == null ? null : Status.of(db);
            }
        }
    }
}

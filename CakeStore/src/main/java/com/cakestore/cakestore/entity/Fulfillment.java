package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Fulfillments", indexes = {
        @Index(name = "IX_Fulfillments_Order", columnList = "OrderId")
})
public class Fulfillment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    // nhiều fulfillment -> 1 đơn hàng
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "OrderId", nullable = false)
    private Order order;

    // Trạng thái: PACKING, OUT_FOR_DELIVERY, DELIVERED, FAILED
    @Convert(converter = Status.Converter.class)
    @Column(name = "Status", nullable = false, length = 20)
    private Status status = Status.PACKING;

    @Column(name = "CarrierCode", length = 50)
    private String carrierCode; // GHN/GHTK/INTERNAL...

    @Column(name = "TrackingNo", length = 100)
    private String trackingNo;

    @Column(name = "Note", length = 300)
    private String note;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // Quan hệ: phân công shipper
    @OneToMany(mappedBy = "fulfillment", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ShipAssignment> assignments = new LinkedHashSet<>();

    public Fulfillment() {
    }

    public Fulfillment(Order order) {
        this.order = order;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
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

    public Set<ShipAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(Set<ShipAssignment> assignments) {
        this.assignments = assignments;
    }

    // ===== Helpers =====
    public void addAssignment(ShipAssignment sa) {
        if (sa == null)
            return;
        sa.setFulfillment(this);
        this.assignments.add(sa);
    }

    // ===== equals/hashCode theo Id =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Fulfillment that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // ===== Enum + Converter để lưu đúng chuỗi trong DB =====
    public enum Status {
        PACKING("PACKING"),
        OUT_FOR_DELIVERY("OUT_FOR_DELIVERY"),
        DELIVERED("DELIVERED"),
        FAILED("FAILED");

        private final String db;

        Status(String db) {
            this.db = db;
        }

        public String getDb() {
            return db;
        }

        public static Status of(String v) {
            for (var s : values())
                if (s.db.equalsIgnoreCase(v))
                    return s;
            throw new IllegalArgumentException("Unknown Fulfillment Status: " + v);
        }

        @jakarta.persistence.Converter(autoApply = false)
        public static class Converter implements AttributeConverter<Status, String> {
            @Override
            public String convertToDatabaseColumn(Status a) {
                return a == null ? null : a.getDb();
            }

            @Override
            public Status convertToEntityAttribute(String v) {
                return v == null ? null : Status.of(v);
            }
        }
    }
}

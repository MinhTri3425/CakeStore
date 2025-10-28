package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "ShipAssignments", indexes = {
        @Index(name = "IX_ShipAssign_Fulfill", columnList = "FulfillmentId")
})
public class ShipAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "FulfillmentId", nullable = false)
    private Fulfillment fulfillment;

    // Shipper là User có role 'shipper'
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ShipperId", nullable = false)
    private User shipper;

    @Column(name = "AssignedAt", insertable = false, updatable = false)
    private LocalDateTime assignedAt;

    @Convert(converter = Status.Converter.class)
    @Column(name = "Status", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private Status status = Status.ASSIGNED;

    /* ===== Constructors ===== */
    public ShipAssignment() {
    }

    public ShipAssignment(Fulfillment fulfillment, User shipper) {
        this.fulfillment = fulfillment;
        this.shipper = shipper;
    }

    /* ===== Getters/Setters ===== */
    public Long getId() {
        return id;
    }

    public Fulfillment getFulfillment() {
        return fulfillment;
    }

    public void setFulfillment(Fulfillment fulfillment) {
        this.fulfillment = fulfillment;
    }

    public User getShipper() {
        return shipper;
    }

    public void setShipper(User shipper) {
        this.shipper = shipper;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    /* ===== equals/hashCode theo Id ===== */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ShipAssignment that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    /* ===== Enum + Converter ===== */
    public enum Status {
        ASSIGNED("assigned"),
        IN_PROGRESS("in_progress"),
        DONE("done"),
        FAILED("failed");

        private final String db;

        Status(String db) {
            this.db = db;
        }

        public String getDb() {
            return db;
        }

        public static Status of(String v) {
            if (v == null)
                return null;
            for (var s : values())
                if (s.db.equalsIgnoreCase(v))
                    return s;
            throw new IllegalArgumentException("Unknown ShipAssignment status: " + v);
        }

        @jakarta.persistence.Converter(autoApply = false)
        public static class Converter implements AttributeConverter<Status, String> {
            @Override
            public String convertToDatabaseColumn(Status a) {
                return a == null ? null : a.getDb();
            }

            @Override
            public Status convertToEntityAttribute(String v) {
                return Status.of(v);
            }
        }
    }
}

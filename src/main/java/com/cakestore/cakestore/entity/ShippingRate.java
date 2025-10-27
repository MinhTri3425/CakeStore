package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "ShippingRates", indexes = {
        @Index(name = "IX_ShippingRates_Branch", columnList = "BranchId")
})
public class ShippingRate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    // FK -> Carriers(Id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CarrierId", nullable = false)
    private Carrier carrier;

    // FK -> Branches(Id), có thể NULL (áp dụng toàn hệ thống)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BranchId")
    private Branch branch;

    @Column(name = "District", length = 120)
    private String district;

    @Column(name = "City", length = 120)
    private String city;

    @Column(name = "BaseFee", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseFee;

    @Column(name = "ExtraPerKm", precision = 12, scale = 2)
    private BigDecimal extraPerKm;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive = true;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public ShippingRate() {
    }

    public ShippingRate(Carrier carrier, BigDecimal baseFee) {
        this.carrier = carrier;
        this.baseFee = baseFee;
    }

    // ===== Getters/Setters =====
    public Long getId() {
        return id;
    }

    public Carrier getCarrier() {
        return carrier;
    }

    public void setCarrier(Carrier carrier) {
        this.carrier = carrier;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public BigDecimal getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(BigDecimal baseFee) {
        this.baseFee = baseFee;
    }

    public BigDecimal getExtraPerKm() {
        return extraPerKm;
    }

    public void setExtraPerKm(BigDecimal extraPerKm) {
        this.extraPerKm = extraPerKm;
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

    // ===== equals/hashCode theo Id =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ShippingRate that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

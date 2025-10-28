package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Carriers", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_Carriers_Code", columnNames = "Code")
})
public class Carrier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    /** Mã hãng: GHN, GHTK, INTERNAL... */
    @Column(name = "Code", nullable = false, length = 50, unique = true, columnDefinition = "NVARCHAR(50)")
    private String code;

    @Column(name = "Name", nullable = false, length = 120, columnDefinition = "NVARCHAR(120)")
    private String name;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive = true;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // Quan hệ ngược tới ShippingRates
    @OneToMany(mappedBy = "carrier", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ShippingRate> shippingRates = new LinkedHashSet<>();

    public Carrier() {
    }

    public Carrier(String code, String name) {
        this.code = code;
        this.name = name;
    }

    // Getters/Setters
    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Set<ShippingRate> getShippingRates() {
        return shippingRates;
    }

    public void setShippingRates(Set<ShippingRate> shippingRates) {
        this.shippingRates = shippingRates;
    }

    // equals/hashCode theo Id
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Carrier that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "Addresses", indexes = {
        @Index(name = "IX_Addresses_User", columnList = "UserId")
})
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Column(name = "FullName", nullable = false, length = 120, columnDefinition = "NVARCHAR(120)")
    private String fullName;

    @Column(name = "Phone", nullable = false, length = 20, columnDefinition = "NVARCHAR(20)")
    private String phone;

    @Column(name = "Line1", nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String line1;

    @Column(name = "Ward", length = 120, columnDefinition = "NVARCHAR(120)")
    private String ward;

    @Column(name = "District", length = 120, columnDefinition = "NVARCHAR(120)")
    private String district;

    @Column(name = "City", nullable = false, length = 120, columnDefinition = "NVARCHAR(120)")
    private String city;

    @Column(name = "IsDefault", nullable = false)
    private boolean isDefault = false;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Address() {
    }

    public Address(User user, String fullName, String phone, String line1, String city) {
        this.user = user;
        this.fullName = fullName;
        this.phone = phone;
        this.line1 = line1;
        this.city = city;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(String line1) {
        this.line1 = line1;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
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

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // ===== Helper: format địa chỉ đầy đủ =====
    @Transient
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (line1 != null && !line1.isBlank())
            sb.append(line1);
        if (ward != null && !ward.isBlank())
            sb.append(", ").append(ward);
        if (district != null && !district.isBlank())
            sb.append(", ").append(district);
        if (city != null && !city.isBlank())
            sb.append(", ").append(city);
        return sb.toString();
    }

    // ===== equals/hashCode theo Id =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Address that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

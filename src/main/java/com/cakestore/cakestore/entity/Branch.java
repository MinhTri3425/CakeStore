package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Branches", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_Branches_Code", columnNames = "Code")
})
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    /** Ví dụ: HCM-Q1, HN-CG */
    @Column(name = "Code", nullable = false, length = 50, unique = true, columnDefinition = "NVARCHAR(50)")
    private String code;

    @Column(name = "Name", nullable = false, length = 150, columnDefinition = "NVARCHAR(150)")
    private String name;

    @Column(name = "Phone", length = 20, columnDefinition = "NVARCHAR(20)")
    private String phone;

    @Column(name = "Line1", length = 255, columnDefinition = "NVARCHAR(255)")
    private String line1;

    @Column(name = "Ward", length = 120, columnDefinition = "NVARCHAR(120)")
    private String ward;

    @Column(name = "District", length = 120, columnDefinition = "NVARCHAR(120)")
    private String district;

    @Column(name = "City", length = 120, columnDefinition = "NVARCHAR(120)")
    private String city;

    @Column(name = "IsActive", nullable = false)
    private boolean isActive = true;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /* ================= Relations (tùy nhu cầu dùng) ================= */

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BranchProduct> branchProducts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BranchInventory> branchInventory = new LinkedHashSet<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private Set<Cart> carts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private Set<Order> orders = new LinkedHashSet<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private Set<ShippingRate> shippingRates = new LinkedHashSet<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private Set<Promotion> promotions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private Set<Coupon> coupons = new LinkedHashSet<>();

    /* ================= Constructors ================= */

    public Branch() {
    }

    public Branch(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /* ================= Getters/Setters ================= */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Set<BranchProduct> getBranchProducts() {
        return branchProducts;
    }

    public void setBranchProducts(Set<BranchProduct> branchProducts) {
        this.branchProducts = branchProducts;
    }

    public Set<BranchInventory> getBranchInventory() {
        return branchInventory;
    }

    public void setBranchInventory(Set<BranchInventory> branchInventory) {
        this.branchInventory = branchInventory;
    }

    public Set<Cart> getCarts() {
        return carts;
    }

    public void setCarts(Set<Cart> carts) {
        this.carts = carts;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    public void setOrders(Set<Order> orders) {
        this.orders = orders;
    }

    public Set<ShippingRate> getShippingRates() {
        return shippingRates;
    }

    public void setShippingRates(Set<ShippingRate> shippingRates) {
        this.shippingRates = shippingRates;
    }

    public Set<Promotion> getPromotions() {
        return promotions;
    }

    public void setPromotions(Set<Promotion> promotions) {
        this.promotions = promotions;
    }

    public Set<Coupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(Set<Coupon> coupons) {
        this.coupons = coupons;
    }

    /* ================= equals/hashCode theo Id ================= */

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Branch other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Users", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_Users_Email", columnNames = "Email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @Column(name = "Email", nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "PasswordHash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "FullName", nullable = false, columnDefinition = "NVARCHAR(150)")
    private String fullName;

    /** customer/admin/shipper — ràng buộc bằng CHECK ở DB */
    @Column(name = "Role", nullable = false, length = 20)
    private String role = "customer";

    @Column(name = "IsActive", nullable = false)
    private boolean isActive = true;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /* ================= Quan hệ thường dùng (LAZY) ================= */

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Address> addresses = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OtpToken> otpTokens = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RefreshToken> refreshTokens = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Favorite> favorites = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<RecentlyViewed> recentlyViewed = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Review> reviews = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Comment> comments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<CouponUsage> couponUsages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Cart> carts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Order> orders = new LinkedHashSet<>();

    // Nếu dùng vai trò shipper: gán vào phân công giao hàng
    @OneToMany(mappedBy = "shipper", fetch = FetchType.LAZY)
    private Set<ShipAssignment> shipAssignments = new LinkedHashSet<>();

    /* ================= Constructors ================= */

    public User() {
    }

    public User(String email, String passwordHash, String fullName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }

    /* ================= Getters/Setters ================= */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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

    public Set<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(Set<Address> addresses) {
        this.addresses = addresses;
    }

    public Set<OtpToken> getOtpTokens() {
        return otpTokens;
    }

    public void setOtpTokens(Set<OtpToken> otpTokens) {
        this.otpTokens = otpTokens;
    }

    public Set<RefreshToken> getRefreshTokens() {
        return refreshTokens;
    }

    public void setRefreshTokens(Set<RefreshToken> refreshTokens) {
        this.refreshTokens = refreshTokens;
    }

    public Set<Favorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(Set<Favorite> favorites) {
        this.favorites = favorites;
    }

    public Set<RecentlyViewed> getRecentlyViewed() {
        return recentlyViewed;
    }

    public void setRecentlyViewed(Set<RecentlyViewed> recentlyViewed) {
        this.recentlyViewed = recentlyViewed;
    }

    public Set<Review> getReviews() {
        return reviews;
    }

    public void setReviews(Set<Review> reviews) {
        this.reviews = reviews;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public Set<CouponUsage> getCouponUsages() {
        return couponUsages;
    }

    public void setCouponUsages(Set<CouponUsage> couponUsages) {
        this.couponUsages = couponUsages;
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

    public Set<ShipAssignment> getShipAssignments() {
        return shipAssignments;
    }

    public void setShipAssignments(Set<ShipAssignment> shipAssignments) {
        this.shipAssignments = shipAssignments;
    }

    /* ================= equals/hashCode theo Id ================= */

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof User other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

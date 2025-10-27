package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "CouponUsages", indexes = {
        @Index(name = "IX_CouponUsages_User", columnList = "UserId"),
        @Index(name = "IX_CouponUsages_Coupon", columnList = "CouponId")
})
public class CouponUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CouponId", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Column(name = "UsedAt", insertable = false, updatable = false)
    private LocalDateTime usedAt;

    public CouponUsage() {
    }

    public CouponUsage(Coupon coupon, User user) {
        this.coupon = coupon;
        this.user = user;
    }

    // ===== Getters/Setters =====
    public Long getId() {
        return id;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    // ===== equals/hashCode theo Id =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CouponUsage that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

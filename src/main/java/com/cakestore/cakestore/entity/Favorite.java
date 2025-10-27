package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "Favorites", indexes = {
        @Index(name = "IX_Favorites_User", columnList = "UserId"),
        @Index(name = "IX_Favorites_Product", columnList = "ProductId")
})
@IdClass(Favorite.FavoriteKey.class) // <-- dùng inner class làm IdClass
public class Favorite {

    @Id
    @Column(name = "UserId", nullable = false)
    private Long userId;

    @Id
    @Column(name = "ProductId", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UserId", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ProductId", insertable = false, updatable = false)
    private Product product;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Favorite() {
    }

    public Favorite(User user, Product product) {
        setUser(user);
        setProduct(product);
    }

    public Long getUserId() {
        return userId;
    }

    public Long getProductId() {
        return productId;
    }

    public User getUser() {
        return user;
    }

    public Product getProduct() {
        return product;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setUser(User user) {
        this.user = user;
        this.userId = (user != null ? user.getId() : null);
    }

    public void setProduct(Product product) {
        this.product = product;
        this.productId = (product != null ? product.getId() : null);
    }

    /** Inner static key class – không cần file riêng */
    public static class FavoriteKey implements Serializable {
        private Long userId;
        private Long productId;

        public FavoriteKey() {
        }

        public FavoriteKey(Long userId, Long productId) {
            this.userId = userId;
            this.productId = productId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof FavoriteKey k))
                return false;
            return Objects.equals(userId, k.userId) && Objects.equals(productId, k.productId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, productId);
        }
    }
}

package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "Reviews", indexes = {
        @Index(name = "IX_Reviews_User", columnList = "UserId"),
        @Index(name = "IX_Reviews_Product", columnList = "ProductId")
})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    // optional: link tới dòng hàng đã mua; DB: ON DELETE SET NULL
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "OrderItemId")
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ProductId", nullable = false)
    private Product product;

    @Min(1)
    @Max(5)
    @Column(name = "Rating", nullable = false)
    private Integer rating;

    @Lob
    @Column(name = "Content", nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String content;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /* ============ Quan hệ con ============ */

    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ReviewMedia> media = new LinkedHashSet<>();

    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new LinkedHashSet<>();

    /* ============ Constructors ============ */

    public Review() {
    }

    public Review(User user, Product product, Integer rating, String content) {
        this.user = user;
        this.product = product;
        this.rating = rating;
        this.content = content;
    }

    /* ============ Getters/Setters ============ */

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Set<ReviewMedia> getMedia() {
        return media;
    }

    public void setMedia(Set<ReviewMedia> media) {
        this.media = media;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    /* ============ Helpers ============ */

    public void addMedia(ReviewMedia m) {
        if (m == null)
            return;
        m.setReview(this);
        this.media.add(m);
    }

    public void addComment(Comment c) {
        if (c == null)
            return;
        c.setReview(this);
        this.comments.add(c);
    }

    /* equals/hashCode theo Id */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Review other))
            return false;
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

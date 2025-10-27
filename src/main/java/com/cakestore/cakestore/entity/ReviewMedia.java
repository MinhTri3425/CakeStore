package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "ReviewMedia", indexes = {
        @Index(name = "IX_ReviewMedia_Review", columnList = "ReviewId")
})
public class ReviewMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ReviewId", nullable = false)
    private Review review;

    @Column(name = "MediaUrl", nullable = false, length = 500)
    private String mediaUrl;

    @Convert(converter = MediaType.Converter.class)
    @Column(name = "MediaType", nullable = false, length = 20)
    private MediaType mediaType; // image | video

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public ReviewMedia() {
    }

    public ReviewMedia(Review review, String mediaUrl, MediaType mediaType) {
        this.review = review;
        this.mediaUrl = mediaUrl;
        this.mediaType = mediaType;
    }

    // ===== Getters/Setters =====
    public Long getId() {
        return id;
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ===== equals/hashCode theo Id =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ReviewMedia that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // ===== Enum + Converter (khớp CHECK constraint) =====
    public enum MediaType {
        IMAGE("image"),
        VIDEO("video");

        private final String db;

        MediaType(String db) {
            this.db = db;
        }

        public String getDb() {
            return db;
        }

        public static MediaType of(String v) {
            for (var t : values())
                if (t.db.equalsIgnoreCase(v))
                    return t;
            throw new IllegalArgumentException("Unknown MediaType: " + v);
        }

        @jakarta.persistence.Converter(autoApply = false)
        public static class Converter implements AttributeConverter<MediaType, String> {
            @Override
            public String convertToDatabaseColumn(MediaType a) {
                return a == null ? null : a.getDb();
            }

            @Override
            public MediaType convertToEntityAttribute(String v) {
                return v == null ? null : MediaType.of(v);
            }
        }
    }
}

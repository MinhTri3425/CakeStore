package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "OtpTokens", indexes = {
        @Index(name = "IX_OtpTokens_User", columnList = "UserId")
})
public class OtpToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UserId", nullable = false)
    private User user;

    @Column(name = "Code", nullable = false, length = 10)
    private String code;

    @Convert(converter = Purpose.Converter.class)
    @Column(name = "Purpose", nullable = false, length = 20)
    private Purpose purpose; // activate | reset

    @Column(name = "ExpiresAt", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "UsedAt")
    private LocalDateTime usedAt;

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public OtpToken() {
    }

    public OtpToken(User user, String code, Purpose purpose, LocalDateTime expiresAt) {
        this.user = user;
        this.code = code;
        this.purpose = purpose;
        this.expiresAt = expiresAt;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Purpose getPurpose() {
        return purpose;
    }

    public void setPurpose(Purpose purpose) {
        this.purpose = purpose;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // ===== Helpers =====
    @Transient
    public boolean isExpired(LocalDateTime now) {
        return expiresAt != null && now != null && now.isAfter(expiresAt);
    }

    @Transient
    public boolean isUsed() {
        return usedAt != null;
    }

    // ===== equals/hashCode theo Id =====
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OtpToken that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // ===== Enum + Converter (khớp CHECK constraint) =====
    public enum Purpose {
        ACTIVATE("activate"),
        RESET("reset");

        private final String db;

        Purpose(String db) {
            this.db = db;
        }

        public String getDb() {
            return db;
        }

        public static Purpose of(String v) {
            for (var p : values())
                if (p.db.equalsIgnoreCase(v))
                    return p;
            throw new IllegalArgumentException("Unknown OTP Purpose: " + v);
        }

        @jakarta.persistence.Converter(autoApply = false)
        public static class Converter implements AttributeConverter<Purpose, String> {
            @Override
            public String convertToDatabaseColumn(Purpose a) {
                return a == null ? null : a.getDb();
            }

            @Override
            public Purpose convertToEntityAttribute(String v) {
                return v == null ? null : Purpose.of(v);
            }
        }
    }
}

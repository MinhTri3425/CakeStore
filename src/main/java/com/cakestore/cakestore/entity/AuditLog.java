package com.cakestore.cakestore.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "AuditLogs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Long id;

    // User có thể bị xóa hoặc null (DB: ON DELETE SET NULL)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId")
    private User user;

    @Column(name = "Action", nullable = false, length = 100)
    private String action; // ví dụ: ORDER_STATUS_CHANGE

    @Column(name = "Entity", length = 100)
    private String entity; // ví dụ: Orders

    @Column(name = "EntityId", length = 100)
    private String entityId; // ví dụ: "123"

    @Lob
    @Column(name = "Meta")
    private String meta; // JSON text

    @Column(name = "CreatedAt", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public AuditLog() {
    }

    public AuditLog(User user, String action, String entity, String entityId, String meta) {
        this.user = user;
        this.action = action;
        this.entity = entity;
        this.entityId = entityId;
        this.meta = meta;
    }

    // Getters/Setters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // equals/hashCode theo Id
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AuditLog that))
            return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // Helper tiện dụng
    public static AuditLog of(User user, String action, String entity, String entityId, String metaJson) {
        return new AuditLog(user, action, entity, entityId, metaJson);
    }
}

package com.cpr_db.cpr_db.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "skills")
public class Skill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 500)
    private String icon;

    @Column(name = "scene_id")
    private Long sceneId;

    @Column(length = 20)
    private String status;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Skill() {}

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = "active";
        if (sortOrder == null) sortOrder = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Long getSceneId() { return sceneId; }
    public void setSceneId(Long sceneId) { this.sceneId = sceneId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

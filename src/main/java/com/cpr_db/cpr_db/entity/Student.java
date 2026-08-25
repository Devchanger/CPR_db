package com.cpr_db.cpr_db.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "students", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username")
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "group_name", length = 100)
    private String groupName;

    @Column(name = "cert_status", length = 30)
    private String certStatus;

    @Column(name = "trained_at")
    private LocalDateTime trainedAt;

    @Column(length = 20)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** D19: nullable mapping key to the User account (not an FK, not a hard binding). */
    @Column(length = 50)
    private String username;

    public Student() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "active";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getCertStatus() {
        return certStatus;
    }

    public void setCertStatus(String certStatus) {
        this.certStatus = certStatus;
    }

    public LocalDateTime getTrainedAt() {
        return trainedAt;
    }

    public void setTrainedAt(LocalDateTime trainedAt) {
        this.trainedAt = trainedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

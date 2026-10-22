package com.moneyguard.api.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true, length = 32)
    private String phone;

    @Column(name = "phone_normalized", unique = true, length = 32)
    private String phoneNormalized;

    @Column(unique = true, length = 254)
    private String email;

    @Column(name = "display_name", length = 120)
    private String displayName;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false, length = 255)
    private String roles; // comma-separated

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPhoneNormalized() { return phoneNormalized; }
    public void setPhoneNormalized(String phoneNormalized) { this.phoneNormalized = phoneNormalized; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRoles() { return roles; }
    public void setRoles(String roles) { this.roles = roles; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

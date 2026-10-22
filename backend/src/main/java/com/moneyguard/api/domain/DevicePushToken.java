package com.moneyguard.api.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "user_device_tokens")
public class DevicePushToken {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "device_id", nullable = false, length = 128)
    private String deviceId;

    @Column(name = "expo_push_token", nullable = false, length = 255)
    private String expoPushToken;

    @Column(length = 32)
    private String platform;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        if (updatedAt == null) updatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate() { updatedAt = Instant.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getExpoPushToken() { return expoPushToken; }
    public void setExpoPushToken(String expoPushToken) { this.expoPushToken = expoPushToken; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

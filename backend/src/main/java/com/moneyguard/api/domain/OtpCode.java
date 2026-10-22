package com.moneyguard.api.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "otp_codes")
public class OtpCode {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "subject", nullable = false, length = 128)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Enums.OtpPurpose purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Enums.OtpChannel channel;

    @Column(name = "code_hash", nullable = false, length = 255)
    private String codeHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public Enums.OtpPurpose getPurpose() { return purpose; }
    public void setPurpose(Enums.OtpPurpose purpose) { this.purpose = purpose; }
    public Enums.OtpChannel getChannel() { return channel; }
    public void setChannel(Enums.OtpChannel channel) { this.channel = channel; }
    public String getCodeHash() { return codeHash; }
    public void setCodeHash(String codeHash) { this.codeHash = codeHash; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Instant consumedAt) { this.consumedAt = consumedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

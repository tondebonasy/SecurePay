package com.moneyguard.api.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "outbox_jobs")
public class OutboxJob {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "job_type", nullable = false, length = 32)
    @Enumerated(EnumType.STRING)
    private Enums.OutboxType jobType;

    @Column(nullable = false, length = 16)
    @Enumerated(EnumType.STRING)
    private Enums.OutboxStatus status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "next_run_at", nullable = false)
    private Instant nextRunAt;

    @Lob
    @Column(name = "payload_json", nullable = false)
    private String payloadJson;

    @Lob
    @Column(name = "last_error")
    private String lastError;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (status == null) status = Enums.OutboxStatus.PENDING;
        if (nextRunAt == null) nextRunAt = now;
    }

    @PreUpdate
    public void onUpdate() { updatedAt = Instant.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Enums.OutboxType getJobType() { return jobType; }
    public void setJobType(Enums.OutboxType jobType) { this.jobType = jobType; }
    public Enums.OutboxStatus getStatus() { return status; }
    public void setStatus(Enums.OutboxStatus status) { this.status = status; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public Instant getNextRunAt() { return nextRunAt; }
    public void setNextRunAt(Instant nextRunAt) { this.nextRunAt = nextRunAt; }
    public String getPayloadJson() { return payloadJson; }
    public void setPayloadJson(String payloadJson) { this.payloadJson = payloadJson; }
    public String getLastError() { return lastError; }
    public void setLastError(String lastError) { this.lastError = lastError; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

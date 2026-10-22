package com.moneyguard.api.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "webhook_events")
public class WebhookEvent {
    @Id
    @Column(length = 36)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Enums.WebhookProvider provider;

    @Column(name = "event_id", nullable = false, length = 128)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 128)
    private String eventType;

    @Column(nullable = false)
    private boolean verified;

    @Lob
    @Column(name = "raw_body", nullable = false)
    private String rawBody;

    @Lob
    @Column(name = "headers_json", nullable = false)
    private String headersJson;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Enums.WebhookStatus status;

    @PrePersist
    public void onCreate() {
        if (receivedAt == null) receivedAt = Instant.now();
        if (status == null) status = Enums.WebhookStatus.RECEIVED;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Enums.WebhookProvider getProvider() { return provider; }
    public void setProvider(Enums.WebhookProvider provider) { this.provider = provider; }
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public String getRawBody() { return rawBody; }
    public void setRawBody(String rawBody) { this.rawBody = rawBody; }
    public String getHeadersJson() { return headersJson; }
    public void setHeadersJson(String headersJson) { this.headersJson = headersJson; }
    public Instant getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Instant receivedAt) { this.receivedAt = receivedAt; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
    public Enums.WebhookStatus getStatus() { return status; }
    public void setStatus(Enums.WebhookStatus status) { this.status = status; }
}

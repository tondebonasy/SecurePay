package com.moneyguard.api.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payment_requests")
public class PaymentRequest {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "payee_id", nullable = false, length = 36)
    private String payeeId;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 8)
    private String currency;

    @Column(nullable = false, length = 16)
    private String provider;

    @Column(name = "paynow_method", length = 16)
    private String paynowMethod;

    @Column(name = "paynow_phone", length = 32)
    private String paynowPhone;

    @Column(length = 255)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Enums.PaymentRequestStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, length = 64)
    private String nonce;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "consumed_tx_id", length = 36)
    private String consumedTxId;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getPayeeId() { return payeeId; }
    public void setPayeeId(String payeeId) { this.payeeId = payeeId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getPaynowMethod() { return paynowMethod; }
    public void setPaynowMethod(String paynowMethod) { this.paynowMethod = paynowMethod; }
    public String getPaynowPhone() { return paynowPhone; }
    public void setPaynowPhone(String paynowPhone) { this.paynowPhone = paynowPhone; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public Enums.PaymentRequestStatus getStatus() { return status; }
    public void setStatus(Enums.PaymentRequestStatus status) { this.status = status; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getConsumedTxId() { return consumedTxId; }
    public void setConsumedTxId(String consumedTxId) { this.consumedTxId = consumedTxId; }
}

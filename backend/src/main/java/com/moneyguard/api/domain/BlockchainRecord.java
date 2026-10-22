package com.moneyguard.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "blockchain_records")
public class BlockchainRecord {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "tx_id", nullable = false, length = 36)
    private String txId;

    @Column(name = "payload_hash", nullable = false, length = 128)
    private String payloadHash;

    @Column(name = "stellar_tx_hash", nullable = false, length = 128)
    private String stellarTxHash;

    @Column(name = "anchored_at", nullable = false)
    private Instant anchoredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTxId() { return txId; }
    public void setTxId(String txId) { this.txId = txId; }
    public String getPayloadHash() { return payloadHash; }
    public void setPayloadHash(String payloadHash) { this.payloadHash = payloadHash; }
    public String getStellarTxHash() { return stellarTxHash; }
    public void setStellarTxHash(String stellarTxHash) { this.stellarTxHash = stellarTxHash; }
    public Instant getAnchoredAt() { return anchoredAt; }
    public void setAnchoredAt(Instant anchoredAt) { this.anchoredAt = anchoredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

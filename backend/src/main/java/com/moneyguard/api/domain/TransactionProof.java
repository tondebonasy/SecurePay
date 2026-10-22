package com.moneyguard.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "transaction_proofs")
public class TransactionProof {
    @Id
    @Column(length = 36)
    private String id;

    @Column(name = "transaction_id", nullable = false, length = 36)
    private String transactionId;

    @Column(name = "proof_hash", nullable = false, length = 128)
    private String proofHash;

    @Column(name = "stellar_tx_hash", nullable = false, length = 128)
    private String stellarTxHash;

    @Column(name = "ledger")
    private Long ledger;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    public String getProofHash() { return proofHash; }
    public void setProofHash(String proofHash) { this.proofHash = proofHash; }
    public String getStellarTxHash() { return stellarTxHash; }
    public void setStellarTxHash(String stellarTxHash) { this.stellarTxHash = stellarTxHash; }
    public Long getLedger() { return ledger; }
    public void setLedger(Long ledger) { this.ledger = ledger; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

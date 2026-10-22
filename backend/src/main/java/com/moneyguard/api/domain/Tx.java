package com.moneyguard.api.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class Tx {
  @Version
  @Column(name = "row_version", nullable = false)
  private Long rowVersion;

  @Id
  @Column(length = 36)
  private String id;

  @Column(name = "user_id", nullable = false, length = 36)
  private String userId;

  @Column(nullable = false, precision = 18, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 8)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private Enums.TxStatus status;

  @Column(name = "paypal_order_id", length = 64)
  private String paypalOrderId;

  @Column(name = "paypal_capture_id", length = 64)
  private String paypalCaptureId;

  @Column(name = "paynow_reference", length = 64)
  private String paynowReference;

  @Column(name = "paynow_poll_url", length = 512)
  private String paynowPollUrl;

  @Column(name = "paynow_method", length = 32)
  private String paynowMethod;

  @Column(name = "paynow_phone", length = 32)
  private String paynowPhone;

  @Column(name = "idempotency_key", length = 128)
  private String idempotencyKey;

  @Column(name = "paid_by_user_id", length = 36)
  private String paidByUserId;

  @Column(name = "payee_user_id", length = 36)
  private String payeeUserId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @PrePersist
  public void onCreate() {
    Instant now = Instant.now();
    if (createdAt == null) createdAt = now;
    if (updatedAt == null) updatedAt = now;
  }

  @PreUpdate
  public void onUpdate() {
    updatedAt = Instant.now();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public Enums.TxStatus getStatus() {
    return status;
  }

  public void setStatus(Enums.TxStatus status) {
    this.status = status;
  }

  public String getPaypalOrderId() {
    return paypalOrderId;
  }

  public void setPaypalOrderId(String paypalOrderId) {
    this.paypalOrderId = paypalOrderId;
  }

  public String getPaypalCaptureId() {
    return paypalCaptureId;
  }

  public void setPaypalCaptureId(String paypalCaptureId) {
    this.paypalCaptureId = paypalCaptureId;
  }

  public String getPaynowReference() {
    return paynowReference;
  }

  public void setPaynowReference(String paynowReference) {
    this.paynowReference = paynowReference;
  }

  @com.fasterxml.jackson.annotation.JsonIgnore
  public String getPaynowPollUrl() {
    return paynowPollUrl;
  }

  public void setPaynowPollUrl(String paynowPollUrl) {
    this.paynowPollUrl = paynowPollUrl;
  }

  public String getPaynowMethod() {
    return paynowMethod;
  }

  public void setPaynowMethod(String paynowMethod) {
    this.paynowMethod = paynowMethod;
  }

  public String getPaynowPhone() {
    return paynowPhone;
  }

  public void setPaynowPhone(String paynowPhone) {
    this.paynowPhone = paynowPhone;
  }

  @com.fasterxml.jackson.annotation.JsonIgnore
  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public void setIdempotencyKey(String idempotencyKey) {
    this.idempotencyKey = idempotencyKey;
  }

  public String getPaidByUserId() {
    return paidByUserId;
  }

  public void setPaidByUserId(String paidByUserId) {
    this.paidByUserId = paidByUserId;
  }

  public String getPayeeUserId() {
    return payeeUserId;
  }

  public void setPayeeUserId(String payeeUserId) {
    this.payeeUserId = payeeUserId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}

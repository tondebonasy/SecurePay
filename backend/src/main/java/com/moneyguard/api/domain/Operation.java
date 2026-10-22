package com.moneyguard.api.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "operations")
public class Operation {
  @Id public String id;

  @Column(nullable = false, length = 40)
  public String kind;

  @Column(name = "transaction_id", length = 36)
  public String transactionId;

  @Column(nullable = false, length = 36)
  public String actor;

  @Column(nullable = false, length = 40)
  public String state;

  @Column(nullable = false, length = 4000)
  public String evidence;

  @Column(name = "created_at", nullable = false)
  public Instant createdAt;

  @Column(name = "dedup_key", unique = true, length = 160)
  public String dedupKey;
}

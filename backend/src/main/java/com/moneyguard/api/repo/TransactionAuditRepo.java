package com.moneyguard.api.repo;

import com.moneyguard.api.domain.TransactionAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionAuditRepo extends JpaRepository<TransactionAudit, String> {
    List<TransactionAudit> findByTransactionIdOrderByCreatedAtDesc(String transactionId);
}

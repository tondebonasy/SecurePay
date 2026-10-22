package com.moneyguard.api.repo;

import com.moneyguard.api.domain.TransactionProof;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionProofRepo extends JpaRepository<TransactionProof, String> {
    Optional<TransactionProof> findByTransactionId(String transactionId);
}

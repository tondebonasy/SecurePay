package com.moneyguard.api.repo;

import com.moneyguard.api.domain.BlockchainRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlockchainRecordRepo extends JpaRepository<BlockchainRecord, String> {
    Optional<BlockchainRecord> findByTxId(String txId);
}

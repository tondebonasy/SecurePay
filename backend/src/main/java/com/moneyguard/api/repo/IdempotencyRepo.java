package com.moneyguard.api.repo;

import com.moneyguard.api.domain.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface IdempotencyRepo extends JpaRepository<IdempotencyRecord, String> {
    Optional<IdempotencyRecord> findByUserIdAndIdemKey(String userId, String idemKey);
    List<IdempotencyRecord> findByExpiresAtBefore(Instant cutoff);
}

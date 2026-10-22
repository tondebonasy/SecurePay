package com.moneyguard.api.repo;

import com.moneyguard.api.domain.OutboxJob;
import com.moneyguard.api.domain.Enums;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface OutboxRepo extends JpaRepository<OutboxJob, String> {
    List<OutboxJob> findTop50ByStatusAndNextRunAtBeforeOrderByNextRunAtAsc(Enums.OutboxStatus status, Instant now);
}

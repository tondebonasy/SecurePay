package com.moneyguard.api.service;

import com.moneyguard.api.domain.Enums;
import com.moneyguard.api.domain.OutboxJob;
import com.moneyguard.api.repo.OutboxRepo;
import com.moneyguard.api.util.Crypto;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OutboxService {

    private final OutboxRepo repo;

    public OutboxService(OutboxRepo repo) {
        this.repo = repo;
    }

    public OutboxJob enqueue(Enums.OutboxType type, String payloadJson) {
        OutboxJob j = new OutboxJob();
        j.setId(Crypto.uuid());
        j.setJobType(type);
        j.setStatus(Enums.OutboxStatus.PENDING);
        j.setAttempts(0);
        j.setNextRunAt(Instant.now());
        j.setPayloadJson(payloadJson);
        return repo.save(j);
    }
}

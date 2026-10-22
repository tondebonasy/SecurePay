package com.moneyguard.api.jobs;

import com.moneyguard.api.repo.DeviceSessionRepo;
import com.moneyguard.api.repo.IdempotencyRepo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class MaintenanceJobs {

    private final DeviceSessionRepo sessionRepo;
    private final IdempotencyRepo idemRepo;

    public MaintenanceJobs(DeviceSessionRepo sessionRepo, IdempotencyRepo idemRepo) {
        this.sessionRepo = sessionRepo;
        this.idemRepo = idemRepo;
    }

    @Scheduled(fixedDelay = 600000) // 10 minutes
    @Transactional
    public void cleanup() {
        var expiredSessions = sessionRepo.findByRefreshExpiresAtBefore(Instant.now());
        for (var s : expiredSessions) {
            if (s.getRevokedAt() == null) s.setRevokedAt(Instant.now());
            sessionRepo.save(s);
        }

        var expiredIdem = idemRepo.findByExpiresAtBefore(Instant.now());
        idemRepo.deleteAll(expiredIdem);
    }
}

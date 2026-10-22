package com.moneyguard.api.jobs;

import com.moneyguard.api.domain.Enums;
import com.moneyguard.api.repo.OutboxRepo;
import com.moneyguard.api.service.EmailService;
import com.moneyguard.api.service.ExpoPushService;
import com.moneyguard.api.service.SmsService;
import com.moneyguard.api.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class OutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxRepo repo;
    private final Json json;
    private final EmailService email;
    private final SmsService sms;
    private final ExpoPushService expo;
    public OutboxProcessor(OutboxRepo repo, Json json, EmailService email, SmsService sms, ExpoPushService expo) {
        this.repo = repo;
        this.json = json;
        this.email = email;
        this.sms = sms;
        this.expo = expo;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void tick() {
        List<com.moneyguard.api.domain.OutboxJob> jobs = repo.findTop50ByStatusAndNextRunAtBeforeOrderByNextRunAtAsc(
                Enums.OutboxStatus.PENDING, Instant.now()
        );

        for (var job : jobs) {
            try {
                job.setStatus(Enums.OutboxStatus.RUNNING);
                repo.save(job);

                handle(job.getJobType(), job.getPayloadJson());

                job.setStatus(Enums.OutboxStatus.SUCCESS);
                job.setLastError(null);
                repo.save(job);
            } catch (Exception e) {
                job.setStatus(Enums.OutboxStatus.PENDING);
                job.setAttempts(job.getAttempts() + 1);
                job.setLastError(e.getMessage());
                job.setNextRunAt(Instant.now().plus(backoff(job.getAttempts())));
                repo.save(job);
                log.error("Outbox job failed type={} attempts={}", job.getJobType(), job.getAttempts(), e);
                if (job.getAttempts() >= 5) {
                    job.setStatus(Enums.OutboxStatus.FAILED);
                    repo.save(job);
                }
            }
        }
    }

    private Duration backoff(int attempts) {
        int secs = Math.min(300, (int) Math.pow(2, Math.max(1, attempts)));
        return Duration.ofSeconds(secs);
    }

    @SuppressWarnings("unchecked")
    private void handle(Enums.OutboxType type, String payloadJson) {
        Map<String, Object> payload = json.parse(payloadJson, Map.class);

        switch (type) {
            case SEND_EMAIL -> email.send(
                    (String) payload.get("to"),
                    (String) payload.getOrDefault("subject", "SecurePay"),
                    (String) payload.getOrDefault("text", "")
            );
            case SEND_SMS -> sms.send(
                    (String) payload.get("to"),
                    (String) payload.getOrDefault("text", "")
            );
            case SEND_EXPO -> {
                List<String> recipients = requireExpoRecipients(payload.get("to"));
                if (!recipients.isEmpty()) {
                    expo.send(
                            recipients,
                            (String) payload.getOrDefault("title", "SecurePay"),
                            (String) payload.getOrDefault("body", ""),
                            (Map<String, Object>) payload.getOrDefault("data", Map.of())
                    );
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> requireExpoRecipients(Object to) {
        if (to == null) return List.of();
        if (to instanceof String s) return List.of(s);
        if (to instanceof List<?> list) {
            return list.stream().filter(String.class::isInstance).map(String.class::cast).toList();
        }
        return List.of();
    }
}

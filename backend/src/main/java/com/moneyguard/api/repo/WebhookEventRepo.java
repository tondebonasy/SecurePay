package com.moneyguard.api.repo;

import com.moneyguard.api.domain.WebhookEvent;
import com.moneyguard.api.domain.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface WebhookEventRepo extends JpaRepository<WebhookEvent, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WebhookEvent> findByProviderAndEventId(Enums.WebhookProvider provider, String eventId);
}

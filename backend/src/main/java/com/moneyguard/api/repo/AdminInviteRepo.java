package com.moneyguard.api.repo;

import com.moneyguard.api.domain.AdminInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface AdminInviteRepo extends JpaRepository<AdminInvite, String> {
    Optional<AdminInvite> findTopByCodeHashAndConsumedAtIsNullAndExpiresAtAfter(String codeHash, Instant now);
}

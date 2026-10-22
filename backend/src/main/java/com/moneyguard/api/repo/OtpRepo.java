package com.moneyguard.api.repo;

import com.moneyguard.api.domain.OtpCode;
import com.moneyguard.api.domain.Enums;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface OtpRepo extends JpaRepository<OtpCode, String> {
    Optional<OtpCode> findTopBySubjectAndPurposeAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            String subject, Enums.OtpPurpose purpose, Instant now);
}

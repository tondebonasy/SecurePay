package com.moneyguard.api.repo;

import com.moneyguard.api.domain.PaymentRequest;
import com.moneyguard.api.domain.Enums;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PaymentRequestRepo extends JpaRepository<PaymentRequest, String> {
    Optional<PaymentRequest> findByIdAndStatus(String id, Enums.PaymentRequestStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PaymentRequest p where p.id = ?1 and p.status = ?2")
    Optional<PaymentRequest> findByIdAndStatusForUpdate(String id, Enums.PaymentRequestStatus status);
    List<PaymentRequest> findByStatusAndExpiresAtBefore(Enums.PaymentRequestStatus status, Instant cutoff);
}

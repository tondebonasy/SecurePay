package com.moneyguard.api.repo;

import com.moneyguard.api.domain.Tx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TxRepo extends JpaRepository<Tx, String>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Tx> {
    List<Tx> findByUserIdOrderByCreatedAtDesc(String userId);
    @Query("select t from Tx t where t.userId = ?1 or t.paidByUserId = ?1 or t.payeeUserId = ?1 order by t.createdAt desc")
    List<Tx> findForUser(String userId);
    Optional<Tx> findByUserIdAndIdempotencyKey(String userId, String idempotencyKey);
    Optional<Tx> findByPaypalOrderId(String paypalOrderId);
    Optional<Tx> findByPaynowReference(String paynowReference);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tx t where t.id = ?1")
    Optional<Tx> findForUpdateById(String id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tx t where t.paypalOrderId = ?1")
    Optional<Tx> findForUpdateByPaypalOrderId(String orderId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Tx t where t.paynowReference = ?1")
    Optional<Tx> findForUpdateByPaynowReference(String reference);
    long countByStatus(com.moneyguard.api.domain.Enums.TxStatus status);
    long countByStatusIn(Collection<com.moneyguard.api.domain.Enums.TxStatus> statuses);
    long countByPaypalCaptureIdIsNotNull();
    List<Tx> findAllByOrderByCreatedAtDesc();
    List<Tx> findByStatusOrderByCreatedAtDesc(com.moneyguard.api.domain.Enums.TxStatus status);
    List<Tx> findByStatus(com.moneyguard.api.domain.Enums.TxStatus status);

    @Query("select coalesce(sum(t.amount), 0) from Tx t")
    java.math.BigDecimal sumAmount();

    @Query("select coalesce(sum(t.amount), 0) from Tx t where t.status = ?1")
    java.math.BigDecimal sumAmountByStatus(com.moneyguard.api.domain.Enums.TxStatus status);

    @Query("select coalesce(sum(t.amount), 0) from Tx t where t.status in ?1")
    java.math.BigDecimal sumAmountByStatusIn(Collection<com.moneyguard.api.domain.Enums.TxStatus> statuses);
}

package com.moneyguard.api.service;

import com.moneyguard.api.domain.Enums;
import com.moneyguard.api.domain.TransactionAudit;
import com.moneyguard.api.domain.TransactionProof;
import com.moneyguard.api.domain.Tx;
import com.moneyguard.api.dto.TransactionDtos;
import com.moneyguard.api.repo.TransactionAuditRepo;
import com.moneyguard.api.repo.TransactionProofRepo;
import com.moneyguard.api.repo.TxRepo;
import com.moneyguard.api.util.Crypto;
import com.moneyguard.api.web.ApiException;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

  private final TxRepo txRepo;
  private final TransactionAuditRepo auditRepo;
  private final TransactionProofRepo proofRepo;
  private final PayPalService payPal;
  private final PaynowService paynow;
  private final StellarClient stellarClient;
  private final ExpoPushService expoPushService;
  private final DeviceService deviceService;
  private final UserService userService;

  public TransactionService(
      TxRepo txRepo,
      TransactionAuditRepo auditRepo,
      TransactionProofRepo proofRepo,
      PayPalService payPal,
      PaynowService paynow,
      StellarClient stellarClient,
      ExpoPushService expoPushService,
      DeviceService deviceService,
      UserService userService) {
    this.txRepo = txRepo;
    this.auditRepo = auditRepo;
    this.proofRepo = proofRepo;
    this.payPal = payPal;
    this.paynow = paynow;
    this.stellarClient = stellarClient;
    this.expoPushService = expoPushService;
    this.deviceService = deviceService;
    this.userService = userService;
  }

  public record InitiateResult(Tx tx, String approveUrl) {}

  @Transactional
  public InitiateResult initiate(
      String userId,
      BigDecimal amount,
      String currency,
      String idempotencyKey,
      String returnUrl,
      String cancelUrl) {
    return initiate(userId, amount, currency, idempotencyKey, returnUrl, cancelUrl, userId, userId);
  }

  public InitiateResult initiate(
      String userId,
      BigDecimal amount,
      String currency,
      String idempotencyKey,
      String returnUrl,
      String cancelUrl,
      String paidByUserId,
      String payeeUserId) {
    if (amount == null || amount.signum() <= 0) {
      throw ApiException.badRequest("AMOUNT_INVALID", "Amount must be greater than 0");
    }
    if (currency == null || currency.isBlank()) currency = "USD";

    if (idempotencyKey != null && !idempotencyKey.isBlank()) {
      var existing = txRepo.findByUserIdAndIdempotencyKey(userId, idempotencyKey);
      if (existing.isPresent()) return new InitiateResult(existing.get(), null);
    }

    Tx tx = new Tx();
    tx.setId(Crypto.uuid());
    tx.setUserId(userId);
    tx.setPaidByUserId(paidByUserId);
    tx.setPayeeUserId(payeeUserId);
    tx.setAmount(amount);
    tx.setCurrency(currency.toUpperCase());
    tx.setStatus(Enums.TxStatus.CREATED);
    tx.setIdempotencyKey(idempotencyKey);
    Instant now = Instant.now();
    tx.setCreatedAt(now);
    tx.setUpdatedAt(now);
    txRepo.save(tx);
    audit(tx.getId(), null, Enums.TxStatus.CREATED, "created");

    var order =
        payPal.createOrder(
            amount.toPlainString(), tx.getCurrency(), returnUrl, cancelUrl, tx.getId());
    tx.setPaypalOrderId(order.id());
    tx.setStatus(Enums.TxStatus.PAYPAL_ORDER_CREATED);
    txRepo.save(tx);
    audit(
        tx.getId(),
        Enums.TxStatus.CREATED,
        Enums.TxStatus.PAYPAL_ORDER_CREATED,
        "paypal order created");

    return new InitiateResult(tx, order.approveUrl());
  }

  @Transactional
  public Tx confirm(String txId) {
    Tx tx = getOrThrow(txId);
    if (tx.getPaypalOrderId() == null) {
      throw ApiException.badRequest("PAYPAL_ORDER_MISSING", "PayPal order missing");
    }

    if (tx.getPaypalCaptureId() != null || OperationsService.paid(tx)) return tx;
    var capture = payPal.captureOrder(tx.getPaypalOrderId());
    if (!"COMPLETED".equalsIgnoreCase(capture.status())) {
      tx.setStatus(Enums.TxStatus.FAILED);
      txRepo.save(tx);
      audit(
          tx.getId(),
          Enums.TxStatus.PAYPAL_ORDER_CREATED,
          Enums.TxStatus.FAILED,
          "paypal capture failed");
      notifyFailure(tx, "Your transaction could not be completed.");
      return tx;
    }

    tx.setPaypalCaptureId(capture.captureId());
    return finalizeAfterPayment(
        tx, Enums.TxStatus.PAYPAL_ORDER_CREATED, Enums.TxStatus.PAYPAL_CAPTURED, "paypal captured");
  }

  public Tx getOrThrow(String id) {
    return txRepo
        .findById(id)
        .orElseThrow(() -> ApiException.notFound("TX_NOT_FOUND", "Transaction not found"));
  }

  public TransactionProof getProofOrThrow(String id) {
    return proofRepo
        .findByTransactionId(id)
        .orElseThrow(() -> ApiException.notFound("PROOF_NOT_FOUND", "Proof not found"));
  }

  public TransactionDtos.ProofVerifyResponse verifyProof(String txId) {
    Tx tx = getOrThrow(txId);
    TransactionProof proof = getProofOrThrow(txId);
    String receiptHash = Crypto.sha256Hex(canonicalPayload(tx));
    var res = stellarClient.verify(receiptHash, proof.getStellarTxHash());
    return new TransactionDtos.ProofVerifyResponse(res.verified(), res.reason());
  }

  public java.util.List<TransactionDtos.TransactionResponse> listForUser(String userId) {
    return txRepo.findForUser(userId).stream().map(tx -> mapResponse(tx, null)).toList();
  }

  private void sendNotification(Tx tx, String title, String body) {
    var payload = new java.util.HashMap<String, Object>();
    payload.put("txId", tx.getId());
    payload.put("status", tx.getStatus().name());
    var recipients = new java.util.HashSet<String>();
    if (tx.getPaidByUserId() != null) recipients.add(tx.getPaidByUserId());
    if (tx.getPayeeUserId() != null) recipients.add(tx.getPayeeUserId());
    for (String userId : recipients) {
      var tokens = deviceService.findExpoTokensForUser(userId);
      if (!tokens.isEmpty()) {
        expoPushService.send(tokens, title, body, payload);
      }
    }
  }

  private void notifyFailure(Tx tx, String body) {
    sendNotification(tx, "Payment failed", body);
  }

  private void audit(
      String txId, Enums.TxStatus oldStatus, Enums.TxStatus newStatus, String message) {
    TransactionAudit audit = new TransactionAudit();
    audit.setId(Crypto.uuid());
    audit.setTransactionId(txId);
    audit.setOldStatus(oldStatus == null ? null : oldStatus.name());
    audit.setNewStatus(newStatus.name());
    audit.setMessage(message);
    auditRepo.save(audit);
  }

  @Transactional
  public void handlePaypalWebhook(String orderId, String status) {
    if (orderId == null || orderId.isBlank()) return;
    txRepo
        .findByPaypalOrderId(orderId)
        .ifPresent(
            tx -> {
              if ("COMPLETED".equalsIgnoreCase(status)
                  && tx.getStatus() == Enums.TxStatus.PAYPAL_ORDER_CREATED) {
                confirm(tx.getId());
              }
            });
  }

  @Transactional
  public TransactionDtos.TransactionResponse initiatePaynowRemote(
      String userId, BigDecimal amount, String currency, String phone, String method) {
    Tx tx = initiatePaynowRemoteInternal(userId, amount, currency, phone, method, userId, userId);
    return mapResponse(tx, null);
  }

  @Transactional
  public TransactionDtos.TransactionResponse initiatePaynowRemote(
      String userId,
      BigDecimal amount,
      String currency,
      String phone,
      String method,
      String payeePhone) {
    if (payeePhone == null || payeePhone.isBlank()) {
      throw ApiException.badRequest("PAYEE_PHONE_REQUIRED", "Receiving user phone required");
    }
    String payeeUserId = userService.getByPhoneOrThrow(payeePhone.trim()).getId();
    Tx tx =
        initiatePaynowRemoteInternal(userId, amount, currency, phone, method, userId, payeeUserId);
    return mapResponse(tx, null);
  }

  @Transactional
  public Tx initiatePaynowRemoteForRequest(
      String userId,
      BigDecimal amount,
      String currency,
      String phone,
      String method,
      String payeeUserId) {
    return initiatePaynowRemoteInternal(
        userId, amount, currency, phone, method, userId, payeeUserId);
  }

  private Tx initiatePaynowRemoteInternal(
      String userId,
      BigDecimal amount,
      String currency,
      String phone,
      String method,
      String paidByUserId,
      String payeeUserId) {
    if (amount == null || amount.signum() <= 0) {
      throw ApiException.badRequest("AMOUNT_INVALID", "Amount must be greater than 0");
    }
    if (phone == null || phone.isBlank()) {
      throw ApiException.badRequest("PHONE_REQUIRED", "Phone required");
    }
    if (method == null || method.isBlank()) {
      throw ApiException.badRequest("PAYNOW_METHOD_REQUIRED", "Paynow method required");
    }
    if (currency == null || currency.isBlank()) currency = "USD";

    Tx tx = new Tx();
    tx.setId(Crypto.uuid());
    tx.setUserId(userId);
    tx.setPaidByUserId(paidByUserId);
    tx.setPayeeUserId(payeeUserId);
    tx.setAmount(amount);
    tx.setCurrency(currency.toUpperCase());
    tx.setStatus(Enums.TxStatus.CREATED);
    tx.setPaynowMethod(method.toLowerCase());
    tx.setPaynowPhone(phone);
    tx.setPaynowReference(tx.getId());
    Instant now = Instant.now();
    tx.setCreatedAt(now);
    tx.setUpdatedAt(now);
    txRepo.save(tx);
    audit(tx.getId(), null, Enums.TxStatus.CREATED, "created");

    var res =
        paynow.remoteTransaction(
            tx.getPaynowReference(), amount.toPlainString(), phone, method.toLowerCase());
    if (!"Ok".equalsIgnoreCase(res.status())) {
      tx.setStatus(Enums.TxStatus.PAYNOW_FAILED);
      txRepo.save(tx);
      audit(
          tx.getId(),
          Enums.TxStatus.CREATED,
          Enums.TxStatus.PAYNOW_FAILED,
          "paynow request failed");
      notifyFailure(tx, "The PayNow request failed before payment was completed.");
      throw ApiException.badRequest(
          "PAYNOW_REQUEST_FAILED", res.fields().getOrDefault("error", "Paynow request failed"));
    }
    if (!res.verified()) {
      tx.setStatus(Enums.TxStatus.PAYNOW_FAILED);
      txRepo.save(tx);
      audit(
          tx.getId(), Enums.TxStatus.CREATED, Enums.TxStatus.PAYNOW_FAILED, "paynow hash invalid");
      notifyFailure(tx, "The PayNow request could not be verified.");
      throw ApiException.badRequest("PAYNOW_HASH_INVALID", "Paynow response hash invalid");
    }
    if (res.pollUrl() == null || res.pollUrl().isBlank()) {
      tx.setStatus(Enums.TxStatus.PAYNOW_FAILED);
      txRepo.save(tx);
      audit(
          tx.getId(),
          Enums.TxStatus.CREATED,
          Enums.TxStatus.PAYNOW_FAILED,
          "paynow poll url missing");
      notifyFailure(tx, "The PayNow request could not be completed.");
      throw ApiException.badRequest("PAYNOW_POLL_URL_MISSING", "Paynow poll URL missing");
    }
    tx.setPaynowPollUrl(res.pollUrl());
    tx.setStatus(Enums.TxStatus.PAYNOW_REQUESTED);
    txRepo.save(tx);
    audit(tx.getId(), Enums.TxStatus.CREATED, Enums.TxStatus.PAYNOW_REQUESTED, "paynow requested");

    return tx;
  }

  @Transactional
  public TransactionDtos.TransactionResponse pollPaynow(String txId) {
    Tx tx = getOrThrow(txId);
    if (tx.getPaynowPollUrl() == null || tx.getPaynowPollUrl().isBlank()) {
      throw ApiException.badRequest("PAYNOW_POLL_URL_MISSING", "Paynow poll URL missing");
    }
    var res = paynow.pollStatus(tx.getPaynowPollUrl());
    if (!res.verified()) {
      throw ApiException.badRequest("PAYNOW_HASH_INVALID", "Paynow response hash invalid");
    }
    applyPaynowStatus(tx, res.status());
    return mapResponse(tx, null);
  }

  @Transactional
  public TransactionDtos.TransactionResponse tracePaynow(String txId) {
    Tx tx = getOrThrow(txId);
    if (tx.getPaynowReference() == null || tx.getPaynowReference().isBlank()) {
      throw ApiException.badRequest("PAYNOW_REF_REQUIRED", "Paynow reference missing");
    }
    var res = paynow.trace(tx.getPaynowReference());
    if (!res.verified()) {
      throw ApiException.badRequest("PAYNOW_HASH_INVALID", "Paynow response hash invalid");
    }
    applyPaynowStatus(tx, res.status());
    return mapResponse(tx, null);
  }

  @Transactional
  public void handlePaynowWebhook(String reference, String status) {
    if (reference == null || reference.isBlank()) return;
    txRepo.findByPaynowReference(reference).ifPresent(tx -> applyPaynowStatus(tx, status));
  }

  public TransactionDtos.TransactionResponse mapResponse(Tx tx, String approveUrl) {
    var paidBy = userService.getByIdOrNull(tx.getPaidByUserId());
    var payee = userService.getByIdOrNull(tx.getPayeeUserId());
    return new TransactionDtos.TransactionResponse(
        tx.getId(),
        tx.getUserId(),
        tx.getPaidByUserId(),
        tx.getPayeeUserId(),
        paidBy == null ? null : paidBy.getPhone(),
        paidBy == null ? null : paidBy.getDisplayName(),
        payee == null ? null : payee.getPhone(),
        payee == null ? null : payee.getDisplayName(),
        tx.getAmount(),
        tx.getCurrency(),
        tx.getStatus().name(),
        tx.getPaypalOrderId(),
        tx.getPaypalCaptureId(),
        tx.getPaynowReference(),
        tx.getPaynowMethod(),
        tx.getPaynowPhone(),
        approveUrl,
        tx.getCreatedAt() == null ? null : tx.getCreatedAt().toString(),
        tx.getUpdatedAt() == null ? null : tx.getUpdatedAt().toString());
  }

  private String canonicalPayload(Tx tx) {
    String paymentRef =
        tx.getPaypalCaptureId() != null ? tx.getPaypalCaptureId() : tx.getPaynowReference();
    String method =
        tx.getPaypalCaptureId() != null
            ? "paypal"
            : (tx.getPaynowMethod() == null ? "paynow" : tx.getPaynowMethod());
    return tx.getId()
        + "|"
        + tx.getUserId()
        + "|"
        + tx.getAmount().toPlainString()
        + "|"
        + tx.getCurrency()
        + "|"
        + tx.getCreatedAt()
        + "|"
        + method
        + "|"
        + paymentRef;
  }

  private Tx finalizeAfterPayment(
      Tx tx, Enums.TxStatus from, Enums.TxStatus paidStatus, String message) {
    tx.setStatus(paidStatus);
    txRepo.save(tx);
    audit(tx.getId(), from, paidStatus, message);

    String proofHash = Crypto.sha256Hex(canonicalPayload(tx));
    var anchor = stellarClient.anchor(tx.getId(), proofHash);
    if (anchor.anchored() && anchor.stellarTxHash() != null && !anchor.stellarTxHash().isBlank()) {
      TransactionProof proof = new TransactionProof();
      proof.setId(Crypto.uuid());
      proof.setTransactionId(tx.getId());
      proof.setProofHash(proofHash);
      proof.setStellarTxHash(anchor.stellarTxHash());
      proof.setLedger(anchor.ledger());
      proofRepo.save(proof);
      tx.setStatus(Enums.TxStatus.PROOF_SUBMITTED);
      txRepo.save(tx);
      audit(
          tx.getId(),
          paidStatus,
          Enums.TxStatus.PROOF_SUBMITTED,
          "receipt integrity record submitted");
    }
    sendNotification(
        tx,
        "Payment confirmed",
        "Your provider payment is confirmed. Receipt anchoring depends on service availability.");
    Enums.TxStatus beforeNotification = tx.getStatus();
    tx.setStatus(Enums.TxStatus.NOTIFIED);
    txRepo.save(tx);
    audit(tx.getId(), beforeNotification, Enums.TxStatus.NOTIFIED, "notified");

    return tx;
  }

  private void applyPaynowStatus(Tx tx, String status) {
    if (status == null) return;
    String normalized = status.trim().toLowerCase();
    if (java.util.Set.of("paid", "awaiting delivery", "delivered").contains(normalized)) {
      if (tx.getStatus() == Enums.TxStatus.NOTIFIED
          || tx.getStatus() == Enums.TxStatus.PROOF_SUBMITTED) {
        return;
      }
      finalizeAfterPayment(tx, tx.getStatus(), Enums.TxStatus.PAYNOW_PAID, "paynow paid");
      return;
    }
    if (normalized.contains("cancel")
        || normalized.contains("failed")
        || normalized.contains("error")) {
      Enums.TxStatus old = tx.getStatus();
      if (OperationsService.paid(tx)) return;
      if (old == Enums.TxStatus.PAYNOW_FAILED) {
        return;
      }
      tx.setStatus(Enums.TxStatus.PAYNOW_FAILED);
      txRepo.save(tx);
      audit(tx.getId(), old, Enums.TxStatus.PAYNOW_FAILED, "paynow failed");
      notifyFailure(tx, "Your PayNow transaction failed or was cancelled.");
    }
  }
}

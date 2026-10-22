package com.moneyguard.api.service;

import com.moneyguard.api.domain.*;
import com.moneyguard.api.repo.*;
import com.moneyguard.api.web.ApiException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationsService {
  private final TxRepo txs;
  private final OperationRepo ops;

  public OperationsService(TxRepo txs, OperationRepo ops) {
    this.txs = txs;
    this.ops = ops;
  }

  public static boolean paid(Tx t) {
    return t.getPaypalCaptureId() != null
        || Set.of(
                Enums.TxStatus.PAYPAL_CAPTURED,
                Enums.TxStatus.PAYNOW_PAID,
                Enums.TxStatus.PROOF_SUBMITTED,
                Enums.TxStatus.NOTIFIED)
            .contains(t.getStatus());
  }

  public static String reference(Tx t) {
    return t.getPaynowReference() != null
        ? t.getPaynowReference()
        : t.getPaypalCaptureId() != null ? t.getPaypalCaptureId() : t.getPaypalOrderId();
  }

  public static String match(Tx t, BigDecimal amount, String currency) {
    if (!t.getCurrency().equals(currency)) return "CURRENCY_MISMATCH";
    if (t.getAmount().compareTo(amount) != 0) return "AMOUNT_MISMATCH";
    return "MATCHED";
  }

  public Operation record(
      String kind, String tx, String actor, String state, String evidence, String dedup) {
    Operation o = new Operation();
    o.id = UUID.randomUUID().toString();
    o.kind = kind;
    o.transactionId = tx;
    o.actor = actor;
    o.state = state;
    o.evidence = evidence;
    o.createdAt = Instant.now();
    o.dedupKey = dedup;
    return ops.save(o);
  }

  @Transactional
  public Operation verify(String id, String actor) {
    Tx t =
        txs.findById(id)
            .orElseThrow(() -> ApiException.notFound("TX_NOT_FOUND", "Transaction not found"));
    // Stored references and uploaded receipts cannot establish provider authenticity.
    return record(
        "VERIFICATION",
        id,
        actor,
        "REQUIRES_MANUAL_REVIEW",
        "Stored reference: "
            + Objects.toString(reference(t), "none")
            + ". No fresh trusted provider response was obtained. Receipt authenticity is not"
            + " payment verification.",
        null);
  }

  @Transactional
  public Operation refund(String id, String actor, String reason) {
    Tx t =
        txs.findById(id)
            .orElseThrow(() -> ApiException.notFound("TX_NOT_FOUND", "Transaction not found"));
    if (reason == null || reason.trim().length() < 10 || reason.length() > 500)
      throw ApiException.badRequest("REASON_INVALID", "Refund reason must be 10–500 characters");
    if (!paid(t))
      throw ApiException.badRequest(
          "NOT_ELIGIBLE", "Only provider-paid transactions are eligible for a refund request");
    String key = "REFUND:" + id;
    if (ops.existsByDedupKey(key))
      throw ApiException.conflict("REFUND_EXISTS", "A refund request already exists");
    return record(
        "REFUND",
        id,
        actor,
        "MANUAL_REQUESTED",
        "No funds moved. External provider refund required. Reason: " + reason.trim(),
        key);
  }

  @Transactional
  public List<Operation> reconcile(String csv, String actor) {
    if (csv == null || csv.length() > 200000)
      throw ApiException.badRequest("CSV_SIZE", "Settlement CSV must be under 200 KB");
    String[] lines = csv.strip().split("\\R");
    if (lines.length < 2
        || lines.length > 1001
        || !lines[0].trim().equals("reference,amount,currency"))
      throw ApiException.badRequest(
          "CSV_FORMAT",
          "Use reference,amount,currency header and 1–1000 rows; unquoted fields only");
    record Row(String ref, BigDecimal amount, String currency) {}
    ;
    List<Row> rows = new ArrayList<>();
    for (int i = 1; i < lines.length; i++) {
      String[] cells = lines[i].split(",", -1);
      if (cells.length != 3
          || !cells[0].matches("[A-Za-z0-9_-]{1,64}")
          || !cells[1].matches("[0-9]{1,16}(\\.[0-9]{1,2})?")
          || !cells[2].matches("[A-Z]{3}"))
        throw ApiException.badRequest("CSV_ROW", "Invalid settlement row " + (i + 1));
      BigDecimal amount = new BigDecimal(cells[1]);
      if (amount.signum() <= 0)
        throw ApiException.badRequest("CSV_AMOUNT", "Settlement amounts must be positive");
      rows.add(new Row(cells[0], amount, cells[2]));
    }
    String run = UUID.randomUUID().toString();
    List<Tx> all = txs.findAll();
    List<Operation> results = new ArrayList<>();
    for (Row r : rows) {
      List<Tx> matches = all.stream().filter(t -> r.ref.equals(reference(t))).toList();
      String state =
          rows.stream().filter(x -> x.ref.equals(r.ref)).count() > 1 || matches.size() > 1
              ? "DUPLICATE_REFERENCE"
              : matches.isEmpty()
                  ? "MISSING_INTERNAL"
                  : match(matches.get(0), r.amount, r.currency);
      results.add(
          record(
              "RECONCILIATION",
              matches.size() == 1 ? matches.get(0).getId() : null,
              actor,
              state,
              "Run "
                  + run
                  + "; imported settlement (not provider-authenticated): "
                  + r.ref
                  + " "
                  + r.amount
                  + " "
                  + r.currency,
              null));
    }
    Set<String> refs = new HashSet<>();
    rows.forEach(r -> refs.add(r.ref));
    for (Tx t : all)
      if (paid(t) && reference(t) != null && !refs.contains(reference(t)))
        results.add(
            record(
                "RECONCILIATION",
                t.getId(),
                actor,
                "MISSING_SETTLEMENT",
                "Run " + run + "; paid internal record absent from this imported settlement",
                null));
    return results;
  }
}

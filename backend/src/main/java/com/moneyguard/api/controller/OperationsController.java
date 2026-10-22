package com.moneyguard.api.controller;

import com.moneyguard.api.domain.*;
import com.moneyguard.api.repo.*;
import com.moneyguard.api.security.AuthUser;
import com.moneyguard.api.service.OperationsService;
import com.moneyguard.api.web.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/admin/operations")
public class OperationsController {
  private final TxRepo txs;
  private final OperationRepo ops;
  private final OperationsService service;

  public OperationsController(TxRepo txs, OperationRepo ops, OperationsService service) {
    this.txs = txs;
    this.ops = ops;
    this.service = service;
  }

  private Specification<Tx> filter(
      String currency, String status, String q, String method, Instant from, Instant to) {
    return (root, query, cb) -> {
      var terms = new ArrayList<jakarta.persistence.criteria.Predicate>();
      if (currency != null && !currency.isBlank()) {
        if (!currency.matches("[A-Z]{3}"))
          throw ApiException.badRequest(
              "CURRENCY_INVALID", "Use a three-letter uppercase currency");
        terms.add(cb.equal(root.get("currency"), currency));
      }
      if (status != null && !status.isBlank()) {
        try {
          terms.add(cb.equal(root.get("status"), Enums.TxStatus.valueOf(status)));
        } catch (IllegalArgumentException e) {
          throw ApiException.badRequest("STATUS_INVALID", "Invalid status");
        }
      }
      if (q != null && !q.isBlank()) {
        if (q.length() > 64)
          throw ApiException.badRequest("SEARCH_INVALID", "Search must be at most 64 characters");
        String term = "%" + q.toLowerCase().replace("%", "").replace("_", "") + "%";
        terms.add(
            cb.or(
                cb.like(cb.lower(root.get("id")), term),
                cb.like(cb.lower(root.get("paynowReference")), term),
                cb.like(cb.lower(root.get("paypalCaptureId")), term)));
      }
      if (method != null && !method.isBlank()) {
        if (method.equals("PAYNOW")) terms.add(cb.isNotNull(root.get("paynowReference")));
        else if (method.equals("PAYPAL")) terms.add(cb.isNotNull(root.get("paypalOrderId")));
        else throw ApiException.badRequest("METHOD_INVALID", "Use PAYPAL or PAYNOW");
      }
      if (from != null) terms.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
      if (to != null) terms.add(cb.lessThan(root.get("createdAt"), to));
      if (from != null && to != null && !from.isBefore(to))
        throw ApiException.badRequest("DATE_INVALID", "End must be after start");
      return cb.and(terms.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };
  }

  @GetMapping("/transactions")
  public ApiResponse<?> transactions(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(defaultValue = "createdAt") String sort,
      @RequestParam(defaultValue = "desc") String direction,
      @RequestParam(required = false) String currency,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String method,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to) {
    if (page < 0
        || size < 1
        || size > 100
        || !Set.of("createdAt", "amount", "status").contains(sort)
        || !Set.of("asc", "desc").contains(direction))
      throw ApiException.badRequest("PAGE_INVALID", "Invalid pagination or sorting");
    var data =
        txs.findAll(
            filter(currency, status, q, method, from, to),
            PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.fromString(direction), sort).and(Sort.by("id"))));
    return ApiResponse.ok(
        Map.of(
            "content",
            data.getContent(),
            "totalElements",
            data.getTotalElements(),
            "totalPages",
            data.getTotalPages()));
  }

  @GetMapping(value = "/export", produces = "text/csv")
  public org.springframework.http.ResponseEntity<String> export(
      @RequestParam(required = false) String currency,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String method,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to) {
    var selected =
        txs.findAll(
            filter(currency, status, q, method, from, to),
            PageRequest.of(0, 10001, Sort.by(Sort.Direction.DESC, "createdAt")));
    if (selected.getTotalElements() > 10000)
      throw ApiException.badRequest(
          "EXPORT_LIMIT", "Narrow filters to at most 10,000 transactions before exporting");
    StringBuilder csv = new StringBuilder("id,amount,currency,status,createdAt,reference\r\n");
    for (Tx t : selected) {
      String[] cells = {
        t.getId(),
        t.getAmount().toPlainString(),
        t.getCurrency(),
        t.getStatus().name(),
        t.getCreatedAt().toString(),
        Objects.toString(OperationsService.reference(t), "")
      };
      csv.append(
              Arrays.stream(cells)
                  .map(this::csvCell)
                  .collect(java.util.stream.Collectors.joining(",")))
          .append("\r\n");
    }
    return org.springframework.http.ResponseEntity.ok()
        .header("Content-Disposition", "attachment; filename=securepay-transactions.csv")
        .body(csv.toString());
  }

  private String csvCell(String value) {
    String safe = value.matches("(?s)^[\\s]*[=+@-].*") ? "'" + value : value;
    return "\"" + safe.replace("\"", "\"\"") + "\"";
  }

  @GetMapping("/analytics")
  public ApiResponse<?> analytics(
      @RequestParam(defaultValue = "USD") String currency,
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to) {
    var data = txs.findAll(filter(currency, null, null, null, from, to));
    Map<String, Long> statuses = new TreeMap<>(), methods = new TreeMap<>();
    Map<String, BigDecimal> daily = new TreeMap<>();
    BigDecimal total = BigDecimal.ZERO;
    long success = 0, failed = 0;
    for (Tx t : data) {
      total = total.add(t.getAmount());
      if (OperationsService.paid(t)) success++;
      else if (Set.of(Enums.TxStatus.FAILED, Enums.TxStatus.PAYNOW_FAILED).contains(t.getStatus()))
        failed++;
      statuses.merge(t.getStatus().name(), 1L, Long::sum);
      methods.merge(
          t.getPaynowReference() != null
              ? "PAYNOW"
              : t.getPaypalOrderId() != null ? "PAYPAL" : "UNASSIGNED",
          1L,
          Long::sum);
      daily.merge(
          t.getCreatedAt().atZone(ZoneOffset.UTC).toLocalDate().toString(),
          t.getAmount(),
          BigDecimal::add);
    }
    return ApiResponse.ok(
        Map.of(
            "currency",
            currency,
            "totalTx",
            data.size(),
            "totalValue",
            total,
            "successful",
            success,
            "failed",
            failed,
            "pending",
            data.size() - success - failed,
            "successRate",
            data.isEmpty() ? 0 : 100.0 * success / data.size(),
            "statuses",
            statuses,
            "methods",
            methods,
            "daily",
            daily));
  }

  @GetMapping("/history/{kind}")
  public ApiResponse<?> history(@PathVariable String kind) {
    if (!Set.of("VERIFICATION", "REFUND", "RECONCILIATION", "RISK_REVIEW").contains(kind))
      throw ApiException.badRequest("KIND_INVALID", "Invalid history type");
    return ApiResponse.ok(ops.findTop100ByKindOrderByCreatedAtDesc(kind));
  }

  public record Reason(String reason) {}

  public record Settlement(String csv) {}

  @PostMapping("/transactions/{id}/verify")
  public ApiResponse<?> verify(@PathVariable String id, @AuthenticationPrincipal AuthUser user) {
    return ApiResponse.ok(service.verify(id, user.getUserId()));
  }

  @PostMapping("/transactions/{id}/refund")
  public ApiResponse<?> refund(
      @PathVariable String id, @RequestBody Reason req, @AuthenticationPrincipal AuthUser user) {
    return ApiResponse.ok(service.refund(id, user.getUserId(), req.reason()));
  }

  @PostMapping("/reconcile")
  public ApiResponse<?> reconcile(
      @RequestBody Settlement req, @AuthenticationPrincipal AuthUser user) {
    return ApiResponse.ok(service.reconcile(req.csv(), user.getUserId()));
  }

  @GetMapping("/risk")
  public ApiResponse<?> risk(
      @org.springframework.beans.factory.annotation.Value("${app.risk.high-amount:10000}")
          BigDecimal threshold) {
    List<Map<String, Object>> flags = new ArrayList<>();
    var all = txs.findAll();
    for (Tx t : all) {
      List<String> reasons = new ArrayList<>();
      if (t.getAmount().compareTo(threshold) > 0)
        reasons.add("Amount exceeds " + threshold + " " + t.getCurrency() + " threshold");
      String ref = OperationsService.reference(t);
      if (ref != null
          && all.stream().filter(x -> ref.equals(OperationsService.reference(x))).count() > 1)
        reasons.add("Duplicate provider reference");
      if (all.stream()
              .filter(
                  x ->
                      x.getUserId().equals(t.getUserId())
                          && Set.of(Enums.TxStatus.FAILED, Enums.TxStatus.PAYNOW_FAILED)
                              .contains(x.getStatus())
                          && x.getCreatedAt().isAfter(Instant.now().minusSeconds(86400)))
              .count()
          >= 3) reasons.add("At least three failed payments in 24 hours");
      if (!reasons.isEmpty())
        flags.add(
            Map.of(
                "transactionId",
                t.getId(),
                "currency",
                t.getCurrency(),
                "amount",
                t.getAmount(),
                "rules",
                reasons));
    }
    return ApiResponse.ok(flags);
  }

  @PostMapping("/transactions/{id}/risk-review")
  public ApiResponse<?> review(
      @PathVariable String id, @RequestBody Reason req, @AuthenticationPrincipal AuthUser user) {
    if (!txs.existsById(id)) throw ApiException.notFound("TX_NOT_FOUND", "Transaction not found");
    if (req.reason() == null || req.reason().length() < 10 || req.reason().length() > 500)
      throw ApiException.badRequest(
          "REASON_INVALID", "Investigation note must be 10–500 characters");
    return ApiResponse.ok(
        service.record("RISK_REVIEW", id, user.getUserId(), "REVIEWED", req.reason(), null));
  }
}

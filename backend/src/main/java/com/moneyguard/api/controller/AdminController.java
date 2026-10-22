package com.moneyguard.api.controller;

import com.moneyguard.api.domain.Enums;
import com.moneyguard.api.domain.TransactionAudit;
import com.moneyguard.api.dto.AdminTransactionDtos;
import com.moneyguard.api.dto.AdminUserDtos;
import com.moneyguard.api.repo.TransactionAuditRepo;
import com.moneyguard.api.repo.TransactionProofRepo;
import com.moneyguard.api.repo.TxRepo;
import com.moneyguard.api.repo.UserRepo;
import com.moneyguard.api.security.AuthUser;
import com.moneyguard.api.service.UserService;
import com.moneyguard.api.util.Crypto;
import com.moneyguard.api.web.ApiException;
import com.moneyguard.api.web.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/admin")
public class AdminController {

    private final TxRepo txRepo;
    private final UserRepo userRepo;
    private final UserService userService;
    private final TransactionProofRepo proofRepo;
    private final TransactionAuditRepo auditRepo;

    public AdminController(TxRepo txRepo, UserRepo userRepo, UserService userService,
                           TransactionProofRepo proofRepo, TransactionAuditRepo auditRepo) {
        this.txRepo = txRepo;
        this.userRepo = userRepo;
        this.userService = userService;
        this.proofRepo = proofRepo;
        this.auditRepo = auditRepo;
    }

    private void ensureAdmin(AuthUser user) {
        if (user == null || !user.getRoles().contains(Enums.Role.ADMIN.name())) {
            throw ApiException.forbidden("FORBIDDEN", "Admin only");
        }
    }

    @GetMapping("/metrics")
    public ApiResponse<Object> metrics(@AuthenticationPrincipal AuthUser user) {
        ensureAdmin(user);
        Map<String, Object> metrics = new LinkedHashMap<>();
        metrics.put("totalTx", txRepo.count());
        metrics.put("volumeByCurrency", txRepo.findAll().stream().collect(java.util.stream.Collectors.groupingBy(
            com.moneyguard.api.domain.Tx::getCurrency, java.util.stream.Collectors.reducing(java.math.BigDecimal.ZERO,
            com.moneyguard.api.domain.Tx::getAmount, java.math.BigDecimal::add))));
        return ApiResponse.ok(metrics);
    }

    @GetMapping("/transactions")
    public ApiResponse<Object> transactions(@AuthenticationPrincipal AuthUser user,
                                            @RequestParam(required = false) String status) {
        ensureAdmin(user);
        if (status == null || status.isBlank()) {
            return ApiResponse.ok(txRepo.findAllByOrderByCreatedAtDesc());
        }
        try {
            var st = Enums.TxStatus.valueOf(status.trim().toUpperCase());
            return ApiResponse.ok(txRepo.findByStatusOrderByCreatedAtDesc(st));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("STATUS_INVALID", "Invalid status");
        }
    }

    @GetMapping("/transactions/{id}")
    public ApiResponse<Object> transactionDetail(@PathVariable String id,
                                                 @AuthenticationPrincipal AuthUser user) {
        ensureAdmin(user);
        var tx = txRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("TX_NOT_FOUND", "Transaction not found"));
        return ApiResponse.ok(transactionDetailPayload(tx));
    }

    @PatchMapping("/transactions/{id}")
    @Transactional
    public ApiResponse<Object> updateTransaction(@PathVariable String id,
                                                 @RequestBody AdminTransactionDtos.UpdateTransactionRequest req,
                                                 @AuthenticationPrincipal AuthUser user) {
        ensureAdmin(user);
        if (req.status() == null || req.status().isBlank()) {
            throw ApiException.badRequest("STATUS_REQUIRED", "Status required");
        }

        Enums.TxStatus next;
        try {
            next = Enums.TxStatus.valueOf(req.status().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("STATUS_INVALID", "Invalid status");
        }

        var tx = txRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("TX_NOT_FOUND", "Transaction not found"));
        var current = tx.getStatus();
        if (req.message() == null || req.message().trim().length() < 10) {
            throw ApiException.badRequest("REASON_REQUIRED", "Provide an investigation reason of at least 10 characters");
        }
        if (next != Enums.TxStatus.FAILED || current == Enums.TxStatus.PAYPAL_CAPTURED ||
            current == Enums.TxStatus.PAYNOW_PAID || current == Enums.TxStatus.PROOF_SUBMITTED ||
            current == Enums.TxStatus.NOTIFIED || current == Enums.TxStatus.FAILED) {
            throw ApiException.badRequest("TRANSITION_INVALID", "Only unpaid, nonterminal transactions can be manually marked failed. Provider-paid states cannot be assigned manually.");
        }

        if (current != next) {
            tx.setStatus(next);
            txRepo.save(tx);

            TransactionAudit audit = new TransactionAudit();
            audit.setId(Crypto.uuid());
            audit.setTransactionId(tx.getId());
            audit.setOldStatus(current == null ? null : current.name());
            audit.setNewStatus(next.name());
            audit.setMessage(adminAuditMessage(user, req.message()));
            auditRepo.save(audit);
        }

        return ApiResponse.ok(transactionDetailPayload(tx));
    }

    @GetMapping("/users")
    public ApiResponse<Object> users(@AuthenticationPrincipal AuthUser user) {
        ensureAdmin(user);
        return ApiResponse.ok(userRepo.findAll());
    }

    @PostMapping("/users")
    public ApiResponse<Object> createUser(@RequestBody AdminUserDtos.CreateUserRequest req,
                                          @AuthenticationPrincipal AuthUser user) {
        ensureAdmin(user);
        if (req.password() == null || req.password().length() < 8) {
            throw ApiException.badRequest("PASSWORD_WEAK", "Password must be at least 8 chars");
        }
        var created = userService.register(req.phone(), req.email(), req.name(), req.password());
        if (req.role() != null && req.role().equalsIgnoreCase("admin")) {
            userService.addRole(created, Enums.Role.ADMIN);
        }
        return ApiResponse.ok(created);
    }

    @PatchMapping("/users/{id}")
    public ApiResponse<Object> updateUser(@PathVariable String id,
                                          @RequestBody AdminUserDtos.UpdateUserRequest req,
                                          @AuthenticationPrincipal AuthUser user) {
        ensureAdmin(user);
        var u = userService.getByIdOrThrow(id);
        if ("DELETED".equalsIgnoreCase(u.getStatus())) {
            throw ApiException.badRequest("USER_DELETED", "Deleted users cannot be updated");
        }
        if (req.status() != null && !req.status().isBlank()) {
            if (!java.util.Set.of("ACTIVE", "DISABLED").contains(req.status().toUpperCase())) throw ApiException.badRequest("STATUS_INVALID", "Use ACTIVE or DISABLED");
            if (id.equals(user.getUserId()) && !"ACTIVE".equalsIgnoreCase(req.status())) throw ApiException.badRequest("SELF_DISABLE", "You cannot disable your own account");
            u.setStatus(req.status().toUpperCase());
            userRepo.save(u);
        }
        return ApiResponse.ok(u);
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Object> deleteUser(@PathVariable String id,
                                          @AuthenticationPrincipal AuthUser user) {
        ensureAdmin(user);
        return ApiResponse.ok(userService.safeDelete(id, user.getUserId()));
    }

    private Map<String, Object> transactionDetailPayload(com.moneyguard.api.domain.Tx tx) {
        var proof = proofRepo.findByTransactionId(tx.getId()).orElse(null);
        var audit = auditRepo.findByTransactionIdOrderByCreatedAtDesc(tx.getId());
        Map<String, Object> payload = new HashMap<>();
        payload.put("transaction", tx);
        payload.put("proof", proof);
        payload.put("audit", audit);
        return payload;
    }

    private String adminAuditMessage(AuthUser user, String message) {
        String suffix = message == null || message.isBlank() ? "manual admin update" : message.trim();
        if (suffix.length() > 200) suffix = suffix.substring(0, 200);
        return "admin " + user.getUserId() + ": " + suffix;
    }
}

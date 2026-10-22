package com.moneyguard.api.controller;

import com.moneyguard.api.domain.Enums;
import com.moneyguard.api.dto.TransactionDtos;
import com.moneyguard.api.security.AuthUser;
import com.moneyguard.api.service.TransactionService;
import com.moneyguard.api.web.ApiException;
import com.moneyguard.api.web.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping("/initiate")
    public ApiResponse<TransactionDtos.TransactionResponse> initiate(@RequestBody TransactionDtos.InitiateRequest req,
                                                                     @AuthenticationPrincipal AuthUser user,
                                                                     HttpServletRequest http) {
        if (user == null) throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        String idem = http.getHeader("Idempotency-Key");
        var res = service.initiate(
                user.getUserId(),
                req.amount(),
                req.currency(),
                idem,
                req.returnUrl(),
                req.cancelUrl()
        );
        return ApiResponse.ok(service.mapResponse(res.tx(), res.approveUrl()));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<TransactionDtos.TransactionResponse> confirm(@PathVariable String id,
                                                                    @AuthenticationPrincipal AuthUser user) {
        if (user == null) throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        var tx = service.getOrThrow(id);
        if (!isParty(user, tx) && !user.getRoles().contains(Enums.Role.ADMIN.name())) {
            throw ApiException.forbidden("FORBIDDEN", "Not allowed");
        }
        tx = service.confirm(id);
        return ApiResponse.ok(service.mapResponse(tx, null));
    }

    @GetMapping("/{id}")
    public ApiResponse<TransactionDtos.TransactionResponse> get(@PathVariable String id,
                                                                @AuthenticationPrincipal AuthUser user) {
        if (user == null) throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        var tx = service.getOrThrow(id);
        if (!isParty(user, tx) && !user.getRoles().contains(Enums.Role.ADMIN.name())) {
            throw ApiException.forbidden("FORBIDDEN", "Not allowed");
        }
        return ApiResponse.ok(service.mapResponse(tx, null));
    }

    @GetMapping
    public ApiResponse<Object> list(@AuthenticationPrincipal AuthUser user) {
        if (user == null) throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        return ApiResponse.ok(service.listForUser(user.getUserId()));
    }

    @GetMapping("/{id}/proof")
    public ApiResponse<TransactionDtos.ProofResponse> proof(@PathVariable String id,
                                                            @AuthenticationPrincipal AuthUser user) {
        if (user == null) throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        var tx = service.getOrThrow(id);
        if (!isParty(user, tx) && !user.getRoles().contains(Enums.Role.ADMIN.name())) {
            throw ApiException.forbidden("FORBIDDEN", "Not allowed");
        }
        var record = service.getProofOrThrow(id);
        return ApiResponse.ok(new TransactionDtos.ProofResponse(
                record.getProofHash(),
                record.getStellarTxHash(),
                record.getLedger(),
                record.getCreatedAt().toString()
        ));
    }

    @GetMapping("/{id}/verify-proof")
    public ApiResponse<TransactionDtos.ProofVerifyResponse> verifyProof(@PathVariable String id,
                                                                        @AuthenticationPrincipal AuthUser user) {
        if (user == null) throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        var tx = service.getOrThrow(id);
        if (!isParty(user, tx) && !user.getRoles().contains(Enums.Role.ADMIN.name())) {
            throw ApiException.forbidden("FORBIDDEN", "Not allowed");
        }
        return ApiResponse.ok(service.verifyProof(id));
    }

    private boolean isParty(AuthUser user, com.moneyguard.api.domain.Tx tx) {
        String userId = user.getUserId();
        return userId.equals(tx.getUserId()) || userId.equals(tx.getPaidByUserId()) || userId.equals(tx.getPayeeUserId());
    }

    
}

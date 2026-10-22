package com.moneyguard.api.controller;

import com.moneyguard.api.dto.PaynowDtos;
import com.moneyguard.api.security.AuthUser;
import com.moneyguard.api.service.TransactionService;
import com.moneyguard.api.web.ApiException;
import com.moneyguard.api.web.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/paynow")
public class PaynowController {

    private final TransactionService transactionService;

    public PaynowController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/remote")
    public ApiResponse<Object> remote(@RequestBody PaynowDtos.RemoteRequest req,
                                      @AuthenticationPrincipal AuthUser user) {
        if (user == null) throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        var tx = transactionService.initiatePaynowRemote(
                user.getUserId(),
                req.amount(),
                req.currency(),
                req.phone(),
                req.method(),
                req.payeePhone()
        );
        return ApiResponse.ok(tx);
    }

    @PostMapping("/{id}/poll")
    public ApiResponse<Object> poll(@PathVariable String id,
                                    @AuthenticationPrincipal AuthUser user) {
        if (user == null) throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        requireParty(id, user);
        return ApiResponse.ok(transactionService.pollPaynow(id));
    }

    @PostMapping("/{id}/trace")
    public ApiResponse<Object> trace(@PathVariable String id,
                                     @AuthenticationPrincipal AuthUser user) {
        if (user == null) throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        requireParty(id, user);
        return ApiResponse.ok(transactionService.tracePaynow(id));
    }

    private void requireParty(String id, AuthUser user) {
        var tx = transactionService.getOrThrow(id);
        String userId = user.getUserId();
        if (!userId.equals(tx.getUserId()) && !userId.equals(tx.getPaidByUserId())
                && !userId.equals(tx.getPayeeUserId()) && !user.getRoles().contains("ADMIN")) {
            throw ApiException.forbidden("FORBIDDEN", "Not allowed");
        }
    }
}

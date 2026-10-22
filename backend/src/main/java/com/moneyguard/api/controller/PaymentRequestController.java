package com.moneyguard.api.controller;

import com.moneyguard.api.dto.PaymentRequestDtos;
import com.moneyguard.api.dto.TransactionDtos;
import com.moneyguard.api.security.AuthUser;
import com.moneyguard.api.service.PaymentRequestService;
import com.moneyguard.api.service.TransactionService;
import com.moneyguard.api.web.ApiException;
import com.moneyguard.api.web.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/payment-requests")
public class PaymentRequestController {

    private final PaymentRequestService service;
    private final TransactionService transactionService;

    public PaymentRequestController(PaymentRequestService service, TransactionService transactionService) {
        this.service = service;
        this.transactionService = transactionService;
    }

    @PostMapping
    public ApiResponse<PaymentRequestDtos.PaymentRequestResponse> create(@RequestBody PaymentRequestDtos.CreatePaymentRequest req,
                                                                         @AuthenticationPrincipal AuthUser user) {
        if (user == null) throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        var res = service.create(
                user.getUserId(),
                req.amount(),
                req.currency(),
                req.note(),
                req.ttlMinutes(),
                req.payerPhone(),
                req.provider(),
                req.paynowMethod(),
                req.paynowPhone()
        );
        var pr = res.request();
        return ApiResponse.ok(new PaymentRequestDtos.PaymentRequestResponse(
                pr.getId(),
                pr.getPayeeId(),
                pr.getAmount(),
                pr.getCurrency(),
                pr.getNote(),
                pr.getStatus().name(),
                pr.getExpiresAt() == null ? null : pr.getExpiresAt().toString(),
                pr.getCreatedAt() == null ? null : pr.getCreatedAt().toString(),
                res.qrPayload(),
                res.token(),
                pr.getProvider(),
                pr.getPaynowMethod(),
                pr.getPaynowPhone()
        ));
    }

    @PostMapping("/consume")
    public ApiResponse<TransactionDtos.TransactionResponse> consume(@RequestBody PaymentRequestDtos.ConsumePaymentRequest req,
                                                                    @AuthenticationPrincipal AuthUser user) {
        if (user == null) throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        var res = service.consume(
                user.getUserId(),
                req.qrPayload(),
                req.returnUrl(),
                req.cancelUrl()
        );
        return ApiResponse.ok(transactionService.mapResponse(res.tx(), res.approveUrl()));
    }
}

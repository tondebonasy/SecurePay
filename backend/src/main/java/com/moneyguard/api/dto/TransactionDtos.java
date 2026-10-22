package com.moneyguard.api.dto;

import java.math.BigDecimal;

public class TransactionDtos {
    public record InitiateRequest(BigDecimal amount, String currency, String description, String returnUrl, String cancelUrl) {}
    public record TransactionResponse(
            String id,
            String userId,
            String paidByUserId,
            String payeeUserId,
            String paidByPhone,
            String paidByName,
            String payeePhone,
            String payeeName,
            BigDecimal amount,
            String currency,
            String status,
            String paypalOrderId,
            String paypalCaptureId,
            String paynowReference,
            String paynowMethod,
            String paynowPhone,
            String approveUrl,
            String createdAt,
            String updatedAt
    ) {}
    public record ProofVerifyResponse(boolean verified, String reason) {}
    public record ProofResponse(String proofHash, String stellarTxHash, Long ledger, String createdAt) {}
}

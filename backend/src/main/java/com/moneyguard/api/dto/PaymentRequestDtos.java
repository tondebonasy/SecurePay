package com.moneyguard.api.dto;

import java.math.BigDecimal;

public class PaymentRequestDtos {
    public record CreatePaymentRequest(BigDecimal amount, String currency, String note, Integer ttlMinutes, String payerPhone,
                                       String provider, String paynowMethod, String paynowPhone) {}
    public record ConsumePaymentRequest(String qrPayload, String returnUrl, String cancelUrl) {}
    public record PaymentRequestResponse(
            String id,
            String payeeId,
            BigDecimal amount,
            String currency,
            String note,
            String status,
            String expiresAt,
            String createdAt,
            String qrPayload,
            String token,
            String provider,
            String paynowMethod,
            String paynowPhone
    ) {}
}

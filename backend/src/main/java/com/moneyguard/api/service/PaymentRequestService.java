package com.moneyguard.api.service;

import com.moneyguard.api.config.AppProperties;
import com.moneyguard.api.domain.Enums;
import com.moneyguard.api.domain.PaymentRequest;
import com.moneyguard.api.repo.PaymentRequestRepo;
import com.moneyguard.api.util.Crypto;
import com.moneyguard.api.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

@Service
public class PaymentRequestService {

    private static final int DEFAULT_TTL_MINUTES = 15;
    private static final int MAX_TTL_MINUTES = 1440;
    private static final int NONCE_BYTES = 16;

    private final PaymentRequestRepo repo;
    private final AppProperties props;
    private final TransactionService transactionService;
    private final UserService userService;
    private final DeviceService deviceService;
    private final ExpoPushService expoPushService;
    private final SecureRandom rnd = new SecureRandom();

    public PaymentRequestService(PaymentRequestRepo repo, AppProperties props, TransactionService transactionService,
                                 UserService userService, DeviceService deviceService, ExpoPushService expoPushService) {
        this.repo = repo;
        this.props = props;
        this.transactionService = transactionService;
        this.userService = userService;
        this.deviceService = deviceService;
        this.expoPushService = expoPushService;
    }

    public CreateResult create(String userId, BigDecimal amount, String currency, String note, Integer ttlMinutes,
                               String payerPhone, String provider, String paynowMethod, String paynowPhone) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw ApiException.badRequest("AMOUNT_INVALID", "Amount must be greater than 0");
        }
        if (currency == null || currency.isBlank()) {
            throw ApiException.badRequest("CURRENCY_REQUIRED", "Currency required");
        }
        String normalizedProvider = provider == null || provider.isBlank() ? "PAYPAL" : provider.trim().toUpperCase();
        if (!"PAYPAL".equals(normalizedProvider) && !"PAYNOW".equals(normalizedProvider)) {
            throw ApiException.badRequest("PROVIDER_INVALID", "Provider must be PAYPAL or PAYNOW");
        }
        int ttl = ttlMinutes == null ? DEFAULT_TTL_MINUTES : ttlMinutes;
        if (ttl < 1 || ttl > MAX_TTL_MINUTES) {
            throw ApiException.badRequest("TTL_INVALID", "TTL must be between 1 and 1440 minutes");
        }

        String normalizedPayerPhone = payerPhone == null ? "" : payerPhone.trim();
        String normalizedPaynowPhone = paynowPhone == null ? "" : paynowPhone.trim();
        String normalizedPaynowMethod = paynowMethod == null ? "" : paynowMethod.trim().toLowerCase();
        if ("PAYNOW".equals(normalizedProvider)) {
            if (normalizedPaynowMethod.isBlank()) {
                throw ApiException.badRequest("PAYNOW_METHOD_REQUIRED", "PayNow method required");
            }
            if (!"ecocash".equals(normalizedPaynowMethod) && !"onemoney".equals(normalizedPaynowMethod)) {
                throw ApiException.badRequest("PAYNOW_METHOD_INVALID", "PayNow method must be EcoCash or OneMoney");
            }
            if (normalizedPaynowPhone.isBlank()) {
                throw ApiException.badRequest("PAYNOW_PHONE_REQUIRED", "PayNow phone required");
            }
            if (normalizedPayerPhone.isBlank()) {
                normalizedPayerPhone = normalizedPaynowPhone;
            }
        }

        PaymentRequest request = new PaymentRequest();
        request.setId(Crypto.uuid());
        request.setPayeeId(userId);
        request.setAmount(amount);
        request.setCurrency(currency.trim().toUpperCase());
        request.setNote(note);
        request.setProvider(normalizedProvider);
        request.setPaynowMethod(normalizedPaynowMethod.isBlank() ? null : normalizedPaynowMethod);
        request.setPaynowPhone(normalizedPaynowPhone.isBlank() ? null : normalizedPaynowPhone);
        request.setStatus(Enums.PaymentRequestStatus.ACTIVE);
        request.setExpiresAt(Instant.now().plus(Duration.ofMinutes(ttl)));
        request.setNonce(generateNonce());
        repo.save(request);

        String payload = buildPayload(request);
        String signature = Crypto.hmacSha256Base64Url(props.getQr().getHmacSecret(), payload);
        String token = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(payload.getBytes(StandardCharsets.UTF_8)) + "." + signature;
        String qrPayload = "mg:pr:" + token;

        if (!normalizedPayerPhone.isBlank()) {
            var payer = userService.getByPhoneOrThrow(normalizedPayerPhone);
            sendPaymentRequestNotification(payer.getId(), qrPayload, request.getAmount().stripTrailingZeros().toPlainString(), request.getCurrency());
        }

        return new CreateResult(request, qrPayload, token);
    }

    private String buildPayload(PaymentRequest request) {
        String amount = request.getAmount().stripTrailingZeros().toPlainString();
        long exp = request.getExpiresAt().getEpochSecond();
        return String.join("|",
                "v1",
                request.getId(),
                request.getNonce(),
                String.valueOf(exp),
                amount,
                request.getCurrency()
        );
    }

    private String generateNonce() {
        byte[] bytes = new byte[NONCE_BYTES];
        rnd.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record CreateResult(PaymentRequest request, String qrPayload, String token) {}

    @Transactional
    public ConsumeResult consume(String userId, String qrPayload, String returnUrl, String cancelUrl) {
        if (qrPayload == null || qrPayload.isBlank()) {
            throw ApiException.badRequest("QR_REQUIRED", "QR payload required");
        }
        ParsedToken token = parseAndVerify(qrPayload.trim());
        PaymentRequest request = repo.findByIdAndStatusForUpdate(token.requestId(), Enums.PaymentRequestStatus.ACTIVE)
                .orElseThrow(() -> ApiException.badRequest("REQUEST_INVALID", "Payment request is invalid or already used"));

        if (!Objects.equals(request.getNonce(), token.nonce())) {
            throw ApiException.badRequest("REQUEST_INVALID", "Payment request is invalid");
        }
        if (request.getExpiresAt() != null && request.getExpiresAt().isBefore(Instant.now())) {
            request.setStatus(Enums.PaymentRequestStatus.EXPIRED);
            repo.save(request);
            throw ApiException.badRequest("REQUEST_EXPIRED", "Payment request has expired");
        }
        if (!matchesRequest(request, token)) {
            throw ApiException.badRequest("REQUEST_TAMPERED", "Payment request data mismatch");
        }

        com.moneyguard.api.domain.Tx tx;
        String approveUrl = null;
        if ("PAYNOW".equalsIgnoreCase(request.getProvider())) {
            tx = transactionService.initiatePaynowRemoteForRequest(
                    userId,
                    request.getAmount(),
                    request.getCurrency(),
                    request.getPaynowPhone(),
                    request.getPaynowMethod(),
                    request.getPayeeId()
            );
        } else {
            var res = transactionService.initiate(
                    userId,
                    request.getAmount(),
                    request.getCurrency(),
                    null,
                    returnUrl,
                    cancelUrl,
                    userId,
                    request.getPayeeId()
            );
            tx = res.tx();
            approveUrl = res.approveUrl();
        }

        request.setStatus(Enums.PaymentRequestStatus.CONSUMED);
        request.setConsumedTxId(tx.getId());
        repo.save(request);

        return new ConsumeResult(request, tx, approveUrl);
    }

    private ParsedToken parseAndVerify(String qrPayload) {
        String raw = qrPayload;
        if (raw.startsWith("mg:pr:")) {
            raw = raw.substring("mg:pr:".length());
        }
        int dot = raw.indexOf('.');
        if (dot <= 0 || dot == raw.length() - 1) {
            throw ApiException.badRequest("QR_INVALID", "QR payload invalid");
        }
        String payloadB64 = raw.substring(0, dot);
        String signature = raw.substring(dot + 1);
        String payload;
        try {
            payload = new String(Base64.getUrlDecoder().decode(payloadB64), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest("QR_INVALID", "QR payload invalid");
        }
        String expected = Crypto.hmacSha256Base64Url(props.getQr().getHmacSecret(), payload);
        if (!expected.equals(signature)) {
            throw ApiException.badRequest("QR_INVALID", "QR signature invalid");
        }
        String[] parts = payload.split("\\|");
        if (parts.length != 6 || !"v1".equals(parts[0])) {
            throw ApiException.badRequest("QR_INVALID", "QR payload invalid");
        }
        String requestId = parts[1];
        String nonce = parts[2];
        long expires;
        try {
            expires = Long.parseLong(parts[3]);
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("QR_INVALID", "QR payload invalid");
        }
        String amount = parts[4];
        String currency = parts[5];
        return new ParsedToken(requestId, nonce, expires, amount, currency);
    }

    private boolean matchesRequest(PaymentRequest request, ParsedToken token) {
        String amount = request.getAmount().stripTrailingZeros().toPlainString();
        return amount.equals(token.amount())
                && request.getCurrency().equalsIgnoreCase(token.currency());
    }

    public record ConsumeResult(PaymentRequest request, com.moneyguard.api.domain.Tx tx, String approveUrl) {}
    private record ParsedToken(String requestId, String nonce, long expiresAtEpoch, String amount, String currency) {}

    private void sendPaymentRequestNotification(String payerUserId, String qrPayload, String amount, String currency) {
        var tokens = deviceService.findExpoTokensForUser(payerUserId);
        if (tokens.isEmpty()) return;
        String qrLink = "moneyguard://pay?qr=" + URLEncoder.encode(qrPayload, StandardCharsets.UTF_8);
        var payload = new java.util.HashMap<String, Object>();
        payload.put("qrLink", qrLink);
        payload.put("amount", amount);
        payload.put("currency", currency);
        expoPushService.send(tokens, "Payment request", "Scan the QR or open the request to pay.", payload);
    }
}

package com.moneyguard.api.controller;

import com.moneyguard.api.domain.Enums;
import com.moneyguard.api.domain.WebhookEvent;
import com.moneyguard.api.repo.WebhookEventRepo;
import com.moneyguard.api.service.PayPalService;
import com.moneyguard.api.service.PaynowService;
import com.moneyguard.api.service.TransactionService;
import com.moneyguard.api.util.Crypto;
import com.moneyguard.api.util.Json;
import com.moneyguard.api.web.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/webhooks")
public class WebhookController {

    private final PayPalService payPalService;
    private final PaynowService paynowService;
    private final TransactionService transactionService;
    private final WebhookEventRepo webhookRepo;
    private final Json json;

    public WebhookController(PayPalService payPalService, PaynowService paynowService, TransactionService transactionService,
                             WebhookEventRepo webhookRepo, Json json) {
        this.payPalService = payPalService;
        this.paynowService = paynowService;
        this.transactionService = transactionService;
        this.webhookRepo = webhookRepo;
        this.json = json;
    }

    @PostMapping("/paypal")
    public ApiResponse<Object> paypal(@RequestBody String body, HttpServletRequest req) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> names = req.getHeaderNames();
        while (names.hasMoreElements()) {
            String n = names.nextElement();
            headers.put(n.toUpperCase(), req.getHeader(n));
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> event = json.parse(body, Map.class);
        String eventId = (String) event.get("id");
        String eventType = (String) event.get("event_type");
        if (eventId == null || eventId.isBlank()) {
            eventId = "unknown_" + Crypto.sha256Hex(body).substring(0, 16);
        }

        var existing = webhookRepo.findByProviderAndEventId(Enums.WebhookProvider.PAYPAL, eventId);
        if (existing.isPresent()) {
            return ApiResponse.ok(Map.of("verified", existing.get().isVerified(), "deduped", true));
        }

        boolean verified = payPalService.verifyWebhookSignature(headers, body);
        WebhookEvent record = new WebhookEvent();
        record.setId(Crypto.uuid());
        record.setProvider(Enums.WebhookProvider.PAYPAL);
        record.setEventId(eventId);
        record.setEventType(eventType == null ? "unknown" : eventType);
        record.setVerified(verified);
        record.setRawBody(body);
        record.setHeadersJson(json.stringify(headers));
        record.setStatus(verified ? Enums.WebhookStatus.VERIFIED : Enums.WebhookStatus.RECEIVED);
        webhookRepo.save(record);

        if (!verified) {
            return ApiResponse.ok(Map.of("verified", false));
        }

        Object resource = event.get("resource");
        String orderId = null;
        String status = null;
        if (resource instanceof Map<?,?> res) {
            orderId = (String) res.get("id");
            status = (String) res.get("status");
        }

        if (eventType != null && orderId != null) {
            transactionService.handlePaypalWebhook(orderId, status);
            record.setStatus(Enums.WebhookStatus.PROCESSED);
            record.setProcessedAt(java.time.Instant.now());
            webhookRepo.save(record);
        }
        return ApiResponse.ok(Map.of("verified", true));
    }

    @PostMapping("/paynow")
    public ApiResponse<Object> paynow(@RequestParam Map<String, String> params) {
        boolean verified = paynowService.verifyHash(params);
        String reference = params.get("reference");
        if (reference == null || reference.isBlank()) {
            reference = params.get("paynowreference");
        }
        if (reference == null || reference.isBlank()) {
            reference = params.get("merchantreference");
        }
        String status = params.get("status");

        String eventId = reference == null ? "unknown" : reference + "_" + Crypto.sha256Hex(json.stringify(params)).substring(0, 8);
        WebhookEvent record = new WebhookEvent();
        record.setId(Crypto.uuid());
        record.setProvider(Enums.WebhookProvider.PAYNOW);
        record.setEventId(eventId);
        record.setEventType("paynow_result");
        record.setVerified(verified);
        record.setRawBody(json.stringify(params));
        record.setHeadersJson("{}");
        record.setReceivedAt(java.time.Instant.now());
        record.setStatus(verified ? Enums.WebhookStatus.VERIFIED : Enums.WebhookStatus.RECEIVED);
        webhookRepo.save(record);

        if (verified && reference != null) {
            transactionService.handlePaynowWebhook(reference, status);
            record.setStatus(Enums.WebhookStatus.PROCESSED);
            record.setProcessedAt(java.time.Instant.now());
            webhookRepo.save(record);
        }

        return ApiResponse.ok(Map.of("verified", verified));
    }
}

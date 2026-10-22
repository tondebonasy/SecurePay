package com.moneyguard.api.service;

import com.moneyguard.api.config.AppProperties;
import com.moneyguard.api.util.Json;
import com.moneyguard.api.web.ApiException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class PayPalService {

    private final AppProperties props;
    private final WebClient webClient;
    private final Json json;

    public PayPalService(AppProperties props, WebClient.Builder builder, Json json) {
        this.props = props;
        this.webClient = builder.build();
        this.json = json;
    }

    public record Order(String id, String status, String approveUrl) {}
    public record Capture(String status, String captureId) {}

    private String accessToken() {
        if (props.getPaypal().getClientId() == null || props.getPaypal().getClientSecret() == null
                || props.getPaypal().getClientId().isBlank() || props.getPaypal().getClientSecret().isBlank()) {
            throw ApiException.badRequest("PAYPAL_NOT_CONFIGURED", "PayPal is not configured");
        }
        String auth = props.getPaypal().getClientId() + ":" + props.getPaypal().getClientSecret();
        String basic = "Basic " + Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        var form = new LinkedMultiValueMap<String, String>();
        form.add("grant_type", "client_credentials");

        String resp = webClient.post()
                .uri(props.getPaypal().getBaseUrl() + "/v1/oauth2/token")
                .header("Authorization", basic)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        @SuppressWarnings("unchecked")
        Map<String, Object> map = json.parse(resp, Map.class);
        return (String) map.get("access_token");
    }

    public Order createOrder(String amount, String currency, String returnUrl, String cancelUrl, String customId) {
        String token = accessToken();

        var unit = new java.util.HashMap<String, Object>();
        unit.put("custom_id", customId);
        unit.put("amount", Map.of(
                "currency_code", currency,
                "value", amount
        ));
        if (props.getPaypal().getMerchantEmail() != null && !props.getPaypal().getMerchantEmail().isBlank()) {
            unit.put("payee", Map.of("email_address", props.getPaypal().getMerchantEmail()));
        }

        Map<String, Object> body = Map.of(
                "intent", "CAPTURE",
                "purchase_units", List.of(unit),
                "application_context", Map.of(
                        "return_url", returnUrl,
                        "cancel_url", cancelUrl
                )
        );

        String resp = webClient.post()
                .uri(props.getPaypal().getBaseUrl() + "/v2/checkout/orders")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        @SuppressWarnings("unchecked")
        Map<String, Object> map = json.parse(resp, Map.class);
        String id = (String) map.get("id");
        String status = (String) map.get("status");
        String approve = null;
        Object linksObj = map.get("links");
        if (linksObj instanceof List<?> links) {
            for (Object o : links) {
                if (o instanceof Map<?,?> m) {
                    Object rel = m.get("rel");
                    if ("approve".equals(rel)) approve = (String) m.get("href");
                }
            }
        }
        return new Order(id, status, approve);
    }

    public Capture captureOrder(String orderId) {
        String token = accessToken();

        String resp = webClient.post()
                .uri(props.getPaypal().getBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        @SuppressWarnings("unchecked")
        Map<String, Object> map = json.parse(resp, Map.class);
        String status = (String) map.get("status");

        String captureId = null;
        Object puObj = map.get("purchase_units");
        if (puObj instanceof List<?> pu && !pu.isEmpty()) {
            Object payments = ((Map<?,?>) pu.get(0)).get("payments");
            if (payments instanceof Map<?,?> payMap) {
                Object captures = payMap.get("captures");
                if (captures instanceof List<?> caps && !caps.isEmpty()) {
                    captureId = (String) ((Map<?,?>) caps.get(0)).get("id");
                }
            }
        }
        return new Capture(status, captureId);
    }

    public boolean verifyWebhookSignature(Map<String, String> headers, String body) {
        if (!props.getPaypal().isWebhooksEnabled()) return false;
        if (props.getPaypal().getWebhookId() == null || props.getPaypal().getWebhookId().isBlank()) return false;

        String token = accessToken();
        Map<String, Object> payload = Map.of(
                "auth_algo", headers.getOrDefault("PAYPAL-AUTH-ALGO", ""),
                "cert_url", headers.getOrDefault("PAYPAL-CERT-URL", ""),
                "transmission_id", headers.getOrDefault("PAYPAL-TRANSMISSION-ID", ""),
                "transmission_sig", headers.getOrDefault("PAYPAL-TRANSMISSION-SIG", ""),
                "transmission_time", headers.getOrDefault("PAYPAL-TRANSMISSION-TIME", ""),
                "webhook_id", props.getPaypal().getWebhookId(),
                "webhook_event", json.parse(body, Map.class)
        );

        String resp = webClient.post()
                .uri(props.getPaypal().getBaseUrl() + "/v1/notifications/verify-webhook-signature")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        @SuppressWarnings("unchecked")
        Map<String, Object> map = json.parse(resp, Map.class);
        String status = (String) map.get("verification_status");
        return "SUCCESS".equalsIgnoreCase(status);
    }
}

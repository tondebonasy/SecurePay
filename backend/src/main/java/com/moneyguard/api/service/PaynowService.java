package com.moneyguard.api.service;

import com.moneyguard.api.config.AppProperties;
import com.moneyguard.api.util.Crypto;
import com.moneyguard.api.web.ApiException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class PaynowService {

    private static final Logger log = LoggerFactory.getLogger(PaynowService.class);

    private final AppProperties props;
    private final WebClient webClient;

    public PaynowService(AppProperties props, WebClient.Builder builder) {
        this.props = props;
        this.webClient = builder.build();
    }

    public record RemoteResponse(String status, String pollUrl, String reference, boolean verified, Map<String, String> fields) {}
    public record PollResponse(String status, String reference, boolean verified, Map<String, String> fields) {}
    public record TraceResponse(String status, String reference, boolean verified, Map<String, String> fields) {}

    public RemoteResponse remoteTransaction(String reference, String amount, String phone, String method) {
        ensureConfigured();
        if (reference == null || reference.isBlank()) throw ApiException.badRequest("PAYNOW_REF_REQUIRED", "Reference required");
        if (amount == null || amount.isBlank()) throw ApiException.badRequest("PAYNOW_AMOUNT_REQUIRED", "Amount required");
        if (phone == null || phone.isBlank()) throw ApiException.badRequest("PAYNOW_PHONE_REQUIRED", "Phone required");
        if (method == null || method.isBlank()) throw ApiException.badRequest("PAYNOW_METHOD_REQUIRED", "Method required");
        if (log.isInfoEnabled()) {
            log.info("Paynow URLs returnUrl={} resultUrl={}", props.getPaynow().getReturnUrl(), props.getPaynow().getResultUrl());
        }
        String returnUrl = requireHttpUrl(props.getPaynow().getReturnUrl(), "PAYNOW_RETURN_URL_REQUIRED", "Paynow return URL must start with http:// or https://");
        String resultUrl = requireHttpUrl(props.getPaynow().getResultUrl(), "PAYNOW_RESULT_URL_REQUIRED", "Paynow result URL must start with http:// or https://");

        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("id", props.getPaynow().getIntegrationId().trim());
        fields.put("reference", reference);
        fields.put("amount", amount);
        fields.put("returnurl", returnUrl);
        fields.put("resulturl", resultUrl);
        fields.put("status", "Message");
        if (props.getPaynow().getAuthEmail() != null && !props.getPaynow().getAuthEmail().isBlank()) {
            fields.put("authemail", props.getPaynow().getAuthEmail().trim());
        }
        fields.put("phone", phone);
        fields.put("method", method);
        fields.put("hash", hash(fields));

        var form = new LinkedMultiValueMap<String, String>();
        fields.forEach(form::add);

        String resp = webClient.post()
                .uri(props.getPaynow().getBaseUrl() + "/interface/remotetransaction")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        LinkedHashMap<String, String> parsed = parseResponse(resp);
        boolean verified = verifyHash(parsed);
        if (!verified && log.isWarnEnabled()) {
            log.warn("Paynow hash invalid on remote response. status={} keys={}", parsed.get("status"), parsed.keySet());
        }
        String status = parsed.getOrDefault("status", "Error");
        String pollUrl = parsed.get("pollurl");
        String ref = parsed.getOrDefault("reference", reference);
        return new RemoteResponse(status, pollUrl, ref, verified, parsed);
    }

    public PollResponse pollStatus(String pollUrl) {
        ensureConfigured();
        if (pollUrl == null || pollUrl.isBlank()) throw ApiException.badRequest("PAYNOW_POLLURL_REQUIRED", "Poll URL missing");

        String resp = webClient.post()
                .uri(pollUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(new LinkedMultiValueMap<>()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        LinkedHashMap<String, String> parsed = parseResponse(resp);
        boolean verified = verifyHash(parsed);
        if (!verified && log.isWarnEnabled()) {
            log.warn("Paynow hash invalid on poll response. status={} keys={}", parsed.get("status"), parsed.keySet());
        }
        String status = parsed.getOrDefault("status", "Unknown");
        String reference = parsed.get("reference");
        return new PollResponse(status, reference, verified, parsed);
    }

    public TraceResponse trace(String reference) {
        ensureConfigured();
        if (reference == null || reference.isBlank()) throw ApiException.badRequest("PAYNOW_REF_REQUIRED", "Reference required");

        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("id", props.getPaynow().getIntegrationId().trim());
        fields.put("merchanttrace", reference);
        fields.put("status", "Message");
        fields.put("hash", hash(fields));

        var form = new LinkedMultiValueMap<String, String>();
        fields.forEach(form::add);

        String resp = webClient.post()
                .uri(props.getPaynow().getBaseUrl() + "/interface/trace")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(form))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        LinkedHashMap<String, String> parsed = parseResponse(resp);
        boolean verified = verifyHash(parsed);
        if (!verified && log.isWarnEnabled()) {
            log.warn("Paynow hash invalid on trace response. status={} keys={}", parsed.get("status"), parsed.keySet());
        }
        String status = parsed.getOrDefault("status", "Unknown");
        String ref = parsed.getOrDefault("reference", reference);
        return new TraceResponse(status, ref, verified, parsed);
    }

    public boolean verifyHash(Map<String, String> fields) {
        if (fields == null || !fields.containsKey("hash")) return false;
        LinkedHashMap<String, String> normalized = new LinkedHashMap<>();
        fields.forEach((k, v) -> normalized.put(k.toLowerCase(), v));
        LinkedHashMap<String, String> ordered = (fields instanceof LinkedHashMap<?, ?>)
                ? new LinkedHashMap<>(normalized)
                : orderFields(normalized);
        String expected = ordered.remove("hash");
        String actual = hash(ordered);
        return expected != null && expected.equalsIgnoreCase(actual);
    }

    private String hash(LinkedHashMap<String, String> fields) {
        StringBuilder sb = new StringBuilder();
        for (String value : fields.values()) {
            if (value != null) sb.append(value);
        }
        sb.append(props.getPaynow().getIntegrationKey().trim());
        return Crypto.sha512HexUpper(sb.toString());
    }

    private LinkedHashMap<String, String> parseResponse(String body) {
        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        if (body == null) return map;
        String[] pairs = body.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx <= 0) continue;
            String key = decode(pair.substring(0, idx));
            String value = decode(pair.substring(idx + 1));
            map.put(key.toLowerCase(), value);
        }
        return map;
    }

    private LinkedHashMap<String, String> orderFields(Map<String, String> fields) {
        if (fields instanceof LinkedHashMap<?, ?> linked) {
            @SuppressWarnings("unchecked")
            LinkedHashMap<String, String> cast = (LinkedHashMap<String, String>) linked;
            return new LinkedHashMap<>(cast);
        }
        String[] preferred = new String[] {
                "id",
                "reference",
                "paynowreference",
                "amount",
                "merchanttrace",
                "pollurl",
                "status",
                "error",
                "hash"
        };
        LinkedHashMap<String, String> ordered = new LinkedHashMap<>();
        for (String key : preferred) {
            if (fields.containsKey(key)) {
                ordered.put(key, fields.get(key));
            }
        }
        fields.forEach((k, v) -> {
            if (!ordered.containsKey(k) && !"hash".equals(k)) ordered.put(k, v);
        });
        if (fields.containsKey("hash")) ordered.put("hash", fields.get("hash"));
        return ordered;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private void ensureConfigured() {
        if (props.getPaynow().getIntegrationId() == null || props.getPaynow().getIntegrationId().isBlank()
                || props.getPaynow().getIntegrationKey() == null || props.getPaynow().getIntegrationKey().isBlank()) {
            throw ApiException.badRequest("PAYNOW_NOT_CONFIGURED", "Paynow is not configured");
        }
    }

    private String requireHttpUrl(String value, String code, String message) {
        if (value == null) throw ApiException.badRequest(code, message);
        String trimmed = value.trim();
        if (trimmed.startsWith("//")) {
            trimmed = "https:" + trimmed;
        }
        if (!(trimmed.startsWith("http://") || trimmed.startsWith("https://"))) {
            throw ApiException.badRequest(code, message);
        }
        return trimmed;
    }
}

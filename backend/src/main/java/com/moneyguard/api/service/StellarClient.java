package com.moneyguard.api.service;

import com.moneyguard.api.config.AppProperties;
import com.moneyguard.api.util.Crypto;
import com.moneyguard.api.util.Json;
import com.moneyguard.api.web.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.Map;

@Service
public class StellarClient {

    private static final Logger log = LoggerFactory.getLogger(StellarClient.class);

    private final AppProperties props;
    private final WebClient webClient;
    private final Json json;

    public StellarClient(AppProperties props, WebClient.Builder builder, Json json) {
        this.props = props;
        this.webClient = builder.build();
        this.json = json;
    }

    public record AnchorResult(boolean anchored, String stellarTxHash, Long ledger, Instant anchoredAt) {}
    public record VerifyResult(boolean verified, String reason) {}

    public AnchorResult anchor(String txId, String receiptHash) {
        if (!props.getStellar().isEnabled()) {
            return new AnchorResult(false, null, null, null);
        }
        try {
            String resp = webClient.post()
                    .uri(props.getStellar().getWorkerUrl() + "/anchor")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("txId", txId, "receiptHash", receiptHash))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            @SuppressWarnings("unchecked")
            Map<String, Object> map = json.parse(resp, Map.class);
            String hash = (String) map.get("stellarTxHash");
            Long ledger = null;
            Object ledgerObj = map.get("ledger");
            if (ledgerObj instanceof Number n) ledger = n.longValue();
            return new AnchorResult(true, hash, ledger, Instant.now());
        } catch (Exception e) {
            log.error("Stellar worker anchor failed", e);
            throw ApiException.badRequest("STELLAR_ANCHOR_FAILED", "Anchoring failed");
        }
    }

    public VerifyResult verify(String receiptHash, String stellarTxHash) {
        if (stellarTxHash == null || stellarTxHash.isBlank()) return new VerifyResult(false, "Missing hash");
        if (!props.getStellar().isEnabled()) {
            // mock mode: treat as not verifiable on chain
            return new VerifyResult(false, "On-chain verification disabled; no evidence checked");
        }
        try {
            String resp = webClient.get()
                    .uri(props.getStellar().getWorkerUrl() + "/verify/" + stellarTxHash + "?receiptHash=" + receiptHash)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            @SuppressWarnings("unchecked")
            Map<String, Object> map = json.parse(resp, Map.class);
            boolean verified = Boolean.TRUE.equals(map.get("verified"));
            String reason = (String) map.getOrDefault("reason", verified ? "OK" : "Not verified");
            return new VerifyResult(verified, reason);
        } catch (Exception e) {
            return new VerifyResult(false, "Not found");
        }
    }
}

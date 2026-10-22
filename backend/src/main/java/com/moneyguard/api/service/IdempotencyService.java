package com.moneyguard.api.service;

import com.moneyguard.api.config.AppProperties;
import com.moneyguard.api.domain.IdempotencyRecord;
import com.moneyguard.api.repo.IdempotencyRepo;
import com.moneyguard.api.util.Crypto;
import com.moneyguard.api.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class IdempotencyService {

    private final IdempotencyRepo repo;
    private final long ttlMinutes;

    public IdempotencyService(IdempotencyRepo repo, AppProperties props) {
        this.repo = repo;
        this.ttlMinutes = props.getIdempotency().getTtlMinutes();
    }

    public record StoredResponse(int status, String body) {}

    public StoredResponse find(String userId, String key, String requestHash) {
        var recOpt = repo.findByUserIdAndIdemKey(userId, key);
        if (recOpt.isEmpty()) return null;
        IdempotencyRecord rec = recOpt.get();
        if (rec.getExpiresAt().isBefore(Instant.now())) return null;
        if (!rec.getRequestHash().equals(requestHash)) {
            throw new ApiException(HttpStatus.CONFLICT, "IDEMPOTENCY_KEY_REUSE", "Idempotency-Key already used with a different request body");
        }
        return new StoredResponse(rec.getResponseStatus(), rec.getResponseBody());
    }

    public void store(String userId, String key, String requestHash, int status, String responseBody) {
        IdempotencyRecord rec = new IdempotencyRecord();
        rec.setId(Crypto.uuid());
        rec.setUserId(userId);
        rec.setIdemKey(key);
        rec.setRequestHash(requestHash);
        rec.setResponseStatus(status);
        rec.setResponseBody(responseBody);
        rec.setExpiresAt(Instant.now().plus(Duration.ofMinutes(ttlMinutes)));
        repo.save(rec);
    }
}

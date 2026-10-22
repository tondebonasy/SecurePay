package com.moneyguard.api.service;

import com.moneyguard.api.config.AppProperties;
import com.moneyguard.api.domain.DeviceSession;
import com.moneyguard.api.repo.DeviceSessionRepo;
import com.moneyguard.api.util.Crypto;
import com.moneyguard.api.web.ApiException;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class SessionService {

    private final DeviceSessionRepo repo;
    private final long refreshTtlDays;

    public SessionService(DeviceSessionRepo repo, AppProperties props) {
        this.repo = repo;
        this.refreshTtlDays = props.getJwt().getRefreshTtlDays();
    }

    public record SessionTokens(String accessToken, String refreshToken, String sessionId) {}

    public SessionTokens createSession(String userId, String deviceId, String accessToken) {
        String refresh = randomToken();
        DeviceSession s = repo.findByUserIdAndDeviceIdAndRevokedAtIsNull(userId, deviceId)
                .orElseGet(DeviceSession::new);

        if (s.getId() == null) s.setId(Crypto.uuid());
        s.setUserId(userId);
        s.setDeviceId(deviceId);
        s.setRefreshTokenHash(Crypto.sha256Hex(refresh));
        s.setRefreshExpiresAt(Instant.now().plus(Duration.ofDays(refreshTtlDays)));
        s.setRevokedAt(null);
        s.setLastUsedAt(Instant.now());
        repo.save(s);

        return new SessionTokens(accessToken, refresh, s.getId());
    }

    public SessionTokens refresh(String sessionId, String deviceId, String refreshToken, String newAccessToken) {
        DeviceSession s = repo.findByIdAndRevokedAtIsNull(sessionId)
                .orElseThrow(() -> ApiException.unauthorized("SESSION_INVALID", "Session invalid"));
        if (!s.getDeviceId().equals(deviceId)) throw ApiException.unauthorized("DEVICE_MISMATCH", "Device mismatch");
        if (s.getRefreshExpiresAt().isBefore(Instant.now())) throw ApiException.unauthorized("SESSION_EXPIRED", "Session expired");
        if (!s.getRefreshTokenHash().equals(Crypto.sha256Hex(refreshToken))) throw ApiException.unauthorized("SESSION_INVALID", "Session invalid");

        // rotate
        String newRefresh = randomToken();
        s.setRefreshTokenHash(Crypto.sha256Hex(newRefresh));
        s.setLastUsedAt(Instant.now());
        repo.save(s);

        return new SessionTokens(newAccessToken, newRefresh, s.getId());
    }

    public void revoke(String sessionId, String deviceId) {
        DeviceSession s = repo.findByIdAndRevokedAtIsNull(sessionId)
                .orElseThrow(() -> ApiException.notFound("SESSION_NOT_FOUND", "Session not found"));
        if (!s.getDeviceId().equals(deviceId)) throw ApiException.unauthorized("DEVICE_MISMATCH", "Device mismatch");
        s.setRevokedAt(Instant.now());
        repo.save(s);
    }

    private String randomToken() {
        byte[] b = new byte[32];
        new SecureRandom().nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}

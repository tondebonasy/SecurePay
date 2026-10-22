package com.moneyguard.api.security;

import com.moneyguard.api.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final Duration accessTtl;

    public JwtService(AppProperties props) {
        String configured = props.getJwt().getSecret();
        if (configured == null || configured.isBlank()) throw new IllegalStateException("JWT_SECRET is required");
        byte[] secret = configured.getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) throw new IllegalStateException("JWT_SECRET must contain at least 32 UTF-8 bytes");
        this.key = Keys.hmacShaKeyFor(secret);
        this.accessTtl = Duration.ofMinutes(props.getJwt().getAccessTtlMinutes());
    }

    public String issueAccessToken(String userId, List<String> roles) {
        return issueAccessToken(userId, roles, null);
    }

    public String issueAccessToken(String userId, List<String> roles, String deviceId) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessTtl);

        var builder = Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .addClaims(Map.of("roles", roles));

        if (deviceId != null && !deviceId.isBlank()) builder.claim("did", deviceId);

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private byte[] pad(byte[] secret) {
        // JJWT requires >=256-bit key for HS256
        if (secret.length >= 32) return secret;
        byte[] out = new byte[32];
        System.arraycopy(secret, 0, out, 0, secret.length);
        for (int i = secret.length; i < 32; i++) out[i] = (byte) (i * 31);
        return out;
    }
}

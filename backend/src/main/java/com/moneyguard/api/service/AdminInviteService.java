package com.moneyguard.api.service;

import com.moneyguard.api.domain.AdminInvite;
import com.moneyguard.api.domain.Enums;
import com.moneyguard.api.repo.AdminInviteRepo;
import com.moneyguard.api.util.Crypto;
import com.moneyguard.api.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class AdminInviteService {

    private final AdminInviteRepo repo;
    private final UserService userService;

    public AdminInviteService(AdminInviteRepo repo, UserService userService) {
        this.repo = repo;
        this.userService = userService;
    }

    public record Invite(String code, Instant expiresAt) {}

    public Invite createInvite(String adminUserId, int ttlMinutes) {
        if (ttlMinutes <= 0 || ttlMinutes > 1440) {
            throw ApiException.badRequest("TTL_INVALID", "TTL must be between 1 and 1440 minutes");
        }
        String code = randomCode();
        AdminInvite invite = new AdminInvite();
        invite.setId(Crypto.uuid());
        invite.setCodeHash(Crypto.sha256Hex(code));
        invite.setCreatedByUserId(adminUserId);
        invite.setExpiresAt(Instant.now().plus(Duration.ofMinutes(ttlMinutes)));
        repo.save(invite);
        return new Invite(code, invite.getExpiresAt());
    }

    @Transactional
    public void redeem(String userId, String code) {
        if (code == null || code.isBlank()) {
            throw ApiException.badRequest("INVITE_REQUIRED", "Invite code required");
        }
        String hash = Crypto.sha256Hex(code.trim());
        var invite = repo.findTopByCodeHashAndConsumedAtIsNullAndExpiresAtAfter(hash, Instant.now())
                .orElseThrow(() -> ApiException.badRequest("INVITE_INVALID", "Invite code invalid or expired"));

        invite.setConsumedAt(Instant.now());
        invite.setConsumedByUserId(userId);
        repo.save(invite);

        var user = userService.getByIdOrThrow(userId);
        userService.addRole(user, Enums.Role.ADMIN);
    }

    private String randomCode() {
        byte[] b = new byte[24];
        new SecureRandom().nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}

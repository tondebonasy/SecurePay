package com.moneyguard.api.service;

import com.moneyguard.api.domain.User;
import com.moneyguard.api.domain.Enums;
import com.moneyguard.api.repo.DeviceSessionRepo;
import com.moneyguard.api.repo.PushTokenRepo;
import com.moneyguard.api.repo.UserRepo;
import com.moneyguard.api.util.Crypto;
import com.moneyguard.api.web.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.time.Instant;
import java.util.List;

@Service
public class UserService {
    private final UserRepo repo;
    private final PasswordEncoder passwordEncoder;
    private final DeviceSessionRepo sessionRepo;
    private final PushTokenRepo pushTokenRepo;

    public UserService(UserRepo repo, PasswordEncoder passwordEncoder,
                       DeviceSessionRepo sessionRepo, PushTokenRepo pushTokenRepo) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
        this.sessionRepo = sessionRepo;
        this.pushTokenRepo = pushTokenRepo;
    }

    @Transactional
    public synchronized User register(String phone, String email, String displayName, String rawPassword) {
        String normalized = normalizePhone(phone);
        if (normalized.isBlank()) throw ApiException.badRequest("PHONE_REQUIRED", "Phone required");
        if (repo.findByPhoneNormalized(normalized).isPresent() || repo.findByPhone(phone).isPresent()) {
            throw ApiException.conflict("PHONE_EXISTS", "Phone already registered");
        }
        if (email != null && !email.isBlank() && repo.findByEmail(email).isPresent()) throw ApiException.conflict("EMAIL_EXISTS", "Email already registered");

        User u = new User();
        u.setId(Crypto.uuid());
        u.setPhone(phone);
        u.setPhoneNormalized(normalized);
        u.setEmail((email == null || email.isBlank()) ? null : email);
        u.setDisplayName(displayName);
        u.setPasswordHash(passwordEncoder.encode(rawPassword));
        u.setRoles(repo.countByRolesContaining(Enums.Role.ADMIN.name()) == 0
                ? Enums.Role.ADMIN.name()
                : Enums.Role.USER.name());
        u.setStatus("ACTIVE");
        return repo.save(u);
    }

    public User getByPhoneOrThrow(String phone) {
        String normalized = normalizePhone(phone);
        var byNormalized = repo.findByPhoneNormalized(normalized);
        if (byNormalized.isPresent()) return byNormalized.get();
        return repo.findByPhone(phone).orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));
    }

    public User getByIdOrThrow(String id) {
        return repo.findById(id).orElseThrow(() -> ApiException.notFound("USER_NOT_FOUND", "User not found"));
    }

    public User getByIdOrNull(String id) {
        if (id == null || id.isBlank()) return null;
        return repo.findById(id).orElse(null);
    }

    public boolean passwordMatches(User u, String rawPassword) {
        return passwordEncoder.matches(rawPassword, u.getPasswordHash());
    }

    public List<String> roles(User u) {
        return Arrays.stream(u.getRoles().split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    public void ensureAdmin(User u) {
        if (!roles(u).contains(Enums.Role.ADMIN.name())) throw new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", "Admin only");
    }


    public User addRole(User user, Enums.Role role) {
        String roleName = role.name();
        var roles = roles(user);
        if (roles.contains(roleName)) return user;
        String updated = String.join(",", roles) + (roles.isEmpty() ? "" : ",") + roleName;
        user.setRoles(updated);
        return repo.save(user);
    }

    public User grantFirstAdmin(String userId) {
        if (repo.countByRolesContaining(Enums.Role.ADMIN.name()) > 0) {
            throw ApiException.conflict("ADMIN_EXISTS", "An admin already exists");
        }
        User user = getByIdOrThrow(userId);
        return addRole(user, Enums.Role.ADMIN);
    }

    public void ensureActive(User user) {
        if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw ApiException.unauthorized("USER_INACTIVE", "User account is not active");
        }
    }

    @Transactional
    public User safeDelete(String userId, String actorUserId) {
        if (userId == null || userId.isBlank()) {
            throw ApiException.badRequest("USER_ID_REQUIRED", "User ID required");
        }
        if (userId.equals(actorUserId)) {
            throw ApiException.badRequest("SELF_DELETE_BLOCKED", "You cannot delete your own admin account");
        }

        User user = getByIdOrThrow(userId);
        if ("DELETED".equalsIgnoreCase(user.getStatus())) return user;

        if (roles(user).contains(Enums.Role.ADMIN.name())
                && repo.countByRolesContainingAndStatusNot(Enums.Role.ADMIN.name(), "DELETED") <= 1) {
            throw ApiException.badRequest("LAST_ADMIN_BLOCKED", "Cannot delete the last admin account");
        }

        Instant now = Instant.now();
        var sessions = sessionRepo.findAllByUserIdAndRevokedAtIsNull(userId);
        sessions.forEach(s -> s.setRevokedAt(now));
        sessionRepo.saveAll(sessions);
        pushTokenRepo.deleteAllByUserId(userId);

        String suffix = userId.replace("-", "");
        if (suffix.length() > 24) suffix = suffix.substring(0, 24);
        String tombstone = "deleted-" + suffix;
        user.setPhone(tombstone);
        user.setPhoneNormalized(tombstone);
        user.setEmail("deleted+" + userId + "@deleted.local");
        user.setDisplayName("Deleted user");
        user.setPasswordHash(passwordEncoder.encode(Crypto.uuid()));
        user.setRoles(Enums.Role.USER.name());
        user.setStatus("DELETED");
        return repo.save(user);
    }

    private String normalizePhone(String input) {
        if (input == null) return "";
        String digits = input.replaceAll("[^0-9]", "");
        if (digits.startsWith("263") && digits.length() >= 12) {
            return "0" + digits.substring(digits.length() - 9);
        }
        return digits;
    }

    @Transactional
    public void updateProfile(String id, String name, String email) {
        if (name == null || name.isBlank() || name.length()>120) throw ApiException.badRequest("NAME_INVALID", "Display name must be 1–120 characters");
        if (email != null && !email.isBlank() && (email.length()>254 || !email.matches("[^@ ]+@[^@ ]+\\.[^@ ]+"))) throw ApiException.badRequest("EMAIL_INVALID", "Enter a valid email address");
        var user=getByIdOrThrow(id);user.setDisplayName(name.trim());user.setEmail(email==null||email.isBlank()?null:email.trim().toLowerCase());repo.save(user);
    }
}

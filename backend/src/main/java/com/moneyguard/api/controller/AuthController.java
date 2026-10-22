package com.moneyguard.api.controller;

import com.moneyguard.api.domain.Enums;
import com.moneyguard.api.dto.AuthDtos;
import com.moneyguard.api.security.AuthUser;
import com.moneyguard.api.security.JwtService;
import com.moneyguard.api.service.*;
import com.moneyguard.api.repo.DeviceSessionRepo;
import com.moneyguard.api.util.Json;
import com.moneyguard.api.web.ApiException;
import com.moneyguard.api.web.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    private final UserService userService;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final SessionService sessionService;
    private final DeviceSessionRepo sessionRepo;
    private final AuditService audit;
    private final OutboxService outbox;
    private final Json json;

    public AuthController(UserService userService, OtpService otpService,
                          JwtService jwtService, SessionService sessionService, DeviceSessionRepo sessionRepo,
                          AuditService audit, OutboxService outbox, Json json) {
        this.userService = userService;
        this.otpService = otpService;
        this.jwtService = jwtService;
        this.sessionService = sessionService;
        this.sessionRepo = sessionRepo;
        this.audit = audit;
        this.outbox = outbox;
        this.json = json;
    }

    @PostMapping("/otp")
    public ApiResponse<Object> requestOtp(@RequestBody AuthDtos.OtpRequest req) {
        String phone = req.phone();
        if (phone == null || phone.isBlank()) throw ApiException.badRequest("PHONE_REQUIRED", "Phone required");

        Enums.OtpPurpose purpose = parseEnum(req.purpose(), Enums.OtpPurpose.class, "OTP_PURPOSE_INVALID", "Invalid OTP purpose");
        Enums.OtpChannel channel = parseEnum(req.channel(), Enums.OtpChannel.class, "OTP_CHANNEL_INVALID", "Invalid OTP channel");

        String subject = purpose == Enums.OtpPurpose.REGISTER ? ("phone:" + phone) : userService.getByPhoneOrThrow(phone).getId();
        String code = otpService.issueOtp(subject, purpose, channel);

        // For now: SMS is the default delivery mechanism. Email can be added easily using SEND_EMAIL outbox jobs.
        outbox.enqueue(Enums.OutboxType.SEND_SMS, json.stringify(Map.of("to", phone, "text", "Your SecurePay code is " + code)));

        return ApiResponse.ok(Map.of("sent", true));
    }

    @PostMapping("/register")
    public ApiResponse<AuthDtos.AuthResponse> register(@RequestBody AuthDtos.RegisterRequest req, HttpServletRequest http) {
        if (req.phone() == null || req.phone().isBlank()) throw ApiException.badRequest("PHONE_REQUIRED", "Phone required");
        if (req.password() == null || req.password().length() < 8) throw ApiException.badRequest("PASSWORD_WEAK", "Password must be at least 8 chars");

        var user = userService.register(req.phone(), req.email(), req.displayName(), req.password());
        String deviceId = deviceIdOrThrow(http);
        String access = jwtService.issueAccessToken(user.getId(), userService.roles(user), deviceId);

        var tokens = sessionService.createSession(user.getId(), deviceId, access);

        audit.log(http, user.getId(), "REGISTER", "User", user.getId(), Map.of("phone", user.getPhone()));
        return ApiResponse.ok(new AuthDtos.AuthResponse(tokens.accessToken(), tokens.refreshToken(), tokens.sessionId()));
    }

    @PostMapping("/login")
    public ApiResponse<AuthDtos.AuthResponse> login(@RequestBody AuthDtos.LoginRequest req, HttpServletRequest http) {
        if (req.phone() == null || req.phone().isBlank()) throw ApiException.badRequest("PHONE_REQUIRED", "Phone required");
        if (req.password() == null || req.password().isBlank()) throw ApiException.badRequest("PASSWORD_REQUIRED", "Password required");

        com.moneyguard.api.domain.User user;
        try { user = userService.getByPhoneOrThrow(req.phone()); }
        catch (ApiException e) { throw ApiException.unauthorized("INVALID_CREDENTIALS", "Invalid credentials"); }
        userService.ensureActive(user);
        if (!userService.passwordMatches(user, req.password())) throw ApiException.unauthorized("INVALID_CREDENTIALS", "Invalid credentials");

        String deviceId = deviceIdOrThrow(http);
        String access = jwtService.issueAccessToken(user.getId(), userService.roles(user), deviceId);
        var tokens = sessionService.createSession(user.getId(), deviceId, access);

        audit.log(http, user.getId(), "LOGIN", "User", user.getId(), null);
        return ApiResponse.ok(new AuthDtos.AuthResponse(tokens.accessToken(), tokens.refreshToken(), tokens.sessionId()));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthDtos.AuthResponse> refresh(@RequestBody AuthDtos.RefreshRequest req, HttpServletRequest http) {
        if (req.sessionId() == null || req.sessionId().isBlank()) throw ApiException.badRequest("SESSION_REQUIRED", "Session required");
        if (req.refreshToken() == null || req.refreshToken().isBlank()) throw ApiException.badRequest("REFRESH_REQUIRED", "Refresh token required");

        var ds = sessionRepo.findByIdAndRevokedAtIsNull(req.sessionId())
                .orElseThrow(() -> ApiException.unauthorized("SESSION_INVALID", "Session invalid"));
        var user = userService.getByIdOrThrow(ds.getUserId());
        userService.ensureActive(user);

        String deviceId = deviceIdOrThrow(http);
        String access = jwtService.issueAccessToken(user.getId(), userService.roles(user), deviceId);
        var tokens = sessionService.refresh(req.sessionId(), deviceId, req.refreshToken(), access);

        audit.log(http, user.getId(), "REFRESH", "DeviceSession", req.sessionId(), null);
        return ApiResponse.ok(new AuthDtos.AuthResponse(tokens.accessToken(), tokens.refreshToken(), tokens.sessionId()));
    }

    @PostMapping("/logout")
    public ApiResponse<Object> logout(@RequestBody AuthDtos.RefreshRequest req, HttpServletRequest http, @AuthenticationPrincipal AuthUser user) {
        String deviceId = deviceIdOrThrow(http);
        sessionService.revoke(req.sessionId(), deviceId);
        audit.log(http, user == null ? null : user.getUserId(), "LOGOUT", "DeviceSession", req.sessionId(), null);
        return ApiResponse.ok(Map.of("loggedOut", true));
    }


    private String deviceIdOrThrow(HttpServletRequest http) {
        String deviceId = http.getHeader("X-Device-Id");
        if (deviceId == null || deviceId.isBlank()) throw ApiException.badRequest("DEVICE_ID_REQUIRED", "X-Device-Id header required");
        return deviceId;
    }

    private static <T extends Enum<T>> T parseEnum(String value, Class<T> enumType, String code, String message) {
        if (value == null || value.isBlank()) throw ApiException.badRequest(code, message);
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest(code, message);
        }
    }
}

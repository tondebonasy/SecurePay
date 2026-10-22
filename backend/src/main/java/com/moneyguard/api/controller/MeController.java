package com.moneyguard.api.controller;

import com.moneyguard.api.security.AuthUser;
import com.moneyguard.api.service.UserService;
import com.moneyguard.api.web.ApiException;
import com.moneyguard.api.web.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MeController {
    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/v1/auth/me")
    public ApiResponse<Object> me(@AuthenticationPrincipal AuthUser user) {
        if (user == null) throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        var u = userService.getByIdOrThrow(user.getUserId());
        return ApiResponse.ok(Map.of(
                "id", u.getId(),
                "phone", u.getPhone(),
                "email", java.util.Objects.toString(u.getEmail(), ""),
                "displayName", java.util.Objects.toString(u.getDisplayName(), ""),
                "roles", user.getRoles()
        ));
    }

    public record Profile(String displayName, String email) {}
    @org.springframework.web.bind.annotation.PatchMapping("/v1/auth/me")
    public ApiResponse<Object> update(@AuthenticationPrincipal AuthUser user, @org.springframework.web.bind.annotation.RequestBody Profile profile) {
        if (user == null) throw ApiException.unauthorized("UNAUTHORIZED", "Authentication required");
        userService.updateProfile(user.getUserId(), profile.displayName(), profile.email());
        return me(user);
    }
}

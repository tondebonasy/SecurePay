package com.moneyguard.api.controller;

import com.moneyguard.api.dto.InviteDtos;
import com.moneyguard.api.security.AuthUser;
import com.moneyguard.api.service.AdminInviteService;
import com.moneyguard.api.service.AuditService;
import com.moneyguard.api.service.UserService;
import com.moneyguard.api.web.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminInviteController {

    private final AdminInviteService invites;
    private final AuditService audit;
    private final UserService userService;

    public AdminInviteController(AdminInviteService invites, AuditService audit, UserService userService) {
        this.invites = invites;
        this.audit = audit;
        this.userService = userService;
    }

    @PostMapping("/v1/admin/invites")
    public ApiResponse<InviteDtos.CreateInviteResponse> create(@RequestBody InviteDtos.CreateInviteRequest req,
                                                               @AuthenticationPrincipal AuthUser user,
                                                               HttpServletRequest http) {
        int ttl = req.ttlMinutes() <= 0 ? 60 : req.ttlMinutes();
        var invite = invites.createInvite(user.getUserId(), ttl);
        audit.log(http, user.getUserId(), "ADMIN_INVITE_CREATE", "AdminInvite", null, null);
        return ApiResponse.ok(new InviteDtos.CreateInviteResponse(invite.code(), invite.expiresAt().toString()));
    }

    @PostMapping("/v1/invites/redeem")
    public ApiResponse<InviteDtos.RedeemInviteResponse> redeem(@RequestBody InviteDtos.RedeemInviteRequest req,
                                                               @AuthenticationPrincipal AuthUser user,
                                                               HttpServletRequest http) {
        invites.redeem(user.getUserId(), req.code());
        audit.log(http, user.getUserId(), "ADMIN_INVITE_REDEEM", "User", user.getUserId(), null);
        return ApiResponse.ok(new InviteDtos.RedeemInviteResponse(true));
    }

    @PostMapping("/v1/invites/first-admin")
    public ApiResponse<InviteDtos.FirstAdminResponse> firstAdmin(@AuthenticationPrincipal AuthUser user,
                                                                  HttpServletRequest http) {
        throw com.moneyguard.api.web.ApiException.forbidden("BOOTSTRAP_DISABLED", "Public administrator bootstrap is disabled. Provision an administrator through a trusted database operation.");

    }
}

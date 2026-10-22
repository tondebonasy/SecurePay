package com.moneyguard.api.dto;

public class InviteDtos {
    public record CreateInviteRequest(int ttlMinutes) {}
    public record CreateInviteResponse(String code, String expiresAt) {}
    public record RedeemInviteRequest(String code) {}
    public record RedeemInviteResponse(boolean adminGranted) {}
    public record FirstAdminResponse(boolean adminGranted) {}
}

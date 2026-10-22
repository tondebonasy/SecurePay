package com.moneyguard.api.dto;

public class AuthDtos {
    public record RegisterRequest(String phone, String email, String displayName, String password, String otp) {}
    public record LoginRequest(String phone, String password) {}
    public record OtpRequest(String phone, String purpose, String channel) {}
    public record RefreshRequest(String sessionId, String refreshToken) {}
    public record AuthResponse(String accessToken, String refreshToken, String sessionId) {}
}

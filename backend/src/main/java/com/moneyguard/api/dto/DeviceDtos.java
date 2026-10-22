package com.moneyguard.api.dto;

public class DeviceDtos {
    public record RegisterPushTokenRequest(String expoPushToken, String platform) {}
}

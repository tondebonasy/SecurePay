package com.moneyguard.api.service;

public interface SmsService {
    void send(String phone, String message);
}

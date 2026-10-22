package com.moneyguard.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MockSmsService implements SmsService {
    private static final Logger log = LoggerFactory.getLogger(MockSmsService.class);

    @Override
    public void send(String phone, String message) {
        log.info("[MOCK_SMS] to={} msg={}", phone, message);
    }
}

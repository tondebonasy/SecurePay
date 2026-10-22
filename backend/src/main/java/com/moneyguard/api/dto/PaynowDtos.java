package com.moneyguard.api.dto;

import java.math.BigDecimal;

public class PaynowDtos {
    public record RemoteRequest(BigDecimal amount, String currency, String description, String phone, String method, String payeePhone) {}
}

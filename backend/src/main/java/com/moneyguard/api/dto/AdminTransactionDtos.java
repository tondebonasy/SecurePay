package com.moneyguard.api.dto;

public class AdminTransactionDtos {
    public record UpdateTransactionRequest(String status, String message) {}
}

package com.moneyguard.api.dto;

public class AdminUserDtos {
    public record CreateUserRequest(String phone, String email, String name, String password, String role) {}
    public record UpdateUserRequest(String status) {}
}

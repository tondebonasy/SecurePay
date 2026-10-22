package com.moneyguard.api.web;

public class ApiError {
    private String code;
    private String message;
    private Object details;

    public ApiError() {}

    public ApiError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ApiError(String code, String message, Object details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }

    public String getCode() { return code; }
    public String getMessage() { return message; }
    public Object getDetails() { return details; }
    public void setCode(String code) { this.code = code; }
    public void setMessage(String message) { this.message = message; }
    public void setDetails(Object details) { this.details = details; }
}

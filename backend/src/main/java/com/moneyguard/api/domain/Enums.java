package com.moneyguard.api.domain;

public class Enums {
    public enum OtpPurpose { REGISTER, RESET }
    public enum OtpChannel { SMS, EMAIL }
    public enum Role { USER, ADMIN }
    public enum PaymentRequestStatus { ACTIVE, CONSUMED, CANCELLED, EXPIRED }
    public enum TxStatus {
        CREATED,
        PAYPAL_ORDER_CREATED,
        PAYPAL_CAPTURED,
        PAYNOW_REQUESTED,
        PAYNOW_PAID,
        PAYNOW_FAILED,
        PROOF_SUBMITTED,
        NOTIFIED,
        FAILED
    }
    public enum SecurityStatus { GREEN, AMBER, RED }
    public enum WebhookProvider { PAYPAL, PAYNOW }
    public enum WebhookStatus { RECEIVED, VERIFIED, PROCESSED, FAILED }
    public enum OutboxStatus { PENDING, RUNNING, SUCCESS, FAILED }
    public enum OutboxType { SEND_EMAIL, SEND_SMS, SEND_EXPO, STELLAR_ANCHOR }
}

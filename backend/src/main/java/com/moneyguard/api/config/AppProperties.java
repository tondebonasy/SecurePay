package com.moneyguard.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Cors cors = new Cors();
    private Jwt jwt = new Jwt();
    private Qr qr = new Qr();
    private Idempotency idempotency = new Idempotency();
    private Paypal paypal = new Paypal();
    private Paynow paynow = new Paynow();
    private Expo expo = new Expo();
    private RateLimit rateLimit = new RateLimit();
    private Sms sms = new Sms();
    private Stellar stellar = new Stellar();

    public Cors getCors() { return cors; }
    public Jwt getJwt() { return jwt; }
    public Qr getQr() { return qr; }
    public Idempotency getIdempotency() { return idempotency; }
    public Paypal getPaypal() { return paypal; }
    public Paynow getPaynow() { return paynow; }
    public Expo getExpo() { return expo; }
    public RateLimit getRateLimit() { return rateLimit; }
    public void setRateLimit(RateLimit rateLimit) { this.rateLimit = rateLimit; }
    public Sms getSms() { return sms; }
    public Stellar getStellar() { return stellar; }

    public static class Cors {
        private String allowedOrigins = "http://localhost:5173";
        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }

    public static class Jwt {
        private String secret;
        private long accessTtlMinutes = 30;
        private long refreshTtlDays = 30;
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getAccessTtlMinutes() { return accessTtlMinutes; }
        public void setAccessTtlMinutes(long accessTtlMinutes) { this.accessTtlMinutes = accessTtlMinutes; }
        public long getRefreshTtlDays() { return refreshTtlDays; }
        public void setRefreshTtlDays(long refreshTtlDays) { this.refreshTtlDays = refreshTtlDays; }
    }

    public static class Qr {
        private String hmacSecret;
        public String getHmacSecret() { return hmacSecret; }
        public void setHmacSecret(String hmacSecret) { this.hmacSecret = hmacSecret; }
    }

    public static class Idempotency {
        private long ttlMinutes = 60;
        public long getTtlMinutes() { return ttlMinutes; }
        public void setTtlMinutes(long ttlMinutes) { this.ttlMinutes = ttlMinutes; }
    }

    public static class Paypal {
        private String baseUrl = "https://api-m.sandbox.paypal.com";
        private String clientId;
        private String clientSecret;
        private boolean webhooksEnabled = false;
        private String webhookId;
        private String merchantEmail;
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
        public boolean isWebhooksEnabled() { return webhooksEnabled; }
        public void setWebhooksEnabled(boolean webhooksEnabled) { this.webhooksEnabled = webhooksEnabled; }
        public String getWebhookId() { return webhookId; }
        public void setWebhookId(String webhookId) { this.webhookId = webhookId; }
        public String getMerchantEmail() { return merchantEmail; }
        public void setMerchantEmail(String merchantEmail) { this.merchantEmail = merchantEmail; }
    }

    public static class Paynow {
        private String baseUrl = "https://www.paynow.co.zw";
        private String integrationId;
        private String integrationKey;
        private String returnUrl;
        private String resultUrl;
        private String authEmail;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getIntegrationId() { return integrationId; }
        public void setIntegrationId(String integrationId) { this.integrationId = integrationId; }
        public String getIntegrationKey() { return integrationKey; }
        public void setIntegrationKey(String integrationKey) { this.integrationKey = integrationKey; }
        public String getReturnUrl() { return returnUrl; }
        public void setReturnUrl(String returnUrl) { this.returnUrl = returnUrl; }
        public String getResultUrl() { return resultUrl; }
        public void setResultUrl(String resultUrl) { this.resultUrl = resultUrl; }
        public String getAuthEmail() { return authEmail; }
        public void setAuthEmail(String authEmail) { this.authEmail = authEmail; }
    }


    public static class RateLimit {
        private int requestsPerMinute = 120;
        public int getRequestsPerMinute() { return requestsPerMinute; }
        public void setRequestsPerMinute(int requestsPerMinute) { this.requestsPerMinute = requestsPerMinute; }
    }

    public static class Expo {
        private boolean enabled = false;
        private String accessToken;
        private String workerUrl;
        public String getWorkerUrl() { return workerUrl; }
        public void setWorkerUrl(String workerUrl) { this.workerUrl = workerUrl; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    }

    public static class Sms {
        private String provider = "mock";
        private Twilio twilio = new Twilio();
        public String getProvider() { return provider; }
        public void setProvider(String provider) { this.provider = provider; }
        public Twilio getTwilio() { return twilio; }

        public static class Twilio {
            private String accountSid;
            private String authToken;
            private String fromNumber;
            public String getAccountSid() { return accountSid; }
            public void setAccountSid(String accountSid) { this.accountSid = accountSid; }
            public String getAuthToken() { return authToken; }
            public void setAuthToken(String authToken) { this.authToken = authToken; }
            public String getFromNumber() { return fromNumber; }
            public void setFromNumber(String fromNumber) { this.fromNumber = fromNumber; }
        }
    }

    public static class Stellar {
        private String workerUrl = "http://localhost:3030";
        private boolean enabled = false;
        private String horizonUrl = "https://horizon-testnet.stellar.org";
        private String networkPassphrase = "Test SDF Network ; September 2015";
        private String secretKey;
        public String getWorkerUrl() { return workerUrl; }
        public void setWorkerUrl(String workerUrl) { this.workerUrl = workerUrl; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getHorizonUrl() { return horizonUrl; }
        public void setHorizonUrl(String horizonUrl) { this.horizonUrl = horizonUrl; }
        public String getNetworkPassphrase() { return networkPassphrase; }
        public void setNetworkPassphrase(String networkPassphrase) { this.networkPassphrase = networkPassphrase; }
        public String getSecretKey() { return secretKey; }
        public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
    }


}

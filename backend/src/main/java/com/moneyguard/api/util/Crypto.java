package com.moneyguard.api.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class Crypto {

    private static final SecureRandom rnd = new SecureRandom();

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return hex(b);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String sha512HexUpper(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] b = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return hex(b).toUpperCase();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String hmacSha256Base64Url(String secret, String message) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] out = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String randomNumericCode(int digits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits; i++) {
            sb.append(rnd.nextInt(10));
        }
        return sb.toString();
    }

    private static String hex(byte[] bytes) {
        char[] hexArray = "0123456789abcdef".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}

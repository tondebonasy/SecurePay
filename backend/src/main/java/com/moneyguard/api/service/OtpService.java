package com.moneyguard.api.service;

import com.moneyguard.api.domain.Enums;
import com.moneyguard.api.domain.OtpCode;
import com.moneyguard.api.repo.OtpRepo;
import com.moneyguard.api.util.Crypto;
import com.moneyguard.api.web.ApiException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class OtpService {

    private final OtpRepo repo;

    public OtpService(OtpRepo repo) {
        this.repo = repo;
    }

    public String issueOtp(String subject, Enums.OtpPurpose purpose, Enums.OtpChannel channel) {
        String code = Crypto.randomNumericCode(6);
        OtpCode o = new OtpCode();
        o.setId(Crypto.uuid());
        o.setSubject(subject);
        o.setPurpose(purpose);
        o.setChannel(channel);
        o.setCodeHash(Crypto.sha256Hex(code));
        o.setExpiresAt(Instant.now().plus(Duration.ofMinutes(10)));
        repo.save(o);
        return code;
    }

    public void verifyOtp(String subject, Enums.OtpPurpose purpose, String code) {
        OtpCode otp = repo.findTopBySubjectAndPurposeAndConsumedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                subject, purpose, Instant.now()
        ).orElseThrow(() -> ApiException.badRequest("OTP_INVALID", "Invalid or expired OTP"));

        if (!otp.getCodeHash().equals(Crypto.sha256Hex(code))) {
            throw ApiException.badRequest("OTP_INVALID", "Invalid or expired OTP");
        }

        otp.setConsumedAt(Instant.now());
        repo.save(otp);
    }
}

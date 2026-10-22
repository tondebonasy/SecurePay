package com.moneyguard.api.service;

import com.moneyguard.api.domain.DevicePushToken;
import com.moneyguard.api.repo.PushTokenRepo;
import com.moneyguard.api.util.Crypto;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {
    private final PushTokenRepo repo;

    public DeviceService(PushTokenRepo repo) {
        this.repo = repo;
    }

    public void upsertExpoToken(String userId, String deviceId, String expoToken, String platform) {
        Optional<DevicePushToken> existing = repo.findByUserIdAndDeviceId(userId, deviceId);
        DevicePushToken d = existing.orElseGet(DevicePushToken::new);
        if (d.getId() == null) d.setId(Crypto.uuid());
        d.setUserId(userId);
        d.setDeviceId(deviceId);
        d.setExpoPushToken(expoToken);
        d.setPlatform(platform);
        repo.save(d);
    }

    public List<String> findExpoTokensForUser(String userId) {
        return repo.findAllByUserId(userId).stream()
                .map(DevicePushToken::getExpoPushToken)
                .filter(t -> t != null && !t.isBlank())
                .toList();
    }
}

package com.moneyguard.api.repo;

import com.moneyguard.api.domain.DevicePushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PushTokenRepo extends JpaRepository<DevicePushToken, String> {
    Optional<DevicePushToken> findByUserIdAndDeviceId(String userId, String deviceId);
    List<DevicePushToken> findAllByUserId(String userId);
    void deleteAllByUserId(String userId);
}

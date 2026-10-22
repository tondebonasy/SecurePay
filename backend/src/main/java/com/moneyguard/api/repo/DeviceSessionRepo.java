package com.moneyguard.api.repo;

import com.moneyguard.api.domain.DeviceSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceSessionRepo extends JpaRepository<DeviceSession, String> {
    Optional<DeviceSession> findByUserIdAndDeviceIdAndRevokedAtIsNull(String userId, String deviceId);
    Optional<DeviceSession> findByIdAndRevokedAtIsNull(String id);
    List<DeviceSession> findByRefreshExpiresAtBefore(Instant cutoff);
    List<DeviceSession> findAllByUserIdAndRevokedAtIsNull(String userId);
}

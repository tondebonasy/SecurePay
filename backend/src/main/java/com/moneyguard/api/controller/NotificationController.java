package com.moneyguard.api.controller;

import com.moneyguard.api.dto.DeviceDtos;
import com.moneyguard.api.security.AuthUser;
import com.moneyguard.api.service.DeviceService;
import com.moneyguard.api.web.ApiException;
import com.moneyguard.api.web.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/notifications")
public class NotificationController {

    private final DeviceService deviceService;

    public NotificationController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/register-token")
    public ApiResponse<Object> registerToken(@RequestBody DeviceDtos.RegisterPushTokenRequest req,
                                             @AuthenticationPrincipal AuthUser user,
                                             @RequestHeader(name = "X-Device-Id", required = true) String deviceId) {
        if (user == null) {
            throw ApiException.unauthorized("UNAUTHORIZED", "Unauthorized");
        }
        deviceService.upsertExpoToken(user.getUserId(), deviceId, req.expoPushToken(), req.platform());
        return ApiResponse.ok(Map.of("saved", true));
    }
}

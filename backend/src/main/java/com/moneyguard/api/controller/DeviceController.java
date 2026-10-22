package com.moneyguard.api.controller;

import com.moneyguard.api.dto.DeviceDtos;
import com.moneyguard.api.security.AuthUser;
import com.moneyguard.api.service.DeviceService;
import com.moneyguard.api.web.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/devices")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/expo-token")
    public ApiResponse<Object> registerPushToken(@RequestBody DeviceDtos.RegisterPushTokenRequest req,
                                                 @AuthenticationPrincipal AuthUser user,
                                                 @RequestHeader(name = "X-Device-Id", required = true) String deviceId) {
        deviceService.upsertExpoToken(user.getUserId(), deviceId, req.expoPushToken(), req.platform());
        return ApiResponse.ok(Map.of("saved", true));
    }

}

package com.moneyguard.api.controller;
import com.moneyguard.api.web.ApiResponse;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class HealthController {
  @GetMapping("/v1/health")
  public ApiResponse<Object> health() { return ApiResponse.ok(Map.of("status", "ok")); }
}

package com.moneyguard.api;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration")
@org.springframework.context.annotation.Import(IntegrationFixtures.class)
class OperationsIntegrationTest {
  @Autowired com.moneyguard.api.repo.TxRepo txRepo;
  @Autowired TestRestTemplate http;
  HttpHeaders headers;

  Map<?, ?> login(String phone) {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    h.set("X-Device-Id", "integration-device");
    var result =
        http.postForEntity(
            "/v1/auth/login",
            new HttpEntity<>(Map.of("phone", phone, "password", "IntegrationOnly!2026"), h),
            Map.class);
    assertEquals(200, result.getStatusCode().value());
    return (Map<?, ?>) result.getBody().get("data");
  }

  @BeforeEach
  void auth() {
    http.getRestTemplate()
        .setRequestFactory(new org.springframework.http.client.JdkClientHttpRequestFactory());
    var token = login("+263770000000");
    headers = new HttpHeaders();
    headers.setBearerAuth((String) token.get("accessToken"));
    headers.set("X-Device-Id", "integration-device");
    headers.setContentType(MediaType.APPLICATION_JSON);
  }

  @Test
  void adminAnalyticsAndSearchReconcile() {
    var health = http.getForEntity("/v1/health", Map.class);
    assertEquals(200, health.getStatusCode().value());
    assertEquals("ok", ((Map<?, ?>) health.getBody().get("data")).get("status"));
    var risk =
        http.exchange(
            "/v1/admin/operations/risk", HttpMethod.GET, new HttpEntity<>(headers), Map.class);
    assertEquals(200, risk.getStatusCode().value());
    assertTrue(((java.util.List<?>) risk.getBody().get("data")).size() >= 3);
    var result =
        http.exchange(
            "/v1/admin/operations/analytics?currency=USD",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class);
    assertEquals(200, result.getStatusCode().value());
    var data = (Map<?, ?>) result.getBody().get("data");
    assertEquals(42, ((Number) data.get("totalTx")).intValue());
    var page =
        http.exchange(
            "/v1/admin/operations/transactions?currency=USD&size=5",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class);
    assertEquals(
        42, ((Number) ((Map<?, ?>) page.getBody().get("data")).get("totalElements")).intValue());
  }

  @Test
  void standardUserCannotAdminister() {
    var token = login("+263770000001");
    headers.setBearerAuth((String) token.get("accessToken"));
    assertEquals(
        403,
        http.exchange(
                "/v1/admin/operations/analytics",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class)
            .getStatusCode()
            .value());
  }

  @Test
  void publicBootstrapIsDisabled() {
    assertEquals(
        403,
        http.exchange(
                "/v1/invites/first-admin",
                HttpMethod.POST,
                new HttpEntity<>(Map.of(), headers),
                String.class)
            .getStatusCode()
            .value());
  }

  @Test
  void invalidInputsAndLoginFail() {
    assertEquals(
        400,
        http.exchange(
                "/v1/admin/operations/transactions?size=1000",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class)
            .getStatusCode()
            .value());
    assertEquals(
        400,
        http.exchange(
                "/v1/admin/operations/reconcile",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("csv", "bad csv"), headers),
                String.class)
            .getStatusCode()
            .value());
    HttpHeaders h = new HttpHeaders();
    h.set("X-Device-Id", "bad-device");
    assertEquals(
        401,
        http.postForEntity(
                "/v1/auth/login",
                new HttpEntity<>(Map.of("phone", "+263770000000", "password", "incorrect"), h),
                String.class)
            .getStatusCode()
            .value());
  }

  @Test
  void workflowHistoryDuplicatesAndForbiddenTransitions() {
    var found =
        http.exchange(
            "/v1/admin/operations/transactions?status=PAYNOW_PAID&size=1",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Map.class);
    var records = (java.util.List<?>) ((Map<?, ?>) found.getBody().get("data")).get("content");
    String id = (String) ((Map<?, ?>) records.get(0)).get("id");
    assertEquals(
        200,
        http.exchange(
                "/v1/admin/operations/transactions/" + id + "/verify",
                HttpMethod.POST,
                new HttpEntity<>(Map.of(), headers),
                Map.class)
            .getStatusCode()
            .value());
    assertEquals(
        200,
        http.exchange(
                "/v1/admin/operations/transactions/" + id + "/refund",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("reason", "Customer requested a manual refund"), headers),
                Map.class)
            .getStatusCode()
            .value());
    assertEquals(
        409,
        http.exchange(
                "/v1/admin/operations/transactions/" + id + "/refund",
                HttpMethod.POST,
                new HttpEntity<>(Map.of("reason", "Customer requested a manual refund"), headers),
                Map.class)
            .getStatusCode()
            .value());
    assertEquals(
        400,
        http.exchange(
                "/v1/admin/transactions/" + id,
                HttpMethod.PATCH,
                new HttpEntity<>(
                    Map.of("status", "PAYPAL_CAPTURED", "message", "Administrative investigation"),
                    headers),
                Map.class)
            .getStatusCode()
            .value());
    var result =
        http.exchange(
            "/v1/admin/operations/reconcile",
            HttpMethod.POST,
            new HttpEntity<>(
                Map.of("csv", "reference,amount,currency\nTEST-0-2,63.50,USD"), headers),
            Map.class);
    assertEquals(200, result.getStatusCode().value());
    assertEquals(
        200,
        http.exchange(
                "/v1/admin/operations/history/RECONCILIATION",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class)
            .getStatusCode()
            .value());
  }

  @Test
  void logoutRevokesAccessAndProfileHidesHashes() {
    var me = http.exchange("/v1/auth/me", HttpMethod.GET, new HttpEntity<>(headers), String.class);
    assertEquals(200, me.getStatusCode().value());
    assertFalse(me.getBody().contains("passwordHash"));
    var token = login("+263770000000");
    headers.setBearerAuth((String) token.get("accessToken"));
    assertEquals(
        200,
        http.exchange(
                "/v1/auth/logout",
                HttpMethod.POST,
                new HttpEntity<>(
                    Map.of(
                        "sessionId",
                        token.get("sessionId"),
                        "refreshToken",
                        token.get("refreshToken")),
                    headers),
                String.class)
            .getStatusCode()
            .value());
    assertEquals(
        401,
        http.exchange(
                "/v1/admin/operations/analytics",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class)
            .getStatusCode()
            .value());
  }

  @Test
  void concurrentTransactionUpdatesCannotOverwriteNewerState() {
    var original = txRepo.findByStatus(com.moneyguard.api.domain.Enums.TxStatus.CREATED).get(0);
    var stale = txRepo.findById(original.getId()).orElseThrow();
    original.setStatus(com.moneyguard.api.domain.Enums.TxStatus.FAILED);
    txRepo.saveAndFlush(original);
    stale.setStatus(com.moneyguard.api.domain.Enums.TxStatus.PAYPAL_ORDER_CREATED);
    assertThrows(
        org.springframework.dao.OptimisticLockingFailureException.class,
        () -> txRepo.saveAndFlush(stale));
    assertEquals(
        com.moneyguard.api.domain.Enums.TxStatus.FAILED,
        txRepo.findById(original.getId()).orElseThrow().getStatus());
  }
}

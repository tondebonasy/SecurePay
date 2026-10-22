package com.moneyguard.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.moneyguard.api.domain.*;
import com.moneyguard.api.repo.*;
import com.moneyguard.api.service.OperationsService;
import com.moneyguard.api.web.ApiException;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;

class OperationsServiceTest {
  TxRepo txs = mock(TxRepo.class);
  OperationRepo ops = mock(OperationRepo.class);
  OperationsService service = new OperationsService(txs, ops);

  Tx transaction() {
    Tx t = new Tx();
    t.setId("tx-1");
    t.setCurrency("USD");
    t.setAmount(new BigDecimal("25.00"));
    t.setStatus(Enums.TxStatus.PAYNOW_PAID);
    t.setPaynowReference("REF-1");
    return t;
  }

  @BeforeEach
  void setup() {
    when(ops.save(any())).thenAnswer(i -> i.getArgument(0));
  }

  @Test
  void moneyMatchingUsesDecimalsAndCurrency() {
    var t = transaction();
    assertEquals("MATCHED", OperationsService.match(t, new BigDecimal("25.0"), "USD"));
    assertEquals("CURRENCY_MISMATCH", OperationsService.match(t, new BigDecimal("25"), "EUR"));
    assertEquals("AMOUNT_MISMATCH", OperationsService.match(t, new BigDecimal("24.99"), "USD"));
  }

  @Test
  void referencesNeverCountAsTrustedVerification() {
    var t = transaction();
    when(txs.findById(t.getId())).thenReturn(Optional.of(t));
    var result = service.verify(t.getId(), "admin");
    assertEquals("REQUIRES_MANUAL_REVIEW", result.state);
    assertTrue(result.evidence.contains("No fresh trusted provider"));
  }

  @Test
  void refundIsManualAndRequiresPaidState() {
    var t = transaction();
    when(txs.findById(t.getId())).thenReturn(Optional.of(t));
    assertEquals(
        "MANUAL_REQUESTED", service.refund(t.getId(), "admin", "Customer requested return").state);
    t.setStatus(Enums.TxStatus.CREATED);
    assertThrows(
        ApiException.class, () -> service.refund(t.getId(), "admin", "Customer requested return"));
  }

  @Test
  void refundRejectsDuplicateAndMissingReason() {
    var t = transaction();
    when(txs.findById(t.getId())).thenReturn(Optional.of(t));
    assertThrows(ApiException.class, () -> service.refund(t.getId(), "admin", "short"));
    when(ops.existsByDedupKey("REFUND:tx-1")).thenReturn(true);
    assertThrows(
        ApiException.class, () -> service.refund(t.getId(), "admin", "Customer requested return"));
  }

  @Test
  void settlementFindsMissingMismatchAndDuplicate() {
    var t = transaction();
    when(txs.findAll()).thenReturn(List.of(t));
    var result =
        service.reconcile("reference,amount,currency\nREF-1,25.00,USD\nMISSING,2.00,USD", "admin");
    assertEquals("MATCHED", result.get(0).state);
    assertEquals("MISSING_INTERNAL", result.get(1).state);
    assertEquals(
        "AMOUNT_MISMATCH",
        service.reconcile("reference,amount,currency\nREF-1,26.00,USD", "admin").get(0).state);
    assertEquals(
        "CURRENCY_MISMATCH",
        service.reconcile("reference,amount,currency\nREF-1,25.00,EUR", "admin").get(0).state);
    assertEquals(
        "DUPLICATE_REFERENCE",
        service
            .reconcile("reference,amount,currency\nREF-1,25.00,USD\nREF-1,25.00,USD", "admin")
            .get(0)
            .state);
    verify(txs, never()).save(any());
  }

  @Test
  void invalidImportWritesNothing() {
    assertThrows(
        ApiException.class,
        () -> service.reconcile("reference,amount,currency\nREF-1,-25.00,USD", "admin"));
    verify(ops, never()).save(any());
  }

  @Test
  void userSerializationDoesNotExposePassword() throws Exception {
    User user = new User();
    user.setPasswordHash("private-password-hash");
    String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(user);
    assertFalse(json.contains("passwordHash"));
    assertFalse(json.contains("private-password-hash"));
  }
}

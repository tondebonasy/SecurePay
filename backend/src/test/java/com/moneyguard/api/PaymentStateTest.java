package com.moneyguard.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.moneyguard.api.domain.*;
import com.moneyguard.api.repo.*;
import com.moneyguard.api.service.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class PaymentStateTest {
  TxRepo txs = mock(TxRepo.class);
  TransactionProofRepo proofs = mock(TransactionProofRepo.class);
  StellarClient stellar = mock(StellarClient.class);
  PayPalService paypal = mock(PayPalService.class);
  TransactionService service =
      new TransactionService(
          txs,
          mock(TransactionAuditRepo.class),
          proofs,
          paypal,
          mock(PaynowService.class),
          stellar,
          mock(ExpoPushService.class),
          mock(DeviceService.class),
          mock(UserService.class));

  Tx pending() {
    Tx tx = new Tx();
    tx.setId("test-tx");
    tx.setUserId("test-user");
    tx.setAmount(new BigDecimal("25.00"));
    tx.setCurrency("USD");
    tx.setCreatedAt(Instant.now());
    tx.setStatus(Enums.TxStatus.PAYNOW_REQUESTED);
    tx.setPaynowReference("test-ref");
    when(txs.findByPaynowReference("test-ref")).thenReturn(Optional.of(tx));
    return tx;
  }

  @Test
  void confirmedProviderPaymentDoesNotInventProofWhenAnchoringDisabled() {
    Tx tx = pending();
    when(stellar.anchor(anyString(), anyString()))
        .thenReturn(new StellarClient.AnchorResult(false, null, null, null));
    service.handlePaynowWebhook("test-ref", "Paid");
    assertEquals(Enums.TxStatus.NOTIFIED, tx.getStatus());
    verify(proofs, never()).save(any());
    service.handlePaynowWebhook("test-ref", "Paid");
    verify(stellar, times(1)).anchor(anyString(), anyString());
  }

  @Test
  void delayedFailureCannotUndoConfirmedPayment() {
    Tx tx = pending();
    tx.setStatus(Enums.TxStatus.NOTIFIED);
    service.handlePaynowWebhook("test-ref", "Failed");
    assertEquals(Enums.TxStatus.NOTIFIED, tx.getStatus());
    verify(txs, never()).save(any());
  }

  @Test
  void unpaidIsNotPaid() {
    Tx tx = pending();
    service.handlePaynowWebhook("test-ref", "Unpaid");
    assertEquals(Enums.TxStatus.PAYNOW_REQUESTED, tx.getStatus());
    verifyNoInteractions(stellar);
  }

  @Test
  void repeatedPaypalConfirmationDoesNotCaptureAgain() {
    Tx tx = pending();
    tx.setPaypalOrderId("test-order");
    tx.setPaypalCaptureId("test-capture");
    tx.setStatus(Enums.TxStatus.NOTIFIED);
    when(txs.findById("test-tx")).thenReturn(Optional.of(tx));
    assertSame(tx, service.confirm("test-tx"));
    verifyNoInteractions(paypal);
  }
}

package com.moneyguard.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.moneyguard.api.config.AppProperties;
import com.moneyguard.api.domain.IdempotencyRecord;
import com.moneyguard.api.repo.IdempotencyRepo;
import com.moneyguard.api.service.IdempotencyService;
import com.moneyguard.api.web.ApiException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class IdempotencyServiceTest {
  @Test
  void replayReturnsStoredResultAndRejectsDifferentBody() {
    var repo = mock(IdempotencyRepo.class);
    var service = new IdempotencyService(repo, new AppProperties());
    var record = new IdempotencyRecord();
    record.setRequestHash("request-hash");
    record.setResponseStatus(201);
    record.setResponseBody("stored-response");
    record.setExpiresAt(Instant.now().plusSeconds(60));
    when(repo.findByUserIdAndIdemKey("user", "key")).thenReturn(Optional.of(record));
    assertEquals(201, service.find("user", "key", "request-hash").status());
    assertEquals("stored-response", service.find("user", "key", "request-hash").body());
    assertThrows(ApiException.class, () -> service.find("user", "key", "different-request"));
  }

  @Test
  void expiredAndMissingRecordsAreNotReplayed() {
    var repo = mock(IdempotencyRepo.class);
    var service = new IdempotencyService(repo, new AppProperties());
    var record = new IdempotencyRecord();
    record.setExpiresAt(Instant.now().minusSeconds(1));
    when(repo.findByUserIdAndIdemKey("user", "expired")).thenReturn(Optional.of(record));
    assertNull(service.find("user", "expired", "request"));
    assertNull(service.find("user", "missing", "request"));
  }
}

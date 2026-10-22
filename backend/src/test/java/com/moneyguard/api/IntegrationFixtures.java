package com.moneyguard.api;

import com.moneyguard.api.domain.*;
import com.moneyguard.api.repo.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@org.springframework.boot.test.context.TestConfiguration

public class IntegrationFixtures {
  @Bean
  CommandLineRunner seed(UserRepo users, TxRepo txs, PasswordEncoder encoder) {
    return args -> {
      for (int i = 0; i < 6; i++) {
        User u = new User();
        u.setId(UUID.randomUUID().toString());
        u.setPhone("+26377000000" + i);
        u.setPhoneNormalized("+26377000000" + i);
        u.setEmail("fixture" + i + "@example.test");
        u.setDisplayName(i == 0 ? "Test Administrator" : "Fictional Customer " + i);
        u.setPasswordHash(encoder.encode("IntegrationOnly!2026"));
        u.setRoles(i == 0 ? "ADMIN,USER" : "USER");
        u.setStatus("ACTIVE");
        users.save(u);
        for (int j = 0; j < 8; j++) {
          Tx t = new Tx();
          t.setId(UUID.randomUUID().toString());
          t.setUserId(u.getId());
          t.setAmount(new BigDecimal(i == 2 && j == 2 ? "12500.00" : 25 + i * 37 + j * 19 + ".50"));
          t.setCurrency(j == 7 ? "EUR" : "USD");
          t.setCreatedAt(Instant.now().minusSeconds((i * 8 + j) * 18000L));
          t.setStatus(
              j % 4 == 0
                  ? Enums.TxStatus.FAILED
                  : j % 4 == 1 ? Enums.TxStatus.CREATED : Enums.TxStatus.PAYNOW_PAID);
          t.setPaynowReference(i == 1 && j == 3 ? "TEST-1-2" : "TEST-" + i + "-" + j);
          t.setPaynowMethod("TEST");
          txs.save(t);
        }
      }
    };
  }
}


package com.moneyguard.api.repo;

import com.moneyguard.api.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditRepo extends JpaRepository<AuditEvent, String> {}

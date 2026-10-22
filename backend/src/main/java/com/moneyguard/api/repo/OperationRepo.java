package com.moneyguard.api.repo;

import com.moneyguard.api.domain.Operation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepo extends JpaRepository<Operation, String> {
  List<Operation> findTop100ByKindOrderByCreatedAtDesc(String kind);

  boolean existsByDedupKey(String dedupKey);
}

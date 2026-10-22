package com.moneyguard.api.repo;

import com.moneyguard.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, String> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByPhoneNormalized(String phoneNormalized);
    Optional<User> findByEmail(String email);
    long countByRolesContaining(String role);
    long countByRolesContainingAndStatusNot(String role, String status);
}

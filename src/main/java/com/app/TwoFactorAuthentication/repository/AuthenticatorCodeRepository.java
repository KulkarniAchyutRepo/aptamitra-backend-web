package com.app.TwoFactorAuthentication.repository;

import com.app.TwoFactorAuthentication.entity.AuthenticatorCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AuthenticatorCodeRepository extends JpaRepository<AuthenticatorCode, Long> {

    AuthenticatorCode findByUserEmail(String userEmail);

    void deleteByUserEmail(String userEmail);

    void deleteByExpiryTimeBefore(LocalDateTime now);

    // Add custom query method to find by code if needed
}


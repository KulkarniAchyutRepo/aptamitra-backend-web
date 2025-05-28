package com.app.TwoFactorAuthentication.repository;

import com.app.TwoFactorAuthentication.entity.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    Optional<Otp> findByEmail(String email);
    Optional<Otp> findByEmailAndOtp(String email, String otp);
    void deleteByEmail(String email);
    void deleteByExpiryTimeBefore(LocalDateTime now);
}

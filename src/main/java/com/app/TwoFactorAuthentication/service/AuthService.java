package com.app.TwoFactorAuthentication.service;
import com.app.TwoFactorAuthentication.entity.Otp;
import com.app.TwoFactorAuthentication.entity.User;
import com.app.TwoFactorAuthentication.exceptions.authExceptions.InvalidPasswordException;
import com.app.TwoFactorAuthentication.exceptions.authExceptions.UserNotFoundException;
import com.app.TwoFactorAuthentication.repository.OtpRepository;
import com.app.TwoFactorAuthentication.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    private static final int OTP_VALIDITY_MINUTES = 5;

    public boolean checkUserExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public String authenticateViaPassword(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);

        if (user.isEmpty()) {
            throw new UserNotFoundException("Invalid email or password.");
        }

        // Verify the password using BCrypt
        if (!BCrypt.checkpw(password, user.get().getPassword())) {
            throw new InvalidPasswordException("Invalid email or password.");
        }

        return "User authenticated successfully via password.";
    }

    @Transactional
    public String generateAndSendOtp(String email) {
        // Check if an OTP is already generated and not expired
        Optional<Otp> existingOtp = otpRepository.findByEmail(email);

        if (existingOtp.isPresent() && existingOtp.get().getExpiryTime().isAfter(LocalDateTime.now())) {
            return "OTP has already been sent to your email.";
        }

        // Generate a new OTP if no valid OTP exists
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        // Delete any existing stale OTP for the email
        otpRepository.deleteByEmail(email);

        // Create and save the new OTP
        Otp otpEntity = new Otp();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        otpRepository.save(otpEntity);

        // Send the OTP to the user's email
        emailService.sendOtpEmail(email, otp);

        return "OTP has been sent to your email.";
    }

    @Transactional
    public boolean verifyOtp(String email, String otp) {
        Optional<Otp> storedOtp = otpRepository.findByEmailAndOtp(email, otp);

        if (storedOtp.isPresent() && storedOtp.get().getExpiryTime().isAfter(LocalDateTime.now())) {
            otpRepository.deleteByEmail(email);
            return true;
        }
        return false;
    }

    @Scheduled(fixedRate = 60000) // Runs every 1 minute
    @Transactional
    public void deleteStaleOtps() {
        otpRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
    }
}
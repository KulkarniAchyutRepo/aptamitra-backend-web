package com.app.TwoFactorAuthentication.service;

import com.app.TwoFactorAuthentication.entity.AuthenticatorCode;
import com.app.TwoFactorAuthentication.repository.AuthenticatorCodeRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthenticatorService {

    @Autowired
    private AuthenticatorCodeRepository codeRepository;

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRATION_SECONDS = 60;
    @Transactional
    public String generateNewCode(String userEmail) {
        // Delete if there is an existing code for this email
        codeRepository.deleteByUserEmail(userEmail);

        // Generate a random 6-digit code
        String code = generateRandomCode(CODE_LENGTH);

        // Save code with expiry time
        AuthenticatorCode authCode = new AuthenticatorCode();
        authCode.setUserEmail(userEmail);
        authCode.setCode(code);
        authCode.setExpiryTime(LocalDateTime.now().plusSeconds(EXPIRATION_SECONDS));
        codeRepository.save(authCode);

        return code;
    }

    public boolean validateCode(String userEmail, String code) {
        AuthenticatorCode authCode = codeRepository.findByUserEmail(userEmail);

        if (authCode != null && authCode.getCode().equals(code) && authCode.getExpiryTime().isAfter(LocalDateTime.now())) {
            // Code is valid, delete it from repository
            codeRepository.delete(authCode);
            return true;
        }

        return false;
    }

    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10)); // Generate digits 0-9
        }

        return sb.toString();
    }

    @Scheduled(fixedRate = 60000) // Runs every 1 minute
    @Transactional
    public void deleteStaleOtps() {
        codeRepository.deleteByExpiryTimeBefore(LocalDateTime.now());
    }
}


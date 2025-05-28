package com.app.TwoFactorAuthentication.controller;

import com.app.TwoFactorAuthentication.dto.request.RegistrationRequest;
import com.app.TwoFactorAuthentication.dto.response.StandardApiResponse;
import com.app.TwoFactorAuthentication.service.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class RegistrationController {
    RegistrationService registrationService;
    @Autowired
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/api/v1/register")
    public ResponseEntity<StandardApiResponse> register(@RequestBody RegistrationRequest registrationRequest) {
        String email = registrationRequest.email();
        String name = registrationRequest.name();
        String password = registrationRequest.password();
        String response = registrationService.registerUser(email, name, password);
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                response,
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }
}

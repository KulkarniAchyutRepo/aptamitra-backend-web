package com.app.TwoFactorAuthentication.controller;

import com.app.TwoFactorAuthentication.dto.request.*;
import com.app.TwoFactorAuthentication.dto.response.AuthJwtResponse;
import com.app.TwoFactorAuthentication.dto.response.StandardApiResponse;
import com.app.TwoFactorAuthentication.entity.User;
import com.app.TwoFactorAuthentication.repository.UserRepository;
import com.app.TwoFactorAuthentication.service.AuthenticatorService;
import com.app.TwoFactorAuthentication.service.JwtService;
import com.app.TwoFactorAuthentication.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/")
public class AuthenticatorController {

    @Autowired
    private AuthenticatorService authenticatorService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/authenticator-generate-code")
    public ResponseEntity<StandardApiResponse> generateCode(@RequestBody AuthenticatorGenerateCodeRequest authenticatorGenerateCodeRequest) {
        String email = authenticatorGenerateCodeRequest.email();
        String code = authenticatorService.generateNewCode(email);
        Map<String, String> map = new HashMap<>();
        map.put("code", code);
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                "Code generation successful.",
                map,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

    @PostMapping("/authenticator-validate-code")
    public ResponseEntity<?> validateCode(@RequestBody AuthenticatorValidateCodeRequest authenticatorValidateCodeRequest, HttpServletResponse response) {
        String email = authenticatorValidateCodeRequest.email();
        String code = authenticatorValidateCodeRequest.code();
        if (authenticatorService.validateCode(email, code)) {

            // Generate JWT tokens
            String accessToken = jwtService.generateToken(email, true);
            String refreshToken = jwtService.generateToken(email, false);

            // Create cookies using ResponseCookie
            ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofMinutes(10))
                    .sameSite("None") // SameSite directly supported
                    .build();

            ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofDays(1))
                    .sameSite("None")
                    .build();

            response.addHeader("Set-Cookie", accessTokenCookie.toString());
            response.addHeader("Set-Cookie", refreshTokenCookie.toString());

            User user = userService.findByEmail(email);
//            AuthJwtResponse authJwtResponse = new AuthJwtResponse(accessToken, refreshToken, user);
            StandardApiResponse standardApiResponse = new StandardApiResponse(
                    true,
                    HttpStatus.OK.value(),
                    "Code verification successful.",
                    user,
                    LocalDateTime.now()
            );
            return ResponseEntity.ok(standardApiResponse);
        } else {
            StandardApiResponse standardApiResponse = new StandardApiResponse(
                    false,
                    HttpStatus.UNAUTHORIZED.value(),
                    "Invalid code or expired.",
                    null,
                    LocalDateTime.now()
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(standardApiResponse);
        }
    }

    @PostMapping("/forgot-password-verify-email-otp")
    public ResponseEntity<StandardApiResponse> verifyEmailLogin(@RequestBody VerifyEmailOtpRequest verifyEmailOtpRequest, HttpServletResponse response) {
        String email = verifyEmailOtpRequest.email();
        String otp = verifyEmailOtpRequest.otp();

        boolean isVerified = authenticatorService.validateCode(email, otp);
        if (isVerified) {
            // Generate reset password JWT token
            String resetPasswordToken = jwtService.generateResetPasswordToken(email);
            ResponseCookie resetPasswordTokenCookie = ResponseCookie.from("accessToken", resetPasswordToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .maxAge(Duration.ofMinutes(5))
                    .sameSite("None") // SameSite directly supported
                    .build();
            response.addHeader("Set-Cookie", resetPasswordTokenCookie.toString());
            StandardApiResponse standardApiResponse = new StandardApiResponse(
                    true,
                    HttpStatus.OK.value(),
                    "Email & Password authenticated successfully.",
                    null,
                    LocalDateTime.now()
            );
            return ResponseEntity.ok(standardApiResponse);
        }
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                false,
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication failed. Invalid otp or otp expired.",
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(standardApiResponse);
    }

    @PutMapping("/forgot-password-reset-password")
    public ResponseEntity<StandardApiResponse> resetPassword(@RequestBody ResetPasswordRequest resetPasswordRequest, HttpServletResponse response, HttpServletRequest request){
        String password = resetPasswordRequest.password();
        // Retrieve the JWT from the "accessToken" cookie
        Optional<String> tokenOptional = jwtService.getJwtFromCookies(request, "accessToken");
        String token = "";
        if (!tokenOptional.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new StandardApiResponse(false, HttpStatus.UNAUTHORIZED.value(), "Unauthorized: No token found.", null, LocalDateTime.now()));
        }else{
            token = tokenOptional.get();
        }

        // Extract the username/email from the token
        String email = jwtService.getUsernameFromToken(token);
        // Generate JWT tokens
        String accessToken = jwtService.generateToken(email, true);
        String refreshToken = jwtService.generateToken(email, false);
        // Create cookies using ResponseCookie
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(10))
                .sameSite("None") // SameSite directly supported
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());
        User user = userService.updatePassword(email, password);
//        AuthJwtResponse authJwtResponse = new AuthJwtResponse(accessToken, refreshToken, user);
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                "Reset Password successful.",
                user,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

}


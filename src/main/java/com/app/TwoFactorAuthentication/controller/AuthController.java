package com.app.TwoFactorAuthentication.controller;
import com.app.TwoFactorAuthentication.dto.request.LoginViaEmailOtpRequest;
import com.app.TwoFactorAuthentication.dto.request.LoginViaPasswordRequest;
import com.app.TwoFactorAuthentication.dto.request.VerifyEmailOtpRequest;
import com.app.TwoFactorAuthentication.dto.response.AuthJwtResponse;
import com.app.TwoFactorAuthentication.dto.request.RefreshTokenRequest;
import com.app.TwoFactorAuthentication.dto.response.StandardApiResponse;
import com.app.TwoFactorAuthentication.entity.User;
import com.app.TwoFactorAuthentication.repository.UserRepository;
import com.app.TwoFactorAuthentication.service.AuthService;
import com.app.TwoFactorAuthentication.service.JwtService;
import com.app.TwoFactorAuthentication.service.RegistrationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import com.app.TwoFactorAuthentication.exceptions.authExceptions.UserNotFoundException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private AuthService authService;
    private RegistrationService registrationService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Autowired
    public AuthController(AuthService authService, RegistrationService registrationService, AuthenticationManager authenticationManager, UserDetailsService userDetailsService, JwtService jwtService, UserRepository userRepository) {
        this.authService = authService;
        this.registrationService = registrationService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login-password")
    public ResponseEntity<StandardApiResponse> loginViaPassword(@RequestBody LoginViaPasswordRequest loginViaPasswordRequest,HttpServletResponse response) {
        String email = loginViaPasswordRequest.getEmail();
        String password = loginViaPasswordRequest.getPassword();

        // Authenticate using Spring Security
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        // Generate temporary JWT token
        String temporaryToken = jwtService.generateTemporaryToken(email);
        ResponseCookie temporaryTokenCookie = ResponseCookie.from("accessToken", temporaryToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(10))
                .sameSite("None") // SameSite directly supported
                .build();
        response.addHeader("Set-Cookie", temporaryTokenCookie.toString());
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                "Email & Password authenticated successfully.",
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }


    @PostMapping("/auth/refresh")
    public ResponseEntity<?> refreshAccessToken(HttpServletRequest request) {
        Optional<String> refreshTokenOpt = jwtService.getJwtFromCookies(request, "refreshToken");

        if (refreshTokenOpt.isPresent() && jwtService.validateToken(refreshTokenOpt.get())) {
            String email = jwtService.getUsernameFromToken(refreshTokenOpt.get());
            String newAccessToken = jwtService.generateToken(email, true);

            ResponseCookie accessCookie = ResponseCookie.from("accessToken", newAccessToken)
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("None")
                    .maxAge(600) // 10 minutes
                    .build();

            StandardApiResponse standardApiResponse = new StandardApiResponse(
                    true,
                    HttpStatus.OK.value(),
                    "Access token refreshed.",
                    null,
                    LocalDateTime.now()
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .body(standardApiResponse);
        }

        StandardApiResponse standardApiResponse = new StandardApiResponse(
                false,
                HttpStatus.BAD_REQUEST.value(),
                "Invalid refresh token.",
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(standardApiResponse);
    }

    @PostMapping("/login-email-otp")
    public ResponseEntity<StandardApiResponse> loginViaEmail(@RequestBody LoginViaEmailOtpRequest loginViaEmailRequest) {
        String email = loginViaEmailRequest.email();
        if (!authService.checkUserExists(email)) {
            throw new UserNotFoundException("User does not exist. Please register first.");
        }

        String responseMessage = authService.generateAndSendOtp(email);
        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                responseMessage,
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.ok(standardApiResponse);
    }

    @PostMapping("/verify-email-otp")
    public ResponseEntity<StandardApiResponse> verifyEmailLogin(@RequestBody VerifyEmailOtpRequest verifyEmailOtpRequest,
                                                                HttpServletResponse response) {
        String email = verifyEmailOtpRequest.email();
        String otp = verifyEmailOtpRequest.otp();

        boolean isVerified = authService.verifyOtp(email, otp);
        if (isVerified) {
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

            User user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
//            AuthJwtResponse authJwtResponse = new AuthJwtResponse(accessToken, refreshToken, user);
            StandardApiResponse standardApiResponse = new StandardApiResponse(
                    true,
                    HttpStatus.OK.value(),
                    "Email otp authentication successful.",
                    user,
                    LocalDateTime.now()
            );
            return ResponseEntity.ok(standardApiResponse);
        }

        StandardApiResponse standardApiResponse = new StandardApiResponse(
                false,
                HttpStatus.UNAUTHORIZED.value(),
                "Authentication failed. Invalid OTP or OTP expired.",
                null,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(standardApiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<StandardApiResponse> logout(HttpServletResponse response) {
        // Create expired ResponseCookies
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("None") // Prevent CSRF attacks
                .build();

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("Strict")
                .build();

        // Add cookies to response header
        response.addHeader("Set-Cookie", accessTokenCookie.toString());
        response.addHeader("Set-Cookie", refreshTokenCookie.toString());

        StandardApiResponse standardApiResponse = new StandardApiResponse(
                true,
                HttpStatus.OK.value(),
                "Logged out successfully!",
                null,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(standardApiResponse);
    }

}
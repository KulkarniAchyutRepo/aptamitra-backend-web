package com.app.TwoFactorAuthentication.controller;
import jakarta.servlet.http.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class CsrfController {

    @GetMapping("/csrf-token")
    public ResponseEntity<?> getCsrfToken(HttpServletRequest request) {
//        public ResponseEntity<CsrfToken> getCsrfToken(HttpServletRequest request) {
//        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
//
//        ResponseCookie clearCookie = ResponseCookie.from("XSRF-TOKEN", "")
//                .httpOnly(false)
//                .secure(true)
//                .path("/")
//                .sameSite("None")
//                .maxAge(0) // Expire old token
//                .build();
//
//        ResponseCookie newCookie = ResponseCookie.from("XSRF-TOKEN", csrfToken.getToken())
//                .httpOnly(false)
//                .secure(true)
//                .path("/")
//                .sameSite("None")
//                .build();
    Map<String, String> map = new HashMap<>();
    map.put("token", "REPLACEMENT TOKEN");
    map.put("headerName", "X-XSRF-TOKEN");
        return ResponseEntity.ok()
//                .header(HttpHeaders.SET_COOKIE, clearCookie.toString()) // First clear old token
//                .header(HttpHeaders.SET_COOKIE, newCookie.toString())  // Then set the new token
//                .body(csrfToken);
                .body(map);
    }
}

package com.app.TwoFactorAuthentication.config;

import com.app.TwoFactorAuthentication.service.JwtService;
import com.app.TwoFactorAuthentication.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Component
public class JwtCookieAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;

    @Autowired
    public JwtCookieAuthenticationFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        // Bypass JWT authentication for CSRF token requests
        if (requestUri.equals("/api/v1/csrf-token")) {
            // ✅ First, clear old XSRF-TOKEN cookies before proceeding
            Cookie clearCsrfCookie = new Cookie("XSRF-TOKEN", "");
            clearCsrfCookie.setPath("/");
            clearCsrfCookie.setHttpOnly(false);
            clearCsrfCookie.setSecure(true);
            clearCsrfCookie.setMaxAge(0);  // Expire immediately
            response.addCookie(clearCsrfCookie);

            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token from cookies
        Optional<String> tokenOptional = getJwtFromCookies(request);

        if (tokenOptional.isPresent()) {
            System.out.println("The request has been rejected -1");
            String token = tokenOptional.get();

            // Validate token before processing
            if (jwtService.validateToken(token)) {
                if (isValidForEndpoint(token, requestUri)) {
                    authenticateUser(token);
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    System.out.println("The request has been rejected -2");
                    return;
                }
            }
        }
        System.out.println("The request has been rejected -3");

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT from cookies.
     */
    private Optional<String> getJwtFromCookies(HttpServletRequest request) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> "accessToken".equals(cookie.getName())) // Ensure you use the correct cookie name
                    .map(Cookie::getValue)
                    .findFirst();
        }
        return Optional.empty();
    }

    /**
     * Authenticates user and sets authentication in the security context.
     */
    private void authenticateUser(String token) {
        String userName = jwtService.getUsernameFromToken(token);
        UserDetails userDetails = userService.loadUserByUsername(userName);

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
    }

    /**
     * Checks if the token is valid for the given endpoint.
     */
    private boolean isValidForEndpoint(String token, String requestUri) {
        if (jwtService.isResetPasswordToken(token)) {
            return "/api/v1/forgot-password-reset-password".equals(requestUri);
        }
        if (jwtService.isTemporaryToken(token)) {
            return "/api/v1/authenticator-validate-code".equals(requestUri);
        }
        return true; // Default: regular access token allows access
    }

}

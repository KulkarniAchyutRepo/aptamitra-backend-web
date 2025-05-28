package com.app.TwoFactorAuthentication.config;

import org.apache.catalina.filters.CorsFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;


import java.util.List;

@Configuration
public class SecurityConfig {

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter;

    @Autowired
    public SecurityConfig(JwtCookieAuthenticationFilter jwtCookieAuthenticationFilter, JwtAuthenticationFilter jwtAuthenticationFilter, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.jwtCookieAuthenticationFilter=jwtCookieAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        httpSecurity
                .csrf(csrf -> csrf.disable()) // Disable CSRF if using stateless auth (or configure properly)
//                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())) // CSRF protection enabled
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(
                        auth-> auth
                                .requestMatchers(HttpMethod.GET, "/api/v1/csrf-token").permitAll()
                                .requestMatchers(HttpMethod.POST, "api/v1/logout").permitAll()
                                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // Allow CORS Preflight
                                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                                    .requestMatchers(HttpMethod.POST, "/api/v1/authenticator-generate-code").authenticated()
                                    .requestMatchers(HttpMethod.POST, "/api/v1/authenticator-validate-code").authenticated()
                                    .requestMatchers(HttpMethod.POST, "/api/v1/forgot-password-reset-password").authenticated()
                                    .requestMatchers(HttpMethod.POST, "/api/v1/register").permitAll()
                                    .requestMatchers(HttpMethod.POST, "/api/v1/forgot-password-verify-email-otp").permitAll()
                                    .requestMatchers(HttpMethod.POST, "/api/v1/login-email-otp").permitAll()
                                    .requestMatchers(HttpMethod.POST, "/api/v1/verify-email-otp").permitAll()
                                    .requestMatchers(HttpMethod.POST, "/api/v1/login-password").permitAll()
                                    .requestMatchers("/api/v1/users").permitAll()
                                    .anyRequest().permitAll()
                )
                .exceptionHandling(exception->exception.authenticationEntryPoint(customAuthenticationEntryPoint))
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtCookieAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("https://apt-sage.vercel.app", "http://localhost:5173", "https://aapthamithra.com")); // Update with your frontend URL
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//        config.setAllowedHeaders(List.of("*"));
        config.setAllowedHeaders(List.of("Origin", "Content-Type", "Accept", "X-XSRF-TOKEN", "Authorization")); // Add any other custom headers you use
        config.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", config);

        return (CorsConfigurationSource) source; // Explicitly cast to CorsConfigurationSource
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}

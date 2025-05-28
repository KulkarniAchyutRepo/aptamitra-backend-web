package com.app.TwoFactorAuthentication.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {
    private static final String JWT_SECRET_KEY = "Kx92X#nB!!2jLxoPEgQs67FA8cPmA@vXJpT1FtGs9nI2";

    private static final long TEMP_TOKEN_EXPIRATION = 10*60*1000; // 10  minute
    private static final long RESET_PASSWORD_TOKEN_EXPIRATION = 5*60*1000; // 5  minute
    private static final long ACCESS_TOKEN_EXPIRATION = 10*60*1000; // 10 minute
//    private static final long REFRESH_TOKEN_EXPIRATION = 5*60*1000; // 5  minutes
    private static final long REFRESH_TOKEN_EXPIRATION = 24*60*60*1000; // 24 hrs

    //generate a new token
    public String generateToken(String username, Boolean isAccessToken){
        long expirationTime = isAccessToken?ACCESS_TOKEN_EXPIRATION:REFRESH_TOKEN_EXPIRATION;
        String tokenType = isAccessToken?"accessToken":"refreshToken";
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+expirationTime))
                .claim("tokenType", tokenType)
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTemporaryToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TEMP_TOKEN_EXPIRATION))
                .claim("tokenType", "temporaryToken")
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateResetPasswordToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + RESET_PASSWORD_TOKEN_EXPIRATION))
                .claim("tokenType", "resetPasswordToken")
                .signWith(Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    //get username from token
    public String getUsernameFromToken(String token){
        return Jwts.parser()
                .setSigningKey(JWT_SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    //validate token
    public boolean validateToken(String token){
        try{
            Jwts.parser()
                    .setSigningKey(JWT_SECRET_KEY.getBytes())
                    .build()
                    .parseClaimsJws(token);

            return true;
        }catch (Exception e){
            return false;
        }
    }


    public boolean isTemporaryToken(String token) {
        Claims claims = extractClaims(token);
        return "temp".equals(claims.get("tokenType"));
    }

    public boolean isResetPasswordToken(String token) {
        Claims claims = extractClaims(token);
        return "resetPasswordToken".equals(claims.get("tokenType"));
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .setSigningKey(JWT_SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Optional<String> getJwtFromCookies(HttpServletRequest request, String cookieName) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookieName.equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst();
        }
        return Optional.empty();
    }

}

package com.shishir.socialmedia.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationTime;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, String userId, String role) {
        try {
            Date now = new Date();
            Date expiry = new Date(now.getTime() + expirationTime);
            return Jwts.builder()
                    .subject(email)
                    .claim("userId", userId)
                    .claim("role", role)
                    .claim("email", email)
                    .issuedAt(now)
                    .expiration(expiry)
                    .signWith(signingKey)
                    .compact();
        } catch (JwtException e) {
            log.error("Error generating JWT token", e);
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public String getUserIdFromToken(String token) {
        return parseClaims(token).get("userId", String.class);
    }

    public String getRoleFromToken(String token) {
        return parseClaims(token).get("role", String.class);
    }

    public long getExpirationTimeFromToken(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return (expiration.getTime() - System.currentTimeMillis()) / 1000;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("Error parsing JWT token", e);
            throw new JwtException("Invalid token", e);
        }
    }
}

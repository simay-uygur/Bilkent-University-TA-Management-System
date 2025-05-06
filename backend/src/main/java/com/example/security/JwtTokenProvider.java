package com.example.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expirationMs}")
    private long jwtExpirationMs;

    private Key signingKey;

    /**
     * Initialize the signing key from the raw secret bytes (UTF-8).
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        // For HS512, ensure jwtSecret is at least 64 bytes
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate a JWT token using the pre-built signing key.
     */
    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();

        // 2) Build the JWT with the user’s ID as the subject
        Date now = new Date();
        return Jwts.builder()
            .setSubject(String.valueOf(userPrincipal.getId()))             // <-- ID, not webmail
            .claim("role", userPrincipal.getAuthorities().iterator().next().getAuthority())
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime() + jwtExpirationMs))
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact();
    }

    /**
     * Extract username from JWT using the same signing key.
     */
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder()
                   .setSigningKey(signingKey)
                   .build()
                   .parseClaimsJws(token)
                   .getBody()
                   .getSubject();
    }

    /**
     * Validate the JWT token signature and expiration.
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            // You can log specific validation errors here if desired
        }
        return false;
    }
}

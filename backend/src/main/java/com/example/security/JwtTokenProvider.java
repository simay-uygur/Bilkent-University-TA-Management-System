package com.example.security;

import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtTokenProvider {
    private final String jwtSecret = System.getProperty("JWT_SECRET");
    private final int jwtExpirationMs = Integer.parseInt(System.getProperty("JWT_EXPIRATION_MS"));

    public String generateJwtToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        return Jwts.builder()
            .setSubject(userPrincipal.getUsername())
            .claim("role", userPrincipal.getAuthorities().iterator().next().getAuthority())
            .setIssuedAt(new Date())
            .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()
                  .setSigningKey(jwtSecret)
                  .parseClaimsJws(token)
                  .getBody()
                  .getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (JwtException e) {
            // log token errors here if desired
        }
        return false;
    }
}

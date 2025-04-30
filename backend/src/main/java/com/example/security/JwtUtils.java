package com.example.security;

import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtils {
    private final String jwtSecret = System.getProperty("JWT_SECRET");
    private final int jwtExpirationMs = Integer.parseInt(System.getProperty("JWT_EXPIRATION_MS"));
  

        public String generateJwtToken(UserDetails userDetails) {
            return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .claim("roles", userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
        }
        
        public String getUserNameFromJwt(String token) {
            return Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
        }
        
        public boolean validateJwtToken(String token) {
            try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
            } catch (JwtException | IllegalArgumentException e) {
            // log invalid token…
            }
            return false;
        }
}

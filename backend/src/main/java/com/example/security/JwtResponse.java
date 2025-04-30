package com.example.security;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String webmail;
    private String role;

    public JwtResponse(String token, Long id, String webmail, String role) {
        this.token = token;
        this.id = id;
        this.webmail = webmail;
        this.role = role;
    }
    // getters only
}


package com.example.security;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String userName;
    private String webmail;
    private String role;
    private String currentSemester;

    public JwtResponse(String token, Long id, String webmail,String name, String role, String currentSemester) {
        this.token = token;
        this.id = id;
        this.userName = name;
        this.webmail = webmail;
        this.role = role;
        this.currentSemester = currentSemester;
    }
    // getters only
}


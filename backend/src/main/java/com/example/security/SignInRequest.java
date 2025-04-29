package com.example.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInRequest {
    private Long id;
    private String password;

    public Long getId() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

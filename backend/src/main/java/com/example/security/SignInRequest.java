package com.example.security;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignInRequest {
    private Long id;
    private String password;
}

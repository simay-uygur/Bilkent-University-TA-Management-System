package com.example.security;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class SignInRequest {
    private Long id;
    private String password;

    
}

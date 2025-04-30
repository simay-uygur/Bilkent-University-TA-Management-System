package com.example.security;

import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.entity.Actors.User;
import com.example.service.UserServ;

import lombok.AllArgsConstructor;

@Component
@Primary
@AllArgsConstructor
public class UserAuth implements AuthenticationManager {
    private final UserServ serv;
    private final BCryptPasswordEncoder enc;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Long userId = Long.parseLong(authentication.getName());
        User u = serv.getUserById(userId);
        if (u == null) {
            throw new UsernameNotFoundException("User not found: " + userId);
        }
        if (!enc.matches(authentication.getCredentials().toString(), u.getPassword())) {
            throw new BadCredentialsException("Password is incorrect!");
        }

        // Build your UserDetailsImpl
        UserDetailsImpl userDetails = UserDetailsImpl.build(u);
        // Return it as the principal, with its authorities
        return new UsernamePasswordAuthenticationToken(
            userDetails,                // ← principal is now UserDetailsImpl
            null,                       // credentials (we don’t need to keep the raw password)
            userDetails.getAuthorities()
        );
    }
}

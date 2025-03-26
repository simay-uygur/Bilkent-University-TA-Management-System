package com.example.demo1.security;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo1.entity.User;
import com.example.demo1.exception.UserNotFoundExc;
import com.example.demo1.service.UserServ;

import lombok.AllArgsConstructor;

@Component
@Primary
@AllArgsConstructor
public class UserAuth implements AuthenticationManager{

    @Autowired
    UserServ serv ;
    @Autowired
    BCryptPasswordEncoder enc ;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Long user_id = Long.parseLong(authentication.getName()) ;

        User u = serv.getUserById(user_id) ;

        if (u == null)
            throw new UserNotFoundExc(user_id) ;
        else if (!enc.matches(authentication.getCredentials().toString(),u.getPassword()))
            throw new RuntimeException("Password is incorrect!") ;

        return new UsernamePasswordAuthenticationToken(user_id, u.getPassword(), new ArrayList<>());
    }
    
}

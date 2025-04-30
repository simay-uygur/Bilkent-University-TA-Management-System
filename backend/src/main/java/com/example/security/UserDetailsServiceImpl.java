package com.example.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.entity.Actors.User;
import com.example.repo.UserRepo;

import jakarta.transaction.Transactional;

public class UserDetailsServiceImpl implements UserDetailsService{
    @Autowired
    private UserRepo userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String id)
            throws UsernameNotFoundException {
        User user = userRepository.findById(Long.valueOf(id))
            .orElseThrow(() ->
                new UsernameNotFoundException(id + "User not found with id: ")
            );
        return UserDetailsImpl.build(user);
    }
}

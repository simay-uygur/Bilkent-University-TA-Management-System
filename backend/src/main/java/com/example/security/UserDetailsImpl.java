package com.example.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.entity.Actors.User;


public class UserDetailsImpl implements UserDetails {
    private Long id;
    private String userName;
    private String webmail;
    private String password;
    private boolean enabled;  
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String webmail, String password, String userName,
                           Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.webmail = webmail;
        this.userName = userName;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        SimpleGrantedAuthority authority =
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        return new UserDetailsImpl(
            user.getId(),
            user.getWebmail(),
            user.getPassword(),
            user.getName() + " " + user.getSurname(),
            Collections.singletonList(authority)
        );
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    @Override public String getPassword() { return password; }
    @Override public String getUsername() { return webmail; }
    public String getName() { return userName; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return enabled; }

    public Long getId() {
        return id;
    }
}


package com.example.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthTokenFilter    jwtAuthTokenFilter;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
          .userDetailsService(userDetailsService)
          .passwordEncoder(passwordEncoder);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .csrf (csrf -> csrf.disable())
            .authorizeHttpRequests( req -> req
            /*.requestMatchers("/api/ta/{id}").hasRole("TA")
            .requestMatchers("/api/admin/{id}").hasRole("ADMIN")
            .requestMatchers("/api/staff/{id}").hasRole("DEPARTMENT_STAFF")
            .requestMatchers("/api/chair/{id}").hasRole("DEPARTMENT_CHAIR")
            .requestMatchers("/api/office/{id}").hasRole("DEANS_OFFICE")
            .requestMatchers("/api/fac_mem/{id}").hasRole("FACULTY_MEMBER")
            .requestMatchers("/api/uploadTAs/").hasRole("ADMIN")
            .requestMatchers("/api/uploadTAs/").hasRole("DEANS_OFFICE")
            .requestMatchers(HttpMethod.POST, "/api/student").permitAll() // ADDed
            .requestMatchers("/api/**").permitAll()
                .anyRequest().authenticated()*/
            .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/**").permitAll()
            .requestMatchers(HttpMethod.PUT, "/api/**").permitAll()
            .requestMatchers(HttpMethod.DELETE, "/api/**").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.addFilterBefore(jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build() ;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authBuilder =
            http.getSharedObject(AuthenticationManagerBuilder.class);

        // Tell Spring to use your UserDetailsServiceImpl + the PasswordEncoder
        authBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder);

        return authBuilder.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(userDetailsService);
        p.setPasswordEncoder(passwordEncoder);
        return p;
    }
}

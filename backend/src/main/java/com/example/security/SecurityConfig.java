package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
                .requestMatchers("/signIn", "/signUp", "/**").permitAll()
                .anyRequest().authenticated()
            .and()
            .formLogin()
            .and()
            .httpBasic();
        return http.build();
    } */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf (csrf -> csrf.disable())
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
            .requestMatchers(HttpMethod.POST, "/api/mail/**").permitAll()  // ← BURASI KRİTİK
            .requestMatchers(HttpMethod.GET, "/api/mail/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/**").permitAll()
            .requestMatchers(HttpMethod.PUT, "/api/**").permitAll()
            .requestMatchers(HttpMethod.DELETE, "/api/**").permitAll()
            .anyRequest().authenticated()
        );
        return http.build() ;
    }

    @Bean
    public AuthenticationManager authenticationManager(UserAuth userAuth) {
        return userAuth;
    }

}

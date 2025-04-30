package com.example.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthTokenFilter jwtAuthTokenFilter;

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
            .requestMatchers(HttpMethod.GET, "/api/**").permitAll()
            .requestMatchers(HttpMethod.POST, "/api/**").permitAll()
            .requestMatchers(HttpMethod.PUT, "/api/**").permitAll()
            .requestMatchers(HttpMethod.DELETE, "/api/**").permitAll()
            .anyRequest().authenticated()
        );
        return http.build() ;
    }

    // Expose AuthenticationManager for AuthController to use
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig
    ) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Use a DaoAuthenticationProvider wired with your UserDetailsService + PasswordEncoder
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}

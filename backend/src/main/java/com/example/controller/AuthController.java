package com.example.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Actors.User;
import com.example.exception.IncorrectWebMailException;
import com.example.exception.UserExistsExc;
import com.example.exception.UserNotFoundExc;
import com.example.repo.UserRepo;
import com.example.security.JwtResponse;
import com.example.security.JwtTokenProvider;
import com.example.security.SignInRequest;
import com.example.security.UserDetailsImpl;
import com.example.security.UserDetailsServiceImpl;
import com.example.service.UserServ;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// better to use different controllers for each role, because the logic for each role is different
@RestController
@RequiredArgsConstructor
public class AuthController {
    
   
    private final UserServ serv;
    private final UserRepo userRepo;

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder; 
    
    @PostMapping("/api/signUp")
    public ResponseEntity<User> createUser(@RequestBody User u) 
    {
        //System.out.println("role: " + u.getRole() + "id: " + u.getId());
        User user_to_check = serv.getUserById(u.getId());
        if (serv.getUserById(u.getId()) != null)
            throw new UserExistsExc(u.getId()) ;
        String[] name = u.getName().split(" ");
        String name_to_check = name[name.length-1];

        String[] surname = u.getSurname().split(" ");
        String surname_to_check = surname[surname.length-1];
        String check_mail = name_to_check.toLowerCase() + 
                            "." + 
                            surname_to_check.toLowerCase() + 
                            "@ug.bilkent.edu.tr";
        if (!check_mail.matches(u.getWebmail().toLowerCase()) && !Objects.equals(user_to_check.getId(), u.getId()))
            throw new IncorrectWebMailException() ;
        return new ResponseEntity<>(serv.createUser(u), HttpStatus.CREATED) ;
        //return ResponseEntity.created(URI.create("/signIn/{id}")).body(serv.createUser(u)) ;
    }

    @PostMapping("/api/signIn")
    public ResponseEntity<?> signIn(@Valid @RequestBody SignInRequest request) {
        if(!userRepo.existsById(request.getId())){
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
        // 1) Load user by ID
        UserDetailsImpl user = (UserDetailsImpl) 
            userDetailsService.loadUserByUsername(request.getId().toString());
        // 2) Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid credentials"));
        }
        // 3) Build an Authentication object so SecurityContext is populated
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                user, 
                null, 
                user.getAuthorities()
            );
        SecurityContextHolder.getContext().setAuthentication(auth);
        // 4) Generate JWT
        String jwt = tokenProvider.generateJwtToken(auth);
        // 5) Return the token + user info
        JwtResponse body = new JwtResponse(
            jwt,
            user.getId(),
            user.getUsername(),
            user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("ROLE_USER")
        );
        return ResponseEntity.ok(body);
    }


    @GetMapping("/api/all")
    public ResponseEntity<List<User>> getUsers() {
        return new ResponseEntity<>(serv.getAllUsers(), HttpStatus.OK) ;
    }

    @DeleteMapping("/api/{id}")
    public ResponseEntity<HttpStatus> deleteStudent(@PathVariable Long id) {
        User u = serv.getUserById(id) ;
        if (u == null){
            throw new UserNotFoundExc(id) ;
        }
        serv.deleteUser(u);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
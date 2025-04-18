package com.example.controller;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import com.example.entity.Actors.User;
import com.example.exception.IncorrectWebMailException;
import com.example.exception.UserExistsExc;
import com.example.exception.UserNotFoundExc;
import com.example.security.SignInRequest;
import com.example.service.UserServ;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

// better to use different controllers for each role, because the logic for each role is different
@RestController
@RequiredArgsConstructor
public class log_controller {
    
    @Autowired
    UserServ serv ;


    private final AuthenticationManager authenticationManager;
    
    @PostMapping("/api/signUp")
    public ResponseEntity<User> createUser(@RequestBody User u) 
    {
        //System.out.println("role: " + u.getRole() + "id: " + u.getId());
        User user_to_check = serv.getUserById(u.getId());
        if (serv.getUserById(u.getId()) != null)
            throw new UserExistsExc(u.getId()) ;
        String check_mail = u.getName().toLowerCase() + 
                            "." + 
                            u.getSurname().toLowerCase() + 
                            "@ug.bilkent.edu.tr";
        if (!check_mail.matches(u.getWebmail().toLowerCase()) && !Objects.equals(user_to_check.getId(), u.getId()))
            throw new IncorrectWebMailException() ;
        return new ResponseEntity<>(serv.createUser(u), HttpStatus.CREATED) ;
        //return ResponseEntity.created(URI.create("/signIn/{id}")).body(serv.createUser(u)) ;
    }

    @PostMapping("/api/signIn")
    public ResponseEntity<?> auth(@RequestBody SignInRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getId(), request.getPassword())
            );
            User user = serv.getUserById(request.getId());
            return ResponseEntity.ok(user);
        } catch (AuthenticationException e) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        return null ;
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
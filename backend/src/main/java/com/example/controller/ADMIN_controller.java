package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.User;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ADMIN_controller {
    
    @GetMapping("/{id}")
    public void getMethodName(@RequestBody User u) {
        System.out.println("Accessed");
    }
    
}

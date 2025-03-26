package com.example.demo1.service;

import java.util.List;

import com.example.demo1.entity.User;


public interface UserServ {

    public List<User> getAllUsers();

    public User createUser(User u) ;
    
    public void deleteUser(User u) ;

    public User getUserById(Long id) ;

}

package com.example.service;

import java.util.List;

import com.example.entity.Actors.User;


public interface UserServ {

    public List<User> getAllUsers();

    public User createUser(User u) ;
    
    public void deleteUser(User u) ;

    public User getUserById(Long id) ;
    
    public User getUserByEmail(String email);


}

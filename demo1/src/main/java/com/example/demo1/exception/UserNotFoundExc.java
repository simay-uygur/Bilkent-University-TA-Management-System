package com.example.demo1.exception;

public class UserNotFoundExc extends RuntimeException{
    public UserNotFoundExc(Long id){
        super("User with " + id + " not found in the DB.") ;
    }
}

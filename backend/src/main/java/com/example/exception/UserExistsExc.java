package com.example.exception;

public class UserExistsExc extends RuntimeException{
    public UserExistsExc(Long id){
        super("User with " + id + " already exists.") ;
    }
}

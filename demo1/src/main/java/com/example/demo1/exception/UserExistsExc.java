package com.example.demo1.exception;

public class UserExistsExc extends RuntimeException{
    public UserExistsExc(Long id){
        super("User with " + id + " already exists.") ;
    }
}

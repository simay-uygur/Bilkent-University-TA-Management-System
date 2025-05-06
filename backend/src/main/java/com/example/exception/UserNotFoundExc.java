package com.example.exception;

public class UserNotFoundExc extends RuntimeException{
    public UserNotFoundExc(Long id){
        super("User with " + id + " not found in the DB.") ;
    }
    public UserNotFoundExc(String deptName){
        super("Users within " + deptName + "are not found in the DB.") ;
    }
}

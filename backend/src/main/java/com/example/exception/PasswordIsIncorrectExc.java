package com.example.exception;

public class PasswordIsIncorrectExc extends RuntimeException{
    public PasswordIsIncorrectExc(){
        super("The password is incorrect. Please check again!");
    }
}

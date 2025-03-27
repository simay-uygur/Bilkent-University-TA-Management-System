package com.example.exception;

public class IncorrectWebMailException extends RuntimeException{
    public IncorrectWebMailException(){
        super("Incorrect webmail input. Please check again!") ;
    }
}

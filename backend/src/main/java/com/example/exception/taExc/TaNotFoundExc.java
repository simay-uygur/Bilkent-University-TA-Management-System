package com.example.exception.taExc;

public class TaNotFoundExc extends RuntimeException{
    public TaNotFoundExc(Long id){
        super("User with " + id + " not found in the DB.") ;
    }
}

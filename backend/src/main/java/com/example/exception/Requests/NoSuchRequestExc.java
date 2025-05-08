package com.example.exception.Requests;

public class NoSuchRequestExc extends RuntimeException{
    public NoSuchRequestExc(Long id){
        super("No such request with id" + id + "was found");
    }
}

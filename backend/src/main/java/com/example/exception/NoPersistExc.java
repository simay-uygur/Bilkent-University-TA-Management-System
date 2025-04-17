package com.example.exception;

public class NoPersistExc extends IllegalStateException{
    public NoPersistExc(String msg){
        super(msg + " did not persist!") ;
    }
}

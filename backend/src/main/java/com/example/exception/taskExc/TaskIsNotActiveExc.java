package com.example.exception.taskExc;

public class TaskIsNotActiveExc extends IllegalStateException{
    public TaskIsNotActiveExc(){
        super("Task is not active and is not responded!") ;
    }
}

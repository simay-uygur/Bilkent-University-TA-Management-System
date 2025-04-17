package com.example.exception.taskExc;

public class TaskNoTasExc extends RuntimeException{
    public TaskNoTasExc(){
        super("Task has no TAs assigned.") ;
    }
}

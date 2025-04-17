package com.example.exception.taskExc;

public class TaskLimitExc extends IllegalStateException{
    public TaskLimitExc(){
        super("Task has reached its limits in amount of participants!");
    }
}

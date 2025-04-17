package com.example.exception.taskExc;

public class TaskNotFoundExc extends RuntimeException{
    public TaskNotFoundExc(int task_id)
    {
        super("Task with ID " + task_id + " not found.");
    }
}

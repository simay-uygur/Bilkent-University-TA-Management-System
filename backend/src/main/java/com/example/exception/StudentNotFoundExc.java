package com.example.exception;

public class StudentNotFoundExc extends RuntimeException {

    public StudentNotFoundExc(Long id) {
        super("Student not found with id: " + id);
    }
}
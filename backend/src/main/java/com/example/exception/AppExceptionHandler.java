package com.example.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.exception.Course.CourseNotFoundExc;
import com.example.exception.Course.NoPrereqCourseFound;
import com.example.exception.taskExc.TaskIsNotActiveExc;
import com.example.exception.taskExc.TaskLimitExc;
import com.example.exception.taskExc.TaskNoTasExc;
import com.example.exception.taskExc.TaskNotFoundExc;

@RestControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler(UserNotFoundExc.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundExc ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(UserExistsExc.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserExistsExc ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(PasswordIsIncorrectExc.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(PasswordIsIncorrectExc ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(IncorrectWebMailException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(IncorrectWebMailException ex) {
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(GeneralExc.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(GeneralExc ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoPersistExc.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(NoPersistExc ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    //TASK exceptions
    @ExceptionHandler(TaskIsNotActiveExc.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(TaskIsNotActiveExc ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TaskLimitExc.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(TaskLimitExc ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TaskNoTasExc.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(TaskNoTasExc ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(TaskNotFoundExc.class)
    public ResponseEntity<Map<String, String>> handleUserNotFoundException(TaskNotFoundExc ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(CourseNotFoundExc.class)
    public ResponseEntity<Map<String, String>> handleCourseNotFoundException(CourseNotFoundExc ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(NoPrereqCourseFound.class)
    public ResponseEntity<Map<String, String>> handleCourseNotFoundException(NoPrereqCourseFound ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}

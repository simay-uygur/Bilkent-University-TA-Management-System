package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;
@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @PostConstruct
    public void debug() {
        System.out.println("🔍 DB URL from env: " + System.getenv("SPRING_DATASOURCE_URL"));
        System.out.println("🔍 DB URL from Spring: " + System.getProperty("spring.datasource.url"));
    }
}


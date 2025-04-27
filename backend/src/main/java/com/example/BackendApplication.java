package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;
@EnableAsync
@EnableScheduling
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


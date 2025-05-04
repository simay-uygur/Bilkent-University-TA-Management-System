package com.example.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.AppLog;

public interface AppLogRepo extends JpaRepository<AppLog, Long> {
    // Özel sorgu gerekirse ekleyebilirsin
}

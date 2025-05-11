package com.example.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.example.entity.AppLog;
import com.example.repo.AppLogRepo;

import lombok.RequiredArgsConstructor;

/**
 * Servis katmanında tek satırla log atmak için:
 *   log.info("LeaveService","Leave 42 oluşturuldu");
 */
@Service
@RequiredArgsConstructor
public class LogService {

    private final AppLogRepo repo;

    public void info (String src, String msg) { save("INFO",  src, msg); }
    public void warn (String src, String msg) { save("WARN",  src, msg); }
    public void error(String src, String msg) { save("ERROR", src, msg); }

    @Async("setExecutor")
    private void save(String level, String src, String msg) {
        repo.save(AppLog.builder()
                        .level(level)
                        .source(src)
                        .message(msg)
                        .build());
    }
}

package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.repo.AppLogRepo;
import com.example.service.LogService;

import lombok.RequiredArgsConstructor;

@Component                // <‑‑ Spring açılırken otomatik çalışır
@RequiredArgsConstructor
public class LogDemoRunner implements CommandLineRunner {

    private final LogService log;   // hazır servis
    private final AppLogRepo repo;  // kayıt sayısını saymak için

    @Override
    public void run(String... args) {
        System.out.println("Önceki kayıt sayısı = " + repo.count());
        log.info("DemoRunner", "Merhaba log!");
        System.out.println("Sonraki kayıt sayısı = " + repo.count());
    }
}

package com.siac.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

@RestController
public class ServiceController {

    private final Instant startTime = Instant.now();

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getStatus() {
        Instant now = Instant.now();
        Duration uptime = Duration.between(startTime, now);

        StatusResponse status = new StatusResponse(
            "Service is active",
            uptime.toHours(),
            uptime.toMinutes() % 60,
            uptime.getSeconds() % 60
        );

        return ResponseEntity.ok(status);
    }

    static class StatusResponse {
        public final String message;
        public final long uptimeHours;
        public final long uptimeMinutes;
        public final long uptimeSeconds;

        public StatusResponse(String message, long uptimeHours, long uptimeMinutes, long uptimeSeconds) {
            this.message = message;
            this.uptimeHours = uptimeHours;
            this.uptimeMinutes = uptimeMinutes;
            this.uptimeSeconds = uptimeSeconds;
        }
    }
}

package com.siac.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import com.sun.management.OperatingSystemMXBean;

@RestController
public class ServiceController {

    private final Instant startTime = Instant.now();
    private final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getStatus() {
        Instant now = Instant.now();
        Duration uptime = Duration.between(startTime, now);
        double cpuLoad = osBean.getCpuLoad() * 100;  // Directly use the CPU load as a percentage
        double totalMemory = osBean.getTotalMemorySize() / (1024.0 * 1024.0 * 1024.0);  // Convert to GB
        double freeMemory = osBean.getFreeMemorySize() / (1024.0 * 1024.0 * 1024.0);  // Convert to GB

        StatusResponse status = new StatusResponse(
            "Service is active",
            uptime.toHours(),
            uptime.toMinutes() % 60,
            uptime.getSeconds() % 60,
            cpuLoad,
            totalMemory,
            freeMemory
        );

        return ResponseEntity.ok(status);
    }

    static class StatusResponse {
        public final String message;
        public final long uptimeHours;
        public final long uptimeMinutes;
        public final long uptimeSeconds;
        public final double cpuLoad;
        public final double totalMemory;
        public final double freeMemory;

        public StatusResponse(String message, long uptimeHours, long uptimeMinutes, long uptimeSeconds, double cpuLoad, double totalMemory, double freeMemory) {
            this.message = message;
            this.uptimeHours = uptimeHours;
            this.uptimeMinutes = uptimeMinutes;
            this.uptimeSeconds = uptimeSeconds;
            this.cpuLoad = cpuLoad;
            this.totalMemory = totalMemory;
            this.freeMemory = freeMemory;
        }
    }
}

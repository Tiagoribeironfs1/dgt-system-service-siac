package com.siac.controller;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ServiceController {

    private final Instant startTime = Instant.now();

    @GetMapping("/status")
    public ResponseEntity<StatusResponse> getStatus() {
        Instant now = Instant.now();
        Duration uptime = Duration.between(startTime, now);
        long hours = uptime.toHours();
        long minutes = uptime.toMinutes() % 60;
        long seconds = uptime.getSeconds() % 60;

        // Formatar uptime como "2h:42m:36s"
        String formattedUptime = String.format("%dh:%dm:%ds", hours, minutes, seconds);


        // Criar a resposta com a assinatura do desenvolvedor
        StatusResponse status = new StatusResponse(
            "API Siac is running!",
            "1.0",  // Versão da API
            formattedUptime,
            new DeveloperSignature(
                "Tiago Ribeiro",
                "https://github.com/tiagoribeironfs1",
                "Digital Tracer - DGT",
                "https://github.com/digitaltracer1"
            )
        );

        return ResponseEntity.ok(status);
    }

    // Classe da resposta de status
    static class StatusResponse {
        public final String status;
        public final String version;
        public final String uptime;
        public final DeveloperSignature devSignature;

        public StatusResponse(String status, String version, String uptime, DeveloperSignature devSignature) {
            this.status = status;
            this.version = version;
            this.uptime = uptime;
            this.devSignature = devSignature;
        }
    }

    // Classe para a assinatura do desenvolvedor
    static class DeveloperSignature {
        public final String developerName;
        public final String developerGit;
        public final String organizationName;
        public final String organizationGit;

        public DeveloperSignature(String developerName, String developerGit, String organizationName, String organizationGit) {
            this.developerName = developerName;
            this.developerGit = developerGit;
            this.organizationName = organizationName;
            this.organizationGit = organizationGit;
        }
    }
}

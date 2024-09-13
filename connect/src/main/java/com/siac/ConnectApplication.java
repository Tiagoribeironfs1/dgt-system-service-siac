package com.siac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
public class ConnectApplication {

    private static final Logger log = LoggerFactory.getLogger(ConnectApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ConnectApplication.class, args);
        log.info("Service connect start localhost:8081");
    }

}


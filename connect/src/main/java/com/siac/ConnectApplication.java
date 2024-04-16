package com.siac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import com.siac.service.ConnectSiacService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class ConnectApplication {

    private static final Logger log = LoggerFactory.getLogger(ConnectApplication.class);

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(ConnectApplication.class, args);
        ConnectSiacService connectSiacService = context.getBean(ConnectSiacService.class);
        connectSiacService.realizarOperacoesBancoDados();
        log.info("Service connect start localhost:8081");
    }

}


package com.siac;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import com.siac.service.MyService;

@SpringBootApplication
public class ConnectApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(ConnectApplication.class, args);
		
		MyService myService = context.getBean(MyService.class);
		myService.realizarOperacoesBancoDados();
		System.out.println("Service connect start localhost:8081");
	}
	
}


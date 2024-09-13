package com.siac.config;

import java.sql.Connection;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DataSourceConfig {
    private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);

    @Value("${database.server.ip}")
    private String serverIp;

    @Value("${database.server.dbPath}")
    private String serverDbPath;

    @Value("${database.server.port}") 
    private String serverPort;

    @Value("${database.username}")
    private String username;

    @Value("${database.password}")
    private String password;

    @Value("${database.driverClassName}")
    private String driverClassName;

    @Value("${database.reconnectDelay}")
    private long reconnectDelay;  // Tempo de espera entre tentativas de reconexão, em milissegundos

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(buildDatabaseUrl());
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        boolean connected = false;
        while (!connected) {
            try (Connection conn = dataSource.getConnection()) {
                log.info("Conexão com o banco de dados estabelecida com sucesso.");
                connected = true; // Conexão bem-sucedida, saindo do laço
            } catch (Exception e) {
                log.error("Falha ao conectar ao banco de dados. Tentando novamente em {} milissegundos...", reconnectDelay, e);
                try {
                    Thread.sleep(reconnectDelay);  // Espera antes de tentar novamente
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();  // Manter o status de interrupção
                    log.error("Thread interrompida durante o sleep.", ie);
                }
            }
        }

        return dataSource;
    }

    private String buildDatabaseUrl() {
        String dbPathFull = String.format("//%s%s", serverIp, serverDbPath);
        return String.format("jdbc:extendedsystems:advantage://%s:%s;catalog=%s;TableType=cdx;", serverIp, serverPort, dbPathFull);
    }
}

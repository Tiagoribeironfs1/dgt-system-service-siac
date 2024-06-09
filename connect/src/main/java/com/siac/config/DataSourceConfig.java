package com.siac.config;

import javax.sql.DataSource;
import java.sql.Connection;

import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.slf4j.Logger;


@Configuration
public class DataSourceConfig {
     private static final Logger log = LoggerFactory.getLogger(DataSourceConfig.class);
    
    @Bean
    public DataSource dataSource() {
        
        String serverIp = "192.168.1.4";
        String serverDbPath =  serverIp + "/hdServer/Sistemas/Siac;";

        // String serverIp = "10.1.1.5";
        // String serverDbPath =  serverIp + "/hdCServer/Sistemas/Siac;";
        
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.extendedsystems.jdbc.advantage.ADSDriver");
        // Utilizando a variável serverIp na URL de conexão
        dataSource.setUrl("jdbc:extendedsystems:advantage://" + serverIp + ":6262;catalog=//" + serverDbPath + "TableType=cdx;");
        dataSource.setPassword("");
        dataSource.setUsername("");

        try (Connection conn = dataSource.getConnection()) {
            log.info("Conexão com o banco de dados estabelecida com sucesso.");
        } catch (Exception e) {
            log.error("Falha ao conectar ao banco de dados: " + e.getMessage());
        }
        
        return dataSource;
    }
}

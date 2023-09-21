package com.siac.config;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class DataSourceConfig {
    
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.extendedsystems.jdbc.advantage.ADSDriver");
        dataSource.setUrl("jdbc:extendedsystems:advantage://10.1.1.5:6262;catalog=//10.1.1.5/hdCserver/Sistemas/Siac;TableType=cdx;");
        dataSource.setPassword("");
        dataSource.setUsername("");
        
        return dataSource;
    }
}

package com.common.auth.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties("spring.datasource.write")
public class WriteDataSourceProperties {
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName = "org.postgresql.Driver";
    private HikariProperties hikari = new HikariProperties();

    @Data
    public static class HikariProperties {
        private boolean autoCommit = false;
        private int maximumPoolSize = 10;
        private int minimumIdle = 5;
        private long connectionTimeout = 20000;
        private long idleTimeout = 300000;
        private long maxLifetime = 1200000;
    }
}
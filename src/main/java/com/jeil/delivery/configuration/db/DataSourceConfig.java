package com.jeil.delivery.configuration.db;

import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties
@Slf4j
public class DataSourceConfig {
	public static final String OPERATION_DATASOURCE = "operationDs";
//	public static final String COMMON_DATASOURCE = "commonDs";

    private final Environment env;

    public DataSourceConfig(Environment env) {
        this.env = env;
    }

    @Value("${ssh.host}")
    private String sshHost;

    @Value("${ssh.port}")
    private int sshPort;

    @Value("${ssh.username}")
    private String sshUsername;

    @Value("${ssh.password}")
    private String sshPassword;

    @Value("${database.host}")
    private String databaseHost;

    @Value("${database.port}")
    private int databasePort;

    private Session sshSession;

	@Primary
	@Bean(name = OPERATION_DATASOURCE)
	@ConfigurationProperties(prefix = "spring.datasource.hikari")
	public DataSource operationDs() throws JSchException {
        String[] profiles = env.getActiveProfiles();
        boolean isProd = Arrays.asList(profiles).contains("prod");
        log.info("isProd=>{}", isProd);
        //프로퍼티별 ssh분기 (prod일떄만)
        if(isProd) {
            JSch jsch = new JSch();
            sshSession = jsch.getSession(sshUsername, sshHost, sshPort);
            sshSession.setPassword(sshPassword);
            sshSession.setConfig("StrictHostKeyChecking", "no");
            sshSession.connect();
            sshSession.setPortForwardingL(15432, databaseHost, databasePort);
        }
		return new HikariDataSource();
	}

	@PreDestroy
	public void closeSshSession() {
	    if (sshSession != null && sshSession.isConnected()) {
	        sshSession.disconnect();
	    }
	}

//	@Bean(name = COMMON_DATASOURCE)
//	@ConfigurationProperties(prefix = "spring.datasource.hikari.common")
//	public DataSource commonDs() {
//		return new HikariDataSource();
//	}
}
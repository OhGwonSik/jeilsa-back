package com.common.auth.common.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.common.auth.common.datasource.DynamicDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Configuration
@EnableAspectJAutoProxy
@EnableConfigurationProperties({WriteDataSourceProperties.class, ReadDataSourceProperties.class})
@MapperScan(basePackages = "com.common.auth")
public class DatabaseConfig {
    //----- Fields -----//
    @Value("${DB_SCHEMA:auth}")
    private String dbSchema;
    
    private final WriteDataSourceProperties writeDataSourceProperties;
    private final ReadDataSourceProperties readDataSourceProperties;
    
    // Write DataSource
    @Bean(name = "writeDataSource")
    public DataSource writeDataSource() {
        log.info("Creating writeDataSource...");
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(writeDataSourceProperties.getJdbcUrl());
        config.setUsername(writeDataSourceProperties.getUsername());
        config.setPassword(writeDataSourceProperties.getPassword());
        config.setDriverClassName(writeDataSourceProperties.getDriverClassName());
        config.setAutoCommit(writeDataSourceProperties.getHikari().isAutoCommit());
        config.setMaximumPoolSize(writeDataSourceProperties.getHikari().getMaximumPoolSize());
        config.setMinimumIdle(writeDataSourceProperties.getHikari().getMinimumIdle());
        config.setConnectionTimeout(writeDataSourceProperties.getHikari().getConnectionTimeout());
        config.setIdleTimeout(writeDataSourceProperties.getHikari().getIdleTimeout());
        config.setMaxLifetime(writeDataSourceProperties.getHikari().getMaxLifetime());
        
        HikariDataSource dataSource = new HikariDataSource(config);
        
        log.info("WriteDataSource - AutoCommit: {}, MaxPoolSize: {}, MinIdle: {}",
                dataSource.isAutoCommit(), dataSource.getMaximumPoolSize(), dataSource.getMinimumIdle());
        return dataSource;
    }

    // Read DataSource
    @Bean(name = "readDataSource")
    public DataSource readDataSource() {
        log.info("Creating readDataSource...");
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(readDataSourceProperties.getJdbcUrl());
        config.setUsername(readDataSourceProperties.getUsername());
        config.setPassword(readDataSourceProperties.getPassword());
        config.setDriverClassName(readDataSourceProperties.getDriverClassName());
        config.setAutoCommit(readDataSourceProperties.getHikari().isAutoCommit());
        config.setMaximumPoolSize(readDataSourceProperties.getHikari().getMaximumPoolSize());
        config.setMinimumIdle(readDataSourceProperties.getHikari().getMinimumIdle());
        config.setConnectionTimeout(readDataSourceProperties.getHikari().getConnectionTimeout());
        config.setIdleTimeout(readDataSourceProperties.getHikari().getIdleTimeout());
        config.setMaxLifetime(readDataSourceProperties.getHikari().getMaxLifetime());
        
        HikariDataSource dataSource = new HikariDataSource(config);
        
        log.info("ReadDataSource - AutoCommit: {}, MaxPoolSize: {}, MinIdle: {}",
                dataSource.isAutoCommit(), dataSource.getMaximumPoolSize(), dataSource.getMinimumIdle());
        return dataSource;
    }

    // Dynamic DataSource
    @Bean(name = "dynamicDataSource")
    @Primary
    public DataSource dynamicDataSource(@Qualifier("writeDataSource") DataSource writeDataSource,
                                       @Qualifier("readDataSource") DataSource readDataSource) {
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        
        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("auth.write", writeDataSource);
        dataSourceMap.put("auth.read", readDataSource);
        
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        dynamicDataSource.setDefaultTargetDataSource(writeDataSource);
        
        return dynamicDataSource;
    }

    // SqlSessionFactory
    @Bean(name = "sqlSessionFactory")
    @Primary
    public SqlSessionFactory sqlSessionFactory(@Qualifier("dynamicDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        sessionFactory.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath:mapper/**/*.xml")
        );
        
        sessionFactory.setTypeAliasesPackage("com.common.auth.domain");
        
        // MyBatis Variables 설정 (스키마명 포함)
        Properties variables = new Properties();
        variables.setProperty("schema", dbSchema);
        sessionFactory.setConfigurationProperties(variables);
        
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setDefaultFetchSize(100);
        configuration.setDefaultStatementTimeout(30);
        sessionFactory.setConfiguration(configuration);
        
        return sessionFactory.getObject();
    }

    // SqlSessionTemplate
    @Bean(name = "sqlSessionTemplate")
    @Primary
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    // Write Transaction Manager
    @Bean(name = "writeTransactionManager")
    @Primary
    public PlatformTransactionManager writeTransactionManager(@Qualifier("writeDataSource") DataSource writeDataSource) {
        return new DataSourceTransactionManager(writeDataSource);
    }

    // Read Transaction Manager
    @Bean(name = "readTransactionManager")
    public PlatformTransactionManager readTransactionManager(@Qualifier("readDataSource") DataSource readDataSource) {
        return new DataSourceTransactionManager(readDataSource);
    }
}
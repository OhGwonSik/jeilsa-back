package com.common.auth.common.config;

import java.net.InetAddress;


import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class MyBatisConfig {
    private final SqlSessionFactory sqlSessionFactory;
    
    @PostConstruct
    public void configureTypeHandlers() {
        TypeHandlerRegistry typeHandlerRegistry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
        typeHandlerRegistry.register(InetAddress.class, InetAddressTypeHandler.class);
    }
}
package com.common.auth.common.config;

import java.util.Properties;

import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.github.pagehelper.PageInterceptor;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Configuration
@Order(900)
@RequiredArgsConstructor
public class PageHelperConfig {
    private final SqlSessionFactory sqlSessionFactory;

    @PostConstruct
    public void addPageInterceptor() {
        PageInterceptor interceptor = new PageInterceptor();
        Properties properties = new Properties();
        properties.setProperty("helperDialect", "postgresql");
        properties.setProperty("reasonable", "true");
        properties.setProperty("supportMethodsArguments", "true");
        properties.setProperty("params", "count=countSql");
        interceptor.setProperties(properties);
        
        sqlSessionFactory.getConfiguration().addInterceptor(interceptor);
    }
}

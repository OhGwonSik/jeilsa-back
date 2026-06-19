package com.common.auth.common.config;

import org.apache.ibatis.session.SqlSessionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.common.auth.audit.component.AuditTargetCache;
import com.common.auth.audit.service.AuditLogService;
import com.common.auth.common.aop.AuditSqlTableResolver;
import com.common.auth.common.interceptor.AuditMyBatisInterceptor;

import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
@Order(1000)
public class AuditMyBatisInterceptorConfig {

    private final SqlSessionFactory sqlSessionFactory;
    private final AuditTargetCache auditTargetCacheMain;
    private final AuditSqlTableResolver sqlTableResolver;
    private final AuditLogService auditLogService;

    public AuditMyBatisInterceptorConfig(SqlSessionFactory sqlSessionFactory, AuditTargetCache auditTargetCacheMain,
                                          AuditSqlTableResolver sqlTableResolver, AuditLogService auditLogService) {
        this.sqlSessionFactory = sqlSessionFactory;
        this.auditTargetCacheMain = auditTargetCacheMain;
        this.sqlTableResolver = sqlTableResolver;
        this.auditLogService = auditLogService;
    }

    @PostConstruct
    public void addAuditInterceptor() {
        AuditMyBatisInterceptor interceptor = new AuditMyBatisInterceptor(
                auditLogService,
                auditTargetCacheMain,
                sqlTableResolver
        );
        try {
            org.apache.ibatis.session.Configuration myBatisConfig = sqlSessionFactory.getConfiguration();
            myBatisConfig.addInterceptor(interceptor);
        } catch (Exception exception) {
            log.error("AuditMyBatisInterceptor 등록 실패", exception);
        }
    }
}



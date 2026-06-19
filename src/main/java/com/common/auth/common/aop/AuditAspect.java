package com.common.auth.common.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.common.auth.audit.component.AuditTargetCache;
import com.common.auth.audit.service.AuditLogService;


@Aspect
@Component
@Order(100)
public class AuditAspect {

    private final ObjectProvider<AuditLogService> auditLogServiceProvider;
    private final ObjectProvider<AuditTargetCache> auditTargetCacheProvider;
    private final PlatformTransactionManager writeTxManager;

    public AuditAspect(ObjectProvider<AuditLogService> auditLogServiceProvider,
                      ObjectProvider<AuditTargetCache> auditTargetCacheProvider,
                      @Qualifier("writeTransactionManager") PlatformTransactionManager writeTxManager) {
        this.auditLogServiceProvider = auditLogServiceProvider;
        this.auditTargetCacheProvider = auditTargetCacheProvider;
        this.writeTxManager = writeTxManager;
    }

    @Before("execution(* com.common.auth..service..*(..))")
    public void registerAfterCompletion() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }

        AuditLogService auditLogService = auditLogServiceProvider.getIfAvailable();
        AuditTargetCache auditTargetCache = auditTargetCacheProvider.getIfAvailable();
        if (auditLogService == null || auditTargetCache == null) {
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(
                new AuditTransactionSynchronization(
                        auditLogService,
                        auditTargetCache,
                        writeTxManager
                )
        );
    }
}



package com.common.auth.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.common.auth.common.constant.MethodPatterns;
import com.common.auth.common.datasource.DataSourceContextHolder;

import lombok.extern.slf4j.Slf4j;

/**
 * 데이터소스 라우팅을 위한 AOP Aspect
 * 메서드 패턴에 따라 자동으로 데이터소스를 선택
 */
@Slf4j
@Aspect
@Component
@Order(0) // 트랜잭션보다 먼저 실행되어야 함
public class DataSourceRoutingAspect {
        
    /**
     * 읽기 전용 메서드들을 읽기 데이터소스로 라우팅
     */
    @Around("(" + MethodPatterns.READ_METHODS + ")")
    public Object routeToReadDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        String previousDataSource = DataSourceContextHolder.getDataSource();
        
        try {
            DataSourceContextHolder.setDataSource("auth.read");
            log.debug("Routing to READ datasource for method: {}", joinPoint.getSignature().getName());

            if (TransactionSynchronizationManager.isCurrentTransactionReadOnly() && TransactionSynchronizationManager.isActualTransactionActive()) {
                log.debug("읽기 전용 트랜잭션에서 쓰기 작업 발생: {}", joinPoint.getSignature().toShortString());
            }
        
            return joinPoint.proceed();
        } finally {
            if (previousDataSource != null) {
                DataSourceContextHolder.setDataSource(previousDataSource);
            } else {
                DataSourceContextHolder.clearDataSource();
            }
        }
    }
    
    /**
     * 쓰기 메서드들을 쓰기 데이터소스로 라우팅
     */
    @Around("(" + MethodPatterns.WRITE_METHODS + ")")
    public Object routeToWriteDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        String previousDataSource = DataSourceContextHolder.getDataSource();
        
        try {
            DataSourceContextHolder.setDataSource("auth.write");
            log.debug("Routing to WRITE datasource for method: {}", joinPoint.getSignature().getName());
            return joinPoint.proceed();
        } finally {
            if (previousDataSource != null) {
                DataSourceContextHolder.setDataSource(previousDataSource);
            } else {
                DataSourceContextHolder.clearDataSource();
            }
        }
    }
}
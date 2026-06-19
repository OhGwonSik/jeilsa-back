package com.common.auth.common.config;

import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import com.common.auth.common.constant.MethodPatterns;

import lombok.extern.slf4j.Slf4j;

/**
 * Configuration 기반 트랜잭션 설정
 * Annotation 없이 메서드 패턴으로 트랜잭션 제어
 */

@Slf4j
@EnableTransactionManagement
@Configuration
public class TransactionConfig {
    //----- DI Fields-----//
    private final PlatformTransactionManager writeTransactionManager;
    private final PlatformTransactionManager readTransactionManager;

    //----- Constructor -----//
    public TransactionConfig(
            @Qualifier("writeTransactionManager") PlatformTransactionManager writeTransactionManager,
            @Qualifier("readTransactionManager") PlatformTransactionManager readTransactionManager) {
        this.writeTransactionManager = writeTransactionManager;
        this.readTransactionManager = readTransactionManager;
    }

    /**
     * 읽기 전용 트랜잭션 속성 소스
     */
    @Bean
    public TransactionAttributeSource readTransactionAttributeSource() {
        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        
        // 읽기 전용 트랜잭션 속성 정의
        RuleBasedTransactionAttribute readOnlyTx = new RuleBasedTransactionAttribute();
        readOnlyTx.setReadOnly(true);
        readOnlyTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_SUPPORTS);
        
        // 읽기 메서드 패턴들 - MethodPatterns 상수 사용
        for (String method : MethodPatterns.READ_METHOD_PATTERNS) {
            source.addTransactionalMethod(method, readOnlyTx);
        }
        
        return source;
    }

    /**
     * 읽기 전용 트랜잭션 설정 (auth.read 데이터소스)
     */
    @Bean
    public TransactionInterceptor readTransactionInterceptor() {
        TransactionInterceptor interceptor = new TransactionInterceptor(){
            @Override
            public Object invoke(MethodInvocation invocation) throws Throwable {
                String methodName = invocation.getMethod().getName();
                String className = invocation.getMethod().getDeclaringClass().getSimpleName();
                log.info("읽기 전용 트랜잭션 시작 - {}.{}", className, methodName);
                
                try {
                    Object result = super.invoke(invocation);
                    log.info("읽기 전용 트랜잭션 완료 - {}.{}", className, methodName);
                    return result;
                } catch (Exception e) {
                    log.error("읽기 전용 트랜잭션 오류 - {}.{}: {}", className, methodName, e.getMessage());
                    throw e;
                }
            }
        };

        interceptor.setTransactionManager(readTransactionManager);
        interceptor.setTransactionAttributeSource(readTransactionAttributeSource());
        return interceptor;
    }

    /**
     * 쓰기 트랜잭션 속성 소스
     */
    @Bean
    public TransactionAttributeSource writeTransactionAttributeSource() {
        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        
        // 쓰기 트랜잭션 속성 정의
        RuleBasedTransactionAttribute writeTx = new RuleBasedTransactionAttribute();
        writeTx.setReadOnly(false);
        writeTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        writeTx.getRollbackRules().add(new RollbackRuleAttribute(Exception.class));

        
        // 쓰기 메서드 패턴들 - MethodPatterns 상수 사용
        for (String method : MethodPatterns.WRITE_METHOD_PATTERNS) {
            source.addTransactionalMethod(method, writeTx);
        }
        // Audit 관련 서비스는 제외
        
        return source;
    }

    /**
     * 쓰기 트랜잭션 설정 (auth.write 데이터소스)
     */
    @Bean
    public TransactionInterceptor writeTransactionInterceptor() {
        TransactionInterceptor interceptor = new TransactionInterceptor(){
            @Override
            public Object invoke(MethodInvocation invocation) throws Throwable {
                String methodName = invocation.getMethod().getName();
                String className = invocation.getMethod().getDeclaringClass().getSimpleName();
                log.info("트랜잭션 시작 - {}.{}", className, methodName);
                
                try {
                    Object result = super.invoke(invocation);
                    log.info("트랜잭션 커밋 - {}.{}", className, methodName);
                    return result;
                } catch (Exception e) {
                    log.error("트랜잭션 롤백 - {}.{}: {}", className, methodName, e.getMessage());
                    throw e;
                }
            }
        };
        interceptor.setTransactionManager(writeTransactionManager);
        interceptor.setTransactionAttributeSource(writeTransactionAttributeSource());
        return interceptor;
    }

    /**
     * 읽기 트랜잭션 Advisor
     */
    @Bean
    public Advisor readTransactionAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(MethodPatterns.READ_METHODS_WITH_EXCLUSION);
        
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, readTransactionInterceptor());
        advisor.setOrder(1); // 낮은 우선순위 (읽기가 우선)
        return advisor;
    }

    /**
     * 쓰기 트랜잭션 Advisor
     */
    @Bean
    public Advisor writeTransactionAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(MethodPatterns.WRITE_METHODS_WITH_EXCLUSION);
        
        DefaultPointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, writeTransactionInterceptor());
        advisor.setOrder(2); // 높은 우선순위 (쓰기가 후순위, 더 구체적)
        return advisor;
    }

    /**
     * 멀티 데이터소스 트랜잭션 매니저 (ChainedTransactionManager 대안)
     * 향후 물류, ERP 등 추가 데이터소스를 위한 확장 포인트
     */
    @Bean
    public MultiDataSourceTransactionManager multiDataSourceTransactionManager() {
        Map<String, PlatformTransactionManager> transactionManagers = new HashMap<>();
        transactionManagers.put("auth.write", writeTransactionManager);
        transactionManagers.put("auth.read", readTransactionManager);
        
        // 향후 추가 데이터소스
        // transactionManagers.put("logistics.write", logisticsWriteTransactionManager);
        // transactionManagers.put("erp.write", erpWriteTransactionManager);
        
        return new MultiDataSourceTransactionManager(transactionManagers);
    }
}
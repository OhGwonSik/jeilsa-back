package com.common.auth.common.config;

import java.util.Map;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;

import com.common.auth.common.datasource.DataSourceContextHolder;

/**
 * Multi-DataSource 환경을 위한 커스텀 트랜잭션 매니저
 * 현재 데이터소스 컨텍스트에 따라 적절한 트랜잭션 매니저를 선택
 */
public class MultiDataSourceTransactionManager implements PlatformTransactionManager {
    
    private final Map<String, PlatformTransactionManager> transactionManagers;
    private final PlatformTransactionManager defaultTransactionManager;
    
    public MultiDataSourceTransactionManager(Map<String, PlatformTransactionManager> transactionManagers) {
        this.transactionManagers = transactionManagers;
        this.defaultTransactionManager = transactionManagers.get("auth.write");
    }
    
    @Override
    @NonNull
    public TransactionStatus getTransaction(@Nullable TransactionDefinition definition) throws TransactionException {
        return getTransactionManager().getTransaction(definition);
    }
    
    @Override
    public void commit(@NonNull TransactionStatus status) throws TransactionException {
        getTransactionManager().commit(status);
    }
    
    @Override
    public void rollback(@NonNull TransactionStatus status) throws TransactionException {
        getTransactionManager().rollback(status);
    }
    
    private PlatformTransactionManager getTransactionManager() {
        String dataSource = DataSourceContextHolder.getDataSource();
        if (dataSource != null && transactionManagers.containsKey(dataSource)) {
            return transactionManagers.get(dataSource);
        }
        return defaultTransactionManager;
    }
}
package com.common.auth.common.aop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.common.auth.audit.domain.SqlRecord;
import com.common.auth.audit.dto.KeyValueDTO;

/**
 * 감사 추적 컨텍스트 홀더 - 트랜잭션 안전 관리
 */
public final class AuditTrailContextHolder {

    private AuditTrailContextHolder() {}

    private static final String AUDIT_CONTEXT_KEY = "AUDIT_TRAIL_CONTEXT";

    /**
     * SQL 레코드 등록
     */
    public static SqlRecord recordSql(SqlCommandType commandType, String tableName, 
                                    List<KeyValueDTO> parameterValues, BoundSql boundSql, 
                                    MappedStatement mappedStatement) {
        SqlRecord recordToStore = new SqlRecord(commandType, tableName, 
                                               parameterValues != null ? parameterValues : Collections.emptyList(), 
                                               boundSql, mappedStatement);
        
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            getOrCreateRecordList().add(recordToStore);
        }
        
        return recordToStore;
    }

    /**
     * 모든 레코드 추출 및 정리
     */
    public static List<SqlRecord> drainAll() {
        if (!TransactionSynchronizationManager.hasResource(AUDIT_CONTEXT_KEY)) {
            return Collections.emptyList();
        }
        
        @SuppressWarnings("unchecked")
        List<SqlRecord> recordsList = (List<SqlRecord>) TransactionSynchronizationManager
                .unbindResource(AUDIT_CONTEXT_KEY);
        
        return recordsList;
    }

    /**
     * 레코드 리스트 조회 또는 생성
     */
    private static List<SqlRecord> getOrCreateRecordList() {
        @SuppressWarnings("unchecked")
        List<SqlRecord> existingRecords = (List<SqlRecord>) TransactionSynchronizationManager
                .getResource(AUDIT_CONTEXT_KEY);
        
        if (existingRecords == null) {
            List<SqlRecord> newRecords = new ArrayList<>();
            TransactionSynchronizationManager.bindResource(AUDIT_CONTEXT_KEY, newRecords);
            return newRecords;
        }
        
        return existingRecords;
    }
}


package com.common.auth.common.aop;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionTemplate;

import com.common.auth.audit.domain.SqlRecord;
import com.common.auth.audit.component.AuditTargetCache;
import com.common.auth.audit.service.AuditLogService;

/**
 * 트랜잭션 동기화 감사 처리 - 독립 트랜잭션으로 안전 보장
 */
@Slf4j
public class AuditTransactionSynchronization implements TransactionSynchronization {

    private final AuditLogService auditLogService;
    private final AuditTargetCache auditTargetCache;
    private final PlatformTransactionManager writeTransactionManager;

    public AuditTransactionSynchronization(AuditLogService auditLogService,
                                         AuditTargetCache auditTargetCache,
                                         PlatformTransactionManager writeTransactionManager) {
        this.auditLogService = auditLogService;
        this.auditTargetCache = auditTargetCache;
        this.writeTransactionManager = writeTransactionManager;
    }

    /**
     * 트랜잭션 완료 후 감사 처리
     */
    @Override
    public void afterCompletion(int completionStatus) {
        try {
            processAuditRecords(completionStatus);
        } catch (Exception exception) {
            log.error("Critical audit logging failure", exception);
        }
    }

    /**
     * 감사 레코드 처리 (독립 트랜잭션)
     */
    private void processAuditRecords(int completionStatus) {
        List<SqlRecord> records = AuditTrailContextHolder.drainAll();
        if (records.isEmpty()) {
            return;
        }

        String transactionStatus = (completionStatus == TransactionSynchronization.STATUS_COMMITTED) ? "COMMIT" : "ROLLBACK";

        TransactionTemplate transactionTemplate = new TransactionTemplate(writeTransactionManager);
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        
        transactionTemplate.execute(transactionStatusParam -> {
            records.stream()
                   .filter(sqlRecordItem -> sqlRecordItem != null && sqlRecordItem.getTableName() != null && sqlRecordItem.getCommandType() != null)
                   .forEach(sqlRecord -> {
                       try {
                           auditLogService.processAuditRecord(sqlRecord, auditTargetCache, transactionStatus);
                       } catch (Exception exception) {
                           log.warn("Failed to process audit record for table {} operation {}", 
                                    sqlRecord.getTableName(), sqlRecord.getCommandType(), exception);
                       }
                   });
            return null;
        });
    }
}
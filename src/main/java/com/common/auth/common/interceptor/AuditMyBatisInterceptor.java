package com.common.auth.common.interceptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.stereotype.Component;

import com.common.auth.audit.component.AuditTargetCache;
import com.common.auth.audit.domain.SqlRecord;
import com.common.auth.audit.dto.KeyValueDTO;
import com.common.auth.audit.service.AuditLogService;
import com.common.auth.audit.util.AuditUtil;
import com.common.auth.common.aop.AuditSqlTableResolver;
import com.common.auth.common.aop.AuditTrailContextHolder;
import com.common.auth.common.util.RequestUtil;
import com.common.auth.common.util.SecurityUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MyBatis DML 인터셉터 - 고성능 감사 추적
 */
@Component
@RequiredArgsConstructor
@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
@Slf4j
public class AuditMyBatisInterceptor implements Interceptor {

    private final AuditLogService auditLogService;
    private final AuditTargetCache auditTargetCache;
    private final AuditSqlTableResolver sqlTableResolver;

    /**
     * DML 연산 인터셉트
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement mappedStatement = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs().length > 1 ? invocation.getArgs()[1] : null;
        SqlCommandType commandType = mappedStatement.getSqlCommandType();

        if (!isDMLOperation(commandType)) {
            return invocation.proceed();
        }

        try {
            return processAuditableOperation(invocation, mappedStatement, parameter, commandType);
        } catch (Exception exception) {
            log.debug("Audit processing failed, proceeding with original query", exception);
            return invocation.proceed();
        }
    }

    /**
     * DML 연산 여부 확인
     */
    private boolean isDMLOperation(SqlCommandType commandType) {
        return commandType == SqlCommandType.INSERT || 
               commandType == SqlCommandType.UPDATE || 
               commandType == SqlCommandType.DELETE;
    }

    /**
     * 감사 가능한 연산 처리
     */
    private Object processAuditableOperation(Invocation invocation, MappedStatement mappedStatement, 
                                        Object parameter, SqlCommandType commandType) throws Throwable {
        try {
            String tableName = sqlTableResolver.resolve(mappedStatement, parameter, commandType);
            if (tableName == null) {
                return invocation.proceed();
            }
            
            Map<String, Integer> auditableColumns = auditTargetCache.getTargetIdsByColumns(tableName);
            if (auditableColumns.isEmpty()) {
                return invocation.proceed();
            }

            BoundSql boundSql = mappedStatement.getBoundSql(parameter);
            List<KeyValueDTO> parameterValues = extractParameterValues(boundSql, mappedStatement);

            // 🎯 SecurityUtil 호출을 try-catch로 감싸서 에러 처리
            Integer actorUserId = null;
            try {
                actorUserId = SecurityUtil.getCurrentMemberId();
            } catch (Throwable t) {  
                log.debug("SecurityUtil.getCurrentUserId() failed, ignoring: {}", t.getMessage());
                actorUserId = null;
            }
            
            if (actorUserId == null) {
                log.debug("No authenticated user found, skipping audit for table: {}", tableName);
                return invocation.proceed();
            }

            SqlRecord sqlRecord = AuditTrailContextHolder.recordSql(
                    commandType, tableName, parameterValues, boundSql, mappedStatement
            );

            HttpServletRequest httpRequest = RequestUtil.getHttpServletRequest();
            sqlRecord.setActorUserId(actorUserId);
            sqlRecord.setRequestIp(RequestUtil.getClientIpAddress(httpRequest));
            capturePreImageIfNeeded(sqlRecord, commandType);
            
            return invocation.proceed();
        } catch (RuntimeException runtimeException) {
            log.warn("Audit processing failed for operation on table: {}", 
                    sqlTableResolver.resolve(mappedStatement, parameter, commandType), runtimeException);
            return invocation.proceed();
        }
    }
    
    /**
     * Pre-image 캡처 (필요시)
     */
    private void capturePreImageIfNeeded(SqlRecord sqlRecord, SqlCommandType commandType) {
        if (commandType == SqlCommandType.UPDATE || commandType == SqlCommandType.DELETE) {
            List<KeyValueDTO> preImage = auditLogService.capturePreImageSnapshot(sqlRecord, auditTargetCache);
            sqlRecord.setOldRow(preImage);
        }
    }

    /**
     * 파라미터 값 추출 (PropertyTokenizer 기반)
     */
    private List<KeyValueDTO> extractParameterValues(BoundSql boundSql, MappedStatement mappedStatement) {
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (parameterMappings == null || parameterMappings.isEmpty()) {
            return Collections.emptyList();
        }

        TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        Object rootParameterObject = boundSql.getParameterObject();
        MetaObject metaObject = rootParameterObject == null ? null : 
                               mappedStatement.getConfiguration().newMetaObject(rootParameterObject);

        List<KeyValueDTO> result = new ArrayList<>(parameterMappings.size());
        for (ParameterMapping parameterMapping : parameterMappings) {
            KeyValueDTO extracted = extractSingleParameter(parameterMapping, boundSql, 
                                                          rootParameterObject, typeHandlerRegistry, metaObject);
            result.add(extracted);
        }
        return result;
    }

    /**
     * 단일 파라미터 추출
     */
    private KeyValueDTO extractSingleParameter(ParameterMapping parameterMapping, BoundSql boundSql,
                                              Object rootParameterObject, TypeHandlerRegistry typeHandlerRegistry,
                                              MetaObject metaObject) {
        String propertyName = parameterMapping.getProperty();
        Object parameterValue = getParameterValue(propertyName, boundSql, rootParameterObject, 
                                                 typeHandlerRegistry, metaObject);
        
        String normalizedKey = AuditUtil.normalizeAndSnakeUpper(propertyName);
        return new KeyValueDTO(normalizedKey, parameterValue);
    }

    /**
     * 파라미터 값 조회
     */
    private Object getParameterValue(String propertyName, BoundSql boundSql, Object rootParameterObject,
                                    TypeHandlerRegistry typeHandlerRegistry, MetaObject metaObject) {
        if (boundSql.hasAdditionalParameter(propertyName)) {
            return boundSql.getAdditionalParameter(propertyName);
        }
        if (rootParameterObject == null) {
            return null;
        }
        if (typeHandlerRegistry.hasTypeHandler(rootParameterObject.getClass())) {
            return rootParameterObject;
        }
        if (metaObject != null) {
            return metaObject.getValue(propertyName);
        }
        return null;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
}



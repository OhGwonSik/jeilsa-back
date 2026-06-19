package com.common.auth.audit.domain;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;

import com.common.auth.audit.dto.KeyValueDTO;

import lombok.Getter;

/**
 * SQL 실행 컨텍스트 기록 - 공통 감사 시스템
 * 
 */
@Getter
public final class SqlRecord {
    private final SqlCommandType commandType;
    private final String tableName;
    private final List<KeyValueDTO> parameterValues;
    private final List<KeyValueDTO> oldRow;
    private final BoundSql boundSql;
    private final MappedStatement mappedStatement;

    private Integer actorUserId;
    private InetAddress requestIp;

    public SqlRecord(SqlCommandType commandType, String tableName, List<KeyValueDTO> parameterValues, 
                     BoundSql boundSql, MappedStatement mappedStatement) {
        this.commandType = commandType;
        this.tableName = tableName;
        // 성능 최적화: 이미 불변인 경우 복사 생략
        if (parameterValues == null) {
            this.parameterValues = List.of();
        } else if (parameterValues instanceof ArrayList) {
            this.parameterValues = List.copyOf(parameterValues);
        } else {
            this.parameterValues = parameterValues;
        }
        this.oldRow = new ArrayList<>();
        this.boundSql = boundSql;
        this.mappedStatement = mappedStatement;

    }

    /**
     * Old row 데이터 설정 (UPDATE/DELETE 전용)
     */
    public void setOldRow(List<KeyValueDTO> columns) {
        this.oldRow.clear();
        if (columns != null) {
            this.oldRow.addAll(columns);
        }
    }

    // mutable context fields setters
    public void setActorUserId(Integer actorUserId) {
        this.actorUserId = actorUserId;
    }

    public void setRequestIp(InetAddress requestIp) {
        this.requestIp = requestIp;
    }
    
    // Override Lombok getter for collections to ensure immutability
    public List<KeyValueDTO> getParameterValues() { 
        return Collections.unmodifiableList(parameterValues); 
    }

    public List<KeyValueDTO> getOldRow() { 
        return Collections.unmodifiableList(oldRow); 
    }
}



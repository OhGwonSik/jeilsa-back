package com.common.auth.audit.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.common.auth.audit.dto.AuditLogDTO;
import com.common.auth.audit.dto.AuditTableMetaDTO;
import com.common.auth.audit.dto.KeyValueDTO;
import com.common.auth.audit.dto.PkConditionDTO;

@Mapper
public interface AuditMapper {

    List<AuditTableMetaDTO> getAuditTableMetaList();

    void insertAuditLog(AuditLogDTO auditLog);

    List<KeyValueDTO> selectRowColumns(String tableName, List<String> columns, List<PkConditionDTO> pkConditions);
}



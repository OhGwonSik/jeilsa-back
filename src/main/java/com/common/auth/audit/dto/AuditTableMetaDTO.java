package com.common.auth.audit.dto;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class AuditTableMetaDTO {
    private Integer targetId;       // AUDIT_TARGET.TARGET_ID
    private String tableName;    // AUDIT_TARGET.TABLE_NAME
    private String tableColumn;  // AUDIT_TARGET.TABLE_COLUMN
    private String description;  // AUDIT_TARGET.DESCRIPTION
    private String pkColumnsCsv; // PK 컬럼들 (CSV, information_schema에서)
}



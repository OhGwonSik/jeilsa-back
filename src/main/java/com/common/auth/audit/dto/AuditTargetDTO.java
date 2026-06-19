package com.common.auth.audit.dto;



import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
public class AuditTargetDTO {
    private Integer targetId;
    private String tableName;
    private String tableColumn;
    private String description;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private String delYn;

    // Custom
    private String pkColumn;
}



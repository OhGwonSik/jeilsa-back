package com.common.auth.audit.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.InetAddress;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
public class AuditLogDTO {
    private Integer logId;
    private Integer targetId;
    private String tableName;
    private String tableColumn;
    private String operation; 
    private String transactionStatus; 
    private Object valueOld;  // String → Object로 변경
    private Object valueNew;  // String → Object로 변경
    private String valueOldType;  // 컬럼 값의 타입 정보
    private String valueNewType;  // 컬럼 값의 타입 정보
    private Integer userId;
    private InetAddress requestIp;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private String delYn;
}



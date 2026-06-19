package com.common.auth.permission.dto;


import com.common.auth.permission.domain.Permission;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PermissionResponse {
    private Integer permissionId;
    private String permissionName;
    private String description;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private Integer delId;
    private LocalDateTime delDt;
    private String delYn;
    
    public PermissionResponse(Permission permission) {
        this.permissionId = permission.getPermissionId();
        this.permissionName = permission.getPermissionName();
        this.description = permission.getDescription();
        this.regId = permission.getRegId();
        this.regDt = permission.getRegDt();
        this.chgId = permission.getChgId();
        this.chgDt = permission.getChgDt();
        this.delId = permission.getDelId();
        this.delDt = permission.getDelDt();
        this.delYn = permission.getDelYn();
    }

    public static PermissionResponse from(Permission permission) {
        return new PermissionResponse(permission);
    }
}
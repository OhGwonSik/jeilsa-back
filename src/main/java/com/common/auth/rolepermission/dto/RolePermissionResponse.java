package com.common.auth.rolepermission.dto;


import com.common.auth.rolepermission.domain.RolePermission;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class RolePermissionResponse {
    //Field
    private Integer roleId;
    private Integer permissionId;
    private String roleName;
    private String permissionName;
    private String delYn;
    private String description;
    private LocalDateTime regDt;
    private Integer regId;
    private LocalDateTime chgDt;
    private Integer chgId;
    private LocalDateTime delDt;
    private Integer delId;
    
    //Constructor
    public RolePermissionResponse(RolePermission rolePermission) {
        this.roleId = rolePermission.getRoleId();
        this.permissionId = rolePermission.getPermissionId();
        this.description = rolePermission.getDescription();
        this.delYn = rolePermission.getDelYn();
        this.regDt = rolePermission.getRegDt();
        this.regId = rolePermission.getRegId();
        this.chgDt = rolePermission.getChgDt();
        this.chgId = rolePermission.getChgId();
        this.delDt = rolePermission.getDelDt();
        this.delId = rolePermission.getDelId();
    }
    
    // Factory Method
    public static RolePermissionResponse from(RolePermission rolePermission) {
        return new RolePermissionResponse(rolePermission);
    }
}
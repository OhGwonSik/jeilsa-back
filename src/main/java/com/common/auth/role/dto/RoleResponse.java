package com.common.auth.role.dto;


import com.common.auth.role.domain.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class RoleResponse {
    //----- Fields -----//
    private Integer roleId;
    private String roleName;
    private String description;
    private Boolean isDefault;
    private String delYn;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private Integer delId;
    private LocalDateTime delDt;
    
    public RoleResponse(Role role) {
        this.roleId = role.getRoleId();
        this.roleName = role.getRoleName();
        this.description = role.getDescription();
        this.isDefault = role.getIsDefault();
        this.delYn = role.getDelYn();
        this.regId = role.getRegId();
        this.regDt = role.getRegDt();
        this.chgId = role.getChgId();
        this.chgDt = role.getChgDt();
        this.delId = role.getDelId();
        this.delDt = role.getDelDt();
    }
    
    public static RoleResponse from(Role role) {
        return new RoleResponse(role);
    }
}
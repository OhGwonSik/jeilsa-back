package com.common.auth.rolepermission.domain;



import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class RolePermission {
    private Integer roleId;
    private Integer permissionId;
    private String description;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private Integer delId;
    private LocalDateTime delDt;
    private String delYn;

    public RolePermission() {
        this.regDt = LocalDateTime.now();
        this.chgDt = LocalDateTime.now();
        this.delYn = "N";
    }

    public RolePermission(Integer roleId, Integer permissionId) {
        this();
        this.roleId = roleId;
        this.permissionId = permissionId;
    }
}
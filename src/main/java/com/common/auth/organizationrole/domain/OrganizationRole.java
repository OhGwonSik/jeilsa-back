package com.common.auth.organizationrole.domain;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class OrganizationRole {
    private Integer organizationId;
    private String organizationName;
    private Integer roleId;
    private String roleName;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private Integer delId;
    private LocalDateTime delDt;
    private String delYn;

    public OrganizationRole() {
        this.regDt = LocalDateTime.now();
        this.chgDt = LocalDateTime.now();
        this.delYn = "N";
    }

    public OrganizationRole(Integer organizationId, Integer roleId) {
        this();
        this.organizationId = organizationId;
        this.roleId = roleId;
    }
}
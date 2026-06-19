package com.common.auth.userrole.domain;




import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class UserRole {
    private Integer memberId;
    private Integer roleId;
    private String userName;
    private String roleName;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private Integer delId;
    private LocalDateTime delDt;
    private String delYn;

    public UserRole() {
        this.regDt = LocalDateTime.now();
        this.chgDt = LocalDateTime.now();
        this.delYn = "N";
    }

    public UserRole(Integer memberId, Integer roleId) {
        this();
        this.memberId = memberId;
        this.roleId = roleId;
    }
}
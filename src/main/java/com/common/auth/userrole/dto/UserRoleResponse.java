package com.common.auth.userrole.dto;


import com.common.auth.userrole.domain.UserRole;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class UserRoleResponse {
    private Integer roleId;
    private Integer memberId;
    private String userName;
    private String roleName;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private Integer delId;
    private LocalDateTime delDt;
    private String delYn;

    public UserRoleResponse(UserRole userRole){
        this.roleId = userRole.getRoleId();
        this.memberId = userRole.getMemberId();
        this.userName = userRole.getUserName();
        this.roleName = userRole.getRoleName();
        this.regId = userRole.getRegId();
        this.regDt = userRole.getRegDt();
        this.chgId = userRole.getChgId();
        this.chgDt = userRole.getChgDt();
        this.delId = userRole.getDelId();
        this.delDt = userRole.getDelDt();
        this.delYn = userRole.getDelYn();
    }
}

package com.common.auth.userorganization.dto;


import com.common.auth.userorganization.domain.UserOrganization;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class UserOrganizationResponse {
    private Integer userOrganizationId;
    private Integer memberId;
    private String userName;
    private String email;
    private Integer organizationId;
    private String organizationName;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private Integer delId;
    private LocalDateTime delDt;
    private String delYn;

    public UserOrganizationResponse(UserOrganization userOrganization){
        this.userOrganizationId = userOrganization.getOrganizationId();
        this.memberId = userOrganization.getMemberId();
        this.userName = userOrganization.getUserName();
        this.email = userOrganization.getEmail();
        this.organizationId = userOrganization.getOrganizationId();
        this.organizationName = userOrganization.getOrganizationName();
        this.regId = userOrganization.getRegId();
        this.regDt = userOrganization.getRegDt();
        this.chgId = userOrganization.getChgId();
        this.chgDt = userOrganization.getChgDt();
        this.delId = userOrganization.getDelId();
        this.delDt = userOrganization.getDelDt();
        this.delYn = userOrganization.getDelYn();
    }
}

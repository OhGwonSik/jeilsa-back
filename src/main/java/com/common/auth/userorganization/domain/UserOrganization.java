package com.common.auth.userorganization.domain;




import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class UserOrganization {
    private Integer memberId;
    private Integer organizationId;
    private String userName;
    private String organizationName;
    private String email;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private Integer delId;
    private LocalDateTime delDt;
    private String delYn;

    public UserOrganization() {
        this.regDt = LocalDateTime.now();
        this.chgDt = LocalDateTime.now();
        this.delYn = "N";
    }

    public UserOrganization(Integer memberId, Integer organizationId) {
        this();
        this.memberId = memberId;
        this.organizationId = organizationId;
    }
}
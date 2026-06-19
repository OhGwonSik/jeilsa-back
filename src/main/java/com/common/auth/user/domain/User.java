package com.common.auth.user.domain;




import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class User {
    //----- Constants -----//
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_LOCKED = "LOCKED";
    
    private Integer memberId;
    private String userName;
    private String email;
    private String passwordHash;
    private String telNo;
    private String userStatusCd;
    private LocalDateTime lastLoginDt;
    private Integer loginFailCnt;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private Integer delId;
    private LocalDateTime delDt;
    private String delYn;

    public User() {
        this.regDt = LocalDateTime.now();
        this.chgDt = LocalDateTime.now();
        this.delYn = "N";
        this.loginFailCnt = 0;
    }

    public User(String userName, String email, String passwordHash) {
        this();
        this.userName = userName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.userStatusCd = STATUS_ACTIVE;
    }
}
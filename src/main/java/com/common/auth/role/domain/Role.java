package com.common.auth.role.domain;




import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Role {
    private Integer roleId;
    private String roleName;
    private String description;
    private Boolean isDefault;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private Integer delId;
    private LocalDateTime delDt;
    private String delYn;

    public Role() {
        this.regDt = LocalDateTime.now();
        this.chgDt = LocalDateTime.now();
        this.delYn = "N";
        this.isDefault = false;
    }

    public Role(String roleName, String description) {
        this();
        this.roleName = roleName;
        this.description = description;
    }
}
package com.common.auth.rolemenu.domain;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class RoleMenu {
    private Integer roleId;
    private Integer menuId;
    private String roleName;
    private String menuName;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private Integer delId;
    private LocalDateTime delDt;
    private String delYn;
    private List<RoleMenu> items;

    public RoleMenu() {
        this.regDt = LocalDateTime.now();
        this.chgDt = LocalDateTime.now();
        this.delYn = "N";
    }

    public RoleMenu(Integer roleId, Integer menuId) {
        this();
        this.roleId = roleId;
        this.menuId = menuId;
    }
}
package com.common.auth.menu.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
public class Menu {
    private Integer menuId;
    private String menuName;
    private String menuType;
    private String description;
    private Integer parentId;
    private String menuPath;
    private Integer menuOrder;
    private Integer regId;
    private LocalDateTime regDt;
    private Integer chgId;
    private LocalDateTime chgDt;
    private Integer delId;
    private LocalDateTime delDt;
    private String delYn;
    private List<Menu> items;

    public Menu() {
        this.regDt = LocalDateTime.now();
        this.chgDt = LocalDateTime.now();
        this.delYn = "N";
    }

    public Menu(String menuName, String menuType, String menuPath, Integer parentId, Integer menuOrder) {
        this();
        this.menuName = menuName;
        this.menuType = menuType;
        this.menuPath = menuPath;
        this.parentId = parentId;
        this.menuOrder = menuOrder;
    }
}
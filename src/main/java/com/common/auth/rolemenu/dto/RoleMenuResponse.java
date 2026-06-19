package com.common.auth.rolemenu.dto;


import com.common.auth.rolemenu.domain.RoleMenu;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 부서-역할 매핑 응답 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@Schema(description = "롤-메뉴 매핑 응답 DTO")
public class RoleMenuResponse {
    @Schema(description = "롤 ID")
    private Integer roleId;
    
    @Schema(description = "롤명")
    private String roleName;

    @Schema(description = "메뉴 ID")
    private Integer menuId;
    
    @Schema(description = "메뉴명")
    private String menuName;

    @Schema(description = "상위 메뉴 ID")
    private Integer parentId;
    
    @Schema(description = "활성 상태")
    private String delYn;
    
    @Schema(description = "생성자 ID")
    private Integer regId;
    
    @Schema(description = "생성일시")
    private LocalDateTime regDt;
    
    @Schema(description = "수정자 ID")
    private Integer chgId;
    
    @Schema(description = "수정일시")
    private LocalDateTime chgDt;

    @Schema(description = "삭제자 ID")
    private Integer delId;
    
    @Schema(description = "삭제일시")
    private LocalDateTime delDt;

    @JsonProperty("Items")
    private List<RoleMenuResponse> items;
    
    public RoleMenuResponse(RoleMenu roleMenu) {
        this.roleId = roleMenu.getRoleId();
        this.menuId = roleMenu.getMenuId();
        this.roleName = roleMenu.getRoleName();
        this.menuName = roleMenu.getMenuName();
        this.regId = roleMenu.getRegId();
        this.regDt = roleMenu.getRegDt();
        this.chgId = roleMenu.getChgId();
        this.chgDt = roleMenu.getChgDt();
        this.delId = roleMenu.getDelId();
        this.delDt = roleMenu.getDelDt();
        this.delYn = roleMenu.getDelYn();

        this.items = roleMenu.getItems().stream()
                        .map(this::from)
                        .collect(Collectors.toList());
    }    

    public RoleMenuResponse from(RoleMenu roleMenu) {
        return new RoleMenuResponse(roleMenu);
    }
}

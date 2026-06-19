package com.common.auth.menu.dto;

import com.common.auth.menu.domain.Menu;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Schema(description = "메뉴 응답 DTO")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class MenuResponse {
    @Schema(description = "메뉴 ID")
    private Integer menuId;
    
    @Schema(description = "메뉴 이름")
    private String menuName;
    
    @Schema(description = "메뉴 타입")
    private String menuType;
    
    @Schema(description = "설명")
    private String description;
    
    @Schema(description = "상위 메뉴 ID")
    private Integer parentId;
    
    @Schema(description = "메뉴 경로")
    private String menuPath;
    
    @Schema(description = "메뉴 순서")
    private Integer menuOrder;
    
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
    @Schema(description = "자식 item")
    private List<MenuResponse> items;
    
    public MenuResponse(Menu menu) {
        this.menuId = menu.getMenuId();
        this.menuName = menu.getMenuName();
        this.menuType = menu.getMenuType();
        this.description = menu.getDescription();
        this.parentId = menu.getParentId();
        this.menuPath = menu.getMenuPath();
        this.menuOrder = menu.getMenuOrder();
        this.delYn = menu.getDelYn();
        this.regId = menu.getRegId();
        this.regDt = menu.getRegDt();
        this.chgId = menu.getChgId();
        this.chgDt = menu.getChgDt();
        this.delId = menu.getDelId();
        this.delDt = menu.getDelDt();

        this.items = menu.getItems().stream()
                        .map(this::from)
                        .collect(Collectors.toList());
    }

    public MenuResponse from(Menu menu) {
        return new MenuResponse(menu);
    }
}
package com.common.auth.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;
import java.util.Map;


import com.common.auth.menu.dto.MenuResponse;
import com.common.auth.role.dto.RoleResponse;
import com.common.auth.user.dto.UserResponse;

@Getter
@Setter
@ToString
@Schema(description = "동적 리소스 응답 DTO")
public class DynamicResourceResponse {
    
    @Schema(description = "사용자별 역할 목록")
    private List<RoleResponse> userRoles;
    
    @Schema(description = "사용자별 메뉴 목록")
    private List<MenuResponse> userMenus;
    
    @Schema(description = "사용자별 메뉴 트리")
    private List<MenuTreeNode> menuTree;
    
    @Schema(description = "사용자별 권한 목록")
    private List<String> userPermissions;
    
    @Schema(description = "사용자 정보")
    private UserResponse userInfo;
    
    @Schema(description = "추가 메타데이터")
    private Map<String, Object> metadata;

    @Getter
    @Setter
    @ToString
    @Schema(description = "메뉴 트리 노드")
    public static class MenuTreeNode {
        @Schema(description = "메뉴 ID")
        private Integer menuId;
        
        @Schema(description = "메뉴 이름")
        private String menuName;
        
        @Schema(description = "메뉴 타입")
        private String menuType;
        
        @Schema(description = "메뉴 경로")
        private String menuPath;
        
        @Schema(description = "메뉴 순서")
        private Integer menuOrder;
        
        @Schema(description = "상위 메뉴 ID")
        private Integer parentId;
        
        @Schema(description = "하위 메뉴 목록")
        private List<MenuTreeNode> items;
        
        @Schema(description = "활성 상태")
        private String delYn;

        // Getters and Setters
        public Integer getMenuId() {
            return menuId;
        }

        public void setMenuId(Integer menuId) {
            this.menuId = menuId;
        }

        public String getMenuName() {
            return menuName;
        }

        public void setMenuName(String menuName) {
            this.menuName = menuName;
        }

        public String getMenuType() {
            return menuType;
        }

        public void setMenuType(String menuType) {
            this.menuType = menuType;
        }

        public String getMenuPath() {
            return menuPath;
        }

        public void setMenuPath(String menuPath) {
            this.menuPath = menuPath;
        }

        public Integer getMenuOrder() {
            return menuOrder;
        }

        public void setMenuOrder(Integer menuOrder) {
            this.menuOrder = menuOrder;
        }

        public Integer getParentId() {
            return parentId;
        }

        public void setParentId(Integer parentId) {
            this.parentId = parentId;
        }
    }
}
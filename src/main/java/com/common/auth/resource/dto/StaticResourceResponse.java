package com.common.auth.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Map;

import com.common.auth.menu.dto.MenuResponse;
import com.common.auth.organization.dto.OrganizationResponse;
import com.common.auth.permission.dto.PermissionResponse;
import com.common.auth.role.dto.RoleResponse;

@Schema(description = "정적 리소스 응답 DTO")
public class StaticResourceResponse {
    
    @Schema(description = "역할 목록")
    private List<RoleResponse> roles;
    
    @Schema(description = "권한 목록")
    private List<PermissionResponse> permissions;
    
    @Schema(description = "조직 목록")
    private List<OrganizationResponse> organizations;
    
    @Schema(description = "메뉴 목록")
    private List<MenuResponse> menus;
    
    @Schema(description = "공통 코드 목록")
    private Map<String, List<CodeItem>> commonCodes;

    @Schema(description = "코드 아이템")
    public static class CodeItem {
        @Schema(description = "코드 값")
        private String code;
        
        @Schema(description = "코드 명")
        private String name;
        
        @Schema(description = "설명")
        private String description;
        
        @Schema(description = "정렬 순서")
        private Integer sortOrder;

        public CodeItem() {}

        public CodeItem(String code, String name, String description, Integer sortOrder) {
            this.code = code;
            this.name = name;
            this.description = description;
            this.sortOrder = sortOrder;
        }

        // Getters and Setters
        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getSortOrder() {
            return sortOrder;
        }

        public void setSortOrder(Integer sortOrder) {
            this.sortOrder = sortOrder;
        }
    }

    // Getters and Setters
    public List<RoleResponse> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleResponse> roles) {
        this.roles = roles;
    }

    public List<PermissionResponse> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionResponse> permissions) {
        this.permissions = permissions;
    }

    public List<OrganizationResponse> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<OrganizationResponse> organizations) {
        this.organizations = organizations;
    }

    public List<MenuResponse> getMenus() {
        return menus;
    }

    public void setMenus(List<MenuResponse> menus) {
        this.menus = menus;
    }

    public Map<String, List<CodeItem>> getCommonCodes() {
        return commonCodes;
    }

    public void setCommonCodes(Map<String, List<CodeItem>> commonCodes) {
        this.commonCodes = commonCodes;
    }
}
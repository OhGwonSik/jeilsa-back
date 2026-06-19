package com.common.auth.common.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 전역 컬럼 매핑 서비스
 * 모든 도메인의 정렬 컬럼 매핑을 중앙에서 관리
 */
@Slf4j
@Service
public class ColumnMappingService {
    
    private final Map<String, Map<String, String>> domainColumnMappings;
    
    public ColumnMappingService() {
        this.domainColumnMappings = new HashMap<>();
        initializeMappings();
    }
    
    private void initializeMappings() {
        // User 도메인 매핑
        Map<String, String> userMappings = new HashMap<>();
        userMappings.put("username", "user_name");
        userMappings.put("userName", "user_name");
        userMappings.put("email", "email");
        userMappings.put("phonenumber", "telNo");
        userMappings.put("telNo", "telNo");
        userMappings.put("userstatuscd", "user_status_cd");
        userMappings.put("userStatusCd", "user_status_cd");
        userMappings.put("status", "user_status_cd");
        userMappings.put("lastlogindt", "last_login_dt");
        userMappings.put("lastLoginDt", "last_login_dt");
        userMappings.put("lastLoginAt", "last_login_dt");
        userMappings.put("lastlogin", "last_login_dt");
        userMappings.put("inactive", "del_yn");
        userMappings.put("inActive", "del_yn");
        userMappings.put("active", "del_yn");
        userMappings.put("createdDt", "reg_dt");
        userMappings.put("createdAt", "reg_dt");
        userMappings.put("created", "reg_dt");
        userMappings.put("changedDt", "chg_dt");
        userMappings.put("changedAt", "chg_dt");
        userMappings.put("updated", "chg_dt");
        userMappings.put("modified", "chg_dt");
        domainColumnMappings.put("user", userMappings);
        
        // Role 도메인 매핑
        Map<String, String> roleMappings = new HashMap<>();
        roleMappings.put("rolename", "role_name");
        roleMappings.put("roleName", "role_name");
        roleMappings.put("name", "role_name");
        roleMappings.put("description", "description");
        roleMappings.put("isdefault", "is_default");
        roleMappings.put("isDefault", "is_default");
        roleMappings.put("defaultrole", "is_default");
        roleMappings.put("inactive", "del_yn");
        roleMappings.put("inActive", "del_yn");
        roleMappings.put("active", "del_yn");
        roleMappings.put("createdDt", "reg_dt");
        roleMappings.put("createdAt", "reg_dt");
        roleMappings.put("created", "reg_dt");
        roleMappings.put("changedDt", "chg_dt");
        roleMappings.put("changedAt", "chg_dt");
        roleMappings.put("updated", "chg_dt");
        roleMappings.put("modified", "chg_dt");
        domainColumnMappings.put("role", roleMappings);
        
        // Organization 도메인 매핑
        Map<String, String> organizationMappings = new HashMap<>();
        organizationMappings.put("organizationname", "organization_name");
        organizationMappings.put("organizationName", "organization_name");
        organizationMappings.put("name", "organization_name");
        organizationMappings.put("description", "description");
        organizationMappings.put("inactive", "del_yn");
        organizationMappings.put("inActive", "del_yn");
        organizationMappings.put("active", "del_yn");
        organizationMappings.put("createdDt", "reg_dt");
        organizationMappings.put("createdAt", "reg_dt");
        organizationMappings.put("created", "reg_dt");
        organizationMappings.put("changedDt", "chg_dt");
        organizationMappings.put("changedAt", "chg_dt");
        organizationMappings.put("updated", "chg_dt");
        organizationMappings.put("modified", "chg_dt");
        domainColumnMappings.put("organization", organizationMappings);
        
        // Menu 도메인 매핑
        Map<String, String> menuMappings = new HashMap<>();
        menuMappings.put("menuname", "menu_name");
        menuMappings.put("menuName", "menu_name");
        menuMappings.put("name", "menu_name");
        menuMappings.put("menutype", "menu_type");
        menuMappings.put("menuType", "menu_type");
        menuMappings.put("type", "menu_type");
        menuMappings.put("menupath", "menu_path");
        menuMappings.put("menuPath", "menu_path");
        menuMappings.put("path", "menu_path");
        menuMappings.put("menuorder", "menu_order");
        menuMappings.put("menuOrder", "menu_order");
        menuMappings.put("order", "menu_order");
        menuMappings.put("parentid", "parent_id");
        menuMappings.put("parentId", "parent_id");
        menuMappings.put("parent", "parent_id");
        menuMappings.put("description", "description");
        menuMappings.put("inactive", "del_yn");
        menuMappings.put("inActive", "del_yn");
        menuMappings.put("active", "del_yn");
        menuMappings.put("createdDt", "reg_dt");
        menuMappings.put("createdAt", "reg_dt");
        menuMappings.put("created", "reg_dt");
        menuMappings.put("changedDt", "chg_dt");
        menuMappings.put("changedAt", "chg_dt");
        menuMappings.put("updated", "chg_dt");
        menuMappings.put("modified", "chg_dt");
        domainColumnMappings.put("menu", menuMappings);
        
        // Permission 도메인 매핑
        Map<String, String> permissionMappings = new HashMap<>();
        permissionMappings.put("permissionname", "permission_name");
        permissionMappings.put("permissionName", "permission_name");
        permissionMappings.put("name", "permission_name");
        permissionMappings.put("description", "description");
        permissionMappings.put("inactive", "del_yn");
        permissionMappings.put("inActive", "del_yn");
        permissionMappings.put("active", "del_yn");
        permissionMappings.put("createdDt", "reg_dt");
        permissionMappings.put("createdAt", "reg_dt");
        permissionMappings.put("created", "reg_dt");
        permissionMappings.put("changedDt", "chg_dt");
        permissionMappings.put("changedAt", "chg_dt");
        permissionMappings.put("updated", "chg_dt");
        permissionMappings.put("modified", "chg_dt");
        domainColumnMappings.put("permission", permissionMappings);
        
        log.info("Column mappings initialized for {} domains", domainColumnMappings.size());
    }
    
    /**
     * 도메인별 컬럼 매핑
     * @param domain 도메인명 (user, role, organization, menu, permission 등)
     * @param clientColumn 클라이언트에서 전달된 컬럼명
     * @return 매핑된 DB 컬럼명 (매핑되지 않으면 null)
     */
    public String mapColumn(String domain, String clientColumn) {
        if (domain == null || clientColumn == null) {
            return null;
        }
        
        Map<String, String> domainMappings = domainColumnMappings.get(domain.toLowerCase());
        if (domainMappings == null) {
            log.warn("No column mappings found for domain: {}", domain);
            return null;
        }
        
        String mappedColumn = domainMappings.get(clientColumn.toLowerCase());
        if (mappedColumn != null) {
            log.debug("Column mapped for domain {}: {} -> {}", domain, clientColumn, mappedColumn);
        }
        
        return mappedColumn;
    }
    
    /**
     * 허용된 정렬 컬럼인지 확인
     * @param domain 도메인명
     * @param clientColumn 클라이언트에서 전달된 컬럼명
     * @return 허용된 컬럼이면 true
     */
    public boolean isAllowedSortColumn(String domain, String clientColumn) {
        return mapColumn(domain, clientColumn) != null;
    }
}
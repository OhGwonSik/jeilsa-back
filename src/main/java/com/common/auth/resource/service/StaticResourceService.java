package com.common.auth.resource.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.common.auth.menu.domain.Menu;
import com.common.auth.menu.dto.MenuResponse;
import com.common.auth.menu.mapper.MenuMapper;
import com.common.auth.organization.domain.Organization;
import com.common.auth.organization.dto.OrganizationResponse;
import com.common.auth.organization.mapper.OrganizationMapper;
import com.common.auth.permission.domain.Permission;
import com.common.auth.permission.dto.PermissionResponse;
import com.common.auth.permission.mapper.PermissionMapper;
import com.common.auth.resource.dto.StaticResourceResponse;
import com.common.auth.role.domain.Role;
import com.common.auth.role.dto.RoleResponse;
import com.common.auth.role.mapper.RoleMapper;
import com.common.auth.user.domain.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class StaticResourceService {
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final OrganizationMapper organizationMapper;
    private final MenuMapper menuMapper;

    public StaticResourceService(RoleMapper roleMapper, 
                                PermissionMapper permissionMapper,
                                OrganizationMapper organizationMapper,
                                MenuMapper menuMapper) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.organizationMapper = organizationMapper;
        this.menuMapper = menuMapper;
    }

    public StaticResourceResponse getAllStaticResources() {
        log.debug("Fetching all static resources");
        
        StaticResourceResponse response = new StaticResourceResponse();
        
        // 역할 목록 조회
        List<Role> roles = roleMapper.selectAllRoles();
        response.setRoles(roles.stream()
                .map(RoleResponse::from)
                .collect(Collectors.toList()));
        
        // 권한 목록 조회
        List<Permission> permissions = permissionMapper.selectAllPermissions();
        response.setPermissions(permissions.stream()
                .map(permission -> new PermissionResponse(permission))
                .collect(Collectors.toList()));
        
        // 조직 목록 조회
        List<Organization> organizations = organizationMapper.selectAllOrganizations();
        response.setOrganizations(organizations.stream()
                .map(organization -> new OrganizationResponse(organization))
                .collect(Collectors.toList()));
        
        // 메뉴 목록 조회
        List<Menu> menus = menuMapper.selectAllMenus();
        response.setMenus(menus.stream()
                .map(menu -> new MenuResponse(menu))
                .collect(Collectors.toList()));
        
        // 공통 코드 조회
        response.setCommonCodes(getCommonCodes());
        
        log.debug("Static resources fetched - Roles: {}, Permissions: {}, Organizations: {}, Menus: {}", 
                roles.size(), permissions.size(), organizations.size(), menus.size());
        
        return response;
    }

    public List<RoleResponse> getAllRoles() {
        log.debug("Fetching all roles");
        
        List<Role> roles = roleMapper.selectAllRoles();
        return roles.stream()
                .map(role -> new RoleResponse(role))
                .collect(Collectors.toList());
    }

    public List<PermissionResponse> getAllPermissions() {
        log.debug("Fetching all permissions");
        
        List<Permission> permissions = permissionMapper.selectAllPermissions();
        return permissions.stream()
                .map(permission -> new PermissionResponse(permission))
                .collect(Collectors.toList());
    }

    public List<OrganizationResponse> getAllOrganizations() {
        log.debug("Fetching all organizations");
        
        List<Organization> organizations = organizationMapper.selectAllOrganizations();
        return organizations.stream()
                .map(organization -> new OrganizationResponse(organization))
                .collect(Collectors.toList());
    }

    public List<MenuResponse> getAllMenus() {
        log.debug("Fetching all menus");
        
        List<Menu> menus = menuMapper.selectAllMenus();
        return menus.stream()
                .map(menu -> new MenuResponse(menu))
                .collect(Collectors.toList());
    }

    public Map<String, List<StaticResourceResponse.CodeItem>> getCommonCodes() {
        log.debug("Fetching common codes");
        
        Map<String, List<StaticResourceResponse.CodeItem>> commonCodes = new HashMap<>();
        
        // 사용자 상태 코드
        List<StaticResourceResponse.CodeItem> userStatusCodes = new ArrayList<>();
        userStatusCodes.add(new StaticResourceResponse.CodeItem(User.STATUS_ACTIVE, "활성", "활성 상태의 사용자", 1));
        userStatusCodes.add(new StaticResourceResponse.CodeItem(User.STATUS_INACTIVE, "비활성", "비활성 상태의 사용자", 2));
        userStatusCodes.add(new StaticResourceResponse.CodeItem(User.STATUS_LOCKED, "잠김", "계정이 잠긴 사용자", 3));
        commonCodes.put("USER_STATUS", userStatusCodes);
        
        // 메뉴 타입 코드
        List<StaticResourceResponse.CodeItem> menuTypeCodes = new ArrayList<>();
        menuTypeCodes.add(new StaticResourceResponse.CodeItem("MENU", "메뉴", "일반 메뉴", 1));
        menuTypeCodes.add(new StaticResourceResponse.CodeItem("PAGE", "페이지", "페이지 메뉴", 2));
        menuTypeCodes.add(new StaticResourceResponse.CodeItem("FOLDER", "폴더", "폴더형 메뉴", 3));
        menuTypeCodes.add(new StaticResourceResponse.CodeItem("LINK", "링크", "외부 링크", 4));
        commonCodes.put("MENU_TYPE", menuTypeCodes);
        
        // 정렬 방향 코드
        List<StaticResourceResponse.CodeItem> sortDirectionCodes = new ArrayList<>();
        sortDirectionCodes.add(new StaticResourceResponse.CodeItem("ASC", "오름차순", "오름차순 정렬", 1));
        sortDirectionCodes.add(new StaticResourceResponse.CodeItem("DESC", "내림차순", "내림차순 정렬", 2));
        commonCodes.put("SORT_DIRECTION", sortDirectionCodes);
        
        // 활성 상태 코드
        List<StaticResourceResponse.CodeItem> activeStatusCodes = new ArrayList<>();
        activeStatusCodes.add(new StaticResourceResponse.CodeItem("true", "활성", "활성 상태", 1));
        activeStatusCodes.add(new StaticResourceResponse.CodeItem("false", "비활성", "비활성 상태", 2));
        commonCodes.put("ACTIVE_STATUS", activeStatusCodes);
        
        return commonCodes;
    }
}
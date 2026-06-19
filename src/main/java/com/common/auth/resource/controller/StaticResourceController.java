package com.common.auth.resource.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.menu.dto.MenuResponse;
import com.common.auth.organization.dto.OrganizationResponse;
import com.common.auth.permission.dto.PermissionResponse;
import com.common.auth.resource.dto.StaticResourceResponse;
import com.common.auth.resource.service.StaticResourceService;
import com.common.auth.role.dto.RoleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/static-resources")
@Tag(name = "Static Resource Management", description = "정적 리소스 관리 API")
@RequiredArgsConstructor
public class StaticResourceController {
    private final StaticResourceService staticResourceService;

    @GetMapping
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'resource', 'read')")
    @Operation(summary = "모든 정적 리소스 조회", description = "모든 정적 리소스(역할, 권한, 조직, 메뉴, 공통코드)를 한번에 조회합니다. (권한: admin 이상 resource:read)")
    public ResponseEntity<ApiResponse<StaticResourceResponse>> getAllStaticResources() {
        
        log.info("Fetching all static resources");
        
        StaticResourceResponse response = staticResourceService.getAllStaticResources();
        
        log.info("Static resources fetched successfully");
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/roles")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'role', 'read')")
    @Operation(summary = "모든 역할 조회", description = "시스템의 모든 역할을 조회합니다. (권한: admin 이상 role:read)")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        
        log.info("Fetching all roles");
        
        List<RoleResponse> roles = staticResourceService.getAllRoles();
        
        log.info("Roles fetched successfully: {} items", roles.size());
        
        return ResponseEntity.ok(ApiResponse.success(roles));
    }

    @GetMapping("/permissions")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'permission', 'read')")
    @Operation(summary = "모든 권한 조회", description = "시스템의 모든 권한을 조회합니다. (권한: admin 이상 permission:read)")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        
        log.info("Fetching all permissions");
        
        List<PermissionResponse> permissions = staticResourceService.getAllPermissions();
        
        log.info("Permissions fetched successfully: {} items", permissions.size());
        
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/organizations")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'organization', 'read')")
    @Operation(summary = "모든 조직 조회", description = "시스템의 모든 조직을 조회합니다. (권한: admin 이상 organization:read)")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getAllOrganizations() {
        
        log.info("Fetching all organizations");
        
        List<OrganizationResponse> organizations = staticResourceService.getAllOrganizations();
        
        log.info("Organizations fetched successfully: {} items", organizations.size());
        
        return ResponseEntity.ok(ApiResponse.success(organizations));
    }

    @GetMapping("/menus")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'menu', 'read')")
    @Operation(summary = "모든 메뉴 조회", description = "시스템의 모든 메뉴를 조회합니다. (권한: admin 이상 menu:read)")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getAllMenus() {
        
        log.info("Fetching all menus");
        
        List<MenuResponse> menus = staticResourceService.getAllMenus();
        
        log.info("Menus fetched successfully: {} items", menus.size());
        
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    @GetMapping("/common-codes")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'code', 'read')")
    @Operation(summary = "공통 코드 조회", description = "시스템의 모든 공통 코드를 조회합니다. (권한: admin 이상 code:read)")
    public ResponseEntity<ApiResponse<Map<String, List<StaticResourceResponse.CodeItem>>>> getCommonCodes() {
        
        log.info("Fetching common codes");
        
        Map<String, List<StaticResourceResponse.CodeItem>> commonCodes = staticResourceService.getCommonCodes();
        
        log.info("Common codes fetched successfully: {} categories", commonCodes.size());
        
        return ResponseEntity.ok(ApiResponse.success(commonCodes));
    }
}
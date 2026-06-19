package com.common.auth.resource.controller;

import java.util.List;


import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.menu.dto.MenuResponse;
import com.common.auth.resource.dto.DynamicResourceResponse;
import com.common.auth.resource.service.DynamicResourceService;
import com.common.auth.role.dto.RoleResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/dynamic-resources")
@Tag(name = "Dynamic Resource Management", description = "동적 리소스 관리 API")
@RequiredArgsConstructor
public class DynamicResourceController {
    private final DynamicResourceService dynamicResourceService;

    @GetMapping("/users/{memberId}")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'user', 'read')")
    @Operation(summary = "사용자별 동적 리소스 조회", description = "특정 사용자의 모든 동적 리소스(역할, 권한, 메뉴)를 조회합니다. (권한: admin 이상 user:read)")
    public ResponseEntity<ApiResponse<DynamicResourceResponse>> getUserResources(
            @Parameter(description = "사용자 ID", required = true, in = ParameterIn.PATH)
            @PathVariable("memberId") Integer memberId) {
        
        log.info("Fetching user resources for userId: {}", memberId);
        
        DynamicResourceResponse response = dynamicResourceService.getUserResources(memberId);
        
        log.info("User resources fetched successfully for userId: {}", memberId);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/users/{memberId}/roles")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'user', 'read') or #memberId == authentication.principal")
    @Operation(summary = "사용자별 역할 조회", description = "특정 사용자의 역할 목록을 조회합니다. (권한: admin 이상 user:read 또는 자신의 정보)")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getUserRoles(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable("memberId") Integer memberId) {
        
        log.info("Fetching user roles for userId: {}", memberId);
        
        List<RoleResponse> roles = dynamicResourceService.getUserRoles(memberId);
        
        log.info("User roles fetched successfully for userId: {}, count: {}", memberId, roles.size());
        
        return ResponseEntity.ok(ApiResponse.success(roles));
    }
    
    @GetMapping("/users/{memberId}/permissions")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'user', 'read') or #memberId == authentication.principal")
    @Operation(summary = "사용자별 권한 조회", description = "특정 사용자의 권한 목록을 조회합니다. (권한: admin 이상 user:read 또는 자신의 정보)")
    public ResponseEntity<ApiResponse<List<String>>> getUserPermissions(
            @Parameter(description = "사용자 ID", required = true, in = ParameterIn.PATH)
            @PathVariable("memberId") Integer memberId) {
        
        log.info("Fetching user permissions for userId: {}", memberId);
        
        List<String> permissions = dynamicResourceService.getUserPermissions(memberId);
        
        log.info("User permissions fetched successfully for userId: {}, count: {}", memberId, permissions.size());
        
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }

    @GetMapping("/users/{memberId}/menus")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'user', 'read') or #memberId == authentication.principal")
    @Operation(summary = "사용자별 메뉴 조회", description = "특정 사용자의 메뉴 목록을 조회합니다. (권한: admin 이상 user:read 또는 자신의 정보)")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getUserMenus(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable("memberId") Integer memberId) {
        
        log.info("Fetching user menus for userId: {}", memberId);
        
        List<MenuResponse> menus = dynamicResourceService.getUserMenus(memberId);
        
        log.info("User menus fetched successfully for userId: {}, count: {}", memberId, menus.size());
        
        return ResponseEntity.ok(ApiResponse.success(menus));
    }

    @GetMapping("/users/{memberId}/menu-tree")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'user', 'read') or #memberId == authentication.principal")
    @Operation(summary = "사용자별 메뉴 트리 조회", description = "특정 사용자의 계층형 메뉴 트리를 조회합니다. (권한: admin 이상 user:read 또는 자신의 정보)")
    public ResponseEntity<ApiResponse<List<DynamicResourceResponse.MenuTreeNode>>> getUserMenuTree(
            @Parameter(description = "사용자 ID", required = true, in = ParameterIn.PATH)
            @PathVariable("memberId") Integer memberId) {
        
        log.info("Fetching user menu tree for userId: {}", memberId);
        
        List<DynamicResourceResponse.MenuTreeNode> menuTree = dynamicResourceService.getUserMenuTree(memberId);
        
        log.info("User menu tree fetched successfully for userId: {}, root nodes: {}", memberId, menuTree.size());
        
        return ResponseEntity.ok(ApiResponse.success(menuTree));
    }
}
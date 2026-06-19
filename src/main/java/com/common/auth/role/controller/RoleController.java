package com.common.auth.role.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.role.dto.RoleResponse;
import com.common.auth.role.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "역할 관리 API")
public class RoleController {
    //----- DI Fields -----//
    private final RoleService roleService;
    
    /**
     * 역할 목록 조회
     */
    @GetMapping
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'role', 'read')")
    @Operation(summary = "역할 목록 조회", description = "전체 역할 목록을 조회합니다. (권한: admin 이상 role:read)")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        
        log.debug("Getting all roles");
        
        List<RoleResponse> roles = roleService.getAllRoles();
        
        return ResponseEntity.ok(ApiResponse.success(roles));
    }
}
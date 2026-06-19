package com.common.auth.permission.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.permission.dto.PermissionResponse;
import com.common.auth.permission.service.PermissionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/permission")
@Tag(name = "Permission Management", description = "권한 관리 API")
@RequiredArgsConstructor
public class PermissionController {
    //----- DI Fields -----//
    private final PermissionService permissionService;

    /**
     * 권한 목록 조회
     */
    @GetMapping
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'permission', 'read')")
    @Operation(summary = "권한 목록 조회", description = "전체 권한 목록을 조회합니다. (권한: admin 이상 permission:read)")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        
        log.debug("Getting all permissions");
        
        List<PermissionResponse> permissions = permissionService.getAllPermissions();
        
        return ResponseEntity.ok(ApiResponse.success(permissions));
    }
}
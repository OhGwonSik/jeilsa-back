package com.common.auth.rolepermission.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.rolepermission.dto.RolePermissionGridFilterDTO;
import com.common.auth.rolepermission.dto.RolePermissionGridUpsertRequest;
import com.common.auth.rolepermission.dto.RolePermissionResponse;
import com.common.auth.rolepermission.service.RolePermissionGridService;
import com.github.pagehelper.PageInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/r" +
        "ole-permission/grid")
@Tag(name = "Role Permission Grid Management", description = "역할-권한 매핑 그리드 관리 API")
@RequiredArgsConstructor
public class RolePermissionGridController {
    //----- DI Fields -----/
    private final RolePermissionGridService rolePermissionGridService;
    
    //----- Methods -----/
    @PostMapping("/search")
    @PreAuthorize("@permissionHelper.hasMultiDomain('admin', 'role:read', 'permission:read')")
    @Operation(summary = "역할-권한 매핑 조건별 조회", description = "필터, 정렬, 페이징 조건으로 역할-권한 매핑을 조회합니다. (권한: admin 이상 role:read,permission:read)")
    public ResponseEntity<ApiResponse<PageInfo<RolePermissionResponse>>> selectRolePermissionsWithFilter(
            @Parameter(description = "검색 조건", required = true)
            @Valid @RequestBody RolePermissionGridFilterDTO filter) {
        
        log.info("Searching role permissions with filter: {}", filter);
        
        PageInfo<RolePermissionResponse> response = rolePermissionGridService.selectRolePermissionsWithFilter(filter);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/bulk/upsert")
    @PreAuthorize("@permissionHelper.hasMultiDomain('admin', 'role:read', 'role:create', 'role:update', 'role:delete', 'permission:read', 'permission:create', 'permission:update', 'permission:delete')")
    @Operation(summary = "역할-권한 매핑 일괄 upsert", description = "여러 역할-권한 매핑을 일괄로 생성/수정합니다. (권한: admin 이상 role:read,create,update,delete 및 permission:read,create,update,delete)")
    public ResponseEntity<ApiResponse<OperationResult>> upsertRolePermissionsBulk(
            @Parameter(description = "일괄 upsert 요청", required = true)
            @Valid @RequestBody RolePermissionGridUpsertRequest request) {
        
        log.info("Bulk upsert role permissions: {} items", request.getItems().size());
        
        OperationResult result = rolePermissionGridService.upsertRolePermissionsBulk(request);
        
        log.info("Bulk upsert completed: {}", result);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
package com.common.auth.role.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.role.dto.RoleGridFilterDTO;
import com.common.auth.role.dto.RoleGridUpsertRequest;
import com.common.auth.role.dto.RoleResponse;
import com.common.auth.role.service.RoleGridService;
import com.github.pagehelper.PageInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/role/grid")
@RequiredArgsConstructor
@Tag(name = "Role Grid Management", description = "역할 그리드 관리 API")
public class RoleGridController {
    //----- DI Fields -----//
    private final RoleGridService roleGridService;

    @PostMapping("/search")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'role', 'read')")
    @Operation(summary = "역할 조건별 조회", description = "필터, 정렬, 페이징 조건으로 역할을 조회합니다. (권한: admin 이상 role:read)")
    public ResponseEntity<ApiResponse<PageInfo<RoleResponse>>> searchRoles(
            @Parameter(description = "검색 조건", required = true)
            @Valid @RequestBody RoleGridFilterDTO filter) {
        
        log.info("Searching roles with filter: {}", filter);
        
        PageInfo<RoleResponse> response = roleGridService.selectRolesWithFilter(filter);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/bulk/upsert")
    @PreAuthorize("@permissionHelper.hasAllMinLevel('admin', 'role', 'read', 'create', 'update', 'delete')")
    @Operation(summary = "역할 일괄 upsert", description = "여러 역할을 일괄로 생성/수정합니다. (권한: admin 이상 role:read,create,update,delete)")
    public ResponseEntity<ApiResponse<OperationResult>> upsertRolesBulk(
            @Parameter(description = "일괄 upsert 요청", required = true)
            @Valid @RequestBody RoleGridUpsertRequest request) {
        
        log.info("Bulk upsert roles: {} items", request.getItems().size());
        
        OperationResult result = roleGridService.upsertRolesBulk(request);
        
        log.info("Bulk upsert completed: {}", result);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
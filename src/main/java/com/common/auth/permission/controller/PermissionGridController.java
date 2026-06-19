package com.common.auth.permission.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.permission.dto.PermissionGridFilterDTO;
import com.common.auth.permission.dto.PermissionGridUpsertRequest;
import com.common.auth.permission.dto.PermissionResponse;
import com.common.auth.permission.service.PermissionGridService;
import com.github.pagehelper.PageInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/permission/grid")
@RequiredArgsConstructor
@Tag(name = "Permission Grid Management", description = "권한 그리드 관리 API")
public class PermissionGridController {
    //----- DI Fields -----//
    private final PermissionGridService permissionGridService;

    @PostMapping("/search")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'permission', 'read')")
    @Operation(summary = "권한 조건별 조회", description = "필터, 정렬, 페이징 조건으로 권한을 조회합니다. (권한: admin 이상 permission:read)")
    public ResponseEntity<ApiResponse<PageInfo<PermissionResponse>>> searchPermissions(
            @Parameter(description = "검색 조건", required = true)
            @Valid @RequestBody PermissionGridFilterDTO filter) {
        
        log.info("Searching permissions with filter: {}", filter);
        
        PageInfo<PermissionResponse> response = permissionGridService.selectPermissionsWithFilter(filter);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/bulk/upsert")
    @PreAuthorize("@permissionHelper.hasAllMinLevel('admin', 'permission', 'read', 'create', 'update', 'delete')")
    @Operation(summary = "권한 일괄 upsert", description = "여러 권한을 일괄로 생성/수정합니다. (권한: admin 이상 permission:read,create,update,delete)")
    public ResponseEntity<ApiResponse<OperationResult>> upsertPermissionsBulk(
            @Parameter(description = "일괄 upsert 요청", required = true)
            @Valid @RequestBody PermissionGridUpsertRequest request) {
        
        log.info("Bulk upsert permissions: {} items", request.getItems().size());
        
        OperationResult result = permissionGridService.upsertPermissionsBulk(request);
        
        log.info("Bulk upsert completed: {}", result);  
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

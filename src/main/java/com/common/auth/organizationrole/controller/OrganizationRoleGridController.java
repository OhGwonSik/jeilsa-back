package com.common.auth.organizationrole.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.organizationrole.dto.OrganizationRoleGridFilterDTO;
import com.common.auth.organizationrole.dto.OrganizationRoleGridUpsertRequest;
import com.common.auth.organizationrole.dto.OrganizationRoleResponse;
import com.common.auth.organizationrole.service.OrganizationRoleGridService;
import com.github.pagehelper.PageInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/organization-role/grid")
@RequiredArgsConstructor
@Tag(name = "Organization Role Grid Management", description = "부서-역할 그리드 관리 API")
public class OrganizationRoleGridController {
    //----- DI Fields -----/
    private final OrganizationRoleGridService organizationRoleGridService;
    
    //----- Methods -----/
    @PostMapping("/search")
    @PreAuthorize("@permissionHelper.hasMultiDomain('admin', 'organization:read', 'role:read')")
    @Operation(summary = "부서-역할 매핑 조건별 조회", description = "필터, 정렬, 페이징 조건으로 부서-역할 매핑을 조회합니다. (권한: admin 이상 organization:read,role:read)")
    public ResponseEntity<ApiResponse<PageInfo<OrganizationRoleResponse>>> searchOrganizationRolesWithFilter(
            @Parameter(description = "검색 조건", required = true)
            @Valid @RequestBody OrganizationRoleGridFilterDTO filter) {
        
        log.info("Searching organization roles with filter: {}", filter);
        PageInfo<OrganizationRoleResponse> response = organizationRoleGridService.selectOrganizationRolesWithFilter(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/bulk/upsert")
    @PreAuthorize("@permissionHelper.hasMultiDomain('admin', 'organization:read', 'organization:create', 'organization:update', 'organization:delete', 'role:read', 'role:create', 'role:update', 'role:delete')")
    @Operation(summary = "부서-역할 매핑 일괄 upsert", description = "여러 부서-역할 매핑을 일괄로 생성/수정합니다. (권한: admin 이상 organization:CRUD + role:CRUD)")
    public ResponseEntity<ApiResponse<OperationResult>> upsertOrganizationRoles(
            @Parameter(description = "일괄 upsert 요청", required = true)
            @Valid @RequestBody OrganizationRoleGridUpsertRequest request) {
        
        log.info("Bulk upsert organization roles: {} items", request.getItems().size());
        
        OperationResult result = organizationRoleGridService.upsertOrganizationRolesBulk(request);
        
        log.info("Bulk upsert completed: {}", result);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

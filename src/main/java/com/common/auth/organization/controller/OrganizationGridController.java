package com.common.auth.organization.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.organization.dto.OrganizationGridFilterDTO;
import com.common.auth.organization.dto.OrganizationGridUpsertRequest;
import com.common.auth.organization.dto.OrganizationResponse;
import com.common.auth.organization.service.OrganizationGridService;
import com.github.pagehelper.PageInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/organization/grid")
@Tag(name = "Organization Grid Management", description = "조직 그리드 관리 API")
@RequiredArgsConstructor
public class OrganizationGridController {
    //----- DI Fields -----//
    private final OrganizationGridService organizationGridService;
    
    @PostMapping("/search")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'organization', 'read')")
    @Operation(summary = "조직 조건별 조회", description = "필터, 정렬, 페이징 조건으로 조직을 조회합니다. (권한: admin 이상 organization:read)")
    public ResponseEntity<ApiResponse<PageInfo<OrganizationResponse>>> selectOrganizationsWithFilter(
            @Parameter(description = "검색 조건", required = true)
            @Valid @RequestBody OrganizationGridFilterDTO filter) {
        
        log.info("Searching organizations with filter: {}", filter);
        
        PageInfo<OrganizationResponse> response = organizationGridService.selectOrganizationsWithFilter(filter);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/bulk/upsert")
    @PreAuthorize("@permissionHelper.hasAllMinLevel('admin', 'organization', 'create', 'update', 'delete')")
    @Operation(summary = "조직 일괄 upsert", description = "여러 조직을 일괄로 생성/수정합니다. (권한: admin 이상 organization:create,update,delete)")
    public ResponseEntity<ApiResponse<OperationResult>> upsertOrganizationsBulk(
            @Parameter(description = "일괄 upsert 요청", required = true)
            @Valid @RequestBody OrganizationGridUpsertRequest request) {
        
        log.info("Bulk upsert organizations: {} items", request.getItems().size());
        
        OperationResult result = organizationGridService.upsertOrganizationsBulk(request);
        
        log.info("Bulk upsert completed: {}", result);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
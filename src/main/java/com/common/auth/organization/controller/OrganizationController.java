package com.common.auth.organization.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.organization.dto.OrganizationResponse;
import com.common.auth.organization.service.OrganizationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/organization")
@Tag(name = "Organization Management", description = "부서 관리 API")
@RequiredArgsConstructor
public class OrganizationController {
    //----- DI Fields -----//
    private final OrganizationService organizationService;

    /**
     * 부서 목록 조회
     */
    @GetMapping
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'organization', 'read')")
    @Operation(summary = "부서 목록 조회", description = "전체 부서 목록을 조회합니다. (권한: admin 이상 organization:read)")
    public ResponseEntity<ApiResponse<List<OrganizationResponse>>> getAllOrganizations() {
        
        log.debug("Getting all organizations");
        
        List<OrganizationResponse> organizations = organizationService.getAllOrganizations();
        
        return ResponseEntity.ok(ApiResponse.success(organizations));
    }
}
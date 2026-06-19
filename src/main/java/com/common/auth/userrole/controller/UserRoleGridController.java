package com.common.auth.userrole.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.userrole.dto.UserRoleGridFilterDTO;
import com.common.auth.userrole.dto.UserRoleGridUpsertRequest;
import com.common.auth.userrole.dto.UserRoleResponse;
import com.common.auth.userrole.service.UserRoleGridService;
import com.github.pagehelper.PageInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user-role/grid") 
@Tag(name = "User Role Grid Management", description = "유저-역할 그리드 관리 API")
@RequiredArgsConstructor
public class UserRoleGridController {
    //----- DI Fields -----/
    private final UserRoleGridService userRoleGridService;
    
    //----- Methods -----/
    @PostMapping("/search")
    @PreAuthorize("@permissionHelper.hasMultiDomain('admin', 'user:read', 'role:read')")
    @Operation(summary = "유저-역할 매핑 조건별 조회", description = "필터, 정렬, 페이징 조건으로 유저-역할 매핑을 조회합니다. (권한: admin 이상 user:read,role:read)")
    public ResponseEntity<ApiResponse<PageInfo<UserRoleResponse>>> searchUserRoles(
            @Parameter(description = "검색 조건", required = true)
            @Valid @RequestBody UserRoleGridFilterDTO filter) {
        
        log.info("Searching user roles with filter: {}", filter);
        
        PageInfo<UserRoleResponse> response = userRoleGridService.selectUserRolesWithFilter(filter);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/bulk/upsert")
    @PreAuthorize("@permissionHelper.hasMultiDomain('admin', 'user:read', 'user:create', 'user:update', 'user:delete', 'role:read', 'role:create', 'role:update', 'role:delete')")
    @Operation(summary = "유저-역할 매핑 일괄 upsert", description = "여러 유저-역할 매핑을 일괄로 생성/수정합니다. (권한: admin 이상 user:read,create,update,delete 및 role:read,create,update,delete)")
    public ResponseEntity<ApiResponse<OperationResult>> upsertUserRolesBulk(
            @Parameter(description = "일괄 upsert 요청", required = true)
            @Valid @RequestBody UserRoleGridUpsertRequest request) {
        OperationResult result = userRoleGridService.upsertUserRolesBulk(request);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

package com.common.auth.rolemenu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.rolemenu.dto.RoleMenuGridFilterDTO;
import com.common.auth.rolemenu.dto.RoleMenuGridUpsertRequest;
import com.common.auth.rolemenu.dto.RoleMenuResponse;
import com.common.auth.rolemenu.service.RoleMenuGridService;
import com.github.pagehelper.PageInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/role-menu/grid")
@Tag(name = "Role Menu Grid Management", description = "롤-메뉴 그리드 관리 API")
@RequiredArgsConstructor
public class RoleMenuGridController {
    //----- DI Fields -----//
    private final RoleMenuGridService roleMenuGridService;

    @PostMapping("/search")
    @PreAuthorize("@permissionHelper.hasMultiDomain('admin', 'role:read', 'menu:read')")
    @Operation(summary = "롤-메뉴 매핑 조건별 조회", description = "필터, 정렬, 페이징 조건으로 롤-메뉴 매핑을 조회합니다. (권한: admin 이상 role:read,menu:read)")
    public ResponseEntity<ApiResponse<PageInfo<RoleMenuResponse>>> searchRoleMenus(
            @Parameter(description = "검색 조건", required = true)
            @Valid @RequestBody RoleMenuGridFilterDTO filter) {
        
        log.info("Searching role menus with filter: {}", filter);
        PageInfo<RoleMenuResponse> response = roleMenuGridService.selectRoleMenusWithFilter(filter);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/bulk/upsert")
    @PreAuthorize("@permissionHelper.hasMultiDomain('admin', 'role:read', 'role:create', 'role:update', 'role:delete', 'menu:read', 'menu:create', 'menu:update', 'menu:delete')")
    @Operation(summary = "롤-메뉴 매핑 일괄 upsert", description = "여러 롤-메뉴 매핑을 일괄로 생성/수정합니다. (권한: admin 이상 role:read,create,update,delete 및 menu:read,create,update,delete)")
    public ResponseEntity<ApiResponse<OperationResult>> upsertRoleMenusBulk(
            @Parameter(description = "일괄 upsert 요청", required = true)
            @Valid @RequestBody RoleMenuGridUpsertRequest request) {
        
        log.info("Bulk upsert role menus: {} items", request.getItems().size());
        
        OperationResult result = roleMenuGridService.upsertRoleMenusBulk(request);
        
        log.info("Bulk upsert completed: {}", result);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}

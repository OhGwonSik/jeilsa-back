package com.common.auth.menu.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.menu.dto.MenuGridFilterDTO;
import com.common.auth.menu.dto.MenuGridUpsertRequest;
import com.common.auth.menu.dto.MenuResponse;
import com.common.auth.menu.service.MenuGridService;
import com.github.pagehelper.PageInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/menu/grid")
@Tag(name = "Menu Grid Management", description = "메뉴 그리드 관리 API")
@RequiredArgsConstructor
public class MenuGridController {
    //----- DI Fields -----//
    private final MenuGridService menuGridService;

    @PostMapping("/search")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'menu', 'read')")
    @Operation(summary = "메뉴 조건별 조회", description = "필터, 정렬, 페이징 조건으로 메뉴를 조회합니다. (권한: admin 이상 menu:read)")
    public ResponseEntity<ApiResponse<PageInfo<MenuResponse>>> selectMenusWithFilter(
            @Parameter(description = "검색 조건", required = true)
            @Valid @RequestBody MenuGridFilterDTO filter) {
        
        log.info("Searching menus with filter: {}", filter);
        
        PageInfo<MenuResponse> response = menuGridService.selectMenusWithFilter(filter);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/bulk/upsert")
    @PreAuthorize("@permissionHelper.hasAllMinLevel('admin', 'menu', 'read', 'create', 'update', 'delete')")
    @Operation(summary = "메뉴 일괄 upsert", description = "여러 메뉴를 일괄로 생성/수정합니다. (권한: admin 이상 menu:read,create,update,delete)")
    public ResponseEntity<ApiResponse<OperationResult>> upsertMenusBulk(
            @Parameter(description = "일괄 upsert 요청", required = true)
            @Valid @RequestBody MenuGridUpsertRequest request) {
        
        log.info("Bulk upsert menus: {} items", request.getItems().size());
        
        OperationResult result = menuGridService.upsertMenusBulk(request);
        
        log.info("Bulk upsert completed: {}", result);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
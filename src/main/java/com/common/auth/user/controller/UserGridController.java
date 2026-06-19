package com.common.auth.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.user.dto.UserGridFilterDTO;
import com.common.auth.user.dto.UserGridUpsertRequest;
import com.common.auth.user.dto.UserResponse;
import com.common.auth.user.service.UserGridService;
import com.github.pagehelper.PageInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user")
// @RequestMapping("/user/grid")
@Tag(name = "User Grid Management", description = "사용자 그리드 관리 API")
@RequiredArgsConstructor
public class UserGridController {
    //----- DI Fields -----//
    private final UserGridService userGridService;

    @PostMapping("/bulk/upsert")
    @PreAuthorize("@permissionHelper.hasAllMinLevel('admin', 'user', 'read', 'create', 'update', 'delete')")
    @Operation(summary = "사용자 일괄 upsert", description = "여러 사용자를 일괄로 생성/수정합니다. (권한: admin 이상 user:read,create,update,delete)")
    public ResponseEntity<ApiResponse<OperationResult>> upsertUsersBulk(
            @Parameter(description = "일괄 upsert 요청", required = true)
            @Valid @RequestBody UserGridUpsertRequest request) {
        
        log.info("Bulk upsert users: {} items", request.getItems().size());
        
        OperationResult result = userGridService.upsertUsersBulk(request);
        
        log.info("Bulk upsert completed: {}", result);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
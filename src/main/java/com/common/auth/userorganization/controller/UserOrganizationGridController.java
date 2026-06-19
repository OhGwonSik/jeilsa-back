package com.common.auth.userorganization.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.userorganization.dto.UserOrganizationGridFilterDTO;
import com.common.auth.userorganization.dto.UserOrganizationGridUpsertRequest;
import com.common.auth.userorganization.dto.UserOrganizationResponse;
import com.common.auth.userorganization.service.UserOrganizationGridService;
import com.github.pagehelper.PageInfo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@RestController
@RequestMapping("/user-organization/grid")
@Tag(name = "User Organization Grid Management", description = "유저-부서 그리드 관리 API")
@RequiredArgsConstructor
public class UserOrganizationGridController {
    //----- DI Fields -----/
    private final UserOrganizationGridService userOrganizationGridService;
    
    //----- Methods -----/
    @PostMapping("/search")
    @PreAuthorize("@permissionHelper.hasMultiDomain('admin', 'user:read', 'organization:read')")
    @Operation(summary = "유저-부서 매핑 조건별 조회", description = "필터, 정렬, 페이징 조건으로 유저-부서 매핑을 조회합니다. (권한: admin 이상 user:read,organization:read)")
  public ResponseEntity<ApiResponse<PageInfo<UserOrganizationResponse>>> selectUserOrganizationsWithFilter(
           @Parameter(description = "검색 조건", required = true)
           @Valid @RequestBody UserOrganizationGridFilterDTO filter) {

      log.info("Searching user organizations with filter: {}", filter);
      PageInfo<UserOrganizationResponse> response = userOrganizationGridService.selectUserOrganizationsWithFilter(filter);

      return ResponseEntity.ok(ApiResponse.success(response));
  }

    @PostMapping("/bulk/upsert")
    @PreAuthorize("@permissionHelper.hasMultiDomain('admin', 'user:read', 'user:create', 'user:update', 'user:delete', 'organization:read', 'organization:create', 'organization:update', 'organization:delete')")
    @Operation(summary = "유저-부서 매핑 일괄 upsert", description = "여러 유저-부서 매핑을 일괄로 생성/수정합니다. (권한: admin 이상 user:read,create,update,delete 및 organization:read,create,update,delete)")
    public ResponseEntity<ApiResponse<OperationResult>> upsertUserOrganizationsBulk(
            @Parameter(description = "일괄 upsert 요청", required = true)
            @Valid @RequestBody UserOrganizationGridUpsertRequest request) {
        
        log.info("Bulk upsert user organizations: {} items", request.getItems().size());
        
        OperationResult result = userOrganizationGridService.upsertUserOrganizationsBulk(request);
        
        log.info("Bulk upsert completed: {}", result);
        
        return ResponseEntity.ok(ApiResponse.success(result));
    } 
}

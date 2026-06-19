package com.jeil.delivery.controller;

import com.common.auth.common.dto.ApiResponse;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.user.dto.UserGridFilterDTO;
import com.common.auth.user.dto.UserResponse;
import com.common.auth.userrole.dto.UserRoleGridFilterDTO;
import com.common.auth.userrole.dto.UserRoleGridUpsertRequest;
import com.common.auth.userrole.dto.UserRoleResponse;
import com.github.pagehelper.PageInfo;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/list")
    @PreAuthorize("@permissionHelper.hasMinLevel('user', 'member', 'read')")
    public List<MemberDTO> selectMemberList(MemberDTO memberDTO) {
        return memberService.selectMemberList(memberDTO);
    }

    @PostMapping("/insert")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner', 'member', 'create')")
    public int insertMember(@RequestBody MemberDTO memberDTO) {
        return memberService.insertMember(memberDTO);
    }

    @PutMapping("/update")
    @PreAuthorize("@permissionHelper.hasMinLevel('user', 'member', 'update')")
    public int updateMember(@RequestBody MemberDTO memberDTO) {
        return memberService.updateMember(memberDTO);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner', 'member', 'delete')")
    public int deleteMember(@PathVariable("id") int id) {
        MemberDTO dto = new MemberDTO();
        dto.setMemberId(id);
        return memberService.deleteMember(dto);
    }

    // 유저 아이디 중복확인
    @GetMapping("/userId/check/{userId}")
    @PreAuthorize("@permissionHelper.hasMinLevel('owner', 'member', 'read')")
    public boolean checkUserId(@PathVariable("userId") String userId) {
        return memberService.checkUserId(userId);
    }

    @PostMapping("/search")
    @PreAuthorize("@permissionHelper.hasMinLevel('admin', 'user', 'read')")
    @Operation(summary = "사용자 조건별 조회", description = "필터, 정렬, 페이징 조건으로 사용자를 조회합니다. (권한: admin 이상 user:read)")
    public ResponseEntity<ApiResponse<PageInfo<UserResponse>>> selectUsersWithFilter(
            @Parameter(description = "검색 조건", required = true)
            @Valid @RequestBody UserGridFilterDTO filter) {
        PageInfo<UserResponse> response = memberService.selectUsersWithFilter(filter);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/role/grid/search")
    @PreAuthorize("@permissionHelper.hasMultiDomain('admin', 'user:read', 'role:read')")
    @Operation(summary = "유저-역할 매핑 조건별 조회", description = "필터, 정렬, 페이징 조건으로 유저-역할 매핑을 조회합니다. (권한: admin 이상 user:read,role:read)")
    public ResponseEntity<ApiResponse<PageInfo<UserRoleResponse>>> searchUserRoles(
            @Parameter(description = "검색 조건", required = true)
            @Valid @RequestBody UserRoleGridFilterDTO filter) {
        PageInfo<UserRoleResponse> response = memberService.selectUserRolesWithFilter(filter);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/role/grid/upsert")
    @PreAuthorize("@permissionHelper.hasMultiDomain('admin', 'user:read', 'user:create', 'user:update', 'user:delete', 'role:read', 'role:create', 'role:update', 'role:delete')")
    @Operation(summary = "유저-역할 매핑 일괄 upsert", description = "여러 유저-역할 매핑을 일괄로 생성/수정합니다. (권한: admin 이상 user:read,create,update,delete 및 role:read,create,update,delete)")
    public ResponseEntity<ApiResponse<OperationResult>> upsertUserRolesBulk(
            @Parameter(description = "일괄 upsert 요청", required = true)
            @Valid @RequestBody UserRoleGridUpsertRequest request) {
        OperationResult result = memberService.upsertUserRolesBulk(request);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}




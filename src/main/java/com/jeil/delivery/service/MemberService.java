package com.jeil.delivery.service;

import com.common.auth.common.annotation.AutoPaging;
import com.common.auth.common.dto.OperationOptions;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.common.enums.PermissionHierarchyLevel;
import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.role.domain.Role;
import com.common.auth.role.mapper.RoleMapper;
import com.common.auth.user.dto.UserGridFilterDTO;
import com.common.auth.user.dto.UserResponse;
import com.common.auth.userrole.domain.UserRole;
import com.common.auth.userrole.dto.UserRoleGridFilterDTO;
import com.common.auth.userrole.dto.UserRoleGridUpsertRequest;
import com.common.auth.userrole.dto.UserRoleResponse;
import com.common.auth.userrole.dto.UserRoleUpsertItem;
import com.github.pagehelper.PageInfo;
import com.jeil.delivery.domain.MemberDTO;
import com.jeil.delivery.mapper.MemberMapper;
import com.jeil.delivery.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberService {

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleMapper roleMapper;

    public List<MemberDTO> selectMemberList(MemberDTO dto) {
        // user일때 본인만 조회되어야함
        MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
        boolean isUser = PermissionHierarchyLevel.USER.getName().equals(memberDTO.getRoles());
        boolean isManager = PermissionHierarchyLevel.MANAGER.getName().equals(memberDTO.getRoles());
        if (isUser||isManager) {
            dto.setSelfOnly(true);
            dto.setMemberId(memberDTO.getMemberId()); // 로그인한 본인 id로 강제
        }
        dto.setSelfOnly(isUser);
        return memberMapper.selectMemberList(dto);
    }

    public int insertMember(MemberDTO dto) {
        Integer currentMemberId = SecurityUtil.getCurrentMemberId();
        dto.setUserPw(passwordEncoder.encode(dto.getUserPw()));
        dto.setChgId(currentMemberId);
        dto.setRegId(currentMemberId);

        memberMapper.insertMember(dto);

        Integer roleId = dto.getRoleId(); // null이면 모든 활성 역할 삭제
        applySingleRoleByBulk(dto.getMemberId(), roleId);

        return 1;
    }

    private OperationResult applySingleRoleByBulk(Integer memberId, Integer newRoleId) {
        // 1) 현재 활성 역할 -> Set<Integer>
        List<Role> currentRoles = roleMapper.selectRolesByMemberId(memberId); // ur.del_yn='N'만 가져오는 쿼리
        Set<Integer> currentSet = (currentRoles == null)
                ? Collections.emptySet()
                : currentRoles.stream().map(Role::getRoleId).filter(Objects::nonNull).collect(Collectors.toSet());

        List<UserRoleUpsertItem> items = new ArrayList<>();

        if (newRoleId != null) {
            // 나머지 활성은 모두 삭제
            for (Integer rid : currentSet) {
                if (!Objects.equals(rid, newRoleId)) {
                    UserRoleUpsertItem del = new UserRoleUpsertItem();
                    del.setTempId("del-" + rid);
                    del.setStatus("DELETED");
                    del.setMemberId(memberId);
                    del.setRoleId(rid);
                    items.add(del);
                }
            }

            if (!currentSet.contains(newRoleId)) {
                // 활성은 아니므로, 과거 상태 확인 (null | 'Y' | 'N')
                String delYn = roleMapper.selectUserRoleDelYn(memberId, newRoleId); // 단건 조회 추가

                if (delYn == null) {
                    // 완전 신규 → ADDED (INSERT)
                    UserRoleUpsertItem add = new UserRoleUpsertItem();
                    add.setTempId("add-" + newRoleId);
                    add.setStatus("ADDED");
                    add.setMemberId(memberId);
                    add.setRoleId(newRoleId);
                    items.add(add);

                } else if ("Y".equalsIgnoreCase(delYn)) {
                    // 과거에 있었지만 비활성 → CHANGED로 재활성
                    UserRoleUpsertItem react = new UserRoleUpsertItem();
                    react.setTempId("chg-" + newRoleId);
                    react.setStatus("CHANGED");
                    react.setMemberId(memberId);
                    react.setRoleId(newRoleId);
                    react.setDelYn("N"); // ★ 재활성 의도 전달
                    items.add(react);
                }
                // delYn == 'N' 이면 논리상 currentSet에 있었어야 하므로 아무 것도 안 함.
            }
        } else {
            // newRoleId == null → 현재 활성 모두 삭제
            for (Integer rid : currentSet) {
                UserRoleUpsertItem del = new UserRoleUpsertItem();
                del.setTempId("del-" + rid);
                del.setStatus("DELETED");
                del.setMemberId(memberId);
                del.setRoleId(rid);
                items.add(del);
            }
        }

        if (items.isEmpty()) {
            OperationResult ok = new OperationResult();
            ok.markComplete();
            return ok;
        }

        // 공통 호출
        OperationOptions opts = new OperationOptions(); opts.setStopOnError(true);
        UserRoleGridUpsertRequest req = new UserRoleGridUpsertRequest(); req.setOptions(opts); req.setItems(items);

        return upsertUserRolesBulk(req);
    }

    public int updateMember(MemberDTO dto) {
        MemberDTO memberDTO = SecurityUtils.getCustomUserDetails().getMemberDTO();
        // manager, user일때 본인만 수정되어야함
        boolean isUser = PermissionHierarchyLevel.USER.getName().equals(memberDTO.getRoles());
        boolean isManager = PermissionHierarchyLevel.MANAGER.getName().equals(memberDTO.getRoles());
        dto.setSelfOnly(isUser||isManager);
        dto.setChgId(memberDTO.getMemberId());
        // 비밀번호가 입력된 경우에만 암호화
        if (dto.getUserPw() != null && !dto.getUserPw().trim().isEmpty()) {
            dto.setUserPw(passwordEncoder.encode(dto.getUserPw()));
        } else {
            dto.setUserPw(null);
        }
        memberMapper.updateMember(dto);

        Integer roleId = dto.getRoleId(); // null이면 모든 활성 역할 삭제
        applySingleRoleByBulk(dto.getMemberId(), roleId);

        return 1;
    }

    public int deleteMember(MemberDTO dto) {
        Integer currentMemberId = SecurityUtil.getCurrentMemberId();
        dto.setChgId(currentMemberId);
        return memberMapper.deleteMember(dto);
    }

    public boolean checkUserId(String userId) {
        return memberMapper.checkUserId(userId);
    }

    public int selectMemberIdByUserId(String userId) {
        return memberMapper.selectMemberIdByUserId(userId);
    }

    @AutoPaging(defaultSort = "email", columnMapperMethod = "mapSortColumn")
    public PageInfo<UserResponse> selectUsersWithFilter(UserGridFilterDTO filter) {
        List<UserResponse> userResponses = memberMapper.selectUsersWithFilter(filter);

        return new PageInfo<>(userResponses);
    }

    public PageInfo<UserRoleResponse> selectUserRolesWithFilter(UserRoleGridFilterDTO filter) {
        List<UserRoleResponse> userRoleResponses = memberMapper.selectUserRolesWithFilter(filter);

        return new PageInfo<>(userRoleResponses);
    }

    public OperationResult upsertUserRolesBulk(UserRoleGridUpsertRequest request) {
        OperationResult result = new OperationResult();
        Integer currentMemberId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();
        OperationOptions userOptions = request.getOptions();

        // 요청 데이터 분류
        Map<String, List<UserRoleUpsertItem>> itemsByStatus = request.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getStatus().toUpperCase()));

        List<UserRoleUpsertItem> insertItems = itemsByStatus.getOrDefault("ADDED", Collections.emptyList());
        List<UserRoleUpsertItem> updateItems = itemsByStatus.getOrDefault("CHANGED", Collections.emptyList());
        List<UserRoleUpsertItem> deleteItems = itemsByStatus.getOrDefault("DELETED", Collections.emptyList());

        // === INSERT 검증 및 처리 ===
        if (!insertItems.isEmpty()) {
            // 중복 체크를 위해 임시 UserRole 객체들 생성
            List<UserRole> tempUserRoles = insertItems.stream()
                    .map(item -> {
                        UserRole tempUserRole = new UserRole();
                        tempUserRole.setMemberId(item.getMemberId());
                        tempUserRole.setRoleId(item.getRoleId());
                        return tempUserRole;
                    })
                    .toList();

            // 기존에 존재하는 사용자-역할 매핑 조회
            List<UserRole> existingUserRoles = memberMapper.selectExistingUserRolesByCompositeKeys(tempUserRoles);

            if (!existingUserRoles.isEmpty()) {
                // 중복된 항목들에 대해 에러 처리
                for (UserRole existing : existingUserRoles) {
                    UserRoleUpsertItem duplicateItem = insertItems.stream()
                            .filter(item -> item.getMemberId().equals(existing.getMemberId()) &&
                                    item.getRoleId().equals(existing.getRoleId()))
                            .findFirst()
                            .orElse(null);

                    if (duplicateItem != null) {
                        String errorMsg = String.format("사용자-역할 매핑이 이미 존재합니다: userId=%s, roleId=%s",
                                existing.getMemberId(), existing.getRoleId());
                        result.addError(duplicateItem.getTempId(), errorMsg, "DUPLICATE_USER_ROLE");

                        if (userOptions.isStopOnError()) {
                            throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                        }
                    }
                }

                // 중복된 항목들을 INSERT 대상에서 제외
                insertItems = insertItems.stream()
                        .filter(item -> existingUserRoles.stream()
                                .noneMatch(existing -> existing.getMemberId().equals(item.getMemberId()) &&
                                        existing.getRoleId().equals(item.getRoleId())))
                        .collect(Collectors.toList());
            }
        }

        // === UPDATE 검증 및 처리 ===
        if(!updateItems.isEmpty()){
            // 업데이트 대상이 실제로 존재하는지 검증
            List<UserRole> tempUpdateUserRoles = updateItems.stream()
                    .map(item -> {
                        UserRole tempUserRole = new UserRole();
                        tempUserRole.setMemberId(item.getMemberId());
                        tempUserRole.setRoleId(item.getRoleId());
                        return tempUserRole;
                    })
                    .toList();

            List<UserRole> existingUpdateUserRoles = memberMapper.selectExistingUserRolesByCompositeKeys(tempUpdateUserRoles);

            // 존재하지 않는 항목들에 대해 에러 처리
            for (UserRoleUpsertItem item : updateItems) {
                boolean exists = existingUpdateUserRoles.stream()
                        .anyMatch(existing -> existing.getMemberId().equals(item.getMemberId()) &&
                                existing.getRoleId().equals(item.getRoleId()));

                if (!exists) {
                    String errorMsg = String.format("업데이트할 사용자-역할 매핑이 존재하지 않습니다: userId=%s, roleId=%s",
                            item.getMemberId(), item.getRoleId());
                    result.addError(item.getTempId(), errorMsg, "UPDATE_TARGET_NOT_FOUND");

                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                    }
                }
            }

            // 존재하지 않는 항목들을 UPDATE 대상에서 제외
            updateItems = updateItems.stream()
                    .filter(item -> existingUpdateUserRoles.stream()
                            .anyMatch(existing -> existing.getMemberId().equals(item.getMemberId()) &&
                                    existing.getRoleId().equals(item.getRoleId())))
                    .collect(Collectors.toList());
        }

        // === DELETE 검증 및 처리 ===
        if(!deleteItems.isEmpty()){
            // 삭제 대상이 실제로 존재하는지 검증
            List<UserRole> tempDeleteUserRoles = deleteItems.stream()
                    .map(item -> {
                        UserRole tempUserRole = new UserRole();
                        tempUserRole.setMemberId(item.getMemberId());
                        tempUserRole.setRoleId(item.getRoleId());
                        return tempUserRole;
                    })
                    .toList();

            List<UserRole> existingDeleteUserRoles = memberMapper.selectExistingUserRolesByCompositeKeys(tempDeleteUserRoles);

            // 존재하지 않는 항목들에 대해 에러 처리
            for (UserRoleUpsertItem item : deleteItems) {
                boolean exists = existingDeleteUserRoles.stream()
                        .anyMatch(existing -> existing.getMemberId().equals(item.getMemberId()) &&
                                existing.getRoleId().equals(item.getRoleId()));

                if (!exists) {
                    String errorMsg = String.format("삭제할 사용자-역할 매핑이 존재하지 않습니다: userId=%s, roleId=%s",
                            item.getMemberId(), item.getRoleId());
                    result.addError(item.getTempId(), errorMsg, "DELETE_TARGET_NOT_FOUND");

                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                    }
                }
            }

            // 존재하지 않는 항목들을 DELETE 대상에서 제외
            deleteItems = deleteItems.stream()
                    .filter(item -> existingDeleteUserRoles.stream()
                            .anyMatch(existing -> existing.getMemberId().equals(item.getMemberId()) &&
                                    existing.getRoleId().equals(item.getRoleId())))
                    .collect(Collectors.toList());
        }

        // 실제 처리할 데이터 객체들
        List<UserRole> toInsert = new ArrayList<>();
        List<UserRole> toUpdate = new ArrayList<>();
        List<UserRole> toDelete = new ArrayList<>();

        // INSERT 처리
        for (UserRoleUpsertItem item : insertItems) {
            try {
                UserRole newUserRole = createUserRoleFromItem(item, currentMemberId, now);
                toInsert.add(newUserRole);
            } catch (Exception e) {
                result.addError(item.getTempId(), e.getMessage(), "INSERT_PROCESSING_ERROR");
                if (userOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "INSERT 처리 중 오류가 발생했습니다");
                }
            }
        }

        // UPDATE 처리
        for (UserRoleUpsertItem item : updateItems) {
            try {
                if (item.getMemberId() == null) {
                    result.addError(item.getTempId(), "UPDATE 작업에는 userId가 필요합니다", "MISSING_USER_ID");
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 작업에는 userId가 필요합니다");
                    }
                } else {
                    UserRole updatedUserRole = updateUserRoleFromItem(item, currentMemberId, now);
                    toUpdate.add(updatedUserRole);
                }
            } catch (Exception e) {
                result.addError(item.getTempId(), e.getMessage(), "UPDATE_PROCESSING_ERROR");
                if (userOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "UPDATE 처리 중 오류가 발생했습니다");
                }
            }
        }

        // DELETE 처리 (Soft Delete - delYn = false로 변경)
        for (UserRoleUpsertItem item : deleteItems) {
            try {
                if (item.getMemberId() == null) {
                    result.addError(item.getTempId(), "DELETE 작업에는 userId가 필요합니다", "MISSING_USER_ID");
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 작업에는 userId가 필요합니다");
                    }
                } else {
                    UserRole softDeleteUserRole = softDeleteUserRoleFromItem(item, currentMemberId, now);
                    toDelete.add(softDeleteUserRole);
                }
            } catch (Exception e) {
                result.addError(item.getTempId(), e.getMessage(), "DELETE_PROCESSING_ERROR");
                if (userOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "DELETE 처리 중 오류가 발생했습니다");
                }
            }
        }

        // 데이터베이스 작업 수행
        try {
            int insertCount = 0;
            int updateCount = 0;
            int deleteCount = 0;

            if (!toInsert.isEmpty()) {
                insertCount = memberMapper.insertUserRolesBulk(toInsert);
                result.incrementSuccess(insertCount);
            }

            if (!toUpdate.isEmpty()) {
                updateCount = memberMapper.updateUserRolesBulk(toUpdate);
                result.incrementSuccess(updateCount);
            }

            if (!toDelete.isEmpty()) {
                deleteCount = memberMapper.softDeleteUserRolesBulk(toDelete);
                result.incrementSuccess(deleteCount);
            }

            result.getSummary().put("insertCount", insertCount);
            result.getSummary().put("updateCount", updateCount);
            result.getSummary().put("deleteCount", deleteCount);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "일괄 처리 중 오류가 발생했습니다");
        }

        // 완료 처리
        result.markComplete();

        return result;
    }

    private UserRole softDeleteUserRoleFromItem(UserRoleUpsertItem item, Integer currentMemberId, LocalDateTime now) {
        UserRole userRole = new UserRole();
        userRole.setMemberId(item.getMemberId());
        userRole.setRoleId(item.getRoleId());
        userRole.setDelYn("Y");
        userRole.setChgId(currentMemberId);
        userRole.setChgDt(now);
        userRole.setDelId(currentMemberId);
        userRole.setDelDt(now);

        return userRole;
    }

    private UserRole updateUserRoleFromItem(UserRoleUpsertItem item, Integer currentMemberId, LocalDateTime now) {
        UserRole userRole = new UserRole();
        userRole.setMemberId(item.getMemberId());
        userRole.setRoleId(item.getRoleId());
        userRole.setDelYn(item.getDelYn());
        userRole.setChgId(currentMemberId);
        userRole.setChgDt(now);

        // Only clear del fields when activating
        if ("Y".equals(item.getDelYn())) {
            userRole.setDelYn("Y");
            userRole.setDelId(currentMemberId);
            userRole.setDelDt(now);
        } else if ("N".equals(item.getDelYn())) {
            userRole.setDelYn("N");
            userRole.setDelId(null);
            userRole.setDelDt(null);
        }
        // When deactivating or no change, don't touch del fields (preserve existing DB values)

        return userRole;
    }

    private UserRole createUserRoleFromItem(UserRoleUpsertItem item, Integer currentMemberId, LocalDateTime now) {
        UserRole userRole = new UserRole();
        userRole.setMemberId(item.getMemberId());
        userRole.setRoleId(item.getRoleId());
        userRole.setDelYn(item.getDelYn());
        userRole.setRegId(currentMemberId);
        userRole.setChgId(currentMemberId);
        userRole.setRegDt(now);
        userRole.setChgDt(now);

        return userRole;
    }
}

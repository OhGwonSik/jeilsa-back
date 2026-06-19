package com.common.auth.userrole.service;


import com.common.auth.common.annotation.AutoPaging;
import com.common.auth.common.dto.OperationOptions;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.userrole.domain.UserRole;
import com.common.auth.userrole.dto.UserRoleGridFilterDTO;
import com.common.auth.userrole.dto.UserRoleGridUpsertRequest;
import com.common.auth.userrole.dto.UserRoleResponse;
import com.common.auth.userrole.dto.UserRoleUpsertItem;
import com.common.auth.userrole.mapper.UserRoleMapper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleGridService {
    //----- DI Fields -----//
    private final UserRoleMapper userRoleMapper;

    @AutoPaging(defaultSort = "reg_dt", columnMapperMethod = "mapSortColumn")
    public PageInfo<UserRoleResponse> selectUserRolesWithFilter(UserRoleGridFilterDTO filter) {
        log.debug("Finding user roles with filter: {}", filter);

        List<UserRoleResponse> userRoleResponses = userRoleMapper.selectUserRolesWithFilter(filter);

        return new PageInfo<>(userRoleResponses);
    }

    public OperationResult upsertUserRolesBulk(UserRoleGridUpsertRequest request) {
        log.info("Batch upsert user roles: {} items", request.getItems().size());

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
            List<UserRole> existingUserRoles = userRoleMapper.selectExistingUserRolesByCompositeKeys(tempUserRoles);
            
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
            
            List<UserRole> existingUpdateUserRoles = userRoleMapper.selectExistingUserRolesByCompositeKeys(tempUpdateUserRoles);
            
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
            
            List<UserRole> existingDeleteUserRoles = userRoleMapper.selectExistingUserRolesByCompositeKeys(tempDeleteUserRoles);
            
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
                log.error("Error processing insert item: {}", item, e);
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
                log.error("Error processing update item: {}", item, e);
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
                log.error("Error processing delete item: {}", item, e);
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
                insertCount = userRoleMapper.insertUserRolesBulk(toInsert);
                result.incrementSuccess(insertCount);
                log.info("Bulk insert completed: {} items", insertCount);
            }

            if (!toUpdate.isEmpty()) {
                updateCount = userRoleMapper.updateUserRolesBulk(toUpdate);
                result.incrementSuccess(updateCount);
                log.info("Bulk update completed: {} items", updateCount);
            }

            if (!toDelete.isEmpty()) {
                deleteCount = userRoleMapper.softDeleteUserRolesBulk(toDelete);
                result.incrementSuccess(deleteCount);
                log.info("Bulk soft delete completed: {} items", deleteCount);
            }

            result.getSummary().put("insertCount", insertCount);
            result.getSummary().put("updateCount", updateCount);
            result.getSummary().put("deleteCount", deleteCount);

        } catch (Exception e) {
            log.error("Error during bulk database operation", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "일괄 처리 중 오류가 발생했습니다");
        }

        // 완료 처리
        result.markComplete();
        log.info("Bulk upsert completed: {}", result);

        return result;
    }

    private UserRole createUserRoleFromItem(UserRoleUpsertItem item, Integer currentUserId, LocalDateTime now) {
        UserRole userRole = new UserRole();
        userRole.setMemberId(item.getMemberId());
        userRole.setRoleId(item.getRoleId());
        userRole.setDelYn(item.getDelYn());
        userRole.setRegId(currentUserId);
        userRole.setChgId(currentUserId);
        userRole.setRegDt(now);
        userRole.setChgDt(now);

        return userRole;
    }

    private UserRole updateUserRoleFromItem(UserRoleUpsertItem item, Integer currentUserId, LocalDateTime now) {
        UserRole userRole = new UserRole();
        userRole.setMemberId(item.getMemberId());
        userRole.setRoleId(item.getRoleId());
        userRole.setDelYn(item.getDelYn());
        userRole.setChgId(currentUserId);
        userRole.setChgDt(now);

        // Only clear del fields when activating
        if (item.getDelYn() != null && item.getDelYn().equals("Y")) {
            userRole.setDelId(null);
            userRole.setDelDt(null);
        }
        // When deactivating or no change, don't touch del fields (preserve existing DB values)
        
        return userRole;
    }

    private UserRole softDeleteUserRoleFromItem(UserRoleUpsertItem item, Integer currentUserId, LocalDateTime now) {
        UserRole userRole = new UserRole();
        userRole.setMemberId(item.getMemberId());
        userRole.setRoleId(item.getRoleId());
        userRole.setDelYn("Y");
        userRole.setChgId(currentUserId);
        userRole.setChgDt(now);
        userRole.setDelId(currentUserId);
        userRole.setDelDt(now);

        return userRole;
    }

    /**
     * 클라이언트에서 전달된 sortBy 파라미터를 실제 DB 컬럼명으로 매핑
     * AOP에서 자동으로 호출됨
     */
    public String mapSortColumn(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "reg_dt"; // 기본 정렬 컬럼
        }

        return switch (sortBy.toLowerCase()) {
            case "username" -> "user_name";
            case "rolename" -> "role_name";
            case "delYn", "active" -> "del_yn";
            case "credt" -> "reg_dt";
            case "chgdt" -> "chg_dt";
            case "deldt" -> "del_dt";
            default -> null; // null 반환 시 기본 정렬 사용
        };
    }
}

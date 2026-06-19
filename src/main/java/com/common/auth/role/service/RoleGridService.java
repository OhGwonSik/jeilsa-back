package com.common.auth.role.service;


import com.common.auth.common.annotation.AutoPaging;
import com.common.auth.common.dto.OperationOptions;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.role.domain.Role;
import com.common.auth.role.dto.RoleGridFilterDTO;
import com.common.auth.role.dto.RoleGridUpsertRequest;
import com.common.auth.role.dto.RoleResponse;
import com.common.auth.role.dto.RoleUpsertItem;
import com.common.auth.role.mapper.RoleMapper;
import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleGridService {
    //----- DI Fields -----//
    private final RoleMapper roleMapper;

    @AutoPaging(defaultSort = "role_name", columnMapperMethod = "mapSortColumn")
    public PageInfo<RoleResponse> selectRolesWithFilter(RoleGridFilterDTO filter) {
        log.debug("Finding roles with filter: {}", filter);
        
        List<RoleResponse> roleResponses = roleMapper.selectRolesWithFilter(filter);
        
        return new PageInfo<>(roleResponses);
    }

    public OperationResult upsertRolesBulk(RoleGridUpsertRequest request) {
        log.info("Bulk upsert roles: {} items", request.getItems().size());

        OperationResult result = new OperationResult();
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();
        OperationOptions roleOptions = request.getOptions();

        
        // 요청 데이터 분류
        Map<String, List<RoleUpsertItem>> itemsByStatus = request.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getStatus().toUpperCase()));

        List<RoleUpsertItem> insertItems = itemsByStatus.getOrDefault("ADDED", Collections.emptyList());
        List<RoleUpsertItem> updateItems = itemsByStatus.getOrDefault("CHANGED", Collections.emptyList());
        List<RoleUpsertItem> deleteItems = itemsByStatus.getOrDefault("DELETED", Collections.emptyList());

        // === INSERT 검증 및 처리 ===
        if (!insertItems.isEmpty()) {
            // 요청한 roleId 목록
            List<Integer> insertRoleIdList = insertItems.stream()
                    .map(RoleUpsertItem::getRoleId)
                    .collect(Collectors.toList());
            Set<Integer> insertRoleIdSet = new HashSet<>(insertRoleIdList);

            // 요청한 roleName 목록
            List<String> insertRoleNameList = insertItems.stream()
                    .map(RoleUpsertItem::getRoleName)
                    .collect(Collectors.toList());
            Set<String> insertRoleNameSet = new HashSet<>(insertRoleNameList);

            // 중복 체크용 데이터
            List<String> existingInsertRoleNameList = roleMapper.selectExistRoleNameListByRoleNames(insertRoleNameList);

            // 중단 설정시 validation 체크
            if (roleOptions.isStopOnError()) {
                if (insertRoleIdList.size() != insertRoleIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "INSERT 요청에서 중복된 역할ID가 있습니다.");
                }
                if (insertRoleNameList.size() != insertRoleNameSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "INSERT 요청에서 중복된 역할명이 있습니다.");
                }
                if (!existingInsertRoleNameList.isEmpty()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("INSERT: 데이터베이스에 중복된 역할명이 있습니다", existingInsertRoleNameList));
                }
            }
        }

        // === UPDATE 검증 및 처리 ===
        if (!updateItems.isEmpty()) {
            // 요청한 roleId 목록
            List<Integer> updateRoleIdList = updateItems.stream()
                    .map(RoleUpsertItem::getRoleId)
                    .collect(Collectors.toList());
            Set<Integer> updateRoleIdSet = new HashSet<>(updateRoleIdList);

            // 요청한 roleName 목록 (null 제외)
            List<String> updateRoleNameList = updateItems.stream()
                    .filter(item -> item.getRoleName() != null)
                    .map(RoleUpsertItem::getRoleName)
                    .collect(Collectors.toList());
            Set<String> updateRoleNameSet = new HashSet<>(updateRoleNameList);

            // 중복 체크용 데이터
            List<Integer> existingUpdateRoleIdList = roleMapper.selectExistRoleIdListByRoleIds(updateRoleIdList);
            Set<Integer> existingUpdateRoleIdSet = new HashSet<>(existingUpdateRoleIdList);
            
            // roleName 충돌 체크 (업데이트용)
            List<Role> conflictRoleList = roleMapper.selectRoleListByRoleNamesAndNotInRoleIds(updateItems);

            // 중단 설정시 validation 체크
            if (roleOptions.isStopOnError()) {
                if (updateRoleIdList.size() != updateRoleIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 요청에서 중복된 역할ID가 있습니다.");
                }
                if (!updateRoleNameList.isEmpty() && updateRoleNameList.size() != updateRoleNameSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 요청에서 중복된 역할명이 있습니다.");
                }
                if (updateRoleIdList.size() != existingUpdateRoleIdList.size()) {
                    Set<Integer> nonExistentIdSet = new HashSet<>(updateRoleIdSet);
                    nonExistentIdSet.removeAll(existingUpdateRoleIdSet);
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("UPDATE: 데이터베이스에 존재하지 않는 역할ID가 있습니다", new ArrayList<>(nonExistentIdSet)));
                }
                if (!conflictRoleList.isEmpty()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("UPDATE: 데이터베이스에 충돌되는 역할명이 있습니다", conflictRoleList.stream().map(Role::getRoleName).collect(Collectors.toList())));
                }
            }
        }

        // === DELETE 검증 및 처리 ===
        if (!deleteItems.isEmpty()) {
            // 요청한 roleId 목록
            List<Integer> deleteRoleIdList = deleteItems.stream()
                    .map(RoleUpsertItem::getRoleId)
                    .collect(Collectors.toList());
            Set<Integer> deleteRoleIdSet = new HashSet<>(deleteRoleIdList);

            // 중복 체크용 데이터
            List<Integer> existingDeleteRoleIdList = roleMapper.selectExistRoleIdListByRoleIds(deleteRoleIdList);

            // 중단 설정시 validation 체크
            if (roleOptions.isStopOnError()) {
                if (deleteRoleIdList.size() != deleteRoleIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 요청에서 중복된 역할ID가 있습니다.");
                }
                if (deleteRoleIdList.size() != existingDeleteRoleIdList.size()) {
                    Set<Integer> nonExistentIdSet = new HashSet<>(deleteRoleIdSet);
                    nonExistentIdSet.removeAll(new HashSet<>(existingDeleteRoleIdList));
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("DELETE: 데이터베이스에 존재하지 않는 역할ID가 있습니다", new ArrayList<>(nonExistentIdSet)));
                }
            }
        }

        // 실제 처리할 데이터 객체들
        List<Role> toInsert = new ArrayList<>();
        List<Role> toUpdate = new ArrayList<>();
        List<Role> toDelete = new ArrayList<>();

        // INSERT 처리
        for (RoleUpsertItem item : insertItems) {
            try {
                Role newRole = createRoleFromItem(item, currentUserId, now);
                toInsert.add(newRole);
            } catch (Exception e) {
                log.error("Error processing insert item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "INSERT_PROCESSING_ERROR");
                if (roleOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "INSERT 처리 중 오류가 발생했습니다");
                }
            }
        }

        // UPDATE 처리
        for (RoleUpsertItem item : updateItems) {
            try {
                if (item.getRoleId() == null) {
                    result.addError(item.getTempId(), "UPDATE 작업에는 roleId가 필요합니다", "MISSING_ROLE_ID");
                    if (roleOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 작업에는 roleId가 필요합니다");
                    }
                } else {
                    Role updatedRole = updateRoleFromItem(item, currentUserId, now);
                    toUpdate.add(updatedRole);
                }
            } catch (Exception e) {
                log.error("Error processing update item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "UPDATE_PROCESSING_ERROR");
                if (roleOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "UPDATE 처리 중 오류가 발생했습니다");
                }
            }
        }

        // DELETE 처리 (Soft Delete - delYn = false로 변경)
        for (RoleUpsertItem item : deleteItems) {
            try {
                if (item.getRoleId() == null) {
                    result.addError(item.getTempId(), "DELETE 작업에는 roleId가 필요합니다", "MISSING_ROLE_ID");
                    if (roleOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 작업에는 roleId가 필요합니다");
                    }
                } else {
                    Role softDeleteRole = softDeleteRoleFromItem(item, currentUserId, now);
                    toDelete.add(softDeleteRole);
                }
            } catch (Exception e) {
                log.error("Error processing delete item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "DELETE_PROCESSING_ERROR");
                if (roleOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "DELETE 처리 중 오류가 발생했습니다");
                }
            }
        }

        // 기존 하드 DELETE 처리 (주석 처리)
        // for (RoleUpsertItem item : deleteItems) {
        //     try {
        //         if (item.getRoleId() == null) {
        //             result.addError(item.getTempId(), "DELETE 작업에는 roleId가 필요합니다", "MISSING_ROLE_ID");
        //             if (roleOptions.isStopOnError()) {
        //                 throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 작업에는 roleId가 필요합니다");
        //             }
        //         } else {
        //             Role existing = new Role();
        //             existing.setRoleId(item.getRoleId());
        //             toDelete.add(existing);
        //         }
        //     } catch (Exception e) {
        //         log.error("Error processing delete item: {}", item, e);
        //         result.addError(item.getTempId(), e.getMessage(), "DELETE_PROCESSING_ERROR");
        //         if (roleOptions.isStopOnError()) {
        //             throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "DELETE 처리 중 오류가 발생했습니다");
        //         }
        //     }
        // }

        // 데이터베이스 작업 수행
        try {
            int insertCount = 0;
            int updateCount = 0;
            int deleteCount = 0;

            if (!toInsert.isEmpty()) {
                insertCount = roleMapper.insertRolesBulk(toInsert);
                result.incrementSuccess(insertCount);
                log.info("Bulk insert completed: {} items", insertCount);
            }

            if (!toUpdate.isEmpty()) {
                updateCount = roleMapper.updateRolesBulk(toUpdate);
                result.incrementSuccess(updateCount);
                log.info("Bulk update completed: {} items", updateCount);
            }

            if (!toDelete.isEmpty()) {
                deleteCount = roleMapper.softDeleteRolesBulk(toDelete);
                result.incrementSuccess(deleteCount);
                log.info("Bulk soft delete completed: {} items", deleteCount);
            }

            // 기존 하드 DELETE 작업 (주석 처리)
            // if (!toDelete.isEmpty()) {
            //     deleteCount = roleMapper.deleteRolesBulk(toDelete);
            //     result.incrementSuccess(deleteCount);
            //     log.info("Bulk delete completed: {} items", deleteCount);
            // }

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


    private Role createRoleFromItem(RoleUpsertItem item, Integer currentUserId, LocalDateTime now) {
        Role role = new Role();
        role.setRoleName(item.getRoleName());
        role.setDescription(item.getDescription());
        role.setIsDefault(item.getIsDefault());
        role.setDelYn(item.getDelYn());
        role.setChgId(currentUserId);
        role.setChgDt(now);
        role.setRegId(currentUserId);
        role.setRegDt(now);
        role.setChgId(currentUserId);
        role.setChgDt(now);
        return role;
    }

    private Role updateRoleFromItem(RoleUpsertItem item, Integer currentUserId, LocalDateTime now) {
        Role role = new Role();
        role.setRoleId(item.getRoleId());
        role.setRoleName(item.getRoleName());
        role.setDescription(item.getDescription());
        role.setIsDefault(item.getIsDefault());
        role.setDelYn(item.getDelYn());
        role.setChgId(currentUserId);
        role.setChgDt(now);
        // Only clear del fields when activating
        if (item.getDelYn() != null && item.getDelYn().equals("N")) {
            role.setDelId(null);
            role.setDelDt(null);
        }
        // When deactivating or no change, don't touch del fields (preserve existing DB values)
        return role;
    }

    private Role softDeleteRoleFromItem(RoleUpsertItem item, Integer currentUserId, LocalDateTime now) {
        Role role = new Role();
        role.setRoleId(item.getRoleId());
        role.setDelYn("Y");
        role.setChgId(currentUserId);
        role.setChgDt(now);
        role.setDelId(currentUserId);
        role.setDelDt(now);
        return role;
    }

    private <T> String formatDuplicateErrorMessage(String messagePrefix, List<T> duplicates) {
        int limit = 5;
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(messagePrefix)
                    .append(" (총 ")
                    .append(duplicates.size())
                    .append("건)");

        if (!duplicates.isEmpty()) {
            errorMessage.append(" - 예시: ");
            errorMessage.append(
                duplicates.stream()
                          .limit(limit)
                          .map(Object::toString)
                          .collect(Collectors.joining(", "))
            );
            if (duplicates.size() > limit) {
                errorMessage.append("...");
            }
        }
        return errorMessage.toString();
    }

    /**
     * 클라이언트에서 전달된 sortBy 파라미터를 실제 DB 컬럼명으로 매핑
     * AOP에서 자동으로 호출됨
     */
    public String mapSortColumn(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "role_name"; // 기본 정렬 컬럼
        }
        
        return switch (sortBy.toLowerCase()) {
            case "rolename" -> "role_name";
            case "description" -> "description";
            case "isdefault" -> "is_default";
            case "delYn" -> "del_yn";
            case "credt" -> "reg_dt";
            case "chgdt" -> "chg_dt";
            case "deldt" -> "del_dt";
            default -> null; // null 반환 시 기본 정렬 사용
        };
    }
}
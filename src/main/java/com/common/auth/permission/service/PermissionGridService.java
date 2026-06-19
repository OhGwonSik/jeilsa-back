package com.common.auth.permission.service;


import com.common.auth.common.annotation.AutoPaging;
import com.common.auth.common.dto.OperationOptions;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.permission.domain.Permission;
import com.common.auth.permission.dto.PermissionGridFilterDTO;
import com.common.auth.permission.dto.PermissionGridUpsertRequest;
import com.common.auth.permission.dto.PermissionResponse;
import com.common.auth.permission.dto.PermissionUpsertItem;
import com.common.auth.permission.mapper.PermissionMapper;
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
public class PermissionGridService {
    //----- DI Fields -----//
    private final PermissionMapper permissionMapper;
    
    @AutoPaging(defaultSort = "permission_name", columnMapperMethod = "mapSortColumn")
    public PageInfo<PermissionResponse> selectPermissionsWithFilter(PermissionGridFilterDTO filter) {
        log.debug("Finding permissions with filter: {}", filter);
        
        List<PermissionResponse> permissionResponses = permissionMapper.selectPermissionsWithFilter(filter);
        
        return new PageInfo<>(permissionResponses);
    }

    public OperationResult upsertPermissionsBulk(PermissionGridUpsertRequest request) {
        log.info("Bulk upsert permissions: {} items", request.getItems().size());

        OperationResult result = new OperationResult();
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();
        OperationOptions permissionOptions = request.getOptions();

        // 요청 데이터 분류
        Map<String, List<PermissionUpsertItem>> itemsByStatus = request.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getStatus().toUpperCase()));

        List<PermissionUpsertItem> insertItems = itemsByStatus.getOrDefault("ADDED", Collections.emptyList());
        List<PermissionUpsertItem> updateItems = itemsByStatus.getOrDefault("CHANGED", Collections.emptyList());
        List<PermissionUpsertItem> deleteItems = itemsByStatus.getOrDefault("DELETED", Collections.emptyList());

        // === INSERT 검증 및 처리 ===
        if (!insertItems.isEmpty()) {
            // 요청한 permissionId 목록
            List<Integer> insertPermissionIdList = insertItems.stream()
                    .map(PermissionUpsertItem::getPermissionId)
                    .collect(Collectors.toList());
            Set<Integer> insertPermissionIdSet = new HashSet<>(insertPermissionIdList);

            // 요청한 permissionName 목록
            List<String> insertPermissionNameList = insertItems.stream()
                    .map(PermissionUpsertItem::getPermissionName)
                    .collect(Collectors.toList());
            Set<String> insertPermissionNameSet = new HashSet<>(insertPermissionNameList);

            // 중복 체크용 데이터
            List<Integer> existingInsertPermissionIdList = permissionMapper.selectExistPermissionIdListByPermissionIds(insertPermissionIdList);
            List<String> existingInsertPermissionNameList = permissionMapper.selectExistPermissionNameListByPermissionNames(insertPermissionNameList);

            // 중단 설정시 validation 체크
            if (permissionOptions.isStopOnError()) {
                if (insertPermissionIdList.size() != insertPermissionIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "INSERT 요청에서 중복된 권한ID가 있습니다.");
                }
                if (insertPermissionNameList.size() != insertPermissionNameSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "INSERT 요청에서 중복된 권한명이 있습니다.");
                }
                if (!existingInsertPermissionIdList.isEmpty()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("INSERT: 데이터베이스에 중복된 권한ID가 있습니다", existingInsertPermissionIdList));
                }
                if (!existingInsertPermissionNameList.isEmpty()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("INSERT: 데이터베이스에 중복된 권한명이 있습니다", existingInsertPermissionNameList));
                }
            }
        }

        // === UPDATE 검증 및 처리 ===
        if (!updateItems.isEmpty()) {
            // 요청한 permissionId 목록
            List<Integer> updatePermissionIdList = updateItems.stream()
                    .map(PermissionUpsertItem::getPermissionId)
                    .collect(Collectors.toList());
            Set<Integer> updatePermissionIdSet = new HashSet<>(updatePermissionIdList);

            // 요청한 permissionName 목록 (null 제외)
            List<String> updatePermissionNameList = updateItems.stream()
                    .filter(item -> item.getPermissionName() != null)
                    .map(PermissionUpsertItem::getPermissionName)
                    .collect(Collectors.toList());
            Set<String> updatePermissionNameSet = new HashSet<>(updatePermissionNameList);

            // 중복 체크용 데이터
            List<Integer> existingUpdatePermissionIdList = permissionMapper.selectExistPermissionIdListByPermissionIds(updatePermissionIdList);
            Set<Integer> existingUpdatePermissionIdSet = new HashSet<>(existingUpdatePermissionIdList);
            
            // permissionName 충돌 체크 (업데이트용)
            List<Permission> conflictPermissionList = permissionMapper.selectPermissionListByPermissionNamesAndNotInPermissionIds(updateItems);

            // 중단 설정시 validation 체크
            if (permissionOptions.isStopOnError()) {
                if (updatePermissionIdList.size() != updatePermissionIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 요청에서 중복된 권한ID가 있습니다.");
                }
                if (!updatePermissionNameList.isEmpty() && updatePermissionNameList.size() != updatePermissionNameSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 요청에서 중복된 권한명이 있습니다.");
                }
                if (updatePermissionIdList.size() != existingUpdatePermissionIdList.size()) {
                    Set<Integer> nonExistentIdSet = new HashSet<>(updatePermissionIdSet);
                    nonExistentIdSet.removeAll(existingUpdatePermissionIdSet);
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("UPDATE: 데이터베이스에 존재하지 않는 권한ID가 있습니다", new ArrayList<>(nonExistentIdSet)));
                }
                if (!conflictPermissionList.isEmpty()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("UPDATE: 데이터베이스에 충돌되는 권한명이 있습니다", conflictPermissionList.stream().map(Permission::getPermissionName).collect(Collectors.toList())));
                }
            }
        }

        // === DELETE 검증 및 처리 ===
        if (!deleteItems.isEmpty()) {
            // 요청한 permissionId 목록
            List<Integer> deletePermissionIdList = deleteItems.stream()
                    .map(PermissionUpsertItem::getPermissionId)
                    .collect(Collectors.toList());
            Set<Integer> deletePermissionIdSet = new HashSet<>(deletePermissionIdList);

            // 중복 체크용 데이터
            List<Integer> existingDeletePermissionIdList = permissionMapper.selectExistPermissionIdListByPermissionIds(deletePermissionIdList);

            // 중단 설정시 validation 체크
            if (permissionOptions.isStopOnError()) {
                if (deletePermissionIdList.size() != deletePermissionIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 요청에서 중복된 권한ID가 있습니다.");
                }
                if (deletePermissionIdList.size() != existingDeletePermissionIdList.size()) {
                    Set<Integer> nonExistentIdSet = new HashSet<>(deletePermissionIdSet);
                    nonExistentIdSet.removeAll(new HashSet<>(existingDeletePermissionIdList));
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("DELETE: 데이터베이스에 존재하지 않는 권한ID가 있습니다", new ArrayList<>(nonExistentIdSet)));
                }
            }
        }

        // 실제 처리할 데이터 객체들
        List<Permission> toInsert = new ArrayList<>();
        List<Permission> toUpdate = new ArrayList<>();
        List<Permission> toDelete = new ArrayList<>();

        // INSERT 처리
        for (PermissionUpsertItem item : insertItems) {
            try {
                Permission newPermission = createPermissionFromItem(item, currentUserId, now);
                toInsert.add(newPermission);
            } catch (Exception e) {
                log.error("Error processing insert item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "INSERT_PROCESSING_ERROR");
                if (permissionOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "INSERT 처리 중 오류가 발생했습니다");
                }
            }
        }

        // UPDATE 처리
        for (PermissionUpsertItem item : updateItems) {
            try {
                if (item.getPermissionId() == null) {
                    result.addError(item.getTempId(), "UPDATE 작업에는 permissionId가 필요합니다", "MISSING_PERMISSION_ID");
                    if (permissionOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 작업에는 permissionId가 필요합니다");
                    }
                } else {
                    Permission updatedPermission = updatePermissionFromItem(item, currentUserId, now);
                    toUpdate.add(updatedPermission);
                }
            } catch (Exception e) {
                log.error("Error processing update item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "UPDATE_PROCESSING_ERROR");
                if (permissionOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "UPDATE 처리 중 오류가 발생했습니다");
                }
            }
        }

        // DELETE 처리 (Soft Delete - delYn = false로 변경)
        for (PermissionUpsertItem item : deleteItems) {
            try {
                if (item.getPermissionId() == null) {
                    result.addError(item.getTempId(), "DELETE 작업에는 permissionId가 필요합니다", "MISSING_PERMISSION_ID");
                    if (permissionOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 작업에는 permissionId가 필요합니다");
                    }
                } else {
                    Permission softDeletePermission = softDeletePermissionFromItem(item, currentUserId, now);
                    toDelete.add(softDeletePermission);
                }
            } catch (Exception e) {
                log.error("Error processing delete item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "DELETE_PROCESSING_ERROR");
                if (permissionOptions.isStopOnError()) {
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
                insertCount = permissionMapper.insertPermissionsBulk(toInsert);
                result.incrementSuccess(insertCount);
                log.info("Bulk insert completed: {} items", insertCount);
            }

            if (!toUpdate.isEmpty()) {
                updateCount = permissionMapper.updatePermissionsBulk(toUpdate);
                result.incrementSuccess(updateCount);
                log.info("Bulk update completed: {} items", updateCount);
            }

            if (!toDelete.isEmpty()) {
                deleteCount = permissionMapper.softDeletePermissionsBulk(toDelete);
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

    private Permission createPermissionFromItem(PermissionUpsertItem item, Integer currentUserId, LocalDateTime now) {
        Permission permission = new Permission(item.getPermissionName(), item.getDescription());
        permission.setPermissionId(item.getPermissionId());
        permission.setDelYn(item.getDelYn());
        permission.setRegId(currentUserId);
        permission.setChgId(currentUserId);
        permission.setRegDt(now);
        permission.setChgDt(now);
        
        return permission;
    }

    private Permission updatePermissionFromItem(PermissionUpsertItem item, Integer currentUserId, LocalDateTime now) {
        Permission permission = new Permission();
        permission.setPermissionId(item.getPermissionId());
        permission.setPermissionName(item.getPermissionName());
        permission.setDescription(item.getDescription());
        permission.setDelYn(item.getDelYn());
        permission.setChgId(currentUserId);
        permission.setChgDt(now);

        // Only clear del fields when activating
        if (item.getDelYn() != null && item.getDelYn().equals("N")) {
            permission.setDelId(null);
            permission.setDelDt(null);
        }
        // When deactivating or no change, don't touch del fields (preserve existing DB values)

        return permission;
    }

    private Permission softDeletePermissionFromItem(PermissionUpsertItem item, Integer currentUserId, LocalDateTime now) {
        Permission permission = new Permission();
        permission.setPermissionId(item.getPermissionId());
        permission.setDelYn("Y");
        permission.setChgId(currentUserId);
        permission.setChgDt(now);
        permission.setDelId(currentUserId);
        permission.setDelDt(now);

        return permission;
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
            return "permission_name"; // 기본 정렬 컬럼
        }
        
        return switch (sortBy.toLowerCase()) {
            case "permissionname" -> "permission_name";
            case "description" -> "description";
            case "delYn" -> "del_yn";
            case "credt" -> "reg_dt";
            case "chgdt" -> "chg_dt";
            case "deldt" -> "del_dt";
            default -> null; // null 반환 시 기본 정렬 사용
        };
    }
}

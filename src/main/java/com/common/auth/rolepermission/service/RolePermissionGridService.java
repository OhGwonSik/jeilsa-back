package com.common.auth.rolepermission.service;


import com.common.auth.common.annotation.AutoPaging;
import com.common.auth.common.dto.OperationOptions;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.rolepermission.domain.RolePermission;
import com.common.auth.rolepermission.dto.RolePermissionGridFilterDTO;
import com.common.auth.rolepermission.dto.RolePermissionGridUpsertRequest;
import com.common.auth.rolepermission.dto.RolePermissionResponse;
import com.common.auth.rolepermission.dto.RolePermissionUpsertItem;
import com.common.auth.rolepermission.mapper.RolePermissionMapper;
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
public class RolePermissionGridService {
    //----- DI Fields -----//
    private final RolePermissionMapper rolePermissionMapper;
    
    @AutoPaging(defaultSort = "reg_dt", columnMapperMethod = "mapSortColumn")
    public PageInfo<RolePermissionResponse> selectRolePermissionsWithFilter(RolePermissionGridFilterDTO filter) {
        log.debug("Finding role permissions with filter: {}", filter);
        
        List<RolePermissionResponse> rolePermissionResponses = rolePermissionMapper.selectRolePermissionsWithFilter(filter);
        
        return new PageInfo<>(rolePermissionResponses);
    }

    public OperationResult upsertRolePermissionsBulk(RolePermissionGridUpsertRequest request) {
        log.info("Batch upsert role permissions: {} items", request.getItems().size());

        OperationResult result = new OperationResult();
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();
        OperationOptions userOptions = request.getOptions();

        // 요청 데이터 분류
        Map<String, List<RolePermissionUpsertItem>> itemsByStatus = request.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getStatus().toUpperCase()));

        List<RolePermissionUpsertItem> insertItems = itemsByStatus.getOrDefault("ADDED", Collections.emptyList());
        List<RolePermissionUpsertItem> updateItems = itemsByStatus.getOrDefault("CHANGED", Collections.emptyList());
        List<RolePermissionUpsertItem> deleteItems = itemsByStatus.getOrDefault("DELETED", Collections.emptyList());

        // === INSERT 검증 및 처리 ===
        if (!insertItems.isEmpty()) {
            // 중복 체크를 위해 임시 RolePermission 객체들 생성
            List<RolePermission> tempRolePermissions = insertItems.stream()
                    .map(item -> {
                        RolePermission tempRolePermission = new RolePermission();
                        tempRolePermission.setRoleId(item.getRoleId());
                        tempRolePermission.setPermissionId(item.getPermissionId());
                        return tempRolePermission;
                    })
                    .toList();
            
            // 기존에 존재하는 역할-권한 매핑 조회
            List<RolePermission> existingRolePermissions = rolePermissionMapper.selectExistingRolePermissionsByCompositeKeys(tempRolePermissions);
            
            if (!existingRolePermissions.isEmpty()) {
                // 중복된 항목들에 대해 에러 처리
                for (RolePermission existing : existingRolePermissions) {
                    RolePermissionUpsertItem duplicateItem = insertItems.stream()
                            .filter(item -> item.getRoleId().equals(existing.getRoleId()) && 
                                           item.getPermissionId().equals(existing.getPermissionId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (duplicateItem != null) {
                        String errorMsg = String.format("역할-권한 매핑이 이미 존재합니다: roleId=%s, permissionId=%s", 
                                                      existing.getRoleId(), existing.getPermissionId());
                        result.addError(duplicateItem.getTempId(), errorMsg, "DUPLICATE_ROLE_PERMISSION");
                        
                        if (userOptions.isStopOnError()) {
                            throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                        }
                    }
                }
                
                // 중복된 항목들을 INSERT 대상에서 제외
                insertItems = insertItems.stream()
                        .filter(item -> existingRolePermissions.stream()
                                .noneMatch(existing -> existing.getRoleId().equals(item.getRoleId()) && 
                                                      existing.getPermissionId().equals(item.getPermissionId())))
                        .collect(Collectors.toList());
            }
        }

        // === UPDATE 검증 및 처리 ===
        if(!updateItems.isEmpty()){
            // 업데이트 대상이 실제로 존재하는지 검증
            List<RolePermission> tempUpdateRolePermissions = updateItems.stream()
                    .map(item -> {
                        RolePermission tempRolePermission = new RolePermission();
                        tempRolePermission.setRoleId(item.getRoleId());
                        tempRolePermission.setPermissionId(item.getPermissionId());
                        return tempRolePermission;
                    })
                    .toList();
            
            List<RolePermission> existingUpdateRolePermissions = rolePermissionMapper.selectExistingRolePermissionsByCompositeKeys(tempUpdateRolePermissions);
            
            // 존재하지 않는 항목들에 대해 에러 처리
            for (RolePermissionUpsertItem item : updateItems) {
                boolean exists = existingUpdateRolePermissions.stream()
                        .anyMatch(existing -> existing.getRoleId().equals(item.getRoleId()) && 
                                             existing.getPermissionId().equals(item.getPermissionId()));
                
                if (!exists) {
                    String errorMsg = String.format("업데이트할 역할-권한 매핑이 존재하지 않습니다: roleId=%s, permissionId=%s", 
                                                  item.getRoleId(), item.getPermissionId());
                    result.addError(item.getTempId(), errorMsg, "UPDATE_TARGET_NOT_FOUND");
                    
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                    }
                }
            }
            
            // 존재하지 않는 항목들을 UPDATE 대상에서 제외
            updateItems = updateItems.stream()
                    .filter(item -> existingUpdateRolePermissions.stream()
                            .anyMatch(existing -> existing.getRoleId().equals(item.getRoleId()) && 
                                                 existing.getPermissionId().equals(item.getPermissionId())))
                    .collect(Collectors.toList());
        }

        // === DELETE 검증 및 처리 ===
        if(!deleteItems.isEmpty()){
            // 삭제 대상이 실제로 존재하는지 검증
            List<RolePermission> tempDeleteRolePermissions = deleteItems.stream()
                    .map(item -> {
                        RolePermission tempRolePermission = new RolePermission();
                        tempRolePermission.setRoleId(item.getRoleId());
                        tempRolePermission.setPermissionId(item.getPermissionId());
                        return tempRolePermission;
                    })
                    .toList();
            
            List<RolePermission> existingDeleteRolePermissions = rolePermissionMapper.selectExistingRolePermissionsByCompositeKeys(tempDeleteRolePermissions);
            
            // 존재하지 않는 항목들에 대해 에러 처리
            for (RolePermissionUpsertItem item : deleteItems) {
                boolean exists = existingDeleteRolePermissions.stream()
                        .anyMatch(existing -> existing.getRoleId().equals(item.getRoleId()) && 
                                             existing.getPermissionId().equals(item.getPermissionId()));
                
                if (!exists) {
                    String errorMsg = String.format("삭제할 역할-권한 매핑이 존재하지 않습니다: roleId=%s, permissionId=%s", 
                                                  item.getRoleId(), item.getPermissionId());
                    result.addError(item.getTempId(), errorMsg, "DELETE_TARGET_NOT_FOUND");
                    
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                    }
                }
            }
            
            // 존재하지 않는 항목들을 DELETE 대상에서 제외
            deleteItems = deleteItems.stream()
                    .filter(item -> existingDeleteRolePermissions.stream()
                            .anyMatch(existing -> existing.getRoleId().equals(item.getRoleId()) && 
                                                 existing.getPermissionId().equals(item.getPermissionId())))
                    .collect(Collectors.toList());
        }
        
        // 실제 처리할 데이터 객체들
        List<RolePermission> toInsert = new ArrayList<>();
        List<RolePermission> toUpdate = new ArrayList<>();
        List<RolePermission> toDelete = new ArrayList<>();

        // INSERT 처리
        for (RolePermissionUpsertItem item : insertItems) {
            try {
                RolePermission newRolePermission = createRolePermissionFromItem(item, currentUserId, now);
                toInsert.add(newRolePermission);
            } catch (Exception e) {
                log.error("Error processing insert item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "INSERT_PROCESSING_ERROR");
                if (userOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "INSERT 처리 중 오류가 발생했습니다");
                }
            }
        }

        // UPDATE 처리
        for (RolePermissionUpsertItem item : updateItems) {
            try {
                if (item.getRoleId() == null) {
                    result.addError(item.getTempId(), "UPDATE 작업에는 roleId가 필요합니다", "MISSING_ROLE_ID");
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 작업에는 roleId가 필요합니다");
                    }
                } else {
                    RolePermission updatedRolePermission = updateRolePermissionFromItem(item, currentUserId, now);
                    toUpdate.add(updatedRolePermission);
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
        for (RolePermissionUpsertItem item : deleteItems) {
            try {
                if (item.getRoleId() == null) {
                    result.addError(item.getTempId(), "DELETE 작업에는 roleId가 필요합니다", "MISSING_ROLE_ID");
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 작업에는 roleId가 필요합니다");
                    }
                } else {
                    RolePermission softDeleteRolePermission = softDeleteRolePermissionFromItem(item, currentUserId, now);
                    toDelete.add(softDeleteRolePermission);
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
                rolePermissionMapper.insertRolePermissionsBulk(toInsert);
                insertCount = toInsert.size();
                result.incrementSuccess(insertCount);
                log.info("Bulk insert completed: {} items", insertCount);
            }

            if (!toUpdate.isEmpty()) {
                rolePermissionMapper.updateRolePermissionsBulk(toUpdate);
                updateCount = toUpdate.size();
                result.incrementSuccess(updateCount);
                log.info("Bulk update completed: {} items", updateCount);
            }

            if (!toDelete.isEmpty()) {
                rolePermissionMapper.softDeleteRolePermissionsBulk(toDelete);
                deleteCount = toDelete.size();
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


    private RolePermission createRolePermissionFromItem(RolePermissionUpsertItem item, Integer currentUserId, LocalDateTime now) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(item.getRoleId());
        rolePermission.setPermissionId(item.getPermissionId());
        rolePermission.setDelYn(item.getDelYn());
        rolePermission.setRegId(currentUserId);
        rolePermission.setChgId(currentUserId);
        rolePermission.setRegDt(now);
        rolePermission.setChgDt(now);
        
        return rolePermission;
    }

    private RolePermission updateRolePermissionFromItem(RolePermissionUpsertItem item, Integer currentUserId, LocalDateTime now) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(item.getRoleId());
        rolePermission.setPermissionId(item.getPermissionId());
        rolePermission.setDelYn(item.getDelYn());
        rolePermission.setChgId(currentUserId);
        rolePermission.setChgDt(now);

        if (item.getDelYn().equals("N")) {
            rolePermission.setDelId(null);
            rolePermission.setDelDt(null);
        }
        
        return rolePermission;
    }

    private RolePermission softDeleteRolePermissionFromItem(RolePermissionUpsertItem item, Integer currentUserId, LocalDateTime now) {
        RolePermission rolePermission = new RolePermission();
        rolePermission.setRoleId(item.getRoleId());
        rolePermission.setPermissionId(item.getPermissionId());
        rolePermission.setDelYn("Y");
        rolePermission.setChgId(currentUserId);
        rolePermission.setChgDt(now);
        rolePermission.setDelId(currentUserId);
        rolePermission.setDelDt(now);

        return rolePermission;
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
            case "rolename" -> "role_name";
            case "permissionname" -> "permission_name";
            case "delYn" -> "del_yn";
            case "credt" -> "reg_dt";
            case "chgdt" -> "chg_dt";
            default -> null; // null 반환 시 기본 정렬 사용
        };
    }
}
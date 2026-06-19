package com.common.auth.organizationrole.service;


import com.common.auth.common.annotation.AutoPaging;
import com.common.auth.common.dto.OperationOptions;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.organizationrole.domain.OrganizationRole;
import com.common.auth.organizationrole.dto.OrganizationRoleGridFilterDTO;
import com.common.auth.organizationrole.dto.OrganizationRoleGridUpsertRequest;
import com.common.auth.organizationrole.dto.OrganizationRoleResponse;
import com.common.auth.organizationrole.dto.OrganizationRoleUpsertItem;
import com.common.auth.organizationrole.mapper.OrganizationRoleMapper;
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
public class OrganizationRoleGridService {
    //----- DI Fields -----//
    private final OrganizationRoleMapper organizationRoleMapper;

    @AutoPaging(defaultSort = "reg_dt", columnMapperMethod = "mapSortColumn")
    public PageInfo<OrganizationRoleResponse> selectOrganizationRolesWithFilter(OrganizationRoleGridFilterDTO filter) {
        log.debug("Finding organization roles with filter: {}", filter);

        List<OrganizationRoleResponse> organizationRoles = organizationRoleMapper.selectOrganizationRolesWithFilter(filter);

        return new PageInfo<>(organizationRoles);
    }

    public OperationResult upsertOrganizationRolesBulk(OrganizationRoleGridUpsertRequest request) {
        log.info("Bulk upsert organization roles: {} items", request.getItems().size());
        
        OperationResult result = new OperationResult();
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();
        OperationOptions userOptions = request.getOptions();
        
        // 요청 데이터 분류
        Map<String, List<OrganizationRoleUpsertItem>> itemsByStatus = request.getItems().stream()
                                                                                        .collect(Collectors.groupingBy(item -> item.getStatus().toUpperCase()));

        List<OrganizationRoleUpsertItem> insertItems = itemsByStatus.getOrDefault("ADDED", Collections.emptyList());
        List<OrganizationRoleUpsertItem> updateItems = itemsByStatus.getOrDefault("CHANGED", Collections.emptyList());
        List<OrganizationRoleUpsertItem> deleteItems = itemsByStatus.getOrDefault("DELETED", Collections.emptyList());

        // === INSERT 검증 및 처리 ===
        if (!insertItems.isEmpty()) {
            // 중복 체크를 위해 임시 OrganizationRole 객체들 생성
            List<OrganizationRole> tempOrganizationRoles = insertItems.stream()
                    .map(item -> {
                        OrganizationRole tempOrganizationRole = new OrganizationRole();
                        tempOrganizationRole.setOrganizationId(item.getOrganizationId());
                        tempOrganizationRole.setRoleId(item.getRoleId());
                        return tempOrganizationRole;
                    })
                    .toList();
            
            // 기존에 존재하는 조직-역할 매핑 조회
            List<OrganizationRole> existingOrganizationRoles = organizationRoleMapper.selectExistingOrganizationRolesByCompositeKeys(tempOrganizationRoles);
            
            if (!existingOrganizationRoles.isEmpty()) {
                // 중복된 항목들에 대해 에러 처리
                for (OrganizationRole existing : existingOrganizationRoles) {
                    OrganizationRoleUpsertItem duplicateItem = insertItems.stream()
                            .filter(item -> item.getOrganizationId().equals(existing.getOrganizationId()) && 
                                           item.getRoleId().equals(existing.getRoleId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (duplicateItem != null) {
                        String errorMsg = String.format("조직-역할 매핑이 이미 존재합니다: organizationId=%s, roleId=%s", 
                                                      existing.getOrganizationId(), existing.getRoleId());
                        result.addError(duplicateItem.getTempId(), errorMsg, "DUPLICATE_ORGANIZATION_ROLE");
                        
                        if (userOptions.isStopOnError()) {
                            throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                        }
                    }
                }
                
                // 중복된 항목들을 INSERT 대상에서 제외
                insertItems = insertItems.stream()
                        .filter(item -> existingOrganizationRoles.stream()
                                .noneMatch(existing -> existing.getOrganizationId().equals(item.getOrganizationId()) && 
                                                      existing.getRoleId().equals(item.getRoleId())))
                        .collect(Collectors.toList());
            }
        }

        // === UPDATE 검증 및 처리 ===
        if(!updateItems.isEmpty()){
            // 업데이트 대상이 실제로 존재하는지 검증
            List<OrganizationRole> tempUpdateOrganizationRoles = updateItems.stream()
                    .map(item -> {
                        OrganizationRole tempOrganizationRole = new OrganizationRole();
                        tempOrganizationRole.setOrganizationId(item.getOrganizationId());
                        tempOrganizationRole.setRoleId(item.getRoleId());
                        return tempOrganizationRole;
                    })
                    .toList();
            
            List<OrganizationRole> existingUpdateOrganizationRoles = organizationRoleMapper.selectExistingOrganizationRolesByCompositeKeys(tempUpdateOrganizationRoles);
            
            // 존재하지 않는 항목들에 대해 에러 처리
            for (OrganizationRoleUpsertItem item : updateItems) {
                boolean exists = existingUpdateOrganizationRoles.stream()
                        .anyMatch(existing -> existing.getOrganizationId().equals(item.getOrganizationId()) && 
                                             existing.getRoleId().equals(item.getRoleId()));
                
                if (!exists) {
                    String errorMsg = String.format("업데이트할 조직-역할 매핑이 존재하지 않습니다: organizationId=%s, roleId=%s", 
                                                  item.getOrganizationId(), item.getRoleId());
                    result.addError(item.getTempId(), errorMsg, "UPDATE_TARGET_NOT_FOUND");
                    
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                    }
                }
            }
            
            // 존재하지 않는 항목들을 UPDATE 대상에서 제외
            updateItems = updateItems.stream()
                    .filter(item -> existingUpdateOrganizationRoles.stream()
                            .anyMatch(existing -> existing.getOrganizationId().equals(item.getOrganizationId()) && 
                                                 existing.getRoleId().equals(item.getRoleId())))
                    .collect(Collectors.toList());
        }

        // === DELETE 검증 및 처리 ===
        if(!deleteItems.isEmpty()){
            // 삭제 대상이 실제로 존재하는지 검증
            List<OrganizationRole> tempDeleteOrganizationRoles = deleteItems.stream()
                    .map(item -> {
                        OrganizationRole tempOrganizationRole = new OrganizationRole();
                        tempOrganizationRole.setOrganizationId(item.getOrganizationId());
                        tempOrganizationRole.setRoleId(item.getRoleId());
                        return tempOrganizationRole;
                    })
                    .toList();
            
            List<OrganizationRole> existingDeleteOrganizationRoles = organizationRoleMapper.selectExistingOrganizationRolesByCompositeKeys(tempDeleteOrganizationRoles);
            
            // 존재하지 않는 항목들에 대해 에러 처리
            for (OrganizationRoleUpsertItem item : deleteItems) {
                boolean exists = existingDeleteOrganizationRoles.stream()
                        .anyMatch(existing -> existing.getOrganizationId().equals(item.getOrganizationId()) && 
                                             existing.getRoleId().equals(item.getRoleId()));
                
                if (!exists) {
                    String errorMsg = String.format("삭제할 조직-역할 매핑이 존재하지 않습니다: organizationId=%s, roleId=%s", 
                                                  item.getOrganizationId(), item.getRoleId());
                    result.addError(item.getTempId(), errorMsg, "DELETE_TARGET_NOT_FOUND");
                    
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                    }
                }
            }
            
            // 존재하지 않는 항목들을 DELETE 대상에서 제외
            deleteItems = deleteItems.stream()
                    .filter(item -> existingDeleteOrganizationRoles.stream()
                            .anyMatch(existing -> existing.getOrganizationId().equals(item.getOrganizationId()) && 
                                                 existing.getRoleId().equals(item.getRoleId())))
                    .collect(Collectors.toList());
        }

        // 실제 처리할 데이터 객체들
        List<OrganizationRole> toInsert = new ArrayList<>();
        List<OrganizationRole> toUpdate = new ArrayList<>();
        List<OrganizationRole> toDelete = new ArrayList<>();

        // INSERT 처리
        for (OrganizationRoleUpsertItem item : insertItems) {
            try {
                OrganizationRole newOrganizationRole = createOrganizationRoleFromItem(item, currentUserId, now);
                toInsert.add(newOrganizationRole);
            } catch (Exception e) {
                log.error("Error processing insert item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "INSERT_PROCESSING_ERROR");
                if (userOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "INSERT 처리 중 오류가 발생했습니다");
                }
            }
        }

        // UPDATE 처리
        for (OrganizationRoleUpsertItem item : updateItems) {
            try {
                if (item.getOrganizationId() == null) {
                    result.addError(item.getTempId(), "UPDATE 작업에는 organizationId가 필요합니다", "MISSING_ORGANIZATION_ID");
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 작업에는 organizationId가 필요합니다");
                    }
                } else {
                    OrganizationRole updatedOrganizationRole = updateOrganizationRoleFromItem(item, currentUserId, now);
                    toUpdate.add(updatedOrganizationRole);
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
        for (OrganizationRoleUpsertItem item : deleteItems) {
            try {
                if (item.getOrganizationId() == null) {
                    result.addError(item.getTempId(), "DELETE 작업에는 organizationId가 필요합니다", "MISSING_ORGANIZATION_ID");
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 작업에는 organizationId가 필요합니다");
                    }
                } else {
                    OrganizationRole softDeleteOrganizationRole = softDeleteOrganizationRoleFromItem(item, currentUserId, now);
                    toDelete.add(softDeleteOrganizationRole);
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
                insertCount = organizationRoleMapper.insertOrganizationRolesBulk(toInsert);
                result.incrementSuccess(insertCount);
                log.info("Bulk insert completed: {} items", insertCount);
            }

            if (!toUpdate.isEmpty()) {
                updateCount = organizationRoleMapper.updateOrganizationRolesBulk(toUpdate);
                result.incrementSuccess(updateCount);
                log.info("Bulk update completed: {} items", updateCount);
            }

            if (!toDelete.isEmpty()) {
                deleteCount = organizationRoleMapper.softDeleteOrganizationRolesBulk(toDelete);
                result.incrementSuccess(deleteCount);
                log.info("Bulk soft delete completed: {} items", deleteCount);
            }

            result.getSummary().put("insertCount", insertCount);
            result.getSummary().put("updateCount", updateCount);
            result.getSummary().put("deleteCount", deleteCount);

        } catch (Exception e) {
            log.error("Error during batch database operation", e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "일괄 처리 중 오류가 발생했습니다");
        }

        // 완료 처리
        result.markComplete();
        log.info("Bulk upsert completed: {}", result);

        return result;
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
            case "organizationname" -> "organization_name";
            case "rolename" -> "role_name";
            case "delYn" -> "del_yn";
            case "credt" -> "reg_dt";
            case "chgdt" -> "chg_dt";
            default -> "reg_dt"; // 기본 정렬 사용
        };
    }

    private OrganizationRole createOrganizationRoleFromItem(OrganizationRoleUpsertItem item, Integer currentUserId, LocalDateTime now) {
        OrganizationRole organizationRole = new OrganizationRole();
        organizationRole.setOrganizationId(item.getOrganizationId());
        organizationRole.setRoleId(item.getRoleId());
        organizationRole.setDelYn(item.getDelYn());
        organizationRole.setRegId(currentUserId);
        organizationRole.setChgId(currentUserId);
        organizationRole.setRegDt(now);
        organizationRole.setChgDt(now);

        return organizationRole;
    }

    private OrganizationRole updateOrganizationRoleFromItem(OrganizationRoleUpsertItem item, Integer currentUserId, LocalDateTime now) {
        OrganizationRole organizationRole = new OrganizationRole();
        organizationRole.setOrganizationId(item.getOrganizationId());
        organizationRole.setRoleId(item.getRoleId());
        organizationRole.setDelYn(item.getDelYn());
        organizationRole.setChgId(currentUserId);
        organizationRole.setChgDt(now);

        // Only clear del fields when activating
        if (item.getDelYn() != null && item.getDelYn().equals("N")) {
            organizationRole.setDelId(null);
            organizationRole.setDelDt(null);
        }
        // When deactivating or no change, don't touch del fields (preserve existing DB values)
        
        return organizationRole;
    }

    private OrganizationRole softDeleteOrganizationRoleFromItem(OrganizationRoleUpsertItem item, Integer currentUserId, LocalDateTime now) {
        OrganizationRole organizationRole = new OrganizationRole();
        organizationRole.setOrganizationId(item.getOrganizationId());
        organizationRole.setRoleId(item.getRoleId());
        organizationRole.setDelYn("Y");
        organizationRole.setChgId(currentUserId);
        organizationRole.setChgDt(now);
        organizationRole.setDelId(currentUserId);
        organizationRole.setDelDt(now);

        return organizationRole;
    }
}

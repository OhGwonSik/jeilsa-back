package com.common.auth.organization.service;


import com.common.auth.common.annotation.AutoPaging;
import com.common.auth.common.dto.OperationOptions;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.organization.domain.Organization;
import com.common.auth.organization.dto.OrganizationGridFilterDTO;
import com.common.auth.organization.dto.OrganizationGridUpsertRequest;
import com.common.auth.organization.dto.OrganizationResponse;
import com.common.auth.organization.dto.OrganizationUpsertItem;
import com.common.auth.organization.mapper.OrganizationMapper;
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
public class OrganizationGridService {
    //----- DI Fields -----//
    private final OrganizationMapper organizationMapper;

    @AutoPaging(defaultSort = "organization_name", columnMapperMethod = "mapSortColumn")
    public PageInfo<OrganizationResponse> selectOrganizationsWithFilter(OrganizationGridFilterDTO filter) {
        log.debug("Finding organizations with filter: {}", filter);
        
        List<OrganizationResponse> organizationResponses = organizationMapper.selectOrganizationsWithFilter(filter);
    
        return new PageInfo<>(organizationResponses);
    }

    public OperationResult upsertOrganizationsBulk(OrganizationGridUpsertRequest request) {
        log.info("Bulk upsert organizations: {} items", request.getItems().size());

        OperationResult result = new OperationResult();
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();
        OperationOptions organizationOptions = request.getOptions();

        // 요청 데이터 분류
        Map<String, List<OrganizationUpsertItem>> itemsByStatus = request.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getStatus().toUpperCase()));

        List<OrganizationUpsertItem> insertItems = itemsByStatus.getOrDefault("ADDED", Collections.emptyList());
        List<OrganizationUpsertItem> updateItems = itemsByStatus.getOrDefault("CHANGED", Collections.emptyList());
        List<OrganizationUpsertItem> deleteItems = itemsByStatus.getOrDefault("DELETED", Collections.emptyList());

        // === INSERT 검증 및 처리 ===
        if (!insertItems.isEmpty()) {
            // 요청한 organizationId 목록
            List<Integer> insertOrganizationIdList = insertItems.stream()
                    .map(OrganizationUpsertItem::getOrganizationId)
                    .collect(Collectors.toList());
            Set<Integer> insertOrganizationIdSet = new HashSet<>(insertOrganizationIdList);

            // 요청한 organizationName 목록
            List<String> insertOrganizationNameList = insertItems.stream()
                    .map(OrganizationUpsertItem::getOrganizationName)
                    .collect(Collectors.toList());
            Set<String> insertOrganizationNameSet = new HashSet<>(insertOrganizationNameList);

            // 중복 체크용 데이터
            List<Integer> existingInsertOrganizationIdList = organizationMapper.selectExistOrganizationIdListByOrganizationIds(insertOrganizationIdList);
            List<String> existingInsertOrganizationNameList = organizationMapper.selectExistOrganizationNameListByOrganizationNames(insertOrganizationNameList);

            // 중단 설정시 validation 체크
            if (organizationOptions.isStopOnError()) {
                if (insertOrganizationIdList.size() != insertOrganizationIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "INSERT 요청에서 중복된 조직ID가 있습니다.");
                }
                if (insertOrganizationNameList.size() != insertOrganizationNameSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "INSERT 요청에서 중복된 조직명이 있습니다.");
                }
                if (!existingInsertOrganizationIdList.isEmpty()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("INSERT: 데이터베이스에 중복된 조직ID가 있습니다", existingInsertOrganizationIdList));
                }
                if (!existingInsertOrganizationNameList.isEmpty()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("INSERT: 데이터베이스에 중복된 조직명이 있습니다", existingInsertOrganizationNameList));
                }
            }
        }

        // === UPDATE 검증 및 처리 ===
        if (!updateItems.isEmpty()) {
            // 요청한 organizationId 목록
            List<Integer> updateOrganizationIdList = updateItems.stream()
                    .map(OrganizationUpsertItem::getOrganizationId)
                    .collect(Collectors.toList());
            Set<Integer> updateOrganizationIdSet = new HashSet<>(updateOrganizationIdList);

            // 요청한 organizationName 목록 (null 제외)
            List<String> updateOrganizationNameList = updateItems.stream()
                    .filter(item -> item.getOrganizationName() != null)
                    .map(OrganizationUpsertItem::getOrganizationName)
                    .collect(Collectors.toList());
            Set<String> updateOrganizationNameSet = new HashSet<>(updateOrganizationNameList);

            // 중복 체크용 데이터
            List<Integer> existingUpdateOrganizationIdList = organizationMapper.selectExistOrganizationIdListByOrganizationIds(updateOrganizationIdList);
            Set<Integer> existingUpdateOrganizationIdSet = new HashSet<>(existingUpdateOrganizationIdList);
            
            // organizationName 충돌 체크 (업데이트용)
            List<Organization> conflictOrganizationList = organizationMapper.selectOrganizationListByOrganizationNamesAndNotInOrganizationIds(updateItems);

            // 중단 설정시 validation 체크
            if (organizationOptions.isStopOnError()) {
                if (updateOrganizationIdList.size() != updateOrganizationIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 요청에서 중복된 조직ID가 있습니다.");
                }
                if (!updateOrganizationNameList.isEmpty() && updateOrganizationNameList.size() != updateOrganizationNameSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 요청에서 중복된 조직명이 있습니다.");
                }
                if (updateOrganizationIdList.size() != existingUpdateOrganizationIdList.size()) {
                    Set<Integer> nonExistentIdSet = new HashSet<>(updateOrganizationIdSet);
                    nonExistentIdSet.removeAll(existingUpdateOrganizationIdSet);
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("UPDATE: 데이터베이스에 존재하지 않는 조직ID가 있습니다", new ArrayList<>(nonExistentIdSet)));
                }
                if (!conflictOrganizationList.isEmpty()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("UPDATE: 데이터베이스에 충돌되는 조직명이 있습니다", conflictOrganizationList.stream().map(Organization::getOrganizationName).collect(Collectors.toList())));
                }
            }
        }

        // === DELETE 검증 및 처리 ===
        if (!deleteItems.isEmpty()) {
            // 요청한 organizationId 목록
            List<Integer> deleteOrganizationIdList = deleteItems.stream()
                    .map(OrganizationUpsertItem::getOrganizationId)
                    .collect(Collectors.toList());
            Set<Integer> deleteOrganizationIdSet = new HashSet<>(deleteOrganizationIdList);

            // 중복 체크용 데이터
            List<Integer> existingDeleteOrganizationIdList = organizationMapper.selectExistOrganizationIdListByOrganizationIds(deleteOrganizationIdList);

            // 중단 설정시 validation 체크
            if (organizationOptions.isStopOnError()) {
                if (deleteOrganizationIdList.size() != deleteOrganizationIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 요청에서 중복된 조직ID가 있습니다.");
                }
                if (deleteOrganizationIdList.size() != existingDeleteOrganizationIdList.size()) {
                    Set<Integer> nonExistentIdSet = new HashSet<>(deleteOrganizationIdSet);
                    nonExistentIdSet.removeAll(new HashSet<>(existingDeleteOrganizationIdList));
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("DELETE: 데이터베이스에 존재하지 않는 조직ID가 있습니다", new ArrayList<>(nonExistentIdSet)));
                }
            }
        }

        // 실제 처리할 데이터 객체들
        List<Organization> toInsert = new ArrayList<>();
        List<Organization> toUpdate = new ArrayList<>();
        List<Organization> toDelete = new ArrayList<>();

        // INSERT 처리
        for (OrganizationUpsertItem item : insertItems) {
            try {
                Organization newOrganization = createOrganizationFromItem(item, currentUserId, now);
                toInsert.add(newOrganization);
            } catch (Exception e) {
                log.error("Error processing insert item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "INSERT_PROCESSING_ERROR");
                if (organizationOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "INSERT 처리 중 오류가 발생했습니다");
                }
            }
        }

        // UPDATE 처리
        for (OrganizationUpsertItem item : updateItems) {
            try {
                if (item.getOrganizationId() == null) {
                    result.addError(item.getTempId(), "UPDATE 작업에는 organizationId가 필요합니다", "MISSING_ORGANIZATION_ID");
                    if (organizationOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 작업에는 organizationId가 필요합니다");
                    }
                } else {
                    Organization updatedOrganization = updateOrganizationFromItem(item, currentUserId, now);
                    toUpdate.add(updatedOrganization);
                }
            } catch (Exception e) {
                log.error("Error processing update item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "UPDATE_PROCESSING_ERROR");
                if (organizationOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "UPDATE 처리 중 오류가 발생했습니다");
                }
            }
        }

        // DELETE 처리 (Soft Delete - delYn = false로 변경)
        for (OrganizationUpsertItem item : deleteItems) {
            try {
                if (item.getOrganizationId() == null) {
                    result.addError(item.getTempId(), "DELETE 작업에는 organizationId가 필요합니다", "MISSING_ORGANIZATION_ID");
                    if (organizationOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 작업에는 organizationId가 필요합니다");
                    }
                } else {
                    Organization softDeleteOrganization = softDeleteOrganizationFromItem(item, currentUserId, now);
                    toDelete.add(softDeleteOrganization);
                }
            } catch (Exception e) {
                log.error("Error processing delete item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "DELETE_PROCESSING_ERROR");
                if (organizationOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "DELETE 처리 중 오류가 발생했습니다");
                }
            }
        }

        // 데이터베이스 작업 수행
        try {
            int insertCount = 0, updateCount = 0, deleteCount = 0;

            if (!toInsert.isEmpty()) {
                insertCount = organizationMapper.insertOrganizationsBulk(toInsert);
                result.incrementSuccess(insertCount);
                log.info("Bulk insert completed: {} items", insertCount);
            }

            if (!toUpdate.isEmpty()) {
                updateCount = organizationMapper.updateOrganizationsBulk(toUpdate);
                result.incrementSuccess(updateCount);
                log.info("Bulk update completed: {} items", updateCount);
            }

            if (!toDelete.isEmpty()) {
                deleteCount = organizationMapper.softDeleteOrganizationsBulk(toDelete);
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

    private Organization createOrganizationFromItem(OrganizationUpsertItem item, Integer currentUserId, LocalDateTime now) {
        Organization organization = new Organization(item.getOrganizationName(), item.getDescription());
        organization.setOrganizationId(item.getOrganizationId());
        organization.setDelYn(item.getDelYn());
        organization.setRegId(currentUserId);
        organization.setChgId(currentUserId);
        organization.setRegDt(now);
        organization.setChgDt(now);

        return organization;
    }

    private Organization updateOrganizationFromItem(OrganizationUpsertItem item, Integer currentUserId, LocalDateTime now) {
        Organization organization = new Organization();
        organization.setOrganizationId(item.getOrganizationId());
        organization.setOrganizationName(item.getOrganizationName());
        organization.setDescription(item.getDescription());
        organization.setDelYn(item.getDelYn());
        organization.setChgId(currentUserId);
        organization.setChgDt(now);

        // Only clear del fields when activating
        if (item.getDelYn() != null && item.getDelYn().equals("N")) {
            organization.setDelId(null);
            organization.setDelDt(null);
        }
        // When deactivating or no change, don't touch del fields (preserve existing DB values)

        return organization;
    }
    
    private Organization softDeleteOrganizationFromItem(OrganizationUpsertItem item, Integer currentUserId, LocalDateTime now) {
        Organization organization = new Organization();
        organization.setOrganizationId(item.getOrganizationId());
        organization.setDelYn("Y");
        organization.setChgId(currentUserId);
        organization.setChgDt(now);
        organization.setDelId(currentUserId);
        organization.setDelDt(now);
        return organization;
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
            return "organization_name"; // 기본 정렬 컬럼
        }
        
        return switch (sortBy.toLowerCase()) {
            case "organizationname" -> "organization_name";
            case "description" -> "description";
            case "delYn" -> "del_yn";
            case "credt" -> "reg_dt";
            case "chgdt" -> "chg_dt";
            case "deldt" -> "del_dt";
            default -> null; // null 반환 시 기본 정렬 사용
        };
    }
}
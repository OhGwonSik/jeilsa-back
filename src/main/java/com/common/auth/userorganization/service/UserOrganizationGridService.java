package com.common.auth.userorganization.service;


import com.common.auth.common.annotation.AutoPaging;
import com.common.auth.common.dto.OperationOptions;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.userorganization.domain.UserOrganization;
import com.common.auth.userorganization.dto.UserOrganizationGridFilterDTO;
import com.common.auth.userorganization.dto.UserOrganizationGridUpsertRequest;
import com.common.auth.userorganization.dto.UserOrganizationResponse;
import com.common.auth.userorganization.dto.UserOrganizationUpsertItem;
import com.common.auth.userorganization.mapper.UserOrganizationMapper;
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
public class UserOrganizationGridService {
    //----- DI Fields -----//
    private final UserOrganizationMapper userOrganizationMapper;

    @AutoPaging(defaultSort = "reg_dt", columnMapperMethod = "mapSortColumn")
    public PageInfo<UserOrganizationResponse> selectUserOrganizationsWithFilter(UserOrganizationGridFilterDTO filter) {
        log.debug("Finding user organizations with filter: {}", filter);
        
        List<UserOrganizationResponse> userOrganizationResponses = userOrganizationMapper.selectUserOrganizationsWithFilter(filter);

        return new PageInfo<>(userOrganizationResponses);
    }

    public OperationResult upsertUserOrganizationsBulk(UserOrganizationGridUpsertRequest request) {
        log.info("Batch upsert user organizations: {} items", request.getItems().size());

        OperationResult result = new OperationResult();
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();
        OperationOptions userOptions = request.getOptions();

        // 요청 데이터 분류
        Map<String, List<UserOrganizationUpsertItem>> itemsByStatus = request.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getStatus().toUpperCase()));

        List<UserOrganizationUpsertItem> insertItems = itemsByStatus.getOrDefault("ADDED", Collections.emptyList());
        List<UserOrganizationUpsertItem> updateItems = itemsByStatus.getOrDefault("CHANGED", Collections.emptyList());
        List<UserOrganizationUpsertItem> deleteItems = itemsByStatus.getOrDefault("DELETED", Collections.emptyList());

        // === INSERT 검증 및 처리 ===
        if (!insertItems.isEmpty()) {
            // 중복 체크를 위해 임시 UserOrganization 객체들 생성
            List<UserOrganization> tempUserOrganizations = insertItems.stream()
                    .map(item -> {
                        UserOrganization tempUserOrganization = new UserOrganization();
                        tempUserOrganization.setMemberId(item.getMemberId());
                        tempUserOrganization.setOrganizationId(item.getOrganizationId());
                        return tempUserOrganization;
                    })
                    .toList();
            
            // 기존에 존재하는 사용자-조직 매핑 조회
            List<UserOrganization> existingUserOrganizations = userOrganizationMapper.selectExistingUserOrganizationsByCompositeKeys(tempUserOrganizations);
            
            if (!existingUserOrganizations.isEmpty()) {
                // 중복된 항목들에 대해 에러 처리
                for (UserOrganization existing : existingUserOrganizations) {
                    UserOrganizationUpsertItem duplicateItem = insertItems.stream()
                            .filter(item -> item.getMemberId().equals(existing.getMemberId()) &&
                                           item.getOrganizationId().equals(existing.getOrganizationId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (duplicateItem != null) {
                        String errorMsg = String.format("사용자-조직 매핑이 이미 존재합니다: userId=%s, organizationId=%s", 
                                                      existing.getMemberId(), existing.getOrganizationId());
                        result.addError(duplicateItem.getTempId(), errorMsg, "DUPLICATE_USER_ORGANIZATION");
                        
                        if (userOptions.isStopOnError()) {
                            throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                        }
                    }
                }
                
                // 중복된 항목들을 INSERT 대상에서 제외
                insertItems = insertItems.stream()
                        .filter(item -> existingUserOrganizations.stream()
                                .noneMatch(existing -> existing.getMemberId().equals(item.getMemberId()) &&
                                                      existing.getOrganizationId().equals(item.getOrganizationId())))
                        .collect(Collectors.toList());
            }
        }

        // === UPDATE 검증 및 처리 ===
        if(!updateItems.isEmpty()){
            // 업데이트 대상이 실제로 존재하는지 검증
            List<UserOrganization> tempUpdateUserOrganizations = updateItems.stream()
                    .map(item -> {
                        UserOrganization tempUserOrganization = new UserOrganization();
                        tempUserOrganization.setMemberId(item.getMemberId());
                        tempUserOrganization.setOrganizationId(item.getOrganizationId());
                        return tempUserOrganization;
                    })
                    .toList();
            
            List<UserOrganization> existingUpdateUserOrganizations = userOrganizationMapper.selectExistingUserOrganizationsByCompositeKeys(tempUpdateUserOrganizations);
            
            // 존재하지 않는 항목들에 대해 에러 처리
            for (UserOrganizationUpsertItem item : updateItems) {
                boolean exists = existingUpdateUserOrganizations.stream()
                        .anyMatch(existing -> existing.getMemberId().equals(item.getMemberId()) &&
                                             existing.getOrganizationId().equals(item.getOrganizationId()));
                
                if (!exists) {
                    String errorMsg = String.format("업데이트할 사용자-조직 매핑이 존재하지 않습니다: userId=%s, organizationId=%s", 
                                                  item.getMemberId(), item.getOrganizationId());
                    result.addError(item.getTempId(), errorMsg, "UPDATE_TARGET_NOT_FOUND");
                    
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                    }
                }
            }
            
            // 존재하지 않는 항목들을 UPDATE 대상에서 제외
            updateItems = updateItems.stream()
                    .filter(item -> existingUpdateUserOrganizations.stream()
                            .anyMatch(existing -> existing.getMemberId().equals(item.getMemberId()) &&
                                                 existing.getOrganizationId().equals(item.getOrganizationId())))
                    .collect(Collectors.toList());
        }

        // === DELETE 검증 및 처리 ===
        if(!deleteItems.isEmpty()){
            // 삭제 대상이 실제로 존재하는지 검증
            List<UserOrganization> tempDeleteUserOrganizations = deleteItems.stream()
                    .map(item -> {
                        UserOrganization tempUserOrganization = new UserOrganization();
                        tempUserOrganization.setMemberId(item.getMemberId());
                        tempUserOrganization.setOrganizationId(item.getOrganizationId());
                        return tempUserOrganization;
                    })
                    .toList();
            
            List<UserOrganization> existingDeleteUserOrganizations = userOrganizationMapper.selectExistingUserOrganizationsByCompositeKeys(tempDeleteUserOrganizations);
            
            // 존재하지 않는 항목들에 대해 에러 처리
            for (UserOrganizationUpsertItem item : deleteItems) {
                boolean exists = existingDeleteUserOrganizations.stream()
                        .anyMatch(existing -> existing.getMemberId().equals(item.getMemberId()) &&
                                             existing.getOrganizationId().equals(item.getOrganizationId()));
                
                if (!exists) {
                    String errorMsg = String.format("삭제할 사용자-조직 매핑이 존재하지 않습니다: userId=%s, organizationId=%s", 
                                                  item.getMemberId(), item.getOrganizationId());
                    result.addError(item.getTempId(), errorMsg, "DELETE_TARGET_NOT_FOUND");
                    
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                    }
                }
            }
            
            // 존재하지 않는 항목들을 DELETE 대상에서 제외
            deleteItems = deleteItems.stream()
                    .filter(item -> existingDeleteUserOrganizations.stream()
                            .anyMatch(existing -> existing.getMemberId().equals(item.getMemberId()) &&
                                                 existing.getOrganizationId().equals(item.getOrganizationId())))
                    .collect(Collectors.toList());
        }
        
        // 실제 처리할 데이터 객체들
        List<UserOrganization> toInsert = new ArrayList<>();
        List<UserOrganization> toUpdate = new ArrayList<>();
        List<UserOrganization> toDelete = new ArrayList<>();

        // INSERT 처리
        for (UserOrganizationUpsertItem item : insertItems) {
            try {
                UserOrganization newUserOrganization = createUserOrganizationFromItem(item, currentUserId, now);
                toInsert.add(newUserOrganization);
            } catch (Exception e) {
                log.error("Error processing insert item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "INSERT_PROCESSING_ERROR");
                if (userOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "INSERT 처리 중 오류가 발생했습니다");
                }
            }
        }

        // UPDATE 처리
        for (UserOrganizationUpsertItem item : updateItems) {
            try {
                if (item.getMemberId() == null) {
                    result.addError(item.getTempId(), "UPDATE 작업에는 userId가 필요합니다", "MISSING_USER_ID");
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 작업에는 userId가 필요합니다");
                    }
                } else {
                    UserOrganization updatedUserOrganization = updateUserOrganizationFromItem(item, currentUserId, now);
                    toUpdate.add(updatedUserOrganization);
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
        for (UserOrganizationUpsertItem item : deleteItems) {
            try {
                if (item.getMemberId() == null) {
                    result.addError(item.getTempId(), "DELETE 작업에는 userId가 필요합니다", "MISSING_USER_ID");
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 작업에는 userId가 필요합니다");
                    }
                } else {
                    UserOrganization softDeleteUserOrganization = softDeleteUserOrganizationFromItem(item, currentUserId, now);
                    toDelete.add(softDeleteUserOrganization);
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
                userOrganizationMapper.insertUserOrganizationsBulk(toInsert);
                insertCount = toInsert.size();
                result.incrementSuccess(insertCount);
                log.info("Bulk insert completed: {} items", insertCount);
            }

            if (!toUpdate.isEmpty()) {
                userOrganizationMapper.updateUserOrganizationsBulk(toUpdate);
                updateCount = toUpdate.size();
                result.incrementSuccess(updateCount);
                log.info("Bulk update completed: {} items", updateCount);
            }

            if (!toDelete.isEmpty()) {
                userOrganizationMapper.softDeleteUserOrganizationsBulk(toDelete);
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


    private UserOrganization createUserOrganizationFromItem(UserOrganizationUpsertItem item, Integer currentUserId, LocalDateTime now) {
        UserOrganization userOrganization = new UserOrganization();
        userOrganization.setMemberId(item.getMemberId());
        userOrganization.setOrganizationId(item.getOrganizationId());
        userOrganization.setDelYn(item.getDelYn());
        userOrganization.setRegId(currentUserId);
        userOrganization.setChgId(currentUserId);
        userOrganization.setRegDt(now);
        userOrganization.setChgDt(now);
        return userOrganization;
    }

    private UserOrganization updateUserOrganizationFromItem(UserOrganizationUpsertItem item, Integer currentUserId, LocalDateTime now) {
        UserOrganization userOrganization = new UserOrganization();
        userOrganization.setMemberId(item.getMemberId());
        userOrganization.setOrganizationId(item.getOrganizationId());
        userOrganization.setDelYn(item.getDelYn());
        userOrganization.setChgId(currentUserId);
        userOrganization.setChgDt(now);

        if (item.getDelYn().equals("N")) {
            userOrganization.setDelId(null);
            userOrganization.setDelDt(null);
        }
        
        return userOrganization;
    }

    private UserOrganization softDeleteUserOrganizationFromItem(UserOrganizationUpsertItem item, Integer currentUserId, LocalDateTime now) {
        UserOrganization userOrganization = new UserOrganization();
        userOrganization.setMemberId(item.getMemberId());
        userOrganization.setOrganizationId(item.getOrganizationId());
        userOrganization.setDelYn("Y");
        userOrganization.setChgId(currentUserId);
        userOrganization.setChgDt(now);
        userOrganization.setDelId(currentUserId);
        userOrganization.setDelDt(now);

        return userOrganization;
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
            case "organizationname" -> "organization_name";
            case "delYn" -> "del_yn";
            case "credt" -> "reg_dt";
            case "chgdt" -> "chg_dt";
            case "deldt" -> "del_dt";
            default -> null; // null 반환 시 기본 정렬 사용
        };
    }
}

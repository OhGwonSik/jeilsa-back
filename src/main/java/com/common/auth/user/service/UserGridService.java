package com.common.auth.user.service;


import com.common.auth.common.dto.OperationOptions;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.role.service.RoleService;
import com.common.auth.user.domain.User;
import com.common.auth.user.dto.UserGridUpsertRequest;
import com.common.auth.user.dto.UserUpsertItem;
import com.common.auth.user.mapper.UserMapper;
import com.jeil.delivery.domain.MemberDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserGridService {
    //----- DI Fields -----//
    private final RoleService roleService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    //----- Constants -----//
    // 임시 로직
    // 임시 비밀번호 suffix(id + 기본 비밀번호 조합)
    private final String DEFAULT_PASSWORD_SUFFIX = "1234!@";

//    @AutoPaging(defaultSort = "email", columnMapperMethod = "mapSortColumn")
//    public PageInfo<UserResponse> selectUsersWithFilter(UserGridFilterDTO filter) {
//        log.debug("Finding users with filter: {}", filter);
//
//        List<UserResponse> userResponses = userMapper.selectUsersWithFilter(filter);
//
//        return new PageInfo<>(userResponses);
//    }

    public OperationResult upsertUsersBulk(UserGridUpsertRequest request) {
        log.info("Bulk upsert users: {} items", request.getItems().size());

        OperationResult result = new OperationResult();
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();
        OperationOptions userOptions = request.getOptions();

        // 요청 데이터 분류
        Map<String, List<UserUpsertItem>> itemsByStatus = request.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getStatus().toUpperCase()));

        List<UserUpsertItem> insertItems = itemsByStatus.getOrDefault("ADDED", Collections.emptyList());
        List<UserUpsertItem> updateItems = itemsByStatus.getOrDefault("CHANGED", Collections.emptyList());
        List<UserUpsertItem> deleteItems = itemsByStatus.getOrDefault("DELETED", Collections.emptyList());

        for(UserUpsertItem item : insertItems){
            String password = item.getPassword();
            // password가 null이면 기본값 생성
            if(password == null){
                item.setPassword(item.getEmail() + DEFAULT_PASSWORD_SUFFIX);
            }
            
            // password hash 생성
            item.setPasswordHash(passwordEncoder.encode(password));
        }
        
        // update password hash 생성
        for(UserUpsertItem item : updateItems){
            if(item.getPassword() != null){
                item.setPasswordHash(passwordEncoder.encode(item.getPassword()));
            }
        }

        // === INSERT 검증 및 처리 ===
        if (!insertItems.isEmpty()) {
            // 요청한 userId 목록
            List<Integer> insertUserIdList = insertItems.stream()
                    .map(UserUpsertItem::getMemberId)
                    .collect(Collectors.toList());
            Set<Integer> insertUserIdSet = new HashSet<>(insertUserIdList);

            // 요청한 email 목록
            List<String> insertEmailList = insertItems.stream()
                    .map(UserUpsertItem::getEmail)
                    .collect(Collectors.toList());
            Set<String> insertEmailSet = new HashSet<>(insertEmailList);

            // 중복 체크용 데이터
            List<Integer> existingInsertUserIdList = userMapper.selectExistUserIdListByMemberIds(insertUserIdList);
            List<String> existingInsertEmailList = userMapper.selectExistEmailListByEmails(insertEmailList);

            // 중단 설정시 validation 체크
            if (userOptions.isStopOnError()) {
                if (insertUserIdList.size() != insertUserIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "INSERT 요청에서 중복된 사용자ID가 있습니다.");
                }
                if (insertEmailList.size() != insertEmailSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "INSERT 요청에서 중복된 이메일이 있습니다.");
                }
                if (!existingInsertUserIdList.isEmpty()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("INSERT: 데이터베이스에 중복된 사용자ID가 있습니다", existingInsertUserIdList));
                }
                if (!existingInsertEmailList.isEmpty()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("INSERT: 데이터베이스에 중복된 이메일이 있습니다", existingInsertEmailList));
                }
            }
        }

        // === UPDATE 검증 및 처리 ===
        if (!updateItems.isEmpty()) {
            // 요청한 userId 목록
            List<Integer> updateUserIdList = updateItems.stream()
                    .map(UserUpsertItem::getMemberId)
                    .collect(Collectors.toList());
            Set<Integer> updateUserIdSet = new HashSet<>(updateUserIdList);

            // 요청한 email 목록 (null 제외)
            List<String> updateEmailList = updateItems.stream()
                    .filter(item -> item.getEmail() != null)
                    .map(UserUpsertItem::getEmail)
                    .collect(Collectors.toList());
            Set<String> updateEmailSet = new HashSet<>(updateEmailList);

            // 중복 체크용 데이터
            List<Integer> existingUpdateUserIdList = userMapper.selectExistUserIdListByMemberIds(updateUserIdList);
            Set<Integer> existingUpdateUserIdSet = new HashSet<>(existingUpdateUserIdList);
            
            // email 충돌 체크 (업데이트용)
            List<User> conflictUserList = userMapper.selectUserListByEmailsAndNotInUserIds(updateItems);

            // 중단 설정시 validation 체크
            if (userOptions.isStopOnError()) {
                if (updateUserIdList.size() != updateUserIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 요청에서 중복된 사용자ID가 있습니다.");
                }
                if (!updateEmailList.isEmpty() && updateEmailList.size() != updateEmailSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 요청에서 중복된 이메일이 있습니다.");
                }
                if (updateUserIdList.size() != existingUpdateUserIdList.size()) {
                    Set<Integer> nonExistentIdSet = new HashSet<>(updateUserIdSet);
                    nonExistentIdSet.removeAll(existingUpdateUserIdSet);
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("UPDATE: 데이터베이스에 존재하지 않는 사용자ID가 있습니다", new ArrayList<>(nonExistentIdSet)));
                }
                if (!conflictUserList.isEmpty()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("UPDATE: 데이터베이스에 충돌되는 이메일이 있습니다", conflictUserList.stream().map(User::getEmail).collect(Collectors.toList())));
                }
            }
        }

        // === DELETE 검증 및 처리 ===
        if (!deleteItems.isEmpty()) {
            // 요청한 userId 목록
            List<Integer> deleteUserIdList = deleteItems.stream()
                    .map(UserUpsertItem::getMemberId)
                    .collect(Collectors.toList());
            Set<Integer> deleteUserIdSet = new HashSet<>(deleteUserIdList);

            // 중복 체크용 데이터
            List<Integer> existingDeleteUserIdList = userMapper.selectExistUserIdListByMemberIds(deleteUserIdList);

            // 중단 설정시 validation 체크
            if (userOptions.isStopOnError()) {
                if (deleteUserIdList.size() != deleteUserIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 요청에서 중복된 사용자ID가 있습니다.");
                }
                if (deleteUserIdList.size() != existingDeleteUserIdList.size()) {
                    Set<Integer> nonExistentIdSet = new HashSet<>(deleteUserIdSet);
                    nonExistentIdSet.removeAll(new HashSet<>(existingDeleteUserIdList));
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("DELETE: 데이터베이스에 존재하지 않는 사용자ID가 있습니다", new ArrayList<>(nonExistentIdSet)));
                }
            }
        }

        // 실제 처리할 데이터 객체들
        List<MemberDTO> toInsert = new ArrayList<>();
        List<MemberDTO> toUpdate = new ArrayList<>();
        List<MemberDTO> toDelete = new ArrayList<>();

        // INSERT 처리
        for (UserUpsertItem item : insertItems) {
            try {
                MemberDTO memberDTO = createUserFromItem(item, currentUserId, now);
                toInsert.add(memberDTO);
            } catch (Exception e) {
                log.error("Error processing insert item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "INSERT_PROCESSING_ERROR");
                if (userOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "INSERT 처리 중 오류가 발생했습니다");
                }
            }
        }

        // UPDATE 처리
        for (UserUpsertItem item : updateItems) {
            try {
                if (item.getMemberId() == null) {
                    result.addError(item.getMemberId().toString(), "UPDATE 작업에는 userId가 필요합니다", "MISSING_USER_ID");
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 작업에는 userId가 필요합니다");
                    }
                } else {
                    MemberDTO memberDTO = updateUserFromItem(item, currentUserId, now);
                    toUpdate.add(memberDTO);
                }
            } catch (Exception e) {
                log.error("Error processing update item: {}", item, e);
                result.addError(item.getMemberId().toString(), e.getMessage(), "UPDATE_PROCESSING_ERROR");
                if (userOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "UPDATE 처리 중 오류가 발생했습니다");
                }
            }
        }

        // DELETE 처리 (Soft Delete - delYn = false로 변경)
        for (UserUpsertItem item : deleteItems) {
            try {
                if (item.getMemberId() == null) {
                    result.addError(item.getMemberId().toString(), "DELETE 작업에는 userId가 필요합니다", "MISSING_USER_ID");
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 작업에는 userId가 필요합니다");
                    }
                } else {
                    MemberDTO memberDTO = softDeleteUserFromItem(item, currentUserId, now);
                    toDelete.add(memberDTO);
                }
            } catch (Exception e) {
                log.error("Error processing delete item: {}", item, e);
                result.addError(item.getMemberId().toString(), e.getMessage(), "DELETE_PROCESSING_ERROR");
                if (userOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "DELETE 처리 중 오류가 발생했습니다");
                }
            }
        }

        // 기존 하드 DELETE 처리 (주석 처리)
        // for (UserUpsertItem item : deleteItems) {
        //     try {
        //         if (item.getUserId() == null) {
        //             result.addError(item.getTempId(), "DELETE 작업에는 userId가 필요합니다", "MISSING_USER_ID");
        //             if (userOptions.isStopOnError()) {
        //                 throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 작업에는 userId가 필요합니다");
        //             }
        //         } else {
        //             User existing = new MemberDTO();
        //             existing.setUserId(item.getUserId());
        //             toDelete.add(existing);
        //         }
        //     } catch (Exception e) {
        //         log.error("Error processing delete item: {}", item, e);
        //         result.addError(item.getTempId(), e.getMessage(), "DELETE_PROCESSING_ERROR");
        //         if (userOptions.isStopOnError()) {
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
                insertCount = userMapper.insertUsersBulk(toInsert);
                result.incrementSuccess(insertCount);
                log.info("Bulk insert completed: {} items", insertCount);

                // role 연결
                int userRoleCount = roleService.insertUsersDefaultRoles(toInsert);
                log.info("Bulk insert completed: {} items", userRoleCount);
            }

            if (!toUpdate.isEmpty()) {
                updateCount = userMapper.updateUsersBulk(toUpdate);
                result.incrementSuccess(updateCount);
                log.info("Bulk update completed: {} items", updateCount);
            }

            if (!toDelete.isEmpty()) {
                deleteCount = userMapper.softDeleteUsersBulk(toDelete);
                result.incrementSuccess(deleteCount);
                log.info("Bulk soft delete completed: {} items", deleteCount);
            }

            // 기존 하드 DELETE 작업 (주석 처리)
            // if (!toDelete.isEmpty()) {
            //     deleteCount = userMapper.deleteUsersBulk(toDelete);
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

    private MemberDTO createUserFromItem(UserUpsertItem item, Integer currentUserId, LocalDateTime now) {
        MemberDTO memberDTO = new MemberDTO(item.getUserName(), item.getEmail(), item.getPasswordHash());
        memberDTO.setMemberId(item.getMemberId());
        memberDTO.setTelNo(item.getTelNo());
        memberDTO.setMemberStatusCd(item.getUserStatusCd() == null ? User.STATUS_ACTIVE : item.getUserStatusCd());
        memberDTO.setDelYn(item.getDelYn());
        memberDTO.setRegId(currentUserId);
        memberDTO.setChgId(currentUserId);
        memberDTO.setRegDt(now);
        memberDTO.setChgDt(now);

        return memberDTO;
    }

    private MemberDTO updateUserFromItem(UserUpsertItem item, Integer currentUserId, LocalDateTime now) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberId(item.getMemberId());
        memberDTO.setName(item.getUserName());
        memberDTO.setEmail(item.getEmail());
        if (item.getPasswordHash() != null && !item.getPasswordHash().trim().isEmpty()) {
            memberDTO.setUserPw(item.getPasswordHash());
        }

        memberDTO.setTelNo(item.getTelNo());
        memberDTO.setMemberStatusCd(item.getUserStatusCd());
        memberDTO.setDelYn(item.getDelYn());
        memberDTO.setChgId(currentUserId);
        memberDTO.setChgDt(now);

        // Only clear del fields when activating
        if (item.getDelYn() != null && item.getDelYn().equals("N")) {
            memberDTO.setMemberStatusCd("ACTIVE");
            memberDTO.setDelId(0);
            memberDTO.setDelDt(null);
        } else if (item.getDelYn() != null && item.getDelYn().equals("Y")) {
            memberDTO.setMemberStatusCd("INACTIVE");
        }
        // When deactivating or no change, don't touch del fields (preserve existing DB values)
        
        return memberDTO;
    }

    private MemberDTO softDeleteUserFromItem(UserUpsertItem item, Integer currentUserId, LocalDateTime now) {
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberId(item.getMemberId());
        memberDTO.setDelYn("Y");
        memberDTO.setChgId(currentUserId);
        memberDTO.setChgDt(now);
        memberDTO.setMemberStatusCd("INACTIVE");
        memberDTO.setDelId(currentUserId);
        memberDTO.setDelDt(now);

        return memberDTO;
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
            return "email"; // 기본 정렬 컬럼
        }
        
        return switch (sortBy.toLowerCase()) {
            case "username" -> "user_name";
            case "email" -> "email";
            case "phonenumber" -> "telNo";
            case "userstatuscd" -> "user_status_cd";
            case "lastlogindt" -> "last_login_dt";
            case "delYn" -> "del_yn";
            case "credt" -> "reg_dt";
            case "chgdt" -> "chg_dt";
            case "deldt" -> "del_dt";
            default -> null; // null 반환 시 기본 정렬 사용
        };
    }
}
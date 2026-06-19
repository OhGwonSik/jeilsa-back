package com.common.auth.rolemenu.service;


import com.common.auth.common.annotation.AutoPaging;
import com.common.auth.common.dto.OperationOptions;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.rolemenu.domain.RoleMenu;
import com.common.auth.rolemenu.dto.RoleMenuGridFilterDTO;
import com.common.auth.rolemenu.dto.RoleMenuGridUpsertRequest;
import com.common.auth.rolemenu.dto.RoleMenuResponse;
import com.common.auth.rolemenu.dto.RoleMenuUpsertItem;
import com.common.auth.rolemenu.mapper.RoleMenuMapper;
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
public class RoleMenuGridService {
    //----- DI Fields -----//
    private final RoleMenuMapper roleMenuMapper;

    @AutoPaging(defaultSort = "reg_dt", columnMapperMethod = "mapSortColumn")
    public PageInfo<RoleMenuResponse> selectRoleMenusWithFilter(RoleMenuGridFilterDTO filter) {
        log.debug("Finding role menus with filter: {}", filter);

        // 1. 헤더 메뉴(최상위)에 해당하는 role-menu 매핑 페이징 조회
        List<RoleMenuResponse> roleMenuHeaders = roleMenuMapper.selectRoleMenuHeadersWithFilter(filter);
        
        // 2. 조회된 헤더 메뉴의 ID 추출
        List<Integer> menuHeaderIds = roleMenuHeaders.stream()
                                                .map(RoleMenuResponse::getMenuId)
                                                .collect(Collectors.toList());

        // 3. 해당 헤더에 속한 모든 하위 메뉴의 role-menu 매핑 조회 (재귀적으로)
        List<RoleMenuResponse> allChildRoleMenus = new ArrayList<>();
        if (!menuHeaderIds.isEmpty()) {
            allChildRoleMenus = roleMenuMapper.selectAllDescendantRoleMenus(menuHeaderIds);
        }

        // 4. roleId별로 그룹화하여 메뉴 트리 구성
        Map<Integer, Map<Integer, RoleMenuResponse>> roleMenuMapByRole = new HashMap<>();
        
        // roleId별로 헤더 메뉴들을 그룹화
        Map<Integer, List<RoleMenuResponse>> headersByRole = roleMenuHeaders.stream()
                .collect(Collectors.groupingBy(RoleMenuResponse::getRoleId));
        
        // 각 역할별로 메뉴 맵 초기화
        for (Map.Entry<Integer, List<RoleMenuResponse>> entry : headersByRole.entrySet()) {
            Integer roleId = entry.getKey();
            List<RoleMenuResponse> headers = entry.getValue();
            
            Map<Integer, RoleMenuResponse> menuMap = new HashMap<>();
            headers.forEach(roleMenu -> {
                menuMap.put(roleMenu.getMenuId(), roleMenu);
                if (roleMenu.getItems() == null) {
                    roleMenu.setItems(new ArrayList<>());
                }
            });
            roleMenuMapByRole.put(roleId, menuMap);
        }

        // 5. 역할별로 하위 메뉴들을 부모에 연결
        Map<Integer, List<RoleMenuResponse>> childrenByRole = allChildRoleMenus.stream()
                .collect(Collectors.groupingBy(RoleMenuResponse::getRoleId));
                
        for (Map.Entry<Integer, List<RoleMenuResponse>> entry : childrenByRole.entrySet()) {
            Integer roleId = entry.getKey();
            List<RoleMenuResponse> children = entry.getValue();
            Map<Integer, RoleMenuResponse> roleMenuMap = roleMenuMapByRole.get(roleId);
            
            if (roleMenuMap != null) {
                for (RoleMenuResponse child : children) {
                    RoleMenuResponse parent = roleMenuMap.get(child.getParentId());
                    if (parent != null) {
                        if (parent.getItems() == null) {
                            parent.setItems(new ArrayList<>());
                        }
                        parent.getItems().add(child);
                        roleMenuMap.put(child.getMenuId(), child);
                    }
                }
            }
        }

        return new PageInfo<>(roleMenuHeaders);
    }

    public OperationResult upsertRoleMenusBulk(RoleMenuGridUpsertRequest request) {
        log.info("Batch upsert role menus: {} items", request.getItems().size());

        OperationResult result = new OperationResult();
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();
        OperationOptions userOptions = request.getOptions();

        // 요청 데이터 분류
        Map<String, List<RoleMenuUpsertItem>> itemsByStatus = request.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getStatus().toUpperCase()));

        List<RoleMenuUpsertItem> insertItems = itemsByStatus.getOrDefault("ADDED", Collections.emptyList());
        List<RoleMenuUpsertItem> updateItems = itemsByStatus.getOrDefault("CHANGED", Collections.emptyList());
        List<RoleMenuUpsertItem> deleteItems = itemsByStatus.getOrDefault("DELETED", Collections.emptyList());

        // === INSERT 검증 및 처리 ===
        if (!insertItems.isEmpty()) {
            // 중복 체크를 위해 임시 RoleMenu 객체들 생성
            List<RoleMenu> tempRoleMenus = insertItems.stream()
                    .map(item -> {
                        RoleMenu tempRoleMenu = new RoleMenu();
                        tempRoleMenu.setRoleId(item.getRoleId());
                        tempRoleMenu.setMenuId(item.getMenuId());
                        return tempRoleMenu;
                    })
                    .toList();
            
            // 기존에 존재하는 역할-메뉴 매핑 조회
            List<RoleMenu> existingRoleMenus = roleMenuMapper.selectExistingRoleMenusByCompositeKeys(tempRoleMenus);
            
            if (!existingRoleMenus.isEmpty()) {
                // 중복된 항목들에 대해 에러 처리
                for (RoleMenu existing : existingRoleMenus) {
                    RoleMenuUpsertItem duplicateItem = insertItems.stream()
                            .filter(item -> item.getRoleId().equals(existing.getRoleId()) && 
                                           item.getMenuId().equals(existing.getMenuId()))
                            .findFirst()
                            .orElse(null);
                    
                    if (duplicateItem != null) {
                        String errorMsg = String.format("역할-메뉴 매핑이 이미 존재합니다: roleId=%s, menuId=%s", 
                                                      existing.getRoleId(), existing.getMenuId());
                        result.addError(duplicateItem.getTempId(), errorMsg, "DUPLICATE_ROLE_MENU");
                        
                        if (userOptions.isStopOnError()) {
                            throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                        }
                    }
                }
                
                // 중복된 항목들을 INSERT 대상에서 제외
                insertItems = insertItems.stream()
                        .filter(item -> existingRoleMenus.stream()
                                .noneMatch(existing -> existing.getRoleId().equals(item.getRoleId()) && 
                                                      existing.getMenuId().equals(item.getMenuId())))
                        .collect(Collectors.toList());
            }
        }

        // === UPDATE 검증 및 처리 ===
        if(!updateItems.isEmpty()){
            // 업데이트 대상이 실제로 존재하는지 검증
            List<RoleMenu> tempUpdateRoleMenus = updateItems.stream()
                    .map(item -> {
                        RoleMenu tempRoleMenu = new RoleMenu();
                        tempRoleMenu.setRoleId(item.getRoleId());
                        tempRoleMenu.setMenuId(item.getMenuId());
                        return tempRoleMenu;
                    })
                    .toList();
            
            List<RoleMenu> existingUpdateRoleMenus = roleMenuMapper.selectExistingRoleMenusByCompositeKeys(tempUpdateRoleMenus);
            
            // 존재하지 않는 항목들에 대해 에러 처리
            for (RoleMenuUpsertItem item : updateItems) {
                boolean exists = existingUpdateRoleMenus.stream()
                        .anyMatch(existing -> existing.getRoleId().equals(item.getRoleId()) && 
                                             existing.getMenuId().equals(item.getMenuId()));
                
                if (!exists) {
                    String errorMsg = String.format("업데이트할 역할-메뉴 매핑이 존재하지 않습니다: roleId=%s, menuId=%s", 
                                                  item.getRoleId(), item.getMenuId());
                    result.addError(item.getTempId(), errorMsg, "UPDATE_TARGET_NOT_FOUND");
                    
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                    }
                }
            }
            
            // 존재하지 않는 항목들을 UPDATE 대상에서 제외
            updateItems = updateItems.stream()
                    .filter(item -> existingUpdateRoleMenus.stream()
                            .anyMatch(existing -> existing.getRoleId().equals(item.getRoleId()) && 
                                                 existing.getMenuId().equals(item.getMenuId())))
                    .collect(Collectors.toList());
        }

        // === DELETE 검증 및 처리 ===
        if(!deleteItems.isEmpty()){
            // 삭제 대상이 실제로 존재하는지 검증
            List<RoleMenu> tempDeleteRoleMenus = deleteItems.stream()
                    .map(item -> {
                        RoleMenu tempRoleMenu = new RoleMenu();
                        tempRoleMenu.setRoleId(item.getRoleId());
                        tempRoleMenu.setMenuId(item.getMenuId());
                        return tempRoleMenu;
                    })
                    .toList();
            
            List<RoleMenu> existingDeleteRoleMenus = roleMenuMapper.selectExistingRoleMenusByCompositeKeys(tempDeleteRoleMenus);
            
            // 존재하지 않는 항목들에 대해 에러 처리
            for (RoleMenuUpsertItem item : deleteItems) {
                boolean exists = existingDeleteRoleMenus.stream()
                        .anyMatch(existing -> existing.getRoleId().equals(item.getRoleId()) && 
                                             existing.getMenuId().equals(item.getMenuId()));
                
                if (!exists) {
                    String errorMsg = String.format("삭제할 역할-메뉴 매핑이 존재하지 않습니다: roleId=%s, menuId=%s", 
                                                  item.getRoleId(), item.getMenuId());
                    result.addError(item.getTempId(), errorMsg, "DELETE_TARGET_NOT_FOUND");
                    
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, errorMsg);
                    }
                }
            }
            
            // 존재하지 않는 항목들을 DELETE 대상에서 제외
            deleteItems = deleteItems.stream()
                    .filter(item -> existingDeleteRoleMenus.stream()
                            .anyMatch(existing -> existing.getRoleId().equals(item.getRoleId()) && 
                                                 existing.getMenuId().equals(item.getMenuId())))
                    .collect(Collectors.toList());
        }
        
        // 실제 처리할 데이터 객체들
        List<RoleMenu> toInsert = new ArrayList<>();
        List<RoleMenu> toUpdate = new ArrayList<>();
        List<RoleMenu> toDelete = new ArrayList<>();

        // INSERT 처리
        for (RoleMenuUpsertItem item : insertItems) {
            try {
                RoleMenu newRoleMenu = createRoleMenuFromItem(item, currentUserId, now);
                toInsert.add(newRoleMenu);
            } catch (Exception e) {
                log.error("Error processing insert item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "INSERT_PROCESSING_ERROR");
                if (userOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "INSERT 처리 중 오류가 발생했습니다");
                }
            }
        }

        // UPDATE 처리
        for (RoleMenuUpsertItem item : updateItems) {
            try {
                if (item.getRoleId() == null) {
                    result.addError(item.getTempId(), "UPDATE 작업에는 roleId가 필요합니다", "MISSING_ROLE_ID");
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 작업에는 roleId가 필요합니다");
                    }
                } else {
                    RoleMenu updatedRoleMenu = updateRoleMenuFromItem(item, currentUserId, now);
                    toUpdate.add(updatedRoleMenu);
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
        for (RoleMenuUpsertItem item : deleteItems) {
            try {
                if (item.getRoleId() == null) {
                    result.addError(item.getTempId(), "DELETE 작업에는 roleId가 필요합니다", "MISSING_ROLE_ID");
                    if (userOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 작업에는 roleId가 필요합니다");
                    }
                } else {
                    RoleMenu softDeleteRoleMenu = softDeleteRoleMenuFromItem(item, currentUserId, now);
                    toDelete.add(softDeleteRoleMenu);
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
                roleMenuMapper.insertRoleMenusBulk(toInsert);
                insertCount = toInsert.size();
                result.incrementSuccess(insertCount);
                log.info("Bulk insert completed: {} items", insertCount);
            }

            if (!toUpdate.isEmpty()) {
                roleMenuMapper.updateRoleMenusBulk(toUpdate);
                updateCount = toUpdate.size();
                result.incrementSuccess(updateCount);
                log.info("Bulk update completed: {} items", updateCount);
            }

            if (!toDelete.isEmpty()) {
                roleMenuMapper.softDeleteRoleMenusBulk(toDelete);
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
            case "menuname" -> "menu_name";
            case "delYn"-> "del_yn";
            case "credt" -> "reg_dt";
            case "chgdt" -> "chg_dt";
            case "deldt" -> "del_dt";
            default -> null; // null 반환 시 기본 정렬 사용
        };
    }

    private RoleMenu createRoleMenuFromItem(RoleMenuUpsertItem item, Integer currentUserId, LocalDateTime now) {
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRoleId(item.getRoleId());
        roleMenu.setMenuId(item.getMenuId());
        roleMenu.setDelYn(item.getDelYn());
        roleMenu.setRegId(currentUserId);
        roleMenu.setChgId(currentUserId);
        roleMenu.setRegDt(now);
        roleMenu.setChgDt(now);
        
        return roleMenu;
    }

    private RoleMenu updateRoleMenuFromItem(RoleMenuUpsertItem item, Integer currentUserId, LocalDateTime now) {
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRoleId(item.getRoleId());
        roleMenu.setMenuId(item.getMenuId());
        roleMenu.setDelYn(item.getDelYn());
        roleMenu.setChgId(currentUserId);
        roleMenu.setChgDt(now);

        if (item.getDelYn().equals("N")) {
            roleMenu.setDelId(null);
            roleMenu.setDelDt(null);
        }
        
        return roleMenu;
    }

    private RoleMenu softDeleteRoleMenuFromItem(RoleMenuUpsertItem item, Integer currentUserId, LocalDateTime now) {
        RoleMenu roleMenu = new RoleMenu();
        roleMenu.setRoleId(item.getRoleId());
        roleMenu.setMenuId(item.getMenuId());
        roleMenu.setDelYn("Y");
        roleMenu.setChgId(currentUserId);
        roleMenu.setChgDt(now);
        roleMenu.setDelId(currentUserId);
        roleMenu.setDelDt(now);

        return roleMenu;
    }
}

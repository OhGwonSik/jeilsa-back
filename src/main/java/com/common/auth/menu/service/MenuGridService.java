package com.common.auth.menu.service;


import com.common.auth.common.annotation.AutoPaging;
import com.common.auth.common.dto.OperationOptions;
import com.common.auth.common.dto.OperationResult;
import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.common.util.SecurityUtil;
import com.common.auth.menu.domain.Menu;
import com.common.auth.menu.dto.MenuGridFilterDTO;
import com.common.auth.menu.dto.MenuGridUpsertRequest;
import com.common.auth.menu.dto.MenuResponse;
import com.common.auth.menu.dto.MenuUpsertItem;
import com.common.auth.menu.mapper.MenuMapper;
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
public class MenuGridService {
    //----- DI Fields -----//
    private final MenuMapper menuMapper;

    @AutoPaging(defaultSort = "menu_order, menu_name", columnMapperMethod = "mapSortColumn")
    public PageInfo<MenuResponse> selectMenusWithFilter(MenuGridFilterDTO filter) {
        log.debug("Finding menus with filter: {}", filter);
        
        // 1. 헤더 메뉴 페이징 조회
        List<MenuResponse> menuHeaders = menuMapper.selectMenuHeadersWithFilter(filter);
        
        // 2. 조회된 헤더 메뉴의 ID 추출
        List<Integer> menuHeaderIds = menuHeaders.stream()
                                            .map(MenuResponse::getMenuId)
                                            .collect(Collectors.toList());

        // 3. 해당 헤더에 속한 모든 하위 메뉴 조회 (재귀적으로)
        List<MenuResponse> allChildMenus = new ArrayList<>();
        if (!menuHeaderIds.isEmpty()) {
            allChildMenus = menuMapper.selectAllDescendantMenus(menuHeaderIds);
        }

        // 4. 메뉴 트리 구성
        Map<Integer, MenuResponse> menuMap = new HashMap<>();
        menuHeaders.forEach(menu -> {
            menuMap.put(menu.getMenuId(), menu);
            if (menu.getItems() == null) {
                menu.setItems(new ArrayList<>());  // 자식 리스트 초기화
            }
        });

        // 5. 모든 하위 메뉴를 부모에 연결
        for (MenuResponse menu : allChildMenus) {
            MenuResponse parent = menuMap.get(menu.getParentId());
            if (parent != null) {
                if (parent.getItems() == null) {
                    parent.setItems(new ArrayList<>());
                }
                parent.getItems().add(menu);
                menuMap.put(menu.getMenuId(), menu);  // 현재 메뉴도 맵에 추가
            }
        }

        return new PageInfo<>(menuHeaders);
    }

        
    //     List<MenuResponse> menuResponses = menuMapper.selectMenusWithFilter(
    //         filter.getMenuIds(),
    //         filter.getMenuName(),
    //         filter.getMenuType(),
    //         filter.getMenuPath(),
    //         filter.getParentId(),
    //         filter.getDelYn()
    //     );
                
    //     return new PageInfo<>(menuResponses);
    // }

    public OperationResult upsertMenusBulk(MenuGridUpsertRequest request) {
        log.info("Bulk upsert menus: {} items", request.getItems().size());

        OperationResult result = new OperationResult();
        Integer currentUserId = SecurityUtil.getCurrentMemberId();
        LocalDateTime now = LocalDateTime.now();
        OperationOptions menuOptions = request.getOptions();

        // 요청 데이터 분류
        Map<String, List<MenuUpsertItem>> itemsByStatus = request.getItems().stream()
                .collect(Collectors.groupingBy(item -> item.getStatus().toUpperCase()));

        List<MenuUpsertItem> insertItems = itemsByStatus.getOrDefault("ADDED", Collections.emptyList());
        List<MenuUpsertItem> updateItems = itemsByStatus.getOrDefault("CHANGED", Collections.emptyList());
        List<MenuUpsertItem> deleteItems = itemsByStatus.getOrDefault("DELETED", Collections.emptyList());

        // === INSERT 검증 및 처리 ===
        if(!insertItems.isEmpty()) {
            // 요청한 menuId 목록
            List<Integer> insertMenuIdList = insertItems.stream()
                    .map(MenuUpsertItem::getMenuId)
                    .collect(Collectors.toList());
            Set<Integer> insertMenuIdSet = new HashSet<>(insertMenuIdList);

            // 요청한 menu_name + menu_type 복합 키 목록
            List<String> insertMenuCompositeKeyList = insertItems.stream()
                    .map(item -> item.getMenuName() + "|" + item.getMenuType())
                    .collect(Collectors.toList());
            Set<String> insertMenuCompositeKeySet = new HashSet<>(insertMenuCompositeKeyList);

            // 중복 체크용 데이터
            List<Integer> existingInsertMenuIdList = menuMapper.selectExistMenuIdListByMenuIds(insertMenuIdList);
            Set<Menu> existingInsertMenuCompositeKeySet = new HashSet<>(menuMapper.selectExistMenuListByMenuNamesAndMenuTypes(insertItems));

            // 중단 설정시 validation 체크
            if(menuOptions.isStopOnError()) {
                if(insertMenuIdList.size() != insertMenuIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "INSERT 요청에서 중복된 메뉴ID가 있습니다.");
                }
                if(insertMenuCompositeKeyList.size() != insertMenuCompositeKeySet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "INSERT 요청에서 중복된 메뉴명+메뉴타입 조합이 있습니다.");
                }
                if(!existingInsertMenuIdList.isEmpty()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("INSERT: 데이터베이스에 중복된 메뉴ID가 있습니다", existingInsertMenuIdList));
                }
                if(!existingInsertMenuCompositeKeySet.isEmpty()) {
                    List<String> duplicateKeys = existingInsertMenuCompositeKeySet.stream()
                            .map(menu -> menu.getMenuName() + "+" + menu.getMenuType())
                            .collect(Collectors.toList());
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("INSERT: 데이터베이스에 중복된 메뉴명+메뉴타입 조합이 있습니다", duplicateKeys));
                }
            }
        }

        // === UPDATE 검증 및 처리 ===
        if(!updateItems.isEmpty()) {
            // 요청한 menuId 목록
            List<Integer> updateMenuIdList = updateItems.stream()
                    .map(MenuUpsertItem::getMenuId)
                    .collect(Collectors.toList());
            Set<Integer> updateMenuIdSet = new HashSet<>(updateMenuIdList);

            // 요청한 menu_name + menu_type 복합 키 목록 (null 제외)
            List<String> updateMenuCompositeKeyList = updateItems.stream()
                    .filter(item -> item.getMenuName() != null && item.getMenuType() != null)
                    .map(item -> item.getMenuName() + "|" + item.getMenuType())
                    .collect(Collectors.toList());
            Set<String> updateMenuCompositeKeySet = new HashSet<>(updateMenuCompositeKeyList);

            // 중복 체크용 데이터
            List<Integer> existingUpdateMenuIdList = menuMapper.selectExistMenuIdListByMenuIds(updateMenuIdList);
            Set<Integer> existingUpdateMenuIdSet = new HashSet<>(existingUpdateMenuIdList);
            
            // menu_name + menu_type 충돌 체크 (업데이트용)
            List<Menu> conflictMenuList = menuMapper.selectMenuListByMenuNamesAndMenuTypesAndNotInMenuIds(updateItems);

            // 중단 설정시 validation 체크
            if(menuOptions.isStopOnError()) {
                if(updateMenuIdList.size() != updateMenuIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 요청에서 중복된 메뉴ID가 있습니다.");
                }
                if(!updateMenuCompositeKeyList.isEmpty() && updateMenuCompositeKeyList.size() != updateMenuCompositeKeySet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 요청에서 중복된 메뉴명+메뉴타입 조합이 있습니다.");
                }
                if(updateMenuIdList.size() != existingUpdateMenuIdList.size()) {
                    Set<Integer> nonExistentIdSet = new HashSet<>(updateMenuIdSet);
                    nonExistentIdSet.removeAll(existingUpdateMenuIdSet);
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("UPDATE: 데이터베이스에 존재하지 않는 메뉴ID가 있습니다", new ArrayList<>(nonExistentIdSet)));
                }
                if(!conflictMenuList.isEmpty()) {
                    List<String> conflictKeys = conflictMenuList.stream()
                            .map(menu -> menu.getMenuName() + "+" + menu.getMenuType())
                            .collect(Collectors.toList());
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("UPDATE: 데이터베이스에 충돌되는 메뉴명+메뉴타입 조합이 있습니다", conflictKeys));
                }
            }
        }

        // === DELETE 검증 및 처리 ===
        if(!deleteItems.isEmpty()) {
            // 요청한 menuId 목록
            List<Integer> deleteMenuIdList = deleteItems.stream()
                    .map(MenuUpsertItem::getMenuId)
                    .collect(Collectors.toList());
            Set<Integer> deleteMenuIdSet = new HashSet<>(deleteMenuIdList);

            // 중복 체크용 데이터
            List<Integer> existingDeleteMenuIdList = menuMapper.selectExistMenuIdListByMenuIds(deleteMenuIdList);

            // 중단 설정시 validation 체크
            if(menuOptions.isStopOnError()) {
                if(deleteMenuIdList.size() != deleteMenuIdSet.size()) {
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 요청에서 중복된 메뉴ID가 있습니다.");
                }
                if(deleteMenuIdList.size() != existingDeleteMenuIdList.size()) {
                    Set<Integer> nonExistentIdSet = new HashSet<>(deleteMenuIdSet);
                    nonExistentIdSet.removeAll(new HashSet<>(existingDeleteMenuIdList));
                    throw new BusinessException(ErrorCode.VALIDATION_FAILED, formatDuplicateErrorMessage("DELETE: 데이터베이스에 존재하지 않는 메뉴ID가 있습니다", new ArrayList<>(nonExistentIdSet)));
                }
            }
        }

        // 실제 처리할 데이터 객체들
        List<Menu> toInsert = new ArrayList<>();
        List<Menu> toUpdate = new ArrayList<>();
        List<Menu> toDelete = new ArrayList<>();

        // INSERT 처리
        for(MenuUpsertItem item : insertItems) {
            try {
                Menu newMenu = createMenuFromItem(item, currentUserId, now);
                toInsert.add(newMenu);
            } catch(Exception e) {
                log.error("Error processing insert item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "INSERT_PROCESSING_ERROR");
                if (menuOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "INSERT 처리 중 오류가 발생했습니다");
                }
            }
        }

        // UPDATE 처리
        for(MenuUpsertItem item : updateItems) {
            try {
                if(item.getMenuId() == null) {
                    result.addError(item.getTempId(), "UPDATE 작업에는 menuId가 필요합니다", "MISSING_MENU_ID");
                    if(menuOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "UPDATE 작업에는 menuId가 필요합니다");
                    }
                } else {
                    Menu updatedMenu = updateMenuFromItem(item, currentUserId, now);
                    toUpdate.add(updatedMenu);
                }
            } catch(Exception e) {
                log.error("Error processing update item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "UPDATE_PROCESSING_ERROR");
                if (menuOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "UPDATE 처리 중 오류가 발생했습니다");
                }
            }
        }

        // DELETE 처리 (Soft Delete - delYn = false로 변경)
        for(MenuUpsertItem item : deleteItems) {
            try {
                if (item.getMenuId() == null) {
                    result.addError(item.getTempId(), "DELETE 작업에는 menuId가 필요합니다", "MISSING_MENU_ID");
                    if (menuOptions.isStopOnError()) {
                        throw new BusinessException(ErrorCode.VALIDATION_FAILED, "DELETE 작업에는 menuId가 필요합니다");
                    }
                } else {
                    Menu softDeleteMenu = softDeleteMenuFromItem(item, currentUserId, now);
                    toDelete.add(softDeleteMenu);
                }
            } catch(Exception e) {
                log.error("Error processing delete item: {}", item, e);
                result.addError(item.getTempId(), e.getMessage(), "DELETE_PROCESSING_ERROR");
                if (menuOptions.isStopOnError()) {
                    throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "DELETE 처리 중 오류가 발생했습니다");
                }
            }
        }

        // 데이터베이스 작업 수행
        try {
            int insertCount = 0, updateCount = 0, deleteCount = 0;

            if (!toInsert.isEmpty()) {
                insertCount = menuMapper.insertMenusBulk(toInsert);
                result.incrementSuccess(insertCount);
                log.info("Bulk insert completed: {} items", insertCount);
            }

            if (!toUpdate.isEmpty()) {
                updateCount = menuMapper.updateMenusBulk(toUpdate);
                result.incrementSuccess(updateCount);
                log.info("Bulk update completed: {} items", updateCount);
            }

            if (!toDelete.isEmpty()) {
                deleteCount = menuMapper.softDeleteMenusBulk(toDelete);
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

    private Menu createMenuFromItem(MenuUpsertItem item, Integer currentUserId, LocalDateTime now) {
        Menu menu = new Menu(item.getMenuName(), item.getMenuType(), item.getMenuPath(), 
                            item.getParentId(), item.getMenuOrder());
        menu.setMenuId(item.getMenuId());
        menu.setDescription(item.getDescription());
        menu.setDelYn(item.getDelYn());
        menu.setRegId(currentUserId);
        menu.setChgId(currentUserId);
        menu.setRegDt(now);
        menu.setChgDt(now);
        return menu;
    }

    private Menu updateMenuFromItem(MenuUpsertItem item, Integer currentUserId, LocalDateTime now) {
        Menu menu = new Menu();
        menu.setMenuId(item.getMenuId());
        menu.setMenuName(item.getMenuName());
        menu.setMenuType(item.getMenuType());
        menu.setDescription(item.getDescription());
        menu.setParentId(item.getParentId());
        menu.setMenuPath(item.getMenuPath());
        menu.setMenuOrder(item.getMenuOrder());
        menu.setDelYn(item.getDelYn());
        menu.setChgId(currentUserId);
        menu.setChgDt(now);

        // Only clear del fields when activating
        if (item.getDelYn() != null && item.getDelYn().equals("N")) {
            menu.setDelId(null);
            menu.setDelDt(null);
        }
        // When deactivating or no change, don't touch del fields (preserve existing DB values)

        return menu;
    }

    private Menu softDeleteMenuFromItem(MenuUpsertItem item, Integer currentUserId, LocalDateTime now) {
        Menu menu = new Menu();
        menu.setMenuId(item.getMenuId());
        menu.setDelYn("Y");
        menu.setChgId(currentUserId);
        menu.setChgDt(now);
        menu.setDelId(currentUserId);
        menu.setDelDt(now);

        return menu;
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
            return "menu_order"; // 기본 정렬 컬럼
        }
        
        return switch (sortBy.toLowerCase()) {
            case "menuname" -> "menu_name";
            case "menutype" -> "menu_type";
            case "menupath" -> "menu_path";
            case "menuorder" -> "menu_order";
            case "parentid" -> "parent_id";
            case "description" -> "description";
            case "delYn" -> "del_yn";
            case "credt" -> "reg_dt";
            case "chgdt" -> "chg_dt";
            case "deldt" -> "del_dt";
            default -> null; // null 반환 시 기본 정렬 사용
        };
    }
}
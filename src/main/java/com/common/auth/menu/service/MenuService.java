package com.common.auth.menu.service;


import com.common.auth.common.util.SecurityUtil;
import com.common.auth.menu.domain.Menu;
import com.common.auth.menu.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {
    //----- DI Fields -----//
    private final MenuMapper menuMapper;

    public Optional<Menu> findById(Integer menuId) {
        log.debug("Finding menu by ID: {}", menuId);
        return menuMapper.selectMenuByMenuId(menuId);
    }

    public List<Menu> findAll() {
        log.debug("Finding all menus");
        return menuMapper.selectAllMenus();
    }

    public List<Menu> findByParentId(Integer parentId) {
        log.debug("Finding menus by parent ID: {}", parentId);
        return menuMapper.selectMenusByParentMenuId(parentId);
    }

    public List<Menu> findRootMenus() {
        log.debug("Finding root menus");
        return menuMapper.selectRootMenus();
    }

    public List<Menu> findByRoleId(Integer roleId) {
        log.debug("Finding menus for role ID: {}", roleId);
        return menuMapper.selectMenusByRoleId(roleId);
    }

    public List<Menu> findByMemberId(Integer memberId) {
        log.debug("Finding menus for user ID: {}", memberId);
        return menuMapper.selectMenusByMemberId(memberId);
    }

    public List<Map<String, Object>> getMenuTreeByMemberId(Integer memberId) {
        log.debug("Getting menu tree for user ID: {}", memberId);
        return menuMapper.selectMenuTreeByMemberId(memberId);
    }

    public List<Map<String, Object>> getMenusByMemberId(Integer memberId, boolean hierarchical) {
        log.debug("Getting menus for user ID: {}, hierarchical: {}", memberId, hierarchical);
        
        List<Map<String, Object>> flatMenus = menuMapper.selectMenuTreeMapByMemberId(memberId);

        if (!hierarchical) {
            // Return flat structure sorted by display_order
            return flatMenus.stream()
                .sorted((m1, m2) -> {
                    // First sort by menu_type (HEADER first, then MENU)
                    String type1 = (String) m1.get("menuType");
                    String type2 = (String) m2.get("menuType");
                    
                    if ("HEADER".equals(type1) && "MENU".equals(type2)) return -1;
                    if ("MENU".equals(type1) && "HEADER".equals(type2)) return 1;
                    
                    // Then sort by display_order
                    Integer order1 = (Integer) m1.get("menuOrder");
                    Integer order2 = (Integer) m2.get("menuOrder");
                    if (order1 == null) order1 = 0;
                    if (order2 == null) order2 = 0;
                    
                    return order1.compareTo(order2);
                })
                .collect(Collectors.toList());
        }
        
        // Convert to hierarchical structure
        return buildHierarchicalStructure(flatMenus);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildHierarchicalStructure(List<Map<String, Object>> flatMenus) {
        long t0 = System.currentTimeMillis();

        // 0) 입력 가드 + 1차 진단
        if (flatMenus == null) {
            log.error("[MENUS] flatMenus is NULL (DB/Mapper 문제 가능).");
            return Collections.emptyList();
        }
        log.info("[MENUS] input size={}, firstRowKeys={}",
                flatMenus.size(),
                flatMenus.isEmpty() ? "[]" : (flatMenus.get(0) == null ? "null-row" : flatMenus.get(0).keySet()));

        // 0-1) 분포/샘플 키 진단
        if (!flatMenus.isEmpty()) {
            int sampleLimit = Math.min(3, flatMenus.size());
            for (int i = 0; i < sampleLimit; i++) {
                Map<String, Object> r = flatMenus.get(i);
                if (r == null) { log.warn("[MENUS] sample[{}]=null", i); }
                else {
                    log.debug("[MENUS] sample[{}] keys={}, menuId={}, parentId={}, menuOrder={}",
                            i, r.keySet(), r.get("menuId"), r.get("parentId"), r.get("menuOrder"));
                }
            }
        }

        // 1) menuMap 구성 (항상 items 생성). 중복/누락 진단
        Map<String, Map<String, Object>> menuMap = new LinkedHashMap<>();
        int nullRows = 0, noIdRows = 0, duplicateIds = 0;
        Set<String> seenIds = new HashSet<>();

        for (int i = 0; i < flatMenus.size(); i++) {
            Map<String, Object> row = flatMenus.get(i);
            if (row == null) { nullRows++; continue; }

            String menuId = (row.get("menuId") == null) ? null : String.valueOf(row.get("menuId"));
            if (menuId == null || menuId.isBlank()) {
                noIdRows++;
                log.warn("[MENUS] row[{}] without menuId, keys={}", i, row.keySet());
                continue;
            }

            if (!seenIds.add(menuId)) {
                duplicateIds++;
                log.warn("[MENUS] DUPLICATE menuId detected: {} (rowIdx={})", menuId, i);
            }

            Map<String, Object> copy = new HashMap<>(row);
            copy.put("items", new ArrayList<Map<String, Object>>()); // NPE 방지
            menuMap.put(menuId, copy);
        }

        log.info("[MENUS] built menuMap size={}, nullRows={}, noIdRows={}, duplicateIds={}",
                menuMap.size(), nullRows, noIdRows, duplicateIds);

        if (menuMap.isEmpty()) {
            log.error("[MENUS] menuMap is EMPTY (SQL alias 또는 필터 문제 가능: AS \"menuId\"/\"parentId\"/\"menuOrder\"를 확인).");
            return Collections.emptyList();
        }

        // 2) 트리 빌드 (부모 없음/미존재 → 고아로 집계, 필요 시 root로 승격)
        List<Map<String, Object>> roots = new ArrayList<>();
        int orphansParentNull = 0, orphansParentMissing = 0, attached = 0;

        for (int i = 0; i < flatMenus.size(); i++) {
            Map<String, Object> row = flatMenus.get(i);
            if (row == null) continue;

            String id = (row.get("menuId") == null) ? null : String.valueOf(row.get("menuId"));
            if (id == null || id.isBlank()) continue;

            String parentId = (row.get("parentId") == null) ? null : String.valueOf(row.get("parentId"));
            Map<String, Object> me = menuMap.get(id);
            if (me == null) {
                // 방어: 이건 이론상 없어야 함 (위에서 다 넣음)
                log.warn("[MENUS] row[{}] id={} exists in input but not in menuMap", i, id);
                continue;
            }

            if (parentId == null || parentId.isBlank()) {
                orphansParentNull++;
                roots.add(me);
            } else {
                Map<String, Object> parent = menuMap.get(parentId);
                if (parent == null) {
                    // 고아: parentId가 있으나 parent 미존재
                    orphansParentMissing++;
                    log.warn("[MENUS] dangling parentId: {} for id={} (rowIdx={}) → treating as root", parentId, id, i);
                    roots.add(me);
                } else {
                    List<Map<String, Object>> items = (List<Map<String, Object>>) parent.get("items");
                    if (items == null) { // 극단 방어
                        items = new ArrayList<>();
                        parent.put("items", items);
                        log.warn("[MENUS] parent had null items list (parentId={}), created new list.", parentId);
                    }
                    items.add(me);
                    attached++;
                }
            }
        }

        log.info("[MENUS] attach stats: roots(by null parent)={}, roots(by missing parent)={}, attachedToParent={}",
                orphansParentNull, orphansParentMissing, attached);

        // 3) 정렬 (menuOrder → menuId 보조) + 하위 재귀 정렬
        Comparator<Map<String, Object>> byOrderThenId = Comparator
                .comparingInt((Map<String, Object> m) -> toInt(m.get("menuOrder"), Integer.MAX_VALUE))
                .thenComparing((Map<String, Object> m) -> String.valueOf(m.get("menuId")));

        sortRecursively(roots, byOrderThenId);


        long took = System.currentTimeMillis() - t0;
        log.info("[MENUS] hierarchy built: rootCount={}, took={}ms", roots.size(), took);
        if (roots.isEmpty()) {
            log.warn("[MENUS] NO ROOT NODES. parentId가 모두 존재하지만 parent 레코드를 못 찾은 케이스인지 확인 필요 (쿼리/조인/필터 확인).");
        }

        // 4) 최종 구조 일부 프린트 (깊이1만)
        int limit = Math.min(5, roots.size());
        for (int i = 0; i < limit; i++) {
            Map<String, Object> r = roots.get(i);
            List<Map<String, Object>> items = (List<Map<String, Object>>) r.get("items");
            log.debug("[MENUS] root[{}] id={}, name={}, order={}, childCount={}",
                    i, r.get("menuId"), r.get("menuName"), r.get("menuOrder"), (items == null ? 0 : items.size()));
        }

        return roots;
    }

    private static int toInt(Object o, int def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return def; }
    }

    @SuppressWarnings("unchecked")
    private static void sortRecursively(List<Map<String, Object>> nodes, Comparator<Map<String, Object>> comp) {
        nodes.sort(comp);
        for (Map<String, Object> n : nodes) {
            List<Map<String, Object>> items = (List<Map<String, Object>>) n.get("items");
            if (items != null && !items.isEmpty()) sortRecursively(items, comp);
        }
    }


    public Menu createMenu(String menuName, String menuType, String menuPath, Integer parentId, Integer menuOrder) {
        log.info("Creating new menu: {}", menuName);

        Menu menu = new Menu(menuName, menuType, menuPath, parentId, menuOrder);
        menuMapper.insertMenu(menu);
        
        log.info("Successfully created menu: {}", menuName);
        return menu;
    }

    public Menu updateMenu(Integer menuId, String menuName, String menuType, String menuPath, Integer parentId, Integer menuOrder, boolean delYn) {
        log.info("Updating menu with ID: {}", menuId);
        
        Optional<Menu> existingMenu = menuMapper.selectMenuByMenuId(menuId);
        if (existingMenu.isEmpty()) {
            throw new IllegalArgumentException("Menu not found with ID: " + menuId);
        }

        Menu menu = existingMenu.get();
        menu.setMenuName(menuName);
        menu.setMenuType(menuType);
        menu.setMenuPath(menuPath);
        menu.setParentId(parentId);
        menu.setMenuOrder(menuOrder);
        menu.setDelYn("N");

        int updatedRows = menuMapper.updateMenu(menu);
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update menu with ID: " + menuId);
        }

        log.info("Successfully updated menu with ID: {}", menuId);
        return menu;
    }

    public void deleteMenu(Integer menuId) {
        log.info("Deleting menu with ID: {}", menuId);
        
        int deletedRows = menuMapper.deleteMenuByMenuId(menuId);
        if (deletedRows == 0) {
            throw new IllegalArgumentException("Menu not found with ID: " + menuId);
        }

        log.info("Successfully deleted menu with ID: {}", menuId);
    }

    public void deactivateMenu(Integer menuId) {
        log.info("Deactivating menu with ID: {}", menuId);
        updateMenuStatus(menuId, false);
    }

    public void activateMenu(Integer menuId) {
        log.info("Activating menu with ID: {}", menuId);
        updateMenuStatus(menuId, true);
    }

    private void updateMenuStatus(Integer menuId, boolean delYn) {
        Optional<Menu> existingMenu = menuMapper.selectMenuByMenuId(menuId);
        if (existingMenu.isEmpty()) {
            throw new IllegalArgumentException("Menu not found with ID: " + menuId);
        }

        Menu menu = existingMenu.get();
        menu.setDelYn("N");
        menu.setChgId(SecurityUtil.getCurrentMemberId());

        if (!delYn) {
            menu.setDelId(SecurityUtil.getCurrentMemberId());
            menu.setDelDt(LocalDateTime.now());
        } else {
            menu.setDelId(null);
            menu.setDelDt(null);
        }

        int updatedRows = menuMapper.updateMenu(menu);
        if (updatedRows == 0) {
            throw new RuntimeException("Failed to update menu status for ID: " + menuId);
        }
    }

    public int getMenuCount() {
        return menuMapper.selectCountMenus();
    }
}
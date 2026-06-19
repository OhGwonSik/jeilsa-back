package com.common.auth.menu.mapper;

import com.common.auth.menu.domain.Menu;
import com.common.auth.menu.dto.MenuGridFilterDTO;
import com.common.auth.menu.dto.MenuResponse;
import com.common.auth.menu.dto.MenuUpsertItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mapper
public interface MenuMapper {
    Optional<Menu> selectMenuByMenuId(@Param("menuId") Integer menuId);
    
    List<Menu> selectAllMenus();
    
    List<Menu> selectMenusByParentMenuId(@Param("parentId") Integer parentId);
    
    List<Menu> selectRootMenus();
    
    List<Menu> selectMenusByRoleId(@Param("roleId") Integer roleId);
    
    List<Menu> selectMenusByMemberId(@Param("userId") Integer userId);

    List<Map<String, Object>> selectMenuTreeByMemberId(@Param("memberId") Integer memberId);
    
    void insertMenu(Menu menu);
    
    int updateMenu(Menu menu);
    
    int deleteMenuByMenuId(@Param("menuId") Integer menuId);
    
    int selectCountMenus();
    
    // Grid 관련 메서드
    List<MenuResponse> selectMenusWithFilter(@Param("filter") MenuGridFilterDTO filter);

    List<MenuResponse> selectMenuHeadersWithFilter(@Param("filter") MenuGridFilterDTO filter);
    
    List<MenuResponse> selectMenuHeadersItemsWithFilter(
        @Param("parentIds") List<Integer> parentIds, 
        @Param("filter") MenuGridFilterDTO filter
    );

    void insertMenusBatch(@Param("menus") List<Menu> menus);
    
    void updateMenusBatch(@Param("menus") List<Menu> menus);
    
    // Bulk operations - 중복 체크용 메서드들
    List<Integer> selectExistMenuIdListByMenuIds(@Param("menuIds") List<Integer> menuIds);
    
    List<Menu> selectExistMenuListByMenuNamesAndMenuTypes(@Param("items") List<MenuUpsertItem> items);
    
    List<Menu> selectMenuListByMenuNamesAndMenuTypesAndNotInMenuIds(@Param("items") List<MenuUpsertItem> items);
    
    // Bulk operations - CRUD 메서드들
    int insertMenusBulk(@Param("menus") List<Menu> menus);
    
    int updateMenusBulk(@Param("menus") List<Menu> menus);
    
    int deleteMenusBulk(@Param("menus") List<Menu> menus);
    
    int softDeleteMenusBulk(@Param("menus") List<Menu> menus);
    
    /**
     * 주어진 헤더 메뉴 ID들에 대한 모든 하위 메뉴를 재귀적으로 조회합니다.
     * @param headerIds 조회할 헤더 메뉴 ID 목록
     * @return 모든 하위 메뉴 목록 (재귀적으로 조회된 모든 자식 메뉴 포함)
     */
    List<MenuResponse> selectAllDescendantMenus(@Param("headerIds") List<Integer> headerIds);

    List<Map<String, Object>> selectMenuTreeMapByMemberId(@Param("memberId") Integer memberId);
}
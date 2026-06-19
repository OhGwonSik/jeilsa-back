package com.common.auth.rolemenu.mapper;

import java.util.List;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.common.auth.rolemenu.domain.RoleMenu;
import com.common.auth.rolemenu.dto.RoleMenuGridFilterDTO;
import com.common.auth.rolemenu.dto.RoleMenuResponse;

@Mapper
public interface RoleMenuMapper {
    
    List<RoleMenu> findByRoleId(@Param("roleId") Integer roleId);
    
    List<RoleMenu> findByMenuId(@Param("menuId") Integer menuId);
    
    List<RoleMenu> findAllRoleMenus();

    List<RoleMenuResponse> selectRoleMenusWithFilter(@Param("filter") RoleMenuGridFilterDTO filter);

    List<RoleMenuResponse> selectRoleMenuHeadersWithFilter(@Param("filter") RoleMenuGridFilterDTO filter);

    List<RoleMenuResponse> selectAllDescendantRoleMenus(@Param("menuIds") List<Integer> menuIds);
    
    void insert(RoleMenu roleMenu);
    
    int insertRoleMenusBulk(@Param("roleMenus") List<RoleMenu> roleMenus);
    
    int updateRoleMenusBulk(@Param("roleMenus") List<RoleMenu> roleMenus);

    int deleteByRoleId(@Param("roleId") Integer roleId);
    
    int deleteByMenuId(@Param("menuId") Integer menuId);
    
    int deleteByRoleIdAndMenuId(@Param("roleId") Integer roleId, @Param("menuId") Integer menuId);
    
    int deleteByRoleIdAndMenuIdBatch(@Param("roleIdMenuIdList") List<Integer> roleIdMenuIdList);

    void deleteRoleMenuByRoleIdAndMenuId(@Param("roleId") Integer roleId, @Param("menuId") Integer menuId);

    boolean existsByRoleIdAndMenuId(@Param("roleId") Integer roleId, @Param("menuId") Integer menuId);

    List<RoleMenu> selectExistingRoleMenusByCompositeKeys(@Param("roleMenus") List<RoleMenu> roleMenus);
    
    int softDeleteRoleMenusBulk(@Param("roleMenus") List<RoleMenu> roleMenus);
}
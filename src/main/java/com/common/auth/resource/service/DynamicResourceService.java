package com.common.auth.resource.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.stream.Collectors;

import com.jeil.delivery.domain.MemberDTO;
import org.springframework.stereotype.Service;

import com.common.auth.common.exception.BusinessException;
import com.common.auth.common.exception.ErrorCode;
import com.common.auth.menu.domain.Menu;
import com.common.auth.menu.dto.MenuResponse;
import com.common.auth.menu.mapper.MenuMapper;
import com.common.auth.permission.domain.Permission;
import com.common.auth.permission.mapper.PermissionMapper;
import com.common.auth.resource.dto.DynamicResourceResponse;
import com.common.auth.role.domain.Role;
import com.common.auth.role.dto.RoleResponse;
import com.common.auth.role.mapper.RoleMapper;
import com.common.auth.user.dto.UserResponse;
import com.common.auth.user.mapper.UserMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DynamicResourceService {
    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final MenuMapper menuMapper;

    public DynamicResourceService(UserMapper userMapper,
                                 RoleMapper roleMapper,
                                 PermissionMapper permissionMapper,
                                 MenuMapper menuMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.menuMapper = menuMapper;
    }

    public DynamicResourceResponse getUserResources(Integer memberId) {
        log.debug("Fetching user resources for userId: {}", memberId);
        
        // 사용자 존재 확인
        Optional<MemberDTO> userOpt = userMapper.selectUserByMemberId(memberId);
        if (userOpt.isEmpty()) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다: " + memberId);
        }
        
        MemberDTO memberDTO = userOpt.get();
        DynamicResourceResponse response = new DynamicResourceResponse();
        
        // 사용자 정보 설정
        response.setUserInfo(new UserResponse(memberDTO));
        
        // 사용자별 역할 조회
        List<Role> userRoles = roleMapper.selectRolesByMemberId(memberId);
        response.setUserRoles(userRoles.stream()
                .map(RoleResponse::from)
                .collect(Collectors.toList()));
        
        // 사용자별 권한 조회 (역할을 통한 권한)
        List<String> userPermissions = getUserPermissions(memberId);
        response.setUserPermissions(userPermissions);
        
        // 사용자별 메뉴 조회
        List<Menu> userMenus = menuMapper.selectMenusByMemberId(memberId);
        response.setUserMenus(userMenus.stream()
                .map(menu -> new MenuResponse(menu))
                .collect(Collectors.toList()));
        
        // 사용자별 메뉴 트리 구성
        List<DynamicResourceResponse.MenuTreeNode> menuTree = buildMenuTree(memberId);
        response.setMenuTree(menuTree);
        
        // 추가 메타데이터
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("roleCount", userRoles.size());
        metadata.put("permissionCount", userPermissions.size());
        metadata.put("menuCount", userMenus.size());
        metadata.put("lastLogin", memberDTO.getLastLoginDt());
        metadata.put("userStatus", memberDTO.getMemberStatusCd());
        response.setMetadata(metadata);
        
        log.debug("User resources fetched - Roles: {}, Permissions: {}, Menus: {}", 
                userRoles.size(), userPermissions.size(), userMenus.size());
        
        return response;
    }

    public List<RoleResponse> getUserRoles(Integer memberId) {
        log.debug("Fetching user roles for userId: {}", memberId);
        
        List<Role> roles = roleMapper.selectRolesByMemberId(memberId);
        return roles.stream()
                .map(RoleResponse::from)
                .collect(Collectors.toList());
    }

    public List<String> getUserPermissions(Integer memberId) {
        log.debug("Fetching user permissions for userId: {}", memberId);
        
        List<Permission> permissions = permissionMapper.selectPermissionsByMemberId(memberId);
        return permissions.stream()
                .map((Permission permission) -> permission.getPermissionId().toString() + ":" + permission.getPermissionName())
                .collect(Collectors.toList());
    }

    public List<MenuResponse> getUserMenus(Integer memberId) {
        log.debug("Fetching user menus for userId: {}", memberId);
        
        List<Menu> menus = menuMapper.selectMenusByMemberId(memberId);
        return menus.stream()
                .map(menu -> new MenuResponse(menu))
                .collect(Collectors.toList());
    }

    public List<DynamicResourceResponse.MenuTreeNode> getUserMenuTree(Integer memberId) {
        log.debug("Building user menu tree for userId: {}", memberId);
        
        return buildMenuTree(memberId);
    }

    private List<DynamicResourceResponse.MenuTreeNode> buildMenuTree(Integer memberId) {
        // 사용자별 메뉴 트리 데이터 조회
        List<Map<String, Object>> menuTreeData = menuMapper.selectMenuTreeByMemberId(memberId);
        
        // Map을 MenuTreeNode로 변환
        List<DynamicResourceResponse.MenuTreeNode> allNodes = menuTreeData.stream()
                .map(this::mapToMenuTreeNode)
                .collect(Collectors.toList());
        
        // 루트 노드들만 필터링 (parentId가 null인 노드들)
        List<DynamicResourceResponse.MenuTreeNode> rootNodes = allNodes.stream()
                .filter(node -> node.getParentId() == null)
                .collect(Collectors.toList());
        
        // 각 루트 노드에 하위 노드들을 재귀적으로 추가
        for (DynamicResourceResponse.MenuTreeNode rootNode : rootNodes) {
            buildChildNodes(rootNode, allNodes);
        }
        
        return rootNodes;
    }

    private void buildChildNodes(DynamicResourceResponse.MenuTreeNode parentNode, 
                                List<DynamicResourceResponse.MenuTreeNode> allNodes) {
        List<DynamicResourceResponse.MenuTreeNode> items = allNodes.stream()
                .filter(node -> parentNode.getMenuId().equals(node.getParentId()))
                .collect(Collectors.toList());
        
        parentNode.setItems(items);
        
        // 재귀적으로 하위 노드들의 items도 설정
        for (DynamicResourceResponse.MenuTreeNode item : items) {
            buildChildNodes(item, allNodes);
        }
    }

    private DynamicResourceResponse.MenuTreeNode mapToMenuTreeNode(Map<String, Object> menuData) {
        DynamicResourceResponse.MenuTreeNode node = new DynamicResourceResponse.MenuTreeNode();
        
        node.setMenuId((Integer) menuData.get("menuId"));
        node.setParentId((Integer) menuData.get("parentId"));
        node.setMenuName((String) menuData.get("menuName"));
        node.setMenuType((String) menuData.get("menuType"));
        node.setMenuPath((String) menuData.get("menuPath"));
        node.setMenuOrder((Integer) menuData.get("menuOrder"));
        node.setDelYn((String) menuData.get("delYn"));
        node.setItems(new ArrayList<>());
        
        return node;
    }
}
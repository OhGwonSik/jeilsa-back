package com.common.auth.rolepermission.dto;

import jakarta.validation.constraints.NotNull;


public class CreateRolePermissionRequest {
    
    @NotNull(message = "Role ID is required")
    private Integer roleId;
    
    @NotNull(message = "Permission ID is required")
    private Integer permissionId;
    
    public CreateRolePermissionRequest() {
    }
    
    public CreateRolePermissionRequest(Integer roleId, Integer permissionId) {
        this.roleId = roleId;
        this.permissionId = permissionId;
    }
    
    public Integer getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
    
    public Integer getPermissionId() {
        return permissionId;
    }
    
    public void setPermissionId(Integer permissionId) {
        this.permissionId = permissionId;
    }
    
    @Override
    public String toString() {
        return "CreateRolePermissionRequest{" +
                "roleId=" + roleId +
                ", permissionId=" + permissionId +
                '}';
    }
}
package com.common.auth.rolepermission.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BatchRolePermissionRequest {
    
    @NotNull(message = "Role ID is required")
    private Integer roleId;
    
    @NotEmpty(message = "Permission IDs are required")
    private List<@Valid Integer> permissionIds;
    
    public BatchRolePermissionRequest() {
    }
    
    public BatchRolePermissionRequest(Integer roleId, List<Integer> permissionIds) {
        this.roleId = roleId;
        this.permissionIds = permissionIds;
    }
    
    public Integer getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
    
    public List<Integer> getPermissionIds() {
        return permissionIds;
    }
    
    public void setPermissionIds(List<Integer> permissionIds) {
        this.permissionIds = permissionIds;
    }
    
    @Override
    public String toString() {
        return "BatchRolePermissionRequest{" +
                "roleId=" + roleId +
                ", permissionIds=" + permissionIds +
                '}';
    }
}
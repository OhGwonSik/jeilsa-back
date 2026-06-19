package com.common.auth.rolepermission.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class BatchUpdateRolePermissionRequest {
    
    @NotEmpty(message = "Update requests are required")
    private List<@Valid RolePermissionUpdateItem> updates;
    
    public BatchUpdateRolePermissionRequest() {
    }
    
    public BatchUpdateRolePermissionRequest(List<RolePermissionUpdateItem> updates) {
        this.updates = updates;
    }
    
    public List<RolePermissionUpdateItem> getUpdates() {
        return updates;
    }
    
    public void setUpdates(List<RolePermissionUpdateItem> updates) {
        this.updates = updates;
    }
    
    @Override
    public String toString() {
        return "BatchUpdateRolePermissionRequest{" +
                "updates=" + updates +
                '}';
    }
    
    public static class RolePermissionUpdateItem {
        
        @NotNull(message = "Role ID is required")
        private Integer roleId;
        
        @NotNull(message = "Permission ID is required")
        private Integer permissionId;
        
        private String delYn;
        
        public RolePermissionUpdateItem() {
        }
        
        public RolePermissionUpdateItem(Integer roleId, Integer permissionId, String delYn) {
            this.roleId = roleId;
            this.permissionId = permissionId;
            this.delYn = delYn;
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
        
        public String getDelYn() {
            return delYn;
        }
        
        public void setDelYn(String delYn) {
            this.delYn = delYn;
        }
        
        @Override
        public String toString() {
            return "RolePermissionUpdateItem{" +
                    "roleId=" + roleId +
                    ", permissionId=" + permissionId +
                    ", delYn=" + delYn +
                    '}';
        }

    }
}
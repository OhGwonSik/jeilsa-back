package com.common.auth.rolepermission.dto;

public class UpdateRolePermissionRequest {
    
    private String delYn;
    
    public UpdateRolePermissionRequest() {
    }
    
    public UpdateRolePermissionRequest(String delYn) {
        this.delYn = delYn;
    }
    
    public String getDelYn() {
        return delYn;
    }
    
    public void setDelYn(String delYn) {
        this.delYn = delYn;
    }
    
    @Override
    public String toString() {
        return "UpdateRolePermissionRequest{" +
                "delYn=" + delYn +
                '}';
    }
}
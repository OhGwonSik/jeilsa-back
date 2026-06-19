package com.common.auth.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateRoleRequest {
    
    @NotBlank(message = "Role name is required")
    @Size(max = 255, message = "Role name must be less than 255 characters")
    private String roleName;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
    
    private Boolean isDefault;
    
    private String delYn;
    
    public UpdateRoleRequest() {
    }
    
    public UpdateRoleRequest(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }
    
    public UpdateRoleRequest(String roleName, String description, Boolean isDefault, String delYn) {
        this.roleName = roleName;
        this.description = description;
        this.isDefault = isDefault;
        this.delYn = delYn;
    }
    
    public String getRoleName() {
        return roleName;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Boolean getIsDefault() {
        return isDefault;
    }
    
    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    public String getDelYn() {
        return delYn;
    }
    
    public void setDelYn(String delYn) {
        this.delYn = delYn;
    }
    
    @Override
    public String toString() {
        return "UpdateRoleRequest{" +
                "roleName='" + roleName + '\'' +
                ", description='" + description + '\'' +
                ", isDefault=" + isDefault +
                ", delYn=" + delYn +
                '}';
    }
}
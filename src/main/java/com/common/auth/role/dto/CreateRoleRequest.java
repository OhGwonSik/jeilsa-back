package com.common.auth.role.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoleRequest {
    @NotBlank(message = "Role name is required")
    @Size(max = 255, message = "Role name must be less than 255 characters")
    private String roleName;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
    
    private Boolean isDefault = false;
        
    public CreateRoleRequest(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }
}
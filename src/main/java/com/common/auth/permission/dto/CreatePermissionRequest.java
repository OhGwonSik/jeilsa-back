package com.common.auth.permission.dto;

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
public class CreatePermissionRequest {
    //----- Fields -----//
    @NotBlank(message = "Permission name is required")
    @Size(max = 255, message = "Permission name must be less than 255 characters")
    private String permissionName;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
}
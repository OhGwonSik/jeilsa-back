package com.common.auth.organization.dto;

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
public class UpdateOrganizationRequest {
    //----- Fields -----//
    @NotBlank(message = "Organization name is required")
    @Size(max = 255, message = "Organization name must be less than 255 characters")
    private String organizationName;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
}
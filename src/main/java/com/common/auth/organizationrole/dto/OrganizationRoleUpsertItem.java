package com.common.auth.organizationrole.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 부서-역할 upsert 항목
 */
@Getter
@Setter
@ToString
@Schema(description = "부서-역할 upsert 항목")
public class OrganizationRoleUpsertItem {
    //----- Fields -----//
    @Schema(description = "임시 ID (클라이언트에서 생성)")
    private String tempId;
    
    @Schema(description = "작업 상태 (ADDED/CHANGED/DELETED)")
    @NotBlank(message = "작업 상태는 필수입니다")
    private String status;
    
    @Schema(description = "조직 ID")
    @NotNull(message = "조직 ID는 필수입니다")
    private Integer organizationId;
    
    @Schema(description = "조직명")
    private String organizationName;
    
    @Schema(description = "역할 ID")
    @NotNull(message = "역할 ID는 필수입니다")
    private Integer roleId;
    
    @Schema(description = "역할명")
    private String roleName;
    
    @Schema(description = "활성 상태")
    private String delYn = "N";

    public String getUniqueKey() {
        return organizationId + ":" + roleId;
    }
}
